package haven;

import java.awt.Color;

public class FBView {
   public final GLFrameBuffer fbo;
   public RenderList rls;
   public GLState basicstate;
   private final GLState ostate;
   private final PView.RenderState rstate = new FBView.RenderState();

   public FBView(GLFrameBuffer fbo, GLState basic) {
      this.fbo = fbo;
      this.basicstate = basic;
      this.ostate = HavenPanel.OrthoState.fixed(fbo.sz());
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

   public void render(Rendered root, GOut ctx) {
      if (this.rls == null || this.rls.cfg != ctx.gc) {
         this.rls = new RenderList(ctx.gc);
      }

      GOut g = this.gderive(ctx);
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

   public GOut gderive(GOut orig) {
      GLState.Buffer def = orig.basicstate();
      this.fbo.prep(def);
      this.ostate.prep(def);
      return new GOut(orig.gl, orig.ctx, orig.gc, orig.st, def, this.fbo.sz());
   }

   public void dispose() {
      this.fbo.dispose();
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
         return FBView.this.fbo.sz();
      }
   }
}
