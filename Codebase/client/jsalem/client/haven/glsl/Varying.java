package haven.glsl;

public class Varying extends Variable.Global {
   public Varying.Interpol ipol = Varying.Interpol.NORMAL;

   protected Varying.Interpol ipol(Context ctx) {
      return this.ipol;
   }

   public Varying(Type type, Symbol name) {
      super(type, name);
   }

   @Override
   public void use(Context ctx) {
      if (!this.defined(ctx)) {
         ctx.vardefs.add(new Varying.Def());
      }
   }

   private class Def extends Variable.Global.Definition {
      private Def() {
      }

      @Override
      public void output(Output out) {
         switch (Varying.this.ipol(out.ctx)) {
            case FLAT:
               out.write("flat ");
               break;
            case NOPERSPECTIVE:
               out.write("noperspective ");
               break;
            case CENTROID:
               out.write("centroid ");
         }

         out.write("varying ");
         super.output(out);
      }
   }

   public static enum Interpol {
      NORMAL,
      FLAT,
      NOPERSPECTIVE,
      CENTROID;
   }
}
