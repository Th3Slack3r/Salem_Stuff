package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;

public class ToolBeltWdg extends Window implements DropTarget {
   private static final String OPT_FLIPPED = "_flipped";
   private static final String OPT_LOCKED = "_locked";
   private static final Coord invsz = Inventory.sqlite.sz();
   private static final int COUNT = 12;
   private static final int BELTS = 6;
   private static final Tex[] BELTNUMS = new Tex[6];
   private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
   private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
   private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
   private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
   GameUI gui;
   private int curbelt = 0;
   private int start = 0;
   boolean locked;
   boolean flipped;
   private Resource pressed;
   private Resource dragging;
   private int preslot;
   private IButton lockbtn;
   private IButton flipbtn;
   private IButton minus;
   private IButton plus;
   public final int[] beltkeys;
   private Tex[] nums;
   private Coord beltNumC;
   private Resource curttr = null;
   private boolean curttl = false;
   private Tex curtt = null;
   private long hoverstart;
   Item ttitem = null;

   public ToolBeltWdg(GameUI parent, String name, int beltn, int[] keys) {
      super(new Coord(5, 500), Coord.z, parent, name);
      this.cap = null;
      this.gui = parent;
      this.start = beltn;
      this.beltkeys = keys;
      this.mrgn = new Coord(0, 0);
      this.cbtn.visible = false;
      this.justclose = true;
      this.init();
   }

