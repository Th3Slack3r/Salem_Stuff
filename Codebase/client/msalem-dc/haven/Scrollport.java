package haven;

public class Scrollport extends Widget {
   public final Scrollbar bar;
   public final Scrollport.Scrollcont cont;

   public Scrollport(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.bar = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, 0) {
         @Override
         public void changed() {
            Scrollport.this.cont.sy = Scrollport.this.bar.val;
         }
      };
      this.cont = new Scrollport.Scrollcont(Coord.z, sz.sub(this.bar.sz.x, 0), this) {
         @Override
         public void update() {
            Scrollport.this.bar.max = Math.max(0, this.csz().y - this.sz.y);
         }
      };
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      this.bar.ch(amount * 15);
      return true;
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      return this.cont.makechild(type, pargs, cargs);
   }

   @Override
   public void resize(Coord nsz) {
      super.resize(nsz);
      this.bar.c = new Coord(this.sz.x - this.bar.sz.x, 0);
      this.cont.resize(this.sz.sub(this.bar.sz.x, 0));
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "wpack") {
         this.resize(new Coord(this.cont.contentsz().x + this.bar.sz.x, this.sz.y));
      } else {
         super.uimsg(msg, args);
      }
   }

   @Widget.RName("scr")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Scrollport(c, (Coord)args[0], parent);
      }
   }

   public static class Scrollcont extends Widget {
      public int sy = 0;

      public Scrollcont(Coord c, Coord sz, Widget parent) {
         super(c, sz, parent);
      }

      public Coord csz() {
         Coord mx = new Coord();

         for (Widget ch = this.child; ch != null; ch = ch.next) {
            if (ch.c.x + ch.sz.x > mx.x) {
               mx.x = ch.c.x + ch.sz.x;
            }

            if (ch.c.y + ch.sz.y > mx.y) {
               mx.y = ch.c.y + ch.sz.y;
            }
         }

         return mx;
      }

      public void update() {
      }

      @Override
      public Widget makechild(String type, Object[] pargs, Object[] cargs) {
         Widget ret = super.makechild(type, pargs, cargs);
         this.update();
         return ret;
      }

      @Override
      public Coord xlate(Coord c, boolean in) {
         return in ? c.add(0, -this.sy) : c.add(0, this.sy);
      }

      @Override
      public void draw(GOut g) {
         Widget wdg = this.child;

         while (wdg != null) {
            Widget next = wdg.next;
            if (wdg.visible) {
               Coord cc = this.xlate(wdg.c, true);
               if (cc.y + wdg.sz.y >= 0 && cc.y <= this.sz.y) {
                  wdg.draw(g.reclip(cc, wdg.sz));
               }
            }

            wdg = next;
         }
      }
   }
}
