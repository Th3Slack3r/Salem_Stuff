package haven;

import java.awt.Color;

public class RichTextBox extends Widget {
   public Color bg = Color.BLACK;
   private final RichText.Foundry fnd;
   private RichText text;
   private Scrollbar sb;

   public RichTextBox(Coord c, Coord sz, Widget parent, String text, RichText.Foundry fnd) {
      super(c, sz, parent);
      this.fnd = fnd;
      this.sb = new Scrollbar(
         new Coord(sz.x - Window.fbox.br.sz().x, Window.fbox.bt.sz().y), sz.y - Window.fbox.bt.sz().y - Window.fbox.bb.sz().y, this, 0, 100
      );
      this.text = fnd.render(text, sz.x - 20);
      this.sb.max = this.text.sz().y + 20 - sz.y;
   }

   public RichTextBox(Coord c, Coord sz, Widget parent, String text, Object... attrs) {
      this(c, sz, parent, text, new RichText.Foundry(attrs));
   }

   @Override
   public void draw(GOut g) {
      if (this.bg != null) {
         g.chcolor(this.bg);
         g.frect(Coord.z, this.sz);
         g.chcolor();
      }

      int v = 10 - (this.sb == null ? 0 : this.sb.val);
      if (this.text != null) {
         g.image(this.text.tex(), new Coord(10, v));
      }

      Window.fbox.draw(g, Coord.z, this.sz);
      super.draw(g);
   }

   public void settext(String text) {
      this.text = this.fnd.render(text, this.sz.x - 20);
      this.sb.max = this.text.sz().y + 20 - this.sz.y;
      this.sb.val = 0;
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      this.sb.ch(amount * 20);
      return true;
   }
}
