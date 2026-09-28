package haven.launcher;

import java.awt.Frame;
import javax.swing.JDialog;

class AWTStatus$2 extends JDialog {
   AWTStatus$2(AWTStatus this$0, Frame arg0, String arg1, boolean arg2, Throwable var5, String var6) {
      super(arg0, arg1, arg2);
      this.this$0 = this$0;
      this.val$exc = var5;
      this.val$trace = var6;
      this.setResizable(false);
      this.add(new AWTStatus$2$1(this));
      this.pack();
      this.addWindowListener(new AWTStatus$2$2(this));
      this.setVisible(true);
   }
}
