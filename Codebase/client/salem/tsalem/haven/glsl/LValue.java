package haven.glsl;

public abstract class LValue extends Expression {
  public abstract LValue process(Context paramContext);
}
