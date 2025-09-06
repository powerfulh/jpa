package root.plm;

import jakarta.transaction.Transactional;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;
import root.repo.plm.PlmContextRepo;

import java.util.ArrayList;
import java.util.List;

public class Sentence extends ArrayList<LlmWord> {
    public Sentence(List<LlmWord> list) {
        super(list);
    }

    @Transactional
    public void learnContext(PlmContextRepo plmContextRepo) {
        for (int i = 0; i < size() - 1; i++) {
            int left = get(i).getN();
            int right = get(i + 1).getN();
            var contextList = plmContextRepo.findAll();
            var context = contextList.stream().filter(item -> item.getLeftword() == left && item.getRightword() == right).findAny().orElse(null);
            if(context == null) {
                context = new PlmContext(); // 실시간 조정과는 다르게 0에서 출발
                context.leftword = left;
                context.rightword = right;
                plmContextRepo.save(context);
            } else context.cnt++;
        }
    }
}
