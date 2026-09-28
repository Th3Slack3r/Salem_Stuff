package haven;

public class Coordf {
  public float x;
  
  public float y;
  
  public Coordf(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  public Coordf(Coord coord) {
    this.x = coord.x;
    this.y = coord.y;
  }
  
  public Coordf rotate(double angle) {
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    return new Coordf((float)(this.x * cos - this.y * sin), (float)(this.y * cos + this.x * sin));
  }
}
