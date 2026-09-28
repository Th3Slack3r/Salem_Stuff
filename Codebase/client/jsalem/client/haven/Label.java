package haven;

import java.awt.Color;

public class Label extends Widget {
   Text.Foundry f;
   Text text;
   String texts;
   Color col = Color.WHITE;

   @Override
   public void draw(GOut g) {
      g.image(this.text.tex(), Coord.z);
   }

   public Label(Coord c, Widget parent, String text, int w, Text.Foundry f) {
      super(c, Coord.z, parent);
      this.f = f;
      this.text = f.renderwrap(this.texts = text, this.col, w);
      this.sz = this.text.sz();
   }

   public Label(Coord c, Widget parent, String text, Text.Foundry f) {
      super(c, Coord.z, parent);
      this.f = f;
      this.text = f.render(this.texts = text, this.col);
      this.sz = this.text.sz();
   }

   public Label(Coord c, Widget parent, String text, int w) {
      this(c, parent, text, w, Text.std);
   }

   public Label(Coord c, Widget parent, String text) {
      this(c, parent, text, Text.std);
   }

   public void settext(String text) {
      this.text = this.f.render(this.texts = text, this.col);
      this.sz = this.text.sz();
   }

   public void setcolor(Color color) {
      this.col = color;
      this.text = this.f.render(this.texts, this.col);
      this.sz = this.text.sz();
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "set") {
         this.settext((String)args[0]);
      } else if (msg == "col") {
         this.setcolor((Color)args[0]);
      } else {
         super.uimsg(msg, args);
      }
   }

   @Widget.RName("lbl")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return args.length > 1 ? new Label(c, parent, (String)args[0], (Integer)args[1]) : new Label(c, parent, (String)args[0]);
      }
   }
}
