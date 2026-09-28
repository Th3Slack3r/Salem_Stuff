package haven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class Audio {
   public static boolean enabled = true;
   private static Audio.Player player;
   public static final AudioFormat fmt = new AudioFormat(44100.0F, 16, 2, true, false);
   private static Collection<Audio.CS> ncl = new LinkedList<>();
   private static Object queuemon = new Object();
   private static Collection<Runnable> queue = new LinkedList<>();
   private static int bufsize = 32768;
   public static double volume = 1.0;

   public static void setvolume(double volume) {
      Audio.volume = volume;
      Utils.setpref("sfxvol", Double.toString(volume));
   }

   public static double[][] pcmi2f(byte[] pcm, int ch) {
      if (pcm.length % (ch * 2) != 0) {
         throw new IllegalArgumentException("Uneven samples in PCM data");
      } else {
         int sm = pcm.length / (ch * 2);
         double[][] ret = new double[ch][sm];
         int off = 0;

         for (int i = 0; i < sm; i++) {
            for (int o = 0; o < ch; o++) {
               int b1 = pcm[off++] & 255;
               int b2 = pcm[off++] & 255;
               int v = b1 + (b2 << 8);
               if (v >= 32768) {
                  v -= 65536;
               }

               ret[o][i] = v / 32768.0;
            }
         }

         return ret;
      }
   }

   private static synchronized void ckpl() {
      if (enabled) {
         if (player == null) {
            player = new Audio.Player();
            player.start();
         }
      } else {
         ncl.clear();
      }
   }

   public static void play(Audio.CS clip) {
      if (clip == null) {
         throw new NullPointerException();
      } else {
         synchronized (ncl) {
            ncl.add(clip);
         }

         ckpl();
      }
   }

   public static void stop(Audio.CS clip) {
      Audio.Player pl = player;
      if (pl != null) {
         pl.stop(clip);
      }
   }

   public static Audio.DataClip play(InputStream clip, double vol, double sp) {
      Audio.DataClip cs = new Audio.DataClip(clip, vol, sp);
      play(cs);
      return cs;
   }

   public static Audio.DataClip play(byte[] clip, double vol, double sp) {
      return play(new ByteArrayInputStream(clip), vol, sp);
   }

   public static Audio.DataClip play(byte[] clip) {
      return play(clip, 1.0, 1.0);
   }

   public static void queue(Runnable d) {
      synchronized (queuemon) {
         queue.add(d);
      }

      ckpl();
   }

   public static Audio.DataClip playres(Resource res) {
      Collection<Resource.Audio> clips = res.layers(Resource.audio);
      int s = (int)(Math.random() * clips.size());
      Resource.Audio clip = null;

      for (Resource.Audio cp : clips) {
         clip = cp;
         if (--s < 0) {
            break;
         }
      }

      return play(clip.pcmstream(), 1.0, 1.0);
   }

   public static void play(final Resource clip) {
      queue(new Runnable() {
         @Override
         public void run() {
            if (clip.loading) {
               Audio.queue.add(this);
            } else {
               Audio.playres(clip);
            }
         }
      });
   }

   public static void play(final Indir<Resource> clip) {
      queue(new Runnable() {
         @Override
         public void run() {
            try {
               Audio.playres(clip.get());
            } catch (Loading var2) {
               Audio.queue.add(this);
            }
         }
      });
   }

   public static byte[] readclip(InputStream in) throws IOException {
      AudioInputStream cs;
      try {
         cs = AudioSystem.getAudioInputStream(fmt, AudioSystem.getAudioInputStream(in));
      } catch (UnsupportedAudioFileException var5) {
         throw new IOException("Unsupported audio encoding");
      }

      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      byte[] bbuf = new byte[65536];

      while (true) {
         int rv = cs.read(bbuf);
         if (rv < 0) {
            return buf.toByteArray();
         }

         buf.write(bbuf, 0, rv);
      }
   }

   public static void main(String[] args) throws Exception {
      Collection<Audio.DataClip> clips = new LinkedList<>();

      for (int i = 0; i < args.length; i++) {
         if (args[i].equals("-b")) {
            bufsize = Integer.parseInt(args[++i]);
         } else {
            Audio.DataClip c = new Audio.DataClip(new FileInputStream(args[i]));
            clips.add(c);
         }
      }

      for (Audio.DataClip c : clips) {
         play(c);
      }

      for (Audio.DataClip c : clips) {
         c.finwait();
      }
   }

   static {
      volume = Double.parseDouble(Utils.getpref("sfxvol", "1.0"));
      Console.setscmd("sfx", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Audio.play(Resource.load(args[1]));
         }
      });
      Console.setscmd("sfxvol", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Audio.setvolume(Double.parseDouble(args[1]));
         }
      });
   }

   public interface CS {
      int get(double[][] var1);
   }

   public static class DataClip implements Audio.CS {
      public final int rate;
      public boolean eof;
      public double vol;
      public double sp;
      private InputStream clip;
      private final int trate;
      private int ack = 0;
      private final byte[] buf = new byte[256];
      private int dp = 0;
      private int dl = 0;

      public DataClip(InputStream clip, int rate, double vol, double sp) {
         this.clip = clip;
         this.rate = rate;
         this.vol = vol;
         this.sp = sp;
         this.trate = (int)Audio.fmt.getSampleRate();
      }

      public DataClip(InputStream clip, double vol, double sp) {
         this(clip, 44100, vol, sp);
      }

      public DataClip(InputStream clip) {
         this(clip, 1.0, 1.0);
      }

      public void finwait() throws InterruptedException {
         while (!this.eof) {
            synchronized (this) {
               this.wait();
            }
         }
      }

      protected void eof() {
         synchronized (this) {
            this.eof = true;
            this.notifyAll();
         }
      }

      @Override
      public int get(double[][] buf) {
         if (this.eof) {
            return -1;
         } else {
            try {
               for (int off = 0; off < buf[0].length; off++) {
                  for (this.ack = (int)(this.ack + this.rate * this.sp); this.ack >= this.trate; this.ack = this.ack - this.trate) {
                     if (this.dl - this.dp < 4) {
                        for (int i = 0; i < this.dl - this.dp; i++) {
                           this.buf[i] = this.buf[this.dp + i];
                        }

                        this.dl = this.dl - this.dp;

                        while (this.dl < 4) {
                           int ret = this.clip.read(this.buf, this.dl, this.buf.length - this.dl);
                           if (ret < 0) {
                              this.eof();
                              return off;
                           }

                           this.dl += ret;
                        }

                        this.dp = 0;
                     }

                     for (int i = 0; i < 2; i++) {
                        int b1 = this.buf[this.dp++] & 255;
                        int b2 = this.buf[this.dp++] & 255;
                        int v = b1 + (b2 << 8);
                        if (v >= 32768) {
                           v -= 65536;
                        }

                        buf[i][off] = v / 32768.0 * this.vol;
                     }
                  }
               }

               return buf[0].length;
            } catch (IOException var7) {
               this.eof();
               return -1;
            }
         }
      }
   }

   private static class Player extends HackThread {
      private Collection<Audio.CS> clips = new LinkedList<>();
      private int srate;
      private int nch = 2;

      Player() {
         super("Haven audio player");
         this.setDaemon(true);
         this.srate = (int)Audio.fmt.getSampleRate();
      }

      private void fillbuf(byte[] dst, int off, int len) {
         int ns = len / (2 * this.nch);
         double[][] val = new double[this.nch][ns];
         double[][] buf = new double[this.nch][ns];
         synchronized (this.clips) {
            Iterator<Audio.CS> i = this.clips.iterator();

            while (i.hasNext()) {
               int left = ns;
               Audio.CS cs = i.next();
               int boff = 0;

               while (left > 0) {
                  int ret = cs.get(buf);
                  if (ret < 0) {
                     i.remove();
                     break;
                  }

                  for (int ch = 0; ch < this.nch; ch++) {
                     for (int sm = 0; sm < ret; sm++) {
                        val[ch][sm + boff] = val[ch][sm + boff] + buf[ch][sm];
                     }
                  }

                  left -= ret;
               }
            }
         }

         for (int i = 0; i < ns; i++) {
            for (int o = 0; o < this.nch; o++) {
               int iv = (int)(val[o][i] * Audio.volume * 32767.0);
               if (iv < 0) {
                  if (iv < -32768) {
                     iv = -32768;
                  }

                  iv += 65536;
               } else if (iv > 32767) {
                  iv = 32767;
               }

               dst[off++] = (byte)(iv & 0xFF);
               dst[off++] = (byte)((iv & 0xFF00) >> 8);
            }
         }
      }

      public void stop(Audio.CS clip) {
         synchronized (this.clips) {
            Iterator<Audio.CS> i = this.clips.iterator();

            while (i.hasNext()) {
               if (i.next() == clip) {
                  i.remove();
                  return;
               }
            }
         }
      }

      @Override
      public void run() {
         SourceDataLine line = null;

         try {
            try {
               line = (SourceDataLine)AudioSystem.getLine(new Info(SourceDataLine.class, Audio.fmt));
               line.open(Audio.fmt, Audio.bufsize);
               line.start();
            } catch (Exception var27) {
               var27.printStackTrace();
               return;
            }

            byte[] buf = new byte[1024];

            while (!Thread.interrupted()) {
               synchronized (Audio.queuemon) {
                  Collection<Runnable> queue = Audio.queue;
                  Audio.queue = new LinkedList<>();

                  for (Runnable r : queue) {
                     r.run();
                  }
               }

               synchronized (Audio.ncl) {
                  synchronized (this.clips) {
                     for (Audio.CS cs : Audio.ncl) {
                        this.clips.add(cs);
                     }

                     Audio.ncl.clear();
                  }
               }

               this.fillbuf(buf, 0, 1024);
               int off = 0;

               while (off < buf.length) {
                  off += line.write(buf, off, buf.length - off);
               }
            }

            throw new InterruptedException();
         } catch (InterruptedException var28) {
         } finally {
            synchronized (Audio.class) {
               Audio.player = null;
            }

            if (line != null) {
               line.close();
            }
         }
      }
   }
}
