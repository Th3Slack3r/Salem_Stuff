package haven;

import java.io.IOException;
import java.net.URL;

public abstract class BrowserAuth extends AuthClient.Credentials {
  public abstract String method();
  
  public String tryauth(AuthClient cl) throws IOException {
    URL url;
    if (WebBrowser.self == null)
      throw new AuthClient.Credentials.AuthException("Could not find any web browser to launch"); 
    Message rpl = cl.cmd(new Object[] { "web", method() });
    String stat = rpl.string();
    if (stat.equals("ok")) {
      url = new URL(rpl.string());
    } else {
      if (stat.equals("no"))
        throw new AuthClient.Credentials.AuthException(rpl.string()); 
      throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
    } 
    try {
      WebBrowser.self.show(url);
    } catch (BrowserException e) {
      throw new AuthClient.Credentials.AuthException("Could not launch web browser");
    } 
    rpl = cl.cmd(new Object[] { "wait" });
    stat = rpl.string();
    if (stat.equals("ok"))
      return rpl.string(); 
    if (stat.equals("no"))
      throw new AuthClient.Credentials.AuthException(rpl.string()); 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
}
