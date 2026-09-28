package haven;

import java.awt.Color;

public class FColor {
   public float r;
   public float g;
   public float b;
   public float a;

   public FColor(float r, float g, float b, float a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
   }

   public FColor(float r, float g, float b) {
      this(r, g, b, 1.0F);
   }

   public FColor(Color c, float f) {
      this(f * c.getRed() / 255.0F, f * c.getGreen() / 255.0F, f * c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
   }

   public FColor(Color c) {
      this(c, 1.0F);
   }

   public FColor blend(FColor o, float f) {
      float F = 1.0F - f;
      return new FColor(this.r * F + o.r * f, this.g * F + o.g * f, this.b * F + o.b * f, this.a * F + o.a * f);
   }

   public float[] to3a() {
      return new float[]{this.r, this.g, this.b};
   }

   public float[] to4a() {
      return new float[]{this.r, this.g, this.b, this.a};
   }

   @Override
   public String toString() {
      return String.format("color(%f, %f, %f, %f)", this.r, this.g, this.b, this.a);
   }
}
