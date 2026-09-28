package haven.glsl;

public abstract class Type {
   public static final Type VOID = new Type.Simple("void");
   public static final Type INT = new Type.Simple("int");
   public static final Type FLOAT = new Type.Simple("float");
   public static final Type VEC2 = new Type.Simple("vec2");
   public static final Type VEC3 = new Type.Simple("vec3");
   public static final Type VEC4 = new Type.Simple("vec4");
   public static final Type IVEC2 = new Type.Simple("ivec2");
   public static final Type IVEC3 = new Type.Simple("ivec3");
   public static final Type IVEC4 = new Type.Simple("ivec4");
   public static final Type MAT3 = new Type.Simple("mat3");
   public static final Type MAT4 = new Type.Simple("mat4");
   public static final Type SAMPLER2D = new Type.Simple("sampler2D");
   public static final Type SAMPLER2DMS = new Type.Simple("sampler2DMS");
   public static final Type SAMPLER3D = new Type.Simple("sampler3D");
   public static final Type SAMPLERCUBE = new Type.Simple("samplerCube");

   public abstract String name(Context var1);

   private static class Simple extends Type {
      private final String name;

      private Simple(String name) {
         this.name = name;
      }

      @Override
      public String name(Context ctx) {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
