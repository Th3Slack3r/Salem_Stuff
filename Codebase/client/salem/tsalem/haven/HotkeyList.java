package haven;

import java.awt.Font;
import java.util.ArrayList;

public class HotkeyList extends Scrollport {
  public static HotkeyList instance = null;
  
  private final IBox box;
  
  public static boolean editMode = false;
  
  public static ArrayList<Integer> idOrder = new ArrayList<>();
  
  public static Integer getNextId() {
    for (Integer i = Integer.valueOf(0); i.intValue() < 100; integer1 = i, integer2 = i = Integer.valueOf(i.intValue() + 1)) {
      Integer integer1;
      Integer integer2;
      if (!idOrder.contains(i)) {
        idOrder.add(i);
        return i;
      } 
    } 
    return Integer.valueOf(0);
  }
  
  public HotkeyList(Coord c, Widget parent) {
    super(c, new Coord(610, 250), parent);
    editMode = false;
    this.box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
    int i = 0;
    Config.loadHotkeyList();
    if (Config.HOTKEYLIST == null || Config.HOTKEYLIST.size() < 1) {
      Config.HOTKEYLIST = new ArrayList<>();
      HotkeyJItem test1 = new HotkeyJItem("W", "act craft roastmeat", Integer.valueOf(0), true);
      test1.ctrl = true;
      Config.HOTKEYLIST.add(test1);
      HotkeyJItem test2 = new HotkeyJItem("W", "act craft roastfish", Integer.valueOf(1), false);
      test2.ctrl = true;
      Config.HOTKEYLIST.add(test2);
      HotkeyJItem test3 = new HotkeyJItem("X", "x", Integer.valueOf(2), true);
      Config.HOTKEYLIST.add(test3);
      HotkeyJItem test4 = new HotkeyJItem("H", "h", Integer.valueOf(3), true);
      test4.ctrl = true;
      Config.HOTKEYLIST.add(test4);
      HotkeyJItem test5 = new HotkeyJItem("Q", "x paginae/add/pickup", Integer.valueOf(4), true);
      Config.HOTKEYLIST.add(test5);
      HotkeyJItem test6 = new HotkeyJItem("R", "act atk bullrun", Integer.valueOf(5), true);
      Config.HOTKEYLIST.add(test6);
      Config.saveHotkeyListButNoUpdateUI();
    } 
    for (HotkeyJItem h : Config.HOTKEYLIST)
      new HotkeyListItem(new Coord(0, 25 * i++), h.getName(), h, this.cont); 
    update();
    instance = this;
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (msg.equals("changed")) {
      String name = (String)args[0];
      HotkeyJItem jItem = (HotkeyJItem)args[1];
      synchronized (Config.HOTKEYLIST) {
        Config.HOTKEYLIST.add(jItem);
      } 
      Config.saveHotkeyListButNoUpdateUI();
    } else if (msg.equals("delete")) {
      HotkeyListItem item = (HotkeyListItem)args[0];
      synchronized (Config.HOTKEYLIST) {
        idOrder.remove(item.jItem.id);
        Config.HOTKEYLIST.remove(item.jItem);
      } 
      for (HotkeyJItem hkji : Config.HOTKEYLIST) {
        idOrder.remove(hkji.id);
        hkji.id = getNextId();
      } 
      this.ui.destroy(sender);
      Config.saveHotkeyListAndUpdateUI();
    } else if (msg.endsWith("_on_off")) {
      Config.saveHotkeyListAndUpdateUI();
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void add(String name, HotkeyJItem item) {
    if (name != null && !name.isEmpty()) {
      synchronized (Config.HOTKEYLIST) {
        Config.HOTKEYLIST.add(item);
      } 
      new HotkeyListItem(new Coord(0, 0), name, item, this.cont);
      Config.saveHotkeyListAndUpdateUI();
    } 
  }
  
  public void add(String key, String com) {
    HotkeyJItem jItem = new HotkeyJItem(key, com, getNextId(), true);
    add(key + com, jItem);
  }
  
  public void update() {
    for (Widget wdg = this.cont.lchild; wdg != null; wdg = wdg.prev) {
      int i = ((HotkeyListItem)wdg).jItem.id.intValue();
      wdg.c.y = 25 * i;
    } 
    this.cont.update();
  }
  
  public void draw(GOut g) {
    super.draw(g);
    this.box.draw(g, Coord.z, this.sz);
  }
  
  public class HotkeyJItem {
    public String key = null;
    
    public String command = null;
    
    public Integer id = null;
    
    public boolean onOff = true;
    
    public boolean ctrl = false;
    
    public boolean shift = false;
    
    public boolean alt = false;
    
    public HotkeyJItem(String key, String command, Integer id, boolean onOff) {
      this.key = key;
      this.command = command;
      this.id = id;
      this.onOff = onOff;
    }
    
    public String getName() {
      return "key: " + this.key + " - command: " + this.command;
    }
  }
  
  public class HotkeyListItem extends Widget {
    HotkeyList.HotkeyJItem jItem = null;
    
    private final CheckBox cb;
    
    private boolean highlight = false;
    
    private boolean a = false;
    
    private IButton deleteButton = null;
    
    private Button editButton = null;
    
    private final TextEntry comText;
    
    private final CheckBox onOffBox = null;
    
    private final CheckBox ctrlBox = null;
    
    private final CheckBox shiftBox = null;
    
    private final CheckBox altBox = null;
    
    public HotkeyListItem(Coord c, String name, HotkeyList.HotkeyJItem item, Widget parent) {
      super(c, new Coord(610, 25), parent);
      this.jItem = item;
      if (item.id == null) {
        this.jItem.id = HotkeyList.getNextId();
      } else if (!HotkeyList.idOrder.contains(item.id)) {
        HotkeyList.idOrder.add(item.id);
      } 
      boolean addThis = true;
      for (HotkeyList.HotkeyJItem hkji : Config.HOTKEYLIST) {
        if (this.jItem.id.equals(hkji.id))
          addThis = false; 
      } 
      if (addThis)
        Config.HOTKEYLIST.add(this.jItem); 
      final Label onOffLabel = new Label(new Coord(22, 3), this, (this.jItem.onOff == true) ? "ON" : "OFF", new Text.Foundry(new Font("SansSerif", 0, 12)));
      this.cb = new CheckBox(new Coord(3, 3), this, "") {
          public void changed(boolean val) {
            HotkeyList.HotkeyListItem.this.jItem.onOff = val;
            onOffLabel.settext((val == true) ? "ON" : "OFF");
            Config.saveHotkeyListAndUpdateUI();
          }
        };
      Label keyLabel = new Label(new Coord(60, 3), this, "Key:", new Text.Foundry(new Font("SansSerif", 0, 12)));
      final Label keyValueLabel = new Label(new Coord(90, 3), this, this.jItem.key, new Text.Foundry(new Font("SansSerif", 1, 12)));
      final TextEntry keyText = new TextEntry(new Coord(90, 3), 20, this, this.jItem.key) {
          protected void changed() {
            if (!HotkeyList.editMode) {
              settext(HotkeyList.HotkeyListItem.this.jItem.key);
              setfocus(OptWnd2.hotkey_Key);
              return;
            } 
            if (this.text.trim().length() > 0 && !this.text.trim().equals(" ")) {
              String t = this.text.toUpperCase().trim();
              if (t.length() > 1)
                t = t.substring(t.length() - 1, t.length()); 
              settext(t);
              keyValueLabel.settext(t);
              HotkeyList.HotkeyListItem.this.jItem.key = t;
              Config.saveHotkeyListButNoUpdateUI();
            } else {
              settext("");
              keyValueLabel.settext("");
              HotkeyList.HotkeyListItem.this.jItem.key = "";
              Config.saveHotkeyListButNoUpdateUI();
            } 
            super.changed();
          }
          
          public void activate(String text) {
            HotkeyList.HotkeyListItem.this.jItem.key = text;
            Config.saveHotkeyListButNoUpdateUI();
            setfocus(HotkeyList.HotkeyListItem.this.comText);
            super.activate(text);
          }
        };
      Label comLabel = new Label(new Coord(120, 3), this, "Command:", new Text.Foundry(new Font("SansSerif", 0, 12)));
      final Label comValueLabel = new Label(new Coord(185, 3), this, this.jItem.command, new Text.Foundry(new Font("SansSerif", 1, 12)));
      this.comText = new TextEntry(new Coord(185, 3), 210, this, this.jItem.command) {
          protected void changed() {
            if (!HotkeyList.editMode) {
              settext(HotkeyList.HotkeyListItem.this.jItem.command);
              setfocus(OptWnd2.hotkey_Key);
              return;
            } 
            String t = this.text.trim();
            comValueLabel.settext(t);
            HotkeyList.HotkeyListItem.this.jItem.command = t;
            Config.saveHotkeyListButNoUpdateUI();
            super.changed();
          }
          
          public void activate(String text) {
            HotkeyList.HotkeyListItem.this.jItem.command = text;
            HotkeyList.HotkeyListItem.this.editButton.click();
            super.activate(text);
          }
        };
      this.cb.a = this.jItem.onOff;
      this.cb.canactivate = true;
      this.deleteButton = new IButton(new Coord(588, 5), this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]);
      (new CheckBox(new Coord(400, 3), this, "CTRL") {
          public void changed(boolean val) {
            HotkeyList.HotkeyListItem.this.jItem.ctrl = val;
            Config.saveHotkeyListAndUpdateUI();
          }
        }).a = this.jItem.ctrl;
      (new CheckBox(new Coord(450, 3), this, "ALT") {
          public void changed(boolean val) {
            HotkeyList.HotkeyListItem.this.jItem.alt = val;
            Config.saveHotkeyListAndUpdateUI();
          }
        }).a = this.jItem.alt;
      (new CheckBox(new Coord(500, 3), this, "SHIFT") {
          public void changed(boolean val) {
            HotkeyList.HotkeyListItem.this.jItem.shift = val;
            Config.saveHotkeyListAndUpdateUI();
          }
        }).a = this.jItem.shift;
      this.editButton = new Button(new Coord(555, 2), Integer.valueOf(30), this, "Edit") {
          public void click() {
            HotkeyList.editMode = !HotkeyList.editMode;
            HotkeyList.HotkeyListItem.this.comText.visible = !HotkeyList.HotkeyListItem.this.comText.visible;
            keyText.visible = !keyText.visible;
            comValueLabel.visible = !comValueLabel.visible;
            keyValueLabel.visible = !keyValueLabel.visible;
            if (HotkeyList.editMode) {
              setfocus(HotkeyList.HotkeyListItem.this.comText);
            } else {
              Config.saveHotkeyListAndUpdateUI();
            } 
          }
        };
      this.comText.visible = false;
      keyText.visible = false;
    }
    
    public void draw(GOut g) {
      if (this.highlight) {
        g.chcolor(255, 255, 0, 128);
        g.poly2(new Object[] { Coord.z, Listbox.selr, new Coord(0, this.sz.y), Listbox.selr, this.sz, Listbox.overr, new Coord(this.sz.x, 0), Listbox.overr });
        g.chcolor();
      } 
      super.draw(g);
    }
    
    public void mousemove(Coord c) {
      this.highlight = c.isect(Coord.z, this.sz);
      super.mousemove(c);
    }
    
    public boolean mousedown(Coord c, int button) {
      if (super.mousedown(c, button))
        return true; 
      if (button != 1)
        return false; 
      this.a = true;
      this.ui.grabmouse(this);
      return true;
    }
    
    public boolean mouseup(Coord c, int button) {
      if (this.a && button == 1) {
        this.a = false;
        this.ui.grabmouse(null);
        return true;
      } 
      return false;
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.deleteButton) {
        wdgmsg("delete", new Object[] { this });
      } else if (msg.equals("ch")) {
        wdgmsg("changed", new Object[] { this.jItem.key, args[0] });
      } else {
        super.wdgmsg(sender, msg, args);
      } 
    }
  }
}
