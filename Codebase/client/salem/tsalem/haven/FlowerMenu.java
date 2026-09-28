package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class FlowerMenu extends Widget {
  public static final Tex pbgl = Resource.loadtex("gfx/hud/fpl");
  
  public static final Tex pbgm = Resource.loadtex("gfx/hud/fpm");
  
  public static final Tex pbgr = Resource.loadtex("gfx/hud/fpr");
  
  static Color ptc = new Color(248, 240, 193);
  
  static Text.Foundry ptf = new Text.Foundry(new Font("SansSerif", 0, 12));
  
  static int ph = (pbgm.sz()).y;
  
  static int ppl = 8;
  
  Petal[] opts;
  
  private double fast_menu1;
  
  private double fast_menu2;
  
  private Petal autochoose = null;
  
  public static boolean crimesOn = false;
  
  static final String SKIN = "Skin";
  
  static final String LARD = "Gather Lard";
  
  static final String PLUCK = "Pluck";
  
  static final String EAT = "Eat";
  
  static final String EAT_ALL = "Eat All";
  
  static Petal skinPetal = null;
  
  static Petal lardPetal = null;
  
  static Petal pluckPetal = null;
  
  static Petal eatPetal = null;
  
  public static boolean interruptReceived = false;
  
  static boolean eatAllOn = false;
  
  @RName("sm")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      if (c.x == -1 && c.y == -1)
        c = parent.ui.lcc; 
      String[] opts = new String[args.length];
      for (int i = 0; i < args.length; i++)
        opts[i] = (String)args[i]; 
      return new FlowerMenu(c, parent, opts);
    }
  }
  
  public class Petal extends Widget {
    public String name;
    
    public double ta;
    
    public double tr;
    
    public int num;
    
    Tex text;
    
    double a = 1.0D;
    
    public Petal(String name) {
      super(Coord.z, Coord.z, FlowerMenu.this);
      this.name = name;
      this.text = new TexI(Utils.outline2((FlowerMenu.ptf.render(name, FlowerMenu.ptc)).img, Utils.contrast(FlowerMenu.ptc)));
      this.sz = new Coord((this.text.sz()).x + 25, FlowerMenu.ph);
      if (Config.fast_menu) {
        FlowerMenu.this.fast_menu1 = 0.0D;
        FlowerMenu.this.fast_menu2 = 0.0D;
      } else {
        FlowerMenu.this.fast_menu1 = 0.25D;
        FlowerMenu.this.fast_menu2 = 0.75D;
      } 
    }
    
    public void move(Coord c) {
      this.c = c.add(this.sz.div(2).inv());
    }
    
    public void move(double a, double r) {
      move(Coord.sc(a, r));
    }
    
    public void draw(GOut g) {
      g.chcolor(255, 255, 255, (int)(255.0D * this.a));
      g.image(FlowerMenu.pbgl, Coord.z);
      g.image(FlowerMenu.pbgm, new Coord((FlowerMenu.pbgl.sz()).x, 0), new Coord(this.sz.x - (FlowerMenu.pbgl.sz()).x - (FlowerMenu.pbgr.sz()).x, this.sz.y));
      g.image(FlowerMenu.pbgr, new Coord(this.sz.x - (FlowerMenu.pbgr.sz()).x, 0));
      g.image(this.text, this.sz.div(2).add(this.text.sz().div(2).inv()));
    }
    
    public boolean mousedown(Coord c, int button) {
      FlowerMenu.this.choose(this);
      return true;
    }
  }
  
  public class Opening extends Widget.NormAnim {
    Opening() {
      super(FlowerMenu.this.fast_menu1);
    }
    
    public void ntick(double s) {
      for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
        p.move(p.ta, p.tr * (2.0D - s));
        p.a = s;
      } 
    }
  }
  
  public class Chosen extends Widget.NormAnim {
    FlowerMenu.Petal chosen;
    
    Chosen(FlowerMenu.Petal c) {
      super(FlowerMenu.this.fast_menu2);
      this.chosen = c;
    }
    
    public void ntick(double s) {
      for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
        if (p == this.chosen) {
          if (s > 0.6D) {
            p.a = 1.0D - (s - 0.6D) / 0.4D;
          } else if (s < 0.3D) {
            p.move(p.ta, p.tr * (1.0D - s / 0.3D));
            p.a = 1.0D;
          } 
        } else if (s > 0.3D) {
          p.a = 0.0D;
        } else {
          p.a = 1.0D - s / 0.3D;
          p.move(p.ta - s * Math.PI, p.tr);
        } 
      } 
      if (s == 1.0D)
        FlowerMenu.this.ui.destroy(FlowerMenu.this); 
    }
  }
  
  public class Cancel extends Widget.NormAnim {
    Cancel() {
      super(FlowerMenu.this.fast_menu1);
    }
    
    public void ntick(double s) {
      for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
        p.move(p.ta, p.tr * (1.0D + s));
        p.a = 1.0D - s;
      } 
      if (s == 1.0D)
        FlowerMenu.this.ui.destroy(FlowerMenu.this); 
    }
  }
  
  private static Rectangle organize(Petal[] opts) {
    int l = 1, p = 0, i = 0;
    int lr = -1;
    Coord min = new Coord(2147483647, 2147483647);
    Coord max = new Coord(-2147483648, -2147483648);
    for (i = 0; i < opts.length; i++) {
      if (lr == -1)
        lr = 75 + 50 * (l - 1); 
      Petal petal = opts[i];
      petal.ta = 1.5707963267948966D - p * 6.283185307179586D / (l * ppl);
      petal.tr = lr;
      if (++p >= ppl * l) {
        l++;
        p = 0;
        lr = -1;
      } 
      Coord tc = Coord.sc(petal.ta, petal.tr).sub(petal.sz.div(2));
      max.x = Math.max(max.x, tc.x + petal.sz.x);
      max.y = Math.max(max.y, tc.y + petal.sz.y);
      min.x = Math.min(min.x, tc.x);
      min.y = Math.min(min.y, tc.y);
    } 
    return new Rectangle(min.x, min.y, max.x - min.x, max.y - min.y);
  }
  
  public FlowerMenu(Coord c, Widget parent, String... options) {
    super(c, Coord.z, parent);
    Petal study = null;
    if (Config.flower_study)
      for (int i = 0; i < options.length; i++) {
        if (options[i].equals("Study")) {
          study = new Petal(options[i]);
          study.num = i;
          break;
        } 
      }  
    if (study == null) {
      this.opts = new Petal[options.length];
      Petal skinPetal = null;
      Petal lardPetal = null;
      Petal pluckPetal = null;
      int uses = 0;
      try {
        uses = ItemData.getUses(WItem.lastIActItem.item.info());
      } catch (Exception exception) {}
      boolean hasUses = (uses > 0);
      boolean hasEatOptionWithShift = false;
      if (interruptReceived) {
        interruptReceived = false;
        eatAllOn = false;
      } 
      for (int i = 0; i < options.length; i++) {
        String name = options[i];
        Petal p = new Petal(name);
        p.num = i;
        boolean auto = (Config.AUTOCHOOSE.containsKey(name) && ((Boolean)Config.AUTOCHOOSE.get(name)).booleanValue());
        boolean single = (this.ui.modctrl && options.length == 1 && Config.singleItemCTRLChoose);
        if (!this.ui.modshift && (auto || single))
          this.autochoose = p; 
        this.opts[i] = p;
        if (name.equals("Skin"))
          skinPetal = p; 
        if (name.equals("Gather Lard"))
          lardPetal = p; 
        if (name.equals("Pluck"))
          pluckPetal = p; 
        if ((eatAllOn || this.ui.modshift) && !this.ui.modctrl && name.equals("Eat")) {
          eatPetal = p;
          hasEatOptionWithShift = true;
        } 
        if (hasEatOptionWithShift && hasUses && 
          i == options.length - 1) {
          Petal p2 = new Petal("Eat All");
          p2.num = options.length;
        } 
      } 
      if (lardPetal != null) {
        if (!this.ui.modshift && Config.AUTOCHOOSE.containsKey("Gather Lard") && ((Boolean)Config.AUTOCHOOSE.get("Gather Lard")).booleanValue())
          this.autochoose = lardPetal; 
      } else if (skinPetal != null) {
        if (!this.ui.modshift && Config.AUTOCHOOSE.containsKey("Skin") && ((Boolean)Config.AUTOCHOOSE.get("Skin")).booleanValue())
          this.autochoose = skinPetal; 
      } else if (pluckPetal != null) {
        if (!this.ui.modshift && Config.AUTOCHOOSE.containsKey("Pluck") && ((Boolean)Config.AUTOCHOOSE.get("Pluck")).booleanValue())
          this.autochoose = pluckPetal; 
      } else if (eatAllOn && eatPetal != null) {
        this.autochoose = eatPetal;
      } 
    } else {
      this.opts = new Petal[] { study };
    } 
    fitscreen(organize(this.opts));
    this.ui.grabmouse(this);
    this.ui.grabkeys(this);
    new Opening();
  }
  
  private void fitscreen(Rectangle rect) {
    Coord ssz = this.ui.gui.sz;
    Coord wsz = new Coord(rect.width, rect.height);
    Coord wc = this.c.add(rect.x, rect.y);
    if (wc.x < 0)
      this.c.x -= wc.x; 
    if (wc.y < 0)
      this.c.y -= wc.y; 
    if (wc.x + wsz.x > ssz.x)
      this.c.x -= wc.x + wsz.x - ssz.x; 
    if (wc.y + wsz.y > ssz.y)
      this.c.y -= wc.y + wsz.y - ssz.y; 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (!this.anims.isEmpty())
      return true; 
    if (!super.mousedown(c, button))
      choose((Petal)null); 
    return true;
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "cancel") {
      new Cancel();
      this.ui.grabmouse(null);
      this.ui.grabkeys(null);
    } else if (msg == "act") {
      new Chosen(this.opts[get(((Integer)args[0]).intValue())]);
      this.ui.grabmouse(null);
      this.ui.grabkeys(null);
      clickNextEat();
    } 
  }
  
  private boolean np(String opt) {
    if (Config.newbie_protection && !crimesOn)
      if (opt.equals("Desecrate") || opt.equals("Scalp")) {
        Utils.msgOut("You need to turn on Criminal acts to do this!", Color.RED);
        if (!Config.newbie_prot_hide_info)
          NewbieProtWnd.toggle(); 
        return true;
      }  
    return false;
  }
  
  private int get(int num) {
    int i = 0;
    for (Petal p : this.opts) {
      if (p.num == num)
        return i; 
      i++;
    } 
    return 0;
  }
  
  public void tick(double dt) {
    if (this.autochoose != null) {
      choose(this.autochoose);
      this.autochoose = null;
    } 
    super.tick(dt);
  }
  
  public void draw(GOut g) {
    draw(g, false);
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key >= '0' && key <= '9') {
      int opt = (key == '0') ? 10 : (key - 49);
      if (opt < this.opts.length)
        choose(this.opts[opt]); 
      this.ui.grabkeys(null);
      return true;
    } 
    if (key == '\033') {
      choose((Petal)null);
      this.ui.grabkeys(null);
      return true;
    } 
    return false;
  }
  
  public void choose(Petal option) {
    if (option == null || option.name == null || np(option.name)) {
      wdgmsg("cl", new Object[] { Integer.valueOf(-1) });
    } else {
      if (option.name.equals("Eat All") && eatPetal != null) {
        startEatAll();
        wdgmsg("cl", new Object[] { Integer.valueOf(eatPetal.num), Integer.valueOf(this.ui.modflags()) });
        eatPetal = null;
      } 
      wdgmsg("cl", new Object[] { Integer.valueOf(option.num), Integer.valueOf(this.ui.modflags()) });
    } 
  }
  
  private void startEatAll() {
    interruptReceived = false;
    eatAllOn = true;
  }
  
  private static void clickNextEat() {
    if (eatAllOn && !interruptReceived) {
      if (WItem.lastIActItem != null)
        WItem.lastIActItem.item.wdgmsg("iact", new Object[] { WItem.lastIActItem.c }); 
    } else {
      eatAllOn = false;
      eatPetal = null;
    } 
  }
}
