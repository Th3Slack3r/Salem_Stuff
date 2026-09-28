package haven.glsl;

public abstract class Element {
   public abstract Element process(Context var1);

   public void output(Output out) {
      throw new RuntimeException("output called on abstract element " + this);
   }
}
