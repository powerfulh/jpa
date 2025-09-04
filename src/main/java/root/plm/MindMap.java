package root.plm;

import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;

import java.util.Comparator;
import java.util.List;

public class MindMap {
    LlmWord word;
    List<LlmWord> next;

    MindMap(int n, List<PlmContext> list, List<LlmWord> wordList) {
        this.word = wordList.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
        int max = list.stream().map(PlmContext::getCnt).max(Comparator.naturalOrder()).orElseThrow();
        next = list.stream()
                .filter(item -> item.getLeftword() == word.getN() && item.getCnt() == max)
                .map(item -> wordList.stream().filter(w -> w.getN() == item.getRightword()).findAny().orElseThrow()).toList();
    }

    public LlmWord getWord() {
        return word;
    }

    public List<LlmWord> getNext() {
        return next;
    }
}
