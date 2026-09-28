package haven.launcher;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Resource {
   public final URI uri;
   public final Collection<Validator> val;
   public URI referrer;

   public Resource(URI uri, Collection<Validator> val) {
      this.uri = uri;
      this.val = val;
   }

   public Resource referrer(URI ref) {
      this.referrer = ref;
      return this;
   }

   private void validate(Cached cf) throws ValidationException {
      if (!this.val.isEmpty()) {
         Collection<ValidationException> errors = new ArrayList<>();
         Iterator e = this.val.iterator();

         while (true) {
            if (!e.hasNext()) {
               ValidationException ex = new ValidationException("Could not validate " + this.uri);

               for (ValidationException err : errors) {
                  ex.addSuppressed(err);
               }

               throw ex;
            }

            Validator val = (Validator)e.next();

            try {
               val.validate(cf);
               break;
            } catch (ValidationException var6) {
               errors.add(var6);
            }
         }
      }
   }

   public Path metafile(String var) {
      return Cache.get().metafile(this.uri, var);
   }

   public Path update() throws IOException {
      Cache cache = Cache.get();
      Cached cf = cache.update(this, false);
      Status st = Status.current();

      Path e;
      try {
         st.messagef("Validating %s...", Utils.basename(this.uri));

         try {
            this.validate(cf);
         } catch (ValidationException var7) {
            if (cf.fresh) {
               throw var7;
            }

            cf = cache.update(this, true);
            this.validate(cf);
         }

         e = cf.path;
      } catch (Throwable var8) {
         if (st != null) {
            try {
               st.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }
         }

         throw var8;
      }

      if (st != null) {
         st.close();
      }

      return e;
   }
}
