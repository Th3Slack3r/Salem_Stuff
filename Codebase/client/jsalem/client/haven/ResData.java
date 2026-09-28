package haven;

import java.util.ArrayList;
import java.util.List;

public class ResData {
   public Indir<Resource> res;
   public Message sdt;

   public ResData(Indir<Resource> res, Message sdt) {
      this.res = res;
      this.sdt = sdt;
   }

   public static List<ResData> wrap(List<? extends Indir<Resource>> in) {
      List<ResData> ret = new ArrayList<>(in.size());

      for (Indir<Resource> res : in) {
         ret.add(new ResData(res, new Message(0, new byte[0])));
      }

      return ret;
   }

   public static ResData[] wrap(Indir<Resource>[] in) {
      ResData[] ret = new ResData[in.length];

      for (int i = 0; i < in.length; i++) {
         ret[i] = new ResData(in[i], Message.nil);
      }

      return ret;
   }
}
