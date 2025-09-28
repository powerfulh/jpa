package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import root.plm.entity.Compound;

@Entity
public class LlmWordCompound implements Compound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    int word;
    int rightword;
    int leftword;

    public int getRightword() {
        return rightword;
    }

    public int getLeftword() {
        return leftword;
    }

    public int getWord() {
        return word;
    }
}
