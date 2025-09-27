package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PlmUltronContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public int sentence;
    public int context;
    public int i;
}
