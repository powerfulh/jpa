package root.plm;

import root.entity.plm.LlmWord;

public class Toke extends LlmWord {
    final LlmWord src;
    final int start;
    final int end;

    public Toke(LlmWord word, int start, int end) {
        src = word;
        this.start = start;
        this.end = end;
        this.word = src.getWord();
    }
}
