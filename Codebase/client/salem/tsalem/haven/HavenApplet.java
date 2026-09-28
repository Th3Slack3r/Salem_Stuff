package haven;

import haven.error.ErrorHandler;
import haven.error.ErrorStatus;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HavenApplet extends Applet {
  public static Map<ThreadGroup, HavenApplet> applets = new HashMap<>();
  
  ThreadGroup p;
  
  HavenPanel h;
  
  boolean running = false;
  
  static boolean initedonce = false;
  
  private class ErrorPanel extends Canvas implements ErrorStatus {
    String status = "";
    
    boolean ar = false;
    
    public ErrorPanel() {
      setBackground(Color.BLACK);
      addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
              if (HavenApplet.ErrorPanel.this.ar && !HavenApplet.this.running) {
                HavenApplet.this.remove(HavenApplet.ErrorPanel.this);
                HavenApplet.this.startgame();
              } 
            }
          });
    }
    
    public boolean goterror(Throwable t) {
      HavenApplet.this.stopgame();
      setSize(HavenApplet.this.getSize());
      HavenApplet.this.add(this);
      repaint();
      return true;
    }
    
    public void connecting() {
      this.status = "Connecting to error report server...";
      repaint();
    }
    
    public void sending() {
      this.status = "Sending error report...";
      repaint();
    }
    
    public void done(String ctype, String info) {
      this.status = "Done";
      this.ar = true;
      repaint();
    }
    
    public void senderror(Exception e) {
      this.status = "Could not send error report";
      this.ar = true;
      repaint();
    }
    
    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.WHITE);
      FontMetrics m = g.getFontMetrics();
      int y = 0;
      g.drawString("An error has occurred.", 0, y + m.getAscent());
      y += m.getHeight();
      g.drawString(this.status, 0, y + m.getAscent());
      y += m.getHeight();
      if (this.ar) {
        g.drawString("Click to restart the game", 0, y + m.getAscent());
        y += m.getHeight();
      } 
    }
  }
  
  private void initonce() {
    if (initedonce)
      return; 
    initedonce = true;
    try {
      Resource.addurl(new URL("https", getCodeBase().getHost(), 443, "/res/"));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } 
    if (!Config.nopreload)
      try {
        InputStream pls = Resource.class.getResourceAsStream("res-preload");
        if (pls != null)
          Resource.loadlist(pls, -5); 
        pls = Resource.class.getResourceAsStream("res-bgload");
        if (pls != null)
          Resource.loadlist(pls, -10); 
      } catch (IOException e) {
        throw new Error(e);
      }  
  }
  
  public void destroy() {
    stopgame();
  }
  
  public void startgame() {
    if (this.running)
      return; 
    this.h = new HavenPanel(800, 600);
    add((Component)this.h);
    this.h.init();
    try {
      this.p = (ThreadGroup)new ErrorHandler(new ErrorPanel(), new URL("http", "asdffghj.com", 79, "/xyz/xyz"));
    } catch (MalformedURLException malformedURLException) {}
    synchronized (applets) {
      applets.put(this.p, this);
    } 
    Thread main = new HackThread(this.p, new Runnable() {
          public void run() {
            Thread ui = new HackThread(HavenApplet.this.h, "Haven UI thread");
            ui.start();
            try {
              Session sess = null;
              while (true) {
                UI.Runner fun;
                if (sess == null) {
                  Bootstrap bill = new Bootstrap(HavenApplet.this.getCodeBase().getHost(), Config.mainport);
                  if (HavenApplet.this.getParameter("username") != null && HavenApplet.this.getParameter("authcookie") != null)
                    bill.setinitcookie(HavenApplet.this.getParameter("username"), Utils.hex2byte(HavenApplet.this.getParameter("authcookie"))); 
                  fun = bill;
                } else {
                  fun = new RemoteUI(sess);
                } 
                sess = fun.run(HavenApplet.this.h.newui(sess));
              } 
            } catch (InterruptedException interruptedException) {
            
            } finally {
              ui.interrupt();
            } 
          }
        }"Haven main thread");
    main.start();
    this.running = true;
  }
  
  public void stopgame() {
    if (!this.running)
      return; 
    this.running = false;
    synchronized (applets) {
      applets.remove(this.p);
    } 
    this.p.interrupt();
    remove((Component)this.h);
    this.p = null;
    this.h = null;
  }
  
  public void init() {
    initonce();
    resize(800, 600);
    startgame();
  }
  
  static {
    WebBrowser.self = new WebBrowser() {
        public void show(URL url) {
          HavenApplet a;
          synchronized (HavenApplet.applets) {
            a = HavenApplet.applets.get(HackThread.tg());
          } 
          if (a != null)
            a.getAppletContext().showDocument(url, "_blank"); 
        }
      };
  }
}
