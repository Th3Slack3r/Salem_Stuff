package haven.res.lib.point;

import haven.Config;
import haven.Coord3f;
import haven.FastMesh;
import haven.GLState;
import haven.Gob;
import haven.Location;
import haven.Material;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import java.awt.Color;
import javax.swing.JOptionPane;

public class TrackEffect extends Sprite {
   static Resource sres = Resource.load("gfx/fx/arrow", 1);
   Rendered fx;
   public double a1;
   public double lasta1 = -1.0;
   public double a2;
   public double lasta2 = -1.0;
   double ca;
   double oa;
   double da;
   double t;
   double tt;

   public TrackEffect(Sprite.Owner paramOwner, double paramDouble1, double paramDouble2) {
      super(paramOwner, sres);
      this.a1 = paramDouble1;
      this.a2 = paramDouble2;
      this.ca = (paramDouble1 + paramDouble2) / 2.0;
      this.t = this.tt = 0.0;
      FastMesh.MeshRes localMeshRes = sres.layer(FastMesh.MeshRes.class);
      this.fx = localMeshRes.mat.get().apply(localMeshRes.m);
   }

   @Override
   public boolean tick(int paramInt) {
      double d1 = paramInt / 1000.0;
      this.t += d1;
      if (this.t > this.tt || Config.altprosp) {
         this.oa = this.ca;
         double d2 = this.a1 + Math.random() * (this.a2 - this.a1);
         this.da = d2 - this.oa;
         if (this.da > Math.PI) {
            this.da -= Math.PI * 2;
         }

         this.t = 0.0;
         this.tt = Math.max(Math.min(Math.abs(this.oa - d2), 0.3), 0.1);
      }

      this.ca = this.oa + this.da * (this.t / this.tt);
      if (this.a1 != this.lasta1 || this.a2 != this.lasta2) {
         System.out.println("From " + this.a1 + " to " + this.a2);
         System.out.println("Mean: " + (this.a1 + this.a2) / 2.0);
         System.out.println("Diff: " + Math.abs(this.a1 - this.a2));
         if (Math.abs(this.a1 - this.a2) < 6.0 && Math.abs(this.lasta1 - this.lasta2) >= 6.0) {
            JOptionPane.showMessageDialog(null, "FOUND PROSPECTING HIT");
         }
      }

      this.lasta1 = this.a1;
      this.lasta2 = this.a2;
      return false;
   }

   @Override
   public boolean setup(RenderList paramRenderList) {
      if (Config.altprosp) {
         paramRenderList.add(
            this.fx,
            Location.compose(new GLState[]{Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a1)), Location.xlate(new Coord3f(5.0F, 0.0F, 0.0F))})
         );
         paramRenderList.add(
            this.fx,
            Location.compose(
               new GLState[]{
                  Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - (this.a1 + this.a2) / 2.0)),
                  Location.xlate(new Coord3f(5.0F, 0.0F, 1.0F)),
                  new Material.Colors(Color.GREEN)
               }
            )
         );
         paramRenderList.add(
            this.fx,
            Location.compose(new GLState[]{Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a2)), Location.xlate(new Coord3f(5.0F, 0.0F, 0.0F))})
         );
      } else {
         paramRenderList.add(this.fx, Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca)));
      }

      return false;
   }
}
