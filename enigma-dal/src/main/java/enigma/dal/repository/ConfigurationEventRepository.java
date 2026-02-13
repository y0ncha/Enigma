package enigma.dal.repository;

import enigma.dal.entity.ConfigurationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigurationEventRepository extends JpaRepository<ConfigurationEventEntity, Long> {

    List<ConfigurationEventEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<ConfigurationEventEntity> findByMachineNameOrderByCreatedAtAsc(String machineName);
}
