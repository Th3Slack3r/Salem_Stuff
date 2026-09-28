package haven;

public class Bufflist extends Widget {
   static final Tex frame = Resource.loadtex("gfx/hud/buffs/frame");
   static final Tex cframe = Resource.loadtex("gfx/hud/buffs/cframe");
   static final Tex ameter = Resource.loadtex("gfx/hud/buffs/cbar");
   static final Coord imgoff = new Coord(6, 6);
   static final Coord ameteroff = new Coord(4, 52);
   static final Coord cmeteroff = new Coord(20, 20);
   static final Coord cmeterul = new Coord(-20, -20);
   static final Coord cmeterbr = new Coord(20, 20);
   static final int margin = 2;
   static final int num = 15;
   private long hoverstart;
   private Tex shorttip;
   private Tex longtip;
   private String tipped;

   public Bufflist(Coord c, Widget parent) {
      super(c, new Coord(15 * frame.sz().x + 28, cframe.sz().y), parent);
   }

   @Override
   public void draw(GOut g) {
      int i = 0;
      int w = frame.sz().x + 2;
      long now = System.currentTimeMillis();
      synchronized (this.ui.sess.glob.buffs) {
         for (Buff b : this.ui.sess.glob.buffs.values()) {
            if (b.major) {
               Coord bc = new Coord(i * w, 0);
               if (b.ameter >= 0) {
                  g.image(cframe, bc);
                  g.image(ameter, bc.add(ameteroff), bc.add(ameteroff), new Coord(b.ameter * ameter.sz().x / 100, ameter.sz().y));
               } else {
                  g.image(frame, bc);
               }

               try {
                  Tex img = b.res.get().layer(Resource.imgc).tex();
                  g.image(img, bc.add(imgoff));
                  if (b.nmeter >= 0) {
                     Tex ntext = b.nmeter();
                     g.image(ntext, bc.add(imgoff).add(img.sz()).add(ntext.sz().inv()).add(-1, -1));
                  }

                  if (b.cmeter >= 0) {
                     double m = b.cmeter / 100.0;
                     if (b.cticks >= 0) {
                        double ot = b.cticks * 0.06;
                        double pt = (now - b.gettime) / 1000.0;
                        m *= (ot - pt) / ot;
                     }

                     m = Utils.clip(m, 0.0, 1.0);
                     g.chcolor(255, 255, 255, 128);
                     g.prect(bc.add(imgoff).add(cmeteroff), cmeterul, cmeterbr, (Math.PI * 2) * m);
                     g.chcolor();
                  }
               } catch (Loading var18) {
               }

               if (++i >= 15) {
                  break;
               }
            }
         }
      }
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      long now = System.currentTimeMillis();
      if (prev != this) {
         this.hoverstart = now;
      }

      int i = 0;
      int w = frame.sz().x + 2;
      synchronized (this.ui.sess.glob.buffs) {
         for (Buff b : this.ui.sess.glob.buffs.values()) {
            if (b.major) {
               Coord bc = new Coord(i * w, 0);
               if (c.isect(bc, frame.sz())) {
                  String tt = b.tooltip();
                  if (this.tipped != tt) {
                     this.shorttip = this.longtip = null;
                  }

                  this.tipped = tt;

                  Tex var10000;
                  try {
                     if (now - this.hoverstart < 1000L) {
                        if (this.shorttip == null) {
                           this.shorttip = Text.render(tt).tex();
                        }

                        return this.shorttip;
                     }

                     if (this.longtip == null) {
                        String text = RichText.Parser.quote(tt);
                        Resource.Pagina pag = b.res.get().layer(Resource.pagina);
                        if (pag != null) {
                           text = text + "\n\n" + pag.text;
                        }

                        this.longtip = RichText.render(text, 200).tex();
                     }

                     var10000 = this.longtip;
                  } catch (Loading var15) {
                     return "...";
                  }

                  return var10000;
               }

               if (++i >= 15) {
                  break;
               }
            }
         }

         return null;
      }
   }

   @Widget.RName("buffs")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Bufflist(c, parent);
      }
   }
}
