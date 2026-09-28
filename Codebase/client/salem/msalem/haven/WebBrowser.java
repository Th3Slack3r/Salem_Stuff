package haven;

import java.net.URL;

public abstract class WebBrowser {
   public static WebBrowser self;

   public abstract void show(URL var1);

   public static void sshow(URL url) {
      if (self == null) {
         throw new WebBrowser.BrowserException("No web browser available");
      } else {
         self.show(url);
      }
   }

   static {
      Console.setscmd("browse", new Console.Command() {
         @Override
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
}
