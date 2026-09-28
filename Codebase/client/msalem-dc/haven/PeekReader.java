package haven;

import java.io.IOException;
import java.io.Reader;

public class PeekReader extends Reader {
   private final Reader back;
   private boolean p = false;
   private int la;

   public PeekReader(Reader back) {
      this.back = back;
   }

   @Override
   public void close() throws IOException {
      this.back.close();
   }

   @Override
   public int read() throws IOException {
      if (this.p) {
         this.p = false;
         return this.la;
      } else {
         return this.back.read();
      }
   }

   @Override
   public int read(char[] b, int off, int len) throws IOException {
      int r = 0;

      while (r < len) {
         int c = this.read();
         if (c < 0) {
            return r;
         }

         b[off + r++] = (char)c;
      }

      return r;
   }

   @Override
   public boolean ready() throws IOException {
      return this.p ? true : this.back.ready();
   }

   protected boolean whitespace(char c) {
      return Character.isWhitespace(c);
   }

   public int peek(boolean skipws) throws IOException {
      while (!this.p || skipws && this.la >= 0 && this.whitespace((char)this.la)) {
         this.la = this.back.read();
         this.p = true;
      }

      return this.la;
   }

   public int peek() throws IOException {
      return this.peek(false);
   }
}
