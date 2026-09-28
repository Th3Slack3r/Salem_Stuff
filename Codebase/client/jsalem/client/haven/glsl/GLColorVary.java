package haven.glsl;

public class GLColorVary implements ShaderMacro {
   public static final AutoVarying color = new AutoVarying(Type.VEC4) {
      @Override
      protected Expression root(VertexContext vctx) {
         return VertexContext.gl_Color.ref();
      }
   };

   @Override
   public void modify(ProgramContext prog) {
      prog.fctx.fragcol.mod(new Macro1<Expression>() {
         public Expression expand(Expression in) {
            return Cons.mul(in, GLColorVary.color.ref());
         }
      }, 0);
   }
}
