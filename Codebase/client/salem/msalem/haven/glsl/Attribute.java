package haven.glsl;

public class Attribute extends Variable.Global {
   public Attribute(Type type, Symbol name) {
      super(type, name);
   }

   public Attribute(Type type, String infix) {
      this(type, new Symbol.Shared("s_" + infix));
   }

   public Attribute(Type type) {
      this(type, new Symbol.Shared());
   }

   @Override
   public void use(Context ctx) {
      if (!this.defined(ctx)) {
         ctx.vardefs.add(new Attribute.Def());
      }
   }

   private class Def extends Variable.Global.Definition {
      private Def() {
      }

      @Override
      public void output(Output out) {
         if (out.ctx instanceof ShaderContext) {
            ((ShaderContext)out.ctx).prog.attribs.add(Attribute.this);
         }

         out.write("attribute ");
         super.output(out);
      }
   }
}
