package enigma.dal.repository;

import enigma.dal.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    List<SessionEntity> findByMachineName(String machineName);
}
