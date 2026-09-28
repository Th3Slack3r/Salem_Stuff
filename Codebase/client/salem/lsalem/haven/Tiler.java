package haven;

import dolda.jglob.Discoverable;
import dolda.jglob.Loader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public abstract class Tiler {
   public final int id;
   private static final Map<String, Tiler.Factory> rnames = new TreeMap<>();

   public Tiler(int id) {
      this.id = id;
   }

   public abstract void lay(MapMesh var1, Random var2, Coord var3, Coord var4);

   public abstract void trans(MapMesh var1, Random var2, Tiler var3, Coord var4, Coord var5, int var6, int var7, int var8);

   public void layover(MapMesh m, Coord lc, Coord gc, int z, Resource.Tile t) {
      m.new Plane(m.gnd(), lc, z, t);
   }

   public GLState drawstate(Glob glob, GLConfig cfg, Coord3f c) {
      return null;
   }

   public static Tiler.Factory byname(String name) {
      return rnames.get(name);
   }

   static {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
         @Override
         public Object run() {
            for (Class<?> cl : Loader.get(Tiler.ResName.class).classes()) {
               String nm = cl.getAnnotation(Tiler.ResName.class).value();

               try {
                  Tiler.rnames.put(nm, (Tiler.Factory)cl.newInstance());
               } catch (InstantiationException var5) {
                  throw new Error(var5);
               } catch (IllegalAccessException var6) {
                  throw new Error(var6);
               }
            }

            return null;
         }
      });
   }

   public static class FactMaker implements Resource.PublishedCode.Instancer {
      public Tiler.Factory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
         if (Tiler.Factory.class.isAssignableFrom(cl)) {
            return cl.asSubclass(Tiler.Factory.class).newInstance();
         } else if (Tiler.class.isAssignableFrom(cl)) {
            Class<? extends Tiler> tcl = cl.asSubclass(Tiler.class);

            try {
               final Constructor<? extends Tiler> cons = tcl.getConstructor(int.class, Resource.Tileset.class);
               return new Tiler.Factory() {
                  @Override
                  public Tiler create(int id, Resource.Tileset set) {
                     return Utils.construct(cons, id, set);
                  }
               };
            } catch (NoSuchMethodException var4) {
               throw new RuntimeException("Could not find dynamic tiler contructor for " + tcl);
            }
         } else {
            return null;
         }
      }
   }

   @Resource.PublishedCode(
      name = "tile",
      instancer = Tiler.FactMaker.class
   )
   public interface Factory {
      Tiler create(int var1, Resource.Tileset var2);
   }

   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Discoverable
   public @interface ResName {
      String value();
   }
}
