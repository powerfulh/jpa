package root.plm;

import root.entity.plm.LlmWord;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MindMap {
    LlmWord word;
    List<MindMap> next;
    LlmWord left;

    MindMap(int n, List<PlmContext> list, List<LlmWord> wordList, List<LlmWordCompound> compoundList, LlmWord left, List<Integer> history) {
        word = selectWord(n, wordList);
        this.left = left == null ? word : left;
        history.add(n);
        nextMap(list, wordList, compoundList, history);
    }
    LlmWord selectWord(int n, List<LlmWord> data) {
        return data.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
    }
    void nextMap(List<PlmContext> list, List<LlmWord> wordList, List<LlmWordCompound> compoundList, List<Integer> history) {
        next = list.stream().filter(item -> item.getLeftword() == left.getN())
                .sorted(Comparator.comparing(item -> {
                    int cnt = item.getCnt();
                    for (int h: history) {
                        var hc = list.stream().filter(ci -> ci.getLeftword() == h && ci.getRightword() == item.getRightword()).findAny().orElse(null);
                        cnt += hc == null ? 0 : hc.getCnt();
                    }
                    return cnt;
                }))
                .map(item -> {
                    var comp = compoundList.stream()
                            .filter(ci -> ci.getLeftword() == item.getLeftword() && ci.getRightword() == item.getRightword())
                            .findAny().orElse(null);
                    return new MindMap(item.getRightword(), list, wordList, compoundList, comp == null ? null : selectWord(comp.word, wordList), history);
                }).toList();
    }
    static MindMap make(int n, List<PlmContext> list, List<LlmWord> wordList, List<LlmWordCompound> compoundList) {
        ArrayList<Integer> history = new ArrayList<>();
        return new MindMap(n, list, wordList, compoundList, null, history);
    }

    public LlmWord getWord() {
        return word;
    }

    public List<MindMap> getNext() {
        return next;
    }
}