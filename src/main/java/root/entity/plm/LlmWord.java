package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import root.plm.entity.Ntity;

@Entity
public class LlmWord implements Ntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    protected String word;
    protected String type;
    protected String memo;

    public static LlmWord to(LlmWord from) {
        var r = new LlmWord();
        r.word = from.word;
        r.type = from.type;
        return r;
    }

    public Integer getN() {
        return n;
    }

    public String getWord() {
        return word;
    }

    public String getType() {
        return type;
    }

    public String getMemo() {
        return memo;
    }
}
