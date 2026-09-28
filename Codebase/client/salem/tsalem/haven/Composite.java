package haven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Composite extends Drawable {
  public static final float ipollen = 0.2F;
  
  public final Indir<Resource> base;
  
  public Composited comp;
  
  private Collection<ResData> nposes = null;
  
  private Collection<ResData> tposes = null;
  
  private boolean retainequ = false;
  
  private float tptime;
  
  private WrapMode tpmode;
  
  public int pseq;
  
  private List<Composited.MD> nmod;
  
  private List<Composited.ED> nequ;
  
  boolean manualRadius = false;
  
  boolean show_radius = false;
  
  List<Gob.Overlay> radii = new ArrayList<>();
  
  public Collection<ResData> rusty_lastposes = null;
  
  private long t = System.currentTimeMillis();
  
  private int delay = 250;
  
  Boolean dead = null;
  
  private boolean isAnimal = false;
  
  private boolean checkedName = false;
  
  private boolean checkedPose = false;
  
  private boolean knockPose = false;
  
  private boolean hasStars = false;
  
  private static ArrayList<String> names = new ArrayList<>();
  
  public Composite(Gob gob, Indir<Resource> base) {
    super(gob);
    names.add("calf");
    names.add("kid");
    names.add("piglet");
    names.add("lamb");
    names.add("cow");
    names.add("bull");
    names.add("male");
    names.add("female");
    names.add("stillborn");
    names.add("deer");
    names.add("bear");
    names.add("borka");
    this.base = base;
  }
  
  private void init() {
    if (this.comp != null)
      return; 
    this.comp = new Composited(((Skeleton.Res)((Resource)this.base.get()).layer((Class)Skeleton.Res.class)).s);
    String name = ((Resource)this.base.get()).name;
    this.radii.addAll(ColoredRadius.getRadii(name, this.gob));
  }
  
  public Resource getres() {
    return this.base.get();
  }
  
  private void checkRadius() {
    if (this.manualRadius) {
      if (!this.show_radius) {
        this.gob.ols.removeAll(this.radii);
        this.gob.ols.addAll(this.radii);
        this.show_radius = true;
      } 
      return;
    } 
    if (this.show_radius != Config.show_radius) {
      this.show_radius = Config.show_radius;
      this.gob.ols.removeAll(this.radii);
      if (Config.show_radius || this.show_radius)
        this.gob.ols.addAll(this.radii); 
    } 
  }
  
  public void setup(RenderList rl) {
    try {
      init();
    } catch (Loading e) {
      return;
    } 
    checkRadius();
    rl.add(this.comp, null);
  }
  
  private List<Skeleton.PoseMod> loadposes(Collection<ResData> rl, Skeleton skel) {
    List<Skeleton.PoseMod> mods = new ArrayList<>(rl.size());
    for (ResData dat : rl)
      mods.add(skel.mkposemod(this.gob, dat.res.get(), dat.sdt)); 
    return mods;
  }
  
  private List<Skeleton.PoseMod> loadposes(Collection<ResData> rl, Skeleton skel, WrapMode mode) {
    List<Skeleton.PoseMod> mods = new ArrayList<>(rl.size());
    for (ResData dat : rl) {
      for (Skeleton.ResPose p : ((Resource)dat.res.get()).<Skeleton.ResPose>layers(Skeleton.ResPose.class))
        mods.add(p.forskel(this.gob, skel, (mode == null) ? p.defmode : mode)); 
    } 
    return mods;
  }
  
  private void updequ() {
    this.retainequ = false;
    if (this.nmod != null) {
      this.comp.chmod(this.nmod);
      this.nmod = null;
    } 
    if (this.nequ != null) {
      this.comp.chequ(this.nequ);
      this.nequ = null;
    } 
  }
  
  public void ctick(int dt) {
    if (this.comp == null)
      return; 
    if (this.gob != null && this.gob.wasHidden) {
      this.gob.wasHidden = false;
      this.t = System.currentTimeMillis();
    } 
    if (this.nposes != null) {
      try {
        this.comp.getClass();
        Composited.Poses np = new Composited.Poses(this.comp, loadposes(this.nposes, this.comp.skel));
        np.set(0.2F);
        this.nposes = null;
      } catch (Loading loading) {}
    } else if (this.tposes != null) {
      try {
        final Composited.Poses cp = this.comp.poses;
        this.comp.getClass();
        Composited.Poses np = new Composited.Poses(this.comp, loadposes(this.tposes, this.comp.skel, this.tpmode)) {
            protected void done() {
              cp.set(0.2F);
              Composite.this.updequ();
            }
          };
        np.limit = this.tptime;
        np.set(0.2F);
        this.tposes = null;
        this.retainequ = true;
      } catch (Loading loading) {}
    } else if (!this.retainequ) {
      updequ();
    } 
    if (this.isAnimal && this.delay > 0)
      try {
        List<Composited.ED> cequ = this.comp.cequ;
        for (Composited.ED ed : cequ) {
          if (((Resource)ed.res.get()).name.toLowerCase().contains("gfx/fx/stars"))
            this.hasStars = true; 
        } 
      } catch (Exception exception) {} 
    if (!this.hasStars && (Config.remove_animations || (this.dead != null && this.dead.booleanValue())))
      return; 
    if (!this.checkedName) {
      this.checkedName = true;
      this.isAnimal = false;
      for (String string : names) {
        if (((Resource)this.base.get()).name.contains(string)) {
          this.isAnimal = true;
          break;
        } 
      } 
    } 
    if (this.checkedName && !this.isAnimal) {
      this.checkedPose = true;
      this.knockPose = false;
      this.dead = Boolean.valueOf(false);
    } 
    if (this.isAnimal && !this.checkedPose) {
      this.checkedPose = true;
      this.knockPose = false;
      this.dead = Boolean.valueOf(false);
      if (this.rusty_lastposes != null)
        for (ResData resData : this.rusty_lastposes) {
          try {
            if (((Resource)resData.res.get()).name.contains("/knock")) {
              this.knockPose = true;
              this.dead = null;
              break;
            } 
          } catch (Exception e) {
            this.checkedPose = false;
          } 
        }  
    } 
    if (!this.hasStars && this.isAnimal && this.knockPose && this.t + 1000L > System.currentTimeMillis()) {
      this.dead = Boolean.valueOf(true);
      this.comp.tick(10000);
      return;
    } 
    if (!this.hasStars && this.isAnimal && this.knockPose)
      if (this.delay > 0) {
        this.delay--;
      } else {
        this.dead = Boolean.valueOf(true);
        return;
      }  
    this.comp.tick(dt);
  }
  
  public Resource.Neg getneg() {
    return ((Resource)this.base.get()).<Resource.Neg>layer(Resource.negc);
  }
  
  public Skeleton.Pose getpose() {
    init();
    return this.comp.pose;
  }
  
  public void chposes(Collection<ResData> poses, boolean interp) {
    if (this.tposes != null)
      this.tposes = null; 
    this.rusty_lastposes = poses;
    this.checkedPose = false;
    if ((!this.checkedName || this.isAnimal) && 
      this.rusty_lastposes != null)
      for (ResData resData : poses) {
        try {
          if (((Resource)resData.res.get()).name.contains("/knock")) {
            this.dead = null;
            this.checkedPose = false;
            this.knockPose = false;
            this.delay = 250;
            this.hasStars = false;
          } 
        } catch (Exception exception) {}
      }  
    this.nposes = poses;
  }
  
  @Deprecated
  public void chposes(List<Indir<Resource>> poses, boolean interp) {
    chposes(ResData.wrap(poses), interp);
  }
  
  public void tposes(Collection<ResData> poses, WrapMode mode, float time) {
    this.tposes = poses;
    this.tpmode = mode;
    this.tptime = time;
  }
  
  @Deprecated
  public void tposes(List<Indir<Resource>> poses, WrapMode mode, float time) {
    tposes(ResData.wrap(poses), mode, time);
  }
  
  public void chmod(List<Composited.MD> mod) {
    this.nmod = mod;
  }
  
  public void chequ(List<Composited.ED> equ) {
    this.nequ = equ;
  }
  
  public static float[][] deepCopy(float[][] original) {
    if (original == null)
      return (float[][])null; 
    float[][] result = new float[original.length][];
    for (int i = 0; i < original.length; i++) {
      result[i] = new float[(original[i]).length];
      for (int j = 0; j < (result[i]).length; j++)
        result[i][j] = original[i][j]; 
    } 
    return result;
  }
  
  public static boolean compArrays(float[][] a, float[][] b) {
    if (b == null || a == null)
      return false; 
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < (a[i]).length; j++) {
        if (a[i][j] != b[i][j])
          return false; 
      } 
    } 
    return true;
  }
}
