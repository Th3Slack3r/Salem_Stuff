package haven;

import java.awt.Color;

public class VMeter extends Widget {
  private static final Coord C2 = new Coord(1, 0);
  
  static Tex bg = Resource.loadtex("gfx/hud/vm-frame");
  
  static Tex fg = Resource.loadtex("gfx/hud/vm-tex");
  
  Color cl;
  
  int amount;
  
  private Tex amt = null;
  
  @RName("vm")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      Color cl;
      if (args.length > 4) {
        cl = new Color(((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), ((Integer)args[3]).intValue(), ((Integer)args[4]).intValue());
      } else if (args.length > 3) {
        cl = new Color(((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), ((Integer)args[3]).intValue());
      } else {
        cl = (Color)args[1];
      } 
      return new VMeter(c, parent, ((Integer)args[0]).intValue(), cl);
    }
  }
  
  public VMeter(Coord c, Widget parent, int amount, Color cl) {
    super(c, bg.sz().add(2, 12), parent);
    this.amount = amount;
    this.cl = cl;
  }
  
  private Tex amt() {
    if (this.amt == null)
      this.amt = Text.render(String.format("%d", new Object[] { Integer.valueOf(this.amount) })).tex(); 
    return this.amt;
  }
  
  public void draw(GOut g) {
    g.image(bg, C2);
    g.chcolor(this.cl);
    int h = this.sz.y - 18;
    h = h * this.amount / 100;
    g.image(fg, C2, new Coord(0, this.sz.y - 15 - h), this.sz.add(0, h));
    g.chcolor();
    g.aimage(amt(), new Coord(this.sz.x / 2, this.sz.y), 0.5D, 1.0D);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "set") {
      this.amount = ((Integer)args[0]).intValue();
      this.amt = null;
      if (args.length > 1)
        this.cl = (Color)args[1]; 
    } else if (msg == "col") {
      this.cl = (Color)args[0];
    } else {
      try {
        if (args != null && args.length > 0 && args[0] instanceof String) {
          String s = args[0].toString();
          if (s.contains("Tranquility")) {
            ((Window)this.parent).temperVMeterSum++;
            if (Config.animal_stat_tranquility) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
          } 
          if (s.contains("Immunity")) {
            if (Config.animal_stat_immunity) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
            ((Window)this.parent).temperVMeterSum++;
          } 
          if (s.contains("Metabolism")) {
            ((Window)this.parent).temperVMeterSum++;
            if (Config.animal_stat_metabolism) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
          } 
          if (s.contains("Size")) {
            ((Window)this.parent).temperVMeterSum++;
            if (Config.animal_stat_size) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
          } 
          if (s.contains("Productiivty") || s.contains("Productivity")) {
            ((Window)this.parent).temperVMeterSum++;
            if (Config.animal_stat_productivity) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
          } 
          if (s.contains("Longevity")) {
            ((Window)this.parent).temperVMeterSum++;
            if (Config.animal_stat_longevity) {
              int val = Integer.parseInt(s.split(":")[1]);
              ((Window)this.parent).temperValueSum += val - 50;
            } 
          } 
          if (((Window)this.parent).temperVMeterSum == 6)
            if (Config.domestic_animal_stats_to_log_chat) {
              String animalName = "Animal";
              try {
                if (this.parent instanceof Window) {
                  Window parent = (Window)this.parent;
                  animalName = parent.name;
                } 
              } catch (Exception exception) {}
              Utils.msgLog(animalName + ": " + (((Window)this.parent).temperValueSum - Config.domestic_animals_stats_offset));
            }  
        } 
      } catch (Exception exception) {}
      super.uimsg(msg, args);
    } 
  }
}
