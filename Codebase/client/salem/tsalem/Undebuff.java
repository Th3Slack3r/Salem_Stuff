import haven.GobbleEventInfo;
import haven.Indir;
import haven.ItemInfo;
import haven.Resource;

public class Undebuff implements ItemInfo.InfoFactory {
  public ItemInfo build(ItemInfo.Owner owner, Object... params) {
    double m = ((Integer)params[2]).intValue() / 100.0D;
    Indir<Resource> res = (owner.glob()).sess.getres(((Integer)params[1]).intValue());
    int val = (int)Math.round(100.0D * m);
    return (ItemInfo)new GobbleEventInfo(owner, val, res);
  }
}
