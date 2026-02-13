package enigma.dal.repository;

import enigma.dal.entity.ProcessRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessRecordRepository extends JpaRepository<ProcessRecordEntity, Long> {

    List<ProcessRecordEntity> findBySessionIdOrderByProcessedAtAsc(UUID sessionId);

    List<ProcessRecordEntity> findByMachineNameOrderByProcessedAtAsc(String machineName);
}
