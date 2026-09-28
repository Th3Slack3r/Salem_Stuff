package haven;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioSprite {
   public static final Sprite.Factory fact = new Sprite.Factory() {
      private Resource.Audio randoom(Resource res, String id) {
         List<Resource.Audio> cl = new ArrayList<>();

         for (Resource.Audio clip : res.layers(Resource.audio)) {
            if (clip.id == id) {
               cl.add(clip);
            }
         }

         return !cl.isEmpty() ? cl.get((int)(Math.random() * cl.size())) : null;
      }

      @Override
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
         Resource.Audio clip = this.randoom(res, "cl");
         if (clip != null) {
            return new AudioSprite.ClipSprite(owner, res, clip);
         } else {
            clip = this.randoom(res, "rep");
            if (clip != null) {
               return new AudioSprite.RepeatSprite(owner, res, this.randoom(res, "beg"), clip, this.randoom(res, "end"));
            } else {
               clip = res.layer(Resource.audio, "amb");
               return clip != null ? new AudioSprite.Ambience(owner, res) : null;
            }
         }
      }
   };

   public static class Ambience extends Sprite {
      public final ActAudio.Ambience amb;

      public Ambience(Sprite.Owner owner, Resource res) {
         super(owner, res);
         this.amb = new ActAudio.Ambience(res);
      }

      @Override
      public boolean setup(RenderList r) {
         r.add(this.amb, null);
         return false;
      }
   }

   public static class ClipSprite extends Sprite {
      public final ActAudio.PosClip clip;
      private boolean done = false;

      public ClipSprite(Sprite.Owner owner, Resource res, Resource.Audio clip) {
         super(owner, res);
         this.clip = new ActAudio.PosClip(new Audio.DataClip(clip.pcmstream()) {
            @Override
            protected void eof() {
               super.eof();
               ClipSprite.this.done = true;
            }
         });
      }

      @Override
      public boolean setup(RenderList r) {
         r.add(this.clip, null);
         return false;
      }

      @Override
      public boolean tick(int dt) {
         return this.done;
      }
   }

   public static class RepeatSprite extends Sprite implements Gob.Overlay.CDel {
      private ActAudio.PosClip clip;
      private final Resource.Audio end;

      public RepeatSprite(Sprite.Owner owner, Resource res, final Resource.Audio beg, final Resource.Audio clip, Resource.Audio end) {
         super(owner, res);
         this.end = end;
         RepeatStream.Repeater rep = new RepeatStream.Repeater() {
            private boolean f = true;

            @Override
            public InputStream cons() {
               if (this.f && beg != null) {
                  this.f = false;
                  return beg.pcmstream();
               } else {
                  return clip.pcmstream();
               }
            }
         };
         this.clip = new ActAudio.PosClip(new Audio.DataClip(new RepeatStream(rep)));
      }

      @Override
      public boolean setup(RenderList r) {
         if (this.clip != null) {
            r.add(this.clip, null);
         }

         return false;
      }

      @Override
      public boolean tick(int dt) {
         return this.clip == null;
      }

      @Override
      public void delete() {
         if (this.end != null) {
            this.clip = new ActAudio.PosClip(new Audio.DataClip(this.end.pcmstream()) {
               @Override
               protected void eof() {
                  super.eof();
                  RepeatSprite.this.clip = null;
               }
            });
         } else {
            this.clip = null;
         }
      }
   }
}
