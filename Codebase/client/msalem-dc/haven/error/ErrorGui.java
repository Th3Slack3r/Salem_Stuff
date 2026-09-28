package haven.error;

import java.awt.BorderLayout;
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
import javax.swing.event.HyperlinkEvent.EventType;

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
      this.setMinimumSize(new Dimension(300, 100));
      this.setResizable(false);
      this.add(new JPanel() {
         {
            this.setLayout(new BoxLayout(this, 3));
            this.add(new JLabel("An error has occurred!"));
            this.add(ErrorGui.this.status = new JLabel("Please wait..."));
            this.add(ErrorGui.this.infoc = new JScrollPane(ErrorGui.this.info = new JEditorPane() {
               {
                  this.setEditable(false);
                  this.addHyperlinkListener(new HyperlinkListener() {
                     @Override
                     public void hyperlinkUpdate(HyperlinkEvent ev) {
                        if (ev.getEventType() == EventType.ACTIVATED) {
                           try {
                              Desktop.getDesktop().browse(ev.getURL().toURI());
                           } catch (Exception var3) {
                              throw new RuntimeException(var3);
                           }
                        } else if (ev.getEventType() == EventType.ENTERED) {
                           setCursor(new Cursor(12));
                        } else if (ev.getEventType() == EventType.EXITED) {
                           setCursor(null);
                        }
                     }
                  });
               }
            }) {
               {
                  this.setPreferredSize(new Dimension(300, 100));
                  this.setVisible(false);
               }
            });
            this.add(new JPanel() {
               {
                  this.setLayout(new FlowLayout());
                  this.setAlignmentX(0.0F);
                  this.add(ErrorGui.this.closebtn = new JButton("Close") {
                     {
                        this.addActionListener(new ActionListener() {
                           @Override
                           public void actionPerformed(ActionEvent ev) {
                              ErrorGui.this.dispose();
                              synchronized (ErrorGui.this) {
                                 ErrorGui.this.done = true;
                                 ErrorGui.this.notifyAll();
                              }
                           }
                        });
                     }
                  });
                  this.add(ErrorGui.this.detbtn = new JButton("Details >>>") {
                     {
                        this.addActionListener(new ActionListener() {
                           @Override
                           public void actionPerformed(ActionEvent ev) {
                              if (ErrorGui.this.details.isVisible()) {
                                 ErrorGui.this.details.setVisible(false);
                                 ErrorGui.this.detbtn.setText("Details >>>");
                              } else {
                                 ErrorGui.this.details.setVisible(true);
                                 ErrorGui.this.detbtn.setText("<<< Details");
                              }

                              ErrorGui.this.pack();
                           }
                        });
                     }
                  });
               }
            });
            this.add(ErrorGui.this.details = new JPanel() {
               {
                  this.setLayout(new BorderLayout());
                  this.setAlignmentX(0.0F);
                  this.setVisible(false);
                  this.add(ErrorGui.this.exboxc = new JScrollPane(ErrorGui.this.exbox = new JTextArea(15, 80) {
                     {
                        this.setEditable(false);
                     }
                  }) {
                     {
                        this.setVisible(true);
                     }
                  });
               }
            });
         }
      });
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent ev) {
            ErrorGui.this.dispose();
            synchronized (ErrorGui.this) {
               ErrorGui.this.done = true;
               ErrorGui.this.notifyAll();
            }

            ErrorGui.this.reporter.interrupt();
         }
      });
      this.pack();
   }

   @Override
   public boolean goterror(Throwable t) {
      this.reporter = Thread.currentThread();
      StringWriter w = new StringWriter();
      t.printStackTrace(new PrintWriter(w));
      final String tr = w.toString();
      SwingUtilities.invokeLater(new Runnable() {
         @Override
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

   @Override
   public void connecting() {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            ErrorGui.this.status.setText("Connecting to server...");
            ErrorGui.this.pack();
         }
      });
   }

   @Override
   public void sending() {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            ErrorGui.this.status.setText("Sending error...");
            ErrorGui.this.pack();
         }
      });
   }

   @Override
   public void done(final String ctype, final String info) {
      this.done = false;
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            ErrorGui.this.closebtn.setEnabled(true);
            if (ctype != null && ctype.equals("text/x-report-info")) {
               ErrorGui.this.status.setText("There is information available about this error:");
               ErrorGui.this.info.setContentType("text/html");
               ErrorGui.this.info.setText(info);
               ErrorGui.this.infoc.setVisible(true);
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     ErrorGui.this.infoc.getVerticalScrollBar().setValue(0);
                  }
               });
            } else {
               ErrorGui.this.status.setText("The error has been reported.");
            }

            ErrorGui.this.pack();
         }
      });
      synchronized (this) {
         try {
            while (!this.done) {
               this.wait();
            }
         } catch (InterruptedException var6) {
            throw new Error(var6);
         }
      }

      this.errorsent();
   }

   @Override
   public void senderror(Exception e) {
      final String errstr;
      if (e instanceof ReportException) {
         StringBuilder buf = new StringBuilder();
         buf.append("<html>");
         String msg = e.getMessage();

         for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '\n') {
               buf.append("<br>");
            } else if (c == '<') {
               buf.append("&lt;");
            } else if (c == '>') {
               buf.append("&gt;");
            } else if (c == '&') {
               buf.append("&amp;");
            } else {
               buf.append(c);
            }
         }

         buf.append("</html>");
         errstr = buf.toString();
      } else {
         e.printStackTrace();
         errstr = "An error occurred while sending!";
      }

      this.done = false;
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            ErrorGui.this.closebtn.setEnabled(true);
            ErrorGui.this.status.setText(errstr);
            ErrorGui.this.pack();
         }
      });
      synchronized (this) {
         try {
            while (!this.done) {
               this.wait();
            }
         } catch (InterruptedException var8) {
            throw new Error(var8);
         }
      }

      this.errorsent();
   }

   public abstract void errorsent();
}
