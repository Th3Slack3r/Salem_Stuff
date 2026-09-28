package haven.glsl;

public abstract class Type {
  private static class Simple extends Type {
    private final String name;
    
    private Simple(String name) {
      this.name = name;
    }
    
    public String name(Context ctx) {
      return this.name;
    }
    
    public String toString() {
      return this.name;
    }
  }
  
  public static final Type VOID = new Simple("void");
  
  public static final Type INT = new Simple("int");
  
  public static final Type FLOAT = new Simple("float");
  
  public static final Type VEC2 = new Simple("vec2");
  
  public static final Type VEC3 = new Simple("vec3");
  
  public static final Type VEC4 = new Simple("vec4");
  
  public static final Type IVEC2 = new Simple("ivec2");
  
  public static final Type IVEC3 = new Simple("ivec3");
  
  public static final Type IVEC4 = new Simple("ivec4");
  
  public static final Type MAT3 = new Simple("mat3");
  
  public static final Type MAT4 = new Simple("mat4");
  
  public static final Type SAMPLER2D = new Simple("sampler2D");
  
  public static final Type SAMPLER2DMS = new Simple("sampler2DMS");
  
  public static final Type SAMPLER3D = new Simple("sampler3D");
  
  public static final Type SAMPLERCUBE = new Simple("samplerCube");
  
  public abstract String name(Context paramContext);
}
