package haven;

import java.util.Collection;
import java.util.LinkedList;

public class StaticSprite extends Sprite {
  public final Rendered[] parts;
  
  public Location prepc_location = null;
  
  public static final Sprite.Factory fact = new Sprite.Factory() {
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
        if (res.layer(FastMesh.MeshRes.class) != null || res.layer(RenderLink.Res.class) != null)
          return new StaticSprite(owner, res, sdt); 
        return null;
      }
    };
  
  public StaticSprite(Sprite.Owner owner, Resource res, Rendered[] parts) {
    super(owner, res);
    this.parts = parts;
  }
  
  public StaticSprite(Sprite.Owner owner, Resource res, Rendered part) {
    this(owner, res, new Rendered[] { part });
  }
  
  public StaticSprite(Sprite.Owner owner, Resource res, Message sdt) {
    this(owner, res, lsparts(res, sdt));
  }
  
  public static Rendered[] lsparts(Resource res, Message sdt) {
    int fl = sdt.eom() ? -65536 : Sprite.decnum(sdt);
    Collection<Rendered> rl = new LinkedList<>();
    for (FastMesh.MeshRes mr : res.<FastMesh.MeshRes>layers(FastMesh.MeshRes.class)) {
      if (mr.mat != null && (mr.id < 0 || (1 << mr.id & fl) != 0))
        rl.add(mr.mat.get().apply(mr.m)); 
    } 
    for (RenderLink.Res lr : res.<RenderLink.Res>layers(RenderLink.Res.class))
      rl.add(lr.l.make()); 
    if (res.layer(Resource.audio, "amb") != null)
      rl.add(new ActAudio.Ambience(res)); 
    return rl.<Rendered>toArray(new Rendered[0]);
  }
  
  public boolean setup(RenderList r) {
    if (this.prepc_location != null) {
      r.prepc(this.prepc_location);
      r.prepc(States.normalize);
    } 
    for (Rendered p : this.parts)
      r.add(p, null); 
    return false;
  }
}
