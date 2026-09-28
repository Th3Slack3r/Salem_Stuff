package haven;

import java.io.IOException;

public class ParadoxCreds extends AuthClient.Credentials {
   private final String username;
   private final String password;

   public ParadoxCreds(String username, String password) {
      this.username = username;
      this.password = password;
   }

   @Override
   public String name() {
      return this.username;
   }

   @Override
   public String tryauth(AuthClient cl) throws IOException {
      Message rpl = cl.cmd("pdx", this.username, this.password);
      String stat = rpl.string();
      if (stat.equals("ok")) {
         return rpl.string();
      } else if (stat.equals("no")) {
         throw new AuthClient.Credentials.AuthException("Username or password incorrect");
      } else {
         throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
      }
   }
}
