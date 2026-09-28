package haven;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActAudio extends GLState.Abstract {
  public static final GLState.Slot<ActAudio> slot = new GLState.Slot<>(GLState.Slot.Type.SYS, ActAudio.class, new GLState.Slot[0]);
  
  private final Collection<Audio.CS> clips = new ArrayList<>();
  
  private final Collection<Audio.CS> current = new ArrayList<>();
  
  private final Map<Global, Global> global = new HashMap<>();
  
  public void prep(GLState.Buffer st) {
    st.put(slot, this);
  }
  
  public static interface Global {
    boolean cycle(ActAudio param1ActAudio);
  }
  
  public static class PosClip implements Rendered {
    private final Audio.DataClip clip;
    
    public PosClip(Audio.DataClip clip) {
      this.clip = clip;
    }
    
    public void draw(GOut g) {
      g.apply();
      ActAudio list = g.st.<ActAudio>cur(ActAudio.slot);
      if (list != null) {
        Coord3f pos = g.st.mv.mul4(Coord3f.o);
        double pd = Math.sqrt((pos.x * pos.x + pos.y * pos.y));
        this.clip.vol = Math.min(1.0D, 50.0D / pd);
        list.add(this.clip);
      } 
    }
    
    public boolean setup(RenderList rl) {
      return true;
    }
  }
  
  public static class Ambience implements Rendered {
    public final Resource res;
    
    public final double bvol;
    
    private Glob glob = null;
    
    public Ambience(Resource res, double bvol) {
      if (res.layer(Resource.audio, "amb") == null)
        throw new RuntimeException("No ambient clip found in " + res); 
      this.res = res;
      this.bvol = bvol;
    }
    
    public Ambience(Resource res) {
      this(res, ((Resource.Audio)res.layer((Class)Resource.audio, (I)"amb")).bvol);
    }
    
    public static class Glob implements ActAudio.Global {
      public final Resource res;
      
      private final Audio.DataClip clip;
      
      private int n;
      
      private double vacc;
      
      private double lastupd = System.currentTimeMillis() / 1000.0D;
      
      public Glob(Resource res) {
        this.res = res;
        final Resource.Audio clip = res.<String, Resource.Audio>layer(Resource.audio, "amb");
        if (clip == null)
          throw new RuntimeException("No ambient clip found in " + res); 
        this.clip = new Audio.DataClip(new RepeatStream(new RepeatStream.Repeater() {
                public InputStream cons() {
                  return clip.pcmstream();
                }
              },  ), 0.0D, 1.0D);
      }
      
      public int hashCode() {
        return this.res.hashCode();
      }
      
      public boolean equals(Object other) {
        return (other instanceof Glob && ((Glob)other).res == this.res);
      }
      
      public boolean cycle(ActAudio list) {
        double now = System.currentTimeMillis() / 1000.0D;
        double td = Math.max(now - this.lastupd, 0.0D);
        if (this.vacc < this.clip.vol) {
          this.clip.vol = Math.max(this.clip.vol - td * 0.5D, 0.0D);
        } else if (this.vacc > this.clip.vol) {
          this.clip.vol = Math.min(this.clip.vol + td * 0.5D, 1.0D);
        } 
        if (this.n == 0 && this.clip.vol < 0.005D)
          return true; 
        this.vacc = 0.0D;
        this.n = 0;
        this.lastupd = now;
        list.add(this.clip);
        return false;
      }
      
      public void add(double vol) {
        this.vacc += vol;
        this.n++;
      }
    }
    
    public void draw(GOut g) {
      g.apply();
      if (this.glob == null) {
        ActAudio list = g.st.<ActAudio>cur(ActAudio.slot);
        if (list == null)
          return; 
        this.glob = list.<Glob>intern(new Glob(this.res));
      } 
      Coord3f pos = g.st.mv.mul4(Coord3f.o);
      double pd = Math.sqrt((pos.x * pos.x + pos.y * pos.y));
      double svol = Math.min(1.0D, 50.0D / pd);
      this.glob.add(svol * this.bvol);
    }
    
    public boolean setup(RenderList rl) {
      return true;
    }
  }
  
  public void add(Audio.CS clip) {
    this.clips.add(clip);
  }
  
  public <T extends Global> T intern(T glob) {
    T t;
    Global global = this.global.get(glob);
    if (global == null)
      this.global.put((Global)glob, (Global)(t = glob)); 
    return t;
  }
  
  public void cycle() {
    Iterator<Global> i;
    for (i = this.global.keySet().iterator(); i.hasNext(); ) {
      Global glob = i.next();
      if (glob.cycle(this))
        i.remove(); 
    } 
    for (i = (Iterator)this.current.iterator(); i.hasNext(); ) {
      Audio.CS clip = (Audio.CS)i.next();
      if (!this.clips.contains(clip))
        Audio.stop(clip); 
    } 
    for (i = (Iterator)this.clips.iterator(); i.hasNext(); ) {
      Audio.CS clip = (Audio.CS)i.next();
      if (!this.current.contains(clip))
        Audio.play(clip); 
    } 
    this.current.clear();
    this.current.addAll(this.clips);
    this.clips.clear();
    AudioSprite.cleanRepeatSounds();
  }
  
  public void clear() {
    for (Audio.CS clip : this.current)
      Audio.stop(clip); 
  }
}
