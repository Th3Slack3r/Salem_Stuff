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
  
  public void close() throws IOException {
    this.back.close();
  }
  
  public int read() throws IOException {
    if (this.p) {
      this.p = false;
      return this.la;
    } 
    return this.back.read();
  }
  
  public int read(char[] b, int off, int len) throws IOException {
    int r = 0;
    while (r < len) {
      int c = read();
      if (c < 0)
        return r; 
      b[off + r++] = (char)c;
    } 
    return r;
  }
  
  public boolean ready() throws IOException {
    if (this.p)
      return true; 
    return this.back.ready();
  }
  
  protected boolean whitespace(char c) {
    return Character.isWhitespace(c);
  }
  
  public int peek(boolean skipws) throws IOException {
    while (!this.p || (skipws && this.la >= 0 && whitespace((char)this.la))) {
      this.la = this.back.read();
      this.p = true;
    } 
    return this.la;
  }
  
  public int peek() throws IOException {
    return peek(false);
  }
}
