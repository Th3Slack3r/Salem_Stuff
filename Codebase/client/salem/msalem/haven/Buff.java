package haven;

import java.awt.Color;

public class Buff {
   public static final Text.Foundry nfnd = new Text.Foundry("SansSerif", 10);
   int id;
   Indir<Resource> res;
   String tt = null;
   int ameter = -1;
   int nmeter = -1;
   int cmeter = -1;
   int cticks = -1;
   long gettime;
   Tex ntext = null;
   boolean major = false;

   public Buff(int id, Indir<Resource> res) {
      this.id = id;
      this.res = res;
   }

   Tex nmeter() {
      if (this.ntext == null) {
         this.ntext = new TexI(Utils.outline2(nfnd.render(Integer.toString(this.nmeter), Color.WHITE).img, Color.BLACK));
      }

      return this.ntext;
   }

   public String tooltip() {
      return this.tt != null ? this.tt : this.res.get().layer(Resource.tooltip).t;
   }
}
