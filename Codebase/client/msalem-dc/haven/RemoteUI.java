package haven;

public class RemoteUI implements UI.Receiver, UI.Runner {
   Session sess;
   Session ret;
   UI ui;

   public RemoteUI(Session sess) {
      this.sess = sess;
      Widget.initnames();
   }

   @Override
   public void rcvmsg(int id, String name, Object... args) {
      Message msg = new Message(1);
      msg.adduint16(id);
      msg.addstring(name);
      msg.addlist(args);
      this.sess.queuemsg(msg);
   }

   public void ret(Session sess) {
      synchronized (this.sess) {
         this.ret = sess;
         this.sess.notifyAll();
      }
   }

   @Override
   public Session run(UI ui) throws InterruptedException {
      this.ui = ui;
      ui.setreceiver(this);

      while (true) {
         Message msg;
         while ((msg = this.sess.getuimsg()) == null) {
            synchronized (this.sess) {
               if (this.ret != null) {
                  this.sess.close();
                  return this.ret;
               }

               if (!this.sess.alive()) {
                  return null;
               }

               this.sess.wait();
            }
         }

         if (msg.type == 0) {
            int id = msg.uint16();
            String type = msg.string();
            int parent = msg.uint16();
            Object[] pargs = msg.list();
            Object[] cargs = msg.list();
            ui.newwidget(id, type, parent, pargs, cargs);
         } else if (msg.type == 1) {
            int id = msg.uint16();
            String name = msg.string();
            ui.uimsg(id, name, msg.list());
         } else if (msg.type == 2) {
            int id = msg.uint16();
            ui.destroy(id);
         }
      }
   }
}
