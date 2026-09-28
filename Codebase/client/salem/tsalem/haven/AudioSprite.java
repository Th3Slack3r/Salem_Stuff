package haven;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AudioSprite {
  private static long lastTCheck = 0L;
  
  private static boolean isMuted = false;
  
  private static double lastMusicVol = 0.0D;
  
  private static long lastSetTS = 0L;
  
  public static boolean wasInactive = false;
  
  private static boolean[] skip = new boolean[5];
  
  private static Map<RepeatSprite, Audio.CS> clipMap = new HashMap<>();
  
  private static RepeatSprite currentBGM = null;
  
  private static long lastCheck = 0L;
  
  public static final Map<Resource, Long> last_instances = new HashMap<>();
  
  public static final Sprite.Factory fact = new Sprite.Factory() {
      private Resource.Audio randoom(Resource res, String id) {
        List<Resource.Audio> cl = new ArrayList<>();
        for (Resource.Audio clip : res.<Resource.Audio>layers(Resource.audio)) {
          if (clip.id == id)
            cl.add(clip); 
        } 
        if (!cl.isEmpty())
          return cl.get((int)(Math.random() * cl.size())); 
        return null;
      }
      
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
        synchronized (AudioSprite.last_instances) {
          Long lasttime = AudioSprite.last_instances.get(res);
          Long now = Long.valueOf(System.currentTimeMillis());
          if (lasttime != null && 
            now.longValue() - lasttime.longValue() < 300L)
            throw new Loading("Too many sounds playing at once."); 
          AudioSprite.last_instances.put(res, now);
        } 
        Resource.Audio clip = randoom(res, "cl");
        if (clip != null)
          return new AudioSprite.ClipSprite(owner, res, clip); 
        clip = randoom(res, "rep");
        if (clip != null)
          return new AudioSprite.RepeatSprite(owner, res, randoom(res, "beg"), clip, randoom(res, "end")); 
        clip = res.<String, Resource.Audio>layer(Resource.audio, "amb");
        if (clip != null)
          return new AudioSprite.Ambience(owner, res); 
        return null;
      }
    };
  
  public static class ClipSprite extends Sprite {
    public final ActAudio.PosClip clip;
    
    private boolean done = false;
    
    public ClipSprite(Sprite.Owner owner, Resource res, Resource.Audio clip) {
      super(owner, res);
      AudioSprite.checkForEvents(res);
      if (!Config.mute_all_audio) {
        this.clip = new ActAudio.PosClip(new Audio.DataClip(clip.pcmstream()) {
              protected void eof() {
                super.eof();
                AudioSprite.ClipSprite.this.done = true;
              }
            });
      } else {
        this.clip = null;
        this.done = true;
      } 
    }
    
    public boolean setup(RenderList r) {
      if (!Config.mute_all_audio && this.clip != null) {
        r.add(this.clip, null);
      } else {
        this.done = true;
      } 
      return false;
    }
    
    public boolean tick(int dt) {
      return this.done;
    }
  }
  
  public static class RepeatSprite extends Sprite implements Gob.Overlay.CDel {
    private ActAudio.PosClip clip;
    
    private final Resource.Audio end;
    
    private final long t = System.currentTimeMillis();
    
    private boolean isBGM = false;
    
    public static long timeStamp = 0L;
    
    public RepeatSprite(Sprite.Owner owner, Resource res, final Resource.Audio beg, final Resource.Audio clip, Resource.Audio end) {
      super(owner, res);
      try {
        if (this.res.name.contains("sfx/bgm/")) {
          this.isBGM = true;
          try {
            if (AudioSprite.currentBGM != null) {
              AudioSprite.currentBGM.delete();
              AudioSprite.currentBGM = this;
            } 
          } catch (Exception e) {
            Utils.msgOut("error in BGM handling");
          } 
        } 
      } catch (Exception exception) {}
      AudioSprite.checkForEvents(res);
      this.end = end;
      RepeatStream.Repeater rep = new RepeatStream.Repeater() {
          private boolean f = true;
          
          public InputStream cons() {
            if (this.f && beg != null) {
              this.f = false;
              return beg.pcmstream();
            } 
            return clip.pcmstream();
          }
        };
      Audio.DataClip dataClip = new Audio.DataClip(new RepeatStream(rep));
      this.clip = new ActAudio.PosClip(dataClip);
      if (!this.isBGM)
        if (dataClip instanceof Audio.CS) {
          Audio.CS cs = dataClip;
          AudioSprite.clipMap.put(this, cs);
        } else {
          Utils.msgLog("Audio was no CS-class: " + this.res.name);
        }  
      try {
        Iterator<Map.Entry<RepeatSprite, Audio.CS>> itty = AudioSprite.clipMap.entrySet().iterator();
        while (itty.hasNext()) {
          Map.Entry<RepeatSprite, Audio.CS> next = itty.next();
          RepeatSprite rSprite = next.getKey();
          Audio.CS cs = next.getValue();
          if (!rSprite.isBGM && rSprite != this)
            if (rSprite.owner == this.owner && 
              rSprite.res.name.equals(this.res.name)) {
              if (cs instanceof Audio.DataClip) {
                Audio.DataClip dc = (Audio.DataClip)cs;
                dc.eof();
              } else {
                Utils.msgLog("Clip was no Dataclip: " + rSprite.res.name);
              } 
              Audio.stop(cs);
              rSprite.clip = null;
              itty.remove();
            }  
        } 
      } catch (Exception exception) {}
    }
    
    public boolean setup(RenderList r) {
      if (this.clip != null && 
        !Config.mute_all_audio && (
        !Config.mute_violin || (!this.res.name.equals("sfx/bgm/boston") && !this.res.name.equals("sfx/bgm/markethustle"))) && (!Config.mute_xmas_sound || 
        !this.res.name.equals("sfx/bgm/xmas")))
        r.add(this.clip, null); 
      return false;
    }
    
    public boolean tick(int dt) {
      return (this.clip == null);
    }
    
    public void delete() {
      if (this.end != null) {
        this.clip = new ActAudio.PosClip(new Audio.DataClip(this.end.pcmstream()) {
              protected void eof() {
                super.eof();
                AudioSprite.RepeatSprite.this.clip = null;
              }
            });
      } else {
        this.clip = null;
      } 
    }
    
    public static long getSaveStamp() {
      return Utils.getprefl("shared_bgm_timestamp", 0L);
    }
    
    public static void setSaveStamp(long tStamp) {
      Utils.setprefl("shared_bgm_timestamp", tStamp);
    }
  }
  
  public static void cleanRepeatSounds() {
    try {
      if (lastCheck + 3000L < System.currentTimeMillis()) {
        Iterator<Map.Entry<RepeatSprite, Audio.CS>> itty = clipMap.entrySet().iterator();
        while (itty.hasNext()) {
          Map.Entry<RepeatSprite, Audio.CS> next = itty.next();
          RepeatSprite rSprite = next.getKey();
          Audio.CS cs = next.getValue();
          boolean remove = false;
          if (!rSprite.isBGM) {
            if (rSprite.res.name.contains("cartpull"))
              if (rSprite.owner == null) {
                remove = true;
              } else if (rSprite.owner instanceof Gob) {
                Gob o = (Gob)rSprite.owner;
                Homing homing = o.<Homing>getattr(Homing.class);
                if (homing == null)
                  remove = true; 
              }  
            if (rSprite.t + 60000L < System.currentTimeMillis() && 
              UI.instance.gui.prog < 0)
              remove = true; 
            if (remove) {
              if (cs instanceof Audio.DataClip) {
                Audio.DataClip dc = (Audio.DataClip)cs;
                dc.eof();
              } else {
                Utils.msgLog("Clip was no Dataclip: " + rSprite.res.name);
              } 
              Audio.stop(cs);
              rSprite.clip = null;
              itty.remove();
            } 
          } 
        } 
        lastCheck = System.currentTimeMillis();
      } 
    } catch (Exception e) {
      Utils.msgLog(e.getMessage() + " in RepeatSprite");
    } 
  }
  
  public static class Ambience extends Sprite {
    public final ActAudio.Ambience amb;
    
    public Ambience(Sprite.Owner owner, Resource res) {
      super(owner, res);
      AudioSprite.checkForEvents(res);
      this.amb = new ActAudio.Ambience(res);
    }
    
    public boolean setup(RenderList r) {
      if (!Config.mute_all_audio)
        r.add(this.amb, null); 
      return false;
    }
  }
  
  private static void checkForEvents(Resource res) {
    try {
      if (res.name.toLowerCase().contains("event") && !res.name.contains("thermaevent")) {
        Utils.msgLog("Exploration Event music playing...");
        Utils.msgOut("Exploration Event music playing...");
      } 
    } catch (Exception e) {
      Utils.msgOut("error in Sprite handling");
    } 
  }
  
  private static boolean shouldNotPlayMusic() {
    if (MainFrame.instance.isActive()) {
      long now = System.currentTimeMillis();
      if (isMuted) {
        isMuted = false;
        Utils.setprefl("bgm_ts", now);
        Config.bgm_ts = now;
        lastSetTS = now;
      } else {
        if (wasInactive) {
          wasInactive = false;
          lastSetTS = 0L;
        } 
        if (Config.bgm_ts == 0L || lastSetTS == 0L || lastSetTS != Config.bgm_ts) {
          Utils.setprefl("bgm_ts", now);
          Config.bgm_ts = now;
          lastSetTS = now;
        } 
      } 
    } else {
      if (isMuted)
        return true; 
      long now = System.currentTimeMillis();
      if (lastTCheck < now) {
        lastTCheck = now + 2000L;
        long ts = Utils.getprefl("bgm_ts", 0L);
        if (ts > Config.bgm_ts)
          isMuted = true; 
      } 
    } 
    return false;
  }
  
  public static void checkForOtherInstancesMusic() {
    if (Music.volume < 0.01D && lastMusicVol < 0.01D)
      return; 
    if (Config.mute_all_audio)
      return; 
    if (shouldNotPlayMusic()) {
      if (Music.volume >= 0.01D) {
        lastMusicVol = Music.volume;
        Music.volume = 0.0D;
      } 
    } else if (Music.volume < 0.01D && lastMusicVol >= 0.01D) {
      Music.volume = lastMusicVol;
    } 
  }
  
  private static boolean skipped() {
    if (!skip[0]) {
      skip[0] = true;
      return true;
    } 
    if (!skip[1]) {
      skip[1] = true;
      skip[0] = false;
      return true;
    } 
    if (!skip[2]) {
      skip[2] = true;
      skip[1] = false;
      skip[0] = false;
      return true;
    } 
    if (!skip[3]) {
      skip[3] = true;
      skip[2] = false;
      skip[1] = false;
      skip[0] = false;
      return true;
    } 
    if (!skip[4]) {
      skip[4] = true;
      skip[3] = false;
      skip[2] = false;
      skip[1] = false;
      skip[0] = false;
      return true;
    } 
    skip[4] = false;
    skip[3] = false;
    skip[2] = false;
    skip[1] = false;
    skip[0] = false;
    return false;
  }
}
