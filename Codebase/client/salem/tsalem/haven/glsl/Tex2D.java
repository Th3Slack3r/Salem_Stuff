package haven.glsl;

public class Tex2D {
  public static final Uniform tex2d = new Uniform(Type.SAMPLER2D);
  
  public Varying.Interpol ipol = Varying.Interpol.NORMAL;
  
  public static final AutoVarying texcoord = new AutoVarying(Type.VEC2, "s_tex2d") {
      protected Expression root(VertexContext vctx) {
        return Cons.pick(VertexContext.gl_MultiTexCoord[0].ref(), "st");
      }
      
      protected Varying.Interpol ipol(Context ctx) {
        Tex2D mod;
        if (ctx instanceof ShaderContext && (mod = ((ShaderContext)ctx).prog.getmod(Tex2D.class)) != null)
          return mod.ipol; 
        return super.ipol(ctx);
      }
    };
  
  public static ValBlock.Value tex2d(FragmentContext fctx) {
    return fctx.uniform.ext(tex2d, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            vals.getClass();
            return new ValBlock.Value(vals, Type.VEC4) {
                public Expression root() {
                  return Cons.texture2D(Tex2D.tex2d.ref(), Tex2D.texcoord.ref());
                }
              };
          }
        });
  }
  
  public Tex2D(ProgramContext prog) {
    prog.module(this);
  }
  
  public static Tex2D get(ProgramContext prog) {
    Tex2D t = prog.<Tex2D>getmod(Tex2D.class);
    if (t == null)
      t = new Tex2D(prog); 
    return t;
  }
  
  public static final ShaderMacro mod = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        final ValBlock.Value tex2d = Tex2D.tex2d(prog.fctx);
        tex2d.force();
        prog.fctx.fragcol.mod(new Macro1<Expression>() {
              public Expression expand(Expression in) {
                return Cons.mul(new Expression[] { in, this.val$tex2d.ref() }, );
              }
            },  0);
      }
    };
  
  public static final ShaderMacro clip = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        final ValBlock.Value tex2d = Tex2D.tex2d(prog.fctx);
        tex2d.force();
        prog.fctx.mainmod(new CodeMacro() {
              public void expand(Block blk) {
                blk.add(new If(Cons.lt(Cons.pick(tex2d.ref(), "a"), Cons.l(0.5D)), new Discard()));
              }
            }-100);
      }
    };
}
