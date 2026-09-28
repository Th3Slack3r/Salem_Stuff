package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GobbleInfo extends ItemInfo.Tip {
   public final int[] l;
   public final int[] h;
   public final int[] types;
   public final int ft;
   public final List<GobbleInfo.Event> evs;
   private static final Text.Line head = Text.render("When gobbled:");

   public GobbleInfo(ItemInfo.Owner owner, int[] l, int[] h, int[] types, int ft, List<GobbleInfo.Event> evs) {
      super(owner);
      this.l = l;
      this.h = h;
      this.types = types;
      this.ft = ft;

      for (GobbleInfo.Event ev : this.evs = evs) {
         ev.rinf = ItemInfo.longtip(ev.info);
         if (ev.p < 1.0) {
            ev.rp = RichText.render(String.format("[%d%%]", (int)Math.round(ev.p * 100.0)), Color.LIGHT_GRAY).img;
         }
      }
   }

   public int mainTemper() {
      int bit = 0;
      int value = 0;

      for (int i = 0; i < 4; i++) {
         int v = (this.l[i] + this.h[i]) / 2;
         if (v > value) {
            value = v;
         }
      }

      return value;
   }

   @Override
   public BufferedImage longtip() {
      StringBuilder buf = new StringBuilder();
      buf.append(String.format("Points: $b{%s : %s : %s : %s}\n", this.point(0), this.point(1), this.point(2), this.point(3)));
      int min = (this.ft + 30) / 60;
      buf.append(String.format("Full and Fed Up for %02d:%02d\n", min / 60, min % 60));
      BufferedImage gi = RichText.render(buf.toString(), 0).img;
      Coord sz = PUtils.imgsz(gi);

      for (GobbleInfo.Event ev : this.evs) {
         int w = ev.rinf.getWidth();
         if (ev.rp != null) {
            w += 5 + ev.rp.getWidth();
         }

         sz.x = Math.max(sz.x, w);
         sz.y = sz.y + ev.rinf.getHeight();
      }

      BufferedImage img = TexI.mkbuf(sz.add(10, head.sz().y + 2));
      Graphics g = img.getGraphics();
      int y = 0;
      g.drawImage(head.img, 0, y, null);
      y += head.sz().y + 2;
      g.drawImage(gi, 10, y, null);
      y += gi.getHeight();

      for (GobbleInfo.Event ev : this.evs) {
         g.drawImage(ev.rinf, 10, y, null);
         if (ev.rp != null) {
            g.drawImage(ev.rp, 10 + ev.rinf.getWidth() + 5, y, null);
         }

         y += ev.rinf.getHeight();
      }

      g.dispose();
      return img;
   }

   private String point(int i) {
      return String.format(
         "$col[%s]{%s} - $col[%s]{%s}", Tempers.tcolors[i], Utils.fpformat(this.l[i], 3, 1), Tempers.tcolors[i], Utils.fpformat(this.h[i], 3, 1)
      );
   }

   public static class Data implements ItemData.ITipData {
      public int[] l;
      public int[] h;
      public int ft;
      public List<GobbleInfo.Event> evs;

      public Data() {
      }

      public Data(GobbleInfo info, double mult) {
         if (mult == 1.0) {
            this.l = info.l;
            this.h = info.h;
         } else {
            this.l = FoodInfo.Data.fixMult(mult, info.l);
            this.h = FoodInfo.Data.fixMult(mult, info.h);
         }

         this.ft = info.ft;
         this.evs = info.evs;
      }

      @Override
      public ItemInfo.Tip create() {
         return new GobbleInfo(null, this.l, this.h, new int[0], this.ft, this.evs);
      }

      public static class DataAdapter extends TypeAdapter<GobbleInfo.Data> {
         public void write(JsonWriter writer, GobbleInfo.Data data) throws IOException {
            writer.beginObject();
            writer.name("fed-up_time").value((long)data.ft);
            writeArray(writer, "high", data.h);
            writeArray(writer, "low", data.l);
            writeEvents(writer, data.evs);
            writer.endObject();
         }

         public GobbleInfo.Data read(JsonReader reader) throws IOException {
            GobbleInfo.Data data = new GobbleInfo.Data();
            reader.beginObject();

            while (reader.hasNext()) {
               String name = reader.nextName();
               if (name.equals("fed-up_time")) {
                  data.ft = reader.nextInt();
               } else if (name.equals("low")) {
                  data.l = readArray(reader);
               } else if (name.equals("high")) {
                  data.h = readArray(reader);
               } else if (name.equals("events")) {
                  data.evs = this.readEvents(reader);
               }
            }

            reader.endObject();
            return data;
         }

         private List<GobbleInfo.Event> readEvents(JsonReader reader) throws IOException {
            List<GobbleInfo.Event> events = new LinkedList<>();
            reader.beginArray();

            while (reader.hasNext()) {
               reader.beginObject();
               double p = 0.0;
               int value = 0;
               String type = null;

               while (reader.hasNext()) {
                  String name = reader.nextName();
                  if (name.equals("chance")) {
                     p = reader.nextDouble();
                  } else if (name.equals("value")) {
                     value = reader.nextInt();
                  } else if (name.equals("type")) {
                     type = reader.nextString();
                  }
               }

               reader.endObject();
               Indir<Resource> res = Resource.load(type).indir();
               LinkedList<ItemInfo> itemInfos = new LinkedList<>();
               itemInfos.add(new GobbleEventInfo(null, value, res));
               events.add(new GobbleInfo.Event(itemInfos, p));
            }

            reader.endArray();
            return events;
         }

         private static void writeEvents(JsonWriter writer, List<GobbleInfo.Event> events) throws IOException {
            writer.name("events");
            writer.beginArray();

            for (GobbleInfo.Event event : events) {
               writer.beginObject();
               writer.name("chance").value(event.p);
               GobbleEventInfo info = (GobbleEventInfo)event.info.get(0);
               writer.name("value").value((long)info.value);
               writer.name("type").value(info.res.get().name);
               writer.endObject();
            }

            writer.endArray();
         }

         private static void writeArray(JsonWriter writer, String name, int[] values) throws IOException {
            writer.name(name);
            writer.beginArray();

            for (int h : values) {
               writer.value((long)h);
            }

            writer.endArray();
         }

         private static int[] readArray(JsonReader reader) throws IOException {
            List<Integer> tmp = new LinkedList<>();
            reader.beginArray();

            while (reader.hasNext()) {
               tmp.add(reader.nextInt());
            }

            reader.endArray();
            int[] values = new int[tmp.size()];

            for (int i = 0; i < values.length; i++) {
               values[i] = tmp.get(i);
            }

            return values;
         }
      }
   }

   public static class Event {
      public final List<ItemInfo> info;
      public final double p;
      private BufferedImage rinf;
      private BufferedImage rp;

      public Event(List<ItemInfo> info, double p) {
         this.info = info;
         this.p = p;
      }
   }
}
