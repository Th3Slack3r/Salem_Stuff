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
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Audio {
  public static boolean enabled = true;
  
  private static Player player;
  
  public static final AudioFormat fmt = new AudioFormat(44100.0F, 16, 2, true, false);
  
  private static Collection<CS> ncl = new LinkedList<>();
  
  private static Object queuemon = new Object();
  
  private static Collection<Runnable> queue = new LinkedList<>();
  
  private static int bufsize = 32768;
  
  public static double volume = 1.0D;
  
  static {
    volume = Double.parseDouble(Utils.getpref("sfxvol", "1.0"));
    Console.setscmd("sfx", new Console.Command() {
          public void run(Console cons, String[] args) {
            Audio.play(Resource.load(args[1]));
          }
        });
    Console.setscmd("sfxvol", new Console.Command() {
          public void run(Console cons, String[] args) {
            Audio.setvolume(Double.parseDouble(args[1]));
          }
        });
  }
  
  public static void setvolume(double volume) {
    Audio.volume = volume;
    Utils.setpref("sfxvol", Double.toString(volume));
  }
  
  public static class DataClip implements CS {
    public final int rate;
    
    public boolean eof;
    
    public double vol;
    
    public double sp;
    
    private final InputStream clip;
    
    private final int trate;
    
    private int ack = 0;
    
    private final byte[] buf = new byte[256];
    
    private int dp = 0, dl = 0;
    
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
      this(clip, 1.0D, 1.0D);
    }
    
    public void finwait() throws InterruptedException {
      while (!this.eof) {
        synchronized (this) {
          wait();
        } 
      } 
    }
    
    protected void eof() {
      synchronized (this) {
        this.eof = true;
        notifyAll();
      } 
    }
    
    public int get(double[][] buf) {
      if (this.eof)
        return -1; 
      try {
        for (int off = 0; off < (buf[0]).length; off++) {
          this.ack = (int)(this.ack + this.rate * this.sp);
          while (this.ack >= this.trate) {
            if (this.dl - this.dp < 4) {
              for (int j = 0; j < this.dl - this.dp; j++)
                this.buf[j] = this.buf[this.dp + j]; 
              this.dl -= this.dp;
              while (this.dl < 4) {
                int ret = this.clip.read(this.buf, this.dl, this.buf.length - this.dl);
                if (ret < 0) {
                  eof();
                  return off;
                } 
                this.dl += ret;
              } 
              this.dp = 0;
            } 
            for (int i = 0; i < 2; i++) {
              int b1 = this.buf[this.dp++] & 0xFF;
              int b2 = this.buf[this.dp++] & 0xFF;
              int v = b1 + (b2 << 8);
              if (v >= 32768)
                v -= 65536; 
              buf[i][off] = v / 32768.0D * this.vol;
            } 
            this.ack -= this.trate;
          } 
        } 
        return (buf[0]).length;
      } catch (IOException e) {
        eof();
        return -1;
      } 
    }
  }
  
  public static double[][] pcmi2f(byte[] pcm, int ch) {
    if (pcm.length % ch * 2 != 0)
      throw new IllegalArgumentException("Uneven samples in PCM data"); 
    int sm = pcm.length / ch * 2;
    double[][] ret = new double[ch][sm];
    int off = 0;
    for (int i = 0; i < sm; i++) {
      for (int o = 0; o < ch; o++) {
        int b1 = pcm[off++] & 0xFF;
        int b2 = pcm[off++] & 0xFF;
        int v = b1 + (b2 << 8);
        if (v >= 32768)
          v -= 65536; 
        ret[o][i] = v / 32768.0D;
      } 
    } 
    return ret;
  }
  
  private static class Player extends HackThread {
    private final Collection<Audio.CS> clips = new LinkedList<>();
    
    private final int srate;
    
    private final int nch = 2;
    
    Player() {
      super("Haven audio player");
      setDaemon(true);
      this.srate = (int)Audio.fmt.getSampleRate();
    }
    
    private void fillbuf(byte[] dst, int off, int len) {
      getClass();
      int ns = len / 2 * 2;
      getClass();
      double[][] val = new double[2][ns];
      getClass();
      double[][] buf = new double[2][ns];
      synchronized (this.clips) {
        for (Iterator<Audio.CS> iterator = this.clips.iterator(); iterator.hasNext(); ) {
          int left = ns;
          Audio.CS cs = iterator.next();
          int boff = 0;
          while (left > 0) {
            int ret = cs.get(buf);
            if (ret < 0) {
              iterator.remove();
              break;
            } 
            int ch = 0;
            getClass();
            for (; ch < 2; ch++) {
              for (int sm = 0; sm < ret; sm++)
                val[ch][sm + 0] = val[ch][sm + 0] + buf[ch][sm]; 
            } 
            left -= ret;
          } 
        } 
      } 
      for (int i = 0; i < ns; i++) {
        int o = 0;
        getClass();
        for (; o < 2; o++) {
          int iv = (int)(val[o][i] * Audio.volume * 32767.0D);
          if (iv < 0) {
            if (iv < -32768)
              iv = -32768; 
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
        for (Iterator<Audio.CS> i = this.clips.iterator(); i.hasNext();) {
          if (i.next() == clip) {
            i.remove();
            return;
          } 
        } 
      } 
    }
    
    public void run() {
      SourceDataLine line = null;
      try {
        try {
          line = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, Audio.fmt));
          line.open(Audio.fmt, Audio.bufsize);
          line.start();
        } catch (Exception e) {
          e.printStackTrace(System.out);
          return;
        } 
        byte[] buf = new byte[1024];
        while (true) {
          if (Thread.interrupted())
            throw new InterruptedException(); 
          synchronized (Audio.queuemon) {
            Collection<Runnable> queue = Audio.queue;
            Audio.queue = new LinkedList();
            for (Runnable r : queue)
              r.run(); 
          } 
          synchronized (Audio.ncl) {
            synchronized (this.clips) {
              for (Audio.CS cs : Audio.ncl)
                this.clips.add(cs); 
              Audio.ncl.clear();
            } 
          } 
          fillbuf(buf, 0, 1024);
          for (int off = 0; off < buf.length; off += line.write(buf, off, buf.length - off));
        } 
      } catch (InterruptedException interruptedException) {
      
      } finally {
        synchronized (Audio.class) {
          Audio.player = null;
        } 
        if (line != null)
          line.close(); 
      } 
    }
  }
  
  private static synchronized void ckpl() {
    if (enabled) {
      if (player == null) {
        player = new Player();
        player.start();
      } 
    } else {
      ncl.clear();
    } 
  }
  
  public static void play(CS clip) {
    if (clip == null)
      throw new NullPointerException(); 
    synchronized (ncl) {
      ncl.add(clip);
    } 
    ckpl();
  }
  
  public static void stop(CS clip) {
    Player pl = player;
    if (pl != null)
      pl.stop(clip); 
  }
  
  public static DataClip play(InputStream clip, double vol, double sp) {
    DataClip cs = new DataClip(clip, vol, sp);
    play(cs);
    return cs;
  }
  
  public static DataClip play(byte[] clip, double vol, double sp) {
    return play(new ByteArrayInputStream(clip), vol, sp);
  }
  
  public static DataClip play(byte[] clip) {
    return play(clip, 1.0D, 1.0D);
  }
  
  public static void queue(Runnable d) {
    synchronized (queuemon) {
      queue.add(d);
    } 
    ckpl();
  }
  
  public static DataClip playres(Resource res) {
    Collection<Resource.Audio> clips = res.layers(Resource.audio);
    int s = (int)(Math.random() * clips.size());
    Resource.Audio clip = null;
    for (Resource.Audio cp : clips) {
      clip = cp;
      if (--s < 0)
        break; 
    } 
    return play(clip.pcmstream(), 1.0D, 1.0D);
  }
  
  public static void play(final Resource clip) {
    queue(new Runnable() {
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
          public void run() {
            try {
              Audio.playres(clip.get());
            } catch (Loading e) {
              Audio.queue.add(this);
            } 
          }
        });
  }
  
  public static byte[] readclip(InputStream in) throws IOException {
    AudioInputStream cs;
    try {
      cs = AudioSystem.getAudioInputStream(fmt, AudioSystem.getAudioInputStream(in));
    } catch (UnsupportedAudioFileException e) {
      throw new IOException("Unsupported audio encoding");
    } 
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    byte[] bbuf = new byte[65536];
    while (true) {
      int rv = cs.read(bbuf);
      if (rv < 0)
        break; 
      buf.write(bbuf, 0, rv);
    } 
    return buf.toByteArray();
  }
  
  public static void main(String[] args) throws Exception {
    Collection<DataClip> clips = new LinkedList<>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-b")) {
        bufsize = Integer.parseInt(args[++i]);
      } else {
        DataClip c = new DataClip(new FileInputStream(args[i]));
        clips.add(c);
      } 
    } 
    for (DataClip c : clips)
      play(c); 
    for (DataClip c : clips)
      c.finwait(); 
  }
  
  public static interface CS {
    int get(double[][] param1ArrayOfdouble);
  }
}
