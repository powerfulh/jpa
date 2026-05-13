package root.entity.plm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import root.plm.entity.Word;

@Entity
public class LlmWord implements Word {
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

    public static LlmWord toWithMemo(LlmWord from) {
        var r = to(from);
        r.memo = from.memo;
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

    // 아래는 에이전트 용
    public void setType(String type) {
        this.type = type;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
