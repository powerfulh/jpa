package root.plm;

import org.springframework.stereotype.Component;
import root.entity.plm.LlmWord;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;
import root.exception.PlmException;

import java.util.ArrayList;
import java.util.List;

/**
 * 더 빠른 초기 시드 구성을 위한 도구
 */
@Component
public class SmartStartBooster {
    final String afterType = "어미";
    final String supportType = "조사";
    final String zeroType = "0";
    final List<Integer> issue29except = List.of(2506, 105, 3876);

    int contextPoint(List<PlmContext> contextList, int left, int right, boolean space, List<Integer> history) {
        history.add(right);
        return contextList.stream().filter(StaticUtil.getContextFinder(left, right)).mapToInt(item -> space ? item.space : item.cnt).sum();
    }
    public Toke rightContext(Toke target, LlmWord left, LlmWord right, List<PlmContext> contextList, List<LlmWordCompound> compoundList, List<LlmWord> wordList, boolean space, boolean otherOption) {
        target.contextHistory.computeIfAbsent(left.getN(), k -> new ArrayList<>());
        var h = target.contextHistory.get(left.getN());
        if(!h.isEmpty() && h.stream().anyMatch(item -> item.equals(right.getN()))) return null;
        target.rightContext += contextPoint(contextList, left.getN(), right.getN(), space, h);
        if(left.getType().equals(zeroType) && right.getType().equals(supportType) && !right.getN().equals(191)) target.rightContext--;
        if(space && right.getType().equals(afterType)) target.rightContext--;
        if(!space) {
            if(left.getType().equals(afterType) && (right.getType().equals(supportType) || right.getType().equals(zeroType)) && !issue29except.contains(right.getN()) && otherOption) throw new PlmException("Maybe wrong", left.getWord() + "+" + target.getWord());
            final boolean leftWrapA = left.getType().equals("무엇") || left.getType().equals("대명사");
            if(leftWrapA && right.getType().equals(supportType)) target.rightContext++;
            if(leftWrapA && right.getType().equals("1")) target.rightContext--;
            if(!left.getN().equals(StaticUtil.opener) && right.getType().equals("감탄사")) target.rightContext--;
        }
        target.rightContext *= left.getWord().length() + target.getWord().length();
        compoundList.stream()
                .filter(item -> right.getN().equals(item.word))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, left, StaticUtil.selectWord(compound.getLeftword(), wordList), contextList, compoundList, wordList, space, false));
        compoundList.stream()
                .filter(item -> left.getN().equals(item.word))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, StaticUtil.selectWord(compound.getRightword(), wordList), right, contextList, compoundList, wordList, space, false));
        return target;
    }
}
