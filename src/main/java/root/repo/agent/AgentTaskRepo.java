package root.repo.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.agent.AgentTask;

public interface AgentTaskRepo extends JpaRepository<AgentTask, Integer> {
}
