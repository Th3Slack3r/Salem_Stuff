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
  
  private final List<Setting<?>> settings = new ArrayList<>();
  
  public final EnumSetting<MeshMode> meshmode;
  
  public final BoolSetting fsaa;
  
  public final BoolSetting alphacov;
  
  public final EnumSetting<ProgMode> progmode;
  
  public final BoolSetting flight;
  
  public final BoolSetting cel;
  
  public final BoolSetting lshadow;
  
  public final BoolSetting outline;
  
  public final BoolSetting wsurf;
  
  public final FloatSetting anisotex;
  
  public static class SettingException extends RuntimeException {
    public SettingException(String msg) {
      super(msg);
    }
  }
  
  public abstract class Setting<T> implements Serializable {
    public final String nm;
    
    public T val;
    
    public Setting(String nm) {
      this.nm = nm.intern();
      GLSettings.this.settings.add(this);
    }
    
    public abstract void set(String param1String);
    
    public abstract void validate(T param1T);
    
    public abstract T defval();
    
    public void set(T val) {
      validate(val);
      this.val = val;
    }
    
    public boolean available(T val) {
      try {
        validate(val);
        return true;
      } catch (SettingException settingException) {
        return false;
      } 
    }
  }
  
  public abstract class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String nm) {
      super(nm);
    }
    
    public void set(String val) {
      boolean bval;
      try {
        bval = Utils.parsebool(val);
      } catch (IllegalArgumentException e) {
        throw new GLSettings.SettingException("Not a boolean value: " + e);
      } 
      set(Boolean.valueOf(bval));
    }
  }
  
  public abstract class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> real;
    
    public EnumSetting(String nm, Class<E> real) {
      super(nm);
      this.real = real;
    }
    
    public void set(String val) {
      Enum enum_;
      E f = null;
      val = val.toUpperCase();
      for (Enum enum_1 : EnumSet.<E>allOf(this.real)) {
        if (enum_1.name().toUpperCase().startsWith(val)) {
          if (f != null)
            throw new GLSettings.SettingException("Multiple settings with this abbreviation: " + f.name() + ", " + enum_1.name()); 
          enum_ = enum_1;
        } 
      } 
      if (enum_ == null)
        throw new GLSettings.SettingException("No such setting: " + val); 
      set((E)enum_);
    }
  }
  
  public abstract class FloatSetting extends Setting<Float> {
    public FloatSetting(String nm) {
      super(nm);
    }
    
    public void set(String val) {
      float fval;
      try {
        fval = Float.parseFloat(val);
      } catch (NumberFormatException e) {
        throw new GLSettings.SettingException("Not a floating-point value: " + val);
      } 
      set(Float.valueOf(fval));
    }
    
    public abstract float min();
    
    public abstract float max();
  }
  
  public enum MeshMode {
    MEM, DLIST, VAO;
  }
  
  private GLSettings(GLConfig cfg) {
    this.meshmode = new EnumSetting<MeshMode>("meshmode", MeshMode.class) {
        public GLSettings.MeshMode defval() {
          if (GLSettings.this.cfg.exts.contains("GL_ARB_vertex_array_object"))
            return GLSettings.MeshMode.VAO; 
          return GLSettings.MeshMode.DLIST;
        }
        
        public void validate(GLSettings.MeshMode mode) {
          switch (mode) {
            case VAO:
              if (!GLSettings.this.cfg.exts.contains("GL_ARB_vertex_array_object"))
                throw new GLSettings.SettingException("VAOs are not supported."); 
              break;
          } 
        }
      };
    this.fsaa = new BoolSetting("fsaa") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue() && !GLSettings.this.cfg.havefsaa())
            throw new GLSettings.SettingException("FSAA is not supported."); 
        }
      };
    this.alphacov = new BoolSetting("alphacov") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue() && 
            !GLSettings.this.fsaa.val.booleanValue())
            throw new GLSettings.SettingException("Alpha-to-coverage must be used with multisampling."); 
        }
      };
    this.progmode = new EnumSetting<ProgMode>("progmode", ProgMode.class) {
        public GLSettings.ProgMode defval() {
          if (GLSettings.this.cfg.haveglsl())
            return GLSettings.ProgMode.REQ; 
          return GLSettings.ProgMode.NEVER;
        }
        
        public void validate(GLSettings.ProgMode val) {
          if (val.on && !GLSettings.this.cfg.haveglsl())
            throw new GLSettings.SettingException("GLSL is not supported."); 
        }
      };
    this.flight = new BoolSetting("flight") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue()) {
            if (!GLSettings.this.cfg.haveglsl())
              throw new GLSettings.SettingException("Per-pixel lighting requires a shader-compatible video card."); 
            if (!((GLSettings.ProgMode)GLSettings.this.progmode.val).on)
              throw new GLSettings.SettingException("Per-pixel lighting requires shader usage."); 
          } 
        }
      };
    this.cel = new BoolSetting("cel") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue() && 
            !GLSettings.this.flight.val.booleanValue())
            throw new GLSettings.SettingException("Cel-shading requires per-fragment lighting."); 
        }
      };
    this.lshadow = new BoolSetting("sdw") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue()) {
            if (!GLSettings.this.flight.val.booleanValue())
              throw new GLSettings.SettingException("Shadowed lighting requires per-fragment lighting."); 
            if (!GLSettings.this.cfg.havefbo())
              throw new GLSettings.SettingException("Shadowed lighting requires a video card supporting framebuffers."); 
          } 
        }
      };
    this.outline = new BoolSetting("outl") {
        public Boolean defval() {
          return Boolean.valueOf(false);
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue()) {
            if (!((GLSettings.ProgMode)GLSettings.this.progmode.val).on)
              throw new GLSettings.SettingException("Outline rendering requires shader usage."); 
            if (!GLSettings.this.cfg.havefbo())
              throw new GLSettings.SettingException("Outline rendering requires a video card supporting framebuffers."); 
          } 
        }
      };
    this.wsurf = new BoolSetting("wsurf") {
        public Boolean defval() {
          return Boolean.valueOf((((GLSettings.ProgMode)GLSettings.this.progmode.val).on && GLSettings.this.cfg.glmajver >= 3));
        }
        
        public void validate(Boolean val) {
          if (val.booleanValue() && 
            !((GLSettings.ProgMode)GLSettings.this.progmode.val).on)
            throw new GLSettings.SettingException("Shaded water surface requires a shader-compatible video card."); 
        }
      };
    this.anisotex = new FloatSetting("aniso") {
        public Float defval() {
          return Float.valueOf(0.0F);
        }
        
        public float min() {
          return 0.0F;
        }
        
        public float max() {
          return GLSettings.this.cfg.anisotropy;
        }
        
        public void validate(Float val) {
          if (val.floatValue() != 0.0F) {
            if (GLSettings.this.cfg.anisotropy <= 1.0F)
              throw new GLSettings.SettingException("Video card does not support anisotropic filtering."); 
            if (val.floatValue() > GLSettings.this.cfg.anisotropy)
              throw new GLSettings.SettingException("Video card only supports up to " + GLSettings.this.cfg.anisotropy + "x anistropic filtering."); 
            if (val.floatValue() < 0.0F)
              throw new GLSettings.SettingException("Anisostropy factor cannot be negative."); 
          } 
        }
        
        public void set(Float val) {
          super.set(val);
          TexGL.setallparams();
        }
      };
    this.cfg = cfg;
  }
  
  public enum ProgMode {
    NEVER(false),
    REQ(true),
    ALWAYS(true);
    
    public final boolean on;
    
    ProgMode(boolean on) {
      this.on = on;
    }
  }
  
  public Iterable<Setting<?>> settings() {
    return this.settings;
  }
  
  public Object savedata() {
    Map<String, Object> ret = new HashMap<>();
    for (Setting<?> s : this.settings)
      ret.put(s.nm, s.val); 
    return ret;
  }
  
  public void save() {
    Utils.setprefb("glconf", Utils.serialize(savedata()));
  }
  
  private static <T> void iAmRunningOutOfNamesToInsultJavaWith(Setting<T> s) {
    s.val = s.defval();
  }
  
  public static GLSettings defconf(GLConfig cfg) {
    GLSettings gs = new GLSettings(cfg);
    for (Setting<?> s : gs.settings)
      iAmRunningOutOfNamesToInsultJavaWith(s); 
    return gs;
  }
  
  private static <T> void iExistOnlyToIntroduceATypeVariableSinceJavaSucks(Setting<T> s, Object val) {
    s.set((T)val);
  }
  
  public static GLSettings load(Object data, GLConfig cfg, boolean failsafe) {
    GLSettings gs = defconf(cfg);
    Map<?, ?> dat = (Map<?, ?>)data;
    for (Setting<?> s : gs.settings) {
      if (dat.containsKey(s.nm))
        try {
          iExistOnlyToIntroduceATypeVariableSinceJavaSucks(s, dat.get(s.nm));
        } catch (SettingException e) {
          if (!failsafe)
            throw e; 
        }  
    } 
    return gs;
  }
  
  public static GLSettings load(GLConfig cfg, boolean failsafe) {
    Object dat;
    byte[] data = Utils.getprefb("glconf", (byte[])null);
    if (data == null)
      return defconf(cfg); 
    try {
      dat = Utils.deserialize(data);
    } catch (Exception e) {
      dat = null;
    } 
    if (dat == null)
      return defconf(cfg); 
    return load(dat, cfg, failsafe);
  }
}
