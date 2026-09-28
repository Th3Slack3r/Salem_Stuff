package haven;

import dolda.jglob.Discoverable;
import dolda.jglob.Loader;
import java.awt.Color;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.media.opengl.GL2;

public class Material extends GLState {
   public final GLState[] states;
   public static final GLState nofacecull = new GLState.StandAlone(GLState.Slot.Type.GEOM, PView.proj) {
      @Override
      public void apply(GOut g) {
         g.gl.glDisable(2884);
      }

      @Override
      public void unapply(GOut g) {
         g.gl.glEnable(2884);
      }
   };
   public static final float[] defamb = new float[]{0.2F, 0.2F, 0.2F, 1.0F};
   public static final float[] defdif = new float[]{0.8F, 0.8F, 0.8F, 1.0F};
   public static final float[] defspc = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
   public static final float[] defemi = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
   public static final GLState.Slot<Material.Colors> colors = new GLState.Slot<>(GLState.Slot.Type.DRAW, Material.Colors.class);
   private static final Map<String, Material.ResCons2> rnames = new TreeMap<>();

   @Override
   public void apply(GOut g) {
   }

   @Override
   public void unapply(GOut g) {
   }

   public Material(GLState... states) {
      this.states = states;
   }

   public Material() {
      this(Light.deflight, new Material.Colors());
   }

   public Material(Color amb, Color dif, Color spc, Color emi, float shine) {
      this(Light.deflight, new Material.Colors(amb, dif, spc, emi, shine));
   }

   public Material(Color col) {
      this(Light.deflight, new Material.Colors(col));
   }

   public Material(Tex tex) {
      this(Light.deflight, new Material.Colors(), tex.draw(), tex.clip());
   }

   @Override
   public String toString() {
      return Arrays.asList(this.states).toString();
   }

   @Override
   public void prep(GLState.Buffer buf) {
      for (GLState st : this.states) {
         st.prep(buf);
      }
   }

   static {
      for (Class<?> cl : Loader.get(Material.ResName.class).classes()) {
         String nm = cl.getAnnotation(Material.ResName.class).value();
         if (Material.ResCons.class.isAssignableFrom(cl)) {
            final Material.ResCons scons;
            try {
               scons = cl.asSubclass(Material.ResCons.class).newInstance();
            } catch (InstantiationException var6) {
               throw new Error(var6);
            } catch (IllegalAccessException var7) {
               throw new Error(var7);
            }

            rnames.put(nm, new Material.ResCons2() {
               @Override
               public void cons(Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
                  GLState ret = scons.cons(res, args);
                  if (ret != null) {
                     states.add(ret);
                  }
               }
            });
         } else if (Material.ResCons2.class.isAssignableFrom(cl)) {
            try {
               rnames.put(nm, cl.asSubclass(Material.ResCons2.class).newInstance());
            } catch (InstantiationException var8) {
               throw new Error(var8);
            } catch (IllegalAccessException var9) {
               throw new Error(var9);
            }
         } else {
            if (!GLState.class.isAssignableFrom(cl)) {
               throw new Error("Illegal material constructor class: " + cl);
            }

            final Constructor<? extends GLState> cons;
            try {
               cons = cl.asSubclass(GLState.class).getConstructor(Resource.class, Object[].class);
            } catch (NoSuchMethodException var5) {
               throw new Error("No proper constructor for res-consable GL state " + cl.getName(), var5);
            }

            rnames.put(nm, new Material.ResCons2() {
               @Override
               public void cons(Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
                  states.add(Utils.construct(cons, res, args));
               }
            });
         }
      }
   }

   @Material.ResName("nofacecull")
   public static class $nofacecull implements Material.ResCons {
      @Override
      public GLState cons(Resource res, Object... args) {
         return Material.nofacecull;
      }
   }

   @Material.ResName("order")
   public static class $order implements Material.ResCons {
      @Override
      public GLState cons(Resource res, Object... args) {
         String nm = (String)args[0];
         if (nm.equals("first")) {
            return Rendered.first;
         } else if (nm.equals("last")) {
            return Rendered.last;
         } else if (nm.equals("pfx")) {
            return Rendered.postpfx;
         } else if (nm.equals("eye")) {
            return Rendered.eyesort;
         } else {
            throw new Resource.LoadException("Unknown draw order: " + nm, res);
         }
      }
   }

   @Material.ResName("vcol")
   public static class $vcol implements Material.ResCons {
      @Override
      public GLState cons(Resource res, Object... args) {
         return new States.ColState((Color)args[0]);
      }
   }

   @Material.ResName("col")
   public static class Colors extends GLState {
      public float[] amb;
      public float[] dif;
      public float[] spc;
      public float[] emi;
      public float shine;

      public Colors() {
         this.amb = Material.defamb;
         this.dif = Material.defdif;
         this.spc = Material.defspc;
         this.emi = Material.defemi;
      }

