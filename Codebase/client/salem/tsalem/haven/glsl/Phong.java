package haven.glsl;

public class Phong extends ValBlock.Group {
  private final ProgramContext prog;
  
  private final Expression vert;
  
  private final Expression edir;
  
  private final Expression norm;
  
  public final ValBlock.Group.GValue bcol = new ValBlock.Group.GValue(this, Type.VEC3);
  
  public final ValBlock.Group.GValue scol = new ValBlock.Group.GValue(this, Type.VEC3);
  
  public final boolean pfrag;
  
  public static final Uniform nlights = new Uniform(Type.INT);
  
  public final DoLight dolight;
  
  public static class CelShade implements ShaderMacro {
    public static final Function celramp = new Function.Def(Type.VEC3) {
      
      };
    
    public void modify(ProgramContext prog) {
      Phong ph = prog.<Phong>getmod(Phong.class);
      Macro1<Expression> cel = new Macro1<Expression>() {
          public Expression expand(Expression in) {
            return Phong.CelShade.celramp.call(new Expression[] { in });
          }
        };
      ph.bcol.mod(cel, 0);
      ph.scol.mod(cel, 0);
    }
  }
  
  public class DoLight extends Function.Def {
    public final Expression i = param(Function.PDir.IN, Type.INT).ref();
    
    public final Expression vert = param(Function.PDir.IN, Type.VEC3).ref();
    
    public final Expression edir = param(Function.PDir.IN, Type.VEC3).ref();
    
    public final Expression norm = param(Function.PDir.IN, Type.VEC3).ref();
    
    public final LValue diff = param(Function.PDir.INOUT, Type.VEC3).ref();
    
    public final LValue spec = param(Function.PDir.INOUT, Type.VEC3).ref();
    
    public final Expression ls;
    
    public final Expression mat;
    
    public final Expression shine;
    
    public final ValBlock.Value lvl;
    
    public final ValBlock.Value dir;
    
    public final ValBlock.Value dl;
    
    public final ValBlock.Value sl;
    
    public final ValBlock dvals;
    
    public final ValBlock svals;
    
    private final OrderList<Runnable> mods;
    
    public Block dcalc;
    
    public Block scalc;
    
    public Statement dcurs;
    
    public Statement scurs;
    
    private DoLight() {
      super(Type.VOID);
      Phong.this.prog;
      this.ls = Cons.idx(ProgramContext.gl_LightSource.ref(), this.i);
      Phong.this.prog;
      this.mat = ProgramContext.gl_FrontMaterial.ref();
      this.shine = Cons.fref(this.mat, "shininess");
      this.dvals = new ValBlock();
      this.svals = new ValBlock();
      this.mods = new OrderList<>();
      this.dvals.getClass();
      ValBlock.Group tdep = new ValBlock.Group(this.dvals) {
          public void cons1() {}
          
          public void cons2(Block blk) {
            Phong.DoLight.this.lvl.var = blk.local(Type.FLOAT, null);
            Phong.DoLight.this.dir.var = blk.local(Type.VEC3, null);
            Block.Local rel = new Block.Local(Type.VEC3);
            Block.Local dst = new Block.Local(Type.FLOAT);
            rel.getClass();
            (new Statement[4])[0] = new Block.Local.Def(rel, 
                Cons.sub(Cons.pick(Cons.fref(Phong.DoLight.this.ls, "position"), "xyz"), Phong.DoLight.this.vert));
            (new Statement[4])[1] = Cons.stmt(Cons.ass(Phong.DoLight.this.dir.var, Cons.normalize(rel.ref())));
            dst.getClass();
            Phong.DoLight.this.code.add(new If(Cons.eq(Cons.pick(Cons.fref(Phong.DoLight.this.ls, "position"), "w"), Cons.l(0.0D)), new Block(new Statement[] { Cons.stmt(Cons.ass(this.this$1.lvl.var, Cons.l(1.0D))), Cons.stmt(Cons.ass(this.this$1.dir.var, Cons.pick(Cons.fref(this.this$1.ls, "position"), "xyz"))) }), new Block(new Statement[] { null, null, new Block.Local.Def(dst, Cons.length(rel.ref())), Cons.stmt(
                        Cons.ass(this.this$1.lvl.var, Cons.inv(Cons.add(new Expression[] { Cons.fref(this.this$1.ls, "constantAttenuation"), Cons.mul(new Expression[] { Cons.fref(this.this$1.ls, "linearAttenuation"), dst.ref() }), Cons.mul(new Expression[] { Cons.fref(this.this$1.ls, "quadraticAttenuation"), dst.ref(), dst.ref() }) })))) })));
          }
        };
      tdep.getClass();
      this.lvl = new ValBlock.Group.GValue(tdep, Type.FLOAT);
      tdep.getClass();
      this.dir = new ValBlock.Group.GValue(tdep, Type.VEC3);
      this.dvals.getClass();
      this.dl = new ValBlock.Value(this.dvals, Type.FLOAT) {
          public Expression root() {
            return Cons.dot(Phong.DoLight.this.norm, Phong.DoLight.this.dir.depref());
          }
        };
      this.svals.getClass();
      this.sl = new ValBlock.Value(this.svals, Type.FLOAT) {
          public Expression root() {
            Expression reflvl = Cons.pow(Cons.max(new Expression[] { Cons.dot(this.this$1.edir, Cons.reflect(Cons.neg(this.this$1.dir.ref()), this.this$1.norm)), Cons.l(0.0D) }), Phong.DoLight.this.shine);
            Expression hvlvl = Cons.pow(Cons.max(new Expression[] { Cons.dot(this.this$1.norm, Cons.normalize(Cons.add(new Expression[] { this.this$1.edir, this.this$1.dir.ref() }))) }), Phong.DoLight.this.shine);
            return reflvl;
          }
        };
      this.lvl.force();
      this.dl.force();
      this.sl.force();
    }
    
