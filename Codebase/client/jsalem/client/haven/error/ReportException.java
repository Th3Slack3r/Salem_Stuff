package haven.error;

import java.io.IOException;

public class ReportException extends IOException {
   public ReportException(String message) {
      super(message);
   }
}
