package haven.rs;

import haven.Coord;
import haven.GLState;
import haven.GOut;
import haven.PView;
import haven.RenderList;
import haven.Rendered;
import java.awt.Color;

public class BufView {
   public final GBuffer buf;
   public RenderList rls;
   public GLState basicstate;
   private final BufView.RenderState rstate = new BufView.RenderState();

   public BufView(GBuffer buf, GLState basic) {
      this.buf = buf;
      this.basicstate = basic;
   }

   protected GLState.Buffer basic(GOut g) {
      GLState.Buffer buf = g.basicstate();
      this.rstate.prep(buf);
      if (this.basicstate != null) {
         this.basicstate.prep(buf);
      }

      return buf;
   }

   public void clear2d(GOut g, Color cc) {
      g.state2d();
      g.apply();
      g.gl.glClearColor(cc.getRed() / 255.0F, cc.getGreen() / 255.0F, cc.getBlue() / 255.0F, cc.getAlpha() / 255.0F);
      g.gl.glClear(16384);
   }

   protected void clear(GOut g) {
      g.gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      g.gl.glClear(16640);
   }

   public void render(Rendered root, GOut g) {
      if (this.rls == null || this.rls.cfg != g.gc) {
         this.rls = new RenderList(g.gc);
         this.rls.ignload = false;
      }

      GLState.Buffer bk = g.st.copy();

      try {
         GLState.Buffer def = this.basic(g);
         this.rls.setup(root, def);
         this.rls.fin();
         g.st.set(def);
         g.apply();
         this.clear(g);
         this.rls.render(g);
      } finally {
         g.st.set(bk);
      }
   }

   private class RenderState extends PView.RenderState {
      private RenderState() {
      }

      @Override
      public Coord ul() {
         return Coord.z;
      }

      @Override
      public Coord sz() {
         return BufView.this.buf.sz;
      }
   }
}
