package haven.test;

import haven.Widget;

public class DumpBot extends Robot {
   public DumpBot(TestClient c) {
      super(c);
   }

   @Override
   public void newwdg(int id, Widget w, Object... args) {
      System.out.println(this.c + ": new widget: " + w + " (" + id + ")");
   }

   @Override
   public void dstwdg(int id, Widget w) {
      System.out.println(this.c + ": destroyed: " + w + " (" + id + ")");
   }

   @Override
   public void uimsg(int id, Widget w, String msg, Object... args) {
      System.out.println(this.c + ": uimsg: " + w + " (" + id + "): " + msg);
   }
}
