package enigma.sessions.service;

import enigma.sessions.model.MachineDefinition;
import enigma.shared.spec.MachineSpec;

import java.util.List;

public interface MachineCatalogService {

    MachineDefinition loadMachine(String xmlPath);

    List<MachineDefinition> listMachines();

    MachineDefinition resolveMachine(String machineName);

    MachineSpec resolveMachineSpec(String machineName);

    void clearRuntimeMetadata();
}
