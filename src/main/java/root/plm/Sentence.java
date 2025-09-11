package root.plm;

import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;
import root.entity.plm.PlmUnderstandBox;
import root.entity.plm.PlmUnderstandBoxWord;
import root.repo.plm.PlmContextRepo;
import root.repo.plm.UnderstandBoxWordRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sentence extends ArrayList<LlmWord> {
    final int contextPoint;

    public Sentence(List<LlmWord> list, PlmContextRepo plmContextRepo) {
        super(list);
        var contextList = plmContextRepo.findAll();
        var openerContext = contextList.stream().filter(StaticUtil.getContextFinder(2903, get(0).getN())).findAny().orElse(null);
        int p = openerContext == null ? 0 : openerContext.cnt;
        for (int i = 0; i < size() - 1; i++) {
            for (int ii = i + 1; ii < size() - 1; ii++) {
                var context = getContext(i, ii, contextList);
                if(context == null) continue;
                p += context.getCnt();
            }
        }
        contextPoint = p;
    }

    PlmContext getContext(int li, int ri, List<PlmContext> list) {
        return list.stream().filter(StaticUtil.getContextFinder(get(li).getN(), get(ri).getN())).findAny().orElse(null);
    }
    public void learnContext(PlmContextRepo plmContextRepo) {
        var contextList = plmContextRepo.findAll();
        for (int i = 0; i < size() - 1; i++) {
            var context = getContext(i, i + 1, contextList);
            if(context == null) {
                context = new PlmContext(); // 실시간 조정과는 다르게 0에서 출발
                context.leftword = get(i).getN();
                context.rightword = get(i + 1).getN();
                plmContextRepo.save(context);
                contextList.add(context);
            } else context.cnt++;
        }
    }
    public int getContextPoint() {
        return contextPoint;
    }
    public Map<String, Object> getDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("point", contextPoint);
        dto.put("list", this);
        return dto;
    }
    public void box(PlmUnderstandBox box, UnderstandBoxWordRepo understandBoxWordRepo) {
        for (int i = 0; i < size(); i++) {
            var word = new PlmUnderstandBoxWord();
            word.understand = box.getN();
            word.i = i;
            word.word = get(i).getN();
            understandBoxWordRepo.save(word);
        }
    }
}
