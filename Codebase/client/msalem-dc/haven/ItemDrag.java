package haven;

public class ItemDrag extends WItem {
   public Coord doff;

   public ItemDrag(Coord dc, Widget parent, GItem item) {
      super(parent.ui.mc.add(dc.inv()), parent, item);
      this.doff = dc;
      this.ui.grabmouse(this);
   }

   @Override
   public void drawmain(GOut g, Tex tex) {
      g.chcolor(255, 255, 255, 128);
      g.image(tex, Coord.z);
      g.chcolor();
   }

   public boolean dropon(Widget w, Coord c) {
      if (w instanceof DTarget && ((DTarget)w).drop(c, c.add(this.doff.inv()))) {
         return true;
      } else {
         for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg != this && wdg.visible) {
               Coord cc = w.xlate(wdg.c, true);
               if (c.isect(cc, wdg.sz) && this.dropon(wdg, c.add(cc.inv()))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean interact(Widget w, Coord c) {
      if (w instanceof DTarget && ((DTarget)w).iteminteract(c, c.add(this.doff.inv()))) {
         return true;
      } else {
         for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg != this && wdg.visible) {
               Coord cc = w.xlate(wdg.c, true);
               if (c.isect(cc, wdg.sz) && this.interact(wdg, c.add(cc.inv()))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button == 1) {
         this.dropon(this.parent, c.add(this.c));
      } else if (button == 3) {
         this.interact(this.parent, c.add(this.c));
      }

      return false;
   }

   @Override
   public void mousemove(Coord c) {
      this.c = this.c.add(c.add(this.doff.inv()));
   }
}
