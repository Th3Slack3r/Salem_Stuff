package haven;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.latikai.bots.BotState;

public class BotPanel extends Window {
   private final Map<String, Button> buttons = new HashMap<>();
   private final Map<String, Boolean> running = new HashMap<>();
   private static final int BW = 120;
   private static final int BH = 25;
   private static final int COL = 2;

   public BotPanel(Coord c, Widget parent) {
      super(c, new Coord(300, 400), parent, "Bot Manager");
      List<String> bots = BotState.getBotNames();
      int x = 10, y = 10;
      for (int i = 0; i < bots.size(); i++) {
         String name = bots.get(i);
         running.put(name, false);
         int col = i % COL;
         int row = i / COL;
         x = 10 + col * (BW + 5);
         y = 10 + row * (BH + 5);
         Button btn = new Button(new Coord(x, y), BW, this, name);
         buttons.put(name, btn);
      }
      this.pack();
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this && msg.equals("close")) {
         this.hide();
      } else if (msg.equals("activate")) {
         for (Map.Entry<String, Button> e : buttons.entrySet()) {
            if (e.getValue() == sender) {
               String bname = e.getKey();
               toggleBot(bname);
               return;
            }
         }
         super.wdgmsg(sender, msg, args);
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   private void toggleBot(String bname) {
      if (UI.instance == null || UI.instance.bmgr == null) return;
      Boolean isRunning = running.get(bname);
      Button btn = buttons.get(bname);
      if (isRunning != null && isRunning) {
         UI.instance.bmgr.stopBot(new String[]{"stopbot", bname});
         running.put(bname, false);
         if (btn != null) btn.change(bname);
      } else {
         UI.instance.bmgr.startBot(new String[]{"startbot", bname});
         running.put(bname, true);
         if (btn != null) btn.change("Stop " + bname);
      }
   }

   @Widget.RName("botpanel")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new BotPanel(c, parent);
      }
   }
}
