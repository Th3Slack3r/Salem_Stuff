package haven.glsl;

public class Add extends Expression {
   public final Expression[] terms;

   public Add(Expression... terms) {
      if (terms.length < 1) {
         throw new RuntimeException("Must have more than zero terms");
      } else {
         this.terms = terms;
      }
   }

   public Add process(Context ctx) {
      Expression[] terms = new Expression[this.terms.length];

      for (int i = 0; i < terms.length; i++) {
         terms[i] = this.terms[i].process(ctx);
      }

      return new Add(terms);
   }

   @Override
   public void output(Output out) {
      out.write("(");
      this.terms[0].output(out);

      for (int i = 1; i < this.terms.length; i++) {
         out.write(" + ");
         this.terms[i].output(out);
      }

      out.write(")");
   }
}
