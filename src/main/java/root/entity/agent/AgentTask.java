package root.entity.agent;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class AgentTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer n;
    public Integer word;
    public String request;
    public String response;
    @CreationTimestamp
    public LocalDateTime createdAt;
}
