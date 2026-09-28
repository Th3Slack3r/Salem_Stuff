package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LoginScreen extends Widget {
   LoginScreen.Login cur;
   Text error;
   IButton btn;
   static final Text.Furnace textf = new Text.Foundry(new Font("Sans", 1, 16), Color.BLACK).aa(true);
   static final Text.Furnace texte = new Text.Foundry(new Font("Sans", 1, 18), new Color(255, 0, 0)).aa(true);
   static final Text.Furnace textfs = new Text.Foundry(new Font("Sans", 1, 14), Color.BLACK).aa(true);
   static final Tex bg = Resource.loadtex("gfx/loginscr");
   static final Tex cbox = Resource.loadtex("gfx/hud/login/cbox");
   static final Coord cboxc = new Coord((bg.sz().x - cbox.sz().x) / 2, 310);
   Text progress = null;
   static final BufferedImage[] loginb = new BufferedImage[]{
      Resource.loadimg("gfx/hud/buttons/loginu"), Resource.loadimg("gfx/hud/buttons/logind"), Resource.loadimg("gfx/hud/buttons/loginh")
   };

   public LoginScreen(Widget parent) {
      super(parent.sz.div(2).sub(bg.sz().div(2)), bg.sz(), parent);
      this.setfocustab(true);
      parent.setfocus(this);
      new Img(Coord.z, bg, this);
      new Img(cboxc, cbox, this);
   }

   private void mklogin() {
      synchronized (this.ui) {
         this.btn = new IButton(cboxc.add((cbox.sz().x - loginb[0].getWidth()) / 2, 140), this, loginb[0], loginb[1], loginb[2]);
         this.progress(null);
      }
   }

   private void error(String error) {
      synchronized (this.ui) {
         if (this.error != null) {
            this.error = null;
         }

         if (error != null) {
            this.error = texte.render(error);
         }
      }
   }

   private void progress(String p) {
      synchronized (this.ui) {
         if (this.progress != null) {
            this.progress = null;
         }

         if (p != null) {
            this.progress = textf.render(p);
         }
      }
   }

   private void clear() {
      if (this.cur != null) {
         this.ui.destroy(this.cur);
         this.cur = null;
         this.ui.destroy(this.btn);
         this.btn = null;
      }

      this.progress(null);
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.btn) {
         if (this.cur.enter()) {
            Object[] data = this.cur.data();
            if (data != null) {
               super.wdgmsg("login", data);
            }
         }
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      synchronized (this.ui) {
         if (msg == "passwd") {
            this.clear();
            if (Config.authmech.equals("native")) {
               this.cur = new LoginScreen.Pwbox((String)args[0], (Boolean)args[1]);
            } else if (Config.authmech.equals("paradox")) {
               this.cur = new LoginScreen.Pdxbox((String)args[0], (Boolean)args[1]);
            } else if (Config.authmech.equals("amz")) {
               this.cur = new LoginScreen.Amazonbox();
            } else {
               if (!Config.authmech.equals("steam")) {
                  throw new RuntimeException("Unknown authmech `" + Config.authmech + "' specified");
               }

               this.cur = new LoginScreen.Steambox();
            }

            this.mklogin();
         } else if (msg == "token") {
            this.clear();
            this.cur = new LoginScreen.Tokenbox((String)args[0]);
            this.mklogin();
         } else if (msg == "error") {
            this.error((String)args[0]);
         } else if (msg == "prg") {
            this.error(null);
            this.clear();
            this.progress((String)args[0]);
         }
      }
   }

   @Override
   public void presize() {
      this.c = this.parent.sz.div(2).sub(this.sz.div(2));
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      if (this.error != null) {
         Coord c = new Coord((this.sz.x - this.error.sz().x) / 2, 290);
         g.chcolor(0, 0, 0, 224);
         g.frect(c.sub(4, 2), this.error.sz().add(8, 4));
         g.chcolor();
         g.image(this.error.tex(), c);
      }

      if (this.progress != null) {
         g.image(this.progress.tex(), new Coord((this.sz.x - this.progress.sz().x) / 2, cboxc.y + (cbox.sz().y - this.progress.sz().y) / 2));
      }
   }

   @Override
   public boolean type(char k, KeyEvent ev) {
      if (k == '\n') {
         if (this.cur != null && this.cur.enter()) {
            Object[] data = this.cur.data();
            if (data != null) {
               this.wdgmsg("login", this.cur.data());
            }
         }

         return true;
      } else {
         return super.type(k, ev);
      }
   }

   private class Amazonbox extends LoginScreen.WebCommon {
      private Amazonbox() {
      }

      @Override
      Object[] data() {
         return new Object[]{new BrowserAuth() {
            @Override
            public String method() {
               return "amz";
            }

            @Override
            public String name() {
               return "Amazon user";
            }
         }, false};
      }
   }

   private abstract static class Login extends Widget {
      private Login(Coord c, Coord sz, Widget parent) {
         super(c, sz, parent);
      }

      abstract Object[] data();

      abstract boolean enter();
   }

   private class Pdxbox extends LoginScreen.PwCommon {
      private Pdxbox(String username, boolean save) {
         super(username, save);
      }

      @Override
      Object[] data() {
         return new Object[]{new ParadoxCreds(this.user.text, this.pass.text), this.savepass.a};
      }
   }

   private abstract class PwCommon extends LoginScreen.Login {
      TextEntry user;
      TextEntry pass;
      CheckBox savepass;

      private PwCommon(String username, boolean save) {
         super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
         this.setfocustab(true);
         new Img(new Coord(35, 30), LoginScreen.textf.render("User name").tex(), this);
         this.user = new TextEntry(new Coord(150, 30), new Coord(150, 20), this, username);
         new Img(new Coord(35, 60), LoginScreen.textf.render("Password").tex(), this);
         this.pass = new TextEntry(new Coord(150, 60), new Coord(150, 20), this, "");
         this.pass.pw = true;
         this.savepass = new CheckBox(new Coord(150, 90), this, "Remember me");
         this.savepass.a = save;
         if (this.user.text.equals("")) {
            this.setfocus(this.user);
         } else {
            this.setfocus(this.pass);
         }
      }

      @Override
      public void wdgmsg(Widget sender, String name, Object... args) {
      }

      @Override
      boolean enter() {
         if (this.user.text.equals("")) {
            this.setfocus(this.user);
            return false;
         } else if (this.pass.text.equals("")) {
            this.setfocus(this.pass);
            return false;
         } else {
            return true;
         }
      }

      @Override
      public boolean globtype(char k, KeyEvent ev) {
         if (k == 'r' && (ev.getModifiersEx() & 768) != 0) {
            this.savepass.set(!this.savepass.a);
            return true;
         } else {
            return false;
         }
      }
   }

   private class Pwbox extends LoginScreen.PwCommon {
      private Pwbox(String username, boolean save) {
         super(username, save);
         if (Config.regurl != null) {
            final RichText text = RichText.render(
               "If you don't have an account, $col[64,64,255]{$u{register one here}}.", 0, TextAttribute.FOREGROUND, Color.BLACK
            );
            new Widget(new Coord(35, 115), text.sz(), this) {
               @Override
               public void draw(GOut g) {
                  g.image(text.tex(), Coord.z);
               }

               @Override
               public boolean mousedown(Coord c, int btn) {
                  if (btn == 1) {
                     Number ul = (Number)text.attrat(c, TextAttribute.UNDERLINE);
                     if (ul != null && ul.intValue() == TextAttribute.UNDERLINE_ON) {
                        try {
                           WebBrowser.sshow(Config.regurl);
                        } catch (WebBrowser.BrowserException var5) {
                           LoginScreen.this.error("Could not launch browser");
                        }
                     }
                  }

                  return true;
               }
            };
         }
      }

      @Override
      Object[] data() {
         return new Object[]{new AuthClient.NativeCred(this.user.text, this.pass.text), this.savepass.a};
      }
   }

   private class Steambox extends LoginScreen.Login {
      Text label = LoginScreen.textfs.render("Using Steam client to log in");

      private Steambox() {
         super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
      }

      @Override
      public void draw(GOut g) {
         g.image(this.label.tex(), new Coord((this.sz.x - this.label.sz().x) / 2, 30));
         super.draw(g);
      }

      @Override
      Object[] data() {
         try {
            return new Object[]{new SteamCreds(), false};
         } catch (IOException var2) {
            LoginScreen.this.error(var2.getMessage());
            return null;
         }
      }

      @Override
      boolean enter() {
         return true;
      }
   }

   private class Tokenbox extends LoginScreen.Login {
      Text label;
      Button btn;

      private Tokenbox(String username) {
         super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
         this.label = LoginScreen.textfs.render("Identity is saved for " + username);
         this.btn = new Button(new Coord((this.sz.x - 100) / 2, 55), 100, this, "Forget me");
      }

      @Override
      Object[] data() {
         return new Object[0];
      }

      @Override
      boolean enter() {
         return true;
      }

      @Override
      public void wdgmsg(Widget sender, String name, Object... args) {
         if (sender == this.btn) {
            LoginScreen.this.wdgmsg("forget", new Object[0]);
         } else {
            super.wdgmsg(sender, name, args);
         }
      }

      @Override
      public void draw(GOut g) {
         g.image(this.label.tex(), new Coord((this.sz.x - this.label.sz().x) / 2, 30));
         super.draw(g);
      }

      @Override
      public boolean globtype(char k, KeyEvent ev) {
         if (k == 'f' && (ev.getModifiersEx() & 768) != 0) {
            LoginScreen.this.wdgmsg("forget", new Object[0]);
            return true;
         } else {
            return false;
         }
      }
   }

   private abstract class WebCommon extends LoginScreen.Login {
      private WebCommon() {
         super(LoginScreen.cboxc, LoginScreen.cbox.sz(), LoginScreen.this);
      }

      @Override
      boolean enter() {
         return true;
      }
   }
}
