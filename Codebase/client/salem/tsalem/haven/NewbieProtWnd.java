package haven;

import java.awt.font.TextAttribute;

public class NewbieProtWnd extends Window {
  private static Window instance;
  
  private final TextEntry textEntry = null;
  
  private final HSlider hSlider = null;
  
  public static CheckBox aCB = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new NewbieProtWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public NewbieProtWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Criminal Actions Info");
    this.justclose = true;
    RichText.Foundry skbodfnd = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12) });
    skbodfnd.aa = true;
    RichTextBox richTextBox = new RichTextBox(new Coord(5, 5), new Coord(400, 140), this, "Now that dead body looks tempting, doesn't it?\n\nBe warned though, pilgrim, both scalping and desecrating the dead\nleaves summon-able scents, and those may very well get you killed!\n\nFor your own safety, you need to turn on Criminal acts to do this.\n(This is a feature of this client, and can be turned off in the options.)", skbodfnd);
    aCB = new CheckBox(new Coord(0, 160), this, "Do not show this message again") {
        public void changed(boolean val) {
          super.changed(val);
          Config.newbie_prot_hide_info = val;
          Utils.setprefb("newbie_prot_hide_info", val);
        }
      };
    aCB.a = Config.newbie_prot_hide_info;
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
