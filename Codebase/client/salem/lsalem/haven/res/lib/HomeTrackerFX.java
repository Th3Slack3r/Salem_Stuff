package haven.res.lib;

import haven.Config;
import haven.Coord;
import haven.Coord3f;
import haven.FastMesh;
import haven.GLState;
import haven.GOut;
import haven.Gob;
import haven.Location;
import haven.Material;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import haven.UI;
import haven.Widget;
import java.awt.Color;

public class HomeTrackerFX extends Sprite {
   private static final Location SCALE = Location.scale(new Coord3f(1.2F, 1.2F, 1.0F));
   private static final Material.Colors COLORS = new Material.Colors(Color.GREEN);
   private static final Location XLATE = Location.xlate(new Coord3f(0.0F, 0.0F, 2.5F));
   static Resource sres = Resource.load("gfx/fx/arrow", 1);
   Rendered fx = null;
   double ca = 0.0;
   Gob.Overlay curol = null;
   public Coord c = null;

   public HomeTrackerFX(Sprite.Owner owner) {
      super(owner, sres);
      ((Gob)owner).ols.add(this.curol = new Gob.Overlay(this));
   }

   @Override
   public boolean setup(RenderList d) {
      if (Config.hptr && UI.instance != null && Config.hpointv) {
         if (this.fx == null) {
            FastMesh.MeshRes mres = sres.layer(FastMesh.MeshRes.class);
            this.fx = mres.mat.get().apply(mres.m);
         }

         if (this.c != null && ((Gob)this.owner).rc != null) {
            Location rot = Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca));
            d.add(this.fx, GLState.compose(XLATE, SCALE, COLORS, rot));
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean tick(int dt) {
      if (this.c != null && ((Gob)this.owner).rc != null) {
         this.ca = ((Gob)this.owner).rc.angle(this.c);
      }

      return false;
   }

   @Override
   public void dispose() {
      super.dispose();
      ((Gob)this.owner).ols.remove(this.curol);
   }

   public static class HTrackWdg extends Widget {
      private Widget ptr;
      private HomeTrackerFX fx;
      private Gob player = null;
      private Coord hc;

      public HTrackWdg(Widget parent, Widget ptr) {
         super(Coord.z, Coord.z, parent);
         this.ptr = ptr;
      }

      @Override
      public void uimsg(String msg, Object... args) {
         if (msg.equals("upd")) {
            Coord hc = (Coord)args[0];
            this.hc = hc;
            if (this.fx != null) {
               this.fx.c = hc;
            }
         }

         this.ptr.uimsg(msg, args);
      }

      @Override
      public void draw(GOut g) {
         super.draw(g);
         Gob gob = this.ui.sess.glob.oc.getgob(this.ui.gui.plid);
         if (gob != this.player) {
            this.player = gob;
            if (this.fx != null) {
               this.fx.dispose();
               this.fx = null;
            }

            if (this.player != null) {
               this.fx = new HomeTrackerFX(this.player);
               this.fx.c = this.hc;
            }
         }
      }

      public void dispose() {
         if (this.fx != null) {
            this.fx.dispose();
         }

         if (this.ptr != null) {
            this.ptr.destroy();
         }
      }
   }
}
