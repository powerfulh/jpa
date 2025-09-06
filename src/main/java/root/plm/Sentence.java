package root.plm;

import root.entity.plm.LlmWord;

import java.util.ArrayList;
import java.util.List;

public class Sentence extends ArrayList<LlmWord> {
    public Sentence(List<LlmWord> list) {
        super(list);
    }
}
