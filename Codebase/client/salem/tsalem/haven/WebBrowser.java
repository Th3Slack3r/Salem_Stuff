package haven;

import java.net.URL;

public abstract class WebBrowser {
  public static WebBrowser self;
  
  static {
    Console.setscmd("browse", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            WebBrowser.sshow(new URL(args[1]));
          }
        });
  }
  
  public static class BrowserException extends RuntimeException {
    public BrowserException(String msg) {
      super(msg);
    }
    
    public BrowserException(Throwable cause) {
      super(cause);
    }
  }
  
  public static void sshow(URL url) {
    if (self == null)
      throw new BrowserException("No web browser available"); 
    self.show(url);
  }
  
  public abstract void show(URL paramURL);
}
