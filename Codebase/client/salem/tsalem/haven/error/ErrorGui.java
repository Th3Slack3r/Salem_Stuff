package haven.error;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public abstract class ErrorGui extends JDialog implements ErrorStatus {
  private JLabel status;
  
  private JEditorPane info;
  
  private JPanel details;
  
  private JButton closebtn;
  
  private JButton detbtn;
  
  private JTextArea exbox;
  
  private JScrollPane infoc;
  
  private JScrollPane exboxc;
  
  private Thread reporter;
  
  private boolean done;
  
  public ErrorGui(Frame parent) {
    super(parent, "Haven error!", true);
    setMinimumSize(new Dimension(300, 100));
    setResizable(false);
    add(new JPanel() {
        
        });
    addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent ev) {
            ErrorGui.this.dispose();
            synchronized (ErrorGui.this) {
              ErrorGui.this.done = true;
              ErrorGui.this.notifyAll();
            } 
            ErrorGui.this.reporter.interrupt();
          }
        });
    pack();
  }
  
  public boolean goterror(Throwable t) {
    this.reporter = Thread.currentThread();
    StringWriter w = new StringWriter();
    t.printStackTrace(new PrintWriter(w));
    final String tr = w.toString();
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ErrorGui.this.closebtn.setEnabled(false);
            ErrorGui.this.status.setText("Please wait...");
            ErrorGui.this.exbox.setText(tr);
            ErrorGui.this.pack();
            ErrorGui.this.setVisible(true);
          }
        });
    return true;
  }
  
  public void connecting() {
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ErrorGui.this.status.setText("Connecting to server...");
            ErrorGui.this.pack();
          }
        });
  }
  
  public void sending() {
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ErrorGui.this.status.setText("Sending error...");
            ErrorGui.this.pack();
          }
        });
  }
  
  public void done(final String ctype, final String info) {
    this.done = false;
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ErrorGui.this.closebtn.setEnabled(true);
            if (ctype != null && ctype.equals("text/x-report-info")) {
              ErrorGui.this.status.setText("There is information available about this error:");
              ErrorGui.this.info.setContentType("text/html");
              ErrorGui.this.info.setText(info);
              ErrorGui.this.infoc.setVisible(true);
              SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      ErrorGui.this.infoc.getVerticalScrollBar().setValue(0);
                    }
                  });
            } else {
              ErrorGui.this.status.setText("");
            } 
            ErrorGui.this.pack();
          }
        });
    synchronized (this) {
      while (true) {
        try {
          if (!this.done) {
            wait();
            continue;
          } 
        } catch (InterruptedException e) {
          throw new Error(e);
        } 
        break;
      } 
    } 
    errorsent();
  }
  
  public void senderror(Exception e) {}
  
  public abstract void errorsent();
}
