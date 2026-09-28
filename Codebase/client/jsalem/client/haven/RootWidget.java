package haven;

import java.awt.event.KeyEvent;

public class RootWidget extends ConsoleHost {
   public static Resource defcurs = Resource.load("gfx/hud/curs/arw");
   Logout logout = null;
   Profile gprof;
   boolean afk = false;

   public RootWidget(UI ui, Coord sz) {
      super(ui, new Coord(0, 0), sz);
      this.setfocusctl(true);
      this.cursor = defcurs;
   }

    @Override
    public boolean globtype(char key, KeyEvent ev) {
       if (!super.globtype(key, ev)) {
          if (Config.profile && key == '`') {
             new Profwnd(new Coord(100, 100), this, this.gprof, "Glob prof");
          } else if (Config.profile && key == '~') {
             GameUI gi = this.findchild(GameUI.class);
             if (gi != null && gi.map != null) {
                new Profwnd(new Coord(100, 100), this, gi.map.prof, "MV prof");
             }
          } else if (key == ':') {
             this.entercmd();
          } else if (ev.getKeyCode() == KeyEvent.VK_F2) {
             CartographWindow.toggle();
          } else if (key != 0) {
             this.wdgmsg("gk", new Object[]{Integer.valueOf(key)});
          }
       }

       return true;
    }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      this.drawcmd(g, new Coord(20, this.sz.y - 20));
   }

   @Override
   public void error(String msg) {
   }
}
