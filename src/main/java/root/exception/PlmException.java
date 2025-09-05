package root.exception;

import java.util.HashMap;
import java.util.Map;

public class PlmException extends RuntimeException {
  public Map<String, String> info;

    public PlmException(String title, String data) {
      info = new HashMap<>();
      info.put(title, data);
    }
}
