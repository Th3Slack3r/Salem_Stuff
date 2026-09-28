package haven;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActAudio extends GLState.Abstract {
   public static final GLState.Slot<ActAudio> slot = new GLState.Slot<>(GLState.Slot.Type.SYS, ActAudio.class);
   private final Collection<Audio.CS> clips = new ArrayList<>();
   private final Collection<Audio.CS> current = new ArrayList<>();
   private final Map<ActAudio.Global, ActAudio.Global> global = new HashMap<>();

   @Override
   public void prep(GLState.Buffer st) {
      st.put(slot, this);
   }

   public void add(Audio.CS clip) {
      this.clips.add(clip);
   }

   public <T extends ActAudio.Global> T intern(T glob) {
      T ret = (T)this.global.get(glob);
      if (ret == null) {
         ret = glob;
         this.global.put(glob, glob);
      }

      return ret;
   }

   public void cycle() {
      Iterator<ActAudio.Global> i = this.global.keySet().iterator();

      while (i.hasNext()) {
         ActAudio.Global glob = i.next();
         if (glob.cycle(this)) {
            i.remove();
         }
      }

      for (Audio.CS clip : this.current) {
         if (!this.clips.contains(clip)) {
            Audio.stop(clip);
         }
      }

      for (Audio.CS clipx : this.clips) {
         if (!this.current.contains(clipx)) {
            Audio.play(clipx);
         }
      }

      this.current.clear();
      this.current.addAll(this.clips);
      this.clips.clear();
   }

   public void clear() {
      for (Audio.CS clip : this.current) {
         Audio.stop(clip);
      }
   }

   public static class Ambience implements Rendered {
      public final Resource res;
      public final double bvol;
      private ActAudio.Ambience.Glob glob = null;

      public Ambience(Resource res, double bvol) {
         if (res.layer(Resource.audio, "amb") == null) {
            throw new RuntimeException("No ambient clip found in " + res);
         } else {
            this.res = res;
            this.bvol = bvol;
         }
      }

      public Ambience(Resource res) {
         this(res, res.layer(Resource.audio, "amb").bvol);
      }

      @Override
      public void draw(GOut g) {
         g.apply();
         if (this.glob == null) {
            ActAudio list = g.st.cur(ActAudio.slot);
            if (list == null) {
               return;
            }

            this.glob = list.intern(new ActAudio.Ambience.Glob(this.res));
         }

         Coord3f pos = g.st.mv.mul4(Coord3f.o);
         double pd = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
         double svol = Math.min(1.0, 50.0 / pd);
         this.glob.add(svol * this.bvol);
      }

      @Override
      public boolean setup(RenderList rl) {
         return true;
      }

      public static class Glob implements ActAudio.Global {
         public final Resource res;
         private final Audio.DataClip clip;
         private int n;
         private double vacc;
         private double lastupd = System.currentTimeMillis() / 1000.0;

         public Glob(Resource res) {
            this.res = res;
            final Resource.Audio clip = res.layer(Resource.audio, "amb");
            if (clip == null) {
               throw new RuntimeException("No ambient clip found in " + res);
            } else {
               this.clip = new Audio.DataClip(new RepeatStream(new RepeatStream.Repeater() {
                  @Override
                  public InputStream cons() {
                     return clip.pcmstream();
                  }
               }), 0.0, 1.0);
            }
         }

         @Override
         public int hashCode() {
            return this.res.hashCode();
         }

         @Override
         public boolean equals(Object other) {
            return other instanceof ActAudio.Ambience.Glob && ((ActAudio.Ambience.Glob)other).res == this.res;
         }

         @Override
         public boolean cycle(ActAudio list) {
            double now = System.currentTimeMillis() / 1000.0;
            double td = Math.max(now - this.lastupd, 0.0);
            if (this.vacc < this.clip.vol) {
               this.clip.vol = Math.max(this.clip.vol - td * 0.5, 0.0);
            } else if (this.vacc > this.clip.vol) {
               this.clip.vol = Math.min(this.clip.vol + td * 0.5, 1.0);
            }

            if (this.n == 0 && this.clip.vol < 0.005) {
               return true;
            } else {
               this.vacc = 0.0;
               this.n = 0;
               this.lastupd = now;
               list.add(this.clip);
               return false;
            }
         }

         public void add(double vol) {
            this.vacc += vol;
            this.n++;
         }
      }
   }

   public interface Global {
      boolean cycle(ActAudio var1);
   }

   public static class PosClip implements Rendered {
      private final Audio.DataClip clip;

      public PosClip(Audio.DataClip clip) {
         this.clip = clip;
      }

      @Override
      public void draw(GOut g) {
         g.apply();
         ActAudio list = g.st.cur(ActAudio.slot);
         if (list != null) {
            Coord3f pos = g.st.mv.mul4(Coord3f.o);
            double pd = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
            this.clip.vol = Math.min(1.0, 50.0 / pd);
            list.add(this.clip);
         }
      }

      @Override
      public boolean setup(RenderList rl) {
         return true;
      }
   }
}
