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
   public HeadlessBootstrap(String hostname, int port) {
      super(hostname, port);
   }

   @Override
   public Session run(UI ui) throws InterruptedException {
      ui.setreceiver(this);
      String loginname = "";
      byte[] token = null;
      String tokenhex = "";

      label148:
      while (true) {
         while (true) {
            byte[] cookie;
            String acctname;
            while (true) {
               System.out.println("");
               System.out.println("");
               System.out.print("Connect to Providence (default) or Popham? ");
               String server_choice = System.console().readLine();
               if (server_choice.equals("Providence") || server_choice.equals("")) {
                  Config.mainport = 1870;
               } else if (server_choice.equals("Popham")) {
                  Config.mainport = 1606;
               } else if (server_choice.equals("none")) {
                  System.exit(0);
               } else {
                  System.out.println("[Bootstrap] This server name is unknown!");
               }

               System.out.print("Please enter your account name: ");
               acctname = System.console().readLine();
               System.out.print("Please enter your password: ");
               String password = new String(System.console().readPassword());
               AuthClient.Credentials creds = new AuthClient.NativeCred(acctname, password);
               loginname = creds.name();
               System.out.println("Authenticating...");

               try {
                  AuthClient auth = new AuthClient(Config.authserv == null ? this.hostname : Config.authserv, Config.authport);

                  try {
                     try {
                        acctname = creds.tryauth(auth);
                     } catch (AuthClient.Credentials.AuthException var21) {
                        System.out.println("[AuthClient] " + var21.getMessage());
                        continue;
                     }

                     cookie = auth.getcookie();
                     break;
                  } finally {
                     auth.close();
                  }
               } catch (UnknownHostException var23) {
                  ui.uimsg(1, "error", "Could not locate server");
               } catch (IOException var24) {
                  ui.uimsg(1, "error", var24.getMessage());
               }
            }

            System.out.println("Connecting...");

            try {
               this.sess = new Session(new InetSocketAddress(InetAddress.getByName(this.hostname), Config.mainport), acctname, cookie);
               break;
            } catch (UnknownHostException var25) {
               System.out.println("[BootStrap] Could not locate server");
            }
         }

         Thread.sleep(100L);

         while (this.sess.state != "") {
            if (this.sess.connfailed != 0) {
               String error;
               switch (this.sess.connfailed) {
                  case 1:
                     error = "Invalid authentication token";
                     break;
                  case 2:
                     error = "Already logged in";
                     break;
                  case 3:
                     error = "Could not connect to server";
                     break;
                  case 4:
                     error = "This client is too old";
                     break;
                  case 5:
                     error = "Authentication token expired";
                     break;
                  default:
                     error = "Connection failed";
               }

               System.out.println("[Bootstrap]" + error);
               this.sess = null;
               continue label148;
            }

            synchronized (this.sess) {
               this.sess.wait();
            }
         }

         ErrorHandler.setprop("usr", this.sess.username);
         System.out.println("Connection Successful!");
         System.out.println("");
         System.out.println("");
         return this.sess;
      }
   }
}
