package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class ConsoleHost extends Widget {
   public static Text.Foundry cmdfoundry = new Text.Foundry(new Font("SansSerif", 1, 14), new Color(0, 192, 192));
   LineEdit cmdline = null;
   private Text.Line cmdtext = null;
   private String cmdtextf = null;
   private List<String> history = new ArrayList<>();
   private int hpos = this.history.size();
   private String hcurrent;

   public ConsoleHost(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
   }

   public ConsoleHost(UI ui, Coord c, Coord sz) {
      super(ui, c, sz);
   }

   public void drawcmd(GOut g, Coord c) {
      if (this.cmdline != null) {
         if (this.cmdtext == null || this.cmdtextf != this.cmdline.line) {
            this.cmdtext = cmdfoundry.render(":" + (this.cmdtextf = this.cmdline.line));
         }

         g.image(this.cmdtext.tex(), c);
         int lx = this.cmdtext.advance(this.cmdline.point + 1);
         g.line(c.add(lx + 1, 2), c.add(lx + 1, 14), 1.0);
      }
   }

   public void entercmd() {
      this.ui.grabkeys(this);
      this.hpos = this.history.size();
      this.cmdline = new ConsoleHost.CommandLine();
   }

   @Override
   public boolean type(char ch, KeyEvent ev) {
      if (this.cmdline == null) {
         return super.type(ch, ev);
      } else {
         this.cmdline.key(ev);
         return true;
      }
   }

   @Override
   public boolean keydown(KeyEvent ev) {
      if (this.cmdline != null) {
         this.cmdline.key(ev);
         return true;
      } else {
         return super.keydown(ev);
      }
   }

   public abstract void error(String var1);

   private class CommandLine extends LineEdit {
      private CommandLine() {
      }

      private CommandLine(String line) {
         super(line);
      }

      private void cancel() {
         ConsoleHost.this.cmdline = null;
         ConsoleHost.this.ui.grabkeys(null);
      }

      @Override
      protected void done(String line) {
         ConsoleHost.this.history.add(line);

         try {
            ConsoleHost.this.ui.cons.run(line);
         } catch (Exception var4) {
            String msg = var4.getMessage();
            if (msg == null) {
               msg = var4.toString();
            }

            ConsoleHost.this.ui.cons.out.println(msg);
            ConsoleHost.this.error(msg);
         }

         this.cancel();
      }

      @Override
      public boolean key(char c, int code, int mod) {
         if (c == 27) {
            this.cancel();
         } else if (c == '\b' && mod == 0 && this.line.length() == 0 && this.point == 0) {
            this.cancel();
         } else if (code == 38) {
            if (ConsoleHost.this.hpos > 0) {
               if (ConsoleHost.this.hpos == ConsoleHost.this.history.size()) {
                  ConsoleHost.this.hcurrent = this.line;
               }

               ConsoleHost.this.cmdline = ConsoleHost.this.new CommandLine(ConsoleHost.this.history.get(--ConsoleHost.this.hpos));
            }
         } else {
            if (code != 40) {
               return super.key(c, code, mod);
            }

            if (ConsoleHost.this.hpos < ConsoleHost.this.history.size()) {
               if (++ConsoleHost.this.hpos == ConsoleHost.this.history.size()) {
                  ConsoleHost.this.cmdline = ConsoleHost.this.new CommandLine(ConsoleHost.this.hcurrent);
               } else {
                  ConsoleHost.this.cmdline = ConsoleHost.this.new CommandLine(ConsoleHost.this.history.get(ConsoleHost.this.hpos));
               }
            }
         }

         return true;
      }
   }
}