    protected void cons() {
      this.dvals.cons(this.code);
      this.code.add(Cons.stmt(Cons.aadd(this.diff, Cons.mul(new Expression[] { Cons.pick(Cons.fref(this.mat, "ambient"), "rgb"), Cons.pick(Cons.fref(this.ls, "ambient"), "rgb"), this.lvl.ref() }))));
      this.code.add(new If(Cons.gt(this.dl.ref(), Cons.l(0.0D)), this.dcalc = new Block(new Statement[0])));
      this.dcalc.add(this.dcurs = new Placeholder());
      this.dcalc.add(Cons.aadd(this.diff, Cons.mul(new Expression[] { Cons.pick(Cons.fref(this.mat, "diffuse"), "rgb"), Cons.pick(Cons.fref(this.ls, "diffuse"), "rgb"), this.dl.ref(), this.lvl.ref() })));
      this.dcalc.add(new If(Cons.gt(this.shine, Cons.l(0.5D)), this.scalc = new Block(new Statement[0])));
      this.svals.cons(this.scalc);
      this.scalc.add(this.scurs = new Placeholder());
      this.scalc.add(Cons.aadd(this.spec, Cons.mul(new Expression[] { Cons.pick(Cons.fref(this.mat, "specular"), "rgb"), Cons.pick(Cons.fref(this.ls, "specular"), "rgb"), this.sl.ref() })));
      for (Runnable mod : this.mods)
        mod.run(); 
    }
    
    public void mod(Runnable mod, int order) {
      this.mods.add(mod, order);
    }
  }
  
  public void cons1() {}
  
  public void cons2(Block blk) {
    this.bcol.var = blk.local(Type.VEC3, Cons.pick(Cons.fref(ProgramContext.gl_FrontMaterial.ref(), "emission"), "rgb"));
    this.scol.var = blk.local(Type.VEC3, Vec3Cons.z);
    boolean unroll = true;
    for (int i = 0; i < 4; i++) {
      blk.add(new If(Cons.gt(nlights.ref(), Cons.l(i)), Cons.stmt(this.dolight.call(new Expression[] { Cons.l(i), this.vert, this.edir, this.norm, this.bcol.var.ref(), this.scol.var.ref() }))));
    } 
    this.bcol.addmods(blk);
    this.scol.addmods(blk);
  }
  
  private static void fmod(FragmentContext fctx, final Expression bcol, final Expression scol) {
    fctx.fragcol.mod(new Macro1<Expression>() {
          public Expression expand(Expression in) {
            return Cons.add(new Expression[] { Cons.mul(new Expression[] { in, Cons.vec4(new Expression[] { this.val$bcol, Cons.pick(Cons.fref(ProgramContext.gl_FrontMaterial.ref(), "diffuse"), "a") }) }), Cons.vec4(new Expression[] { this.val$scol, Cons.l(0.0D) }) });
          }
        }500);
  }
  
  public Phong(VertexContext vctx) {
    super(vctx.mainvals);
    this.pfrag = false;
    this.prog = vctx.prog;
    ValBlock.Value edir = MiscLib.vertedir(vctx);
    depend(vctx.eyev);
    depend(edir);
    depend(vctx.eyen);
    this.vert = Cons.pick(vctx.eyev.ref(), "xyz");
    this.edir = edir.ref();
    this.norm = vctx.eyen.ref();
    Expression bcol = (new AutoVarying(Type.VEC3) {
        public Expression root(VertexContext vctx) {
          return Phong.this.bcol.depref();
        }
      }).ref();
    Expression scol = (new AutoVarying(Type.VEC3) {
        public Expression root(VertexContext vctx) {
          return Phong.this.scol.depref();
        }
      }).ref();
    fmod(vctx.prog.fctx, bcol, scol);
    this.dolight = new DoLight();
    this.prog.module(this);
  }
  
  public Phong(FragmentContext fctx) {
    super(fctx.mainvals);
    this.pfrag = true;
    this.prog = fctx.prog;
    ValBlock.Value edir = MiscLib.fragedir(fctx);
    ValBlock.Value norm = MiscLib.frageyen(fctx);
    depend(edir);
    depend(norm);
    this.vert = MiscLib.frageyev.ref();
    this.edir = edir.ref();
    this.norm = norm.ref();
    fmod(fctx, this.bcol.ref(), this.scol.ref());
    fctx.fragcol.depend(this.bcol);
    this.dolight = new DoLight();
    this.prog.module(this);
  }
}
