package root.plm;

import root.plm.entity.Word;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dict {
    final List<Word> src;
    final Map<Integer, Word> map = new HashMap<>();

    public Dict(List<Word> list) {
        src = list;
        list.forEach(item -> map.put(item.getN(), item));
    }

    Word get(int n) {
        return map.get(n);
    }
}
