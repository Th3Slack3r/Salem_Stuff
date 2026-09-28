package haven;

import haven.glsl.Add;
import haven.glsl.BinOp;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.For;
import haven.glsl.Function;
import haven.glsl.IVec2Cons;
import haven.glsl.If;
import haven.glsl.LValue;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.Mul;
import haven.glsl.Pick;
import haven.glsl.ProgramContext;
import haven.glsl.Return;
import haven.glsl.ShaderMacro;
import haven.glsl.Statement;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.Variable;
import java.awt.Color;
import javax.media.opengl.GL2;

public class Outlines implements Rendered {
  private final boolean symmetric;
  
  public void draw(GOut g) {}
  
  private static final Uniform snrm = new Uniform(Type.SAMPLER2D);
  
  private static final Uniform sdep = new Uniform(Type.SAMPLER2D);
  
  private static final Uniform msnrm = new Uniform(Type.SAMPLER2DMS);
  
  private static final Uniform msdep = new Uniform(Type.SAMPLER2DMS);
  
  private static final ShaderMacro[][] shaders = new ShaderMacro[4][];
  
  private static ShaderMacro shader(final boolean symmetric, final boolean ms) {
    return new ShaderMacro() {
        Color color = Color.BLACK;
        
        Coord[] points = new Coord[] { new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1) };
        
        Expression sample(boolean nrm, Expression c, Expression s, Coord o) {
          Add add;
          if (ms) {
            IVec2Cons iVec2Cons = Cons.ivec2(new Expression[] { Cons.floor((Expression)Cons.mul(new Expression[] { c, (Expression)MiscLib.screensize.ref() })) });
            if (!o.equals(Coord.z))
              add = Cons.add(new Expression[] { (Expression)iVec2Cons, Cons.ivec2(o) }); 
            return Cons.texelFetch((Expression)(nrm ? Outlines.msnrm : Outlines.msdep).ref(), (Expression)add, s);
          } 
          Expression ctc = c;
          if (!o.equals(Coord.z))
            add = Cons.add(new Expression[] { c, (Expression)Cons.mul(new Expression[] { Cons.vec2(o), (Expression)MiscLib.pixelpitch.ref() }) }); 
          return Cons.texture2D((Expression)(nrm ? Outlines.snrm : Outlines.sdep).ref(), (Expression)add);
        }
        
        Function ofac = (Function)new Function.Def(Type.FLOAT) {
          
          };
        
        Function msfac = (Function)new Function.Def(Type.FLOAT) {
          
          };
        
        public void modify(ProgramContext prog) {
          prog.fctx.fragcol.mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                  Expression of = !ms ? Outlines.null.this.ofac.call(new Expression[] { (Expression)Cons.l(-1) }, ) : Outlines.null.this.msfac.call(new Expression[0]);
                  return (Expression)Cons.vec4(new Expression[] { Cons.col3(this.this$0.color), Cons.mix((Expression)Cons.l(0.0D), (Expression)Cons.l(1.0D), of) });
                }
              }0);
        }
      };
  }
  
  static {
    (new ShaderMacro[1])[0] = shader(false, false);
    shaders[0] = new ShaderMacro[1];
    (new ShaderMacro[1])[0] = shader(false, true);
    shaders[1] = new ShaderMacro[1];
    (new ShaderMacro[1])[0] = shader(true, false);
    shaders[2] = new ShaderMacro[1];
    (new ShaderMacro[1])[0] = shader(true, true);
    shaders[3] = new ShaderMacro[1];
  }
  
  public Outlines(boolean symmetric) {
    this.symmetric = symmetric;
  }
  
  public boolean setup(RenderList rl) {
    final PView.ConfContext ctx = (PView.ConfContext)rl.state().<PView.RenderContext>get(PView.ctx);
    final RenderedNormals nrm = ctx.<RenderedNormals>data(RenderedNormals.id);
    final boolean ms = (ctx.cfg.ms > 1);
    ctx.cfg.tdepth = true;
    ctx.cfg.add(nrm);
    rl.prepc(Rendered.postfx);
    rl.add(new Rendered.ScreenQuad(), new States.AdHoc(shaders[(this.symmetric ? true : false) | (ms ? true : false)]) {
          private GLState.TexUnit tnrm;
          
          private GLState.TexUnit tdep;
          
          public void reapply(GOut g) {
            GL2 gl = g.gl;
            gl.glUniform1i(g.st.prog.uniform(!ms ? Outlines.snrm : Outlines.msnrm), this.tnrm.id);
            gl.glUniform1i(g.st.prog.uniform(!ms ? Outlines.sdep : Outlines.msdep), this.tdep.id);
          }
          
          public void apply(GOut g) {
            GL2 gL2 = g.gl;
            if (!ms) {
              this.tnrm = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)nrm.tex).tex);
              this.tdep = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)ctx.cur.depth).tex);
            } else {
              this.tnrm = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)nrm.tex).tex);
              this.tdep = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)ctx.cur.depth).tex);
            } 
            reapply(g);
          }
          
          public void unapply(GOut g) {
            GL2 gL2 = g.gl;
            this.tnrm.ufree();
            this.tnrm = null;
            this.tdep.ufree();
            this.tdep = null;
          }
        });
    return false;
  }
}
