package haven.glsl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Struct extends Type {
  public final Symbol name;
  
  public final List<Field> fields;
  
  public static class Field {
    public final Type type;
    
    public final String name;
    
    public Field(Type type, String name) {
      this.type = type;
      this.name = name;
    }
  }
  
  private Struct(Symbol name, List<Field> fields) {
    this.name = name;
    this.fields = fields;
  }
  
  public Struct(Symbol name, Field... fields) {
    this(name, Arrays.asList(fields));
  }
  
  public Struct(Symbol name) {
    this(name, new LinkedList<>());
  }
  
  public static Struct make(Symbol name, Object... args) {
    Field[] fs = new Field[args.length / 2];
    for (int f = 0, a = 0; a < args.length; ) {
      Type ft = (Type)args[a++];
      String fn = (String)args[a++];
      fs[f++] = new Field(ft, fn);
    } 
    return new Struct(name, fs);
  }
  
  public String name(Context ctx) {
    return "struct " + this.name.name(ctx);
  }
  
  public static final Struct gl_LightSourceParameters = make(new Symbol.Fix("gl_LightSourceParameters"), new Object[] { 
        Type.VEC4, "ambient", Type.VEC4, "diffuse", Type.VEC4, "specular", Type.VEC4, "position", Type.VEC4, "halfVector", 
        Type.VEC3, "spotDirection", Type.FLOAT, "spotExponent", Type.FLOAT, "spotCutoff", Type.FLOAT, "spotCosCutoff", Type.FLOAT, "constantAttenuation", 
        Type.FLOAT, "linearAttenuation", Type.FLOAT, "quadraticAttenuation" });
  
  public static final Struct gl_MaterialParameters = make(new Symbol.Fix("gl_MaterialParameters"), new Object[] { Type.VEC4, "emission", Type.VEC4, "ambient", Type.VEC4, "diffuse", Type.VEC4, "specular", Type.FLOAT, "shininess" });
}
