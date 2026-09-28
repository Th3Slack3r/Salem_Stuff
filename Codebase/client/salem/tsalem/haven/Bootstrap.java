package haven;

import haven.error.ErrorHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class Bootstrap implements UI.Receiver, UI.Runner {
  Session sess;
  
  String hostname;
  
  final Queue<Message> msgs = new LinkedList<>();
  
  String inituser = null;
  
  byte[] initcookie = null;
  
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
  
  public Session run(UI ui) throws InterruptedException {
    ui.setreceiver(this);
    ui.bind(new LoginScreen(ui.root), 1);
    String loginname = getpref("loginname", "");
    boolean savepw = false;
    byte[] token = null;
    String tokenhex = getpref("savedtoken", "");
    if (tokenhex.length() == 64)
      token = Utils.hex2byte(tokenhex); 
    label125: while (true) {
      if (this.initcookie != null) {
        String acctname = this.inituser;
        byte[] cookie = this.initcookie;
        this.initcookie = null;
        continue;
      } 
      String tokenname;
      if (token != null && (tokenname = getpref("tokenname", null)) != null) {
        savepw = true;
        ui.uimsg(1, "token", new Object[] { loginname, tokenhex });
        while (true) {
          Message msg;
          synchronized (this.msgs) {
            while ((msg = this.msgs.poll()) == null)
              this.msgs.wait(); 
          } 
          if (msg.id == 1) {
            if (msg.name == "login") {
              loginname = tokenname = (String)msg.args[0];
              tokenhex = (String)msg.args[1];
              token = Utils.hex2byte(tokenhex);
              break;
            } 
            if (msg.name == "forget") {
              token = null;
              setpref("savedtoken", "");
            } 
          } 
        } 
        ui.uimsg(1, "prg", new Object[] { "Authenticating..." });
        try {
          AuthClient auth = new AuthClient((Config.authserv == null) ? this.hostname : Config.authserv, Config.authport);
          try {
            String acctname;
            if ((acctname = auth.trytoken(tokenname, token)) == null) {
              token = null;
              setpref("savedtoken", "");
              ui.uimsg(1, "error", new Object[] { "Invalid save" });
              auth.close();
              continue;
            } 
            byte[] cookie = auth.getcookie();
            auth.close();
            continue;
          } finally {
            auth.close();
          } 
        } catch (IOException e) {
          Message msg;
          ui.uimsg(1, "error", new Object[] { msg.getMessage() });
          continue;
        } 
      } 
      ui.uimsg(1, "passwd", new Object[] { loginname, Boolean.valueOf(savepw) });
      while (true) {
        Message msg;
        synchronized (this.msgs) {
          while ((msg = this.msgs.poll()) == null)
            this.msgs.wait(); 
        } 
        if (msg.id == 1 && 
          msg.name == "login") {
          byte[] cookie;
          String acctname;
          if (msg.args[0] instanceof String && msg.args[1] instanceof String) {
            loginname = tokenname = (String)msg.args[0];
            tokenhex = (String)msg.args[1];
            token = Utils.hex2byte(tokenhex);
            setpref("savedtoken", tokenhex);
            setpref("tokenname", tokenname);
            continue;
          } 
          AuthClient.Credentials creds = (AuthClient.Credentials)msg.args[0];
          savepw = ((Boolean)msg.args[1]).booleanValue();
          loginname = creds.name();
          ui.uimsg(1, "prg", new Object[] { "Authenticating..." });
          try {
            AuthClient auth = new AuthClient((Config.authserv == null) ? this.hostname : Config.authserv, Config.authport);
            try {
              try {
                acctname = creds.tryauth(auth);
              } catch (AuthException e) {
                ui.uimsg(1, "error", new Object[] { e.getMessage() });
                auth.close();
                continue;
              } 
              cookie = auth.getcookie();
              if (savepw) {
                String hex = Utils.byte2hex(auth.gettoken());
                setpref("savedtoken", hex);
                setpref("tokenname", acctname);
                Config.storeAccount(acctname, hex);
              } 
              auth.close();
            } finally {
              auth.close();
            } 
          } catch (UnknownHostException e) {
            Message message;
            ui.uimsg(1, "error", new Object[] { "Could not locate server" });
            continue;
          } catch (IOException e) {
            ui.uimsg(1, "error", new Object[] { e.getMessage() });
            continue;
          } 
          ui.uimsg(1, "prg", new Object[] { "Connecting..." });
          try {
            this.sess = new Session(new InetSocketAddress(InetAddress.getByName(this.hostname), Config.mainport), acctname, cookie, new Object[0]);
          } catch (UnknownHostException e) {
            AuthClient.Credentials credentials;
            ui.uimsg(1, "error", new Object[] { "Could not locate server" });
            continue;
          } 
          Thread.sleep(100L);
          while (true) {
            if (this.sess.state == "") {
              setpref("loginname", loginname);
              ui.destroy(1);
              break;
            } 
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
                  break;
              } 
              ui.uimsg(1, "error", new Object[] { error });
              this.sess = null;
              continue label125;
            } 
            synchronized (this.sess) {
              this.sess.wait();
            } 
          } 
          ErrorHandler.setprop("usr", this.sess.username);
          return this.sess;
        } 
      } 
      continue;
    } 
    ErrorHandler.setprop("usr", this.sess.username);
    return this.sess;
  }
  
  public void rcvmsg(int widget, String msg, Object... args) {
    synchronized (this.msgs) {
      this.msgs.add(new Message(widget, msg, args));
      this.msgs.notifyAll();
    } 
  }
}
