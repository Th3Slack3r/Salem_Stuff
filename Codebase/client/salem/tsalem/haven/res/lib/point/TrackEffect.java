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

public class TrackEffect extends Sprite {
  static Resource sres = Resource.load("gfx/fx/arrow", 1);
  
  Rendered fx;
  
  public double a1;
  
  public double lasta1;
  
  public double a2;
  
  public double lasta2;
  
  double ca;
  
  double oa;
  
  double da;
  
  double t;
  
  double tt;
  
  public TrackEffect(Sprite.Owner paramOwner, double paramDouble1, double paramDouble2) {
    super(paramOwner, sres);
    this.a1 = paramDouble1;
    this.a2 = paramDouble2;
    this.ca = (paramDouble1 + paramDouble2) / 2.0D;
    this.t = this.tt = 0.0D;
    FastMesh.MeshRes localMeshRes = (FastMesh.MeshRes)sres.layer(FastMesh.MeshRes.class);
    this.fx = localMeshRes.mat.get().apply((Rendered)localMeshRes.m);
  }
  
  public boolean tick(int paramInt) {
    double d1 = paramInt / 1000.0D;
    this.t += d1;
    if (this.t > this.tt || Config.altprosp) {
      this.oa = this.ca;
      double d2 = this.a1 + Math.random() * (this.a2 - this.a1);
      this.da = d2 - this.oa;
      if (this.da > Math.PI)
        this.da -= 6.283185307179586D; 
      this.t = 0.0D;
      this.tt = Math.max(Math.min(Math.abs(this.oa - d2), 0.3D), 0.1D);
    } 
    this.ca = this.oa + this.da * this.t / this.tt;
    if (this.a1 != this.lasta1 || this.a2 != this.lasta2) {
      System.out.println("From " + this.a1 + " to " + this.a2);
      this.lasta1 = this.a1;
      this.lasta2 = this.a2;
    } 
    return false;
  }
  
  public boolean setup(RenderList paramRenderList) {
    if (Config.altprosp) {
      paramRenderList.add(this.fx, GLState.compose(new GLState[] { (GLState)Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a1)), (GLState)Location.xlate(new Coord3f(5.0F, 0.0F, 0.0F)) }));
      paramRenderList.add(this.fx, GLState.compose(new GLState[] { (GLState)Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - (this.a1 + this.a2) / 2.0D)), (GLState)Location.xlate(new Coord3f(5.0F, 0.0F, 1.0F)), (GLState)new Material.Colors(Color.GREEN) }));
      paramRenderList.add(this.fx, GLState.compose(new GLState[] { (GLState)Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.a2)), (GLState)Location.xlate(new Coord3f(5.0F, 0.0F, 0.0F)) }));
    } else {
      paramRenderList.add(this.fx, (GLState)Location.rot(Coord3f.zu, (float)(((Gob)this.owner).a - this.ca)));
    } 
    return false;
  }
}
