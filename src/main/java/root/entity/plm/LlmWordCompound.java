package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LlmWordCompound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public int word;
    int rightword;
    int leftword;

    public int getRightword() {
        return rightword;
    }

    public int getLeftword() {
        return leftword;
    }
}
