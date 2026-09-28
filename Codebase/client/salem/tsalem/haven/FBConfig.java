package haven;

import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import java.util.Collection;
import java.util.LinkedList;

public class FBConfig {
  public final PView.ConfContext ctx;
  
  public Coord sz;
  
  public boolean hdr;
  
  public boolean tdepth;
  
  public int ms = 1;
  
  public GLFrameBuffer fb;
  
  public PView.RenderState wnd;
  
  public GLFrameBuffer.Attachment[] color;
  
  public GLFrameBuffer.Attachment depth;
  
  public GLState state;
  
  private RenderTarget[] tgts = new RenderTarget[0];
  
  private ResolveFilter[] res = new ResolveFilter[0];
  
  private GLState resp;
  
  public FBConfig(PView.ConfContext ctx, Coord sz) {
    this.ctx = ctx;
    this.sz = sz;
  }
  
  public boolean cleanp() {
    if (this.hdr || this.tdepth || this.ms > 1)
      return false; 
    for (int i = 0; i < this.tgts.length; i++) {
      if (this.tgts[i] != null)
        return false; 
    } 
    for (ResolveFilter rf : this.res) {
      if (!rf.cleanp())
        return false; 
    } 
    return true;
  }
  
  private static final ShaderMacro[] nosh = new ShaderMacro[0];
  
  private void create() {
    GLFrameBuffer.Attachment depth;
    final ShaderMacro[] shaders;
    Collection<GLFrameBuffer.Attachment> color = new LinkedList<>();
    Collection<ShaderMacro> shb = new LinkedList<>();
    Collection<GLState> stb = new LinkedList<>();
    int fmt = this.hdr ? 34842 : 6408;
    if (this.ms <= 1) {
      color.add(GLFrameBuffer.Attachment.mk(new TexE(this.sz, fmt, 6408, 5121)));
    } else {
      color.add(GLFrameBuffer.Attachment.mk(new TexMSE(this.sz, this.ms, fmt, 6408, 5121)));
    } 
    if (this.tdepth) {
      if (this.ms <= 1) {
        depth = GLFrameBuffer.Attachment.mk(new TexE(this.sz, 6402, 6402, 5125));
      } else {
        depth = GLFrameBuffer.Attachment.mk(new TexMSE(this.sz, this.ms, 6402, 6402, 5125));
      } 
    } else {
      depth = GLFrameBuffer.Attachment.mk(new GLFrameBuffer.RenderBuffer(this.sz, 6402, this.ms));
    } 
    for (int i = 0; i < this.tgts.length; i++) {
      if (this.tgts[i] != null) {
        color.add(this.tgts[i].maketex(this));
        GLState st = this.tgts[i].state(this, i + 1);
        if (st != null)
          stb.add(st); 
        ShaderMacro code = this.tgts[i].code(this, i + 1);
        if (code != null)
          shb.add(code); 
      } 
    } 
    this.color = color.<GLFrameBuffer.Attachment>toArray(new GLFrameBuffer.Attachment[0]);
    this.depth = depth;
    if (shb.size() < 1) {
      shaders = nosh;
    } else {
      shaders = shb.<ShaderMacro>toArray(new ShaderMacro[0]);
    } 
    this.fb = new GLFrameBuffer(this.color, this.depth) {
        public ShaderMacro[] shaders() {
          return shaders;
        }
        
        public boolean reqshaders() {
          return (shaders.length > 0);
        }
      };
    this.wnd = new PView.RenderState() {
        public Coord ul() {
          return Coord.z;
        }
        
        public Coord sz() {
          return FBConfig.this.sz;
        }
      };
    stb.add(this.fb);
    stb.add(this.wnd);
    this.state = GLState.compose(stb.<GLState>toArray(new GLState[0]));
    if (this.res.length > 0) {
      ShaderMacro[] resp = new ShaderMacro[this.res.length];
      for (int j = 0; j < this.res.length; j++)
        resp[j] = this.res[j].code(this); 
      resp = ArrayIdentity.<ShaderMacro>intern(resp);
      this.resp = new States.AdHoc(resp) {
          public void apply(GOut g) {
            for (FBConfig.ResolveFilter f : FBConfig.this.res)
              f.apply(FBConfig.this, g); 
          }
          
          public void unapply(GOut g) {
            for (FBConfig.ResolveFilter f : FBConfig.this.res)
              f.unapply(FBConfig.this, g); 
          }
        };
    } 
  }
  
