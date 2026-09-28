package haven;

public enum WrapMode {
  ONCE(true),
  LOOP(false),
  PONG(true),
  PONGLOOP(false);
  
  public final boolean ends;
  
  WrapMode(boolean ends) {
    this.ends = ends;
  }
}
