package haven;

import java.io.IOException;
import java.io.InputStream;

public class RepeatStream extends InputStream {
  private final Repeater rep;
  
  private InputStream cur;
  
  public RepeatStream(Repeater rep) {
    this.rep = rep;
    this.cur = rep.cons();
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    if (this.cur == null)
      return -1; 
    int ret;
    while ((ret = this.cur.read(b, off, len)) < 0) {
      this.cur.close();
      if ((this.cur = this.rep.cons()) == null)
        return -1; 
    } 
    return ret;
  }
  
  public int read() throws IOException {
    if (this.cur == null)
      return -1; 
    int ret;
    while ((ret = this.cur.read()) < 0) {
      this.cur.close();
      if ((this.cur = this.rep.cons()) == null)
        return -1; 
    } 
    return ret;
  }
  
  public void close() throws IOException {
    if (this.cur != null)
      this.cur.close(); 
    this.cur = null;
  }
  
  public static interface Repeater {
    InputStream cons();
  }
}
