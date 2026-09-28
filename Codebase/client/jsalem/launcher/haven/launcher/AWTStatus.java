package haven.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AWTStatus implements Status {
   private final JFrame frame;
   private boolean subsumed;
   private JPanel imgcont;
   private JPanel progcont;
   private JLabel message;
   private Component image;
   private JProgressBar prog;
   private URI splash;
   private URI icon;
   private static final String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "PB"};
   private long lastsize;
   private long lastpos;
   private long lastupd;

   public AWTStatus() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var2) {
      }

      this.splash = null;
      this.icon = null;
      this.lastsize = -1L;
      this.lastpos = -1L;
      this.lastupd = 0L;
      this.frame = new JFrame("Launcher");
      this.frame.setResizable(false);
      JPanel cont = new JPanel();
      cont.setLayout(new BoxLayout(cont, 3));
      this.imgcont = new JPanel();
      this.imgcont.setLayout(new BoxLayout(this.imgcont, 2));
      this.imgcont.add(this.image = Box.createHorizontalStrut(450));
      cont.add(this.imgcont);
      this.progcont = new JPanel();
      this.progcont.setLayout(new BoxLayout(this.progcont, 2));
      this.progcont.add(this.message = new JLabel("Initializing..."));
      this.progcont.add(Box.createGlue());
      cont.add(this.progcont);
      this.frame.add(cont);
      this.frame.pack();
      SwingUtilities.invokeLater(() -> {
         this.frame.setVisible(true);
         Dimension ssz = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension fsz = this.frame.getSize();
         this.frame.setLocation((ssz.width - fsz.width) / 2, (ssz.height - fsz.height) / 4);
      });
   }

   public JFrame subsume() {
      this.subsumed = true;
      return this.frame;
   }

   @Override
   public void dispose() {
      if (!this.subsumed) {
         this.frame.dispose();
      }
   }

   private void setimage(Path imgpath) throws IOException {
      InputStream fp = Files.newInputStream(imgpath);

      Image img;
      try {
         img = ImageIO.read(fp);
      } catch (Throwable var7) {
         if (fp != null) {
            try {
               fp.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (fp != null) {
         fp.close();
      }

      SwingUtilities.invokeLater(() -> {
         JLabel nimage = new JLabel(new ImageIcon(img));
         this.imgcont.remove(this.image);
         this.imgcont.add(this.image = nimage);
         nimage.setAlignmentX(0.0F);
         this.frame.pack();
      });
   }

   @Override
   public boolean command(String[] argv, Config cfg, Config.Environment env) {
      String var4 = argv[0];
      switch (var4) {
         case "splash-image":
            if (argv.length < 2) {
               throw new RuntimeException("usage: splash-image URL");
            } else {
               Resource res;
               try {
                  res = new Resource(env.rel.resolve(new URI(Config.expand(argv[1], env))), env.val).referrer(env.src);
               } catch (URISyntaxException var13) {
                  throw new RuntimeException("usage: splash-image URL", var13);
               }

               if (!Objects.equals(res.uri, this.splash)) {
                  try {
                     this.setimage(res.update());
                     this.splash = res.uri;
                  } catch (IOException var12) {
                  }
               }

               return true;
            }
         case "icon":
            if (argv.length < 2) {
               throw new RuntimeException("usage: icon URL");
            } else {
               Resource resx;
               try {
                  resx = new Resource(env.rel.resolve(new URI(Config.expand(argv[1], env))), env.val).referrer(env.src);
               } catch (URISyntaxException var11) {
                  throw new RuntimeException("usage: icon URL", var11);
               }

               if (!Objects.equals(resx.uri, this.icon)) {
                  try {
                     InputStream fp = Files.newInputStream(resx.update());

                     try {
                        Image img = ImageIO.read(fp);
                        SwingUtilities.invokeLater(() -> this.frame.setIconImage(img));
                        this.icon = resx.uri;
                     } catch (Throwable var14) {
                        if (fp != null) {
                           try {
                              fp.close();
                           } catch (Throwable var10) {
                              var14.addSuppressed(var10);
                           }
                        }

                        throw var14;
                     }

                     if (fp != null) {
                        fp.close();
                     }
                  } catch (IOException var15) {
                  }
               }

               return true;
            }
         case "title":
            if (argv.length < 2) {
               throw new RuntimeException("usage: title TITLE");
            }

            String title = Config.expand(argv[1], env);
            SwingUtilities.invokeLater(() -> this.frame.setTitle(title));
            return true;
         default:
            return false;
      }
   }

   @Override
   public void message(String text) {
      SwingUtilities.invokeLater(() -> {
         this.message.setText(text);
         if (this.prog != null) {
            this.progcont.remove(this.prog);
            this.prog = null;
         }
      });
   }

   private static String fmtbytes(long amount) {
      int ui = 0;

      long sz;
      for (sz = 1L; ui < units.length - 1 && amount / sz >= 1000L; sz *= 1000L) {
         ui++;
      }

      return String.format("%d %s", (amount + sz / 2L) / sz, units[ui]);
   }

   @Override
   public void transfer(long size, long pos) {
      SwingUtilities.invokeLater(() -> {
         long now = System.currentTimeMillis();
         if (size < 0L) {
            if (this.prog != null) {
               this.progcont.remove(this.prog);
               this.prog = null;
            }
         } else {
            if (this.prog == null) {
               this.lastsize = this.lastpos = -1L;
               this.lastupd = 0L;
               this.progcont.add(this.prog = new JProgressBar());
               this.prog.setMinimumSize(new Dimension(100, 0));
               this.prog.setStringPainted(true);
            }

            if (now - this.lastupd > 100L || pos >= size) {
               this.prog.setMaximum((int)size);
               this.prog.setValue((int)pos);
               this.prog.setString(String.format("%s / %s", fmtbytes(pos), fmtbytes(size)));
               this.lastupd = now;
            }
         }

         this.lastsize = size;
         this.lastpos = pos;
      });
   }

   @Override
   public void progress() {
   }

   public static void error(Throwable exc, Frame owner) {
      boolean[] done = new boolean[]{false};
      StringWriter buf = new StringWriter();
      exc.printStackTrace(new PrintWriter(buf));
      String trace = buf.toString();

      try {
         SwingUtilities.invokeAndWait(() -> {
            JDialog errwnd = new JDialog(owner, "Launcher error!", true);
            errwnd.setResizable(false);
            JPanel cont = new JPanel();
            cont.setLayout(new BoxLayout(cont, 3));
            String message = ErrorMessage.getmessage(exc);
            cont.add(new JLabel(message != null ? message : "An error has occurred!"));
            cont.add(new JLabel("If you want to report this, please including the following information:"));
            JTextArea body = new JTextArea(15, 80);
            body.setEditable(false);
            body.setText(trace);
            cont.add(new JScrollPane(body));
            errwnd.add(cont);
            errwnd.pack();
            errwnd.addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent ev) {
                  synchronized (done) {
                     done[0] = true;
                     done.notifyAll();
                  }
               }
            });
            errwnd.setVisible(true);
         });
         synchronized (done) {
            while (!done[0]) {
               done.wait();
            }
         }
      } catch (InterruptedException var8) {
      } catch (InvocationTargetException var9) {
         throw new RuntimeException(var9);
      }
   }

   @Override
   public void error(Throwable exc) {
      error(exc, this.frame);
   }
}
