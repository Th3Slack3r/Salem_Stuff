package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "woodcutting",
   step = "flowermenu"
)
class WoodcuttingBotFlowerMenu extends BotState {
   public WoodcuttingBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length <= 1 || !"Chop".equals(fm.opts[1].name) || !ui.rwidgets.containsKey(fm)) {
            ui.message("[Woodcutting] This is not a tree - aborting!", GameUI.MsgType.INFO);
            return BotState.initializeStack("woodcutting", "end");
         }

         try {
            fm.choose(fm.opts[1]);
            return BotState.initializeStack("woodcutting", "chopresults");
         } catch (Exception var6) {
         }
      }

      return null;
   }
}
