package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Makewindow extends Widget {
   Widget obtn;
   Widget cbtn;
   Widget bbtn;
   public Makewindow.Spec[] inputs = new Makewindow.Spec[0];
   public Makewindow.Spec[] outputs = new Makewindow.Spec[0];
   static LinkedList<String> list = new LinkedList<>();
   static boolean requested_restore = false;
   static Coord boff = new Coord(7, 9);
   final int xoff = 40;
   final int yoff = 60;
   public static final Text.Foundry nmf = new Text.Foundry(new Font("Serif", 0, 20));
   private long hoverstart;
   private Resource lasttip;
   private Object stip;
   private Object ltip;

   public Makewindow(Coord c, Widget parent, String rcpnm) {
      super(c, Coord.z, parent);
      Label nm = new Label(new Coord(0, 0), this, rcpnm, nmf);
      nm.c = new Coord(this.sz.x - nm.sz.x, 0);
      new Label(new Coord(0, 20), this, "Input:");
      new Label(new Coord(0, 80), this, "Result:");
      this.obtn = new Button(new Coord(290, 93), 60, this, "Craft");
      this.cbtn = new Button(new Coord(360, 93), 60, this, "Craft All");
      if (list.size() > 0) {
         this.bbtn = new Button(new Coord(220, 93), 60, this, list.peek());
         this.bbtn.pack();
         this.bbtn.c.x = 280 - this.bbtn.sz.x;
      }

      this.pack();
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "inpop") {
         Makewindow.Spec[] inputs = new Makewindow.Spec[args.length / 2];
         int i = 0;

         for (int a = 0; a < args.length; a += 2) {
            inputs[i] = new Makewindow.Spec(this.ui.sess.getres((Integer)args[a]), (Integer)args[a + 1]);
            i++;
         }

         this.inputs = inputs;
      } else if (msg == "opop") {
         Makewindow.Spec[] outputs = new Makewindow.Spec[args.length / 2];
         int i = 0;

         for (int a = 0; a < args.length; a += 2) {
            outputs[i] = new Makewindow.Spec(this.ui.sess.getres((Integer)args[a]), (Integer)args[a + 1]);
            i++;
         }

         this.outputs = outputs;
      }
   }

   @Override
   public void draw(GOut g) {
      Coord c = new Coord(40, 0);
      Inventory.invsq(g, c, new Coord(this.inputs.length, 1));

      for (int i = 0; i < this.inputs.length; i++) {
         Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
         Makewindow.Spec s = this.inputs[i];

         try {
            g.image(s.res.get().layer(Resource.imgc).tex(), ic);
         } catch (Loading var8) {
         }

         if (s.num != null) {
            g.aimage(s.num, ic.add(Inventory.isqsz), 1.0, 1.0);
         }
      }

      c = new Coord(40, 60);
      Inventory.invsq(g, c, new Coord(this.outputs.length, 1));

      for (int i = 0; i < this.outputs.length; i++) {
         Coord ic = c.add(Inventory.sqoff(new Coord(i, 0)));
         Makewindow.Spec s = this.outputs[i];

         try {
            g.image(s.res.get().layer(Resource.imgc).tex(), ic);
         } catch (Loading var7) {
         }

         if (s.num != null) {
            g.aimage(s.num, ic.add(Inventory.isqsz), 1.0, 1.0);
         }
      }

      super.draw(g);
   }

   @Override
   public Object tooltip(Coord mc, Widget prev) {
      return this.tooltip(mc, prev, true);
   }

   public Object tooltip(Coord mc, Widget prev, boolean full) {
      Resource tres = null;
      Coord c = new Coord(40, 0);
      int i = 0;

      while (true) {
         if (i >= this.inputs.length) {
            c = new Coord(40, 60);
            i = 0;
            if (i < this.outputs.length && mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz)) {
               tres = this.outputs[i].res.get();
            }
            break;
         }

         if (mc.isect(c.add(Inventory.sqoff(new Coord(i, 0))), Inventory.isqsz)) {
            tres = this.inputs[i].res.get();
            break;
         }

         i++;
      }

      Resource.Tooltip layer = null;
      if (tres != null) {
         layer = tres.layer(Resource.tooltip);
      }

      String tip = layer != null ? layer.t : "<MISSING TOOLTIP>";
      if (!full) {
         return tres == null ? null : tip;
      } else if (tres == null) {
         return null;
      } else {
         if (this.lasttip != tres) {
            this.lasttip = tres;
            this.stip = this.ltip = null;
         }

         long now = System.currentTimeMillis();
         boolean sh = true;
         if (prev != this) {
            this.hoverstart = now;
         } else if (now - this.hoverstart > 1000L) {
            sh = false;
         }

         if (sh) {
            if (this.stip == null && tip != null) {
               this.stip = Text.render(tip);
            }

            return this.stip;
         } else {
            if (this.ltip == null && tip != null) {
               String t = tip + "\n" + tres.name;
               Resource.Pagina p = tres.layer(Resource.pagina);
               if (p != null) {
                  t = t + "\n\n" + tres.layer(Resource.pagina).text;
               }

               RichText ttip = RichText.render(t, 300);
               this.ltip = this.checkVars(tres.name, ttip);
            }

            return this.ltip;
         }
      }
   }

   private Object checkVars(String name, RichText ltip) {
      ItemData data = ItemData.get(name);
      if (data != null && data.variants != null) {
         BufferedImage longtip = data.variants.create().longtip();
         return new TexI(ItemInfo.catimgs(3, ltip.img, longtip));
      } else {
         return ltip;
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      Object tt = this.tooltip(c, null, false);
      if (tt != null && tt instanceof String) {
         Glob.Pagina p = this.ui.mnu.paginafor((String)tt);
         if (p != null) {
            this.store();
            this.ui.mnu.use(p);
            return true;
         }
      }

      return super.mousedown(c, button);
   }

   @Override
   public void destroy() {
      if (!requested_restore) {
         this.store();
      }

      requested_restore = false;
      super.destroy();
   }

   private void store() {
      try {
         String t = this.outputs[0].res.get().layer(Resource.tooltip).t;
         if (list.isEmpty() || !list.getFirst().equals(t)) {
            list.push(t);
         }
      } catch (Exception var2) {
         var2.printStackTrace(System.out);
      }
   }

   private void restore() {
      try {
         String name = list.pop();
         requested_restore = true;
         Glob.Pagina p = this.ui.mnu.paginafor(name);
         this.ui.mnu.use(p);
      } catch (Exception var3) {
         var3.printStackTrace(System.out);
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.obtn) {
         if (msg == "activate") {
            this.wdgmsg("make", new Object[]{0});
         }
      } else if (sender == this.cbtn) {
         if (msg == "activate") {
            this.wdgmsg("make", new Object[]{1});
         }
      } else if (sender == this.bbtn) {
         this.restore();
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public boolean globtype(char ch, KeyEvent ev) {
      if (ch == '\n') {
         this.wdgmsg("make", new Object[]{this.ui.modctrl ? 1 : 0});
         return true;
      } else {
         return super.globtype(ch, ev);
      }
   }

   @Widget.RName("make")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Makewindow(c, parent, (String)args[0]);
      }
   }

   public static class MakePrep extends ItemInfo implements GItem.ColorInfo {
      private static final Color olcol = new Color(0, 255, 0, 64);

      public MakePrep(ItemInfo.Owner owner) {
         super(owner);
      }

      @Override
      public Color olcol() {
         return olcol;
      }
   }

   public static class Spec {
      public Indir<Resource> res;
      public Tex num;

      public Spec(Indir<Resource> res, int num) {
         this.res = res;
         if (num >= 0) {
            this.num = new TexI(Utils.outline2(Text.render(Integer.toString(num), Color.WHITE).img, Utils.contrast(Color.WHITE)));
         } else {
            this.num = null;
         }
      }
   }
}
