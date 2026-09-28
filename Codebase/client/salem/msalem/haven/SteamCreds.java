package haven;

import com.codedisaster.steamworks.SteamException;
import java.io.IOException;

public class SteamCreds extends AuthClient.Credentials {
   private final Steam api;
   private final String name;

   public SteamCreds() throws IOException {
      if ((this.api = Steam.get()) == null) {
         throw new IOException("Steam is not running");
      } else {
         this.name = this.api.displayname();
      }
   }

   @Override
   public String name() {
      return this.name;
   }

   @Override
   public String tryauth(AuthClient cl) throws IOException {
      Steam.WebTicket tkt;
      try {
         tkt = this.api.webticket();
      } catch (InterruptedException var11) {
         throw new IOException("interrupted", var11);
      } catch (SteamException var12) {
         throw new AuthClient.Credentials.AuthException(var12.getMessage());
      }

      String var6;
      try {
         Message rpl = cl.cmd("steam", Utils.byte2hex(tkt.data));
         String stat = rpl.string();
         if (!stat.equals("ok")) {
            if (stat.equals("no")) {
               throw new AuthClient.Credentials.AuthException(rpl.string());
            }

            throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
         }

         String acct = rpl.string();
         var6 = acct;
      } finally {
         tkt.cancel();
      }

      return var6;
   }
}
