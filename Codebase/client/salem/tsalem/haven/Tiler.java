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
  
  public Tiler(int id) {
    this.id = id;
  }
  
  public void layover(MapMesh m, Coord lc, Coord gc, int z, Resource.Tile t) {
    m.getClass();
    new MapMesh.Plane(m, m.gnd(), lc, z, t);
  }
  
  public GLState drawstate(Glob glob, GLConfig cfg, Coord3f c) {
    return null;
  }
  
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Discoverable
  public static @interface ResName {
    String value();
  }
  
  @PublishedCode(name = "tile", instancer = FactMaker.class)
  public static interface Factory {
    Tiler create(int param1Int, Resource.Tileset param1Tileset);
  }
  
  public static class FactMaker implements Resource.PublishedCode.Instancer {
    public Tiler.Factory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
      if (Tiler.Factory.class.isAssignableFrom(cl))
        return cl.<Tiler.Factory>asSubclass(Tiler.Factory.class).newInstance(); 
      if (Tiler.class.isAssignableFrom(cl)) {
        Class<? extends Tiler> tcl = cl.asSubclass(Tiler.class);
        try {
          final Constructor<? extends Tiler> cons = tcl.getConstructor(new Class[] { int.class, Resource.Tileset.class });
          return new Tiler.Factory() {
              public Tiler create(int id, Resource.Tileset set) {
                return Utils.<Tiler>construct(cons, new Object[] { Integer.valueOf(id), set });
              }
            };
        } catch (NoSuchMethodException noSuchMethodException) {
          throw new RuntimeException("Could not find dynamic tiler contructor for " + tcl);
        } 
      } 
      return null;
    }
  }
  
  private static final Map<String, Factory> rnames = new TreeMap<>();
  
  static {
    AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            for (Class<?> cl : (Iterable<Class<?>>)Loader.get(Tiler.ResName.class).classes()) {
              String nm = ((Tiler.ResName)cl.<Tiler.ResName>getAnnotation(Tiler.ResName.class)).value();
              try {
                Tiler.rnames.put(nm, (Tiler.Factory)cl.newInstance());
              } catch (InstantiationException e) {
                throw new Error(e);
              } catch (IllegalAccessException e) {
                throw new Error(e);
              } 
            } 
            return null;
          }
        });
  }
  
  public static Factory byname(String name) {
    return rnames.get(name);
  }
  
  public abstract void lay(MapMesh paramMapMesh, Random paramRandom, Coord paramCoord1, Coord paramCoord2);
  
  public abstract void trans(MapMesh paramMapMesh, Random paramRandom, Tiler paramTiler, Coord paramCoord1, Coord paramCoord2, int paramInt1, int paramInt2, int paramInt3);
}
