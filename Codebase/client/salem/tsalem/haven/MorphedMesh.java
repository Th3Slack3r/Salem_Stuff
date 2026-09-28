package haven;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class MorphedMesh extends FastMesh implements ResPart {
  private static Map<Morpher.Factory, Collection<MorphedBuf>> bufs = new CacheMap<>(CacheMap.RefType.WEAK);
  
  private static MorphedBuf buf(VertexBuf buf, Morpher.Factory morph) {
    Collection<MorphedBuf> bl;
    synchronized (bufs) {
      bl = bufs.get(morph);
      if (bl == null)
        bufs.put(morph, bl = new LinkedList<>()); 
    } 
    synchronized (bl) {
      for (MorphedBuf morphedBuf : bl) {
        if (morphedBuf.from == buf)
          return morphedBuf; 
      } 
      MorphedBuf b = new MorphedBuf(buf, morph);
      bl.add(b);
      return b;
    } 
  }
  
  public MorphedMesh(FastMesh mesh, Morpher.Factory pose) {
    super(mesh, buf(mesh.vert, pose));
  }
  
  public boolean setup(RenderList rl) {
    ((MorphedBuf)this.vert).update();
    return super.setup(rl);
  }
  
  protected boolean compile() {
    return false;
  }
  
  public int partid() {
    if (this.from instanceof ResPart)
      return ((ResPart)this.from).partid(); 
    return -1;
  }
  
  public String toString() {
    return "morphed(" + this.from + ")";
  }
  
  public static class MorphedBuf extends VertexBuf {
    public final VertexBuf from;
    
    private final MorphedMesh.Morpher morph;
    
    private static VertexBuf.AttribArray[] ohBitterSweetJavaDays(VertexBuf from) {
      VertexBuf.AttribArray[] ret = new VertexBuf.AttribArray[from.bufs.length];
      for (int i = 0; i < from.bufs.length; i++) {
        if (from.bufs[i] instanceof VertexBuf.VertexArray) {
          ret[i] = ((VertexBuf.VertexArray)from.bufs[i]).dup();
          ret[i].vbomode(35048);
        } else if (from.bufs[i] instanceof VertexBuf.NormalArray) {
          ret[i] = ((VertexBuf.NormalArray)from.bufs[i]).dup();
          ret[i].vbomode(35048);
        } else if (from.bufs[i] instanceof PoseMorph.BoneArray) {
          ret[i] = ((PoseMorph.BoneArray)from.bufs[i]).dup();
        } else {
          ret[i] = from.bufs[i];
        } 
      } 
      return ret;
    }
    
    private MorphedBuf(VertexBuf buf, MorphedMesh.Morpher.Factory morph) {
      super(ohBitterSweetJavaDays(buf));
      this.from = buf;
      this.morph = morph.create(this);
    }
    
    public void update() {
      if (!this.morph.update())
        return; 
      VertexBuf.VertexArray apos = buf(VertexBuf.VertexArray.class);
      VertexBuf.NormalArray anrm = buf(VertexBuf.NormalArray.class);
      FloatBuffer opos = ((VertexBuf.VertexArray)this.from.buf((Class)VertexBuf.VertexArray.class)).data, onrm = ((VertexBuf.NormalArray)this.from.buf((Class)VertexBuf.NormalArray.class)).data;
      FloatBuffer npos = apos.data, nnrm = anrm.data;
      this.morph.morphp(npos, opos);
      this.morph.morphd(nnrm, onrm);
      apos.update();
      anrm.update();
    }
  }
  
  public static Morpher.Factory combine(Morpher.Factory... parts) {
    return new Morpher.Factory() {
        public MorphedMesh.Morpher create(MorphedMesh.MorphedBuf vb) {
          final MorphedMesh.Morpher[] mparts = new MorphedMesh.Morpher[parts.length];
          for (int i = 0; i < parts.length; i++)
            mparts[i] = parts[i].create(vb); 
          return new MorphedMesh.Morpher() {
              public boolean update() {
                boolean ret = false;
                for (MorphedMesh.Morpher p : mparts) {
                  if (p.update())
                    ret = true; 
                } 
                return ret;
              }
              
              public void morphp(FloatBuffer dst, FloatBuffer src) {
                for (MorphedMesh.Morpher p : mparts) {
                  p.morphp(dst, src);
                  src = dst;
                } 
              }
              
              public void morphd(FloatBuffer dst, FloatBuffer src) {
                for (MorphedMesh.Morpher p : mparts) {
                  p.morphd(dst, src);
                  src = dst;
                } 
              }
            };
        }
      };
  }
  
  public static interface Morpher {
    boolean update();
    
    void morphp(FloatBuffer param1FloatBuffer1, FloatBuffer param1FloatBuffer2);
    
    void morphd(FloatBuffer param1FloatBuffer1, FloatBuffer param1FloatBuffer2);
    
    public static interface Factory {
      MorphedMesh.Morpher create(MorphedMesh.MorphedBuf param2MorphedBuf);
    }
  }
  
  public static interface Factory {
    MorphedMesh.Morpher create(MorphedMesh.MorphedBuf param1MorphedBuf);
  }
}
