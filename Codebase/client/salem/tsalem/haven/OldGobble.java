package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OldGobble extends SIWidget {
  public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/gbg");
  
  static Text.Foundry tnf = (new Text.Foundry(new Font("serif", 1, 16))).aa(true);
  
  public int[] lev = new int[4];
  
  public List<TypeMod> mods = new ArrayList<>();
  
  static final Color loc = new Color(0, 128, 255);
  
  static final Color hic = new Color(0, 128, 64);
  
  static final BufferedImage[] lobars;
  
  static final BufferedImage[] hibars;
  
  private boolean updt = true;
  
  public long lastUpdate = 0L;
  
  private final TypeList typelist;
  
  private int[] lmax = new int[4];
  
  private int max;
  
  private Tex lvlmask;
  
  private long lvltime;
  
  Tex[] texts = null;
  
  private Tex levels;
  
  private GobbleInfo lfood;
  
  static {
    int n = Tempers.bars.length;
    BufferedImage[] l = new BufferedImage[n];
    BufferedImage[] h = new BufferedImage[n];
    for (int i = 0; i < n; i++) {
      l[i] = PUtils.monochromize(Tempers.bars[i], loc);
      h[i] = PUtils.monochromize(Tempers.bars[i], hic);
    } 
    lobars = l;
    hibars = h;
  }
  
  public static class TypeMod {
    public final Indir<Resource> t;
    
    public double a;
    
    private Tex rn;
    
    private Tex rh;
    
    private Tex ra;
    
    public TypeMod(Indir<Resource> t, double a) {
      this.t = t;
      this.a = a;
    }
  }
  
  private class TypeList extends Widget {
    private int nw;
    
    private TypeList(Coord c, Widget parent) {
      super(c, Coord.z, parent);
    }
    
    public void tick(double dt) {
      if (OldGobble.this.updt) {
        this.nw = 0;
        int aw = 0;
        for (OldGobble.TypeMod m : OldGobble.this.mods) {
          if (m.rn == null)
            try {
              BufferedImage img = ((Resource.Image)((Resource)m.t.get()).layer((Class)Resource.imgc)).img;
              String nm = ((Resource.Tooltip)((Resource)m.t.get()).layer((Class)Resource.tooltip)).t;
              Text rt = OldGobble.tnf.render(nm);
              int i = Inventory.sqsz.y;
              BufferedImage buf = TexI.mkbuf(new Coord(img.getWidth() + 10 + (rt.sz()).x, i));
              Graphics g = buf.getGraphics();
              g.drawImage(img, 0, (i - img.getHeight()) / 2, null);
              g.drawImage(rt.img, img.getWidth() + 10, (i - (rt.sz()).y) / 2, null);
              g.dispose();
              m.rn = new TexI(PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 2, 1, new Color(32, 0, 0))));
              m.rh = new TexI(PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 2, 1, new Color(192, 128, 0))));
            } catch (Loading loading) {} 
          if (m.ra == null) {
            Text rt = OldGobble.tnf.render((int)Math.round(m.a * 100.0D) + "%", new Color(255, (int)(255.0D * m.a), (int)(255.0D * m.a)));
            m.ra = new TexI(PUtils.rasterimg(PUtils.blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
          } 
          this.nw = Math.max(this.nw, (m.rn.sz()).x);
          aw = Math.max(aw, (m.ra.sz()).x);
        } 
        int h = (Inventory.sqsz.y + 5) * OldGobble.this.mods.size();
        h += (OldGobble.this.levels.sz()).y + 20;
        resize(new Coord(Math.max(this.nw + 20 + aw, Tempers.boxsz.x), h));
        this.c = OldGobble.this.parentpos(this.parent).add(Tempers.boxc).add(0, Tempers.boxsz.y + 5);
        OldGobble.this.updt = false;
      } 
    }
    
    public void draw(GOut g) {
      int tn = 0;
      int y = 0;
      int h = Inventory.sqsz.y;
      boolean[] hl = new boolean[OldGobble.this.mods.size()];
      if (OldGobble.this.lfood != null)
        for (int t : OldGobble.this.lfood.types)
          hl[t] = true;  
      g.aimage(OldGobble.this.levels, new Coord(this.sz.x / 2, y), 0.5D, 0.0D);
      y += (OldGobble.this.levels.sz()).y + 20;
      for (OldGobble.TypeMod m : OldGobble.this.mods) {
        if (m.rn != null)
          g.image(hl[tn] ? m.rh : m.rn, new Coord(0, y)); 
        if (m.ra != null)
          g.image(m.ra, new Coord(this.nw + 20, y + (h - (m.ra.sz()).y) / 2)); 
        tn++;
        y += h + 5;
      } 
    }
  }
  
  public OldGobble(Coord c, Widget parent) {
    super(c, Utils.imgsz(Tempers.bg[0]), parent);
    lcount(0, Color.WHITE);
    this.typelist = new TypeList(Coord.z, getparent((Class)GameUI.class));
  }
  
  public void destroy() {
    this.typelist.destroy();
    super.destroy();
  }
  
  public void tick(double dt) {
    int max = 0;
    int[] lmax = new int[4];
    for (int i = 0; i < 4; i++) {
      lmax[i] = ((Glob.CAttr)this.ui.sess.glob.cattr.get(Tempers.anm[i])).base;
      if (lmax[i] == 0)
        return; 
      if (lmax[i] != this.lmax[i]) {
        redraw();
        if (this.lmax[i] != 0)
          this.ui.message(String.format("You have raised %s!", new Object[] { Tempers.rnm[i] }), GameUI.MsgType.GOOD); 
      } 
      max = Math.max(max, lmax[i]);
    } 
    this.lmax = lmax;
    this.max = max;
    GobbleInfo food = null;
    Alchemy ch = null;
    if (this.ui.lasttip instanceof WItem.ItemTip)
      try {
        food = ItemInfo.<GobbleInfo>find(GobbleInfo.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
        ch = ItemInfo.<Alchemy>find(Alchemy.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
      } catch (Loading loading) {} 
    if (this.lfood != food) {
      this.lfood = food;
      redraw();
    } 
  }
  
  public double foodeff(GobbleInfo food) {
    double ret = 1.0D;
    for (int t : food.types)
      ret *= ((TypeMod)this.mods.get(t)).a; 
    return ret;
  }
  
  private WritableRaster rgmeter(GobbleInfo food, double e, int t) {
    return PUtils.alphablit(Tempers.rmeter(hibars[t].getRaster(), this.lev[t] + (int)(e * food.h[t]), this.max), Tempers.rmeter(lobars[t].getRaster(), this.lev[t] + (int)(e * food.l[t]), this.max), Coord.z);
  }
  
  private WritableRaster lgmeter(GobbleInfo food, double e, int t) {
    return PUtils.alphablit(Tempers.lmeter(hibars[t].getRaster(), this.lev[t] + (int)(e * food.h[t]), this.max), Tempers.lmeter(lobars[t].getRaster(), this.lev[t] + (int)(e * food.l[t]), this.max), Coord.z);
  }
  
  public void draw(BufferedImage buf) {
    WritableRaster dst = buf.getRaster();
    PUtils.blit(dst, bg.getRaster(), Coord.z);
    PUtils.alphablit(dst, Tempers.rmeter(Tempers.sbars[0].getRaster(), this.lmax[0], this.max), Tempers.mc[0]);
    PUtils.alphablit(dst, Tempers.lmeter(Tempers.sbars[1].getRaster(), this.lmax[1], this.max), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
    PUtils.alphablit(dst, Tempers.lmeter(Tempers.sbars[2].getRaster(), this.lmax[2], this.max), Tempers.mc[2].sub(Tempers.bars[2].getWidth() - 1, 0));
    PUtils.alphablit(dst, Tempers.rmeter(Tempers.sbars[3].getRaster(), this.lmax[3], this.max), Tempers.mc[3]);
    if (this.lfood != null) {
      double e = foodeff(this.lfood);
      PUtils.alphablit(dst, rgmeter(this.lfood, e, 0), Tempers.mc[0]);
      PUtils.alphablit(dst, lgmeter(this.lfood, e, 1), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, lgmeter(this.lfood, e, 2), Tempers.mc[2].sub(Tempers.bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, rgmeter(this.lfood, e, 3), Tempers.mc[3]);
    } 
    PUtils.alphablit(dst, Tempers.rmeter(Tempers.bars[0].getRaster(), this.lev[0], this.max), Tempers.mc[0]);
    PUtils.alphablit(dst, Tempers.lmeter(Tempers.bars[1].getRaster(), this.lev[1], this.max), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
    PUtils.alphablit(dst, Tempers.lmeter(Tempers.bars[2].getRaster(), this.lev[2], this.max), Tempers.mc[2].sub(Tempers.bars[2].getWidth() - 1, 0));
    PUtils.alphablit(dst, Tempers.rmeter(Tempers.bars[3].getRaster(), this.lev[3], this.max), Tempers.mc[3]);
    StringBuilder tbuf = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      tbuf.append(String.format("%s: %s/%s\n", new Object[] { Tempers.rnm[i], Utils.fpformat(this.lev[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1) }));
    } 
    this.tooltip = RichText.render(tbuf.toString(), 0, new Object[0]).tex();
  }
  
  public void draw(GOut g) {
    super.draw(g);
    if (this.lvlmask != null) {
      long now = System.currentTimeMillis();
      if (now - this.lvltime > 1000L) {
        this.lvlmask.dispose();
        this.lvlmask = null;
      } else {
        g.chcolor(255, 255, 255, 255 - (int)(255L * (now - this.lvltime) / 1000L));
        g.image(this.lvlmask, Coord.z);
      } 
    } 
    if (Config.show_tempers) {
      if (this.texts == null) {
        this.texts = (Tex[])new TexI[4];
        for (int i = 0; i < 4; i++) {
          int attr = ((Glob.CAttr)this.ui.sess.glob.cattr.get(Tempers.anm[i])).comp;
          String str = String.format("%s / %s (%s)", new Object[] { Utils.fpformat(this.lev[i], 3, 1), Utils.fpformat(this.max, 3, 1), Utils.fpformat(attr, 3, 1) });
          this.texts[i] = Tempers.text(str);
        } 
      } 
      g.aimage(this.texts[0], Tempers.mc[0].add(Tempers.bars[0].getWidth() / 2, Tempers.bars[0].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[1], Tempers.mc[1].add(-Tempers.bars[1].getWidth() / 2, Tempers.bars[1].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[2], Tempers.mc[2].add(-Tempers.bars[2].getWidth() / 2, Tempers.bars[2].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[3], Tempers.mc[3].add(Tempers.bars[3].getWidth() / 2, Tempers.bars[3].getHeight() / 2 - 1), 0.5D, 0.5D);
    } 
  }
  
  public void updt(int[] n) {
    this.lev = n;
    this.texts = null;
    redraw();
  }
  
  public void lvlup(int a) {
    WritableRaster buf = PUtils.imgraster(PUtils.imgsz(bg));
    if (a == 0 || a == 3) {
      PUtils.alphablit(buf, Tempers.rmeter(Tempers.bars[a].getRaster(), 1, 1), Tempers.mc[a]);
    } else {
      PUtils.alphablit(buf, Tempers.lmeter(Tempers.bars[a].getRaster(), 1, 1), Tempers.mc[a].sub(Tempers.bars[a].getWidth() - 1, 0));
    } 
    PUtils.imgblur(buf, 2, 2.0D);
    this.lvlmask = new TexI(PUtils.rasterimg(buf));
    this.lvltime = System.currentTimeMillis();
  }
  
  public void lcount(int n, Color c) {
    Text rt = tnf.render(String.format("Gobble Points: %d", new Object[] { Integer.valueOf(n) }), c);
    this.levels = new TexI(PUtils.rasterimg(PUtils.blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
  }
  
  public void typemod(Indir<Resource> t, double a) {
    this.lastUpdate = System.currentTimeMillis();
    this.updt = true;
    for (TypeMod m : this.mods) {
      if (m.t == t) {
        m.a = a;
        m.ra = null;
        return;
      } 
    } 
    this.mods.add(new TypeMod(t, a));
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (!c.isect(Tempers.boxc, Tempers.boxsz))
      return null; 
    return super.tooltip(c, prev);
  }
  
  public static final Collection<String> msgs = Arrays.asList(new String[] { "gtm", "glvlup", "glvls", "gtypemod" });
  
  public void uimsg(String msg, Object... args) {
    if (msg == "gtm") {
      int[] n = new int[4];
      for (int i = 0; i < 4; i++)
        n[i] = ((Integer)args[i]).intValue(); 
      updt(n);
    } else if (msg == "glvlup") {
      lvlup(((Integer)args[0]).intValue());
    } else if (msg == "glvls") {
      lcount(((Integer)args[0]).intValue(), (Color)args[1]);
    } else if (msg == "gtypemod") {
      typemod(this.ui.sess.getres(((Integer)args[0]).intValue()), ((Integer)args[1]).intValue() / 100.0D);
    } else {
      super.uimsg(msg, args);
    } 
  }
}
