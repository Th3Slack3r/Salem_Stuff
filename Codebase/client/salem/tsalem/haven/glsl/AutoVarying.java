package haven.glsl;

public abstract class AutoVarying extends Varying {
  public AutoVarying(Type type, Symbol name) {
    super(type, name);
  }
  
  public AutoVarying(Type type, String prefix) {
    this(type, new Symbol.Shared(prefix));
  }
  
  public AutoVarying(Type type) {
    this(type, new Symbol.Shared());
  }
  
  public abstract class Value extends ValBlock.Value {
    public Value(ValBlock blk) {
      super(AutoVarying.this.type, AutoVarying.this.name);
    }
    
    protected void cons2(Block blk) {
      this.var = AutoVarying.this;
      blk.add(new LBinOp.Assign(this.var.ref(), this.init));
    }
  }
  
  protected Expression root(VertexContext vctx) {
    throw new Error("Neither make() nor root() overridden");
  }
  
  protected Value make(ValBlock vals, final VertexContext vctx) {
    return new Value(vals) {
        public Expression root() {
          return AutoVarying.this.root(vctx);
        }
      };
  }
  
  public ValBlock.Value value(final VertexContext ctx) {
    return ctx.mainvals.ext(this, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            return AutoVarying.this.make(vals, ctx);
          }
        });
  }
  
  public void use(Context ctx) {
    if (ctx instanceof FragmentContext) {
      FragmentContext fctx = (FragmentContext)ctx;
      value(fctx.prog.vctx).force();
    } 
    super.use(ctx);
  }
}
