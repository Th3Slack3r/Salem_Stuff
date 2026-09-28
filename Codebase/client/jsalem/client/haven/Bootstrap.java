package haven;

import haven.error.ErrorHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class Bootstrap implements UI.Receiver, UI.Runner {
   protected Session sess;
   protected String hostname;
   protected int port;
   Queue<Bootstrap.Message> msgs = new LinkedList<>();
   String inituser = null;
   byte[] initcookie = null;

   public Bootstrap(String hostname, int port) {
      this.hostname = hostname;
      this.port = port;
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
      if (this.getpref("savedtoken", "").length() == 64) {
         token = Utils.hex2byte(this.getpref("savedtoken", null));
      }

      String authserver = Config.authserv == null ? this.hostname : Config.authserv;
      int authport = Config.authport;

      label367:
      while (true) {
         byte[] cookie;
         String acctname;
         if (this.initcookie != null) {
            acctname = this.inituser;
            cookie = this.initcookie;
            this.initcookie = null;
         } else {
            String tokenname;
            if (token != null && (tokenname = this.getpref("tokenname", null)) != null) {
               savepw = true;
               ui.uimsg(1, "token", loginname);

               while (true) {
                  Bootstrap.Message msg;
                  synchronized (this.msgs) {
                     while ((msg = this.msgs.poll()) == null) {
                        this.msgs.wait();
                     }
                  }

                  if (msg.id == 1) {
                     if (msg.name == "login") {
                        ui.uimsg(1, "prg", "Authenticating...");

                        try {
                           AuthClient auth = new AuthClient(authserver, authport);

                           try {
                              if ((acctname = auth.trytoken(tokenname, token)) == null) {
                                 token = null;
                                 this.setpref("savedtoken", "");
                                 ui.uimsg(1, "error", "Invalid save");
                                 continue label367;
                              }

                              cookie = auth.getcookie();
                              break;
                           } finally {
                              auth.close();
                           }
                        } catch (IOException var44) {
                           ui.uimsg(1, "error", var44.getMessage());
                           continue label367;
                        }
                     }

                     if (msg.name == "forget") {
                        token = null;
                        this.setpref("savedtoken", "");
                        continue label367;
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

               AuthClient.Credentials creds = (AuthClient.Credentials)msg.args[0];
               savepw = (Boolean)msg.args[1];
               loginname = creds.name();
               ui.uimsg(1, "prg", "Authenticating...");

               try {
                  AuthClient auth = new AuthClient(authserver, authport);

                  try {
                     try {
                        acctname = creds.tryauth(auth);
                     } catch (AuthClient.Credentials.AuthException var39) {
                        ui.uimsg(1, "error", var39.getMessage());
                        continue;
                     }

                     cookie = auth.getcookie();
                     if (savepw) {
                        this.setpref("savedtoken", Utils.byte2hex(auth.gettoken()));
                        this.setpref("tokenname", acctname);
                     }
                  } finally {
                     auth.close();
                  }
               } catch (UnknownHostException var41) {
                  ui.uimsg(1, "error", "Could not locate server");
                  continue;
               } catch (IOException var42) {
                  ui.uimsg(1, "error", var42.getMessage());
                  continue;
               }
            }
         }

         ui.uimsg(1, "prg", "Connecting...");

         try {
            this.sess = new Session(new InetSocketAddress(InetAddress.getByName(this.hostname), this.port), acctname, cookie);
         } catch (UnknownHostException var38) {
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
               continue label367;
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
