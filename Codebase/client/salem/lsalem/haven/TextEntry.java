package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

public class TextEntry extends Widget {
   public static final Text.Foundry fnd = new Text.Foundry(new Font("SansSerif", 0, 12), Color.BLACK);
   public static final int defh = fnd.height() + 2;
   public LineEdit buf;
   public int sx;
   public boolean pw = false;
   public String text;
   private Text.Line tcache = null;

   public void settext(String text) {
      this.buf.setline(text);
   }

   public void rsettext(String text) {
      this.buf = new LineEdit(this.text = text) {
         @Override
         protected void done(String line) {
            TextEntry.this.activate(line);
         }

         @Override
         protected void changed() {
            TextEntry.this.text = this.line;
            TextEntry.this.changed();
         }
      };
   }

   @Override
   public void uimsg(String name, Object... args) {
      if (name == "settext") {
         this.settext((String)args[0]);
      } else if (name == "get") {
         this.wdgmsg("text", new Object[]{this.buf.line});
      } else if (name == "pw") {
         this.pw = (Integer)args[0] == 1;
      } else {
         super.uimsg(name, args);
      }
   }

   protected void drawbg(GOut g) {
      g.frect(Coord.z, this.sz);
   }

   protected Text.Line render_text(String text) {
      return fnd.render(text);
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      String dtext;
      if (this.pw) {
         dtext = "";

         for (int i = 0; i < this.buf.line.length(); i++) {
            dtext = dtext + "*";
         }
      } else {
         dtext = this.buf.line;
      }

      this.drawbg(g);
      if (this.tcache == null || !this.tcache.text.equals(dtext)) {
         this.tcache = this.render_text(dtext);
      }

      int cx = this.tcache.advance(this.buf.point);
      if (cx < this.sx) {
         this.sx = cx;
      }

      if (cx > this.sx + (this.sz.x - 1)) {
         this.sx = cx - (this.sz.x - 1);
      }

      g.image(this.tcache.tex(), new Coord(-this.sx, 0));
      if (this.hasfocus && System.currentTimeMillis() % 1000L > 500L) {
         int lx = cx - this.sx + 1;
         g.chcolor(0, 0, 0, 255);
         g.line(new Coord(lx, 1), new Coord(lx, this.tcache.sz().y - 1), 1.0);
         g.chcolor();
      }
   }

   public TextEntry(Coord c, Coord sz, Widget parent, String deftext) {
      super(c, sz, parent);
      this.rsettext(deftext);
      this.setcanfocus(true);
   }

   public TextEntry(Coord c, int w, Widget parent, String deftext) {
      this(c, new Coord(w, defh), parent, deftext);
   }

   public void changed() {
   }

   public void activate(String text) {
      if (this.canactivate) {
         this.wdgmsg("activate", new Object[]{text});
      }
   }

   @Override
   public boolean type(char c, KeyEvent ev) {
      return this.buf.key(ev);
   }

   @Override
   public boolean keydown(KeyEvent e) {
      this.buf.key(e);
      return true;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      this.parent.setfocus(this);
      if (this.tcache != null) {
         this.buf.point = this.tcache.charat(c.x + this.sx);
      }

      return true;
   }

   @Widget.RName("text")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return args[0] instanceof Coord
            ? new TextEntry(c, (Coord)args[0], parent, (String)args[1])
            : new TextEntry(c, (Integer)args[0], parent, (String)args[1]);
      }
   }
}
