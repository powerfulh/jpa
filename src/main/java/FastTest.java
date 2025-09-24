import java.util.ArrayList;
import java.util.List;

public class FastTest {
    final int SBase = 0xAC00;

    final int none = 0;
    final int complete = 4; // ㄴ
    final int going = 8; // ㄹ
    final int respect = 17; // ㅂ

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
        }
        return 0;
    }
    char changeMother(char target, int motherIndex) {
        int SIndex = target - SBase;
        int choIndex = SIndex / (21 * 28); // 초성 인덱스
        return (char) (SBase + (choIndex * 21 + motherIndex) * 28);
    }
    List<String> help(char target) {
        List<String> list = new ArrayList<>();
        switch (helpable(target)) {
            case 1:
                list.add(addFooter(target, complete) + "다");
                list.add(String.valueOf(addFooter(target, going)));
                list.add(addFooter(target, complete) + "다고");
                list.add(addFooter(target, going) + "게");
                list.add(String.valueOf(addFooter(target, complete)));
                list.add(addFooter(target, going) + "지");
                list.add(addFooter(target, going) + "까");
                list.add(addFooter(target, complete) + "다는");
                list.add(addFooter(target, respect) + "니다");
            case 2:
//                list.add();
        }
        return list;
    }
    public static void main(String[] args) {
        var comp = new FastTest();
        char c0 = '가';
        char c1 = '서';
        char c2 = '키';
        int r = comp.helpable(c0);
        System.out.println(comp.changeMother(c2, 6)); // ㅕ
        System.out.println(comp.changeMother(c2, 7));
//        System.out.println(comp.addFooter('가', comp.going));
//        System.out.println(comp.addFooter('가', comp.respect));
    }
}
