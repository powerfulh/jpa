package root.plm;

import root.entity.plm.LlmWord;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;

import java.util.Comparator;
import java.util.List;

public class MindMap {
    LlmWord word;
    List<MindMap> next;
    LlmWord left;

    LlmWord selectWord(int n, List<LlmWord> data) {
        return data.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
    }
    void nextMap(List<PlmContext> list, List<LlmWord> wordList, List<LlmWordCompound> compoundList) {
        int max = list.stream().filter(item -> item.getLeftword() == left.getN()).map(PlmContext::getCnt).max(Comparator.naturalOrder()).orElse(0);
        if(max > 0) {
            next = list.stream()
                    .filter(item -> item.getLeftword() == left.getN() && item.getCnt() == max)
                    .map(item -> {
                        var comp = compoundList.stream()
                                .filter(ci -> ci.getLeftword() == item.getLeftword() && ci.getRightword() == item.getRightword())
                                .findAny().orElse(null);
                        return new MindMap(item.getRightword(), list, wordList, compoundList, comp == null ? null : selectWord(comp.word, wordList));
                    }).toList();
        }
    }
    MindMap(int n, List<PlmContext> list, List<LlmWord> wordList, List<LlmWordCompound> compoundList, LlmWord left) {
        word = selectWord(n, wordList);
        this.left = left == null ? word : left;
        nextMap(list, wordList, compoundList);
    }

    public LlmWord getWord() {
        return word;
    }

    public List<MindMap> getNext() {
        return next;
    }
}
