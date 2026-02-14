package enigma.dal.repository;

import enigma.dal.entity.MachineReflectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MachineReflectorRepository extends JpaRepository<MachineReflectorEntity, UUID> {

    List<MachineReflectorEntity> findByMachine_IdOrderByReflectorIdAsc(UUID machineId);

    void deleteByMachine_Id(UUID machineId);
}