  private static <T> boolean hasuo(T[] a, T[] b) {
    for (T ae : a) {
      T[] arrayOfT = b;
      int i = arrayOfT.length;
      byte b1 = 0;
      while (true) {
        if (b1 < i) {
          T be = arrayOfT[b1];
          if (Utils.eq(ae, be))
            break; 
          b1++;
          continue;
        } 
        return false;
      } 
    } 
    return true;
  }
  
  public static boolean equals(FBConfig a, FBConfig b) {
    if (!a.sz.equals(b.sz))
      return false; 
    if (a.hdr != b.hdr || a.tdepth != b.tdepth)
      return false; 
    if (a.ms != b.ms)
      return false; 
    if (!hasuo(a.tgts, b.tgts) || !hasuo(b.tgts, a.tgts))
      return false; 
    if (!hasuo(a.res, b.res) || !hasuo(b.res, a.res))
      return false; 
    return true;
  }
  
  private void subsume(FBConfig last) {
    this.fb = last.fb;
    this.wnd = last.wnd;
    this.color = last.color;
    this.depth = last.depth;
    this.tgts = last.tgts;
    this.res = last.res;
    this.resp = last.resp;
    this.state = last.state;
  }
  
  public void fin(FBConfig last) {
    if (this.ms <= 1) {
      add(new Resolve1());
    } else {
      add(new ResolveMS(this.ms));
    } 
    if (equals(this, last)) {
      subsume(last);
      return;
    } 
    if (last.fb != null)
      last.fb.dispose(); 
    if (cleanp())
      return; 
    create();
  }
  
  public void resolve(GOut g) {
    if (this.fb != null) {
      for (ResolveFilter rf : this.res)
        rf.prepare(this, g); 
      g.ftexrect(Coord.z, this.sz, this.resp);
    } 
  }
  
  public RenderTarget add(RenderTarget tgt) {
    if (tgt == null)
      throw new NullPointerException(); 
    for (RenderTarget p : this.tgts) {
      if (Utils.eq(tgt, p))
        return p; 
    } 
    int i = 0;
    if (i < this.tgts.length) {
      if (this.tgts[i] == null)
        this.tgts[i] = tgt; 
      return tgt;
    } 
    this.tgts = Utils.<RenderTarget>extend(this.tgts, i + 1);
    this.tgts[i] = tgt;
    return tgt;
  }
  
  public ResolveFilter add(ResolveFilter rf) {
    if (rf == null)
      throw new NullPointerException(); 
    for (ResolveFilter p : this.res) {
      if (Utils.eq(rf, p))
        return p; 
    } 
    int l = this.res.length;
    this.res = Utils.<ResolveFilter>extend(this.res, l + 1);
    this.res[l] = rf;
    return rf;
  }
  
  public static abstract class RenderTarget {
    public GLFrameBuffer.Attachment tex;
    
    public GLFrameBuffer.Attachment maketex(FBConfig cfg) {
      if (cfg.ms <= 1)
        return this.tex = GLFrameBuffer.Attachment.mk(new TexE(cfg.sz, 6408, 6408, 5121)); 
      return this.tex = GLFrameBuffer.Attachment.mk(new TexMSE(cfg.sz, cfg.ms, 6408, 6408, 5121));
    }
    
    public GLState state(FBConfig cfg, int id) {
      return null;
    }
    
