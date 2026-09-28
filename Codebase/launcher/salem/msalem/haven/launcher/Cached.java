package haven.launcher;

import java.nio.file.Path;
import java.util.Properties;

public class Cached {
   public final Path path;
   public final Properties props;
   public final boolean fresh;

   public Cached(Path path, Properties props, boolean fresh) {
      this.path = path;
      this.props = props;
      this.fresh = fresh;
   }
}
