package haven.error;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ErrorLogFormatter extends Formatter {
   private final Date dat = new Date();
   private long last = 0L;

   @Override
   public synchronized String format(LogRecord record) {
      long now = record.getMillis();
      this.dat.setTime(now);
      String message = this.formatMessage(record) + "\n";
      if (now - this.last > 1000L) {
         message = String.format("%s\n%s", this.dat.toString(), message);
      }

      this.last = now;
      return message;
   }
}
