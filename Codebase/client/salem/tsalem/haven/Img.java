package haven;

public class Img extends Widget {
  private Indir<Resource> res;
  
  private Tex img;
  
  public boolean hit = false;
  
  @RName("img")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      Indir<Resource> res;
      int a = 0;
      if (args[a] instanceof String) {
        String nm = (String)args[a++];
        int ver = (args.length > a) ? ((Integer)args[a++]).intValue() : -1;
        res = new Resource.Spec(nm, ver);
      } else {
        res = parent.ui.sess.getres(((Integer)args[a++]).intValue());
      } 
      Img ret = new Img(c, res, parent);
      if (args.length > a)
        ret.hit = (((Integer)args[a++]).intValue() != 0); 
      return ret;
    }
  }
  
  public void draw(GOut g) {
    if (this.res != null)
      try {
        this.img = ((Resource.Image)((Resource)this.res.get()).<Resource.Image>layer(Resource.imgc)).tex();
        resize(this.img.sz());
        this.res = null;
      } catch (Loading loading) {} 
    if (this.img != null)
      g.image(this.img, Coord.z); 
  }
  
  public Img(Coord c, Tex img, Widget parent) {
    super(c, img.sz(), parent);
    this.res = null;
    this.img = img;
  }
  
  public Img(Coord c, Indir<Resource> res, Widget parent) {
    super(c, Coord.z, parent);
    this.res = res;
    this.img = null;
  }
  
  public void uimsg(String name, Object... args) {
    if (name == "ch")
      if (args[0] instanceof String) {
        String nm = (String)args[0];
        int ver = (args.length > 1) ? ((Integer)args[1]).intValue() : -1;
        this.res = new Resource.Spec(nm, ver);
      } else {
        this.res = this.ui.sess.getres(((Integer)args[0]).intValue());
      }  
  }
  
  public boolean mousedown(Coord c, int button) {
    if (this.hit) {
      wdgmsg("click", new Object[] { c, Integer.valueOf(button), Integer.valueOf(this.ui.modflags()) });
      return true;
    } 
    return false;
  }
}
