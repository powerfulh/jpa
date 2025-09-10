package root.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;
import root.entity.plm.PlmLearn;
import root.exception.PlmException;
import root.plm.Sentence;
import root.plm.StaticUtil;
import root.repo.plm.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlmCore {
    final LlmWordRepo llmWordRepo;
    final PlmLearnRepo plmLearnRepo;
    final LlmWordCompoundRepo llmWordCompoundRepo;
    final PlmSrcBoxRepo plmSrcBoxRepo;
    final ReplaceRepeatedChars replaceRepeatedChars;
    final PlmContextRepo plmContextRepo;

    final String symbolType = "기호";

    public PlmCore(LlmWordRepo llmWordRepo, PlmLearnRepo plmLearnRepo, LlmWordCompoundRepo llmWordCompoundRepo, PlmSrcBoxRepo plmSrcBoxRepo, ReplaceRepeatedChars replaceRepeatedChars, PlmContextRepo plmContextRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmLearnRepo = plmLearnRepo;
        this.llmWordCompoundRepo = llmWordCompoundRepo;
        this.plmSrcBoxRepo = plmSrcBoxRepo;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.plmContextRepo = plmContextRepo;
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

    int contextPoint(List<PlmContext> contextList, int lw, int rw) {
        return contextList.stream().filter(StaticUtil.getContextFinder(lw, rw)).mapToInt(PlmContext::getCnt).sum();
    }
    Comparator<LlmWord> closerContext(List<LlmWord> understandList, List<PlmContext> contextList) {
        return Comparator.comparing(item -> understandList.stream().mapToInt(ui -> contextPoint(contextList, ui.getN(), item.getN())).sum());
    }
    void separateToken(List<LlmWord> understandList, String src, final List<LlmWord> wordList, Map<String, List<LlmWord>> failHistory, List<PlmContext> contextList, List<Sentence> sentenceList) {
        var last = wordList.stream()
                .filter(item -> item.getWord().equals(src))
                .sorted(closerContext(understandList, contextList)).toList();
        if(last.isEmpty()) {
            var h = failHistory.get(src);
            var sameList = wordList.stream()
                    .filter(item -> {
                        if(src.startsWith(item.getWord())) {
                            if(h == null) return true;
                            return h.stream().noneMatch(hi -> Objects.equals(hi.getN(), item.getN()));
                        }
                        return false;
                    })
                    .sorted(closerContext(understandList, contextList))
                    .toList();
            if(sameList.isEmpty()) {
                if (src.startsWith("고") && !src.startsWith("고 ") && src.length() > 1) {
                    String retrySrc = "고 " + src.substring(1);
                    separateToken(understandList, retrySrc, wordList, failHistory, contextList, sentenceList);
                    return;
                }
                if(understandList.isEmpty()) throw new PlmException("Fail to understand", failHistory);
                String backSrc = understandList.get(understandList.size() - 1).getWord().concat(src);
                failHistory.computeIfAbsent(backSrc, k -> new ArrayList<>());
                failHistory.get(backSrc).add(understandList.get(understandList.size() - 1));
                understandList.remove(understandList.size() - 1);
                separateToken(understandList, backSrc, wordList, failHistory, contextList, sentenceList);
                return;
            }
            if(sameList.size() > 1 && !understandList.isEmpty()) {
                sameList.subList(0, sameList.size() - 1).stream()
                        .filter(item -> contextPoint(contextList, understandList.get(understandList.size() - 1).getN(), item.getN()) > 0)
                        .forEach(item -> {
                            var clone = new ArrayList<>(understandList);
                            clone.add(item);
                            separateToken(clone, src.substring(item.getWord().length()), wordList, failHistory, contextList, sentenceList);
                        });
            }
            var current = sameList.get(sameList.size() - 1);
            understandList.add(current);
            separateToken(understandList, src.substring(current.getWord().length()), wordList, failHistory, contextList, sentenceList);
        } else {
            understandList.add(last.get(last.size() - 1));
            sentenceList.add(new Sentence(understandList, plmContextRepo));
        }
    }
    public List<Sentence> understand(String pureSrc) {
        final String src = pureSrc.replaceAll("\\s", "");
        var wordList = llmWordRepo.findAll();
        var openerList = wordList.stream().filter(item -> src.startsWith(item.getWord())).toList();
        if (openerList.isEmpty()) throw new PlmException("Fail to set the opening word", src);
        PlmException e = null;
        Map<String, List<LlmWord>> failHistory = new HashMap<>();
        List<Sentence> sentenceList = new ArrayList<>();
        var contextList = plmContextRepo.findAll();
        for (var opener: openerList) {
            List<LlmWord> understandList = new ArrayList<>();
            understandList.add(opener);
            try {
                separateToken(understandList, src.substring(opener.getWord().length()), wordList, failHistory, contextList, sentenceList);
            } catch (PlmException plmException) {
                e = plmException;
            }
        }
        if(sentenceList.isEmpty() && e != null) throw e; // 싹 다 실패한 경우 나중에는 편집 거리로 리트해봐야겠지
        sentenceList.sort(Comparator.comparing(item -> item.getContextPoint() * -1));
        return sentenceList;
    }

    @Transactional
    public void understandThenLearn(String pureSrc) {
        understand(pureSrc).get(0).learnContext(plmContextRepo);
    }
}

class LearnWord extends LlmWord {
    void set(String lw, String t) {
        word = lw;
        type = t;
    }
}