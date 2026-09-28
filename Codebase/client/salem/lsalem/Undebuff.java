import haven.GobbleEventInfo;
import haven.Indir;
import haven.ItemInfo;
import haven.Resource;

public class Undebuff implements ItemInfo.InfoFactory {
   @Override
   public ItemInfo build(ItemInfo.Owner owner, Object... params) {
      double m = ((Integer)params[2]).intValue() / 100.0;
      Indir<Resource> res = owner.glob().sess.getres((Integer)params[1]);
      int val = (int)Math.round(100.0 * m);
      return new GobbleEventInfo(owner, val, res);
   }
}
