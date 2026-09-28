package haven.launcher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class AWTStatus$2$2 extends WindowAdapter {
   AWTStatus$2$2(AWTStatus$2 this$1) {
      this.this$1 = this$1;
   }

   @Override
   public void windowClosing(WindowEvent ev) {
      synchronized (this.this$1.this$0) {
         AWTStatus.access$402(this.this$1.this$0, true);
         this.this$1.this$0.notifyAll();
      }
   }
}
