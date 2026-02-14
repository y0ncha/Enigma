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
                    machineSpec.getTotalRotors(),
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
        if (machineName == null || machineName.trim().isEmpty()) {
            throw new ApiValidationException("machineName must be provided");
        }

        String key = machineName.trim();
        MachineEntity entity = machineRepository.findByName(key)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + key));

        return toDefinition(entity);
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

    private record MachineRuntimeMetadata(String xmlPath, Instant loadedAt) {
    }
}
