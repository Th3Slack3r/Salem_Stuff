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
  
  public String name(Context ctx) {
    if (this.sz > 0)
      return this.el.name(ctx) + "[" + this.sz + "]"; 
    return this.el.name(ctx) + "[]";
  }
}
