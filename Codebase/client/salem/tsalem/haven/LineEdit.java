package haven;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineEdit {
  public String line = "";
  
  public int point = 0;
  
  private static Text tcache = null;
  
  private static final int C = 1;
  
  private static final int M = 2;
  
  public KeyHandler mode;
  
  public abstract class KeyHandler {
    public abstract boolean key(char param1Char, int param1Int1, int param1Int2);
  }
  
  public class PCMode extends KeyHandler {
    public String cliptext() {
      Clipboard c;
      if ((c = Toolkit.getDefaultToolkit().getSystemSelection()) != null)
        try {
          return (String)c.getData(DataFlavor.stringFlavor);
        } catch (IllegalStateException illegalStateException) {
        
        } catch (IOException iOException) {
        
        } catch (UnsupportedFlavorException unsupportedFlavorException) {} 
      if ((c = Toolkit.getDefaultToolkit().getSystemClipboard()) != null)
        try {
          return (String)c.getData(DataFlavor.stringFlavor);
        } catch (IllegalStateException illegalStateException) {
        
        } catch (IOException iOException) {
        
        } catch (UnsupportedFlavorException unsupportedFlavorException) {} 
      return "";
    }
    
    public boolean key(char c, int code, int mod) {
      if (c == '\b' && mod == 0) {
        if (LineEdit.this.point > 0) {
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point - 1) + LineEdit.this.line.substring(LineEdit.this.point);
          LineEdit.this.point--;
        } 
      } else if (c == '\b' && mod == 1) {
        int b = LineEdit.this.wordstart(LineEdit.this.point);
        LineEdit.this.line = LineEdit.this.line.substring(0, b) + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point = b;
      } else if (c == '\n') {
        LineEdit.this.done(LineEdit.this.line);
      } else if (c == '' && mod == 0) {
        if (LineEdit.this.point < LineEdit.this.line.length())
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + LineEdit.this.line.substring(LineEdit.this.point + 1); 
      } else if (c == '' && mod == 1) {
        int b = LineEdit.this.wordend(LineEdit.this.point);
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + LineEdit.this.line.substring(b);
      } else if (c >= ' ' && mod == 0) {
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + c + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point++;
      } else if ((c == 'v' || c == 'V') && mod == 1) {
        String str = Utils.getClipboard();
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + str + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point += str.length();
      } else if (code == 37 && mod == 0) {
        if (LineEdit.this.point > 0)
          LineEdit.this.point--; 
      } else if (code == 37 && mod == 1) {
        LineEdit.this.point = LineEdit.this.wordstart(LineEdit.this.point);
      } else if (code == 39 && mod == 0) {
        if (LineEdit.this.point < LineEdit.this.line.length())
          LineEdit.this.point++; 
      } else if (code == 39 && mod == 1) {
        LineEdit.this.point = LineEdit.this.wordend(LineEdit.this.point);
      } else if (code == 36 && mod == 0) {
        LineEdit.this.point = 0;
      } else if (code == 35 && mod == 0) {
        LineEdit.this.point = LineEdit.this.line.length();
      } else if (c == 'v' && mod == 1) {
        String cl = cliptext();
        for (int i = 0; i < cl.length(); i++) {
          if (cl.charAt(i) < ' ') {
            cl = cl.substring(0, i);
            break;
          } 
        } 
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + cl + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point += cl.length();
      } else {
        return false;
      } 
      return true;
    }
  }
  
  public class EmacsMode extends KeyHandler {
    private int mark;
    
    private int yankpos;
    
    private int undopos;
    
    private String last;
    
    private final List<String> yanklist;
    
    private final List<UndoState> undolist;
    
    private String lastsel;
    
    private String lastclip;
    
    public EmacsMode() {
      this.last = "";
      this.yanklist = new ArrayList<>();
      this.undolist = new ArrayList<>();
      this.undolist.add(new UndoState());
      this.lastsel = "";
      this.lastclip = "";
    }
    
    private class UndoState {
      private UndoState() {}
      
      private final String line = LineEdit.this.line;
      
      private final int point = LineEdit.this.point;
    }
    
    private void save() {
      if (!(this.undolist.get(this.undolist.size() - 1)).line.equals(LineEdit.this.line))
        this.undolist.add(new UndoState()); 
    }
    
    private void mode(String mode) {
      if (mode == "" || this.last != mode)
        save(); 
      this.last = mode;
    }
    
    private void killclipboard() {
      String cl;
      if (!(cl = cliptext(Toolkit.getDefaultToolkit().getSystemSelection())).equals(this.lastsel)) {
        this.lastsel = cl;
        kill(cl);
        return;
      } 
      if (!(cl = cliptext(Toolkit.getDefaultToolkit().getSystemClipboard())).equals(this.lastclip)) {
        this.lastclip = cl;
        kill(cl);
        return;
      } 
    }
    
    private void kill(String text) {
      killclipboard();
      this.yanklist.add(text);
    }
    
    private String cliptext(Clipboard c) {
      if (c == null)
        return ""; 
      try {
        return (String)c.getData(DataFlavor.stringFlavor);
      } catch (IllegalStateException illegalStateException) {
      
      } catch (IOException iOException) {
      
      } catch (UnsupportedFlavorException unsupportedFlavorException) {}
      return "";
    }
    
    public boolean key(char c, int code, int mod) {
      if (this.mark > LineEdit.this.line.length())
        this.mark = LineEdit.this.line.length(); 
      String last = this.last;
      if (c == '\b' && mod == 0) {
        mode("erase");
        if (LineEdit.this.point > 0) {
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point - 1) + LineEdit.this.line.substring(LineEdit.this.point);
          LineEdit.this.point--;
        } 
      } else if (c == '\b' && (mod == 1 || mod == 2)) {
        mode("backward-kill-word");
        save();
        int b = LineEdit.this.wordstart(LineEdit.this.point);
        if (last == "backward-kill-word") {
          this.yanklist.set(this.yanklist.size() - 1, LineEdit.this.line.substring(b, LineEdit.this.point) + (String)this.yanklist.get(this.yanklist.size() - 1));
        } else {
          kill(LineEdit.this.line.substring(b, LineEdit.this.point));
        } 
        LineEdit.this.line = LineEdit.this.line.substring(0, b) + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point = b;
      } else if (c == '\n') {
        LineEdit.this.done(LineEdit.this.line);
      } else if (c == 'd' && mod == 1) {
        mode("erase");
        if (LineEdit.this.point < LineEdit.this.line.length())
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + LineEdit.this.line.substring(LineEdit.this.point + 1); 
      } else if (c == 'd' && mod == 2) {
        mode("kill-word");
        save();
        int b = LineEdit.this.wordend(LineEdit.this.point);
        if (last == "kill-word") {
          this.yanklist.set(this.yanklist.size() - 1, (String)this.yanklist.get(this.yanklist.size() - 1) + LineEdit.this.line.substring(LineEdit.this.point, b));
        } else {
          kill(LineEdit.this.line.substring(LineEdit.this.point, b));
        } 
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + LineEdit.this.line.substring(b);
      } else if (c == 'b' && mod == 1) {
        mode("move");
        if (LineEdit.this.point > 0)
          LineEdit.this.point--; 
      } else if (c == 'b' && mod == 2) {
        mode("move");
        LineEdit.this.point = LineEdit.this.wordstart(LineEdit.this.point);
      } else if (c == 'f' && mod == 1) {
        mode("move");
        if (LineEdit.this.point < LineEdit.this.line.length())
          LineEdit.this.point++; 
      } else if (c == 'f' && mod == 2) {
        mode("move");
        LineEdit.this.point = LineEdit.this.wordend(LineEdit.this.point);
      } else if (c == 'a' && mod == 1) {
        mode("move");
        LineEdit.this.point = 0;
      } else if (c == 'e' && mod == 1) {
        mode("move");
        LineEdit.this.point = LineEdit.this.line.length();
      } else if (c == 't' && mod == 1) {
        mode("transpose");
        if (LineEdit.this.line.length() >= 2 && LineEdit.this.point > 0)
          if (LineEdit.this.point < LineEdit.this.line.length()) {
            LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point - 1) + LineEdit.this.line.charAt(LineEdit.this.point) + LineEdit.this.line.charAt(LineEdit.this.point - 1) + LineEdit.this.line.substring(LineEdit.this.point + 1);
            LineEdit.this.point++;
          } else {
            LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point - 2) + LineEdit.this.line.charAt(LineEdit.this.point - 1) + LineEdit.this.line.charAt(LineEdit.this.point - 2);
          }  
      } else if (c == 'k' && mod == 1) {
        mode("");
        kill(LineEdit.this.line.substring(LineEdit.this.point));
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point);
      } else if (c == 'w' && mod == 2) {
        mode("");
        if (this.mark < LineEdit.this.point) {
          kill(LineEdit.this.line.substring(this.mark, LineEdit.this.point));
        } else {
          kill(LineEdit.this.line.substring(LineEdit.this.point, this.mark));
        } 
      } else if (c == 'w' && mod == 1) {
        mode("");
        if (this.mark < LineEdit.this.point) {
          kill(LineEdit.this.line.substring(this.mark, LineEdit.this.point));
          LineEdit.this.line = LineEdit.this.line.substring(0, this.mark) + LineEdit.this.line.substring(LineEdit.this.point);
        } else {
          kill(LineEdit.this.line.substring(LineEdit.this.point, this.mark));
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + LineEdit.this.line.substring(this.mark);
        } 
      } else if (c == 'y' && mod == 1) {
        mode("yank");
        save();
        killclipboard();
        this.yankpos = this.yanklist.size();
        if (this.yankpos > 0) {
          String yank = this.yanklist.get(--this.yankpos);
          this.mark = LineEdit.this.point;
          LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + yank + LineEdit.this.line.substring(LineEdit.this.point);
          LineEdit.this.point = this.mark + yank.length();
        } 
      } else if (c == 'y' && mod == 2) {
        mode("yank");
        save();
        if (last == "yank" && this.yankpos > 0) {
          String yank = this.yanklist.get(--this.yankpos);
          LineEdit.this.line = LineEdit.this.line.substring(0, this.mark) + yank + LineEdit.this.line.substring(LineEdit.this.point);
          LineEdit.this.point = this.mark + yank.length();
        } 
      } else if (c == ' ' && mod == 1) {
        mode("");
        this.mark = LineEdit.this.point;
      } else if (c == '_' && mod == 1) {
        mode("undo");
        save();
        if (last != "undo")
          this.undopos = this.undolist.size() - 1; 
        if (this.undopos > 0) {
          UndoState s = this.undolist.get(--this.undopos);
          LineEdit.this.line = s.line;
          LineEdit.this.point = s.point;
        } 
      } else if (c >= ' ' && mod == 0) {
        mode("type");
        LineEdit.this.line = LineEdit.this.line.substring(0, LineEdit.this.point) + c + LineEdit.this.line.substring(LineEdit.this.point);
        LineEdit.this.point++;
      } else {
        return false;
      } 
      return true;
    }
  }
  
  public LineEdit() {
    String mode = Utils.getpref("editmode", "pc");
    if (mode.equals("emacs")) {
      this.mode = new EmacsMode();
    } else {
      this.mode = new PCMode();
    } 
  }
  
  public LineEdit(String line) {
    this();
    this.line = line;
    this.point = line.length();
  }
  
  public void setline(String line) {
    String prev = this.line;
    this.line = line;
    if (this.point > line.length())
      this.point = line.length(); 
    if (!prev.equals(line))
      changed(); 
  }
  
  public boolean key(char c, int code, int mod) {
    String prev = this.line;
    boolean ret = this.mode.key(c, code, mod);
    if (!prev.equals(this.line))
      changed(); 
    return ret;
  }
  
  public boolean key(KeyEvent ev) {
    int mod = 0;
    if ((ev.getModifiersEx() & 0x80) != 0)
      mod |= 0x1; 
    if ((ev.getModifiersEx() & 0x300) != 0)
      mod |= 0x2; 
    if (ev.getID() == 400) {
      char c = ev.getKeyChar();
      if ((mod & 0x1) != 0 && c < ' ')
        if (ev.getKeyCode() != 8 && 
          ev.getKeyCode() != 10 && 
          ev.getKeyCode() != 9 && 
          ev.getKeyCode() != 27)
          if ((ev.getModifiersEx() & 0x40) != 0) {
            c = (char)(c + 65 - 1);
          } else {
            c = (char)(c + 97 - 1);
          }   
      return key(c, ev.getKeyCode(), mod);
    } 
    if (ev.getID() == 401 && 
      ev.getKeyChar() == Character.MAX_VALUE)
      return key(false, ev.getKeyCode(), mod); 
    return false;
  }
  
  private static boolean wordchar(char c) {
    return Character.isLetterOrDigit(c);
  }
  
  private int wordstart(int from) {
    while (from > 0 && !wordchar(this.line.charAt(from - 1)))
      from--; 
    while (from > 0 && wordchar(this.line.charAt(from - 1)))
      from--; 
    return from;
  }
  
  private int wordend(int from) {
    while (from < this.line.length() && !wordchar(this.line.charAt(from)))
      from++; 
    while (from < this.line.length() && wordchar(this.line.charAt(from)))
      from++; 
    return from;
  }
  
  protected void done(String line) {}
  
  protected void changed() {}
  
  public Text render(Text.Foundry f) {
    if (tcache == null || tcache.text != this.line)
      tcache = f.render(this.line); 
    return tcache;
  }
  
  static {
    Console.setscmd("editmode", new Console.Command() {
          public void run(Console cons, String[] args) {
            Utils.setpref("editmode", args[1]);
          }
        });
  }
}
