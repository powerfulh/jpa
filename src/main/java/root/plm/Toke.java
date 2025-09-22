package root.plm;

import root.entity.plm.LlmWord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Toke extends LlmWord {
    final LlmWord src;
    final int start;
    final int end;
    final boolean rightSpace;
    int rightContext; // 내가 오른쪽일때 문맥 점수
    final Map<Integer, List<Integer>> contextHistory = new HashMap<>();

    public Toke(LlmWord llmWord, int start, int end, boolean rightSpace) {
        src = llmWord;
        this.start = start;
        this.end = end;
        this.rightSpace = rightSpace;
        word = src.getWord();
        type = src.getType();
        memo = src.getMemo();
    }

    @Override
    public Integer getN() {
        return src.getN();
    }

    public boolean isRightSpace() {
        return rightSpace;
    }

    public int getRightContext() {
        return rightContext;
    }
}
