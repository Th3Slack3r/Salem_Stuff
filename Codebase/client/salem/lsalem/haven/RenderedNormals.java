package haven;

import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.FragmentContext;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;

public class RenderedNormals extends FBConfig.RenderTarget {
   private static final IntMap<ShaderMacro[]> shcache = new IntMap<>();
   public static final GLState.Slot<GLState> slot;
   public static final PView.RenderContext.DataID<RenderedNormals> id = new PView.RenderContext.DataID<RenderedNormals>() {
      public RenderedNormals make(PView.RenderContext ctx) {
         return new RenderedNormals();
      }
   };

   private static ShaderMacro[] code(final int id) {
      ShaderMacro[] ret = shcache.get(id);
      if (ret == null) {
         ret = new ShaderMacro[]{new ShaderMacro() {
            @Override
            public void modify(final ProgramContext prog) {
               MiscLib.frageyen(prog.fctx);
               FragmentContext var10003 = prog.fctx;
               prog.fctx.getClass();
               new FragmentContext.FragData(var10003, id) {
                  {
                     x0.getClass();
                  }

                  @Override
                  public Expression root() {
                     return Cons.vec4(Cons.mul(Cons.add(MiscLib.frageyen(prog.fctx).depref(), Cons.l(1.0)), Cons.l(0.5)), Cons.l(1.0));
                  }
               };
            }
         }};
         shcache.put(id, ret);
      }

      return ret;
   }

   @Override
   public GLState state(final FBConfig cfg, final int id) {
      return new GLState() {
         private final ShaderMacro[] shaders = RenderedNormals.code(id);

         @Override
         public ShaderMacro[] shaders() {
            return this.shaders;
         }

         @Override
         public boolean reqshaders() {
            return true;
         }

         @Override
         public void apply(GOut g) {
            GLFrameBuffer fb = g.st.get(GLFrameBuffer.slot);
            if (fb != cfg.fb) {
               throw new RuntimeException("Applying normal rendering in illegal framebuffer context");
            } else {
               if (g.st.get(States.presdepth.slot) != null) {
                  fb.mask(g, id, false);
               }
            }
         }

         @Override
         public void unapply(GOut g) {
            g.st.cur(GLFrameBuffer.slot).mask(g, id, true);
         }

         @Override
         public void prep(GLState.Buffer buf) {
            buf.put(RenderedNormals.slot, this);
         }
      };
   }

   static {
      slot = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class, GLFrameBuffer.slot, States.presdepth.slot);
   }
}
