package haven.glsl;

import java.io.IOException;
import java.io.Writer;

public class Output {
   private final Writer out;
   public final Context ctx;
   public int indent = 0;

   public Output(Writer out, Context ctx) {
      this.out = out;
      this.ctx = ctx;
   }

   public void write(char c) {
      try {
         this.out.write(c);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public void write(String str) {
      try {
         this.out.write(str);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public void write(Symbol sym) {
      this.write(sym.name(this.ctx));
   }

   public void indent() {
      for (int i = 0; i < this.indent; i++) {
         this.write("    ");
      }
   }
}