      private Colors(float[] amb, float[] dif, float[] spc, float[] emi, float shine) {
         this.amb = amb;
         this.dif = dif;
         this.spc = spc;
         this.emi = emi;
         this.shine = shine;
      }

      private static float[] colmul(float[] c1, float[] c2) {
         return new float[]{c1[0] * c2[0], c1[1] * c2[1], c1[2] * c2[2], c1[3] * c2[3]};
      }

      private static float[] colblend(float[] in, float[] bl) {
         float f1 = bl[3];
         float f2 = 1.0F - f1;
         return new float[]{in[0] * f2 + bl[0] * f1, in[1] * f2 + bl[1] * f1, in[2] * f2 + bl[2] * f1, in[3]};
      }

      public Colors(Color amb, Color dif, Color spc, Color emi, float shine) {
         this(Utils.c2fa(amb), Utils.c2fa(dif), Utils.c2fa(spc), Utils.c2fa(emi), shine);
      }

      public Colors(Color amb, Color dif, Color spc, Color emi) {
         this(amb, dif, spc, emi, 0.0F);
      }

      public Colors(Color col) {
         this(
            new Color(
               (int)(col.getRed() * Material.defamb[0]), (int)(col.getGreen() * Material.defamb[1]), (int)(col.getBlue() * Material.defamb[2]), col.getAlpha()
            ),
            new Color(
               (int)(col.getRed() * Material.defdif[0]), (int)(col.getGreen() * Material.defdif[1]), (int)(col.getBlue() * Material.defdif[2]), col.getAlpha()
            ),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 0),
            0.0F
         );
      }