   private void init() {
      this.lockbtn = new IButton(Coord.z, this, this.locked ? ilockc : ilocko, this.locked ? ilocko : ilockc, this.locked ? ilockch : ilockoh) {
         @Override
         public void click() {
            ToolBeltWdg.this.locked = !ToolBeltWdg.this.locked;
            if (ToolBeltWdg.this.locked) {
               this.up = ToolBeltWdg.ilockc;
               this.down = ToolBeltWdg.ilocko;
               this.hover = ToolBeltWdg.ilockch;
            } else {
               this.up = ToolBeltWdg.ilocko;
               this.down = ToolBeltWdg.ilockc;
               this.hover = ToolBeltWdg.ilockoh;
            }

            ToolBeltWdg.this.storeOpt("_locked", ToolBeltWdg.this.locked);
         }
      };
      this.lockbtn.recthit = true;
      this.flipbtn = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flipo")) {
         @Override
         public void click() {
            ToolBeltWdg.this.flip();
         }
      };
      this.flipbtn.recthit = true;
      this.minus = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/charsh/minusup"), Resource.loadimg("gfx/hud/charsh/minusdown")) {
         @Override
         public void click() {
            ToolBeltWdg.this.prevBelt();
         }
      };
      this.plus = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/charsh/plusup"), Resource.loadimg("gfx/hud/charsh/plusdown")) {
         @Override
         public void click() {
            ToolBeltWdg.this.nextBelt();
         }
      };
      this.resize();
      this.nums = new Tex[12];

      for (int i = 0; i < 12; i++) {
         String key = KeyEvent.getKeyText(this.beltkeys[i]);
         this.nums[i] = new TexI(Utils.outline2(Text.render(key).img, Color.BLACK, true));
      }
   }

   @Override
   protected void loadOpts() {
      super.loadOpts();
      synchronized (Config.window_props) {
         this.locked = this.getOptBool("_locked", false);
         if (this.getOptBool("_flipped", false)) {
            this.flip();
         }
      }
   }

   private void flip() {
      this.flipped = !this.flipped;
      this.storeOpt("_flipped", this.flipped);
      this.resize();
   }

   private void resize() {
      this.resize(this.beltc(11).add(invsz).add(this.flipped ? new Coord(0, 14) : new Coord(14, 0)));
   }

   protected int getbelt() {
      return this.getbelt(0);
   }

   protected int getbelt(int i) {
      return i + (this.start + this.curbelt) * 12;
   }

   protected void nextBelt() {
      this.curbelt = (this.curbelt + 1) % 6;
   }

   protected void prevBelt() {
      this.curbelt = (this.curbelt + 6 - 1) % 6;
   }

   @Override
   protected void placetwdgs() {
      super.placetwdgs();
      if (this.flipped) {
         if (this.flipbtn != null) {
            this.flipbtn.c = new Coord(this.asz.x - this.flipbtn.sz.x - 7, 0);
         }

         if (this.plus != null) {
            this.plus.c = new Coord(3, this.asz.y - this.plus.sz.y);
         }

         if (this.minus != null) {
            this.minus.c = this.asz.sub(this.minus.sz).add(-3, 0);
            this.beltNumC = this.plus.c.add(this.minus.c).add(this.minus.sz).div(2);
         }
      } else {
         if (this.flipbtn != null) {
            this.flipbtn.c = new Coord(0, this.asz.y - this.flipbtn.sz.y - 7);
         }

         if (this.plus != null) {
            this.plus.c = new Coord(this.asz.x - this.plus.sz.x, 3);
         }

         if (this.minus != null) {
            this.minus.c = this.asz.sub(this.minus.sz).add(0, -3);
            this.beltNumC = this.plus.c.add(this.minus.c).add(this.minus.sz).div(2);
         }
      }
   }

   @Override
   public void cdraw(GOut g) {
      super.cdraw(g);

      for (int i = 0; i < 12; i++) {
         int slot = this.getbelt(i);
         Coord c = this.beltc(i);
         g.image(Inventory.sqlite, this.beltc(i));
         Tex tex = null;

         try {
            Indir<Resource> ir = this.gui.belt[slot];
            if (ir != null) {
               tex = ir.get().layer(Resource.imgc).tex();
            }

            g.image(tex, c.add(4, 4));
         } catch (Loading var7) {
            WItem.missing.loadwait();
            tex = WItem.missing.layer(Resource.imgc).tex();
            g.image(tex, c, invsz);
         }

         g.chcolor(200, 220, 200, 255);
         g.aimage(this.nums[i], c.add(invsz), 1.0, 1.0);
         g.chcolor();
      }

      g.aimage(BELTNUMS[this.curbelt], this.beltNumC, 0.5, 0.5);
   }

   @Override
   public void draw(GOut og) {
      super.draw(og);
      if (this.dragging != null) {
         Tex tex = this.dragging.layer(Resource.imgc).tex();
         og.root().aimage(tex, this.ui.mc, 0.5, 0.5);
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      int slot = this.beltslot(c);
      if (button == 1) {
         this.pressed = this.beltres(slot);
         this.preslot = slot;
         if (this.pressed != null) {
            this.ui.grabmouse(this);
         } else {
            super.mousedown(c, button);
            if (this.locked) {
               this.canceldm();
            }
         }
      } else if (button == 3 && !this.locked) {
         this.clearslot(slot);
      }

      return true;
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      int slot = this.beltslot(c);
      if (button == 1) {
         if (this.dragging != null) {
            this.ui.dropthing(this.ui.root, this.ui.mc, this.dragging);
            this.dragging = this.pressed = null;
         } else if (this.pressed != null) {
            if (this.pressed == this.beltres(slot)) {
               this.use(this.preslot);
            }

            this.pressed = null;
            this.preslot = -1;
         }

         this.ui.grabmouse(null);
      }

      if (this.dm) {
         Config.setWindowOpt(this.name + "_pos", this.c.toString());
      }

      super.mouseup(c, button);
      return true;
   }

   @Override
   public void mousemove(Coord c) {
      if (!this.locked && this.dragging == null && this.pressed != null) {
         this.dragging = this.pressed;
         this.clearslot(this.beltslot(c));
         this.pressed = null;
         this.preslot = -1;
      } else {
         super.mousemove(c);
      }
   }

   public boolean key(char key, KeyEvent ev) {
      if (key != 0) {
         return false;
      } else {
         boolean M = (ev.getModifiersEx() & 768) != 0;

         for (int i = 0; i < this.beltkeys.length; i++) {
            if (ev.getKeyCode() == this.beltkeys[i]) {
               if (M) {
                  this.curbelt = i % 6;
                  return true;
               }

               this.keyact(i);
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean globtype(char ch, KeyEvent ev) {
      return !this.key(ch, ev) ? super.globtype(ch, ev) : true;
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      return false;
   }

   private boolean checkmenu(int slot) {
      Resource res = this.beltres(slot);
      if (res == null) {
         return false;
      } else {
         Resource.AButton ab = res.layer(Resource.AButton.class);
         if (ab != null && (ab.ad.length == 0 || ab.ad[0].equals("@"))) {
            this.ui.mnu.useres(res);
         }

         return false;
      }
   }

   private void use(int slot) {
      if (slot != -1) {
         if (!this.checkmenu(slot)) {
            slot = this.getbelt(slot);
            this.ui.gui.wdgmsg("belt", slot, 1, this.ui.modflags());
         }
      }
   }

   private void keyact(int index) {
      if (index != -1) {
         if (!this.checkmenu(index)) {
            final int slot = this.getbelt(index);
            MapView map = this.ui.gui.map;
            if (map != null) {
               Coord mvc = map.rootxlate(this.ui.mc);
               if (mvc.isect(Coord.z, map.sz)) {
                  map.delay(new MapView.Hittest(map, mvc) {
                     {
                        x0.getClass();
                     }

                     @Override
                     protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                        if (inf != null && inf.gob == null) {
                           ToolBeltWdg.this.ui.gui.wdgmsg("belt", slot, 1, ToolBeltWdg.this.ui.modflags(), mc, (int)inf.gob.id, (int)inf.gob.id);
                        } else {
                           ToolBeltWdg.this.ui.gui.wdgmsg("belt", slot, 1, ToolBeltWdg.this.ui.modflags(), mc);
                        }
                     }

                     @Override
                     protected void nohit(Coord pc) {
                        ToolBeltWdg.this.ui.gui.wdgmsg("belt", slot, 1, ToolBeltWdg.this.ui.modflags());
                     }
                  });
               }
            }
         }
      }
   }

   private void clearslot(int slot) {
      if (slot != -1) {
         this.ui.gui.wdgmsg("setbelt", this.getbelt(slot), 1);
      }
   }

   private Coord beltc(int i) {
      return this.flipped
         ? new Coord(0, (invsz.y + 2) * i + 10 * (i / 4) + ilockc.getWidth() + 2)
         : new Coord((invsz.x + 2) * i + 10 * (i / 4) + ilockc.getWidth() + 2, 0);
   }

   public int beltslot(Coord c) {
      c = c.sub(this.ctl);

      for (int i = 0; i < 12; i++) {
         if (c.isect(this.beltc(i), invsz)) {
            return i;
         }
      }

      return -1;
   }

   public Resource beltres(int slot) {
      if (slot == -1) {
         return null;
      } else {
         slot = this.getbelt(slot);
         Resource res = null;

         try {
            Indir<Resource> ir = this.gui.belt[slot];
            if (ir != null) {
               res = ir.get();
            }
         } catch (Loading var4) {
         }

         return res;
      }
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      int slot = this.beltslot(c);
      long now = System.currentTimeMillis();
      if (slot != -1) {
         slot = this.getbelt(slot);
         Indir<Resource> ir = this.gui.belt[slot];
         if (ir != null) {
            if (prev != this) {
               this.hoverstart = now;
            }

            boolean ttl = now - this.hoverstart > 500L;

            try {
               Resource res = ir.get();
               if (res == null) {
                  return null;
               }

               if (res.layer(Resource.action) == null) {
                  return null;
               }

               Item itm = Wiki.get(res.layer(Resource.action).name);
               if (res != this.curttr || ttl != this.curttl || itm != this.ttitem) {
                  this.ttitem = itm;
                  this.curtt = MenuGrid.rendertt(res, ttl, false);
                  this.curttr = res;
                  this.curttl = ttl;
               }

               return this.curtt;
            } catch (Loading var10) {
               return "...";
            }
         }
      }

      this.hoverstart = now;
      return null;
   }

   @Override
   public boolean dropthing(Coord cc, Object thing) {
      int slot = this.beltslot(cc);
      if (slot != -1) {
         slot = this.getbelt(slot);
         if (thing instanceof Resource) {
            Resource res = (Resource)thing;
            if (res.layer(Resource.action) != null) {
               this.gui.wdgmsg("setbelt", slot, res.name);
               return true;
            }
         }
      }

      return false;
   }

   static {
      for (int i = 0; i < 6; i++) {
         String num = String.format("%d", i);
         BELTNUMS[i] = new TexI(Utils.outline2(Text.render(num).img, Color.BLACK, true));
      }
   }
}
