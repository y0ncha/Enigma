package enigma.dal.repository;

import enigma.dal.entity.ProcessRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessRecordRepository extends JpaRepository<ProcessRecordEntity, UUID> {

    List<ProcessRecordEntity> findBySessionIdOrderByIdAsc(String sessionId);

    List<ProcessRecordEntity> findByMachine_IdOrderByIdAsc(UUID machineId);
}
