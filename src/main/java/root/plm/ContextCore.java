package root.plm;

import org.springframework.stereotype.Component;
import root.plm.entity.Compound;
import root.plm.entity.Context;
import root.plm.entity.Word;
import root.repo.plm.dsl.Repo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ContextCore {
    final String afterType = "어미";
    final String supportType = "cutter";
    final String zeroType = "0";
    final String thingType = "무엇";
    final List<Integer> issue29except = List.of(2506, 105, 3876);

    final Repo dsl;
    final Set<Integer> suffix;

    public ContextCore(Repo dsl) {
        this.dsl = dsl;
        suffix = new HashSet<>(dsl.selectSuffix());
    }

    int contextPoint(List<Context> contextList, int left, int right, boolean space, List<Integer> history) {
        history.add(right);
        return contextList.stream().filter(StaticUtil.getContextFinder(left, right)).mapToInt(item -> space ? item.getSpace() : item.getCnt()).sum();
    }
    /**
     * 더 빠른 초기 시드 구성을 위한 조정
     */
    void smartStartBoost(Word left, Word right, Toke target, boolean space, boolean otherOption, boolean leftBonus) {
        if(left.getType().equals(zeroType) && right.getType().equals(supportType) && !right.getN().equals(191)) target.rightContext--;
        if(space && right.getType().equals(afterType)) target.rightContext--;
        if(!space) {
            if(left.getType().equals(afterType) && (right.getType().equals(supportType) || right.getType().equals(zeroType)) && !issue29except.contains(right.getN()) && otherOption) throw new PlmException("Maybe wrong", left.getWord() + "+" + target.getWord());
            if(left.getType().equals(afterType) && right.getType().equals(thingType)) target.rightContext--;
            final boolean leftWrapA = left.getType().equals(thingType) || left.getType().equals("대명사");
            if(leftWrapA && right.getType().equals(supportType)) target.rightContext++;
            if(leftWrapA && right.getType().equals("1")) target.rightContext--;
            if(!left.getN().equals(StaticUtil.opener) && right.getType().equals("감탄사")) target.rightContext--;
            if(left.getType().equals(zeroType) && right.getType().equals(thingType)) target.rightContext--;
        }
        if(leftBonus) target.rightContext++;
    }
    public void rightContext(Toke target, Word left, Word right, List<Context> contextList, List<Compound> compoundList, Dict wordList, boolean space, boolean otherOption, int leftCompoundLength) {
        target.contextHistory.computeIfAbsent(left.getN(), k -> new ArrayList<>());
        var h = target.contextHistory.get(left.getN());
        if(!h.isEmpty() && h.stream().anyMatch(item -> item.equals(right.getN()))) return;
        target.rightContext += contextPoint(contextList, left.getN(), right.getN(), space, h);
        smartStartBoost(left, right, target, space, target.rightContext < 1 && otherOption, left.getWord().length() < leftCompoundLength);
        target.rightContext *= left.getWord().length() + target.getWord().length();
        compoundList.stream()
                .filter(item -> right.getN().equals(item.getWord()))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, left, wordList.get(compound.getLeftword()), contextList, compoundList, wordList, space, false, 0));
        compoundList.stream()
                .filter(item -> left.getN().equals(item.getWord()))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, wordList.get(compound.getRightword()), right, contextList, compoundList, wordList, space, false, left.getWord().length()));
    }

    public Toke step2(Toke target, boolean space) {
        if(target.rightContext == 0) {
            if (!space && suffix.contains(target.getN())) target.rightContext++;
            else target.rightContext = target.getWord().length() - 1; // 끝까지 문맥이 없는 경우 길이가 긴 것이라도 고르게 하는 최종 조치
        }
        return target;
    }
}
