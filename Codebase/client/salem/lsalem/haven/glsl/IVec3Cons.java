package haven.glsl;

public class IVec3Cons extends Expression {
   public static final IVec3Cons z = new IVec3Cons(IntLiteral.z, IntLiteral.z, IntLiteral.z);
   public static final IVec3Cons u = new IVec3Cons(IntLiteral.u, IntLiteral.u, IntLiteral.u);
   public final Expression[] els;

   public IVec3Cons(Expression... els) {
      if (els.length >= 1 && els.length <= 3) {
         this.els = els;
      } else {
         throw new RuntimeException("Invalid number of arguments for ivec3: " + els.length);
      }
   }

   public IVec3Cons process(Context ctx) {
      Expression[] nels = new Expression[this.els.length];

      for (int i = 0; i < this.els.length; i++) {
         nels[i] = this.els[i].process(ctx);
      }

      return new IVec3Cons(nels);
   }

   @Override
   public void output(Output out) {
      out.write("ivec3(");
      this.els[0].output(out);

      for (int i = 1; i < this.els.length; i++) {
         out.write(", ");
         this.els[i].output(out);
      }

      out.write(")");
   }
}
