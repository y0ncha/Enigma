package enigma.sessions.service.impl;

import enigma.dal.repository.MachineReflectorRepository;
import enigma.dal.repository.MachineRepository;
import enigma.dal.repository.MachineRotorRepository;
import enigma.dal.repository.ProcessRecordRepository;
import enigma.sessions.service.MachineCatalogService;
import enigma.sessions.service.MaintenanceService;
import enigma.sessions.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private final SessionService sessionService;
    private final MachineCatalogService machineCatalogService;
    private final ProcessRecordRepository processRecordRepository;
    private final MachineRotorRepository machineRotorRepository;
    private final MachineReflectorRepository machineReflectorRepository;
    private final MachineRepository machineRepository;

    public MaintenanceServiceImpl(SessionService sessionService,
                                  MachineCatalogService machineCatalogService,
                                  ProcessRecordRepository processRecordRepository,
                                  MachineRotorRepository machineRotorRepository,
                                  MachineReflectorRepository machineReflectorRepository,
                                  MachineRepository machineRepository) {
        this.sessionService = sessionService;
        this.machineCatalogService = machineCatalogService;
        this.processRecordRepository = processRecordRepository;
        this.machineRotorRepository = machineRotorRepository;
        this.machineReflectorRepository = machineReflectorRepository;
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public void clearStorage() {
        sessionService.clearSessions();
        processRecordRepository.deleteAll();
        machineRotorRepository.deleteAll();
        machineReflectorRepository.deleteAll();
        machineRepository.deleteAll();
        machineCatalogService.clearRuntimeMetadata();
    }
}
