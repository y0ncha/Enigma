package enigma.sessions.service.impl;

import enigma.dal.entity.MachineEntity;
import enigma.dal.entity.MachineReflectorEntity;
import enigma.dal.entity.MachineRotorEntity;
import enigma.dal.entity.ReflectorId;
import enigma.dal.repository.MachineReflectorRepository;
import enigma.dal.repository.MachineRotorRepository;
import enigma.dal.repository.MachineRepository;
import enigma.loader.Loader;
import enigma.loader.LoaderXml;
import enigma.loader.exception.EnigmaLoadingException;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.model.MachineDefinition;
import enigma.sessions.service.MachineCatalogService;
import enigma.shared.alphabet.Alphabet;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MachineCatalogServiceImpl implements MachineCatalogService {

    private final MachineRepository machineRepository;
    private final MachineRotorRepository machineRotorRepository;
    private final MachineReflectorRepository machineReflectorRepository;
    private final Loader loader;
    private final Map<String, MachineRuntimeMetadata> runtimeMetadataByMachineName;

    public MachineCatalogServiceImpl(MachineRepository machineRepository,
                                     MachineRotorRepository machineRotorRepository,
                                     MachineReflectorRepository machineReflectorRepository) {
        this.machineRepository = machineRepository;
        this.machineRotorRepository = machineRotorRepository;
        this.machineReflectorRepository = machineReflectorRepository;
        this.loader = new LoaderXml();
        this.runtimeMetadataByMachineName = new ConcurrentHashMap<>();
    }

    @Override
    @Transactional
    public MachineDefinition loadMachine(String xmlPath) {
        if (xmlPath == null || xmlPath.trim().isEmpty()) {
            throw new ApiValidationException("xmlPath must be provided");
        }

        MachineSpec machineSpec;
        try {
            machineSpec = loader.loadSpecs(xmlPath.trim());
        }
        catch (EnigmaLoadingException e) {
            throw new ApiValidationException(e.getMessage(), e);
        }

        String machineName = machineSpec.getMachineName();
        if (machineRepository.existsByName(machineName)) {
            throw new ConflictException("Machine name already exists: " + machineName);
        }

        UUID machineId = UUID.randomUUID();
        Instant loadedAt = Instant.now();
        MachineDefinition definition = new MachineDefinition(machineId, machineName, xmlPath.trim(), loadedAt);

        try {
            MachineEntity machine = machineRepository.save(new MachineEntity(
                    machineId,
                    machineName,
                    machineSpec.getRotorsInUse(),
                    machineSpec.getAlphabet()
            ));

            saveRotors(machine, machineSpec);
            saveReflectors(machine, machineSpec);
        }
        catch (DataIntegrityViolationException e) {
            throw new ConflictException("Machine name already exists: " + machineName);
        }

        runtimeMetadataByMachineName.put(machineName, new MachineRuntimeMetadata(definition.xmlPath(), definition.loadedAt()));
        return definition;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDefinition> listMachines() {
        List<MachineDefinition> items = machineRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toDefinition)
                .toList();
        return List.copyOf(items);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineDefinition resolveMachine(String machineName) {
        MachineEntity entity = resolveMachineEntity(machineName);
        return toDefinition(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineSpec resolveMachineSpec(String machineName) {
        MachineEntity entity = resolveMachineEntity(machineName);
        return toMachineSpec(entity);
    }

    @Override
    public void clearRuntimeMetadata() {
        runtimeMetadataByMachineName.clear();
    }

    private void saveRotors(MachineEntity machine, MachineSpec machineSpec) {
        List<MachineRotorEntity> rotors = new ArrayList<>();
        for (RotorSpec rotorSpec : machineSpec.rotorsById().values()) {
            rotors.add(new MachineRotorEntity(
                    UUID.randomUUID(),
                    machine,
                    rotorSpec.id(),
                    rotorSpec.notchIndex() + 1,
                    new String(rotorSpec.getRightColumn()),
                    new String(rotorSpec.getLeftColumn())
            ));
        }
        machineRotorRepository.saveAll(rotors);
    }

    private void saveReflectors(MachineEntity machine, MachineSpec machineSpec) {
        List<MachineReflectorEntity> reflectors = new ArrayList<>();
        Alphabet alphabet = machineSpec.alphabet();

        for (ReflectorSpec reflectorSpec : machineSpec.reflectorsById().values()) {
            String input = alphabet.letters();
            String output = toOutputWiring(alphabet, reflectorSpec.mapping());

            reflectors.add(new MachineReflectorEntity(
                    UUID.randomUUID(),
                    machine,
                    ReflectorId.fromDbValue(reflectorSpec.id()),
                    input,
                    output
            ));
        }
        machineReflectorRepository.saveAll(reflectors);
    }

    private String toOutputWiring(Alphabet alphabet, int[] mapping) {
        StringBuilder output = new StringBuilder(mapping.length);
        for (int mappedIndex : mapping) {
            output.append(alphabet.charAt(mappedIndex));
        }
        return output.toString();
    }

    private MachineDefinition toDefinition(MachineEntity entity) {
        MachineRuntimeMetadata metadata = runtimeMetadataByMachineName.get(entity.getName());
        String xmlPath = metadata == null ? null : metadata.xmlPath();
        Instant loadedAt = metadata == null ? null : metadata.loadedAt();

        return new MachineDefinition(
                entity.getId(),
                entity.getName(),
                xmlPath,
                loadedAt
        );
    }

    private MachineEntity resolveMachineEntity(String machineName) {
        if (machineName == null || machineName.trim().isEmpty()) {
            throw new ApiValidationException("machineName must be provided");
        }

        String key = machineName.trim();
        return machineRepository.findByName(key)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + key));
    }

    private MachineSpec toMachineSpec(MachineEntity machine) {
        String machineName = machine.getName();
        Alphabet alphabet;
        try {
            alphabet = new Alphabet(machine.getAbc());
        }
        catch (IllegalArgumentException e) {
            throw new ConflictException("Stored alphabet is invalid for machine '" + machineName + "'");
        }

        List<MachineRotorEntity> rotorEntities = machineRotorRepository.findByMachine_IdOrderByRotorIdAsc(machine.getId());
        if (rotorEntities.isEmpty()) {
            throw new ConflictException("Machine '" + machineName + "' has no rotors in storage");
        }

        Map<Integer, RotorSpec> rotorsById = new LinkedHashMap<>();
        for (MachineRotorEntity rotorEntity : rotorEntities) {
            Integer rotorId = rotorEntity.getRotorId();
            Integer notch = rotorEntity.getNotch();
            String right = rotorEntity.getWiringRight();
            String left = rotorEntity.getWiringLeft();

            if (rotorId == null || rotorId <= 0) {
                throw new ConflictException("Machine '" + machineName + "' has rotor with invalid ID");
            }
            if (notch == null || notch <= 0 || notch > alphabet.size()) {
                throw new ConflictException("Machine '" + machineName + "' has rotor " + rotorId + " with invalid notch");
            }
            if (right == null || left == null || right.length() != alphabet.size() || left.length() != alphabet.size()) {
                throw new ConflictException("Machine '" + machineName + "' has rotor " + rotorId + " with invalid wiring size");
            }

            RotorSpec rotorSpec = new RotorSpec(
                    rotorId,
                    notch - 1,
                    right.toCharArray(),
                    left.toCharArray()
            );
            if (rotorsById.putIfAbsent(rotorId, rotorSpec) != null) {
                throw new ConflictException("Machine '" + machineName + "' has duplicate rotor ID: " + rotorId);
            }
        }

        validateRotorIdsContiguous(machineName, rotorsById);

        List<MachineReflectorEntity> reflectorEntities = machineReflectorRepository.findByMachine_IdOrderByReflectorIdAsc(machine.getId());
        if (reflectorEntities.isEmpty()) {
            throw new ConflictException("Machine '" + machineName + "' has no reflectors in storage");
        }

        Map<String, ReflectorSpec> reflectorsById = new LinkedHashMap<>();
        for (MachineReflectorEntity reflectorEntity : reflectorEntities) {
            if (reflectorEntity.getReflectorId() == null) {
                throw new ConflictException("Machine '" + machineName + "' has reflector with missing ID");
            }
            String reflectorId = reflectorEntity.getReflectorId().toDbValue();
            ReflectorSpec reflectorSpec = new ReflectorSpec(
                    reflectorId,
                    toReflectorMapping(machineName, reflectorId, alphabet, reflectorEntity)
            );

            if (reflectorsById.putIfAbsent(reflectorId, reflectorSpec) != null) {
                throw new ConflictException("Machine '" + machineName + "' has duplicate reflector ID: " + reflectorId);
            }
        }

        Integer rotorsInUse = machine.getRotorsCount();
        if (rotorsInUse == null || rotorsInUse <= 0 || rotorsInUse > rotorsById.size()) {
            throw new ConflictException("Machine '" + machineName + "' has invalid rotorsCount");
        }

        return new MachineSpec(
                alphabet,
                rotorsById,
                reflectorsById,
                rotorsInUse,
                machineName
        );
    }

    private void validateRotorIdsContiguous(String machineName, Map<Integer, RotorSpec> rotorsById) {
        int expectedId = 1;
        for (Integer rotorId : rotorsById.keySet()) {
            if (rotorId != expectedId) {
                throw new ConflictException("Machine '" + machineName + "' has non-contiguous rotor IDs in storage");
            }
            expectedId++;
        }
    }

    private int[] toReflectorMapping(String machineName,
                                     String reflectorId,
                                     Alphabet alphabet,
                                     MachineReflectorEntity reflectorEntity) {
        String input = reflectorEntity.getInput();
        String output = reflectorEntity.getOutput();
        int alphabetSize = alphabet.size();
        if (input == null || output == null || input.length() != alphabetSize || output.length() != alphabetSize) {
            throw new ConflictException("Machine '" + machineName + "' has reflector " + reflectorId + " with invalid wiring size");
        }

        int[] mapping = new int[alphabetSize];
        Arrays.fill(mapping, -1);
        for (int i = 0; i < alphabetSize; i++) {
            int inputIndex = alphabet.indexOf(input.charAt(i));
            int outputIndex = alphabet.indexOf(output.charAt(i));
            if (inputIndex < 0 || outputIndex < 0) {
                throw new ConflictException("Machine '" + machineName + "' has reflector "
                        + reflectorId + " with symbols outside alphabet");
            }
            if (mapping[inputIndex] != -1) {
                throw new ConflictException("Machine '" + machineName + "' has reflector "
                        + reflectorId + " with duplicate input mapping");
            }
            mapping[inputIndex] = outputIndex;
        }

        for (int index = 0; index < alphabetSize; index++) {
            int mapped = mapping[index];
            if (mapped < 0) {
                throw new ConflictException("Machine '" + machineName + "' has reflector "
                        + reflectorId + " with incomplete mapping");
            }
            if (mapped == index || mapping[mapped] != index) {
                throw new ConflictException("Machine '" + machineName + "' has reflector "
                        + reflectorId + " with non-symmetric mapping");
            }
        }
        return mapping;
    }

    private record MachineRuntimeMetadata(String xmlPath, Instant loadedAt) {
    }
}
