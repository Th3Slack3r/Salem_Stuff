package haven;

import java.awt.Graphics;

public class SimpleSprite {
   public final Resource.Image img;
   public final Coord cc;

   public SimpleSprite(Resource.Image img, Coord cc) {
      this.img = img;
      this.cc = cc;
   }

   public SimpleSprite(Resource res, int id, Coord cc) {
      for (Resource.Image img : res.layers(Resource.imgc)) {
         if (img.id == id) {
            this.img = img;
            this.cc = cc;
            return;
         }
      }

      throw new RuntimeException("Could not find image with id " + id + " in resource " + res.name);
   }

   public SimpleSprite(Resource res, int id) {
      this(res, id, res.layer(Resource.negc).cc);
   }

   public SimpleSprite(Resource res) {
      this(res, -1);
   }

   public final void draw(GOut g, Coord cc) {
      g.image(this.img.tex(), cc.add(this.ul()));
   }

   public final void draw(Graphics g, Coord cc) {
      Coord c = cc.add(this.ul());
      g.drawImage(this.img.img, c.x, c.y, null);
   }

   public final Coord ul() {
      return this.cc.inv().add(this.img.o);
   }

   public final Coord lr() {
      return this.ul().add(this.img.sz);
   }

   public boolean checkhit(Coord c) {
      c = c.add(this.ul().inv());
      if (c.x >= 0 && c.y >= 0 && c.x < this.img.sz.x && c.y < this.img.sz.y) {
         int cl = this.img.img.getRGB(c.x, c.y);
         return Utils.rgbm.getAlpha(cl) >= 128;
      } else {
         return false;
      }
   }
}
