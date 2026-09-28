package haven;

public class RemoteUI implements UI.Receiver, UI.Runner {
  public static boolean debug = false;
  
  Session sess;
  
  Session ret;
  
  UI ui;
  
  public RemoteUI(Session sess) {
    this.sess = sess;
    Widget.initnames();
  }
  
  public void rcvmsg(int id, String name, Object... args) {
    Message msg = new Message(1);
    msg.adduint16(id);
    msg.addstring(name);
    msg.addlist(args);
    this.sess.queuemsg(msg);
    if (debug) {
      this.ui.message("Message sent. ID:" + id + "\n\tname: " + name, GameUI.MsgType.INFO);
      for (Object o : args)
        this.ui.message("\targ: " + o.toString(), GameUI.MsgType.INFO); 
    } 
  }
  
  public void ret(Session sess) {
    synchronized (this.sess) {
      this.ret = sess;
      this.sess.notifyAll();
    } 
  }
  
  public Session run(UI ui) throws InterruptedException {
    this.ui = ui;
    ui.setreceiver(this);
    while (true) {
      Message msg;
      while ((msg = this.sess.getuimsg()) != null) {
        if (msg.type == 0) {
          int id = msg.uint16();
          String type = msg.string();
          int parent = msg.uint16();
          Object[] pargs = msg.list();
          Object[] cargs = msg.list();
          ui.newwidget(id, type, parent, pargs, cargs);
          continue;
        } 
        if (msg.type == 1) {
          int id = msg.uint16();
          String name = msg.string();
          Object[] args = msg.list();
          ui.uimsg(id, name, args);
          checkvents(name, args);
          continue;
        } 
        if (msg.type == 2) {
          int id = msg.uint16();
          ui.destroy(id);
        } 
      } 
      synchronized (this.sess) {
        if (this.ret != null) {
          this.sess.close();
          return this.ret;
        } 
        if (!this.sess.alive())
          return null; 
        this.sess.wait(50L);
      } 
    } 
  }
  
  private void checkvents(String name, Object[] args) {
    if (name.equals("prog") && 
      args.length == 0)
      progressComplete(); 
  }
  
  private void progressComplete() {
    try {
      if (Config.autosift && UI.isCursor("gfx/hud/curs/sft")) {
        MapView map = UI.instance.gui.map;
        Gob player = map.player();
        map.wdgmsg(map, "click", new Object[] { player.sc, player.rc, Integer.valueOf(1), Integer.valueOf(0) });
      } 
    } catch (Exception exception) {}
  }
}
