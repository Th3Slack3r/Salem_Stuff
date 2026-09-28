package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLSettings implements Serializable {
   public final GLConfig cfg;
   public boolean dirty = false;
   private final List<GLSettings.Setting<?>> settings = new ArrayList<>();
   public final GLSettings.EnumSetting<GLSettings.MeshMode> meshmode = new GLSettings.EnumSetting<GLSettings.MeshMode>("meshmode", GLSettings.MeshMode.class) {
      public GLSettings.MeshMode defval() {
         return GLSettings.this.cfg.exts.contains("GL_ARB_vertex_array_object") ? GLSettings.MeshMode.VAO : GLSettings.MeshMode.DLIST;
      }

      public void validate(GLSettings.MeshMode mode) {
         switch (mode) {
            case VAO:
               if (!GLSettings.this.cfg.exts.contains("GL_ARB_vertex_array_object")) {
                  throw new GLSettings.SettingException("VAOs are not supported.");
               }
         }
      }
   };
   public final GLSettings.BoolSetting fsaa = new GLSettings.BoolSetting("fsaa") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val && !GLSettings.this.cfg.havefsaa()) {
            throw new GLSettings.SettingException("FSAA is not supported.");
         }
      }
   };
   public final GLSettings.BoolSetting alphacov = new GLSettings.BoolSetting("alphacov") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val && !GLSettings.this.fsaa.val) {
            throw new GLSettings.SettingException("Alpha-to-coverage must be used with multisampling.");
         }
      }
   };
   public final GLSettings.EnumSetting<GLSettings.ProgMode> progmode = new GLSettings.EnumSetting<GLSettings.ProgMode>("progmode", GLSettings.ProgMode.class) {
      public GLSettings.ProgMode defval() {
         return GLSettings.this.cfg.haveglsl() ? GLSettings.ProgMode.REQ : GLSettings.ProgMode.NEVER;
      }

      public void validate(GLSettings.ProgMode val) {
         if (val.on && !GLSettings.this.cfg.haveglsl()) {
            throw new GLSettings.SettingException("GLSL is not supported.");
         }
      }
   };
   public final GLSettings.BoolSetting flight = new GLSettings.BoolSetting("flight") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val) {
            if (!GLSettings.this.cfg.haveglsl()) {
               throw new GLSettings.SettingException("Per-pixel lighting requires a shader-compatible video card.");
            }

            if (!GLSettings.this.progmode.val.on) {
               throw new GLSettings.SettingException("Per-pixel lighting requires shader usage.");
            }
         }
      }
   };
   public final GLSettings.BoolSetting cel = new GLSettings.BoolSetting("cel") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val && !GLSettings.this.flight.val) {
            throw new GLSettings.SettingException("Cel-shading requires per-fragment lighting.");
         }
      }
   };
   public final GLSettings.BoolSetting lshadow = new GLSettings.BoolSetting("sdw") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val) {
            if (!GLSettings.this.flight.val) {
               throw new GLSettings.SettingException("Shadowed lighting requires per-fragment lighting.");
            }

            if (!GLSettings.this.cfg.havefbo()) {
               throw new GLSettings.SettingException("Shadowed lighting requires a video card supporting framebuffers.");
            }
         }
      }
   };
   public final GLSettings.BoolSetting outline = new GLSettings.BoolSetting("outl") {
      public Boolean defval() {
         return false;
      }

      public void validate(Boolean val) {
         if (val) {
            if (!GLSettings.this.progmode.val.on) {
               throw new GLSettings.SettingException("Outline rendering requires shader usage.");
            }

            if (!GLSettings.this.cfg.havefbo()) {
               throw new GLSettings.SettingException("Outline rendering requires a video card supporting framebuffers.");
            }
         }
      }
   };
   public final GLSettings.BoolSetting wsurf = new GLSettings.BoolSetting("wsurf") {
      public Boolean defval() {
         return GLSettings.this.progmode.val.on && GLSettings.this.cfg.glmajver >= 3;
      }

      public void validate(Boolean val) {
         if (val && !GLSettings.this.progmode.val.on) {
            throw new GLSettings.SettingException("Shaded water surface requires a shader-compatible video card.");
         }
      }
   };
   public final GLSettings.FloatSetting anisotex = new GLSettings.FloatSetting("aniso") {
      public Float defval() {
         return 0.0F;
      }

      @Override
      public float min() {
         return 0.0F;
      }

      @Override
      public float max() {
         return GLSettings.this.cfg.anisotropy;
      }

      public void validate(Float val) {
         if (val != 0.0F) {
            if (GLSettings.this.cfg.anisotropy <= 1.0F) {
               throw new GLSettings.SettingException("Video card does not support anisotropic filtering.");
            }

            if (val > GLSettings.this.cfg.anisotropy) {
               throw new GLSettings.SettingException("Video card only supports up to " + GLSettings.this.cfg.anisotropy + "x anistropic filtering.");
            }

            if (val < 0.0F) {
               throw new GLSettings.SettingException("Anisostropy factor cannot be negative.");
            }
         }
      }

      public void set(Float val) {
         super.set(val);
         TexGL.setallparams();
      }
   };

   private GLSettings(GLConfig cfg) {
      this.cfg = cfg;
   }

   public Iterable<GLSettings.Setting<?>> settings() {
      return this.settings;
   }

   public Object savedata() {
      Map<String, Object> ret = new HashMap<>();

      for (GLSettings.Setting<?> s : this.settings) {
         ret.put(s.nm, s.val);
      }

      return ret;
   }

   public void save() {
      Utils.setprefb("glconf", Utils.serialize(this.savedata()));
   }

   private static <T> void iAmRunningOutOfNamesToInsultJavaWith(GLSettings.Setting<T> s) {
      s.val = s.defval();
   }

   public static GLSettings defconf(GLConfig cfg) {
      GLSettings gs = new GLSettings(cfg);

      for (GLSettings.Setting<?> s : gs.settings) {
         iAmRunningOutOfNamesToInsultJavaWith(s);
      }

      return gs;
   }

   private static <T> void iExistOnlyToIntroduceATypeVariableSinceJavaSucks(GLSettings.Setting<T> s, Object val) {
      s.set((T)val);
   }

   public static GLSettings load(Object data, GLConfig cfg, boolean failsafe) {
      GLSettings gs = defconf(cfg);
      Map<?, ?> dat = (Map<?, ?>)data;

      for (GLSettings.Setting<?> s : gs.settings) {
         if (dat.containsKey(s.nm)) {
            try {
               iExistOnlyToIntroduceATypeVariableSinceJavaSucks(s, dat.get(s.nm));
            } catch (GLSettings.SettingException var8) {
               if (!failsafe) {
                  throw var8;
               }
            }
         }
      }

      return gs;
   }

   public static GLSettings load(GLConfig cfg, boolean failsafe) {
      byte[] data = Utils.getprefb("glconf", null);
      if (data == null) {
         return defconf(cfg);
      } else {
         Object dat;
         try {
            dat = Utils.deserialize(data);
         } catch (Exception var5) {
            dat = null;
         }

         return dat == null ? defconf(cfg) : load(dat, cfg, failsafe);
      }
   }

   public abstract class BoolSetting extends GLSettings.Setting<Boolean> {
      public BoolSetting(String nm) {
         super(nm);
      }

      @Override
      public void set(String val) {
         boolean bval;
         try {
            bval = Utils.parsebool(val);
         } catch (IllegalArgumentException var4) {
            throw new GLSettings.SettingException("Not a boolean value: " + var4);
         }

         this.set(bval);
      }
   }

   public abstract class EnumSetting<E extends Enum<E>> extends GLSettings.Setting<E> {
      private final Class<E> real;

      public EnumSetting(String nm, Class<E> real) {
         super(nm);
         this.real = real;
      }

      @Override
      public void set(String val) {
         E f = null;
         val = val.toUpperCase();

         for (E e : EnumSet.allOf(this.real)) {
            if (e.name().toUpperCase().startsWith(val)) {
               if (f != null) {
                  throw new GLSettings.SettingException("Multiple settings with this abbreviation: " + f.name() + ", " + e.name());
               }

               f = e;
            }
         }

         if (f == null) {
            throw new GLSettings.SettingException("No such setting: " + val);
         } else {
            this.set(f);
         }
      }
   }

   public abstract class FloatSetting extends GLSettings.Setting<Float> {
      public FloatSetting(String nm) {
         super(nm);
      }

      @Override
      public void set(String val) {
         float fval;
         try {
            fval = Float.parseFloat(val);
         } catch (NumberFormatException var4) {
            throw new GLSettings.SettingException("Not a floating-point value: " + val);
         }

         this.set(fval);
      }

      public abstract float min();

      public abstract float max();
   }

   public static enum MeshMode {
      MEM,
      DLIST,
      VAO;
   }

   public static enum ProgMode {
      NEVER(false),
      REQ(true),
      ALWAYS(true);

      public final boolean on;

      private ProgMode(boolean on) {
         this.on = on;
      }
   }

   public abstract class Setting<T> implements Serializable {
      public final String nm;
      public T val;

      public Setting(String nm) {
         this.nm = nm.intern();
         GLSettings.this.settings.add(this);
      }

      public abstract void set(String var1);

      public abstract void validate(T var1);

      public abstract T defval();

      public void set(T val) {
         this.validate(val);
         this.val = val;
      }
   }

   public static class SettingException extends RuntimeException {
      public SettingException(String msg) {
         super(msg);
      }
   }
}
