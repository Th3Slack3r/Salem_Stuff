package haven;

public enum WrapMode {
   ONCE(true),
   LOOP(false),
   PONG(true),
   PONGLOOP(false);

   public final boolean ends;

   private WrapMode(boolean ends) {
      this.ends = ends;
   }
}
