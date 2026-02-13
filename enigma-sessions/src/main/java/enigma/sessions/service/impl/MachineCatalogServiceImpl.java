package enigma.sessions.service.impl;

import enigma.dal.entity.MachineEntity;
import enigma.dal.repository.MachineRepository;
import enigma.loader.Loader;
import enigma.loader.LoaderXml;
import enigma.loader.exception.EnigmaLoadingException;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.model.MachineDefinition;
import enigma.sessions.service.MachineCatalogService;
import enigma.shared.spec.MachineSpec;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MachineCatalogServiceImpl implements MachineCatalogService {

    private final MachineRepository machineRepository;
    private final Loader loader;
    private final Map<String, MachineDefinition> machinesByName;

    public MachineCatalogServiceImpl(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
        this.loader = new LoaderXml();
        this.machinesByName = new ConcurrentHashMap<>();
    }

    @PostConstruct
    void loadFromDb() {
        for (MachineEntity entity : machineRepository.findAll()) {
            machinesByName.putIfAbsent(
                    entity.getName(),
                    new MachineDefinition(entity.getName(), entity.getXmlPath(), entity.getLoadedAt()));
        }
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

        MachineDefinition definition = new MachineDefinition(machineName, xmlPath.trim(), Instant.now());

        try {
            machineRepository.save(new MachineEntity(
                    java.util.UUID.randomUUID(),
                    definition.machineName(),
                    machineSpec.getTotalRotors(),
                    machineSpec.getTotalReflectors(),
                    machineSpec.getAlphabet(),
                    definition.xmlPath(),
                    definition.loadedAt()
            ));
        }
        catch (DataIntegrityViolationException e) {
            throw new ConflictException("Machine name already exists: " + machineName);
        }

        machinesByName.put(machineName, definition);
        return definition;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDefinition> listMachines() {
        List<MachineDefinition> items = machineRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(entity -> new MachineDefinition(entity.getName(), entity.getXmlPath(), entity.getLoadedAt()))
                .toList();

        for (MachineDefinition item : items) {
            machinesByName.putIfAbsent(item.machineName(), item);
        }
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

        MachineDefinition resolved = new MachineDefinition(entity.getName(), entity.getXmlPath(), entity.getLoadedAt());
        machinesByName.putIfAbsent(entity.getName(), resolved);
        return resolved;
    }
}
