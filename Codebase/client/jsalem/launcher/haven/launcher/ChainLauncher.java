package haven.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ChainLauncher implements Launcher {
   public Resource chain;

   public ChainLauncher(Resource chain) {
      this.chain = chain;
   }

   @Override
   public void launch() throws IOException {
      Config chained = new Config();
      InputStream src = Files.newInputStream(this.chain.update());

      try {
         chained.read(new InputStreamReader(src, Utils.utf8), Config.Environment.from(this.chain));
      } catch (Throwable var6) {
         if (src != null) {
            try {
               src.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (src != null) {
         src.close();
      }

      Driver.run(chained);
   }

   @Override
   public boolean command(String[] args, Config cfg, Config.Environment env) {
      return false;
   }
}
