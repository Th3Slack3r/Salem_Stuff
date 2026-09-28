package haven;

import haven.error.ErrorHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class Bootstrap implements UI.Receiver, UI.Runner {
   public Session sess;
   public String hostname;
   public final Queue<Bootstrap.Message> msgs = new LinkedList<>();
   public String inituser = null;
   public byte[] initcookie = null;

   public Bootstrap(String hostname, int port) {
      Config.server = hostname;
      this.hostname = hostname;
   }

   public void setinitcookie(String username, byte[] cookie) {
      this.inituser = username;
      this.initcookie = cookie;
   }

   private String getpref(String name, String def) {
      return Utils.getpref(name + "@" + this.hostname, def);
   }

   private void setpref(String name, String val) {
      Utils.setpref(name + "@" + this.hostname, val);
   }

   @Override
   public Session run(UI ui) throws InterruptedException {
      ui.setreceiver(this);
      ui.bind(new LoginScreen(ui.root), 1);
      String loginname = this.getpref("loginname", "");
      boolean savepw = false;
      byte[] token = null;
      String tokenhex = this.getpref("savedtoken", "");
      if (tokenhex.length() == 64) {
         token = Utils.hex2byte(tokenhex);
      }

      label384:
      while (true) {
         byte[] cookie;
         String acctname;
         if (this.initcookie != null) {
            acctname = this.inituser;
            cookie = this.initcookie;
            this.initcookie = null;
         } else if (token != null && this.getpref("tokenname", null) != null) {
            savepw = true;
            ui.uimsg(1, "token", loginname, tokenhex);

            while (true) {
               Bootstrap.Message msg;
               synchronized (this.msgs) {
                  while ((msg = this.msgs.poll()) == null) {
                     this.msgs.wait();
                  }
               }

               if (msg.id == 1) {
                  if (msg.name == "login") {
                     String var46;
                     loginname = var46 = (String)msg.args[0];
                     tokenhex = (String)msg.args[1];
                     token = Utils.hex2byte(tokenhex);
                     ui.uimsg(1, "prg", "Authenticating...");

                     try {
                        AuthClient auth = new AuthClient(Config.authserv == null ? this.hostname : Config.authserv, Config.authport);

                        try {
                           if ((acctname = auth.trytoken(var46, token)) == null) {
                              token = null;
                              this.setpref("savedtoken", "");
                              ui.uimsg(1, "error", "Invalid save");
                              continue label384;
                           }

                           cookie = auth.getcookie();
                           break;
                        } finally {
                           auth.close();
                        }
                     } catch (IOException var43) {
                        ui.uimsg(1, "error", var43.getMessage());
                        continue label384;
                     }
                  }

                  if (msg.name == "forget") {
                     token = null;
                     this.setpref("savedtoken", "");
                     continue label384;
                  }
               }
            }
         } else {
            ui.uimsg(1, "passwd", loginname, savepw);

            Bootstrap.Message msg;
            do {
               synchronized (this.msgs) {
                  while ((msg = this.msgs.poll()) == null) {
                     this.msgs.wait();
                  }
               }
            } while (msg.id != 1 || msg.name != "login");

            if (msg.args[0] instanceof String && msg.args[1] instanceof String) {
               String tokenname;
               loginname = tokenname = (String)msg.args[0];
               tokenhex = (String)msg.args[1];
               token = Utils.hex2byte(tokenhex);
               this.setpref("savedtoken", tokenhex);
               this.setpref("tokenname", tokenname);
               continue;
            }

            AuthClient.Credentials creds = (AuthClient.Credentials)msg.args[0];
            savepw = (Boolean)msg.args[1];
            loginname = creds.name();
            ui.uimsg(1, "prg", "Authenticating...");

            try {
               AuthClient auth = new AuthClient(Config.authserv == null ? this.hostname : Config.authserv, Config.authport);

               try {
                  try {
                     acctname = creds.tryauth(auth);
                  } catch (AuthClient.Credentials.AuthException var38) {
                     ui.uimsg(1, "error", var38.getMessage());
                     continue;
                  }

                  cookie = auth.getcookie();
                  if (savepw) {
                     String hex = Utils.byte2hex(auth.gettoken());
                     this.setpref("savedtoken", hex);
                     this.setpref("tokenname", acctname);
                     Config.storeAccount(acctname, hex);
                  }
               } finally {
                  auth.close();
               }
            } catch (UnknownHostException var40) {
               ui.uimsg(1, "error", "Could not locate server");
               continue;
            } catch (IOException var41) {
               ui.uimsg(1, "error", var41.getMessage());
               continue;
            }
         }

         ui.uimsg(1, "prg", "Connecting...");

         try {
            this.sess = new Session(new InetSocketAddress(InetAddress.getByName(this.hostname), Config.mainport), acctname, cookie);
         } catch (UnknownHostException var37) {
            ui.uimsg(1, "error", "Could not locate server");
            continue;
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

               ui.uimsg(1, "error", error);
               this.sess = null;
               continue label384;
            }

            synchronized (this.sess) {
               this.sess.wait();
            }
         }

         this.setpref("loginname", loginname);
         ui.destroy(1);
         ErrorHandler.setprop("usr", this.sess.username);
         return this.sess;
      }
   }

   @Override
   public void rcvmsg(int widget, String msg, Object... args) {
      synchronized (this.msgs) {
         this.msgs.add(new Bootstrap.Message(widget, msg, args));
         this.msgs.notifyAll();
      }
   }

   public static class Message {
      int id;
      String name;
      Object[] args;

      public Message(int id, String name, Object... args) {
         this.id = id;
         this.name = name;
         this.args = args;
      }
   }
}
