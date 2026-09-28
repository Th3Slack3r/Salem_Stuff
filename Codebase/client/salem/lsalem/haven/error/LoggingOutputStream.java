package haven.error;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutputStream extends ByteArrayOutputStream {
   private Logger logger;
   private Level level;
   private String lineSeparator;

   public LoggingOutputStream(Logger logger, Level level) {
      this.logger = logger;
      this.level = level;
      this.lineSeparator = System.getProperty("line.separator");
   }

   @Override
   public void flush() throws IOException {
      synchronized (this) {
         super.flush();
         String record = this.toString();
         super.reset();
         if (record.length() != 0 && !record.equals(this.lineSeparator)) {
            this.logger.logp(this.level, "", "", record);
         }
      }
   }
}
