package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PlmUnderstandBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public String src;

    public Integer getN() {
        return n;
    }
}
