package haven.res.ui.tt.slots;

import haven.Indir;
import haven.ItemInfo;
import haven.Resource;
import java.util.LinkedList;

public class Fac implements ItemInfo.InfoFactory {
  public ItemInfo build(ItemInfo.Owner var1, Object... var2) {
    byte var3 = 1;
    int var15 = 2;
    int var4 = ((Integer)var2[1]).intValue();
    double var5 = ((Integer)var2[var15++]).intValue() / 100.0D;
    double var7 = ((Integer)var2[var15++]).intValue() / 100.0D;
    LinkedList<Object> var9 = new LinkedList();
    while (var2[var15] instanceof String)
      var9.add(var2[var15++]); 
    int var10 = ((Integer)var2[var15++]).intValue();
    ISlots var11;
    for (var11 = new ISlots(var1, var4, var5, var7, var9.<String>toArray(new String[0]), var10); var15 < var2.length; var10000[var12] = new SItem(var11, var13, var14)) {
      int var12 = ((Integer)var2[var15++]).intValue();
      Indir<Resource> var13 = (var1.glob()).sess.getres(((Integer)var2[var15++]).intValue());
      Object[] var14 = (Object[])var2[var15++];
      SItem[] var10000 = var11.s;
      var11.getClass();
    } 
    return (ItemInfo)var11;
  }
}
