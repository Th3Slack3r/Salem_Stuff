package haven.test;

import haven.Charlist;
import haven.Listbox;
import haven.Widget;

public class CharSelector extends Robot {
   Runnable cb;
   String chr;
   Charlist chrlist;

   public CharSelector(TestClient c, String chr, Runnable cb) {
      super(c);
      this.chr = chr;
      this.cb = cb;
   }

   public void check() {
      if (this.chrlist != null) {
         if (this.chr == null) {
            this.chr = this.chrlist.chars.get(0).name;
         } else {
            Charlist.Char found = null;

            for (Charlist.Char ch : this.chrlist.chars) {
               if (ch.name.equals(this.chr)) {
                  found = ch;
                  break;
               }
            }

            if (found == null) {
               throw new RobotException(this, "requested character not found: " + this.chr);
            }
         }

         this.chrlist.wdgmsg("play", new Object[]{this.chr});
      }
   }

   @Override
   public void newwdg(int id, Widget w, Object... args) {
      if (w instanceof Listbox) {
         this.chrlist = (Charlist)w;
      }

      this.check();
   }

   @Override
   public void dstwdg(int id, Widget w) {
      if (w == this.chrlist) {
         this.destroy();
         this.succeed();
      }
   }

   @Override
   public void uimsg(int id, Widget w, String msg, Object... args) {
   }

   public void succeed() {
      if (this.cb != null) {
         this.cb.run();
      }
   }
}
