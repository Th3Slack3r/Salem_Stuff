package haven;

import java.io.IOException;
import java.net.URL;

public abstract class BrowserAuth extends AuthClient.Credentials {
   public abstract String method();

   @Override
   public String tryauth(AuthClient cl) throws IOException {
      if (WebBrowser.self == null) {
         throw new AuthClient.Credentials.AuthException("Could not find any web browser to launch");
      } else {
         Message rpl = cl.cmd("web", this.method());
         String stat = rpl.string();
         if (stat.equals("ok")) {
            URL url = new URL(rpl.string());

            try {
               WebBrowser.self.show(url);
            } catch (WebBrowser.BrowserException var6) {
               throw new AuthClient.Credentials.AuthException("Could not launch web browser");
            }

            rpl = cl.cmd("wait");
            stat = rpl.string();
            if (stat.equals("ok")) {
               return rpl.string();
            } else if (stat.equals("no")) {
               throw new AuthClient.Credentials.AuthException(rpl.string());
            } else {
               throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
            }
         } else if (stat.equals("no")) {
            throw new AuthClient.Credentials.AuthException(rpl.string());
         } else {
            throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
         }
      }
   }
}
