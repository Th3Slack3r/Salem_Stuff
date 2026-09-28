import haven.CheckBox;
import haven.Coord;
import haven.Widget;

class PermBox extends CheckBox {
  int fl;
  
  PermBox(Coord var2, Widget var3, String var4, int var5) {
    super(var2, var3, var4);
    this.fl = var5;
  }
  
  public void changed(boolean var1) {
    int var2 = 0;
    for (PermBox perm : Landwindow.this.perms) {
      if (perm.a)
        var2 |= perm.fl; 
    } 
    Landwindow.this.wdgmsg("shared", new Object[] { Integer.valueOf(this.this$0.group.group), Integer.valueOf(var2) });
  }
}
