package haven;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ResCache {
   ResCache global = ResCache.StupidJavaCodeContainer.makeglobal();

   OutputStream store(String var1) throws IOException;

   InputStream fetch(String var1) throws IOException;

   public static class StupidJavaCodeContainer {
      private static ResCache makeglobal() {
         ResCache ret;
         if ((ret = JnlpCache.create()) != null) {
            return ret;
         } else {
            BaseFileCache var1;
            return Config.fscache && (var1 = BaseFileCache.create()) != null ? var1 : null;
         }
      }
   }

   public static class TestCache implements ResCache {
      @Override
      public OutputStream store(final String name) {
         return new ByteArrayOutputStream() {
            @Override
            public void close() {
               byte[] res = this.toByteArray();
               System.out.println(name + ": " + res.length);
            }
         };
      }

      @Override
      public InputStream fetch(String name) throws IOException {
         throw new FileNotFoundException();
      }
   }
}
