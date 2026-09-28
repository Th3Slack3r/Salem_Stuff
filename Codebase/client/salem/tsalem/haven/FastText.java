package haven;

import java.awt.Font;

public class FastText {
  public static final Text.Foundry fnd = (new Text.Foundry(new Font("Serif", 1, 15))).aa(true);
  
  private static final Tex[] ct = new Tex[225];
  
  public static Tex ch(char c) {
    int i;
    if (c < ' ' || c >= 'Ā') {
      i = 0;
    } else {
      i = c - 31;
    } 
    if (ct[i] == null)
      ct[i] = fnd.render(Character.toString(c)).tex(); 
    return ct[i];
  }
  
  public static int textw(String text) {
    int r = 0;
    for (int i = 0; i < text.length(); i++)
      r += (ch(text.charAt(i)).sz()).x; 
    return r;
  }
  
  public static void aprint(GOut g, Coord c, double ax, double ay, String text) {
    Coord lc = new Coord(c);
    if (ax > 0.0D)
      lc.x = (int)(lc.x - textw(text) * ax); 
    if (ay > 0.0D)
      lc.y = (int)(lc.y - fnd.height() * ay); 
    for (int i = 0; i < text.length(); i++) {
      Tex ch = ch(text.charAt(i));
      g.image(ch, lc);
      lc.x += (ch.sz()).x;
    } 
  }
  
  public static void print(GOut g, Coord c, String text) {
    aprint(g, c, 0.0D, 0.0D, text);
  }
  
  public static void aprintf(GOut g, Coord c, double ax, double ay, String fmt, Object... args) {
    aprint(g, c, ax, ay, String.format(fmt, args));
  }
  
  public static void printf(GOut g, Coord c, String fmt, Object... args) {
    print(g, c, String.format(fmt, args));
  }
}