    public ShaderMacro code(FBConfig cfg, int id) {
      return null;
    }
  }
  
  public static final Uniform numsamples = (Uniform)new Uniform.AutoApply(Type.INT, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        g.gl.glUniform1i(loc, ((PView.ConfContext)g.st.get((GLState.Slot)PView.ctx)).cur.ms);
      }
    };
  
  private static class Resolve1 implements ResolveFilter {
    private Resolve1() {}
    
    public void prepare(FBConfig cfg, GOut g) {}
    
    public boolean cleanp() {
      return true;
    }
    
    private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
    
    private static final ShaderMacro code = new ShaderMacro() {
        public void modify(ProgramContext prog) {
          prog.fctx.fragcol.mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                  return Cons.texture2D((Expression)FBConfig.Resolve1.ctex.ref(), (Expression)Tex2D.texcoord.ref());
                }
              }0);
        }
      };
    
    private GLState.TexUnit csmp;
    
    public ShaderMacro code(FBConfig cfg) {
      return code;
    }
    
    public void apply(FBConfig cfg, GOut g) {
      this.csmp = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)cfg.color[0]).tex);
      g.gl.glUniform1i(g.st.prog.uniform(ctex), this.csmp.id);
    }
    
    public void unapply(FBConfig cfg, GOut g) {
      this.csmp.ufree();
      this.csmp = null;
    }
    
    public boolean equals(Object o) {
      return o instanceof Resolve1;
    }
  }
  
  private static class ResolveMS implements ResolveFilter {
    private final int samples;
    
    private ResolveMS(int samples) {
      this.code = new ShaderMacro() {
          public void modify(ProgramContext prog) {
            prog.fctx.fragcol.mod(new Macro1<Expression>() {
                  public Expression expand(Expression in) {
                    Expression[] texels = new Expression[FBConfig.ResolveMS.this.samples];
                    for (int i = 0; i < FBConfig.ResolveMS.this.samples; i++) {
                      texels[i] = Cons.texelFetch((Expression)FBConfig.ResolveMS.ctex.ref(), (Expression)Cons.ivec2(new Expression[] { Cons.floor((Expression)Cons.mul(new Expression[] { (Expression)Tex2D.texcoord.ref(), (Expression)MiscLib.screensize.ref() }, )) }, ), (Expression)Cons.l(i));
                    } 
                    return (Expression)Cons.mul(new Expression[] { (Expression)Cons.add(texels), (Expression)Cons.l(1.0D / FBConfig.ResolveMS.access$400(this.this$1.this$0)) });
                  }
                }0);
          }
        };
      this.samples = samples;
    }
    
    public void prepare(FBConfig cfg, GOut g) {}
    
    public boolean cleanp() {
      return true;
    }
    
    private static final Uniform ctex = new Uniform(Type.SAMPLER2DMS);
    
    private final ShaderMacro code;
    
    private GLState.TexUnit csmp;
    
    public ShaderMacro code(FBConfig cfg) {
      return this.code;
    }
    
    public void apply(FBConfig cfg, GOut g) {
      this.csmp = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)cfg.color[0]).tex);
      g.gl.glUniform1i(g.st.prog.uniform(ctex), this.csmp.id);
    }
    
    public void unapply(FBConfig cfg, GOut g) {
      this.csmp.ufree();
      this.csmp = null;
    }
    
    public boolean equals(Object o) {
      return (o instanceof ResolveMS && ((ResolveMS)o).samples == this.samples);
    }
  }
  
  public static interface ResolveFilter {
    boolean cleanp();
    
    void prepare(FBConfig param1FBConfig, GOut param1GOut);
    
    ShaderMacro code(FBConfig param1FBConfig);
    
    void apply(FBConfig param1FBConfig, GOut param1GOut);
    
    void unapply(FBConfig param1FBConfig, GOut param1GOut);
  }
}
