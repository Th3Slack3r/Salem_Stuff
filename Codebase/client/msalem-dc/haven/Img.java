package haven;

public class Img extends Widget {
   private Indir<Resource> res;
   private Tex img;
   public boolean hit = false;

   @Override
   public void draw(GOut g) {
      if (this.res != null) {
         try {
            this.img = this.res.get().layer(Resource.imgc).tex();
            this.resize(this.img.sz());
            this.res = null;
         } catch (Loading var3) {
         }
      }

      if (this.img != null) {
         g.image(this.img, Coord.z);
      }
   }

   public Img(Coord c, Tex img, Widget parent) {
      super(c, img.sz(), parent);
      this.res = null;
      this.img = img;
   }

   public Img(Coord c, Indir<Resource> res, Widget parent) {
      super(c, Coord.z, parent);
      this.res = res;
      this.img = null;
   }

   @Override
   public void uimsg(String name, Object... args) {
      if (name == "ch") {
         if (args[0] instanceof String) {
            String nm = (String)args[0];
            int ver = args.length > 1 ? (Integer)args[1] : -1;
            this.res = new Resource.Spec(nm, ver);
         } else {
            this.res = this.ui.sess.getres((Integer)args[0]);
         }
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (this.hit) {
         this.wdgmsg("click", new Object[]{c, button, this.ui.modflags()});
         return true;
      } else {
         return false;
      }
   }

   @Widget.RName("img")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         int a = 0;
         Indir<Resource> res;
         if (args[a] instanceof String) {
            String nm = (String)args[a++];
            int ver = args.length > a ? (Integer)args[a++] : -1;
            res = new Resource.Spec(nm, ver);
         } else {
            res = parent.ui.sess.getres((Integer)args[a++]);
         }

         Img ret = new Img(c, res, parent);
         if (args.length > a) {
            ret.hit = (Integer)args[a++] != 0;
         }

         return ret;
      }
   }
}
