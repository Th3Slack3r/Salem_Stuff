package haven;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SessWidget extends AWidget {
   private final Defer.Future<SessWidget.Connection> conn;
   private boolean rep = false;

   public SessWidget(Widget parent, final String addr, final int port, final byte[] cookie, final Object... args) {
      super(parent);
      Config.server = addr;
      this.conn = Defer.later(new Defer.Callable<SessWidget.Connection>() {
         public SessWidget.Connection call() throws InterruptedException {
            InetAddress host;
            try {
               host = InetAddress.getByName(addr);
            } catch (UnknownHostException var11) {
               return new SessWidget.Connection(null, 3);
            }

            Session sess = new Session(new InetSocketAddress(host, port), SessWidget.this.ui.sess.username, cookie, args);

            SessWidget.Connection var5;
            try {
               synchronized (sess) {
                  while (sess.state != "") {
                     if (sess.connfailed != 0) {
                        return new SessWidget.Connection(null, sess.connfailed);
                     }

                     sess.wait();
                  }

                  SessWidget.Connection ret = new SessWidget.Connection(sess, 0);
                  sess = null;
                  var5 = ret;
               }
            } finally {
               if (sess != null) {
                  sess.close();
               }
            }

            return var5;
         }
      });
   }

   @Override
   public void tick(double dt) {
      super.tick(dt);
      if (!this.rep && this.conn.done()) {
         this.wdgmsg("res", new Object[]{this.conn.get().error});
         this.rep = true;
      }
   }

   @Override
   public void uimsg(String name, Object... args) {
      if (name == "exec") {
         ((RemoteUI)this.ui.rcvr).ret(this.conn.get().sess);
      } else {
         super.uimsg(name, args);
      }
   }

   @Override
   public void destroy() {
      super.destroy();
      if (this.conn.done()) {
         Session sess = this.conn.get().sess;
         if (sess != null) {
            sess.close();
         }
      } else {
         this.conn.cancel();
      }
   }

   @Widget.RName("sess")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String host = (String)args[0];
         int port = (Integer)args[1];
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
}
