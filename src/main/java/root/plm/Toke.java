package root.plm;

import root.entity.plm.LlmWord;

public class Toke extends LlmWord {
    final LlmWord src;
    final int start;
    final int end;
    final boolean rightSpace;

    public Toke(LlmWord llmWord, int start, int end, boolean rightSpace) {
        src = llmWord;
        this.start = start;
        this.end = end;
        this.rightSpace = rightSpace;
        this.word = src.getWord();
        type = src.getType();
        memo = src.getMemo();
    }

    @Override
    public Integer getN() {
        return src.getN();
    }
}
