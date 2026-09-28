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
      int code = ev.getKeyCode();
      boolean ctrl = ev.isControlDown();
      boolean shift = ev.isShiftDown();
      boolean isgui = this.ui != null && this.ui.gui != null;
      boolean alt = ev.isAltDown();
      if (!super.globtype(key, ev)) {
         if (key == 0) {
            return false;
         }

         if (Config.profile && key == '`') {
            new Profwnd(new Coord(100, 100), this, this.gprof, "Glob prof");
         } else if (Config.profile && key == '~') {
            GameUI gi = this.ui.gui;
            if (gi != null && gi.map != null) {
               new Profwnd(new Coord(100, 100), this, gi.map.prof, "MV prof");
            }
         } else if (key == ':') {
            this.entercmd();
         } else if (isgui && (code == 76 || code == 70) && ctrl && !shift) {
            FlatnessTool ft = FlatnessTool.instance(this.ui);
            if (ft != null) {
               ft.toggle();
            }
         } else if (isgui && code == 81 && ctrl && !shift) {
            LocatorTool lt = LocatorTool.instance(this.ui);
            if (lt != null) {
               lt.toggle();
            }
         } else if (isgui && code == 65 && ctrl && !shift) {
            OverviewTool ot = OverviewTool.instance(this.ui);
            if (ot != null) {
               ot.toggle();
            }
         } else if (isgui && code == 88 && ctrl && !shift) {
            CartographWindow.toggle();
         } else if (isgui && code == 68 && ctrl && !shift) {
            DarknessWnd.toggle();
         } else if (isgui && ctrl && shift && this.ui.rwidgets.containsKey(this.ui.gui)) {
            for (int i = 0; i < 6; i++) {
               if (Config.hnames[i].length() == 1 && ev.getKeyCode() == Config.hnames[i].charAt(0)) {
                  try {
                     this.ui.cons.run(Config.hcommands[i]);
                  } catch (Exception var12) {
                     System.out.println("Console not cooperating!");
                  }
               }
            }
         } else if (isgui && code == 78 && ctrl && !shift) {
            if (Config.alwaysbright) {
               Config.brightang++;
               if (Config.brightang >= 4.0F) {
                  Config.alwaysbright = false;
                  Config.brightang = 0.0F;
               }
            } else {
               Config.alwaysbright = true;
            }

            Utils.setprefb("alwaysbright", Config.alwaysbright);
            this.ui.sess.glob.brighten();
         } else if (!isgui || code != 67 || !alt) {
            if (code == 82 && alt) {
               Config.toggleRadius();
            } else if (code == 67 && alt && isgui) {
               this.ui.gui.toggleCraftWnd();
            } else if (code == 70 && alt && isgui) {
               this.ui.gui.toggleFilterWnd();
            } else if (code == 82 && ctrl && isgui) {
               Window toolbelt_window = null;

               for (Widget w : this.ui.widgets.values()) {
                  if (Window.class.isInstance(w)) {
                     Window ww = (Window)w;
                     if (ww.cap.text.contains("belt")) {
                        toolbelt_window = ww;
                     }
                  }
               }

               if (toolbelt_window == null) {
                  if (this.ui.gui.getEquipory().slots[5] != null) {
                     this.ui.gui.getEquipory().slots[5].mousedown(Coord.z, 3);
                  }
               } else {
                  toolbelt_window.cbtn.click();
               }
            } else if (code == 71 && ctrl && isgui) {
               Window toolbelt_backpack = null;

               for (Widget wx : this.ui.widgets.values()) {
                  if (Window.class.isInstance(wx)) {
                     Window ww = (Window)wx;
                     if (ww.cap.text.contains("pack")) {
                        toolbelt_backpack = ww;
                     }
                  }
               }

               if (toolbelt_backpack == null) {
                  if (this.ui.gui.getEquipory().slots[14] != null) {
                     this.ui.gui.getEquipory().slots[14].mousedown(Coord.z, 3);
                  }
               } else {
                  toolbelt_backpack.cbtn.click();
               }
            } else if (code == 90 && ctrl) {
               Config.center = !Config.center;
               this.ui.message(String.format("Tile centering in turned %s", Config.center ? "ON" : "OFF"), GameUI.MsgType.INFO);
            } else if (key != 0) {
               this.wdgmsg("gk", new Object[]{Integer.valueOf(key)});
            }
         }
      }

      return true;
   }

   @Override
   public boolean keyup(KeyEvent ev) {
      if (ev.getKeyCode() == 154) {
         Screenshooter.take(this.ui.gui, Config.screenurl);
         return true;
      } else {
         return super.keyup(ev);
      }
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
