package haven.res.gfx.fx.floatimg;

import haven.BuddyWnd;
import haven.Composite;
import haven.Config;
import haven.Gob;
import haven.KinInfo;
import haven.MainFrame;
import haven.ResDrawable;
import haven.Resource;
import haven.Sprite;
import haven.Tex;
import haven.TexI;
import haven.Text;
import haven.UI;
import haven.Utils;
import java.awt.Color;

public class FloatText extends FloatSprite {
  public static final Text.Foundry fnd = new Text.Foundry("SansSerif", 10);
  
  public FloatText(Sprite.Owner owner, Resource resource, String string, Color color) {
    super(owner, resource, (Tex)new TexI(Utils.outline2((fnd.render(string, color)).img, Utils.contrast(color))), 2000);
    if (Config.floating_text_to_console) {
      String oN = "";
      Color altCol = null;
      if (Config.floating_ttc_add_target_name) {
        Gob g = (Gob)owner;
        try {
          if (g.id == UI.instance.gui.map.plgob)
            if (MainFrame.cName != null) {
              oN = MainFrame.cName;
            } else {
              oN = "Player";
            }  
        } catch (Exception exception) {}
        if (oN == "")
          try {
            KinInfo ki = (KinInfo)g.getattr(KinInfo.class);
            if (ki != null) {
              oN = ki.name;
              altCol = BuddyWnd.gc[ki.group];
            } 
          } catch (Exception exception) {} 
        if (oN == "")
          try {
            ResDrawable rd = (ResDrawable)g.getattr(ResDrawable.class);
            oN = ((Resource)rd.res.get()).name;
          } catch (Exception exception) {} 
        if (oN == "")
          try {
            Composite co = (Composite)g.getattr(Composite.class);
            oN = ((Resource)co.base.get()).name;
          } catch (Exception exception) {} 
        if (oN.contains("/") && altCol == null)
          oN = oN.split("/")[(oN.split("/")).length - 1]; 
        if (oN.equals("body"))
          oN = "Player"; 
        oN = oN + ": ";
      } 
      Utils.msgLog("" + oN + string, (altCol == null) ? color : altCol);
    } 
  }
}
