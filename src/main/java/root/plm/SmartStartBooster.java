package root.plm;

import org.springframework.stereotype.Component;
import root.entity.plm.LlmWord;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;

import java.util.List;

/**
 * 더 빠른 초기 시드 구성을 위한 도구
 */
@Component
public class SmartStartBooster {

    int contextPoint(List<PlmContext> contextList, int left, int right, boolean space) {
        return contextList.stream().filter(StaticUtil.getContextFinder(left, right)).mapToInt(item -> space ? item.space : item.cnt).sum();
    }
    public Toke rightContext(Toke target, LlmWord left, LlmWord right, List<PlmContext> contextList, List<LlmWordCompound> compoundList, List<LlmWord> wordList, boolean space) {
        target.rightContext += contextPoint(contextList, left.getN(), right.getN(), space);
        if(left.getType().equals("0") && right.getType().equals("조사") && !right.getN().equals(191)) target.rightContext--;
        if(space && right.getType().equals("어미")) target.rightContext--;
        if(!space) {
            final boolean leftWrapA = left.getType().equals("무엇") || left.getType().equals("대명사");
            if(leftWrapA && right.getType().equals("조사")) target.rightContext++;
            if(leftWrapA && right.getType().equals("1")) target.rightContext--;
            if(!left.getN().equals(StaticUtil.opener) && right.getType().equals("감탄사")) target.rightContext--;
        }
        target.rightContext *= left.getWord().length() + target.getWord().length();
        compoundList.stream()
                .filter(item -> right.getN().equals(item.word))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, left, StaticUtil.selectWord(compound.getLeftword(), wordList), contextList, compoundList, wordList, space));
        compoundList.stream()
                .filter(item -> left.getN().equals(item.word))
                .findAny()
                .ifPresent(compound ->
                        rightContext(target, StaticUtil.selectWord(compound.getRightword(), wordList), right, contextList, compoundList, wordList, space));
        return target;
    }
}
