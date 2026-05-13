package root.repo.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import root.entity.agent.AgentChange;

import java.util.List;

public interface AgentChangeRepo extends JpaRepository<AgentChange, Integer> {
    List<AgentChange> findByTaskOrderByNDesc(int task);
    List<AgentChange> findByTaskOrderByNAsc(int task);
    @Transactional
    void deleteByTask(int task);
}
