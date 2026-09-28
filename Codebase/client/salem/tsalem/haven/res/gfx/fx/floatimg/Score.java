package haven.res.gfx.fx.floatimg;

import haven.Message;
import haven.Resource;
import haven.Sprite;
import java.awt.Color;

public class Score implements Sprite.Factory {
  private static int dup(int n) {
    return n << 4 | n;
  }
  
  public Sprite create(Sprite.Owner owner, Resource resource, Message message) {
    int n2 = message.int32();
    int n3 = message.uint8();
    int n4 = message.uint16();
    String string = ((n3 & 0x1) != 0) ? "+" : "", string2 = string;
    if (n2 < 0) {
      n2 = -n2;
      string = "-";
    } 
    int n;
    String string3 = ((n = (n3 & 0x6) >> 1) == 1) ? (Integer.toString(n2 / 10) + "." + Integer.toString(n2 % 10)) : ((n == 2) ? String.format("%02d:%02d", new Object[] { Integer.valueOf(n2 / 60), Integer.valueOf(n2 % 60) }) : Integer.toString(n2));
    string3 = string + string3;
    Color color = new Color(dup((n4 & 0xF000) >> 12), dup((n4 & 0xF00) >> 8), dup((n4 & 0xF0) >> 4), dup((n4 & 0xF) >> 0));
    return new FloatText(owner, resource, string3, color);
  }
}
