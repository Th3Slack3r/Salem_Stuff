package haven.headless;

import haven.AuthClient;
import haven.Bootstrap;
import haven.Config;
import haven.Session;
import haven.UI;
import haven.error.ErrorHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class HeadlessBootstrap extends Bootstrap {
   private String acctname;
   private String password;

   public HeadlessBootstrap(String hostname, int port) {
      super(hostname, port);
   }

   public HeadlessBootstrap(String hostname, int port, String acctname, String password) {
      super(hostname, port);
      this.acctname = acctname;
      this.password = password;
   }

   @Override
   public Session run(UI ui) throws InterruptedException {
      ui.setreceiver(this);

      String loginname = "";
      byte[] token = null;

      if (this.acctname == null || this.password == null) {
         System.out.println("[Bootstrap] No credentials provided. Use HeadlessBootstrap(host, port, user, pass)");
         return null;
      }

      while (true) {
         byte[] cookie;
         String acctname = this.acctname;

         System.out.println("Authenticating as " + acctname + "...");

         AuthClient.Credentials creds = new AuthClient.NativeCred(acctname, this.password);
         loginname = creds.name();

         try {
            AuthClient auth = new AuthClient(Config.authserv == null ? this.hostname : Config.authserv, Config.authport);

            try {
               acctname = creds.tryauth(auth);
               cookie = auth.getcookie();
            } finally {
               auth.close();
            }
         } catch (AuthClient.Credentials.AuthException var21) {
            System.out.println("[AuthClient] " + var21.getMessage());
            return null;
         } catch (UnknownHostException var23) {
            System.out.println("[Bootstrap] Could not locate server");
            return null;
         } catch (IOException var24) {
            System.out.println("[Bootstrap] " + var24.getMessage());
            return null;
         }

         System.out.println("Connecting...");

         try {
            this.sess = new Session(new InetSocketAddress(InetAddress.getByName(this.hostname), Config.mainport), acctname, cookie);
            break;
         } catch (UnknownHostException var25) {
            System.out.println("[Bootstrap] Could not locate server");
            return null;
         }

      }

      Thread.sleep(100L);

      while (this.sess.state != "") {
         if (this.sess.connfailed != 0) {
            String error;
            switch (this.sess.connfailed) {
               case 1: error = "Invalid authentication token"; break;
               case 2: error = "Already logged in"; break;
               case 3: error = "Could not connect to server"; break;
               case 4: error = "This client is too old"; break;
               case 5: error = "Authentication token expired"; break;
               default: error = "Connection failed";
            }
            System.out.println("[Bootstrap] " + error);
            return null;
         }
         synchronized (this.sess) {
            this.sess.wait();
         }
      }

      ErrorHandler.setprop("usr", this.sess.username);
      System.out.println("Connection Successful!");
      return this.sess;
   }
}
