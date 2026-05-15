package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PlmUltronSentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public int opener;
    public Integer target;

    public PlmUltronSentence() {}

    public PlmUltronSentence(int word) {
        opener = word;
    }

    public Integer getN() {
        return n;
    }
}
