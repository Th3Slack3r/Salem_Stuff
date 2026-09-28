package haven.launcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

public interface Extension {
   void init(Config var1);

   static Collection<Extension> load(Path jar) throws IOException {
      ClassLoader lib = new URLClassLoader(new URL[]{jar.toUri().toURL()}, Extension.class.getClassLoader());
      ArrayList<Extension> ret = new ArrayList<>();

      for (Extension ext : ServiceLoader.load(Extension.class, lib)) {
         ret.add(ext);
      }

      ret.trimToSize();
      return ret;
   }

   static Collection<Extension> load(Resource uri) throws IOException {
      return load(uri.update());
   }
}
