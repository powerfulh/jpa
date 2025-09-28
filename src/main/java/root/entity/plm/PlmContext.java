package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.UpdateTimestamp;
import root.plm.entity.Context;

import java.time.LocalDateTime;

@Entity
public class PlmContext implements Context {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    public int cnt;
    public int leftword;
    public int rightword;
    @UpdateTimestamp
    LocalDateTime updatedDate;
    public int space;

    public int getCnt() {
        return cnt;
    }

    public int getSpace() {
        return space;
    }

    public int getLeftword() {
        return leftword;
    }

    public int getRightword() {
        return rightword;
    }

    public Integer getN() {
        return n;
    }
}
