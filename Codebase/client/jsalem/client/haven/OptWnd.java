package haven;

import java.awt.event.KeyEvent;

public class OptWnd extends Window {
   public final OptWnd.Panel main = new OptWnd.Panel(new Coord(200, 200));
   public OptWnd.Panel current;

   public void chpanel(OptWnd.Panel p) {
      if (this.current != null) {
         this.current.hide();
      }

      (this.current = p).show();
      this.pack();
   }

   public OptWnd(Coord c, Widget parent) {
      super(c, Coord.z, parent, "Options");
      OptWnd.Panel video = new OptWnd.VideoPanel(this.main);
      OptWnd.Panel audio = new OptWnd.Panel(new Coord(200, 200));
      new OptWnd.PButton(new Coord(0, 0), 200, this.main, "Video settings", 118, video);
      new OptWnd.PButton(new Coord(0, 30), 200, this.main, "Audio settings", 97, audio);
      new Button(new Coord(0, 120), 200, this.main, "Switch character") {
         @Override
         public void click() {
            this.<GameUI>getparent(GameUI.class).act("lo", "cs");
         }
      };
      new Button(new Coord(0, 150), 200, this.main, "Log out") {
         @Override
         public void click() {
            this.<GameUI>getparent(GameUI.class).act("lo");
         }
      };
      new Button(new Coord(0, 180), 200, this.main, "Close") {
         @Override
         public void click() {
            OptWnd.this.hide();
         }
      };
      int y = 0;
      new Label(new Coord(0, y), audio, "Audio volume");
      y += 20;
      new HSlider(new Coord(0, y), 200, audio, 0, 1000, (int)(Audio.volume * 1000.0)) {
         @Override
         public void changed() {
            Audio.setvolume(this.val / 1000.0);
         }
      };
      y += 30;
      new Label(new Coord(0, y), audio, "Music volume");
      y += 20;
      new HSlider(new Coord(0, y), 200, audio, 0, 1000, (int)(Music.volume * 1000.0)) {
         @Override
         public void changed() {
            Music.setvolume(this.val / 1000.0);
         }
      };
      new OptWnd.PButton(new Coord(0, 180), 200, audio, "Back", 27, this.main);
      this.chpanel(this.main);
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this && msg == "close") {
         this.hide();
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void show() {
      this.chpanel(this.main);
      super.show();
   }

   public class PButton extends Button {
      public final OptWnd.Panel tgt;
      public final int key;

      public PButton(Coord c, int w, Widget parent, String title, int key, OptWnd.Panel tgt) {
         super(c, w, parent, title);
         this.tgt = tgt;
         this.key = key;
      }

      @Override
      public void click() {
         OptWnd.this.chpanel(this.tgt);
      }

      @Override
      public boolean type(char key, KeyEvent ev) {
         if (this.key != -1 && key == this.key) {
            this.click();
            return true;
         } else {
            return false;
         }
      }
   }

   public class Panel extends Widget {
      public Panel(Coord sz) {
         super(Coord.z, sz, OptWnd.this);
         this.visible = false;
      }
   }

   public class VideoPanel extends OptWnd.Panel {
      private OptWnd.VideoPanel.CPanel curcf = null;

      public VideoPanel(OptWnd.Panel back) {
         super(new Coord(200, 200));
         OptWnd.this.new PButton(new Coord(0, 180), 200, this, "Back", 27, back);
      }

      @Override
      public void draw(GOut g) {
         if (this.curcf == null || g.gc.pref != this.curcf.cf) {
            if (this.curcf != null) {
               this.curcf.destroy();
            }

            this.curcf = new OptWnd.VideoPanel.CPanel(g.gc.pref);
         }

         super.draw(g);
      }

      public class CPanel extends Widget {
         public final GLSettings cf;

         public CPanel(GLSettings gcf) {
            super(Coord.z, new Coord(200, 175), VideoPanel.this);
            this.cf = gcf;
            int y = 0;
            new CheckBox(new Coord(0, y), this, "Render shadows") {
               {
                  this.a = CPanel.this.cf.lshadow.val;
               }

               @Override
               public void set(boolean val) {
                  if (val) {
                     try {
                        CPanel.this.cf.flight.set(true);
                        CPanel.this.cf.lshadow.set(true);
                     } catch (GLSettings.SettingException var3) {
                        this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
                        return;
                     }
                  } else {
                     CPanel.this.cf.lshadow.set(false);
                     CPanel.this.cf.flight.set(false);
                  }

                  this.a = val;
                  CPanel.this.cf.dirty = true;
               }
            };
            y += 20;
            new CheckBox(new Coord(0, y), this, "Antialiasing") {
               {
                  this.a = CPanel.this.cf.fsaa.val;
               }

               @Override
               public void set(boolean val) {
                  try {
                     CPanel.this.cf.fsaa.set(val);
                  } catch (GLSettings.SettingException var3) {
                     this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
                     return;
                  }

                  this.a = val;
                  CPanel.this.cf.dirty = true;
               }
            };
            y += 20;
            new CheckBox(new Coord(0, y), this, "Better quality water") {
               {
                  this.a = CPanel.this.cf.wsurf.val;
               }

               @Override
               public void set(boolean val) {
                  try {
                     CPanel.this.cf.wsurf.set(val);
                  } catch (GLSettings.SettingException var3) {
                     this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
                     return;
                  }

                  this.a = val;
                  CPanel.this.cf.dirty = true;
               }
            };
            y += 20;
            new Label(new Coord(0, y), this, "Anisotropic filtering");
            if (this.cf.anisotex.max() <= 1.0F) {
               new Label(new Coord(15, y + 15), this, "(Not supported)");
            } else {
               final Label dpy = new Label(new Coord(165, y + 15), this, "");
               new HSlider(
                  new Coord(0, y + 15),
                  160,
                  this,
                  (int)(this.cf.anisotex.min() * 128.0F),
                  (int)(this.cf.anisotex.max() * 128.0F),
                  (int)(this.cf.anisotex.val * 128.0F)
               ) {
                  {
                     this.dpy();
                     this.c.y = dpy.c.y + (dpy.sz.y - this.sz.y) / 2;
                  }

                  void dpy() {
                     if (this.val < 128) {
                        dpy.settext("Off");
                     } else {
                        dpy.settext(String.format("%.1fx", this.val / 128.0));
                     }
                  }

                  @Override
                  public void changed() {
                     try {
                        CPanel.this.cf.anisotex.set(this.val / 128.0F);
                     } catch (GLSettings.SettingException var2) {
                        this.<GameUI>getparent(GameUI.class).error(var2.getMessage());
                        return;
                     }

                     this.dpy();
                     CPanel.this.cf.dirty = true;
                  }
               };
            }

            y += 30;
            new Label(new Coord(0, y), this, "Camera type:");
            y += 20;
            RadioGroup camera_group = new RadioGroup(this) {
               @Override
               public void changed(int btn, String lbl) {
                  if (lbl.equals("Orthographic camera")) {
                     try {
                        CPanel.this.ui.cons.run("cam ortho");
                     } catch (Exception var6) {
                     }
                  } else if (lbl.equals("Follow camera")) {
                     try {
                        CPanel.this.ui.cons.run("cam follow");
                     } catch (Exception var5) {
                     }
                  } else if (lbl.equals("Freestyle camera")) {
                     try {
                        CPanel.this.ui.cons.run("cam best");
                     } catch (Exception var4) {
                     }
                  }

                  Utils.setpref("cameratype", lbl);
               }
            };
            camera_group.add("Orthographic camera", new Coord(15, y));
            y += 20;
            camera_group.add("Follow camera", new Coord(15, y));
            y += 20;
            camera_group.add("Freestyle camera", new Coord(15, y));
            camera_group.check(Utils.getpref("cameratype", "Orthographic camera"));
            camera_group.check("Orthographic camera");
         }
      }
   }
}
