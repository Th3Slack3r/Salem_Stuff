package haven.launcher;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class AWTStatus$2$1 extends JPanel {
   AWTStatus$2$1(AWTStatus$2 this$1) {
      this.this$1 = this$1;
      this.setLayout(new BoxLayout(this, 3));
      String message = ErrorMessage.getmessage(this.this$1.val$exc);
      this.add(new JLabel(message != null ? message : "An error has occurred!"));
      this.add(new JLabel("If you want to report this, please including the following information:"));
      this.add(new JScrollPane(new AWTStatus$2$1$1(this, 15, 80)));
   }
}