      public Colors(Resource res, Object... args) {
         this((Color)args[0], (Color)args[1], (Color)args[2], (Color)args[3], (Float)args[4]);
      }

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         gl.glMaterialfv(1032, 4608, this.amb, 0);
         gl.glMaterialfv(1032, 4609, this.dif, 0);
         gl.glMaterialfv(1032, 4610, this.spc, 0);
         gl.glMaterialfv(1032, 5632, this.emi, 0);
         gl.glMaterialf(1032, 5633, this.shine);
      }

      @Override
      public void unapply(GOut g) {
         GL2 gl = g.gl;
         gl.glMaterialfv(1032, 4608, Material.defamb, 0);
         gl.glMaterialfv(1032, 4609, Material.defdif, 0);
         gl.glMaterialfv(1032, 4610, Material.defspc, 0);
         gl.glMaterialfv(1032, 5632, Material.defemi, 0);
         gl.glMaterialf(1032, 5633, 0.0F);
      }

      @Override
      public int capplyfrom(GLState from) {
         return from instanceof Material.Colors ? 5 : -1;
      }

      @Override
      public void applyfrom(GOut g, GLState from) {
         if (from instanceof Material.Colors) {
            this.apply(g);
         }
      }

      @Override
      public void prep(GLState.Buffer buf) {
         Material.Colors p = buf.get(Material.colors);
         if (p != null) {
            buf.put(Material.colors, p.combine(this));
         } else {
            buf.put(Material.colors, this);
         }
      }

      public Material.Colors combine(Material.Colors other) {
         return new Material.Colors(
            colblend(other.amb, this.amb), colblend(other.dif, this.dif), colblend(other.spc, this.spc), colblend(other.emi, this.emi), other.shine
         );
      }

      @Override
      public String toString() {
         return String.format(
            "(%.1f, %.1f, %.1f), (%.1f, %.1f, %.1f), (%.1f, %.1f, %.1f @ %.1f)",
            this.amb[0],
            this.amb[1],
            this.amb[2],
            this.dif[0],
            this.dif[1],
            this.dif[2],
            this.spc[0],
            this.spc[1],
            this.spc[2],
            this.shine
         );
      }
   }

   @Resource.LayerName("mat2")
   public static class NewMat implements Resource.LayerFactory<Material.Res> {
      public Material.Res cons(Resource res, byte[] bbuf) {
         Message buf = new Message(0, bbuf);
         int id = buf.uint16();
         Material.Res ret = new Material.Res(res, id);

         while (!buf.eom()) {
            String nm = buf.string();
            Object[] args = buf.list();
            if (nm.equals("linear")) {
               ret.linear = true;
            } else if (nm.equals("mipmap")) {
               ret.mipmap = true;
            } else {
               Material.ResCons2 cons = Material.rnames.get(nm);
               if (cons == null) {
                  throw new Resource.LoadException("Unknown material part name: " + nm, res);
               }

               cons.cons(res, ret.states, ret.left, args);
            }
         }

         return ret;
      }
   }

   @Resource.LayerName("mat")
   public static class OldMat implements Resource.LayerFactory<Material.Res> {
      private static Color col(byte[] buf, int[] off) {
         double r = Utils.floatd(buf, off[0]);
         off[0] += 5;
         double g = Utils.floatd(buf, off[0]);
         off[0] += 5;
         double b = Utils.floatd(buf, off[0]);
         off[0] += 5;
         double a = Utils.floatd(buf, off[0]);
         off[0] += 5;
         return new Color((float)r, (float)g, (float)b, (float)a);
      }

      public Material.Res cons(final Resource res, byte[] buf) {
         int id = Utils.uint16d(buf, 0);
         Material.Res ret = new Material.Res(res, id);
         int[] off = new int[]{2};
         GLState light = Light.deflight;

         while (off[0] < buf.length) {
            String thing = Utils.strd(buf, off).intern();
            if (thing == "col") {
               Color amb = col(buf, off);
               Color dif = col(buf, off);
               Color spc = col(buf, off);
               double shine = Utils.floatd(buf, off[0]);
               off[0] += 5;
               Color emi = col(buf, off);
               ret.states.add(new Material.Colors(amb, dif, spc, emi, (float)shine));
            } else if (thing == "linear") {
               ret.linear = true;
            } else if (thing == "mipmap") {
               ret.mipmap = true;
            } else if (thing == "nofacecull") {
               ret.states.add(Material.nofacecull);
            } else if (thing == "tex") {
               final int tid = Utils.uint16d(buf, off[0]);
               off[0] += 2;
               ret.left.add(new Material.Res.Resolver() {
                  @Override
                  public void resolve(Collection<GLState> buf) {
                     for (Resource.Image img : res.layers(Resource.imgc)) {
                        if (img.id == tid) {
                           buf.add(img.tex().draw());
                           buf.add(img.tex().clip());
                           return;
                        }
                     }

                     throw new RuntimeException(String.format("Specified texture %d not found in %s", tid, res));
                  }
               });
            } else if (thing == "texlink") {
               final String nm = Utils.strd(buf, off);
               final int ver = Utils.uint16d(buf, off[0]);
               off[0] += 2;
               final int tid = Utils.uint16d(buf, off[0]);
               off[0] += 2;
               ret.left.add(new Material.Res.Resolver() {
                  @Override
                  public void resolve(Collection<GLState> buf) {
                     Resource tres = Resource.load(nm, ver);

                     for (Resource.Image img : tres.layers(Resource.imgc)) {
                        if (img.id == tid) {
                           buf.add(img.tex().draw());
                           buf.add(img.tex().clip());
                           return;
                        }
                     }

                     throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", tid, res, tres));
                  }
               });
            } else {
               if (thing != "light") {
                  throw new Resource.LoadException("Unknown material part: " + thing, res);
               }

               String l = Utils.strd(buf, off);
               if (l.equals("pv")) {
                  light = Light.vlights;
               } else if (l.equals("pp")) {
                  light = Light.plights;
               } else {
                  if (!l.equals("n")) {
                     throw new Resource.LoadException("Unknown lighting type: " + thing, res);
                  }

                  light = null;
               }
            }
         }

         if (light != null) {
            ret.states.add(light);
         }

         return ret;
      }
   }

   public static class Res extends Resource.Layer implements Resource.IDLayer<Integer> {
      public final int id;
      private transient List<GLState> states;
      private transient List<Material.Res.Resolver> left;
      private transient Material m;
      private boolean mipmap;
      private boolean linear;

      public Res(Resource res, int id) {
         Objects.requireNonNull(res);
         super();
         this.states = new LinkedList<>();
         this.left = new LinkedList<>();
         this.mipmap = false;
         this.linear = false;
         this.id = id;
      }

      public Material get() {
         synchronized (this) {
            if (this.m == null) {
               Iterator<Material.Res.Resolver> i = this.left.iterator();

               while (i.hasNext()) {
                  Material.Res.Resolver r = i.next();
                  r.resolve(this.states);
                  i.remove();
               }

               this.m = new Material(this.states.toArray(new GLState[0])) {
                  @Override
                  public String toString() {
                     return super.toString() + "@" + Res.this.getres().name;
                  }
               };
            }

            return this.m;
         }
      }

      @Override
      public void init() {
         for (Resource.Image img : this.getres().layers(Resource.imgc, false)) {
            TexGL tex = (TexGL)img.tex();
            if (this.mipmap) {
               tex.mipmap();
            }

            if (this.linear) {
               tex.magfilter(9729);
            }
         }
      }

      public Integer layerid() {
         return this.id;
      }

      public interface Resolver {
         void resolve(Collection<GLState> var1);
      }
   }

   public interface ResCons {
      GLState cons(Resource var1, Object... var2);
   }

   public interface ResCons2 {
      void cons(Resource var1, List<GLState> var2, List<Material.Res.Resolver> var3, Object... var4);
   }

   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Discoverable
   public @interface ResName {
      String value();
   }
}
