package enigma.sessions.service;

import enigma.sessions.model.MachineDefinition;

import java.util.List;

public interface MachineCatalogService {

    MachineDefinition loadMachine(String xmlPath);

    List<MachineDefinition> listMachines();

    MachineDefinition resolveMachine(String machineName);
}
