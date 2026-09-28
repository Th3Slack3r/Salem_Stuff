package haven;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class AnimSprite extends Sprite {
  private final Rendered[] parts;
  
  private final MeshAnim.Anim[] anims;
  
  public static final Sprite.Factory fact = new Sprite.Factory() {
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
        if (res.layer(MeshAnim.Res.class) == null)
          return null; 
        return new AnimSprite(owner, res, sdt);
      }
    };
  
  private AnimSprite(Sprite.Owner owner, Resource res, Message sdt) {
    super(owner, res);
    int mask = sdt.eom() ? -65536 : Sprite.decnum(sdt);
    Collection<MeshAnim> anims = new LinkedList<>();
    for (MeshAnim.Res ar : res.<MeshAnim.Res>layers(MeshAnim.Res.class)) {
      if (ar.id < 0 || (1 << ar.id & mask) != 0)
        anims.add(ar.a); 
    } 
    this.anims = new MeshAnim.Anim[anims.size()];
    Iterator<MeshAnim> it = anims.iterator();
    for (int i = 0; it.hasNext(); i++) {
      ((MeshAnim)it.next()).getClass();
      this.anims[i] = new MeshAnim.Anim(it.next());
    } 
    MorphedMesh.Morpher.Factory morph = MorphedMesh.combine((MorphedMesh.Morpher.Factory[])this.anims);
    Collection<Rendered> rl = new LinkedList<>();
    for (FastMesh.MeshRes mr : res.<FastMesh.MeshRes>layers(FastMesh.MeshRes.class)) {
      if (mr.mat != null && (mr.id < 0 || (1 << mr.id & mask) != 0)) {
        boolean stat = true;
        for (MeshAnim anim : anims) {
          if (anim.animp(mr.m)) {
            stat = false;
            break;
          } 
        } 
        if (stat) {
          rl.add(mr.mat.get().apply(mr.m));
          continue;
        } 
        rl.add(mr.mat.get().apply(new MorphedMesh(mr.m, morph)));
      } 
    } 
    this.parts = rl.<Rendered>toArray(new Rendered[0]);
  }
  
  public boolean setup(RenderList rl) {
    for (Rendered p : this.parts)
      rl.add(p, null); 
    return false;
  }
  
  public boolean tick(int idt) {
    float dt = idt / 1000.0F;
    for (MeshAnim.Anim anim : this.anims)
      anim.tick(dt); 
    return false;
  }
}
