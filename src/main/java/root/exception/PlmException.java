package root.exception;

import root.entity.plm.LlmWord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlmException extends RuntimeException {
  public Map<String, Object> info;

    public PlmException(String title, String data) {
      info = new HashMap<>();
      info.put(title, data);
    }

  public PlmException(String failToUnderstand, Map<String, List<LlmWord>> failHistory) {
    info = new HashMap<>();
    info.put(failToUnderstand, failHistory);
  }
}
