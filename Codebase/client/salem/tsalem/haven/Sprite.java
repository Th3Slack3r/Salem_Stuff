package haven;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Sprite implements Rendered {
  public final Resource res;
  
  public final Owner owner;
  
  public static List<Factory> factories = new LinkedList<>();
  
  static {
    factories.add(SkelSprite.fact);
    factories.add(AnimSprite.fact);
    factories.add(StaticSprite.fact);
    factories.add(AudioSprite.fact);
  }
  
  public static class FactMaker implements Resource.PublishedCode.Instancer {
    public Sprite.Factory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
      if (Sprite.Factory.class.isAssignableFrom(cl))
        return cl.<Sprite.Factory>asSubclass(Sprite.Factory.class).newInstance(); 
      if (Sprite.class.isAssignableFrom(cl))
        return Sprite.mkdynfact(cl.asSubclass(Sprite.class)); 
      return null;
    }
  }
  
  public static Factory mkdynfact(Class<? extends Sprite> cl) {
    try {
      final Constructor<? extends Sprite> cons = cl.getConstructor(new Class[] { Owner.class, Resource.class });
      return new Factory() {
          public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
            return Utils.<Sprite>construct(cons, new Object[] { owner, res });
          }
        };
    } catch (NoSuchMethodException noSuchMethodException) {
      try {
        final Constructor<? extends Sprite> cons = cl.getConstructor(new Class[] { Owner.class, Resource.class, Message.class });
        return new Factory() {
            public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
              return Utils.<Sprite>construct(cons, new Object[] { owner, res, sdt });
            }
          };
      } catch (NoSuchMethodException noSuchMethodException1) {
        throw new RuntimeException("Could not find any suitable constructor for dynamic sprite");
      } 
    } 
  }
  
  public static class ResourceException extends RuntimeException {
    public Resource res;
    
    public ResourceException(String msg, Resource res) {
      super(msg + " (" + res + ", from " + res.source + ")");
      this.res = res;
    }
    
    public ResourceException(String msg, Throwable cause, Resource res) {
      super(msg + " (" + res + ", from " + res.source + ")", cause);
      this.res = res;
    }
  }
  
  protected Sprite(Owner owner, Resource res) {
    this.res = res;
    this.owner = owner;
  }
  
  public static int decnum(Message sdt) {
    if (sdt == null)
      return 0; 
    int ret = 0, off = 0;
    while (!sdt.eom()) {
      ret |= sdt.uint8() << off;
      off += 8;
    } 
    return ret;
  }
  
  public static Sprite create(Owner owner, Resource res, Message sdt) {
    Factory f = res.<Factory>getcode(Factory.class, false);
    if (f != null)
      return f.create(owner, res, sdt); 
    for (Factory factory : factories) {
      Sprite ret = factory.create(owner, res, sdt);
      if (ret != null)
        return ret; 
    } 
    throw new ResourceException("Does not know how to draw resource " + res.name, res);
  }
  
  public void draw(GOut g) {}
  
  public boolean tick(int dt) {
    return false;
  }
  
  public void dispose() {}
  
  public abstract boolean setup(RenderList paramRenderList);
  
  @PublishedCode(name = "spr", instancer = FactMaker.class)
  public static interface Factory {
    Sprite create(Sprite.Owner param1Owner, Resource param1Resource, Message param1Message);
  }
  
  public static interface Owner {
    Random mkrandoom();
    
    Resource.Neg getneg();
    
    Glob glob();
  }
}
