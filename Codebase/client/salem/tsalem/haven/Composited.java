package haven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Composited implements Rendered {
  public final Skeleton skel;
  
  public final Skeleton.Pose pose;
  
  private final PoseMorph morph;
  
  private Collection<Model> mod = new LinkedList<>();
  
  private Collection<Equ> equ = new LinkedList<>();
  
  public Poses poses = new Poses();
  
  public List<MD> nmod = null;
  
  public List<MD> cmod = new LinkedList<>();
  
  public List<ED> nequ = null;
  
  public List<ED> cequ = new LinkedList<>();
  
  public class Poses {
    private final Skeleton.PoseMod[] mods;
    
    Skeleton.Pose old;
    
    float ipold = 0.0F;
    
    float ipol = 0.0F;
    
    public float limit = -1.0F;
    
    public boolean stat;
    
    public boolean ldone;
    
    public Poses() {
      this.mods = new Skeleton.PoseMod[0];
    }
    
    public Poses(List<? extends Skeleton.PoseMod> mods) {
      this.mods = mods.<Skeleton.PoseMod>toArray(new Skeleton.PoseMod[0]);
      this.stat = true;
      for (Skeleton.PoseMod mod : this.mods) {
        if (!mod.stat()) {
          this.stat = false;
          break;
        } 
      } 
    }
    
    private void rebuild() {
      Composited.this.pose.reset();
      for (Skeleton.PoseMod m : this.mods)
        m.apply(Composited.this.pose); 
      if (this.ipold > 0.0F)
        Composited.this.pose.blend(this.old, this.ipold); 
      Composited.this.pose.gbuild();
    }
    
    public void set(float ipol) {
      if ((this.ipol = ipol) > 0.0F) {
        Composited.this.skel.getClass();
        this.old = new Skeleton.Pose(Composited.this.skel, Composited.this.pose);
        this.ipold = 1.0F;
      } 
      Composited.this.poses = this;
      rebuild();
    }
    
    public void tick(float dt) {
      boolean build = false;
      if (this.limit >= 0.0F && (
        this.limit -= dt) < 0.0F)
        this.ldone = true; 
      boolean done = this.ldone;
      for (Skeleton.PoseMod m : this.mods) {
        m.tick(dt);
        if (!m.done())
          done = false; 
      } 
      if (!this.stat)
        build = true; 
      if (this.ipold > 0.0F) {
        if ((this.ipold -= dt / this.ipol) < 0.0F) {
          this.ipold = 0.0F;
          this.old = null;
        } 
        build = true;
      } 
      if (build)
        rebuild(); 
      if (done)
        done(); 
    }
    
    @Deprecated
    public void tick(float dt, double v) {
      tick(dt);
    }
    
    protected void done() {}
  }
  
  public Composited(Skeleton skel) {
    this.skel = skel;
    skel.getClass();
    this.pose = new Skeleton.Pose(skel, skel.bindpose);
    this.morph = new PoseMorph(this.pose);
  }
  
  private static final Rendered.Order modorder = new Rendered.Order<Model.Layer>() {
      public int mainz() {
        return 1;
      }
      
      private final Rendered.RComparator<Composited.Model.Layer> cmp = new Rendered.RComparator<Composited.Model.Layer>() {
          public int compare(Composited.Model.Layer a, Composited.Model.Layer b, GLState.Buffer sa, GLState.Buffer sb) {
            if (a.z1 != b.z1)
              return a.z1 - b.z1; 
            return a.z2 - b.z2;
          }
        };
      
      public Rendered.RComparator<Composited.Model.Layer> cmp() {
        return this.cmp;
      }
    };
  
  private class Model implements Rendered {
    private final MorphedMesh m;
    
    int z = 0;
    
    int lz = 0;
    
    private class Layer implements FRendered {
      private final Material mat;
      
      private final int z1;
      
      private final int z2;
      
      private Layer(Material mat, int z1, int z2) {
        this.mat = mat;
        this.z1 = z1;
        this.z2 = z2;
      }
      
      public void draw(GOut g) {
        Composited.Model.this.m.draw(g);
      }
      
      public void drawflat(GOut g) {
        if (this.z2 == 0)
          Composited.Model.this.m.drawflat(g); 
      }
      
      public boolean setup(RenderList r) {
        r.prepo(Composited.modorder);
        r.prepo(this.mat);
        return true;
      }
    }
    
    private final List<Layer> lay = new ArrayList<>();
    
    private Model(FastMesh m) {
      this.m = new MorphedMesh(m, Composited.this.morph);
    }
    
    private void addlay(Material mat) {
      this.lay.add(new Layer(mat, this.z, this.lz++));
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList r) {
      this.m.setup(r);
      for (Layer lay : this.lay)
        r.add(lay, null); 
      return false;
    }
  }
  
  private class SpriteEqu extends Equ {
    private final Sprite spr;
    
    private SpriteEqu(Composited.ED ed) {
      super(ed);
      this.spr = Sprite.create(null, ed.res.get(), new Message(0));
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      rl.add(this.spr, null);
      return false;
    }
    
    public void tick(int dt) {
      this.spr.tick(dt);
    }
  }
  
  private class LightEqu extends Equ {
    private final Light l;
    
    private LightEqu(Composited.ED ed) {
      super(ed);
      this.l = ((Light.Res)((Resource)ed.res.get()).<Light.Res>layer(Light.Res.class)).make();
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      rl.add(this.l, null);
      return false;
    }
  }
  
  private abstract class Equ implements Rendered {
    private final GLState et;
    
    private Equ(Composited.ED ed) {
      GLState bt;
      Skeleton.BoneOffset bo = ((Resource)ed.res.get()).<String, Skeleton.BoneOffset>layer(Skeleton.BoneOffset.class, ed.at);
      if (bo != null) {
        bt = bo.forpose(Composited.this.pose);
      } else {
        Skeleton.Bone bone = Composited.this.skel.bones.get(ed.at);
        bt = Composited.this.pose.bonetrans(bone.idx);
      } 
      if (ed.off.x != 0.0F || ed.off.y != 0.0F || ed.off.z != 0.0F) {
        this.et = GLState.compose(new GLState[] { bt, Location.xlate(ed.off) });
      } else {
        this.et = bt;
      } 
    }
    
    public void tick(int dt) {}
  }
  
  public static class MD implements Cloneable {
    public Indir<Resource> mod;
    
    public List<Indir<Resource>> tex;
    
    private Composited.Model real;
    
    public MD(Indir<Resource> mod, List<Indir<Resource>> tex) {
      this.mod = mod;
      this.tex = tex;
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof MD))
        return false; 
      MD m = (MD)o;
      return (this.mod.equals(m.mod) && this.tex.equals(m.tex));
    }
    
    public MD clone() {
      try {
        MD ret = (MD)super.clone();
        ret.tex = new LinkedList<>(this.tex);
        return ret;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      } 
    }
    
    public String toString() {
      return this.mod + "+" + this.tex;
    }
  }
  
  public static class ED implements Cloneable {
    public int t;
    
    public String at;
    
    public Indir<Resource> res;
    
    public Coord3f off;
    
    public ED(int t, String at, Indir<Resource> res, Coord3f off) {
      this.t = t;
      this.at = at;
      this.res = res;
      this.off = off;
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof ED))
        return false; 
      ED e = (ED)o;
      return (this.t == e.t && this.at.equals(e.at) && this.res.equals(e.res));
    }
    
    public ED clone() {
      try {
        ED ret = (ED)super.clone();
        return ret;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      } 
    }
  }
  
  private void nmod(boolean nocatch) {
    for (Iterator<MD> i = this.nmod.iterator(); i.hasNext(); ) {
      MD md = i.next();
      try {
        if (md.real == null) {
          FastMesh.MeshRes mr = ((Resource)md.mod.get()).<FastMesh.MeshRes>layer(FastMesh.MeshRes.class);
          md.real = new Model(mr.m);
          if (((Resource)md.mod.get()).name.equals("gfx/borka/male") || ((Resource)md.mod.get()).name.equals("gfx/borka/female"))
            md.real.z = -1; 
          this.mod.add(md.real);
        } 
        for (Iterator<Indir<Resource>> o = md.tex.iterator(); o.hasNext(); ) {
          Indir<Resource> res = o.next();
          for (Material.Res mr : ((Resource)res.get()).<Material.Res>layers(Material.Res.class))
            md.real.addlay(mr.get()); 
          o.remove();
        } 
        i.remove();
      } catch (Loading e) {
        if (nocatch)
          throw e; 
      } 
    } 
    if (this.nmod.isEmpty())
      this.nmod = null; 
  }
  
  private void nequ(boolean nocatch) {
    for (Iterator<ED> i = this.nequ.iterator(); i.hasNext(); ) {
      ED ed = i.next();
      try {
        if (ed.t == 0) {
          this.equ.add(new SpriteEqu(ed));
        } else if (ed.t == 1) {
          this.equ.add(new LightEqu(ed));
        } 
        i.remove();
      } catch (Loading loading) {}
    } 
    if (this.nequ.isEmpty())
      this.nequ = null; 
  }
  
  public void changes(boolean nocatch) {
    if (this.nmod != null)
      nmod(nocatch); 
    if (this.nequ != null)
      nequ(nocatch); 
  }
  
  public void changes() {
    changes(false);
  }
  
  public boolean setup(RenderList rl) {
    changes();
    for (Model mod : this.mod)
      rl.add(mod, null); 
    for (Equ equ : this.equ)
      rl.add(equ, equ.et); 
    return false;
  }
  
  public void draw(GOut g) {}
  
  public void tick(int dt) {
    if (this.poses != null)
      this.poses.tick(dt / 1000.0F); 
    for (Equ equ : this.equ)
      equ.tick(dt); 
  }
  
  @Deprecated
  public void tick(int dt, double v) {
    tick(dt);
  }
  
  public void chmod(List<MD> mod) {
    if (mod.equals(this.cmod))
      return; 
    this.mod = new LinkedList<>();
    this.nmod = new LinkedList<>();
    for (MD md : mod)
      this.nmod.add(md.clone()); 
    this.cmod = new ArrayList<>(mod);
  }
  
  public void chequ(List<ED> equ) {
    if (equ.equals(this.cequ))
      return; 
    this.equ = new LinkedList<>();
    this.nequ = new LinkedList<>();
    for (ED ed : equ)
      this.nequ.add(ed.clone()); 
    this.cequ = new ArrayList<>(equ);
  }
}
