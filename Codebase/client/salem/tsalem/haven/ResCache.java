package haven;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ResCache {
  public static final ResCache global = StupidJavaCodeContainer.makeglobal();
  
  OutputStream store(String paramString) throws IOException;
  
  InputStream fetch(String paramString) throws IOException;
  
  public static class StupidJavaCodeContainer {
    private static ResCache makeglobal() {
      ResCache ret;
      if ((ret = JnlpCache.create()) != null)
        return ret; 
      if (Config.fscache && (
        ret = BaseFileCache.create()) != null)
        return ret; 
      return null;
    }
  }
  
  public static class TestCache implements ResCache {
    public OutputStream store(final String name) {
      return new ByteArrayOutputStream() {
          public void close() {
            byte[] res = toByteArray();
            System.out.println(name + ": " + res.length);
          }
        };
    }
    
    public InputStream fetch(String name) throws IOException {
      throw new FileNotFoundException();
    }
  }
}
