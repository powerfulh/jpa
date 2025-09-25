import java.util.ArrayList;
import java.util.List;

public class FastTest {
    final int SBase = 0xAC00;

    final int none = 0;
    final int complete = 4; // ㄴ
    final int going = 8; // ㄹ
    final int respect = 17; // ㅂ
    final int past = 20; // ㅆ

    char addFooter(char target, int footerIndex) {
        return (char) (target + footerIndex);
    }
    int helpable(char ch) {
        final int SLast = 0xD7A3;
        if ((int) ch < SBase || (int) ch > SLast) {
            // 완성형 한글 음절이 아님
            return 0;
        }
        int SIndex = (int) ch - SBase;
        int footer = SIndex % 28;       // 0이면 받침 없음
        int jungIndex = (SIndex / 28) % 21; // 0..20 (ㅏ..ㅣ)
        if(footer == none) {
            switch (jungIndex) {
                case 0: // ㅏ
                    return 1;
                case 20: // ㅣ
                    return 2;
            }
        } else if(footer == 17) {
            if(jungIndex != 8) return 3; // 8: 'ㅗ' 는 존나 규칙이 없음 (고운 좁은 고와 좁아)
        }
        return 0;
    }
    char changeMother(char target, int motherIndex) {
        int SIndex = target - SBase;
        int choIndex = SIndex / (21 * 28); // 초성 인덱스
        return (char) (SBase + (choIndex * 21 + motherIndex) * 28);
    }
    char removeFooter(char target) {
        return (char) (target - ((target - SBase) % 28));
    }
    List<HelpResult> help(char target) {
        List<HelpResult> list = new ArrayList<>();
        switch (helpable(target)) {
            case 1:
                list.add(new HelpResult(215, addFooter(target, complete) + "다"));
                list.add(new HelpResult(48, String.valueOf(addFooter(target, going))));
                list.add(new HelpResult(3039, addFooter(target, complete) + "다고"));
                list.add(new HelpResult(3053, addFooter(target, going) + "게"));
                list.add(new HelpResult(309, String.valueOf(addFooter(target, complete))));
                list.add(new HelpResult(2912, addFooter(target, going) + "지"));
                list.add(new HelpResult(3069, addFooter(target, going) + "까"));
                list.add(new HelpResult(3437, addFooter(target, complete) + "다는"));
                list.add(new HelpResult(318, addFooter(target, respect) + "니다"));
                break;
            case 2:
                list.add(new HelpResult(184, String.valueOf(changeMother(target, 6))));
                list.add(new HelpResult(162, String.valueOf(addFooter(changeMother(target, 6), past))));
                list.add(new HelpResult(50, changeMother(target, 6) + "서"));
                list.add(new HelpResult(48, String.valueOf(addFooter(target, going))));
                list.add(new HelpResult(3053, addFooter(target, going) + "게"));
                list.add(new HelpResult(309, String.valueOf(addFooter(target, complete))));
                list.add(new HelpResult(215, addFooter(target, complete) + "다"));
                break;
            case 3:
                list.add(new HelpResult(83, removeFooter(target) + "운데"));
                list.add(new HelpResult(105, removeFooter(target) + "움"));
                list.add(new HelpResult(184, removeFooter(target) + "워"));
                list.add(new HelpResult(309, removeFooter(target) + "운"));
                list.add(new HelpResult(50, removeFooter(target) + "워서"));
                break;
        }
        return list;
    }
    public static void main(String[] args) {
        var comp = new FastTest();
        var c = List.of('가', '서', '키', '돕', '깝', '눕');
        c.forEach(item -> System.out.println(comp.help(item).stream().map(HelpResult::word).toList()));
    }
}

record HelpResult(int n, String word) {}