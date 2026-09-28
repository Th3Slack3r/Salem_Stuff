package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Party {
   Map<Long, Party.Member> memb = new TreeMap<>();
   Party.Member leader = null;
   public static final int PD_LIST = 0;
   public static final int PD_LEADER = 1;
   public static final int PD_MEMBER = 2;
   private Glob glob;

   public Party(Glob glob) {
      this.glob = glob;
   }

   public void msg(Message msg) {
      while (!msg.eom()) {
         int type = msg.uint8();
         if (type != 0) {
            if (type == 1) {
               Party.Member m = this.memb.get((long)msg.int32());
               if (m != null) {
                  this.leader = m;
               }
            } else if (type == 2) {
               Party.Member m = this.memb.get((long)msg.int32());
               Coord c = null;
               boolean vis = msg.uint8() == 1;
               if (vis) {
                  c = msg.coord();
               }

               Color col = msg.color();
               if (m != null) {
                  m.c = c;
                  m.col = col;
               }
            }
         } else {
            ArrayList<Long> ids = new ArrayList<>();

            while (true) {
               long id = msg.int32();
               if (id < 0L) {
                  Map<Long, Party.Member> nmemb = new TreeMap<>();

                  for (long idx : ids) {
                     Party.Member mx = this.memb.get(idx);
                     if (mx == null) {
                        mx = new Party.Member();
                        mx.gobid = idx;
                     }

                     nmemb.put(idx, mx);
                  }

                  long lid = this.leader == null ? -1L : this.leader.gobid;
                  this.memb = nmemb;
                  this.leader = this.memb.get(lid);
                  break;
               }

               ids.add(id);
            }
         }
      }
   }

   public class Member {
      long gobid;
      private Coord c = null;
      Color col = Color.BLACK;

      public Gob getgob() {
         return Party.this.glob.oc.getgob(this.gobid);
      }

      public Coord getc() {
         try {
            Gob gob;
            if ((gob = this.getgob()) != null) {
               return new Coord(gob.getc());
            }
         } catch (Loading var3) {
         }

         return this.c;
      }
   }
}
