package root.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LlmWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    String word;
    String type;

    public Integer getN() {
        return n;
    }

    public String getWord() {
        return word;
    }

    public String getType() {
        return type;
    }
}
