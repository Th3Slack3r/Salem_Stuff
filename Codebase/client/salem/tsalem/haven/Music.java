package haven;

import java.util.ArrayList;
import java.util.List;

public class Music {
  public static double volume = 1.0D;
  
  private static Resource curres = null;
  
  private static boolean curloop;
  
  private static Audio.CS clip = null;
  
  static {
    volume = Double.parseDouble(Utils.getpref("bgmvol", "1.0"));
    Console.setscmd("bgm", new Console.Command() {
          public void run(Console cons, String[] args) {
            int i = 1;
            boolean loop = false;
            if (i < args.length) {
              String opt;
              while ((opt = args[i]).charAt(0) == '-') {
                i++;
                if (opt.equals("-l"))
                  loop = true; 
              } 
              String resnm = args[i++];
              int ver = -1;
              if (i < args.length)
                ver = Integer.parseInt(args[i++]); 
              Music.play(Resource.load(resnm, ver), loop);
            } else {
              Music.play(null, false);
            } 
          }
        });
    Console.setscmd("bgmvol", new Console.Command() {
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
    
    public int get(double[][] buf) {
      int ns = (buf[0]).length;
      int nch = buf.length;
      for (int i = 0; i < nch; i++) {
        for (int o = 0; o < ns; o++)
          buf[i][o] = 0.0D; 
      } 
      if (this.cur == null) {
        if (this.state == 2)
          return -1; 
        try {
          List<Resource.Audio> clips = new ArrayList<>(this.res.layers(Resource.audio));
          this.cur = new Audio.DataClip(((Resource.Audio)clips.get((int)(Math.random() * clips.size()))).pcmstream());
          if (this.state == 1)
            this.state = 2; 
        } catch (Loading l) {
          return ns;
        } 
      } 
      int ret = this.cur.get(buf);
      double vol = Music.volume;
      if (ret < 0) {
        this.cur = null;
      } else {
        for (int j = 0; j < nch; j++) {
          for (int o = 0; o < ret; o++)
            buf[j][o] = buf[j][o] * vol; 
        } 
      } 
      return ns;
    }
  }
  
  public static void play(Resource res, boolean loop) {
    synchronized (Music.class) {
      curres = res;
      curloop = loop;
      stop();
      if (volume >= 0.01D && res != null)
        Audio.play(clip = new Jukebox(res, loop)); 
    } 
  }
  
  private static void stop() {
    if (clip != null) {
      Audio.stop(clip);
      clip = null;
    } 
  }
  
  public static void setvolume(double vol) {
    synchronized (Music.class) {
      boolean off = (vol < 0.01D);
      boolean prevoff = (volume < 0.01D);
      volume = vol;
      Utils.setpref("bgmvol", Double.toString(volume));
      if (off && !prevoff) {
        stop();
      } else if (!off && prevoff) {
        play(curres, curloop);
      } 
    } 
  }
}
