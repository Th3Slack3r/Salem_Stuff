package haven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvaRender extends TexRT {
   List<Indir<Resource>> layers;
   List<Resource.Image> images;
   boolean loading;
   public static final Coord sz = new Coord(212, 249);

   public AvaRender(List<Indir<Resource>> layers) {
      super(sz);
      this.setlay(layers);
   }

   public void setlay(List<Indir<Resource>> layers) {
      this.layers = layers;
      this.loading = true;
   }

   @Override
   public boolean subrend(GOut g) {
      if (!this.loading) {
         return false;
      } else {
         List<Resource.Image> images = new ArrayList<>();
         this.loading = false;

         for (Indir<Resource> r : this.layers) {
            try {
               images.addAll(r.get().layers(Resource.imgc));
            } catch (Loading var6) {
               this.loading = true;
            }
         }

         Collections.sort(images);
         if (images.equals(this.images)) {
            return false;
         } else {
            this.images = images;
            g.gl.glClearColor(255.0F, 255.0F, 255.0F, 0.0F);
            g.gl.glClear(16384);

            for (Resource.Image i : images) {
               g.image(i.tex(), i.o);
            }

            return true;
         }
      }
   }
}
