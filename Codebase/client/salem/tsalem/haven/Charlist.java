package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Charlist extends Widget {
  public static final Tex bg = Resource.loadtex("gfx/hud/avakort");
  
  public static final int margin = 1;
  
  public static final int bmargin = 46;
  
  public static final BufferedImage[] clu = new BufferedImage[] { Resource.loadimg("gfx/hud/login/cluu"), Resource.loadimg("gfx/hud/login/clud"), Resource.loadimg("gfx/hud/login/cluh") };
  
  public static final BufferedImage[] cld = new BufferedImage[] { Resource.loadimg("gfx/hud/login/cldu"), Resource.loadimg("gfx/hud/login/cldd"), Resource.loadimg("gfx/hud/login/cldh") };
  
  public int height;
  
  public int y;
  
  public IButton sau;
  
  public IButton sad;
  
  public List<Char> chars = new ArrayList<>();
  
  private boolean charschanged = false;
  
  public List<Char> alphachars = new ArrayList<>();
  
  private boolean filterchanged = false;
  
  private boolean prefchanged = true;
  
  private String filterstring = "";
  
  public List<Char> filteredchars = new ArrayList<>();
  
  public static class Char {
    static Text.Furnace tf = new Text.Imager((new Text.Foundry(new Font("Serif", 0, 20), Color.WHITE)).aa(true)) {
        protected BufferedImage proc(Text text) {
          return PUtils.rasterimg(PUtils.blurmask2(text.img.getRaster(), 1, 1, Color.BLACK));
        }
      };
    
    public String name;
    
    Text nt;
    
    Button plb;
    
    public Char(String name) {
      this.name = name;
      this.nt = tf.render(name);
    }
    
    public static class CharComparator implements Comparator<Char> {
      public int compare(Charlist.Char c1, Charlist.Char c2) {
        return c1.name.compareTo(c2.name);
      }
    }
  }
  
  public static class CharComparator implements Comparator<Char> {
    public int compare(Charlist.Char c1, Charlist.Char c2) {
      return c1.name.compareTo(c2.name);
    }
  }
  
  @RName("charlist")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Charlist(c, parent, ((Integer)args[0]).intValue());
    }
  }
  
  private boolean applyThis = false;
  
  CheckBox skipRenderLimitOnce = new CheckBox(new Coord(50, 0), this, "No Render Limit (once)") {
      public void changed(boolean val) {
        super.changed(val);
        Charlist.this.applyThis = val;
        if (val) {
          Config.render_distance_bool_value = false;
          Config.hideSomeGobs = false;
          OCache.renderDistance = false;
          OCache.hideSomeGobs = false;
          MapView.smallView = false;
        } else {
          boolean origRenderDistanceOnOff = Utils.getprefb("render_distance_bool_value", false);
          boolean origHideObjectsOnOff = Utils.getprefb("hide_some_gobs", false);
          Config.render_distance_bool_value = origRenderDistanceOnOff;
          Config.hideSomeGobs = origHideObjectsOnOff;
          OCache.renderDistance = origRenderDistanceOnOff;
          OCache.hideSomeGobs = origRenderDistanceOnOff;
          MapView.smallView = Config.mview_dist_small;
        } 
      }
    };
  
  public Charlist(Coord c, Widget parent, int height) {
    super(c, new Coord(clu[0].getWidth(), 92 + (bg.sz()).y * height + 1 * (height - 1)), parent);
    this.height = height;
    this.y = 0;
    this.sau = new IButton(new Coord(0, 0), this, clu[0], clu[1], clu[2]) {
        public void click() {
          Charlist.this.scroll(-1);
        }
      };
    this.sad = new IButton(new Coord(0, this.sz.y - cld[0].getHeight() - 1), this, cld[0], cld[1], cld[2]) {
        public void click() {
          Charlist.this.scroll(1);
        }
      };
    this.sau.hide();
    this.sad.hide();
    CheckBox alphacheck = new CheckBox(new Coord(50, this.sz.y - 30), this, "Alphabetical sorting") {
        public void changed(boolean val) {
          super.changed(val);
          Config.alphasort = val;
          Utils.setprefb("alphasort", val);
          Charlist.this.prefchanged = true;
        }
      };
    alphacheck.a = Config.alphasort;
    CheckBox reverselist = new CheckBox(new Coord(20, this.sz.y - 30), this, "") {
        public void changed(boolean val) {
          super.changed(val);
          Config.reversesort = val;
          Utils.setprefb("reversesort", val);
        }
      };
    reverselist.a = Config.reversesort;
    TextEntry filter = new TextEntry(new Coord(225, this.sz.y - 30), new Coord(60, 18), this, "") {
        public void changed() {
          super.changed();
          Charlist.this.filterchanged = true;
          Charlist.this.filterstring = this.text;
        }
        
        public boolean type(char c, KeyEvent ke) {
          if (c == ':') {
            this.ui.root.entercmd();
            return true;
          } 
          return super.type(c, ke);
        }
      };
    filter.changed();
    Button logoutbutton = new Button(new Coord(295, this.sz.y - 32), Integer.valueOf(80), this, "Logout") {
        public void click() {
          try {
            this.ui.cons.run("lo");
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          } 
        }
      };
  }
  
  public void scroll(int amount) {
    this.y += amount;
    synchronized (this.chars) {
      if (this.y > this.chars.size() - this.height)
        this.y = this.chars.size() - this.height; 
    } 
    if (this.y < 0)
      this.y = 0; 
  }
  
  public void draw(GOut g) {
    Coord cc = new Coord((clu[0].getWidth() - (bg.sz()).x) / 2, 46);
    synchronized (this.chars) {
      if (this.charschanged) {
        this.alphachars = new ArrayList<>(this.chars);
        Collections.sort(this.alphachars, new Char.CharComparator());
      } 
      if (this.filterchanged || this.charschanged || this.prefchanged) {
        this.filteredchars = new ArrayList<>();
        for (Char c : Config.alphasort ? this.alphachars : this.chars) {
          if ("".equals(this.filterstring) || c.name.contains(this.filterstring))
            this.filteredchars.add(c); 
        } 
      } 
      if (this.filterchanged) {
        this.filterchanged = false;
        Utils.setpref("namefilt", this.filterstring);
      } 
      if (this.charschanged)
        this.charschanged = false; 
      if (this.prefchanged)
        this.prefchanged = false; 
      for (Char c : this.chars)
        c.plb.hide(); 
      int begin = Config.reversesort ? (this.filteredchars.size() - 1) : 0;
      int step = Config.reversesort ? -1 : 1;
      int end1 = Config.reversesort ? (1 - this.y) : (this.filteredchars.size() - this.y);
      int end1manip = Config.reversesort ? -1 : 1;
      int end2 = Config.reversesort ? (this.height + 1 - this.filteredchars.size()) : this.height;
      int end2manip = Config.reversesort ? -1 : 1;
      int ymanip = Config.reversesort ? -1 : 1;
      int i;
      for (i = begin; i * end2manip < end2 && i * end1manip < end1; i += step) {
        if (i + this.y * ymanip >= 0) {
          Char c = this.filteredchars.get(i + this.y * ymanip);
          g.image(bg, cc);
          c.plb.show();
          c.plb.c = cc.add(bg.sz()).sub(110, 30);
          g.image(c.nt.tex(), cc.add(15, 10));
          cc = cc.add(0, (bg.sz()).y + 1);
        } 
      } 
      if (this.filteredchars.size() > this.height) {
        this.sau.show();
        this.sad.show();
      } else {
        this.sau.hide();
        this.sad.hide();
      } 
    } 
    super.draw(g);
  }
  
  public boolean mousewheel(Coord c, int amount) {
    scroll(amount);
    return true;
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender instanceof Button) {
      synchronized (this.chars) {
        for (Char c : this.chars) {
          if (sender == c.plb) {
            Config.setCharName(c.name);
            if (!this.applyThis) {
              boolean origRenderDistanceOnOff = Utils.getprefb("render_distance_bool_value", false);
              boolean origHideObjectsOnOff = Utils.getprefb("hide_some_gobs", false);
              Config.render_distance_bool_value = origRenderDistanceOnOff;
              Config.hideSomeGobs = origHideObjectsOnOff;
              if (OptWnd2.aCB != null)
                OptWnd2.aCB.a = origRenderDistanceOnOff; 
              if (OptWnd2.aCB2 != null)
                OptWnd2.aCB2.a = origHideObjectsOnOff; 
              if (RenderDistanceOptWnd.aCB != null)
                RenderDistanceOptWnd.aCB.a = origRenderDistanceOnOff; 
              OCache.renderDistance = origRenderDistanceOnOff;
              OCache.hideSomeGobs = origHideObjectsOnOff;
              MapView.smallView = Config.mview_dist_small;
            } else {
              this.applyThis = false;
            } 
            wdgmsg("play", new Object[] { c.name });
          } 
        } 
      } 
    } else if (!(sender instanceof Avaview)) {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "add") {
      Char c = new Char((String)args[0]);
      List<Indir<Resource>> resl = new LinkedList<>();
      for (int i = 1; i < args.length; i++)
        resl.add(this.ui.sess.getres(((Integer)args[i]).intValue())); 
      c.plb = new Button(new Coord(0, 0), Integer.valueOf(100), this, "Play");
      c.plb.hide();
      synchronized (this.chars) {
        this.chars.add(c);
        this.charschanged = true;
        if (this.chars.size() > this.height) {
          this.sau.show();
          this.sad.show();
        } 
      } 
    } 
  }
}
