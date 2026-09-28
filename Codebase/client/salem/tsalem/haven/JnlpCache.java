package haven;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;

public class JnlpCache implements ResCache {
  private final PersistenceService back;
  
  private final URL base;
  
  private JnlpCache(PersistenceService back, URL base) {
    this.back = back;
    this.base = base;
  }
  
  public static JnlpCache create() {
    try {
      Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
      Method m = cl.getMethod("lookup", new Class[] { String.class });
      BasicService basic = (BasicService)m.invoke(null, new Object[] { "javax.jnlp.BasicService" });
      PersistenceService prs = (PersistenceService)m.invoke(null, new Object[] { "javax.jnlp.PersistenceService" });
      return new JnlpCache(prs, basic.getCodeBase());
    } catch (Exception e) {
      return null;
    } 
  }
  
  private static String mangle(String nm) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < nm.length(); i++) {
      char c = nm.charAt(i);
      if (c == '/') {
        buf.append("_");
      } else {
        buf.append(c);
      } 
    } 
    return buf.toString();
  }
  
  private void realput(URL loc, byte[] data) {
    try {
      FileContents file;
      try {
        file = this.back.get(loc);
      } catch (FileNotFoundException e) {
        this.back.create(loc, data.length);
        file = this.back.get(loc);
      } 
      if (file.getMaxLength() < data.length && 
        file.setMaxLength(data.length) < data.length) {
        this.back.delete(loc);
        return;
      } 
      OutputStream s = file.getOutputStream(true);
      try {
        s.write(data);
      } finally {
        s.close();
      } 
    } catch (IOException e) {
      return;
    } catch (Exception e) {
      return;
    } 
  }
  
  private void put(final URL loc, final byte[] data) {
    Utils.defer(new Runnable() {
          public void run() {
            JnlpCache.this.realput(loc, data);
          }
        });
  }
  
  private InputStream get(URL loc) throws IOException {
    FileContents file = this.back.get(loc);
    return file.getInputStream();
  }
  
  public OutputStream store(final String name) throws IOException {
    OutputStream ret = new ByteArrayOutputStream() {
        public void close() {
          byte[] res = toByteArray();
          try {
            JnlpCache.this.put(new URL(JnlpCache.this.base, JnlpCache.mangle(name)), res);
          } catch (MalformedURLException e) {
            throw new RuntimeException(e);
          } 
        }
      };
    return ret;
  }
  
  public InputStream fetch(String name) throws IOException {
    try {
      URL loc = new URL(this.base, mangle(name));
      return get(loc);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw (IOException)(new IOException("Virtual NetX IO exception")).initCause(e);
    } 
  }
}
