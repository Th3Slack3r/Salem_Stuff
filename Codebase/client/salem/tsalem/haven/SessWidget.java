package haven;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SessWidget extends AWidget {
  private final Defer.Future<Connection> conn;
  
  private boolean rep = false;
  
  @RName("sess")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      String host = (String)args[0];
      int port = ((Integer)args[1]).intValue();
      byte[] cookie = Utils.hex2byte((String)args[2]);
      Object[] sargs = Utils.splice(args, 3);
      return new SessWidget(parent, host, port, cookie, sargs);
    }
  }
  
  static class Connection {
    final Session sess;
    
    final int error;
    
    Connection(Session sess, int error) {
      this.sess = sess;
      this.error = error;
    }
  }
  
  public SessWidget(Widget parent, final String addr, final int port, final byte[] cookie, Object... args) {
    super(parent);
    Config.server = addr;
    this.conn = Defer.later(new Defer.Callable<Connection>() {
          public SessWidget.Connection call() throws InterruptedException {
            InetAddress host;
            try {
              host = InetAddress.getByName(addr);
            } catch (UnknownHostException e) {
              return new SessWidget.Connection(null, 3);
            } 
            Session sess = new Session(new InetSocketAddress(host, port), SessWidget.this.ui.sess.username, cookie, args);
            try {
            
            } finally {
              if (sess != null)
                sess.close(); 
            } 
          }
        });
  }
  
  public void tick(double dt) {
    super.tick(dt);
    if (!this.rep && this.conn.done()) {
      wdgmsg("res", new Object[] { Integer.valueOf(((Connection)this.conn.get()).error) });
      this.rep = true;
    } 
  }
  
  public void uimsg(String name, Object... args) {
    if (name == "exec") {
      ((RemoteUI)this.ui.rcvr).ret(((Connection)this.conn.get()).sess);
    } else {
      super.uimsg(name, args);
    } 
  }
  
  public void destroy() {
    super.destroy();
    if (this.conn.done()) {
      Session sess = ((Connection)this.conn.get()).sess;
      if (sess != null)
        sess.close(); 
    } else {
      this.conn.cancel();
    } 
  }
}
