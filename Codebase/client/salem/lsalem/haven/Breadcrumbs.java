package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public abstract class Breadcrumbs extends Widget {
   private static final Coord border = new Coord(2, 2);
   private static final BufferedImage separator = Resource.loadimg("gfx/hud/breadcrumb");
   private final Coord SZ;
   private List<Breadcrumbs.Crumb> crumbs;
   private List<IButton> buttons;

   public Breadcrumbs(Coord c, Coord sz, Widget parent) {
      super(c, sz.add(0, border.x * 2), parent);
      int d = sz.y;
      this.SZ = new Coord(d, d);
      this.buttons = new LinkedList<>();
   }

   public void setCrumbs(List<Breadcrumbs.Crumb> crumbs) {
      this.crumbs = crumbs;
      this.cleanButtons();
      this.createButtons();
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender instanceof IButton && this.buttons.contains(sender)) {
         if (msg.equals("activate")) {
            int k = this.buttons.indexOf(sender);
            this.selected(this.crumbs.get(k).data);
         }
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   public abstract void selected(Object var1);

   @Override
   public void draw(GOut g) {
      for (int i = 1; i < this.buttons.size(); i++) {
         g.image(separator, this.buttons.get(i).c.sub(12, -2));
      }

      super.draw(g);
   }

   private void createButtons() {
      int w = 0;

      for (Breadcrumbs.Crumb item : this.crumbs) {
         BufferedImage txt = Text.render(item.text).img;
         Coord isz = Utils.imgsz(txt).add(border.x * 3 + this.SZ.x, 0);
         int ty = (this.sz.y - isz.y) / 2;
         isz.y = this.sz.y;
         BufferedImage up = TexI.mkbuf(isz);
         Graphics g = up.getGraphics();
         g.drawImage(item.img, border.x, border.y, this.SZ.x, this.SZ.y, null);
         g.drawImage(txt, this.SZ.x + border.x * 2, ty, null);
         BufferedImage down = this.highlight(up, new Color(1156572170, true));
         BufferedImage over = this.highlight(up, new Color(1153811389, true));
         IButton btn = new IButton(new Coord(w, 0), this, up, down, over);
         btn.recthit = true;
         this.buttons.add(btn);
         w += isz.x + 14;
      }
   }

   private BufferedImage highlight(BufferedImage img, Color color) {
      Coord imgsz = Utils.imgsz(img);
      BufferedImage ret = TexI.mkbuf(imgsz);
      Graphics g = ret.getGraphics();
      g.drawImage(img, 0, 0, color, null);
      return ret;
   }

   private void cleanButtons() {
      for (IButton btn : this.buttons) {
         this.ui.destroy(btn);
      }

      this.buttons.clear();
   }

   public static class Crumb {
      public BufferedImage img;
      public String text;
      public Object data;

      public Crumb(BufferedImage img, String text, Object data) {
         this.img = img;
         this.text = text;
         this.data = data;
      }
   }
}
