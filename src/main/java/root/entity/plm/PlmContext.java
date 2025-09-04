package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PlmContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    int cnt;
    int leftword;
    int rightword;

    public int getCnt() {
        return cnt;
    }

    public int getLeftword() {
        return leftword;
    }

    public int getRightword() {
        return rightword;
    }
}
