package haven;

import java.io.IOException;

public class ParadoxCreds extends AuthClient.Credentials {
  private final String username;
  
  private final String password;
  
  public ParadoxCreds(String username, String password) {
    this.username = username;
    this.password = password;
  }
  
  public String name() {
    return this.username;
  }
  
  public String tryauth(AuthClient cl) throws IOException {
    Message rpl = cl.cmd(new Object[] { "pdx", this.username, this.password });
    String stat = rpl.string();
    if (stat.equals("ok")) {
      String acct = rpl.string();
      return acct;
    } 
    if (stat.equals("no"))
      throw new AuthClient.Credentials.AuthException("Username or password incorrect"); 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
}
