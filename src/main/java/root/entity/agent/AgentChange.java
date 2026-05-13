package root.entity.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class AgentChange {
    public static final String WORD = "WORD";
    public static final String COMPOUND = "COMPOUND";
    public static final String CONTEXT = "CONTEXT";
    public static final String CONTEXT_CNT = "CONTEXT_CNT";
    public static final String CONTEXT_SPACE = "CONTEXT_SPACE";
    public static final String ULTRON_SENTENCE = "ULTRON_SENTENCE";
    public static final String ULTRON_CONTEXT = "ULTRON_CONTEXT";
    public static final String QA = "QA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer n;
    public int task;
    public String op;
    @Column(name = "entity_n")
    public Integer entityN;
    public Integer prev;
    @CreationTimestamp
    public LocalDateTime createdAt;

    public AgentChange() {}

    public AgentChange(int task, String op, Integer entityN, Integer prev) {
        this.task = task;
        this.op = op;
        this.entityN = entityN;
        this.prev = prev;
    }
}
