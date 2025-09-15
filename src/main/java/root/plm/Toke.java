package root.plm;

import root.entity.plm.LlmWord;

public class Toke extends LlmWord {
    final LlmWord src;
    final int start;
    final int end;

    public Toke(LlmWord llmWord, int start, int end) {
        src = llmWord;
        this.start = start;
        this.end = end;
        this.word = src.getWord();
    }
}
