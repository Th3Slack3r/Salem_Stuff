package haven.test;

import haven.Coord;
import haven.HackThread;
import haven.RemoteUI;
import haven.Session;
import haven.UI;
import haven.Widget;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;

public class TestClient implements Runnable {
  public Session sess;
  
  public InetSocketAddress addr;
  
  public String user;
  
  public byte[] cookie;
  
  public ThreadGroup tg;
  
  public Thread me;
  
  public UI ui;
  
  public boolean loop = false;
  
  public Collection<Robot> robots = new HashSet<>();
  
  private static Object errsync = new Object();
  
  public TestClient(String user) {
    try {
      this.addr = new InetSocketAddress(InetAddress.getByName("localhost"), 1870);
    } catch (UnknownHostException e) {
      throw new RuntimeException("localhost not known");
    } 
    this.user = user;
    this.cookie = new byte[64];
    this.tg = new ThreadGroup(HackThread.tg(), "Test client") {
        public void uncaughtException(Thread t, Throwable e) {
          synchronized (TestClient.errsync) {
            System.err.println("Exception in test client: " + TestClient.this.user);
            e.printStackTrace(System.err);
          } 
          TestClient.this.stop();
        }
      };
  }
  
  public void connect() throws InterruptedException {
    this.sess = new Session(this.addr, this.user, this.cookie, new Object[0]);
    synchronized (this.sess) {
      while (this.sess.state != "") {
        if (this.sess.connfailed != 0)
          throw new RuntimeException("Connection failure for " + this.user + " (" + this.sess.connfailed + ")"); 
        this.sess.wait();
      } 
    } 
  }
  
  public void addbot(Robot bot) {
    synchronized (this.robots) {
      this.robots.add(bot);
    } 
  }
  
  public void rembot(Robot bot) {
    synchronized (this.robots) {
      this.robots.remove(bot);
    } 
  }
  
  public class TestUI extends UI {
    public TestUI(Coord sz, Session sess) {
      super(sz, sess);
    }
    
    public void newwidget(int id, String type, int parent, Object[] pargs, Object... cargs) throws InterruptedException {
      super.newwidget(id, type, parent, pargs, cargs);
      Widget w = (Widget)this.widgets.get(Integer.valueOf(id));
      synchronized (TestClient.this.robots) {
        for (Robot r : TestClient.this.robots)
          r.newwdg(id, w, cargs); 
      } 
    }
    
    public void destroy(Widget w) {
      int id;
      if (!this.rwidgets.containsKey(w)) {
        id = -1;
      } else {
        id = ((Integer)this.rwidgets.get(w)).intValue();
      } 
      synchronized (TestClient.this.robots) {
        for (Robot r : TestClient.this.robots)
          r.dstwdg(id, w); 
      } 
      super.destroy(w);
    }
    
    public void uimsg(int id, String msg, Object... args) {
      Widget w = (Widget)this.widgets.get(Integer.valueOf(id));
      synchronized (TestClient.this.robots) {
        for (Robot r : TestClient.this.robots)
          r.uimsg(id, w, msg, args); 
      } 
      super.uimsg(id, msg, args);
    }
  }
  
  public void run() {
    try {
      try {
        do {
          connect();
          RemoteUI rui = new RemoteUI(this.sess);
          this.ui = new TestUI(new Coord(800, 600), this.sess);
          rui.run(this.ui);
        } while (this.loop);
      } catch (InterruptedException interruptedException) {}
    } finally {
      stop();
    } 
  }
  
  public void start() {
    this.me = (Thread)new HackThread(this.tg, this, "Main thread");
    this.me.start();
  }
  
  public void stop() {
    this.tg.interrupt();
  }
  
  public boolean alive() {
    return (this.me != null && this.me.isAlive());
  }
  
  public void join() {
    while (alive()) {
      try {
        this.me.join();
      } catch (InterruptedException e) {
        this.tg.interrupt();
      } 
    } 
  }
  
  public String toString() {
    return "Client " + this.user;
  }
}
