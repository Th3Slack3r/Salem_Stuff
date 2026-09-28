package haven;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CraftWnd extends Window implements DTarget2 {
  private static final int SZ = 20;
  
  private static final int PANEL_H = 24;
  
  private static final Coord WND_SZ = new Coord(635, 384);
  
  private static final Coord ICON_SZ = new Coord(20, 20);
  
  private static final Coord TEXT_POS = new Coord(22, 10);
  
  private RecipeListBox box;
  
  private Tex description;
  
  private Widget makewnd;
  
  private MenuGrid menu;
  
  private Breadcrumbs breadcrumbs;
  
  private static Glob.Pagina current = null;
  
  private ItemData data;
  
  private int dataidx;
  
  private Resource resd;
  
  private Glob.Pagina senduse = null;
  
  public CraftWnd(Coord c, Widget parent) {
    super(c, WND_SZ.add(0, 5), parent, "Craft window");
    this.ui.gui.craftwnd = this;
    init();
  }
  
  public void destroy() {
    current = null;
    this.box.destroy();
    super.destroy();
  }
  
  private void init() {
    this.box = new RecipeListBox(new Coord(0, 24), this, 200, (WND_SZ.y - 24) / 20) {
        protected void itemclick(Glob.Pagina item, int button) {
          if (button == 1) {
            if (item == CraftWnd.this.menu.bk) {
              item = CraftWnd.current;
              if (CraftWnd.this.getPaginaChildren(CraftWnd.current, null).size() == 0)
                item = CraftWnd.this.menu.getParent(item); 
              item = CraftWnd.this.menu.getParent(item);
            } 
            CraftWnd.this.menu.use(item);
          } 
        }
      };
    this.box.bgcolor = null;
    this.menu = this.ui.gui.menu;
    this.breadcrumbs = new Breadcrumbs(new Coord(0, -2), new Coord(WND_SZ.x, 20), this) {
        public void selected(Object data) {
          CraftWnd.this.select((Glob.Pagina)data, false);
        }
      };
    Glob.Pagina selected = this.menu.cur;
    if (selected == null || !this.menu.isCrafting(selected))
      selected = this.menu.CRAFT; 
    select(selected, true);
  }
  
  public void cdestroy(Widget w) {
    if (w == this.makewnd)
      this.makewnd = null; 
    super.cdestroy(w);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this && msg.equals("close")) {
      if (this.makewnd != null) {
        this.makewnd.wdgmsg("close", new Object[0]);
        this.makewnd = null;
      } 
      this.ui.destroy(this);
      this.ui.gui.craftwnd = null;
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  private List<Glob.Pagina> getPaginaChildren(Glob.Pagina parent, List<Glob.Pagina> buf) {
    if (buf == null)
      buf = new LinkedList<>(); 
    this.menu.cons(parent, buf);
    return buf;
  }
  
  public void select(Resource resource, boolean senduse) {
    select(paginafor(resource), senduse);
  }
  
  public void select(Glob.Pagina p, boolean senduse) {
    if (!this.menu.isCrafting(p))
      return; 
    if (this.box != null) {
      List<Glob.Pagina> children = getPaginaChildren(p, null);
      if (children.size() == 0) {
        children = getPaginaChildren(this.menu.getParent(p), null);
      } else {
        closemake();
      } 
      Collections.sort(children, MenuGrid.sorter);
      if (p != this.menu.CRAFT)
        children.add(0, this.menu.bk); 
      this.box.setitems(children);
      this.box.change(p);
      setCurrent(p);
    } 
    if (senduse)
      this.senduse = p; 
  }
  
  private void closemake() {
    if (this.makewnd != null)
      this.makewnd.wdgmsg("close", new Object[0]); 
    this.senduse = null;
  }
  
  public void cdraw(GOut g) {
    super.cdraw(g);
    if (this.senduse != null) {
      Glob.Pagina p = this.senduse;
      closemake();
      this.menu.senduse(p);
    } 
    drawDescription(g);
  }
  
  public void drawDescription(GOut g) {
    if (this.resd == null)
      return; 
    if (this.description == null)
      if (this.data != null) {
        try {
          this.description = this.data.longtip(this.resd, this.dataidx);
        } catch (Loading loading) {}
      } else {
        this.description = MenuGrid.rendertt(this.resd, true, false);
      }  
    if (this.description != null)
      g.image(this.description, new Coord(215, 24)); 
  }
  
  private void setCurrent(Glob.Pagina current) {
    CraftWnd.current = current;
    updateBreadcrumbs(current);
    updateDescription(current);
  }
  
  private void updateBreadcrumbs(Glob.Pagina p) {
    List<Breadcrumbs.Crumb> crumbs = new LinkedList<>();
    List<Glob.Pagina> parents = getParents(p);
    Collections.reverse(parents);
    for (Glob.Pagina item : parents) {
      BufferedImage img = ((Resource.Image)item.res().layer((Class)Resource.imgc)).img;
      Resource.AButton act = item.act();
      String name = "...";
      if (act != null)
        name = act.name; 
      crumbs.add(new Breadcrumbs.Crumb(img, name, item));
    } 
    this.breadcrumbs.setCrumbs(crumbs);
  }
  
  private List<Glob.Pagina> getParents(Glob.Pagina p) {
    List<Glob.Pagina> list = new LinkedList<>();
    if (getPaginaChildren(p, null).size() > 0)
      list.add(p); 
    Glob.Pagina parent;
    while ((parent = this.menu.getParent(p)) != null) {
      list.add(parent);
      p = parent;
    } 
    return list;
  }
  
  private void updateDescription(Glob.Pagina p) {
    if (this.description != null) {
      this.description.dispose();
      this.description = null;
    } 
    this.resd = p.res();
    this.data = ItemData.get(this.resd.name);
  }
  
  public void setMakewindow(Widget widget) {
    this.makewnd = widget;
  }
  
  public boolean drop(Coord cc, Coord ul, GItem item) {
    ItemData.actualize(item, this.box.sel);
    updateDescription(current);
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul, GItem item) {
    return false;
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (!super.mousewheel(c, amount) && 
      this.data != null) {
      if (amount > 0 && this.dataidx < this.data.data.size()) {
        this.dataidx++;
      } else if (amount < 0 && this.dataidx > 0) {
        this.dataidx--;
      } 
      updateDescription(current);
      return true;
    } 
    return false;
  }
  
  private Glob.Pagina paginafor(String name) {
    return paginafor(Resource.load(name));
  }
  
  private Glob.Pagina paginafor(Resource res) {
    return this.ui.sess.glob.paginafor(res);
  }
  
  private static class RecipeListBox extends Listbox<Glob.Pagina> {
    private List<Glob.Pagina> list;
    
    public RecipeListBox(Coord c, Widget parent, int w, int h) {
      super(c, parent, w, h, 20);
    }
    
    protected Glob.Pagina listitem(int i) {
      if (this.list == null)
        return null; 
      return this.list.get(i);
    }
    
    public void setitems(List<Glob.Pagina> list) {
      if (list.equals(this.list))
        return; 
      this.list = list;
      this.sb.max = listitems() - this.h;
      this.sb.val = 0;
    }
    
    public void change(Glob.Pagina item) {
      super.change(item);
      int k = this.list.indexOf(item);
      if (k >= 0) {
        if (k < this.sb.val)
          this.sb.val = k; 
        if (k >= this.sb.val + this.h)
          this.sb.val = Math.min(this.sb.max, k - this.h + 1); 
      } 
    }
    
    protected int listitems() {
      if (this.list == null)
        return 0; 
      return this.list.size();
    }
    
    protected void drawitem(GOut g, Glob.Pagina item) {
      if (item == null)
        return; 
      g.image(item.img.tex(), Coord.z, CraftWnd.ICON_SZ);
      Resource.AButton act = item.act();
      String name = "...";
      if (act != null) {
        name = act.name;
      } else if (item == this.ui.gui.menu.bk) {
        name = "Back";
      } 
      g.atext(name, CraftWnd.TEXT_POS, 0.0D, 0.5D);
    }
  }
}
