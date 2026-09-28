package haven.res.lib.tree;

import haven.Config;
import haven.GLState;
import haven.Location;
import haven.Matrix4f;
import haven.Message;
import haven.RenderList;
import haven.Resource;
import haven.Sprite;
import haven.States;
import haven.StaticSprite;

public class TreeSprite extends StaticSprite {
  private final Location scale;
  
  public final float fscale;
  
  public TreeSprite(Sprite.Owner paramOwner, Resource paramResource, float paramFloat) {
    super(paramOwner, paramResource, new Message(0));
    boolean bush = (paramResource.name.equals("gfx/terobjs/cranberry") || paramResource.name.equals("gfx/terobjs/myrtleoak") || paramResource.name.contains("bush"));
    if (!Config.raidermodetrees || bush) {
      this.fscale = paramFloat;
      if (paramFloat == 1.0F) {
        this.scale = null;
      } else {
        this.scale = mkscale(paramFloat);
      } 
    } else {
      this.fscale = Math.max(0.1F, paramFloat * 0.2F);
      this.scale = mkscale(this.fscale);
    } 
  }
  
  public TreeSprite(Sprite.Owner paramOwner, Resource paramResource, Message paramMessage) {
    super(paramOwner, paramResource, new Message(0));
    int i;
    if (paramMessage.eom()) {
      i = 100;
    } else {
      i = paramMessage.uint8();
    } 
    boolean bush = (paramResource.name.equals("gfx/terobjs/cranberry") || paramResource.name.equals("gfx/terobjs/myrtleoak") || paramResource.name.contains("bush"));
    if (!Config.raidermodetrees || bush) {
      this.fscale = i / 100.0F;
      if (i == 100) {
        this.scale = null;
      } else {
        this.scale = mkscale(this.fscale);
      } 
    } else {
      this.fscale = Math.max(0.1F, i * 0.2F / 100.0F);
      this.scale = mkscale(this.fscale);
    } 
  }
  
  public static Location mkscale(float paramFloat1, float paramFloat2, float paramFloat3) {
    return new Location(new Matrix4f(paramFloat1, 0.0F, 0.0F, 0.0F, 0.0F, paramFloat2, 0.0F, 0.0F, 0.0F, 0.0F, paramFloat3, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F));
  }
  
  public static Location mkscale(float paramFloat) {
    return mkscale(paramFloat, paramFloat, paramFloat);
  }
  
  public boolean setup(RenderList paramRenderList) {
    if (this.scale != null) {
      paramRenderList.prepc((GLState)this.scale);
      paramRenderList.prepc((GLState)States.normalize);
    } 
    return super.setup(paramRenderList);
  }
}
