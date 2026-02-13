package enigma.dal.repository;

import enigma.dal.entity.MachineRotorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MachineRotorRepository extends JpaRepository<MachineRotorEntity, UUID> {

    List<MachineRotorEntity> findByMachine_IdOrderByRotorIdAsc(UUID machineId);

    void deleteByMachine_Id(UUID machineId);
}
