package haven;

import java.util.LinkedList;

public class Fightview extends Widget {
   static int height = 5;
   static int iheight = 40;
   static int ymarg = 2;
   public static int width = 170;
   static Coord avasz = new Coord(36, 36);
   static Coord cavac = new Coord(width - Avaview.dasz.x - 10, 10);
   static Coord cgivec = new Coord(cavac.x - 70, cavac.y);
   LinkedList<Fightview.Relation> lsrel = new LinkedList<>();
   public Fightview.Relation current = null;
   public Indir<Resource> blk;
   public Indir<Resource> batk;
   public Indir<Resource> iatk;
   public long atkc = -1L;
   public int off;
   public int def;
   private GiveButton curgive;
   private FramedAva curava;

   public Fightview(Coord c, Widget parent) {
      super(c, new Coord(width, (iheight + ymarg) * height), parent);
   }

   private void setcur(Fightview.Relation rel) {
      if (this.current == null && rel != null) {
         this.curgive = new GiveButton(cgivec, this, 0) {
            @Override
            public void wdgmsg(String name, Object... args) {
               if (name == "click") {
                  Fightview.this.wdgmsg("give", new Object[]{(int)Fightview.this.current.gobid, args[0]});
               }
            }
         };
         this.curava = new FramedAva(cavac, Avaview.dasz, this, rel.gobid, "avacam") {
            @Override
            public void wdgmsg(String name, Object... args) {
               if (name == "click") {
                  Fightview.this.wdgmsg("click", new Object[]{(int)Fightview.this.current.gobid, args[0]});
               }
            }
         };
      } else if (this.current != null && rel == null) {
         this.ui.destroy(this.curgive);
         this.ui.destroy(this.curava);
         this.curgive = null;
         this.curava = null;
      } else if (this.current != null && rel != null) {
         this.curgive.state = rel.give.state;
         this.curava.view.avagob = rel.gobid;
      }

      this.current = rel;
   }

   @Override
   public void destroy() {
      this.setcur(null);
      super.destroy();
   }

   @Override
   public void draw(GOut g) {
      int y = 10;
      if (this.curava != null) {
         y = this.curava.c.y + this.curava.sz.y + 10;
      }

      int x = width - 90;

      for (Fightview.Relation rel : this.lsrel) {
         if (rel == this.current) {
            rel.show(false);
         } else {
            rel.ava.c = new Coord(x + 45, 4 + y);
            rel.give.c = new Coord(x + 5, 4 + y);
            rel.show(true);
            y += iheight + ymarg;
         }
      }

      super.draw(g);
   }

   private Fightview.Relation getrel(long gobid) {
      for (Fightview.Relation rel : this.lsrel) {
         if (rel.gobid == gobid) {
            return rel;
         }
      }

      throw new Fightview.Notfound(gobid);
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender instanceof FramedAva) {
         for (Fightview.Relation rel : this.lsrel) {
            if (rel.ava == sender) {
               this.wdgmsg("click", new Object[]{(int)rel.gobid, args[0]});
            }
         }
      } else if (sender instanceof GiveButton) {
         for (Fightview.Relation relx : this.lsrel) {
            if (relx.give == sender) {
               this.wdgmsg("give", new Object[]{(int)relx.gobid, args[0]});
            }
         }
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   private Indir<Resource> n2r(int num) {
      return num < 0 ? null : this.ui.sess.getres(num);
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "new") {
         Fightview.Relation rel = new Fightview.Relation(((Integer)args[0]).intValue());
         rel.give((Integer)args[1]);
         this.lsrel.addFirst(rel);
      } else if (msg == "del") {
         Fightview.Relation rel = this.getrel(((Integer)args[0]).intValue());
         rel.remove();
         this.lsrel.remove(rel);
         if (rel == this.current) {
            this.setcur(null);
         }
      } else if (msg == "upd") {
         Fightview.Relation rel = this.getrel(((Integer)args[0]).intValue());
         rel.give((Integer)args[1]);
      } else if (msg == "cur") {
         try {
            Fightview.Relation rel = this.getrel(((Integer)args[0]).intValue());
            this.lsrel.remove(rel);
            this.lsrel.addFirst(rel);
            this.setcur(rel);
         } catch (Fightview.Notfound var4) {
            this.setcur(null);
         }
      } else if (msg == "atkc") {
         this.atkc = System.currentTimeMillis() + (Integer)args[0] * 60;
      } else if (msg == "blk") {
         this.blk = this.n2r((Integer)args[0]);
      } else if (msg == "atk") {
         this.batk = this.n2r((Integer)args[0]);
         this.iatk = this.n2r((Integer)args[1]);
      } else if (msg == "offdef") {
         this.off = (Integer)args[0];
         this.def = (Integer)args[1];
      } else {
         super.uimsg(msg, args);
      }
   }

   @Widget.RName("frv")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Fightview(c, parent);
      }
   }

   public static class Notfound extends RuntimeException {
      public final long id;

      public Notfound(long id) {
         super("No relation for Gob ID " + id + " found");
         this.id = id;
      }
   }

   public class Relation {
      long gobid;
      FramedAva ava;
      GiveButton give;

      public Relation(long gobid) {
         this.gobid = gobid;
         this.ava = new FramedAva(Coord.z, Fightview.avasz, Fightview.this, gobid, "avacam");
         this.give = new GiveButton(Coord.z, Fightview.this, 0, new Coord(30, 30));
      }

      public void give(int state) {
         if (this == Fightview.this.current) {
            Fightview.this.curgive.state = state;
         }

         this.give.state = state;
      }

      public void show(boolean state) {
         this.ava.show(state);
         this.give.show(state);
      }

      public void remove() {
         Fightview.this.ui.destroy(this.ava);
         Fightview.this.ui.destroy(this.give);
      }
   }
}
