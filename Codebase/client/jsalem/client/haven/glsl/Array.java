package haven.glsl;

public class Array extends Type {
   public final Type el;
   public final int sz;

   public Array(Type el, int sz) {
      this.el = el;
      this.sz = sz;
   }

   public Array(Type el) {
      this(el, 0);
   }

   @Override
   public String name(Context ctx) {
      return this.sz > 0 ? this.el.name(ctx) + "[" + this.sz + "]" : this.el.name(ctx) + "[]";
   }
}
