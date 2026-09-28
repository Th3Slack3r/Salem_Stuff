package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUI extends Widget {
   public static RichText.Foundry fnd = new RichText.Foundry(
      new ChatUI.ChatParser(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Color.BLACK)
   );
   public static Text.Foundry qfnd = new Text.Foundry(new Font("SansSerif", 0, 14), new Color(192, 255, 192));
   public static int selw = 100;
   public ChatUI.Channel sel = null;
   private final ChatUI.Selector chansel;
   private Coord base;
   private static int basesize = 12;
   private ChatUI.QuickLine qline = null;
   private final LinkedList<ChatUI.Notification> notifs = new LinkedList<>();
   private static final Pattern tags_patt = Pattern.compile("\\$(hl)\\[([^\\[\\]]*)\\]");
   private Text.Line rqline = null;
   private int rqpre;
   public static final Resource notifsfx = Resource.load("sfx/tick");

   public ChatUI(Coord c, int w, Widget parent) {
      super(c.add(0, -50), new Coord(w, 50), parent);
      this.chansel = new ChatUI.Selector(Coord.z, new Coord(selw, this.sz.y));
      this.chansel.hide();
      this.base = c;
      this.setfocusctl(true);
      this.setcanfocus(false);
      this.setbasesize((int)Utils.getpreff("chatfontsize", 12.0F));
      this.update_visibility();
   }

   public final void setbasesize(int basesize) {
      fnd = new RichText.Foundry(new ChatUI.ChatParser(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, basesize, TextAttribute.FOREGROUND, Color.BLACK));
      qfnd = new Text.Foundry(new Font("SansSerif", 0, basesize + 2), new Color(192, 255, 192));
      selw = 100 + (basesize - 12) * 6;
      ChatUI.TextEntryChannel.efnd = new Text.Foundry(new Font("SansSerif", 0, basesize), Color.BLACK);
      this.c = this.base.add(0, -100 - (basesize - 12) * 24);
      this.resize(new Coord(this.sz.x, 100 + (basesize - 12) * 24));
      this.chansel.nf = new Text.Foundry("SansSerif", basesize);
      this.chansel.nfu = new Text.Foundry("SansSerif", basesize + 2, 1);
      this.chansel.ymod = (basesize - 12) * 3;
      this.chansel.rerender = true;
      ChatUI.basesize = basesize;
   }

   public static boolean hasTags(String text) {
      return tags_patt.matcher(text).find();
   }

   private static Color lighter(Color col) {
      int[] hsl = new int[3];
      Utils.rgb2hsl(col.getRed(), col.getGreen(), col.getBlue(), hsl);
      hsl[1] = Math.round(0.7F * hsl[1]);
      hsl[2] = 100;
      int[] rgb = Utils.hsl2rgb(hsl);
      return new Color(rgb[0], rgb[1], rgb[2]);
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      return gettype(type).create(Coord.z, this, cargs);
   }

   public void select(ChatUI.Channel chan) {
      ChatUI.Channel prev = this.sel;
      this.sel = chan;
      if (Config.chat_expanded) {
         if (prev != null) {
            prev.hide();
         }

         this.sel.show();
         this.resize(this.sz);
      }
   }

   public void drawsmall(GOut g, Coord br, int h) {
      Coord c;
      if (this.qline != null) {
         if (this.rqline == null || !this.rqline.text.equals(this.qline.line)) {
            String pre = String.format("%s> ", this.qline.chan.name());
            this.rqline = qfnd.render(pre + this.qline.line);
            this.rqpre = pre.length();
         }

         c = br.sub(0, 8 + basesize);
         g.chcolor(24, 24, 16, 200);
         g.frect(c, this.rqline.tex().sz());
         g.chcolor();
         g.image(this.rqline.tex(), c);
         int lx = this.rqline.advance(this.qline.point + this.rqpre);
         g.line(new Coord(br.x + lx + 1, br.y - 18), new Coord(br.x + lx + 1, br.y - 6), 1.0);
      } else {
         c = br.sub(0, 5);
      }

      long now = System.currentTimeMillis();
      synchronized (this.notifs) {
         Iterator<ChatUI.Notification> i = this.notifs.iterator();

         while (i.hasNext()) {
            ChatUI.Notification n = i.next();
            if (now - n.time > 5000L) {
               i.remove();
            } else {
               if ((c.y = c.y - n.msg.sz().y) < br.y - h) {
                  break;
               }

               g.chcolor(24, 24, 16, 200);
               g.frect(c, n.chnm.tex().sz().add(n.msg.tex().sz().x + selw, 0));
               g.chcolor();
               g.image(n.chnm.tex(), c, br.sub(0, h), br.add(selw - 10, 0));
               g.image(n.msg.tex(), c.add(selw, 0));
            }
         }
      }
   }

   public void notify(ChatUI.Channel chan, ChatUI.Channel.Message msg) {
      synchronized (this.notifs) {
         this.notifs.addFirst(new ChatUI.Notification(chan, msg));
      }

      Audio.play(notifsfx);
   }

   @Override
   public void newchild(Widget w) {
      if (w instanceof ChatUI.Channel) {
         ChatUI.Channel chan = (ChatUI.Channel)w;
         this.select(chan);
         this.chansel.add(chan);
         if (!Config.chat_expanded) {
            chan.hide();
         }
      }
   }

   @Override
   public void cdestroy(Widget w) {
      if (w instanceof ChatUI.Channel) {
         ChatUI.Channel chan = (ChatUI.Channel)w;
         if (chan == this.sel) {
            this.sel = null;
         }

         this.chansel.rm(chan);
      }
   }

   @Override
   public void resize(Coord sz) {
      super.resize(sz);
      this.c = this.base.add(0, -this.sz.y);
      this.chansel.resize(new Coord(selw, this.sz.y));
      if (this.sel != null) {
         this.sel.resize(new Coord(this.sz.x - selw, this.sz.y));
      }
   }

   public void resize(int w) {
      this.resize(new Coord(w, this.sz.y));
   }

   public void move(Coord base) {
      this.c = (this.base = base).add(0, -this.sz.y);
   }

   public void expand() {
      if (!Config.chat_expanded) {
         this.resize(new Coord(this.sz.x, 100 + (basesize - 12) * 24));
         Config.chat_expanded = true;
         Utils.setprefb("chat_expanded", true);
         this.update_visibility();
      }
   }

   public void contract() {
      if (Config.chat_expanded) {
         this.resize(new Coord(this.sz.x, 50));
         Config.chat_expanded = false;
         Utils.setprefb("chat_expanded", false);
         this.update_visibility();
      }
   }

   public void update_visibility() {
      if (Config.chat_expanded) {
         this.setcanfocus(true);
         if (this.sel != null) {
            this.sel.show();
         }

         this.chansel.show();
      } else {
         this.setcanfocus(false);
         if (this.sel != null) {
            this.sel.hide();
         }

         this.chansel.hide();
      }
   }

   @Override
   public boolean keydown(KeyEvent ev) {
      boolean M = (ev.getModifiersEx() & 768) != 0;
      if (this.qline == null) {
         if (M && ev.getKeyCode() == 38) {
            this.chansel.up();
            return true;
         } else if (M && ev.getKeyCode() == 40) {
            this.chansel.down();
            return true;
         } else {
            return super.keydown(ev);
         }
      } else if (M && ev.getKeyCode() == 38) {
         ChatUI.Channel prev = this.sel;

         while (this.chansel.up() && !(this.sel instanceof ChatUI.EntryChannel)) {
         }

         if (!(this.sel instanceof ChatUI.EntryChannel)) {
            this.select(prev);
            return true;
         } else {
            this.qline = new ChatUI.QuickLine((ChatUI.EntryChannel)this.sel);
            return true;
         }
      } else if (M && ev.getKeyCode() == 40) {
         ChatUI.Channel prev = this.sel;

         while (this.chansel.down() && !(this.sel instanceof ChatUI.EntryChannel)) {
         }

         if (!(this.sel instanceof ChatUI.EntryChannel)) {
            this.select(prev);
            return true;
         } else {
            this.qline = new ChatUI.QuickLine((ChatUI.EntryChannel)this.sel);
            return true;
         }
      } else {
         this.qline.key(ev);
         return true;
      }
   }

   public void toggle() {
      if (!Config.chat_expanded) {
         this.expand();
         this.parent.setfocus(this);
      } else if (this.hasfocus) {
         if (this.sz.y == 100 + (basesize - 12) * 24) {
            this.resize(new Coord(this.sz.x, 300 + (basesize - 12) * 48));
         } else {
            this.contract();
         }
      } else {
         this.parent.setfocus(this);
      }
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (this.qline != null) {
         this.qline.key(ev);
         return true;
      } else {
         return super.type(key, ev);
      }
   }

   @Override
   public boolean globtype(char key, KeyEvent ev) {
      if (key == '\n' && !Config.chat_expanded && this.sel instanceof ChatUI.EntryChannel) {
         this.ui.grabkeys(this);
         this.qline = new ChatUI.QuickLine((ChatUI.EntryChannel)this.sel);
         return true;
      } else {
         return super.globtype(key, ev);
      }
   }

   private static String parseTags(String text) {
      try {
         Matcher m = tags_patt.matcher(text);

         while (m.find()) {
            String tag = m.group(1);
            String val = m.group(2);
            if (tag.equals("hl")) {
               try {
                  long id = Long.parseLong(val);
                  Gob gob = UI.instance.sess.glob.oc.getgob(id);
                  if (gob != null) {
                     gob.setattr(new GobHighlight(gob));
                     ResDrawable d = gob.getattr(ResDrawable.class);
                     if (d != null) {
                        try {
                           UI.instance.gui.message("Highlighted object: " + d.res.get().name, GameUI.MsgType.INFO);
                        } catch (Exception var9) {
                        }
                     }
                  }
               } catch (NumberFormatException var10) {
               }

               return null;
            }
         }
      } catch (Exception var11) {
      }

      return text;
   }

   @Widget.RName("mchat")
   public static class $MChat implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String name = (String)args[0];
         boolean notify = (Integer)args[1] != 0;
         return new ChatUI.MultiChat(parent, name, notify);
      }
   }

   @Widget.RName("pchat")
   public static class $PChat implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new ChatUI.PartyChat(parent);
      }
   }

   @Widget.RName("pmchat")
   public static class $PMChat implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         int other = (Integer)args[0];
         return new ChatUI.PrivChat(parent, other);
      }
   }

   @Widget.RName("schan")
   public static class $SChan implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String name = (String)args[0];
         return new ChatUI.SimpleChat(parent, name);
      }
   }

   public abstract static class Channel extends Widget {
      public final List<ChatUI.Channel.Message> msgs = new LinkedList<ChatUI.Channel.Message>() {
         public boolean add(ChatUI.Channel.Message message) {
            if (this.size() >= 200) {
               this.removeFirst().tex().dispose();
            }

            return super.add(message);
         }
      };
      private final Scrollbar sb;
      private FileOutputStream fos = null;
      public IButton cbtn;
      protected boolean read = true;
      public final Comparator<ChatUI.Channel.CharPos> poscmp = new Comparator<ChatUI.Channel.CharPos>() {
         public int compare(ChatUI.Channel.CharPos a, ChatUI.Channel.CharPos b) {
            if (a.msg != b.msg) {
               synchronized (Channel.this.msgs) {
                  for (ChatUI.Channel.Message msg : Channel.this.msgs) {
                     if (msg == a.msg) {
                        return -1;
                     }

                     if (msg == b.msg) {
                        return 1;
                     }
                  }
               }

               throw new IllegalStateException("CharPos message is no longer contained in the log");
            } else if (a.part != b.part) {
               RichText.Part part = ((RichText)a.msg.text()).parts;
               if (part != null) {
                  return part == a.part ? -1 : 1;
               } else {
                  throw new IllegalStateException("CharPos is no longer contained in the log");
               }
            } else {
               return a.ch.getInsertionIndex() - b.ch.getInsertionIndex();
            }
         }
      };
      private ChatUI.Channel.CharPos selorig;
      private ChatUI.Channel.CharPos lasthit;
      private ChatUI.Channel.CharPos selstart;
      private ChatUI.Channel.CharPos selend;
      private boolean dragging;

      @Override
      public void show() {
         super.show();
         this.read = true;
      }

      public Channel(Coord c, Coord sz, Widget parent, boolean closeable) {
         super(c, sz, parent);
         this.sb = new Scrollbar(new Coord(sz.x, 0), this.ih(), this, 0, -this.ih());
         if (closeable) {
            this.cbtn = new IButton(Coord.z, this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]);
            this.cbtn.recthit = true;
            this.cbtn.c = new Coord(sz.x - this.cbtn.sz.x - this.sb.sz.x - 3, 0);
         }
      }

      public Channel(Widget parent, boolean closeable) {
         this(new Coord(ChatUI.selw, 0), parent.sz.sub(ChatUI.selw, 0), parent, closeable);
      }

      protected final void startLogging() {
         if (Config.chatlogs) {
            try {
               String fixed_char_name = this.name().replaceAll("[\\/:*?\"<>|]", "");
               String path = String.format("%s/logs/%s/%s/", Config.userhome, Config.currentCharName, fixed_char_name);
               String filename = String.format("%s.txt", Utils.current_date());
               File pathf = new File(path);
               if (!pathf.mkdirs() && !pathf.isDirectory()) {
                  throw new FileNotFoundException("Failed to create parent directories!");
               }

               this.fos = new FileOutputStream(path + filename);
            } catch (FileNotFoundException var5) {
               this.ui.message("Could not open file for chat logging!", GameUI.MsgType.INFO);
            }
         }
      }

      public void append(ChatUI.Channel.Message msg, boolean attn) {
         synchronized (this.msgs) {
            this.msgs.add(msg);
            int y = 0;

            for (ChatUI.Channel.Message m : this.msgs) {
               y += m.sz().y;
            }

            boolean b = this.sb.val >= this.sb.max;
            this.sb.max = y - this.ih();
            if (b) {
               this.sb.val = this.sb.max;
            }

            if (this.fos != null) {
               try {
                  this.fos.write((msg.text().text + "\n").getBytes());
               } catch (IOException var8) {
                  Logger.getLogger(ChatUI.class.getName()).log(Level.SEVERE, null, var8);
               }
            }

            if (attn && !this.visible) {
               this.read = false;
            }
         }

         if (Config.headless) {
            System.out.println("[" + this.name() + "]" + msg.text().text);
         }
      }

      public void append(String line, Color col) {
         this.append(new ChatUI.Channel.SimpleMessage(line, col, this.iw()), false);
      }

      public int iw() {
         return this.sz.x - this.sb.sz.x;
      }

      public int ih() {
         return this.sz.y;
      }

      @Override
      public void draw(GOut g) {
         g.chcolor(24, 24, 16, 200);
         g.frect(Coord.z, this.sz);
         g.chcolor();
         int y = 0;
         boolean sel = false;
         synchronized (this.msgs) {
            for (ChatUI.Channel.Message msg : this.msgs) {
               if (this.selstart != null && msg == this.selstart.msg) {
                  sel = true;
               }

               int y1 = y - this.sb.val - (ChatUI.basesize - 10);
               int y2 = y1 + msg.sz().y;
               if (y2 > 0 && y1 < this.ih()) {
                  if (sel) {
                     this.drawsel(g, msg, y1);
                  }

                  g.image(msg.tex(), new Coord(0, y1));
               }

               if (this.selend != null && msg == this.selend.msg) {
                  sel = false;
               }

               y += msg.sz().y;
            }
         }

         this.sb.max = y - this.ih();
         super.draw(g);
      }

      @Override
      public boolean mousewheel(Coord c, int amount) {
         this.sb.ch(amount * 15);
         return true;
      }

      @Override
      public void resize(Coord sz) {
         super.resize(sz);
         if (this.sb != null) {
            this.sb.resize(this.ih());
            this.sb.move(new Coord(sz.x, 0));
            int y = 0;

            for (ChatUI.Channel.Message m : this.msgs) {
               y += m.sz().y;
            }

            boolean b = this.sb.val >= this.sb.max;
            this.sb.max = y - this.ih();
            if (b) {
               this.sb.val = this.sb.max;
            }
         }

         if (this.cbtn != null) {
            this.cbtn.c = new Coord(sz.x - this.cbtn.sz.x - (this.sb != null ? this.sb.sz.x : 0) - 3, 0);
         }
      }

      public void notify(ChatUI.Channel.Message msg) {
         this.getparent(ChatUI.class).notify(this, msg);
      }

      public ChatUI.Channel.Message messageat(Coord c, Coord hc) {
         int y = -this.sb.val;
         synchronized (this.msgs) {
            for (ChatUI.Channel.Message msg : this.msgs) {
               Coord sz = msg.sz();
               if (c.y >= y && c.y < y + sz.y) {
                  if (hc != null) {
                     hc.x = c.x;
                     hc.y = c.y - y;
                  }

                  return msg;
               }

               y += sz.y;
            }

            return null;
         }
      }

      public ChatUI.Channel.CharPos charat(Coord c) {
         if (c.y >= -this.sb.val) {
            Coord hc = new Coord();
            ChatUI.Channel.Message msg = this.messageat(c, hc);
            if (msg != null && msg.text() instanceof RichText) {
               RichText rt = (RichText)msg.text();
               RichText.Part p = rt.partat(hc);
               if (p == null) {
                  RichText.TextPart lp = null;

                  for (RichText.Part part = ((RichText)msg.text()).parts; part != null; part = part.next) {
                     if (part instanceof RichText.TextPart) {
                        lp = (RichText.TextPart)part;
                     }
                  }

                  return lp == null ? null : new ChatUI.Channel.CharPos(msg, lp, TextHitInfo.trailing(lp.end - lp.start - 1));
               } else if (!(p instanceof RichText.TextPart)) {
                  return null;
               } else {
                  RichText.TextPart tp = (RichText.TextPart)p;
                  return new ChatUI.Channel.CharPos(msg, tp, tp.charat(hc));
               }
            } else {
               return null;
            }
         } else if (this.msgs.size() < 1) {
            return null;
         } else {
            ChatUI.Channel.Message msg = this.msgs.get(0);
            if (!(msg.text() instanceof RichText)) {
               return null;
            } else {
               RichText.TextPart fp = null;

               for (RichText.Part partx = ((RichText)msg.text()).parts; partx != null; partx = partx.next) {
                  if (partx instanceof RichText.TextPart) {
                     fp = (RichText.TextPart)partx;
                     break;
                  }
               }

               return fp == null ? null : new ChatUI.Channel.CharPos(msg, fp, TextHitInfo.leading(0));
            }
         }
      }

      @Override
      public boolean mousedown(Coord c, int btn) {
         if (super.mousedown(c, btn)) {
            return true;
         } else if (btn == 1) {
            this.selstart = this.selend = null;
            ChatUI.Channel.CharPos ch = this.charat(c);
            if (ch != null) {
               this.selorig = this.lasthit = ch;
               this.dragging = false;
               this.ui.grabmouse(this);
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public void mousemove(Coord c) {
         if (this.selorig != null) {
            ChatUI.Channel.CharPos ch = this.charat(c);
            if (ch != null && !ch.equals(this.lasthit)) {
               this.lasthit = ch;
               if (!this.dragging && !ch.equals(this.selorig)) {
                  this.dragging = true;
               }

               int o = this.poscmp.compare(this.selorig, ch);
               if (o < 0) {
                  this.selstart = this.selorig;
                  this.selend = ch;
               } else if (o > 0) {
                  this.selstart = ch;
                  this.selend = this.selorig;
               } else {
                  this.selstart = this.selend = null;
               }
            }
         } else {
            super.mousemove(c);
         }
      }

      protected void selected(ChatUI.Channel.CharPos start, ChatUI.Channel.CharPos end) {
         StringBuilder buf = new StringBuilder();
         synchronized (this.msgs) {
            boolean sel = false;

            for (ChatUI.Channel.Message msg : this.msgs) {
               if (msg.text() instanceof RichText) {
                  RichText rt = (RichText)msg.text();
                  RichText.Part part = null;
                  if (sel) {
                     part = rt.parts;
                  } else if (msg == start.msg) {
                     sel = true;
                     part = rt.parts;

                     while (part != null && part != start.part) {
                        part = part.next;
                     }
                  }

                  if (sel) {
                     for (; part != null; part = part.next) {
                        if (part instanceof RichText.TextPart) {
                           RichText.TextPart tp = (RichText.TextPart)part;
                           CharacterIterator iter = tp.ti();
                           int sch;
                           if (tp == start.part) {
                              sch = tp.start + start.ch.getInsertionIndex();
                           } else {
                              sch = tp.start;
                           }

                           int ech;
                           if (tp == end.part) {
                              ech = tp.start + end.ch.getInsertionIndex();
                           } else {
                              ech = tp.end;
                           }

                           for (int i = sch; i < ech; i++) {
                              buf.append(iter.setIndex(i));
                           }

                           if (part == end.part) {
                              sel = false;
                              break;
                           }

                           buf.append(' ');
                        }
                     }

                     if (sel) {
                        buf.append('\n');
                     }
                  }

                  if (msg == end.msg) {
                     break;
                  }
               }
            }
         }

         Clipboard cl;
         if ((cl = Toolkit.getDefaultToolkit().getSystemSelection()) == null) {
            cl = Toolkit.getDefaultToolkit().getSystemClipboard();
         }

         try {
            final ChatUI.Channel.CharPos ownsel = this.selstart;
            cl.setContents(new StringSelection(buf.toString()), new ClipboardOwner() {
               @Override
               public void lostOwnership(Clipboard cl, Transferable tr) {
                  if (Channel.this.selstart == ownsel) {
                     Channel.this.selstart = Channel.this.selend = null;
                  }
               }
            });
         } catch (IllegalStateException var16) {
         }
      }

      protected void clicked(ChatUI.Channel.CharPos pos) {
         AttributedCharacterIterator inf = pos.part.ti();
         if (pos.ch.getCharIndex() >= inf.getBeginIndex() && pos.ch.getCharIndex() < inf.getEndIndex()) {
            inf.setIndex(pos.ch.getCharIndex());
            ChatUI.FuckMeGentlyWithAChainsaw url = (ChatUI.FuckMeGentlyWithAChainsaw)inf.getAttribute(ChatUI.ChatAttribute.HYPERLINK);
            if (url != null && WebBrowser.self != null) {
               try {
                  WebBrowser.self.show(url.url);
               } catch (WebBrowser.BrowserException var5) {
                  this.getparent(GameUI.class).error("Could not launch web browser.");
               }
            }
         }
      }

      @Override
      public boolean mouseup(Coord c, int btn) {
         if (btn == 1 && this.selorig != null) {
            if (this.selstart != null) {
               this.selected(this.selstart, this.selend);
            } else {
               this.clicked(this.selorig);
            }

            this.ui.grabmouse(null);
            this.selorig = null;
            this.dragging = false;
         }

         return super.mouseup(c, btn);
      }

      public void select() {
         this.getparent(ChatUI.class).select(this);
      }

      public void display() {
         this.select();
         ChatUI chat = this.getparent(ChatUI.class);
         chat.expand();
         chat.parent.setfocus(chat);
      }

      private void drawsel(GOut g, ChatUI.Channel.Message msg, int y) {
         RichText rt = (RichText)msg.text();
         boolean sel = msg != this.selstart.msg;

         for (RichText.Part part = rt.parts; part != null; part = part.next) {
            if (part instanceof RichText.TextPart) {
               RichText.TextPart tp = (RichText.TextPart)part;
               if (tp.start != tp.end) {
                  TextHitInfo a;
                  if (sel) {
                     a = TextHitInfo.leading(0);
                  } else {
                     if (tp != this.selstart.part) {
                        continue;
                     }

                     a = this.selstart.ch;
                     sel = true;
                  }

                  TextHitInfo b;
                  if (tp == this.selend.part) {
                     sel = false;
                     b = this.selend.ch;
                  } else {
                     b = TextHitInfo.trailing(tp.end - tp.start - 1);
                  }

                  Coord ul = new Coord(tp.x + (int)tp.advance(0, a.getInsertionIndex()), tp.y + y);
                  Coord sz = new Coord((int)tp.advance(a.getInsertionIndex(), b.getInsertionIndex()), tp.height());
                  g.chcolor(0, 0, 255, 255);
                  g.frect(ul, sz);
                  g.chcolor();
                  if (!sel) {
                     break;
                  }
               }
            }
         }
      }

      @Override
      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (sender == this.cbtn) {
            this.wdgmsg("close", new Object[0]);
            if (this.fos != null) {
               try {
                  this.fos.close();
               } catch (IOException var5) {
                  Logger.getLogger(ChatUI.class.getName()).log(Level.SEVERE, null, var5);
               }
            }
         } else {
            super.wdgmsg(sender, msg, args);
         }
      }

      @Override
      public void uimsg(String name, Object... args) {
         if (name == "sel") {
            this.select();
         } else if (name == "dsp") {
            this.display();
         } else {
            super.uimsg(name, args);
         }
      }

      public abstract String name();

      public static class CharPos {
         public final ChatUI.Channel.Message msg;
         public final RichText.TextPart part;
         public final TextHitInfo ch;

         public CharPos(ChatUI.Channel.Message msg, RichText.TextPart part, TextHitInfo ch) {
            this.msg = msg;
            this.part = part;
            this.ch = ch;
         }

         @Override
         public boolean equals(Object oo) {
            if (!(oo instanceof ChatUI.Channel.CharPos)) {
               return false;
            } else {
               ChatUI.Channel.CharPos o = (ChatUI.Channel.CharPos)oo;
               return o.msg == this.msg && o.part == this.part && o.ch.equals(this.ch);
            }
         }
      }

      public abstract static class Message {
         public final long time = System.currentTimeMillis();

         public abstract Text text();

         public abstract Tex tex();

         public abstract Coord sz();
      }

      public static class SimpleMessage extends ChatUI.Channel.Message {
         private final Text t;

         public SimpleMessage(String text, Color col, int w) {
            if (Config.timestamp) {
               text = Utils.timestamp(text);
            }

            if (col == null) {
               this.t = ChatUI.fnd.render(RichText.Parser.quote(text), w);
            } else {
               this.t = ChatUI.fnd.render(RichText.Parser.quote(text), w, TextAttribute.FOREGROUND, col);
            }
         }

         @Override
         public Text text() {
            return this.t;
         }

         @Override
         public Tex tex() {
            return this.t.tex();
         }

         @Override
         public Coord sz() {
            return this.t.sz();
         }
      }
   }

   public static class ChatAttribute extends Attribute {
      public static final Attribute HYPERLINK = new ChatUI.ChatAttribute("hyperlink");

      private ChatAttribute(String name) {
         super(name);
      }
   }

   public static class ChatParser extends RichText.Parser {
      public static final Pattern urlpat = Pattern.compile("\\b((https?://)|(www\\.[a-z0-9_.-]+\\.[a-z0-9_.-]+))[a-z0-9/_.~#%+?&:*=-]*", 2);
      public static final Map<? extends Attribute, ?> urlstyle = RichText.fillattrs(
         TextAttribute.FOREGROUND,
         new Color(64, 175, 255),
         TextAttribute.UNDERLINE,
         TextAttribute.UNDERLINE_ON,
         TextAttribute.WEIGHT,
         TextAttribute.WEIGHT_BOLD
      );

      public ChatParser(Object... args) {
         super(args);
      }

      @Override
      protected RichText.Part text(RichText.Parser.PState s, String text, Map<? extends Attribute, ?> attrs) throws IOException {
         RichText.Part ret = null;
         int p = 0;

         while (true) {
            Matcher m;
            URL url;
            while (true) {
               m = urlpat.matcher(text);
               if (!m.find(p)) {
                  if (ret == null) {
                     ret = new RichText.TextPart(text, attrs);
                  } else {
                     ret.append(new RichText.TextPart(text.substring(p), attrs));
                  }

                  return ret;
               }

               try {
                  String su = text.substring(m.start(), m.end());
                  if (su.indexOf(58) < 0) {
                     su = "http://" + su;
                  }

                  url = new URL(su);
                  break;
               } catch (MalformedURLException var10) {
                  p = m.end();
               }
            }

            RichText.Part lead = new RichText.TextPart(text.substring(0, m.start()), attrs);
            if (ret == null) {
               ret = lead;
            } else {
               ret.append(lead);
            }

            Map<Attribute, Object> na = new HashMap<>((Map<? extends Attribute, ? extends Object>)attrs);
            na.putAll((Map<? extends Attribute, ? extends Object>)urlstyle);
            na.put(ChatUI.ChatAttribute.HYPERLINK, new ChatUI.FuckMeGentlyWithAChainsaw(url));
            ret.append(new RichText.TextPart(text.substring(m.start(), m.end()), na));
            p = m.end();
         }
      }
   }

   public abstract static class EntryChannel extends ChatUI.Channel {
      private final TextEntry in;
      private List<String> history = new ArrayList<>();
      private int hpos = 0;
      private String hcurrent;

      public EntryChannel(Widget parent) {
         super(parent, true);
         this.setfocusctl(true);
         int height = ChatUI.basesize + 8;
         this.in = new ChatUI.TextEntryChannel(new Coord(0, this.sz.y - height), new Coord(this.sz.x, height), this, "") {
            @Override
            public void activate(String text) {
               if (text.length() > 0) {
                  EntryChannel.this.send(text);
               }

               this.settext("");
               EntryChannel.this.hpos = EntryChannel.this.history.size();
            }

            @Override
            public boolean keydown(KeyEvent ev) {
               if (ev.getKeyCode() == 38) {
                  if (EntryChannel.this.hpos > 0) {
                     if (EntryChannel.this.hpos == EntryChannel.this.history.size()) {
                        EntryChannel.this.hcurrent = this.text;
                     }

                     this.rsettext(EntryChannel.this.history.get(--EntryChannel.this.hpos));
                  }

                  return true;
               } else if (ev.getKeyCode() == 40) {
                  if (EntryChannel.this.hpos < EntryChannel.this.history.size()) {
                     if (++EntryChannel.this.hpos == EntryChannel.this.history.size()) {
                        this.rsettext(EntryChannel.this.hcurrent);
                     } else {
                        this.rsettext(EntryChannel.this.history.get(EntryChannel.this.hpos));
                     }
                  }

                  return true;
               } else {
                  return super.keydown(ev);
               }
            }
         };
      }

      @Override
      public int ih() {
         return this.sz.y - 20;
      }

      @Override
      public void resize(Coord sz) {
         super.resize(sz);
         if (this.in != null) {
            int height = ChatUI.basesize + 8;
            this.in.c = new Coord(0, this.sz.y - height);
            this.in.resize(new Coord(this.sz.x, height));
         }
      }

      public void send(String text) {
         this.history.add(text);
         this.wdgmsg("msg", new Object[]{text});
      }
   }

   public static class FuckMeGentlyWithAChainsaw {
      public final URL url;

      public FuckMeGentlyWithAChainsaw(URL url) {
         this.url = url;
      }
   }

   public static class Log extends ChatUI.Channel {
      private final String name;

      public Log(Widget parent, String name) {
         super(parent, false);
         this.name = name;
      }

      @Override
      public String name() {
         return this.name;
      }
   }

   public static class MultiChat extends ChatUI.EntryChannel {
      private final String name;
      private final boolean notify;
      private final Map<Integer, Color> pc = new HashMap<>();
      private static final Random cr = new Random();

      public MultiChat(Widget parent, String name, boolean notify) {
         super(parent);
         this.name = name;
         this.notify = notify;
         super.startLogging();
      }

      private static Color randcol() {
         int[] c = new int[]{cr.nextInt(256), cr.nextInt(256), cr.nextInt(256)};
         int mc = Math.max(c[0], Math.max(c[1], c[2]));
         if (c[2] > c[0]) {
            int t = c[0];
            c[0] = c[2];
            c[2] = t;
         }

         for (int i = 0; i < c.length; i++) {
            c[i] = c[i] * 255 / mc;
         }

         return new Color(c[0], c[1], c[2]);
      }

      public Color fromcolor(int from) {
         synchronized (this.pc) {
            Color c = this.pc.get(from);
            if (c == null) {
               this.pc.put(from, c = randcol());
            }

            return c;
         }
      }

      @Override
      public void uimsg(String msg, Object... args) {
         if (msg == "msg") {
            Integer from = (Integer)args[0];
            String line = ChatUI.parseTags((String)args[1]);
            if (line != null) {
               if (from == null) {
                  this.append(new ChatUI.MultiChat.MyMessage(line, this.iw()), true);
               } else {
                  ChatUI.Channel.Message cmsg = new ChatUI.MultiChat.NamedMessage(from, line, this.fromcolor(from), this.iw());
                  this.append(cmsg, true);
                  if (this.notify) {
                     this.notify(cmsg);
                  }
               }
            }
         } else {
            super.uimsg(msg, args);
         }
      }

      @Override
      public String name() {
         return this.name;
      }

      public class MyMessage extends ChatUI.Channel.SimpleMessage {
         public MyMessage(String text, int w) {
            super(text, new Color(192, 192, 255), w);
         }
      }

      public class NamedMessage extends ChatUI.Channel.Message {
         public final int from;
         public final String text;
         public final int w;
         public final Color col;
         private String cn;
         private Text r = null;

         public NamedMessage(int from, String text, Color col, int w) {
            this.from = from;
            this.text = text;
            this.w = w;
            this.col = col;
         }

         @Override
         public Text text() {
            BuddyWnd.Buddy b = MultiChat.this.getparent(GameUI.class).buddies.find(this.from);
            String nm = b == null ? "???" : b.name;
            if (this.r == null || !nm.equals(this.cn)) {
               String msg = RichText.Parser.quote(String.format("%s: %s", nm, this.text));
               if (Config.timestamp) {
                  msg = Utils.timestamp(msg);
               }

               this.r = ChatUI.fnd.render(msg, this.w, TextAttribute.FOREGROUND, this.col);
               this.cn = nm;
            }

            return this.r;
         }

         @Override
         public Tex tex() {
            return this.text().tex();
         }

         @Override
         public Coord sz() {
            return this.r == null ? this.text().sz() : this.r.sz();
         }
      }
   }

   private class Notification {
      public final ChatUI.Channel chan;
      public final Text chnm;
      public final ChatUI.Channel.Message msg;
      public final long time = System.currentTimeMillis();

      private Notification(ChatUI.Channel chan, ChatUI.Channel.Message msg) {
         this.chan = chan;
         this.msg = msg;
         this.chnm = ChatUI.this.chansel.nf.render(chan.name(), Color.WHITE);
      }
   }

   public static class PartyChat extends ChatUI.MultiChat {
      public PartyChat(Widget parent) {
         super(parent, "Party", true);
      }

      @Override
      public void uimsg(String msg, Object... args) {
         if (msg == "msg") {
            Integer from = (Integer)args[0];
            int gobid = (Integer)args[1];
            String line = ChatUI.parseTags((String)args[2]);
            if (line != null) {
               Color col = Color.WHITE;
               synchronized (this.ui.sess.glob.party.memb) {
                  Party.Member pm = this.ui.sess.glob.party.memb.get((long)gobid);
                  if (pm != null) {
                     col = ChatUI.lighter(pm.col);
                  }
               }

               if (from == null) {
                  this.append(new ChatUI.MultiChat.MyMessage(line, this.iw()), true);
               } else {
                  ChatUI.Channel.Message cmsg = new ChatUI.MultiChat.NamedMessage(from, line, col, this.iw());
                  this.append(cmsg, true);
                  this.notify(cmsg);
               }
            }
         } else {
            super.uimsg(msg, args);
         }
      }
   }

   public static class PrivChat extends ChatUI.EntryChannel {
      private final int other;
      public static final Color[] gc = new Color[]{new Color(230, 48, 32), new Color(64, 180, 200)};

      public PrivChat(Widget parent, int other) {
         super(parent);
         this.other = other;
         super.startLogging();
      }

      @Override
      public void uimsg(String msg, Object... args) {
         if (msg == "msg") {
            String t = (String)args[0];
            String line = ChatUI.parseTags((String)args[1]);
            if (t.equals("in") && line != null) {
               ChatUI.Channel.Message cmsg = new ChatUI.PrivChat.InMessage(line, this.iw());
               this.append(cmsg, true);
               this.notify(cmsg);
            } else if (t.equals("out") && line != null) {
               this.append(new ChatUI.PrivChat.OutMessage(line, this.iw()), false);
            }
         } else if (msg == "err") {
            String err = (String)args[0];
            ChatUI.Channel.Message cmsg = new ChatUI.Channel.SimpleMessage(err, Color.RED, this.iw());
            this.append(cmsg, false);
            this.notify(cmsg);
         } else {
            super.uimsg(msg, args);
         }
      }

      @Override
      public String name() {
         BuddyWnd.Buddy b = this.getparent(GameUI.class).buddies.find(this.other);
         return b == null ? "???" : b.name;
      }

      public class InMessage extends ChatUI.Channel.SimpleMessage {
         public InMessage(String text, int w) {
            super(text, ChatUI.PrivChat.gc[0], w);
         }
      }

      public class OutMessage extends ChatUI.Channel.SimpleMessage {
         public OutMessage(String text, int w) {
            super(text, ChatUI.PrivChat.gc[1], w);
         }
      }
   }

   private class QuickLine extends LineEdit {
      public final ChatUI.EntryChannel chan;

      private QuickLine(ChatUI.EntryChannel chan) {
         this.chan = chan;
      }

      private void cancel() {
         ChatUI.this.qline = null;
         ChatUI.this.ui.grabkeys(null);
      }

      @Override
      protected void done(String line) {
         if (line.length() > 0) {
            this.chan.send(line);
         }

         this.cancel();
      }

      @Override
      public boolean key(char c, int code, int mod) {
         if (c == 27) {
            this.cancel();
            return true;
         } else {
            return super.key(c, code, mod);
         }
      }
   }

   private class Selector extends Widget {
      public Text.Foundry nf = new Text.Foundry("SansSerif", 12);
      private final List<ChatUI.Selector.DarkChannel> chls = new ArrayList<>();
      private int s = 0;
      public int ymod = 0;
      public boolean rerender = false;
      public Text.Foundry nfu = new Text.Foundry("SansSerif", 14, 1);

      public Selector(Coord c, Coord sz) {
         super(c, sz, ChatUI.this);
      }

      private void add(ChatUI.Channel chan) {
         synchronized (this.chls) {
            this.chls.add(new ChatUI.Selector.DarkChannel(chan));
         }
      }

      private void rm(ChatUI.Channel chan) {
         synchronized (this.chls) {
            Iterator<ChatUI.Selector.DarkChannel> i = this.chls.iterator();

            while (i.hasNext()) {
               ChatUI.Selector.DarkChannel c = i.next();
               if (c.chan == chan) {
                  i.remove();
               }
            }
         }
      }

      @Override
      public void draw(GOut g) {
         g.chcolor(64, 64, 64, 192);
         g.frect(Coord.z, this.sz);
         int i = this.s;
         int y = 0;
         synchronized (this.chls) {
            while (i < this.chls.size()) {
               ChatUI.Selector.DarkChannel ch = this.chls.get(i);
               if (ch.chan == ChatUI.this.sel) {
                  g.chcolor(128, 128, 192, 255);
                  g.frect(new Coord(0, y), new Coord(this.sz.x, 19 + this.ymod));
               }

               g.chcolor(255, 255, 255, 255);
               if (this.rerender || ch.rname == null || !ch.rname.text.equals(ch.chan.name()) || ch.rread != ch.chan.read) {
                  ch.rread = ch.chan.read;
                  if (ch.rread) {
                     ch.rname = this.nf.render(ch.chan.name());
                  } else {
                     ch.rname = this.nfu.render(ch.chan.name());
                  }
               }

               if (this.rerender) {
                  ch.chan.c = new Coord(ChatUI.selw, 0);
               }

               g.aimage(ch.rname.tex(), new Coord(this.sz.x / 2, y + 10 + this.ymod / 2), 0.5, 0.5);
               g.line(new Coord(5, y + 19 + this.ymod), new Coord(this.sz.x - 5, y + 19 + this.ymod), 1.0);
               y += 20 + this.ymod;
               if (y >= this.sz.y) {
                  break;
               }

               i++;
            }

            this.rerender = false;
         }

         g.chcolor();
      }

      public boolean up() {
         ChatUI.Channel prev = null;

         for (ChatUI.Selector.DarkChannel ch : this.chls) {
            if (ch.chan == ChatUI.this.sel) {
               if (prev != null) {
                  ChatUI.this.select(prev);
                  return true;
               }

               return false;
            }

            prev = ch.chan;
         }

         return false;
      }

      public boolean down() {
         Iterator<ChatUI.Selector.DarkChannel> i = this.chls.iterator();

         while (i.hasNext()) {
            ChatUI.Selector.DarkChannel ch = i.next();
            if (ch.chan == ChatUI.this.sel) {
               if (i.hasNext()) {
                  ChatUI.this.select(i.next().chan);
                  return true;
               }

               return false;
            }
         }

         return false;
      }

      private ChatUI.Channel bypos(Coord c) {
         int i = c.y / (20 + this.ymod) + this.s;
         return i >= 0 && i < this.chls.size() ? this.chls.get(i).chan : null;
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         if (button == 1) {
            ChatUI.Channel chan = this.bypos(c);
            if (chan != null) {
               ChatUI.this.select(chan);
            }
         }

         return true;
      }

      @Override
      public boolean mousewheel(Coord c, int amount) {
         this.s += amount;
         if (this.s >= this.chls.size() - this.sz.y / 20) {
            this.s = this.chls.size() - this.sz.y / 20;
         }

         if (this.s < 0) {
            this.s = 0;
         }

         return true;
      }

      private class DarkChannel {
         public final ChatUI.Channel chan;
         public Text rname;
         public boolean rread;

         private DarkChannel(ChatUI.Channel chan) {
            this.chan = chan;
            this.rread = false;
         }
      }
   }

   public static class SimpleChat extends ChatUI.EntryChannel {
      public final String name;

      public SimpleChat(Widget parent, String name) {
         super(parent);
         this.name = name;
      }

      @Override
      public void uimsg(String msg, Object... args) {
         if (msg != "msg" && msg != "log") {
            super.uimsg(msg, args);
         } else {
            String line = ChatUI.parseTags((String)args[0]);
            if (line != null) {
               Color col = null;
               if (args.length > 1) {
                  col = (Color)args[1];
               }

               if (col == null) {
                  col = Color.WHITE;
               }

               boolean notify = args.length > 2 ? (Integer)args[2] != 0 : false;
               ChatUI.Channel.Message cmsg = new ChatUI.Channel.SimpleMessage(line, col, this.iw());
               this.append(cmsg, true);
               if (notify) {
                  this.notify(cmsg);
               }
            }
         }
      }

      @Override
      public String name() {
         return this.name;
      }
   }

   static class TextEntryChannel extends TextEntry {
      public static Text.Foundry efnd = new Text.Foundry(new Font("SansSerif", 0, 12), Color.BLACK);

      @Override
      protected Text.Line render_text(String text) {
         return efnd.render(text);
      }

      public TextEntryChannel(Coord c, Coord sz, Widget parent, String deftext) {
         super(c, sz, parent, deftext);
      }
   }
}
