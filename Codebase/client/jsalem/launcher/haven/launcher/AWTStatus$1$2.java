package haven.launcher;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

class AWTStatus$1$2 extends JPanel {
   AWTStatus$1$2(<unrepresentable> this$1) {
      this.this$1 = this$1;
      this.setLayout(new BoxLayout(this, 2));
      this.add(AWTStatus.access$302(this.this$1.this$0, new JLabel("Initializing...")));
      this.add(Box.createGlue());
   }
}
