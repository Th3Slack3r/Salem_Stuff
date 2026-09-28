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
  public static RichText.Foundry fnd = new RichText.Foundry(new ChatParser(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12), TextAttribute.FOREGROUND, Color.BLACK }));
  
  public static Text.Foundry qfnd = new Text.Foundry(new Font("SansSerif", 0, 14), new Color(192, 255, 192));
  
  private static int i100 = Config.two_chat_columns ? 200 : 100;
  
  public static int selw = i100;
  
  public Channel sel = null;
  
  public Channel prevsel = null;
  
  protected final Selector chansel;
  
  private Coord base;
  
  private static int basesize = 12;
  
  private QuickLine qline = null;
  
  private final LinkedList<Notification> notifs = new LinkedList<>();
  
  private static final Pattern tags_patt = Pattern.compile("\\$(hl)\\[([^\\[\\]]*)\\]");
  
  private Text.Line rqline;
  
  private int rqpre;
  
  public ChatUI(Coord c, int w, Widget parent) {
    super(c.add(0, -50), new Coord(w, 50), parent);
    this.rqline = null;
    this.chansel = new Selector(Coord.z, new Coord(selw, this.sz.y));
    this.chansel.hide();
    this.base = c;
    setfocusctl(true);
    setcanfocus(false);
    setbasesize((int)Utils.getpreff("chatfontsize", 12.0F));
    update_visibility();
  }
  
  public final void setbasesize(int basesize) {
    fnd = new RichText.Foundry(new ChatParser(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(basesize), TextAttribute.FOREGROUND, Color.BLACK }));
    qfnd = new Text.Foundry(new Font("SansSerif", 0, basesize + 2), new Color(192, 255, 192));
    selw = i100 + (basesize - 12) * 6;
    TextEntryChannel.efnd = new Text.Foundry(new Font("SansSerif", 0, basesize), Color.WHITE);
    this.c = this.base.add(0, -100 - (basesize - 12) * 24);
    resize(new Coord(this.sz.x, 100 + (basesize - 12) * 24));
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
  
  public static class ChatAttribute extends AttributedCharacterIterator.Attribute {
    private ChatAttribute(String name) {
      super(name);
    }
    
    public static final AttributedCharacterIterator.Attribute HYPERLINK = new ChatAttribute("hyperlink");
  }
  
  public static class FuckMeGentlyWithAChainsaw {
    public final URL url;
    
    public FuckMeGentlyWithAChainsaw(URL url) {
      this.url = url;
    }
  }
  
  public static class ChatParser extends RichText.Parser {
    public static final Pattern urlpat = Pattern.compile("\\b((https?://)|(www\\.[a-z0-9_.-]+\\.[a-z0-9_.-]+))[a-z0-9/_.~#%+?&:*=-]*", 2);
    
    public static final Map<? extends AttributedCharacterIterator.Attribute, ?> urlstyle = RichText.fillattrs(new Object[] { TextAttribute.FOREGROUND, new Color(64, 175, 255), TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD });
    
    public ChatParser(Object... args) {
      super(args);
    }
    
    protected RichText.Part text(RichText.Parser.PState s, String text, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      RichText.Part ret = null;
      int p = 0;
      while (true) {
        URL url;
        Matcher m = urlpat.matcher(text);
        if (!m.find(p))
          break; 
        try {
          String su = text.substring(m.start(), m.end());
          if (su.indexOf(':') < 0)
            su = "http://" + su; 
          url = new URL(su);
        } catch (MalformedURLException e) {
          p = m.end();
          continue;
        } 
        RichText.Part lead = new RichText.TextPart(text.substring(0, m.start()), attrs);
        if (ret == null) {
          ret = lead;
        } else {
          ret.append(lead);
        } 
        Map<AttributedCharacterIterator.Attribute, Object> na = new HashMap<>(attrs);
        na.putAll(urlstyle);
        na.put(ChatUI.ChatAttribute.HYPERLINK, new ChatUI.FuckMeGentlyWithAChainsaw(url));
        ret.append(new RichText.TextPart(text.substring(m.start(), m.end()), na));
        p = m.end();
      } 
      if (ret == null) {
        ret = new RichText.TextPart(text, attrs);
      } else {
        ret.append(new RichText.TextPart(text.substring(p), attrs));
      } 
      return ret;
    }
  }
  
  public static abstract class Channel extends Widget {
    public final List<Message> msgs = new LinkedList<Message>() {
        public boolean add(ChatUI.Channel.Message message) {
          if (size() >= 200)
            removeFirst().tex().dispose(); 
          return super.add(message);
        }
      };
    
    private final Scrollbar sb;
    
    private FileOutputStream fos = null;
    
    public IButton cbtn;
    
    protected boolean read = true;
    
    public final Comparator<CharPos> poscmp;
    
    private CharPos selorig;
    
    private CharPos lasthit;
    
    private CharPos selstart;
    
    private CharPos selend;
    
    private boolean dragging;
    
    public void show() {
      super.show();
      this.read = true;
    }
    
    public static abstract class Message {
      public final long time = System.currentTimeMillis();
      
      public abstract Text text();
      
      public abstract Tex tex();
      
      public abstract Coord sz();
    }
    
    public static class SimpleMessage extends Message {
      private final Text t;
      
      public SimpleMessage(String text, Color col, int w) {
        if (Config.timestamp)
          text = Utils.timestamp(text); 
        if (col == null) {
          this.t = ChatUI.fnd.render(RichText.Parser.quote(text), w, new Object[0]);
        } else {
          this.t = ChatUI.fnd.render(RichText.Parser.quote(text), w, new Object[] { TextAttribute.FOREGROUND, col });
        } 
      }
      
      public Text text() {
        return this.t;
      }
      
      public Tex tex() {
        return this.t.tex();
      }
      
      public Coord sz() {
        return this.t.sz();
      }
    }
    
    public Channel(Coord c, Coord sz, Widget parent, boolean closeable) {
      super(c, sz, parent);
      this.poscmp = new Comparator<CharPos>() {
          public int compare(ChatUI.Channel.CharPos a, ChatUI.Channel.CharPos b) {
            if (a.msg != b.msg) {
              synchronized (ChatUI.Channel.this.msgs) {
                for (ChatUI.Channel.Message msg : ChatUI.Channel.this.msgs) {
                  if (msg == a.msg)
                    return -1; 
                  if (msg == b.msg)
                    return 1; 
                } 
              } 
              throw new IllegalStateException("CharPos message is no longer contained in the log");
            } 
            if (a.part != b.part) {
              RichText.Part part = ((RichText)a.msg.text()).parts;
              if (part != null) {
                if (part == a.part)
                  return -1; 
                return 1;
              } 
              throw new IllegalStateException("CharPos is no longer contained in the log");
            } 
            return a.ch.getInsertionIndex() - b.ch.getInsertionIndex();
          }
        };
      this.sb = new Scrollbar(new Coord(sz.x, 0), ih(), this, 0, -ih());
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
      if (Config.chatlogs)
        try {
          String fixed_char_name = name().replaceAll("[\\/:*?\"<>|]", "");
          String path = String.format("%s/logs/%s/%s/", new Object[] { Config.userhome, Config.currentCharName, fixed_char_name });
          String filename = String.format("%s.txt", new Object[] { Utils.current_date() });
          File pathf = new File(path);
          if (pathf.mkdirs() || pathf.isDirectory()) {
            this.fos = new FileOutputStream(path + filename);
          } else {
            throw new FileNotFoundException("Failed to create parent directories!");
          } 
        } catch (FileNotFoundException ex) {
          this.ui.message("Could not open file for chat logging!", GameUI.MsgType.INFO);
        }  
    }
    
    public void append(Message msg, boolean attn) {
      synchronized (this.msgs) {
        this.msgs.add(msg);
        int y = 0;
        for (Message m : this.msgs)
          y += (m.sz()).y; 
        boolean b = (this.sb.val >= this.sb.max);
        this.sb.max = y - ih();
        if (b)
          this.sb.val = this.sb.max; 
        if (this.fos != null)
          try {
            this.fos.write(((msg.text()).text + "\n").getBytes());
          } catch (IOException ex) {
            Logger.getLogger(ChatUI.class.getName()).log(Level.SEVERE, (String)null, ex);
          }  
        if (attn && !this.visible)
          this.read = false; 
      } 
    }
    
    public void append(String line, Color col) {
      append(new SimpleMessage(line, col, iw()), false);
    }
    
    public int iw() {
      return this.sz.x - this.sb.sz.x;
    }
    
    public int ih() {
      return this.sz.y;
    }
    
    public void draw(GOut g) {
      g.chcolor(24, 24, 16, 200);
      g.frect(Coord.z, this.sz);
      g.chcolor();
      int y = 0;
      boolean sel = false;
      synchronized (this.msgs) {
        for (Message msg : this.msgs) {
          if (this.selstart != null && msg == this.selstart.msg)
            sel = true; 
          int y1 = y - this.sb.val - ChatUI.basesize - 10;
          int y2 = y1 + (msg.sz()).y;
          if (y2 > 0 && y1 < ih()) {
            if (sel)
              drawsel(g, msg, y1); 
            g.image(msg.tex(), new Coord(0, y1));
          } 
          if (this.selend != null && msg == this.selend.msg)
            sel = false; 
          y += (msg.sz()).y;
        } 
      } 
      this.sb.max = y - ih();
      super.draw(g);
    }
    
    public boolean mousewheel(Coord c, int amount) {
      this.sb.ch(amount * 15);
      return true;
    }
    
    public void resize(Coord sz) {
      super.resize(sz);
      if (this.sb != null) {
        this.sb.resize(ih());
        this.sb.move(new Coord(sz.x, 0));
        int y = 0;
        for (Message m : this.msgs)
          y += (m.sz()).y; 
        boolean b = (this.sb.val >= this.sb.max);
        this.sb.max = y - ih();
        if (b)
          this.sb.val = this.sb.max; 
      } 
      if (this.cbtn != null)
        this.cbtn.c = new Coord(sz.x - this.cbtn.sz.x - ((this.sb != null) ? this.sb.sz.x : 0) - 3, 0); 
    }
    
    public void notify(Message msg) {
      ((ChatUI)getparent(ChatUI.class)).notify(this, msg);
    }
    
    public static class CharPos {
      public final ChatUI.Channel.Message msg;
      
      public final RichText.TextPart part;
      
      public final TextHitInfo ch;
      
      public CharPos(ChatUI.Channel.Message msg, RichText.TextPart part, TextHitInfo ch) {
        this.msg = msg;
        this.part = part;
        this.ch = ch;
      }
      
      public boolean equals(Object oo) {
        if (!(oo instanceof CharPos))
          return false; 
        CharPos o = (CharPos)oo;
        return (o.msg == this.msg && o.part == this.part && o.ch.equals(this.ch));
      }
    }
    
    public Message messageat(Coord c, Coord hc) {
      int y = -this.sb.val;
      synchronized (this.msgs) {
        for (Message msg : this.msgs) {
          Coord sz = msg.sz();
          if (c.y >= y && c.y < y + sz.y) {
            if (hc != null) {
              hc.x = c.x;
              c.y -= y;
            } 
            return msg;
          } 
          y += sz.y;
        } 
      } 
      return null;
    }
    
    public CharPos charat(Coord c) {
      if (c.y < -this.sb.val) {
        if (this.msgs.size() < 1)
          return null; 
        Message message = this.msgs.get(0);
        if (!(message.text() instanceof RichText))
          return null; 
        RichText.TextPart fp = null;
        for (RichText.Part part = ((RichText)message.text()).parts; part != null; part = part.next) {
          if (part instanceof RichText.TextPart) {
            fp = (RichText.TextPart)part;
            break;
          } 
        } 
        if (fp == null)
          return null; 
        return new CharPos(message, fp, TextHitInfo.leading(0));
      } 
      Coord hc = new Coord();
      Message msg = messageat(c, hc);
      if (msg == null || !(msg.text() instanceof RichText))
        return null; 
      RichText rt = (RichText)msg.text();
      RichText.Part p = rt.partat(hc);
      if (p == null) {
        RichText.TextPart lp = null;
        for (RichText.Part part = ((RichText)msg.text()).parts; part != null; part = part.next) {
          if (part instanceof RichText.TextPart)
            lp = (RichText.TextPart)part; 
        } 
        if (lp == null)
          return null; 
        return new CharPos(msg, lp, TextHitInfo.trailing(lp.end - lp.start - 1));
      } 
      if (!(p instanceof RichText.TextPart))
        return null; 
      RichText.TextPart tp = (RichText.TextPart)p;
      return new CharPos(msg, tp, tp.charat(hc));
    }
    
    public boolean mousedown(Coord c, int btn) {
      if (super.mousedown(c, btn))
        return true; 
      if (btn == 1) {
        this.selstart = this.selend = null;
        CharPos ch = charat(c);
        if (ch != null) {
          this.selorig = this.lasthit = ch;
          this.dragging = false;
          this.ui.grabmouse(this);
        } 
        return true;
      } 
      return false;
    }
    
    public void mousemove(Coord c) {
      if (this.selorig != null) {
        CharPos ch = charat(c);
        if (ch != null && !ch.equals(this.lasthit)) {
          this.lasthit = ch;
          if (!this.dragging && !ch.equals(this.selorig))
            this.dragging = true; 
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
    
    protected void selected(CharPos start, CharPos end) {
      StringBuilder buf = new StringBuilder();
      synchronized (this.msgs) {
        boolean sel = false;
        for (Message msg : this.msgs) {
          if (!(msg.text() instanceof RichText))
            continue; 
          RichText rt = (RichText)msg.text();
          RichText.Part part = null;
          if (sel) {
            part = rt.parts;
          } else if (msg == start.msg) {
            sel = true;
            for (part = rt.parts; part != null && part != start.part; part = part.next);
          } 
          if (sel) {
            for (; part != null; part = part.next) {
              if (part instanceof RichText.TextPart) {
                int sch, ech;
                RichText.TextPart tp = (RichText.TextPart)part;
                CharacterIterator iter = tp.ti();
                if (tp == start.part) {
                  sch = tp.start + start.ch.getInsertionIndex();
                } else {
                  sch = tp.start;
                } 
                if (tp == end.part) {
                  ech = tp.start + end.ch.getInsertionIndex();
                } else {
                  ech = tp.end;
                } 
                for (int i = sch; i < ech; i++)
                  buf.append(iter.setIndex(i)); 
                if (part == end.part) {
                  sel = false;
                  break;
                } 
                buf.append(' ');
              } 
            } 
            if (sel)
              buf.append('\n'); 
          } 
          if (msg == end.msg)
            break; 
        } 
      } 
      Clipboard cl;
      if ((cl = Toolkit.getDefaultToolkit().getSystemSelection()) == null)
        cl = Toolkit.getDefaultToolkit().getSystemClipboard(); 
      try {
        final CharPos ownsel = this.selstart;
        cl.setContents(new StringSelection(buf.toString()), new ClipboardOwner() {
              public void lostOwnership(Clipboard cl, Transferable tr) {
                if (ChatUI.Channel.this.selstart == ownsel)
                  ChatUI.Channel.this.selstart = ChatUI.Channel.this.selend = null; 
              }
            });
      } catch (IllegalStateException illegalStateException) {}
    }
    
    protected void clicked(CharPos pos) {
      AttributedCharacterIterator inf = pos.part.ti();
      if (pos.ch.getCharIndex() < inf.getBeginIndex() || pos.ch.getCharIndex() >= inf.getEndIndex())
        return; 
      inf.setIndex(pos.ch.getCharIndex());
      ChatUI.FuckMeGentlyWithAChainsaw url = (ChatUI.FuckMeGentlyWithAChainsaw)inf.getAttribute(ChatUI.ChatAttribute.HYPERLINK);
      if (url != null && WebBrowser.self != null)
        try {
          WebBrowser.self.show(url.url);
        } catch (BrowserException e) {
          ((GameUI)getparent(GameUI.class)).error("Could not launch web browser.");
        }  
    }
    
    public boolean mouseup(Coord c, int btn) {
      if (btn == 1 && this.selorig != null) {
        if (this.selstart != null) {
          selected(this.selstart, this.selend);
        } else {
          clicked(this.selorig);
        } 
        this.ui.grabmouse(null);
        this.selorig = null;
        this.dragging = false;
      } 
      return super.mouseup(c, btn);
    }
    
    public void select() {
      ((ChatUI)getparent(ChatUI.class)).select(this);
    }
    
    public void display() {
      select();
      ChatUI chat = getparent(ChatUI.class);
      chat.expand();
      chat.parent.setfocus(chat);
    }
    
    private void drawsel(GOut g, Message msg, int y) {
      RichText rt = (RichText)msg.text();
      boolean sel = (msg != this.selstart.msg);
      for (RichText.Part part = rt.parts; part != null; part = part.next) {
        TextHitInfo a, b;
        if (!(part instanceof RichText.TextPart))
          continue; 
        RichText.TextPart tp = (RichText.TextPart)part;
        if (tp.start == tp.end)
          continue; 
        if (sel) {
          a = TextHitInfo.leading(0);
        } else if (tp == this.selstart.part) {
          a = this.selstart.ch;
          sel = true;
        } else {
          continue;
        } 
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
        if (!sel)
          break; 
        continue;
      } 
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.cbtn) {
        wdgmsg("close", new Object[0]);
        if (this.fos != null)
          try {
            this.fos.close();
          } catch (IOException ex) {
            Logger.getLogger(ChatUI.class.getName()).log(Level.SEVERE, (String)null, ex);
          }  
      } else {
        super.wdgmsg(sender, msg, args);
      } 
    }
    
    public void uimsg(String name, Object... args) {
      if (name == "sel") {
        select();
      } else if (name == "dsp") {
        display();
      } else {
        super.uimsg(name, args);
      } 
    }
    
    public abstract String name();
  }
  
  public static abstract class Message {
    public final long time;
    
    public Message() {
      this.time = System.currentTimeMillis();
    }
    
    public abstract Text text();
    
    public abstract Tex tex();
    
    public abstract Coord sz();
  }
  
  public static class Log extends Channel {
    private final String name;
    
    public Log(Widget parent, String name) {
      super(parent, false);
      this.name = name;
    }
    
    public String name() {
      return this.name;
    }
  }
  
  static class TextEntryChannel extends TextEntry {
    public static Text.Foundry efnd = new Text.Foundry(new Font("SansSerif", 0, 12), Color.BLACK);
    
    protected Text.Line render_text(String text) {
      return efnd.render(text);
    }
    
    public TextEntryChannel(Coord c, Coord sz, Widget parent, String deftext) {
      super(c, sz, parent, deftext);
    }
  }
  
  public static abstract class EntryChannel extends Channel {
    private final TextEntry in;
    
    private final List<String> history = new ArrayList<>();
    
    private int hpos = 0;
    
    private String hcurrent;
    
    public EntryChannel(Widget parent) {
      super(parent, true);
      setfocusctl(true);
      int height = ChatUI.basesize + 8;
      this.in = new ChatUI.TextEntryChannel(new Coord(0, this.sz.y - height), new Coord(this.sz.x, height), this, "") {
          public void activate(String text) {
            if (text.length() > 0)
              ChatUI.EntryChannel.this.send(text); 
            settext("");
            ChatUI.EntryChannel.this.hpos = ChatUI.EntryChannel.this.history.size();
          }
          
          public boolean keydown(KeyEvent ev) {
            if (ev.getKeyCode() == 38) {
              if (ChatUI.EntryChannel.this.hpos > 0) {
                if (ChatUI.EntryChannel.this.hpos == ChatUI.EntryChannel.this.history.size())
                  ChatUI.EntryChannel.this.hcurrent = this.text; 
                rsettext(ChatUI.EntryChannel.this.history.get(--ChatUI.EntryChannel.this.hpos));
              } 
              return true;
            } 
            if (ev.getKeyCode() == 40) {
              if (ChatUI.EntryChannel.this.hpos < ChatUI.EntryChannel.this.history.size())
                if (++ChatUI.EntryChannel.this.hpos == ChatUI.EntryChannel.this.history.size()) {
                  rsettext(ChatUI.EntryChannel.this.hcurrent);
                } else {
                  rsettext(ChatUI.EntryChannel.this.history.get(ChatUI.EntryChannel.this.hpos));
                }  
              return true;
            } 
            return super.keydown(ev);
          }
        };
    }
    
    public int ih() {
      return this.sz.y - 20;
    }
    
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
      wdgmsg("msg", new Object[] { text });
    }
  }
  
  public static class SimpleChat extends EntryChannel {
    public final String name;
    
    public SimpleChat(Widget parent, String name) {
      super(parent);
      this.name = name;
    }
    
    public void uimsg(String msg, Object... args) {
      if (msg == "msg" || msg == "log") {
        String line = ChatUI.parseTags((String)args[0]);
        if (line != null) {
          Color col = null;
          if (args.length > 1)
            col = (Color)args[1]; 
          if (col == null)
            col = Color.WHITE; 
          boolean notify = (args.length > 2) ? ((((Integer)args[2]).intValue() != 0)) : false;
          ChatUI.Channel.Message cmsg = new ChatUI.Channel.SimpleMessage(line, col, iw());
          append(cmsg, true);
          if (notify)
            notify(cmsg); 
        } 
      } else {
        super.uimsg(msg, args);
      } 
    }
    
    public String name() {
      return this.name;
    }
  }
  
  public static class MultiChat extends EntryChannel {
    private final String name;
    
    private final boolean notify;
    
    private final Map<Integer, Color> pc = new HashMap<>();
    
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
      
      public Text text() {
        BuddyWnd.Buddy b = ((GameUI)ChatUI.MultiChat.this.getparent((Class)GameUI.class)).buddies.find(this.from);
        String nm = (b == null) ? "???" : b.name;
        if (this.r == null || !nm.equals(this.cn)) {
          String msg = RichText.Parser.quote(String.format("%s: %s", new Object[] { nm, this.text }));
          if (Config.timestamp)
            msg = Utils.timestamp(msg); 
          this.r = ChatUI.fnd.render(msg, this.w, new Object[] { TextAttribute.FOREGROUND, this.col });
          this.cn = nm;
        } 
        return this.r;
      }
      
      public Tex tex() {
        return text().tex();
      }
      
      public Coord sz() {
        if (this.r == null)
          return text().sz(); 
        return this.r.sz();
      }
    }
    
    public class MyMessage extends ChatUI.Channel.SimpleMessage {
      public MyMessage(String text, int w) {
        super(text, new Color(192, 192, 255), w);
      }
    }
    
    public MultiChat(Widget parent, String name, boolean notify) {
      super(parent);
      this.name = name;
      this.notify = notify;
      startLogging();
    }
    
    private static final Random cr = new Random();
    
    private static Color randcol() {
      int[] c = { cr.nextInt(256), cr.nextInt(256), cr.nextInt(256) };
      int mc = Math.max(c[0], Math.max(c[1], c[2]));
      if (c[2] > c[0]) {
        int t = c[0];
        c[0] = c[2];
        c[2] = t;
      } 
      for (int i = 0; i < c.length; i++)
        c[i] = c[i] * 255 / mc; 
      return new Color(c[0], c[1], c[2]);
    }
    
    public Color fromcolor(int from) {
      synchronized (this.pc) {
        Color c = this.pc.get(Integer.valueOf(from));
        if (c == null)
          this.pc.put(Integer.valueOf(from), c = randcol()); 
        return c;
      } 
    }
    
    public void uimsg(String msg, Object... args) {
      if (msg == "msg") {
        Integer from = (Integer)args[0];
        String line = ChatUI.parseTags((String)args[1]);
        if (line != null)
          if (from == null) {
            append(new MyMessage(line, iw()), true);
          } else if (!ChatUI.isIgnored(ChatUI.getName(from.intValue()))) {
            ChatUI.Channel.Message cmsg = new NamedMessage(from.intValue(), line, fromcolor(from.intValue()), iw());
            append(cmsg, true);
            if (this.notify)
              notify(cmsg); 
          }  
      } else {
        super.uimsg(msg, args);
      } 
    }
    
    public String name() {
      return this.name;
    }
  }
  
  public static class PartyChat extends MultiChat {
    public PartyChat(Widget parent) {
      super(parent, "Party", true);
    }
    
    public void uimsg(String msg, Object... args) {
      if (msg == "msg") {
        Integer from = (Integer)args[0];
        int gobid = ((Integer)args[1]).intValue();
        String line = ChatUI.parseTags((String)args[2]);
        if (line != null) {
          Color col = Color.WHITE;
          synchronized (this.ui.sess.glob.party.memb) {
            Party.Member pm = this.ui.sess.glob.party.memb.get(Long.valueOf(gobid));
            if (pm != null)
              col = ChatUI.lighter(pm.col); 
          } 
          if (from == null) {
            append(new ChatUI.MultiChat.MyMessage(this, line, iw()), true);
          } else {
            ChatUI.Channel.Message cmsg = new ChatUI.MultiChat.NamedMessage(this, from.intValue(), line, col, iw());
            append(cmsg, true);
            notify(cmsg);
          } 
        } 
      } else {
        super.uimsg(msg, args);
      } 
    }
  }
  
  public static class PrivChat extends EntryChannel {
    protected final int other;
    
    public static final Color[] gc = new Color[] { new Color(230, 48, 32), new Color(64, 180, 200) };
    
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
    
    public PrivChat(Widget parent, int other) {
      super(parent);
      this.other = other;
      if (!isIgnored())
        ((ChatUI)getparent(ChatUI.class)).select(this); 
      startLogging();
    }
    
    public void uimsg(String msg, Object... args) {
      if (msg == "msg") {
        String t = (String)args[0];
        String line = ChatUI.parseTags((String)args[1]);
        if (t.equals("in")) {
          if (this.msgs.isEmpty() && isIgnored()) {
            send("[Automatic] This player has ignored you and will not see your messages.");
            this.cbtn.click();
            return;
          } 
          if (line != null) {
            ChatUI.Channel.Message cmsg = new InMessage(line, iw());
            append(cmsg, true);
            notify(cmsg);
          } 
        } else if (t.equals("out") && line != null) {
          append(new OutMessage(line, iw()), false);
        } 
      } else if (msg == "err") {
        String err = (String)args[0];
        ChatUI.Channel.Message cmsg = new ChatUI.Channel.SimpleMessage(err, Color.RED, iw());
        append(cmsg, false);
        notify(cmsg);
      } else {
        super.uimsg(msg, args);
      } 
    }
    
    protected boolean isIgnored() {
      String name = name();
      return ChatUI.isIgnored(name);
    }
    
    public String name() {
      BuddyWnd.Buddy b = ((GameUI)getparent((Class)GameUI.class)).buddies.find(this.other);
      if (b == null)
        return "???"; 
      return b.name;
    }
  }
  
  @RName("schan")
  public static class $SChan implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      String name = (String)args[0];
      return new ChatUI.SimpleChat(parent, name);
    }
  }
  
  @RName("mchat")
  public static class $MChat implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      String name = (String)args[0];
      boolean notify = (((Integer)args[1]).intValue() != 0);
      return new ChatUI.MultiChat(parent, name, notify);
    }
  }
  
  @RName("pchat")
  public static class $PChat implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new ChatUI.PartyChat(parent);
    }
  }
  
  @RName("pmchat")
  public static class $PMChat implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      int other = ((Integer)args[0]).intValue();
      return new ChatUI.PrivChat(parent, other);
    }
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    return Widget.gettype(type).create(Coord.z, this, cargs);
  }
  
  protected class Selector extends Widget {
    public Text.Foundry nf = new Text.Foundry("SansSerif", 12);
    
    protected final List<DarkChannel> chls = new ArrayList<>();
    
    public int s = 0;
    
    public int ymod = 0;
    
    public boolean rerender = false;
    
    public Text.Foundry nfu = new Text.Foundry("SansSerif", 14, 1);
    
    protected class DarkChannel {
      public final ChatUI.Channel chan;
      
      public Text rname;
      
      public boolean rread;
      
      private DarkChannel(ChatUI.Channel chan) {
        this.chan = chan;
        this.rread = false;
      }
    }
    
    private void add(ChatUI.Channel chan) {
      synchronized (this.chls) {
        this.chls.add(new DarkChannel(chan));
      } 
    }
    
    private void rm(ChatUI.Channel chan) {
      synchronized (this.chls) {
        for (Iterator<DarkChannel> i = this.chls.iterator(); i.hasNext(); ) {
          DarkChannel c = i.next();
          if (c.chan == chan)
            i.remove(); 
        } 
      } 
    }
    
    public void draw(GOut g) {
      g.chcolor(64, 64, 64, 192);
      g.frect(Coord.z, this.sz);
      int i = this.s;
      int y = 0;
      synchronized (this.chls) {
        boolean lr = true;
        int xs = 0;
        int xe = 0;
        while (i < this.chls.size()) {
          DarkChannel ch = this.chls.get(i);
          if (Config.two_chat_columns) {
            if (lr) {
              xs = 0;
              xe = this.sz.x / 2;
            } else {
              xs = this.sz.x / 2;
              xe = this.sz.x;
            } 
          } else {
            xs = 0;
            xe = this.sz.x;
          } 
          if (ch.chan == ChatUI.this.sel) {
            g.chcolor(128, 128, 192, 255);
            g.frect(new Coord(xs, y), new Coord(xe, 19 + this.ymod));
          } 
          g.chcolor(255, 255, 255, 255);
          if (this.rerender || ch.rname == null || !ch.rname.text.equals(ch.chan.name()) || ch.rread != ch.chan.read) {
            ch.rread = ch.chan.read;
            if (Config.chat_online_colour && ch.chan instanceof ChatUI.PrivChat) {
              Color col = Color.white;
              try {
                int other = ((ChatUI.PrivChat)ch.chan).other;
                BuddyWnd.Buddy b = ((GameUI)getparent((Class)GameUI.class)).buddies.find(other);
                if (b.online == 0) {
                  col = new Color(192, 0, 0, 255);
                } else if (b.online > 0) {
                  col = Color.green;
                } 
              } catch (Exception exception) {}
              this.nf.defcol = col;
              this.nfu.defcol = col;
            } else {
              this.nf.defcol = Color.white;
              this.nfu.defcol = Color.white;
            } 
            if (ch.rread) {
              ch.rname = this.nf.render(ch.chan.name());
            } else {
              ch.rname = this.nfu.render(ch.chan.name());
            } 
          } 
          if (this.rerender)
            ch.chan.c = new Coord(ChatUI.selw, 0); 
          g.aimage(ch.rname.tex(), new Coord(xs + (xe - xs) / 2, y + 10 + this.ymod / 2), 0.5D, 0.5D);
          if (Config.two_chat_columns) {
            if (!lr) {
              g.line(new Coord(5, y + 19 + this.ymod), new Coord(this.sz.x - 5, y + 19 + this.ymod), 1.0D);
              y += 20 + this.ymod;
            } else {
              g.line(new Coord(xe, y), new Coord(xe, y + 19 + this.ymod), 1.0D);
            } 
            lr = !lr;
          } else {
            g.line(new Coord(5, y + 19 + this.ymod), new Coord(this.sz.x - 5, y + 19 + this.ymod), 1.0D);
            y += 20 + this.ymod;
          } 
          if (y >= this.sz.y)
            break; 
          i++;
        } 
        this.rerender = false;
      } 
      g.chcolor();
    }
    
    public boolean up() {
      ChatUI.Channel prev = null;
      for (DarkChannel ch : this.chls) {
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
      for (Iterator<DarkChannel> i = this.chls.iterator(); i.hasNext(); ) {
        DarkChannel ch = i.next();
        if (ch.chan == ChatUI.this.sel) {
          if (i.hasNext()) {
            ChatUI.this.select(((DarkChannel)i.next()).chan);
            return true;
          } 
          return false;
        } 
      } 
      return false;
    }
    
    private ChatUI.Channel bypos(Coord c) {
      if (Config.two_chat_columns) {
        int ix = 0;
        if (c.x > this.sz.x / 2)
          ix = 1; 
        int i = c.y / (20 + this.ymod) * 2 + this.s + ix;
        if (i >= 0 && i < this.chls.size())
          return ((DarkChannel)this.chls.get(i)).chan; 
      } else {
        int i = c.y / (20 + this.ymod) + this.s;
        if (i >= 0 && i < this.chls.size())
          return ((DarkChannel)this.chls.get(i)).chan; 
      } 
      return null;
    }
    
    public boolean mousedown(Coord c, int button) {
      if (button == 1) {
        ChatUI.Channel chan = bypos(c);
        if (chan != null) {
          if (ChatUI.this.sel == chan)
            if (chan.name().equals("System")) {
              Config.mute_system_chat = !Config.mute_system_chat;
              Utils.setprefb("mute_system_chat", Config.mute_system_chat);
              OptWnd2.refresh();
              Utils.msgOut("Mute System channel audio: " + Config.mute_system_chat);
            } else if (chan.name().equals("Log")) {
              Config.mute_log_chat = !Config.mute_log_chat;
              Utils.setprefb("mute_log_chat", Config.mute_log_chat);
              OptWnd2.refresh();
              Utils.msgLog("Mute Log channel audio: " + Config.mute_log_chat);
            }  
          ChatUI.this.select(chan);
        } 
      } else if (button == 3) {
        ChatUI.Channel chan = bypos(c);
        if (ChatUI.this.sel == chan)
          chan.msgs.clear(); 
      } 
      return true;
    }
    
    public boolean mousewheel(Coord c, int amount) {
      this.s += amount * (Config.two_chat_columns ? 2 : 1);
      if (this.s >= this.chls.size() - this.sz.y / 20) {
        this.s = this.chls.size() - this.sz.y / 20;
        if (Config.two_chat_columns && this.s % 2 == 1)
          this.s--; 
      } 
      if (this.s < 0)
        this.s = 0; 
      return true;
    }
    
    public Selector(Coord c, Coord sz) {
      super(c, sz, ChatUI.this);
    }
  }
  
  protected class DarkChannel {
    public final ChatUI.Channel chan;
    
    public Text rname;
    
    public boolean rread;
    
    private DarkChannel(ChatUI.Channel chan) {
      this.chan = chan;
      this.rread = false;
    }
  }
  
  public void select(Channel chan) {
    this.prevsel = this.sel;
    this.sel = chan;
    if (Config.chat_expanded) {
      if (this.prevsel != null)
        this.prevsel.hide(); 
      this.sel.show();
      resize(this.sz);
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
  
  public void drawsmall(GOut g, Coord br, int h) {
    Coord c;
    if (this.qline != null) {
      if (this.rqline == null || !this.rqline.text.equals(this.qline.line)) {
        String pre = String.format("%s> ", new Object[] { this.qline.chan.name() });
        this.rqline = qfnd.render(pre + this.qline.line);
        this.rqpre = pre.length();
      } 
      c = br.sub(0, 8 + basesize);
      g.chcolor(24, 24, 16, 200);
      g.frect(c, this.rqline.tex().sz());
      g.chcolor();
      g.image(this.rqline.tex(), c);
      int lx = this.rqline.advance(this.qline.point + this.rqpre);
      g.line(new Coord(br.x + lx + 1, br.y - 18), new Coord(br.x + lx + 1, br.y - 6), 1.0D);
    } else {
      c = br.sub(0, 5);
    } 
    long now = System.currentTimeMillis();
    synchronized (this.notifs) {
      for (Iterator<Notification> i = this.notifs.iterator(); i.hasNext(); ) {
        Notification n = i.next();
        if (now - n.time > 5000L) {
          i.remove();
          continue;
        } 
        if ((c.y -= (n.msg.sz()).y) < br.y - h)
          break; 
        g.chcolor(24, 24, 16, 200);
        g.frect(c, n.chnm.tex().sz().add((n.msg.tex().sz()).x + selw, 0));
        g.chcolor();
        g.image(n.chnm.tex(), c, br.sub(0, h), br.add(selw - 10, 0));
        g.image(n.msg.tex(), c.add(selw, 0));
      } 
    } 
  }
  
  public static final Resource notifsfx = Resource.load("sfx/tick");
  
  public void notify(Channel chan, Channel.Message msg) {
    synchronized (this.notifs) {
      this.notifs.addFirst(new Notification(chan, msg));
    } 
    Audio.play(notifsfx);
  }
  
  public void newchild(Widget w) {
    if (w instanceof Channel) {
      Channel chan = (Channel)w;
      this.chansel.add(chan);
      if (!PrivChat.class.isInstance(chan))
        select(chan); 
      if (!Config.chat_expanded)
        chan.hide(); 
    } 
  }
  
  public void cdestroy(Widget w) {
    if (w instanceof Channel) {
      Channel chan = (Channel)w;
      if (chan == this.sel)
        this.sel = null; 
      this.chansel.rm(chan);
    } 
  }
  
  public void resize(Coord sz) {
    super.resize(sz);
    this.c = this.base.add(0, -this.sz.y);
    this.chansel.resize(new Coord(selw, this.sz.y));
    if (this.sel != null)
      this.sel.resize(new Coord(this.sz.x - selw, this.sz.y)); 
  }
  
  public void resize(int w) {
    resize(new Coord(w, this.sz.y));
  }
  
  public void move(Coord base) {
    this.c = (this.base = base).add(0, -this.sz.y);
  }
  
  public void expand() {
    if (Config.chat_expanded)
      return; 
    resize(new Coord(this.sz.x, 100 + (basesize - 12) * 24));
    Utils.setprefb("chat_expanded", Config.chat_expanded = true);
    update_visibility();
  }
  
  public void contract() {
    if (!Config.chat_expanded)
      return; 
    resize(new Coord(this.sz.x, 50));
    Utils.setprefb("chat_expanded", Config.chat_expanded = false);
    update_visibility();
  }
  
  public void update_visibility() {
    if (Config.chat_expanded) {
      setcanfocus(true);
      if (this.sel != null)
        this.sel.show(); 
      this.chansel.show();
    } else {
      setcanfocus(false);
      if (this.sel != null)
        this.sel.hide(); 
      this.chansel.hide();
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
    
    protected void done(String line) {
      if (line.length() > 0)
        this.chan.send(line); 
      cancel();
    }
    
    public boolean key(char c, int code, int mod) {
      if (c == '\033') {
        cancel();
      } else {
        return super.key(c, code, mod);
      } 
      return true;
    }
  }
  
  public boolean keydown(KeyEvent ev) {
    boolean M = ((ev.getModifiersEx() & 0x300) != 0);
    if (this.qline != null) {
      if (M && ev.getKeyCode() == 38) {
        Channel prev = this.sel;
        do {
        
        } while (this.chansel.up() && 
          !(this.sel instanceof EntryChannel));
        if (!(this.sel instanceof EntryChannel)) {
          select(prev);
          return true;
        } 
        this.qline = new QuickLine((EntryChannel)this.sel);
        return true;
      } 
      if (M && ev.getKeyCode() == 40) {
        Channel prev = this.sel;
        do {
        
        } while (this.chansel.down() && 
          !(this.sel instanceof EntryChannel));
        if (!(this.sel instanceof EntryChannel)) {
          select(prev);
          return true;
        } 
        this.qline = new QuickLine((EntryChannel)this.sel);
        return true;
      } 
      this.qline.key(ev);
      return true;
    } 
    if (M && ev.getKeyCode() == 38) {
      this.chansel.up();
      return true;
    } 
    if (M && ev.getKeyCode() == 40) {
      this.chansel.down();
      return true;
    } 
    return super.keydown(ev);
  }
  
  public void toggle() {
    if (!Config.chat_expanded) {
      expand();
      this.parent.setfocus(this);
    } else if (this.hasfocus) {
      if (this.sz.y == 100 + (basesize - 12) * 24) {
        resize(new Coord(this.sz.x, 300 + (basesize - 12) * 48));
      } else {
        contract();
      } 
    } else {
      this.parent.setfocus(this);
    } 
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (this.qline != null) {
      this.qline.key(ev);
      return true;
    } 
    return super.type(key, ev);
  }
  
  public boolean globtype(char key, KeyEvent ev) {
    if (key == '\n' && 
      !Config.chat_expanded && this.sel instanceof EntryChannel) {
      this.ui.grabkeys(this);
      this.qline = new QuickLine((EntryChannel)this.sel);
      return true;
    } 
    return super.globtype(key, ev);
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
              ResDrawable d = gob.<ResDrawable>getattr(ResDrawable.class);
              if (d != null)
                try {
                  UI.instance.gui.message("Highlighted object: " + ((Resource)d.res.get()).name, GameUI.MsgType.INFO);
                } catch (Exception exception) {} 
            } 
          } catch (NumberFormatException numberFormatException) {}
          return null;
        } 
      } 
    } catch (Exception exception) {}
    return text;
  }
  
  public static void switchColumCount() {
    UI.instance.gui.chat.chansel.s = 0;
    UI.instance.gui.chat.select(((Selector.DarkChannel)UI.instance.gui.chat.chansel.chls.get(0)).chan);
    if (Config.two_chat_columns) {
      i100 = 200;
    } else {
      i100 = 100;
    } 
    UI.instance.gui.chat.setbasesize(basesize);
  }
  
  public static void toggleLogChannel() {
    if (UI.instance.gui.xlog == null) {
      UI.instance.gui.xlog = new Log(UI.instance.gui.chat, "Log");
    } else {
      UI.instance.gui.chat.chansel.rm(UI.instance.gui.xlog);
      UI.instance.gui.xlog = null;
    } 
  }
  
  public static String getName(int ID) {
    BuddyWnd.Buddy b = UI.instance.gui.buddies.find(ID);
    if (b == null)
      return "???"; 
    return b.name;
  }
  
  public static boolean isIgnored(String name) {
    return name.contains("[ignored]");
  }
}
