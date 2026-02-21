package enigma.dal.repository;

import enigma.dal.entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<MachineEntity, UUID> {

    Optional<MachineEntity> findByName(String name);

    boolean existsByName(String name);
}
