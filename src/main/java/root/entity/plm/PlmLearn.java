package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PlmLearn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public int word;
    public String src;
}
