package haven;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Sprite implements Rendered {
   public final Resource res;
   public final Sprite.Owner owner;
   public static List<Sprite.Factory> factories = new LinkedList<>();

   public static Sprite.Factory mkdynfact(Class<? extends Sprite> cl) {
      try {
         final Constructor<? extends Sprite> cons = cl.getConstructor(Sprite.Owner.class, Resource.class);
         return new Sprite.Factory() {
            @Override
            public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
               return Utils.construct(cons, owner, res);
            }
         };
      } catch (NoSuchMethodException var3) {
         try {
            final Constructor<? extends Sprite> cons = cl.getConstructor(Sprite.Owner.class, Resource.class, Message.class);
            return new Sprite.Factory() {
               @Override
               public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
                  return Utils.construct(cons, owner, res, sdt);
               }
            };
         } catch (NoSuchMethodException var2) {
            throw new RuntimeException("Could not find any suitable constructor for dynamic sprite");
         }
      }
   }

   protected Sprite(Sprite.Owner owner, Resource res) {
      this.res = res;
      this.owner = owner;
   }

   public static int decnum(Message sdt) {
      if (sdt == null) {
         return 0;
      } else {
         int ret = 0;

         for (int off = 0; !sdt.eom(); off += 8) {
            ret |= sdt.uint8() << off;
         }

         return ret;
      }
   }

   public static Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
      Sprite.Factory f = res.getcode(Sprite.Factory.class, false);
      if (f != null) {
         return f.create(owner, res, sdt);
      } else {
         for (Sprite.Factory fx : factories) {
            Sprite ret = fx.create(owner, res, sdt);
            if (ret != null) {
               return ret;
            }
         }

         throw new Sprite.ResourceException("Does not know how to draw resource " + res.name, res);
      }
   }

   @Override
   public void draw(GOut g) {
   }

   @Override
   public abstract boolean setup(RenderList var1);

   public boolean tick(int dt) {
      return false;
   }

   public void dispose() {
   }

   static {
      factories.add(SkelSprite.fact);
      factories.add(AnimSprite.fact);
      factories.add(StaticSprite.fact);
      factories.add(AudioSprite.fact);
   }

   public static class FactMaker implements Resource.PublishedCode.Instancer {
      public Sprite.Factory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
         if (Sprite.Factory.class.isAssignableFrom(cl)) {
            return cl.asSubclass(Sprite.Factory.class).newInstance();
         } else {
            return Sprite.class.isAssignableFrom(cl) ? Sprite.mkdynfact(cl.asSubclass(Sprite.class)) : null;
         }
      }
   }

   @Resource.PublishedCode(
      name = "spr",
      instancer = Sprite.FactMaker.class
   )
   public interface Factory {
      Sprite create(Sprite.Owner var1, Resource var2, Message var3);
   }

   public interface Owner {
      Random mkrandoom();

      Resource.Neg getneg();

      Glob glob();
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
}
