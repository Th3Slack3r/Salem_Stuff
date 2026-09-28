package haven.resutil;

import haven.GLState;
import haven.GOut;
import haven.Material;
import haven.Resource;
import haven.TexGL;
import haven.TexR;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Macro1;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import java.util.Collection;
import java.util.List;

public class TexPal extends GLState {
   public static final GLState.Slot<TexPal> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, TexPal.class);
   public final TexGL tex;
   private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
   private static final ShaderMacro[] shaders = new ShaderMacro[]{new ShaderMacro() {
      @Override
      public void modify(ProgramContext prog) {
         Tex2D.tex2d(prog.fctx).mod(new Macro1<Expression>() {
            public Expression expand(Expression in) {
               return Cons.texture2D(TexPal.ctex.ref(), Cons.pick(in, "rg"));
            }
         }, -100);
      }
   }};
   private GLState.TexUnit sampler;

   public TexPal(TexGL tex) {
      this.tex = tex;
   }

   @Override
   public ShaderMacro[] shaders() {
      return shaders;
   }

   @Override
   public boolean reqshaders() {
      return true;
   }

   @Override
   public void reapply(GOut g) {
   }

   @Override
   public void apply(GOut g) {
      this.sampler = TexGL.lbind(g, this.tex);
      this.reapply(g);
   }

   @Override
   public void unapply(GOut g) {
      this.sampler.ufree();
      this.sampler = null;
   }

   @Override
   public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
   }

   @Material.ResName("pal")
   public static class $res implements Material.ResCons2 {
      @Override
      public void cons(final Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
         int a = 0;
         final Resource tres;
         final int tid;
         if (args[a] instanceof String) {
            tres = Resource.load((String)args[a], (Integer)args[a + 1]);
            tid = (Integer)args[a + 2];
            a += 3;
         } else {
            tres = res;
            tid = (Integer)args[a];
            a++;
         }

         left.add(new Material.Res.Resolver() {
            @Override
            public void resolve(Collection<GLState> buf) {
               TexR rt = tres.layer(TexR.class, tid);
               if (rt == null) {
                  throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", tid, res, tres));
               } else {
                  buf.add(new TexPal(rt.tex()));
               }
            }
         });
      }
   }
}
