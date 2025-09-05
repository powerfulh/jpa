package root.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmLearn;
import root.repo.plm.LlmWordCompoundRepo;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmLearnRepo;
import root.repo.plm.PlmSrcBoxRepo;

import java.util.stream.Collectors;

@Service
public class PlmCore {
    final LlmWordRepo llmWordRepo;
    final PlmLearnRepo plmLearnRepo;
    final LlmWordCompoundRepo llmWordCompoundRepo;
    final PlmSrcBoxRepo plmSrcBoxRepo;
    final ReplaceRepeatedChars replaceRepeatedChars;

    final String symbolType = "기호";

    public PlmCore(LlmWordRepo llmWordRepo, PlmLearnRepo plmLearnRepo, LlmWordCompoundRepo llmWordCompoundRepo, PlmSrcBoxRepo plmSrcBoxRepo, ReplaceRepeatedChars replaceRepeatedChars) {
        this.llmWordRepo = llmWordRepo;
        this.plmLearnRepo = plmLearnRepo;
        this.llmWordCompoundRepo = llmWordCompoundRepo;
        this.plmSrcBoxRepo = plmSrcBoxRepo;
        this.replaceRepeatedChars = replaceRepeatedChars;
    }

    void learn(String w, String src, String rightword, String type) {
        var nw = new LearnWord();
        nw.set(w, type);
        var l = new PlmLearn();
        l.word = llmWordRepo.save(LlmWord.to(nw)).getN();
        l.src = src;
        l.rightword = rightword;
        l.value = w;
        plmLearnRepo.save(l);
    }
    /**
     * @return Token has been solved?
     */
    boolean learnCouple(String leftword, String rightword, String src) {
        if(!llmWordRepo.findAllByWord(leftword).isEmpty()) {
            learn(leftword.concat(rightword), src, rightword, "학습 결합");
            return true;
        }
        return false;
    }
    void learn(String item, String input) {
        var w = llmWordRepo.findAllByWord(item);
        if(w.isEmpty()) {
            String target = item;
            String cutter = null;
            nextCut: for (int ii = 0; ii < item.length() - 1; ii++) {
                int cut = item.length() - 1 - ii;
                String current = item.substring(cut);
                w = llmWordRepo.findAllByWord(current);
                if(w.isEmpty()) continue;
                for (var wi: w) {
                    for (var rw: llmWordCompoundRepo.findByRightword(wi.getN())) {
                        var c = llmWordRepo.findById(rw.word).orElseThrow().getWord();
                        if(item.endsWith(c)) {
                            target = item.substring(0, item.length() - c.length());
                            if(learnCouple(target, c, input)) return;
                            ii += c.length() - wi.getWord().length();
                            continue nextCut;
                        }
                    }
                }
                if(w.size() == 1 && w.get(0).getType().equals(symbolType)) {
                    learn(item.substring(0, cut), input);
                    return;
                }
                target = item.substring(0, cut);
                if(learnCouple(target, current, input)) return;
                cutter = current;
            }
            learn(target, input, cutter, "학습");
        }
    }
    @Transactional
    public void learn(String input) {
        var symbols = llmWordRepo.findByType(symbolType).stream().map(LlmWord::getWord).collect(Collectors.joining()).toCharArray();
        String[] cleanInput = replaceRepeatedChars.replaceRepeatedChars(input, symbols).split(" ");
        for(int i = 0; i < cleanInput.length; i++) {
            String item = cleanInput[i];
            int next = i + 1;
            // '고' 로 끝났다는 것은 '고' 로 시작하는 공백을 포함한 토큰일 가능성이 있다 250905
            if(item.charAt(item.length() - 1) == '고') {
                String compoundNext = item.concat(" ").concat(cleanInput[next]);
                boolean spacyToken = llmWordRepo.findByWordStartingWith("고 ").stream()
                        .anyMatch(gi -> compoundNext.contains(gi.getWord()));
                if(spacyToken) {
                    learn(compoundNext, input);
                    ++i;
                    return;
                }
            }
            learn(item, input);
        }
    }
    public void learnSrcBox() {
        plmSrcBoxRepo.findAll().forEach(item -> learn(item.src));
    }
}

class LearnWord extends LlmWord {
    void set(String lw, String t) {
        word = lw;
        type = t;
    }
}