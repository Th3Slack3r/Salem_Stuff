package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;
import javax.media.opengl.GL2;

public class GobPath extends Sprite {
   private Moving move = null;
   private Gob gob;
   float d = 0.0F;

   public GobPath(Gob gob) {
      super(gob, null);
      this.gob = gob;
   }

   private String resname() {
      Drawable drawable = this.gob.getattr(Drawable.class);

      try {
         if (drawable != null && drawable instanceof Composite) {
            Composite composite = (Composite)drawable;
            return composite.base.get().name;
         }

         if (drawable != null && drawable instanceof ResDrawable) {
            ResDrawable resdraw = (ResDrawable)drawable;
            return resdraw.res.get().name;
         }
      } catch (Resource.Loading var3) {
      }

      return "<unknown>";
   }

   @Override
   public void draw(GOut g) {
      Coord t = this.target();
      if (t != null) {
         boolean good = false;
         Coord td = Coord.z;
         int tz = 0;

         try {
            Coord ss = new Coord((int)(t.x - this.gob.loc.c.x), (int)(t.y + this.gob.loc.c.y));
            td = ss.rotate(-this.gob.a);
            tz = (int)(this.gob.glob.map.getcz(t) - this.gob.glob.map.getcz(this.gob.rc)) + 1;
            good = true;
         } catch (Exception var7) {
         }

         if (good) {
            g.apply();
            GL2 gl = g.gl;
            gl.glLineWidth(3.0F);
            gl.glBegin(1);
            gl.glVertex3i(0, 0, 3);
            gl.glVertex3i(td.x, td.y, tz);
            gl.glEnd();
            GOut.checkerr(gl);
         }
      }
   }

   private Coord target() {
      Moving move = this.move();
      if (move != null) {
         Class<? extends GAttrib> aClass = (Class<? extends GAttrib>)move.getClass();
         if (aClass == LinMove.class) {
            return ((LinMove)move).t;
         }

         if (aClass == Homing.class) {
            return this.getGobCoords(((Homing)move).tgt());
         }

         if (aClass == Following.class) {
            return this.getGobCoords(((Following)move).tgt());
         }
      }

      return null;
   }

   private Coord getGobCoords(Gob gob) {
      if (gob != null) {
         Gob.GobLocation loc = gob.loc;
         if (loc != null) {
            Coord3f c = loc.c;
            if (c != null) {
               return new Coord((int)c.x, -((int)c.y));
            }
         }
      }

      return null;
   }

   @Override
   public boolean setup(RenderList list) {
      GobPath.Cfg cfg = Config.getGobPathCfg(this.resname());
      if (!cfg.show) {
         return false;
      } else {
         Color color = cfg.color;
         if (Config.gobpath_color) {
            KinInfo ki = this.gob.getattr(KinInfo.class);
            if (ki != null) {
               color = BuddyWnd.gc[ki.group];
            }
         }

         if (color == null) {
            color = GobPath.Cfg.def.color;
         }

         list.prepo(new States.ColState(color));
         return true;
      }
   }

   public synchronized Moving move() {
      return this.move;
   }

   public synchronized void move(Moving m) {
      this.move = m;
   }

   public synchronized void stop() {
      this.move = null;
   }

   public static class Cfg {
      public static GobPath.Cfg def = new GobPath.Cfg(Color.WHITE, true);
      public Color color;
      public boolean show;
      public String name;

      public Cfg(Color color, boolean show) {
         this.color = color;
         this.show = show;
      }

      public static Gson getGson() {
         GsonBuilder builder = new GsonBuilder();
         builder.setPrettyPrinting();
         builder.registerTypeAdapter(GobPath.Cfg.class, new GobPath.Cfg.Adapter().nullSafe());
         return builder.create();
      }

      public static class Adapter extends TypeAdapter<GobPath.Cfg> {
         public void write(JsonWriter writer, GobPath.Cfg cfg) throws IOException {
            writer.beginObject();
            writer.name("show").value(cfg.show);
            String color = Utils.color2hex(cfg.color);
            if (color != null) {
               writer.name("color").value(color);
            }

            if (cfg.name != null) {
               writer.name("name").value(cfg.name);
            }

            writer.endObject();
         }

         public GobPath.Cfg read(JsonReader reader) throws IOException {
            GobPath.Cfg cfg = new GobPath.Cfg(null, true);
            reader.beginObject();

            while (reader.hasNext()) {
               String name = reader.nextName();
               if (name.equals("show")) {
                  cfg.show = reader.nextBoolean();
               } else if (name.equals("color")) {
                  cfg.color = Utils.hex2color(reader.nextString(), null);
               } else if (name.equals("name")) {
                  cfg.name = reader.nextString();
               }
            }

            reader.endObject();
            return cfg;
         }
      }
   }
}
