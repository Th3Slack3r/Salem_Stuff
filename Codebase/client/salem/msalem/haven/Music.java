package haven;

import java.util.ArrayList;
import java.util.List;

public class Music {
   public static double volume = 1.0;
   private static Resource curres = null;
   private static boolean curloop;
   private static Audio.CS clip = null;

   public static void play(Resource res, boolean loop) {
      synchronized (Music.class) {
         if (volume < 0.01) {
            res = null;
         }

         if (clip != null) {
            Audio.stop(clip);
            clip = null;
         }

         if (res != null) {
            Audio.play(clip = new Music.Jukebox(res, loop));
            curres = res;
            curloop = loop;
         }
      }
   }

   public static void setvolume(double vol) {
      synchronized (Music.class) {
         boolean off = vol < 0.01;
         boolean prevoff = volume < 0.01;
         volume = vol;
         Utils.setpref("bgmvol", Double.toString(volume));
         if (off && !prevoff) {
            play(null, false);
         } else if (!off && prevoff) {
            play(curres, curloop);
         }
      }
   }

   static {
      volume = Double.parseDouble(Utils.getpref("bgmvol", "1.0"));
      Console.setscmd("bgm", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            int i = 1;
            boolean loop = false;
            if (i >= args.length) {
               Music.play(null, false);
            } else {
               String opt;
               while ((opt = args[i]).charAt(0) == '-') {
                  i++;
                  if (opt.equals("-l")) {
                     loop = true;
                  }
               }

               String resnm = args[i++];
               int ver = -1;
               if (i < args.length) {
                  ver = Integer.parseInt(args[i++]);
               }

               Music.play(Resource.load(resnm, ver), loop);
            }
         }
      });
      Console.setscmd("bgmvol", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Music.setvolume(Double.parseDouble(args[1]));
         }
      });
   }

   public static class Jukebox implements Audio.CS {
      public final Resource res;
      private int state;
      private Audio.DataClip cur = null;

      public Jukebox(Resource res, boolean loop) {
         this.res = res;
         this.state = loop ? 0 : 1;
      }

      @Override
      public int get(double[][] buf) {
         int ns = buf[0].length;
         int nch = buf.length;

         for (int i = 0; i < nch; i++) {
            for (int o = 0; o < ns; o++) {
               buf[i][o] = 0.0;
            }
         }

         if (this.cur != null) {
            int ret = this.cur.get(buf);
            double vol = Music.volume;
            if (ret < 0) {
               this.cur = null;
            } else {
               for (int i = 0; i < nch; i++) {
                  for (int o = 0; o < ret; o++) {
                     buf[i][o] = buf[i][o] * vol;
                  }
               }
            }

            return ns;
         } else if (this.state == 2) {
            return -1;
         } else {
            label29:
            try {
               List<Resource.Audio> clips = new ArrayList<>(this.res.layers(Resource.audio));
               this.cur = new Audio.DataClip(clips.get((int)(Math.random() * clips.size())).pcmstream());
               if (this.state == 1) {
                  this.state = 2;
               }
               break label29;
            } catch (Loading var9) {
               return ns;
            }
         }
      }
   }
}
