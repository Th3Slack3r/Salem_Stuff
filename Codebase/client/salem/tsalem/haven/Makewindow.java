package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class Makewindow extends Widget {
  Widget obtn;
  
  Widget cbtn;
  
  Widget bbtn;
  
  Spec[] inputs = new Spec[0];
  
  Spec[] outputs = new Spec[0];
  
  static LinkedList<StringAndStringArray> list = new LinkedList<>();
  
  static boolean requested_restore = false;
  
  static Coord boff = new Coord(7, 9);
  
  final int xoff = 40;
  
  final int yoff = 60;
  
  public static final Text.Foundry nmf = new Text.Foundry(new Font("Serif", 0, 20));
  
  private final StockBin.Value value;
  
  private final Label vLabel;
  
  private final CheckBox turnOnCraftAmountCBox;
  
  public static boolean turnOnCraftAmount = true;
  
  private static ReentrantLock lock = new ReentrantLock();
  
  public static String[] lastCraftOrBeltAction = null;
  
  private String[] localLastCraftOrBeltAction = null;
  
  private long hoverstart;
  
  private Resource lasttip;
  
  private Object stip;
  
  private Object ltip;
  
  @RName("make")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Makewindow(c, parent, (String)args[0]);
    }
  }
  
  public static class Spec {
    public Indir<Resource> res;
    
    public Tex num;
    
    public Spec(Indir<Resource> res, int num) {
      this.res = res;
      if (num >= 0) {
        this.num = new TexI(Utils.outline2((Text.render(Integer.toString(num), Color.WHITE)).img, Utils.contrast(Color.WHITE)));
      } else {
        this.num = null;
      } 
    }
  }
  
  public Makewindow(Coord c, Widget parent, String rcpnm) {
    super(c, Coord.z, parent);
    this.localLastCraftOrBeltAction = lastCraftOrBeltAction;
    Label nm = new Label(new Coord(0, 0), this, rcpnm, nmf);
    nm.c = new Coord(this.sz.x - nm.sz.x, 0);
    new Label(new Coord(0, 20), this, "Input:");
    new Label(new Coord(0, 80), this, "Result:");
    this.turnOnCraftAmountCBox = new CheckBox(new Coord(150, 70), this, "activate craft amount") {
        public void changed(boolean val) {
          super.changed(val);
          Config.activateCraftAmount = val;
          Utils.setprefb("activate_craft_amount", val);
          Makewindow.turnOnCraftAmount = val;
          if (Makewindow.turnOnCraftAmount) {
            Makewindow.this.value.setcanfocus(true);
            Makewindow.this.value.visible = true;
            Makewindow.this.vLabel.visible = true;
          } else {
            Makewindow.this.value.setcanfocus(false);
            Makewindow.this.value.visible = false;
            Makewindow.this.vLabel.visible = false;
          } 
        }
      };
    turnOnCraftAmount = Config.activateCraftAmount;
    this.turnOnCraftAmountCBox.a = turnOnCraftAmount;
    this.vLabel = new Label(new Coord(290, 60), this, "Amount to craft:");
    this.value = new StockBin.Value(new Coord(380, 60), 35, this, "");
    this.value.canactivate = true;
    if (turnOnCraftAmount) {
      this.value.setcanfocus(true);
      this.value.visible = true;
      this.vLabel.visible = true;
    } else {
      this.value.setcanfocus(false);
      this.value.visible = false;
      this.vLabel.visible = false;
    } 
    this.obtn = new Button(new Coord(290, 93), Integer.valueOf(60), this, "Craft");
    this.cbtn = new Button(new Coord(360, 93), Integer.valueOf(60), this, "Craft All");
    if (list.size() > 0 && list.peek() != null) {
      this.bbtn = new Button(new Coord(220, 93), Integer.valueOf(60), this, ((StringAndStringArray)list.peek()).key);
      this.bbtn.pack();
      this.bbtn.c.x = 280 - this.bbtn.sz.x;
    } 
    pack();
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "inpop") {
      Spec[] inputs = new Spec[args.length / 2];
      for (int i = 0, a = 0; a < args.length; i++, a += 2)
        inputs[i] = new Spec(this.ui.sess.getres(((Integer)args[a]).intValue()), ((Integer)args[a + 1]).intValue()); 
      this.inputs = inputs;
    } else if (msg == "opop") {
      Spec[] outputs = new Spec[args.length / 2];
      for (int i = 0, a = 0; a < args.length; i++, a += 2)
        outputs[i] = new Spec(this.ui.sess.getres(((Integer)args[a]).intValue()), ((Integer)args[a + 1]).intValue()); 
      this.outputs = outputs;
    } 
  }
  
  public void draw(GOut g) {
    getClass();
    Coord c = new Coord(40, 0);
    Inventory.invsq(g, c, new Coord(this.inputs.length, 1));
    int i;
    for (i = 0; i < this.inputs.length; i++) {
      Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
      Spec s = this.inputs[i];
      try {
        if (s.res.get() == null || ((Resource)s.res.get()).layer(Resource.imgc) == null)
          throw new Loading("Some part of this resource image is null!"); 
        g.image(((Resource.Image)((Resource)s.res.get()).<Resource.Image>layer(Resource.imgc)).tex(), ic);
      } catch (Loading loading) {}
      if (s.num != null)
        g.aimage(s.num, ic.add(Inventory.isqsz), 1.0D, 1.0D); 
    } 
    getClass();
    getClass();
    c = new Coord(40, 60);
    Inventory.invsq(g, c, new Coord(this.outputs.length, 1));
    for (i = 0; i < this.outputs.length; i++) {
      Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
      Spec s = this.outputs[i];
      try {
        g.image(((Resource.Image)((Resource)s.res.get()).<Resource.Image>layer(Resource.imgc)).tex(), ic);
      } catch (Loading loading) {}
      if (s.num != null)
        g.aimage(s.num, ic.add(Inventory.isqsz), 1.0D, 1.0D); 
    } 
    super.draw(g);
  }
  
  public Object tooltip(Coord mc, Widget prev) {
    return tooltip(mc, prev, true);
  }
  
  public Object tooltip(Coord mc, Widget prev, boolean full) {
    Resource tres = null;
    getClass();
    Coord c = new Coord(40, 0);
    int i;
    for (i = 0; i < this.inputs.length; i++) {
      if (mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz)) {
        tres = (this.inputs[i]).res.get();
        // Byte code: goto -> 177
      } 
    } 
    getClass();
    getClass();
    c = new Coord(40, 60);
    i = 0;
    if (i < this.outputs.length && 
      mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz))
      tres = (this.outputs[i]).res.get(); 
    Resource.Tooltip layer = null;
    if (tres != null)
      layer = tres.<Resource.Tooltip>layer(Resource.tooltip); 
    String tip = (layer != null) ? layer.t : "<MISSING TOOLTIP>";
    if (!full)
      return (tres == null) ? null : tip; 
    if (tres == null)
      return null; 
    if (this.lasttip != tres) {
      this.lasttip = tres;
      this.stip = this.ltip = null;
    } 
    long now = System.currentTimeMillis();
    boolean sh = true;
    if (prev != this) {
      this.hoverstart = now;
    } else if (now - this.hoverstart > 1000L) {
      sh = false;
    } 
    if (sh) {
      if (this.stip == null && tip != null)
        this.stip = Text.render(tip); 
      return this.stip;
    } 
    if (this.ltip == null && tip != null) {
      String t = tip;
      t = t + "\n" + tres.name;
      Resource.Pagina p = tres.<Resource.Pagina>layer(Resource.pagina);
      if (p != null)
        t = t + "\n\n" + ((Resource.Pagina)tres.layer((Class)Resource.pagina)).text; 
      RichText ttip = RichText.render(t, 300, new Object[0]);
      this.ltip = checkVars(tres.name, ttip);
    } 
    return this.ltip;
  }
  
  private Object checkVars(String name, RichText ltip) {
    ItemData data = ItemData.get(name);
    if (data != null && data.variants != null) {
      BufferedImage longtip = data.variants.create().longtip();
      return new TexI(ItemInfo.catimgs(3, new BufferedImage[] { ltip.img, longtip }));
    } 
    return ltip;
  }
  
  public boolean mousedown(Coord c, int button) {
    Object tt = tooltip(c, (Widget)null, false);
    if (tt != null && tt instanceof String) {
      Glob.Pagina p = this.ui.mnu.paginafor((String)tt);
      if (p != null) {
        store();
        this.ui.mnu.use(p);
        return true;
      } 
    } 
    return super.mousedown(c, button);
  }
  
  public void destroy() {
    if (!requested_restore)
      store(); 
    requested_restore = false;
    super.destroy();
  }
  
  private void store() {
    try {
      String t = ((Resource.Tooltip)((Resource)(this.outputs[0]).res.get()).layer((Class)Resource.tooltip)).t;
      if (list.isEmpty() || !((StringAndStringArray)list.getFirst()).key.equals(t))
        list.push(new StringAndStringArray(t, this.localLastCraftOrBeltAction)); 
    } catch (Exception e) {
      e.printStackTrace(System.out);
    } 
  }
  
  private void restore() {
    try {
      StringAndStringArray pair = list.pop();
      String name = pair.key;
      String[] alternative = pair.value;
      requested_restore = true;
      Glob.Pagina p = this.ui.mnu.paginafor(name);
      if (p == null && alternative != null && (alternative.length == 3 || alternative.length == 4)) {
        if (alternative.length == 3) {
          this.ui.wdgmsg(this.ui.gui, alternative[0], new Object[] { alternative[1], alternative[2] });
          lastCraftOrBeltAction = alternative;
        } else {
          this.ui.wdgmsg(this.ui.gui, alternative[0], new Object[] { alternative[1], alternative[2], alternative[3] });
          lastCraftOrBeltAction = alternative;
        } 
      } else {
        this.ui.mnu.use(p);
      } 
    } catch (Exception e) {
      e.printStackTrace(System.out);
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.obtn) {
      if (msg == "activate")
        wdgmsg("make", new Object[] { Integer.valueOf(0) }); 
      return;
    } 
    if ((sender == this.cbtn || sender == this.value) && 
      msg == "activate") {
      int amount = 0;
      if (Config.activateCraftAmount)
        try {
          if (null != this.value.text && this.value.text.length() > 0)
            amount = Integer.parseInt(this.value.text); 
        } catch (Exception e) {
          this.ui.message("parse exception: " + e.getMessage(), GameUI.MsgType.INFO);
        }  
      if (amount == 0) {
        wdgmsg("make", new Object[] { Integer.valueOf(1) });
        return;
      } 
      wdgmsg("make", new Object[] { Integer.valueOf(1) });
      final int currentValue = amount;
      GameUI.craftP = false;
      (new Thread(new Runnable() {
            public void run() {
              try {
                int counter = 0;
                while (Makewindow.this.getProgress() == -1) {
                  Makewindow.this.sleep(10);
                  counter++;
                  if (counter >= 1000 || GameUI.craftP)
                    return; 
                } 
                int timeOut = 0;
                synchronized (Makewindow.lock) {
                  GameUI.craftP = true;
                } 
                GameUI.craftA = 0;
                while (GameUI.craftP) {
                  Makewindow.this.sleep(10);
                  if (Makewindow.this.getProgress() == -1) {
                    timeOut++;
                  } else {
                    timeOut = 0;
                  } 
                  if (timeOut > 1000) {
                    Utils.msgOut("crafting timed out at: " + GameUI.craftA + " / " + currentValue);
                    GameUI.craftP = false;
                  } 
                  if (GameUI.craftA >= currentValue) {
                    Utils.msgOut("finished crafting: " + GameUI.craftA + " / " + currentValue);
                    GameUI.craftP = false;
                  } 
                } 
              } catch (Exception exception) {}
              Makewindow.this.ui.wdgmsg(Makewindow.this.ui.gui.map, "click", new Object[] { (this.this$0.ui.gui.map.player()).sc, (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf((int)(this.this$0.ui.gui.map.player()).id), (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(0), Integer.valueOf(-1) });
              Makewindow.this.store();
              Makewindow.this.restore();
              GameUI.craftP = false;
            }
          }"CraftCounter")).start();
    } 
    if (sender == this.bbtn) {
      restore();
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  public boolean globtype(char ch, KeyEvent ev) {
    if (ch == '\n') {
      wdgmsg("make", new Object[] { Integer.valueOf(this.ui.modctrl ? 1 : 0) });
      return true;
    } 
    return super.globtype(ch, ev);
  }
  
  public static class MakePrep extends ItemInfo implements GItem.ColorInfo {
    private static final Color olcol = new Color(0, 255, 0, 64);
    
    public MakePrep(ItemInfo.Owner owner) {
      super(owner);
    }
    
    public Color olcol() {
      return olcol;
    }
  }
  
  private void sleep(int timeInMiliS) {
    try {
      Thread.sleep(timeInMiliS);
    } catch (InterruptedException interruptedException) {}
  }
  
  private int getProgress() {
    return this.ui.gui.prog;
  }
}
