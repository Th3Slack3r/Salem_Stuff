package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class LoginScreen extends Widget {
  Login cur;
  
  Text error;
  
  Window log;
  
  IButton btn;
  
  static final Text.Furnace textf;
  
  static final Text.Furnace texte;
  
  static final Text.Furnace textfs;
  
  static final Tex bg = Resource.loadtex("gfx/loginscr");
  
  static final Tex cbox = Resource.loadtex("gfx/hud/login/cbox");
  
  static final Coord cboxc = new Coord(((bg.sz()).x - (cbox.sz()).x) / 2, 310);
  
  Text progress = null;
  
  AccountList accounts;
  
  Button providencestate;
  
  static final BufferedImage[] loginb;
  
  static {
    textf = (new Text.Foundry(new Font("Sans", 1, 16), Color.BLACK)).aa(true);
    texte = (new Text.Foundry(new Font("Sans", 1, 18), new Color(255, 0, 0))).aa(true);
    textfs = (new Text.Foundry(new Font("Sans", 1, 14), Color.BLACK)).aa(true);
    loginb = new BufferedImage[] { Resource.loadimg("gfx/hud/buttons/loginu"), Resource.loadimg("gfx/hud/buttons/logind"), Resource.loadimg("gfx/hud/buttons/loginh") };
  }
  
  public LoginScreen(Widget parent) {
    super(parent.sz.div(2).sub(bg.sz().div(2)), bg.sz(), parent);
    setfocustab(true);
    parent.setfocus(this);
    new Img(Coord.z, bg, this);
    new Img(cboxc, cbox, this);
    this.accounts = new AccountList(Coord.z, this, 10);
    (new Button(new Coord(this.sz.x - 210, 20), Integer.valueOf(190), this, "Connecting to New Haven") {
        public void click() {
          if (Config.authserver_name.equals("Providence")) {
            setAuthServer("Popham");
          } else {
            setAuthServer("Providence");
          } 
        }
        
        public void setAuthServer(String name) {
          Config.authserver_name = name;
          Utils.setpref("authserver_name", name);
          Config.defserv = "game.salemthegame.com";
          if (name.equals("Providence")) {
            Config.mainport = 1870;
          } else {
            Config.mainport = 1606;
          } 
          change("Connecting to " + name);
        }
      }).setAuthServer(Config.authserver_name);
    this.providencestate = new Button(new Coord(this.sz.x - 210, 45), Integer.valueOf(190), this, "Providence: unknown") {
        public void click() {
          LoginScreen.this.update_server_statuses();
        }
      };
    update_server_statuses();
    if (Config.isUpdate || Config.show_patch_notes_always)
      showChangelog(); 
    if (Config.show_discord_on_login)
      showDiscord(parent); 
  }
  
  private void update_server_statuses() {
    this.providencestate.change("Providence: checking ...");
    try {
      URL statepage = new URL("http://login.salemthegame.com/portal/state");
      InputStream is = statepage.openStream();
      int ptr = 0;
      StringBuilder buffer = new StringBuilder();
      while ((ptr = is.read()) != -1)
        buffer.append((char)ptr); 
      is.close();
      String html = buffer.toString();
      String[] lines = html.split("\n");
      String prov = lines[48];
      this.providencestate.change("New Haven: " + prov.substring(prov.indexOf('>') + 1, prov.lastIndexOf('<')));
    } catch (IOException ex) {
      String explanation = "Status page not found.";
      this.providencestate.change("Status page not found.");
    } 
  }
  
  private void showChangelog() {
    this.log = new Window(new Coord(100, 50), new Coord(50, 50), this.ui.root, "Changelog");
    this.log.justclose = true;
    Textlog txt = new Textlog(Coord.z, new Coord(450, 500), this.log);
    txt.quote = false;
    int maxlines = txt.maxLines = 200;
    this.log.pack();
    try {
      InputStream in = LoginScreen.class.getResourceAsStream("/changelog.txt");
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      File f = Config.getFile("changelog.txt");
      FileOutputStream out = new FileOutputStream(f);
      int count = 0;
      String strLine;
      while (count < maxlines && (strLine = br.readLine()) != null) {
        txt.append(strLine);
        out.write((strLine + "\n").getBytes());
        count++;
      } 
      br.close();
      out.close();
      in.close();
    } catch (FileNotFoundException fileNotFoundException) {
    
    } catch (IOException iOException) {}
    txt.setprog(0.0D);
  }
  
  private void showDiscord(Widget parent) {
    Coord discSize = new Coord(260, 100);
    Coord loginScreenCoord = new Coord(parent.sz.div(2).sub(bg.sz().div(2)));
    int x = loginScreenCoord.x + (bg.sz()).x / 2 - discSize.x / 2;
    int y = loginScreenCoord.y - discSize.y - 90;
    Coord discCoord = new Coord(x, y);
    Window disc = new Window(discCoord, discSize, this.ui.root, "Do You happen to be...");
    disc.justclose = true;
    Textlog txt = new Textlog(Coord.z, new Coord(260, 80), disc);
    txt.quote = false;
    new Button(new Coord(40, 90), Integer.valueOf(180), disc, "www.salemthegame.com/discord") {
        public void click() {
          try {
            URL url = new URL("http://www.salemthegame.com/discord");
            WebBrowser.self.show(url);
          } catch (BrowserException e) {
            UI.instance.gui.error("Could not launch web browser.");
          } catch (Exception e) {
            throw new RuntimeException(e);
          } 
        }
      };
    txt.append("...in need of #Help?");
    txt.append("...looking for an opportunity to #Trade?");
    txt.append("...or just up for some talk with other Pilgrims?");
    txt.append("   ");
    txt.append("            Visit the Official Salem Discord at:");
    disc.pack();
    txt.setprog(0.0D);
  }
  
  private static abstract class Login extends Widget {
    abstract boolean enter();
    
    abstract Object[] data();
    
    private Login(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
    }
  }
  
  private abstract class PwCommon extends Login {
    TextEntry user;
    
    TextEntry pass;
    
    CheckBox savepass;
    
    private PwCommon(String username, boolean save) {
      super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
      setfocustab(true);
      new Img(new Coord(35, 30), LoginScreen.textf.render("User name").tex(), this);
      this.user = new TextEntry(new Coord(150, 30), new Coord(150, 20), this, username);
      new Img(new Coord(35, 60), LoginScreen.textf.render("Password").tex(), this);
      this.pass = new TextEntry(new Coord(150, 60), new Coord(150, 20), this, "");
      this.pass.pw = true;
      this.savepass = new CheckBox(new Coord(150, 90), this, "Remember me");
      this.savepass.a = save;
      if (this.user.text.equals("")) {
        setfocus(this.user);
      } else {
        setfocus(this.pass);
      } 
    }
    
    public void wdgmsg(Widget sender, String name, Object... args) {}
    
    boolean enter() {
      if (this.user.text.equals("")) {
        setfocus(this.user);
        return false;
      } 
      if (this.pass.text.equals("")) {
        setfocus(this.pass);
        return false;
      } 
      return true;
    }
    
    public boolean globtype(char k, KeyEvent ev) {
      if (k == 'r' && (ev.getModifiersEx() & 0x300) != 0) {
        this.savepass.set(!this.savepass.a);
        return true;
      } 
      return false;
    }
  }
  
  private class Pwbox extends PwCommon {
    private Pwbox(String username, boolean save) {
      super(username, save);
      if (Config.regurl != null) {
        final RichText text = RichText.render("If you don't have an account, $col[64,64,255]{$u{register one here}}.", 0, new Object[] { TextAttribute.FOREGROUND, Color.BLACK });
        new Widget(new Coord(35, 115), text.sz(), this) {
            public void draw(GOut g) {
              g.image(text.tex(), Coord.z);
            }
            
            public boolean mousedown(Coord c, int btn) {
              if (btn == 1) {
                Number ul = (Number)text.attrat(c, TextAttribute.UNDERLINE);
                if (ul != null && ul.intValue() == TextAttribute.UNDERLINE_ON.intValue())
                  try {
                    WebBrowser.sshow(Config.regurl);
                  } catch (BrowserException e) {
                    LoginScreen.this.error("Could not launch browser");
                  }  
              } 
              return true;
            }
          };
      } 
    }
    
    Object[] data() {
      return new Object[] { new AuthClient.NativeCred(this.user.text, this.pass.text), Boolean.valueOf(this.savepass.a) };
    }
  }
  
  private class Pdxbox extends PwCommon {
    private Pdxbox(String username, boolean save) {
      super(username, save);
    }
    
    Object[] data() {
      return new Object[] { new ParadoxCreds(this.user.text, this.pass.text), Boolean.valueOf(this.savepass.a) };
    }
  }
  
  private abstract class WebCommon extends Login {
    private WebCommon() {
      super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
    }
    
    boolean enter() {
      return true;
    }
  }
  
  private class Amazonbox extends WebCommon {
    private Amazonbox() {}
    
    Object[] data() {
      return new Object[] { new BrowserAuth() {
            public String method() {
              return "amz";
            }
            
            public String name() {
              return "Amazon user";
            }
          }, Boolean.valueOf(false) };
    }
  }
  
  private class Tokenbox extends Login {
    private final String name;
    
    private final String token;
    
    Text label;
    
    Button btn;
    
    private Tokenbox(String username, String token) {
      super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
      this.label = LoginScreen.textfs.render("Identity is saved for " + username);
      this.btn = new Button(new Coord((this.sz.x - 100) / 2, 55), Integer.valueOf(100), this, "Forget me");
      this.name = username;
      this.token = token;
    }
    
    Object[] data() {
      return new Object[] { this.name, this.token };
    }
    
    boolean enter() {
      return true;
    }
    
    public void wdgmsg(Widget sender, String name, Object... args) {
      if (sender == this.btn) {
        LoginScreen.this.wdgmsg("forget", new Object[0]);
        return;
      } 
      super.wdgmsg(sender, name, args);
    }
    
    public void draw(GOut g) {
      g.image(this.label.tex(), new Coord((this.sz.x - (this.label.sz()).x) / 2, 30));
      super.draw(g);
    }
    
    public boolean globtype(char k, KeyEvent ev) {
      if (k == 'f' && (ev.getModifiersEx() & 0x300) != 0) {
        LoginScreen.this.wdgmsg("forget", new Object[0]);
        return true;
      } 
      return false;
    }
  }
  
  private void mklogin() {
    synchronized (this.ui) {
      this.btn = new IButton(cboxc.add(((cbox.sz()).x - loginb[0].getWidth()) / 2, 140), this, loginb[0], loginb[1], loginb[2]);
      progress((String)null);
    } 
  }
  
  private void error(String error) {
    synchronized (this.ui) {
      if (this.error != null)
        this.error = null; 
      if (error != null)
        this.error = texte.render(error); 
    } 
  }
  
  private void progress(String p) {
    synchronized (this.ui) {
      if (this.progress != null)
        this.progress = null; 
      if (p != null)
        this.progress = textf.render(p); 
    } 
  }
  
  private void clear() {
    if (this.cur != null) {
      this.ui.destroy(this.cur);
      this.cur = null;
      this.ui.destroy(this.btn);
      this.btn = null;
    } 
    progress((String)null);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.btn) {
      if (this.cur.enter()) {
        wdgmsg("login", this.cur.data());
        System.out.println("test" + this.cur.data());
        Utils.msgOut("test" + this.cur.data());
      } 
      return;
    } 
    if (msg.equals("account")) {
      boolean repeat = !(this.cur instanceof Tokenbox);
      if (repeat)
        wdgmsg("login", new Object[] { args[0], args[1] }); 
      wdgmsg("login", new Object[] { args[0], args[1] });
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  public void uimsg(String msg, Object... args) {
    synchronized (this.ui) {
      if (msg == "passwd") {
        clear();
        if (Config.authmech.equals("native")) {
          this.cur = new Pwbox((String)args[0], ((Boolean)args[1]).booleanValue());
        } else if (Config.authmech.equals("paradox")) {
          this.cur = new Pdxbox((String)args[0], ((Boolean)args[1]).booleanValue());
        } else if (Config.authmech.equals("amz")) {
          this.cur = new Amazonbox();
        } else {
          throw new RuntimeException("Unknown authmech `" + Config.authmech + "' specified");
        } 
        mklogin();
      } else if (msg == "token") {
        clear();
        this.cur = new Tokenbox((String)args[0], (String)args[1]);
        mklogin();
      } else if (msg == "error") {
        error((String)args[0]);
      } else if (msg == "prg") {
        error((String)null);
        clear();
        progress((String)args[0]);
      } 
    } 
  }
  
  public void presize() {
    this.c = this.parent.sz.div(2).sub(this.sz.div(2));
  }
  
  public void draw(GOut g) {
    super.draw(g);
    if (this.error != null) {
      Coord c = new Coord((this.sz.x - (this.error.sz()).x) / 2, 290);
      g.chcolor(0, 0, 0, 224);
      g.frect(c.sub(4, 2), this.error.sz().add(8, 4));
      g.chcolor();
      g.image(this.error.tex(), c);
    } 
    if (this.progress != null)
      g.image(this.progress.tex(), new Coord((this.sz.x - (this.progress.sz()).x) / 2, cboxc.y + ((cbox.sz()).y - (this.progress.sz()).y) / 2)); 
  }
  
  public boolean type(char k, KeyEvent ev) {
    if (k == '\n') {
      if (this.cur != null && this.cur.enter())
        wdgmsg("login", this.cur.data()); 
      return true;
    } 
    return super.type(k, ev);
  }
  
  public void destroy() {
    if (this.log != null) {
      this.ui.destroy(this.log);
      this.log = null;
    } 
    super.destroy();
  }
}
