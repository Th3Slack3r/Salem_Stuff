package haven.headless;

import haven.Coord;
import haven.GOut;
import haven.GameUI;
import haven.Widget;
import java.awt.Color;

public class HeadlessGameUI extends GameUI {
   public HeadlessGameUI(Widget wdg, String chrid, long plid) {
      super(wdg, chrid, plid);
   }

   @Override
   public void draw(GOut g) {
   }

   @Override
   public void message(String msg, Color msgColor) {
      System.out.println("\t" + msg);
   }

   @Widget.RName("headless_gameui")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String chrid = (String)args[0];
         int plid = (Integer)args[1];
         return new HeadlessGameUI(parent, chrid, plid);
      }
   }
}
