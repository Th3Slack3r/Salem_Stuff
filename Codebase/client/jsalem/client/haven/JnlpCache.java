package haven;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;

public class JnlpCache implements ResCache {
   private PersistenceService back;
   private URL base;

   private JnlpCache(PersistenceService back, URL base) {
      this.back = back;
      this.base = base;
   }

   public static JnlpCache create() {
      try {
         Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
         Method m = cl.getMethod("lookup", String.class);
         BasicService basic = (BasicService)m.invoke(null, "javax.jnlp.BasicService");
         PersistenceService prs = (PersistenceService)m.invoke(null, "javax.jnlp.PersistenceService");
         return new JnlpCache(prs, basic.getCodeBase());
      } catch (Exception var4) {
         return null;
      }
   }

   private static String mangle(String nm) {
      StringBuilder buf = new StringBuilder();

      for (int i = 0; i < nm.length(); i++) {
         char c = nm.charAt(i);
         if (c == '/') {
            buf.append("_");
         } else {
            buf.append(c);
         }
      }

      return buf.toString();
   }

   private void realput(URL loc, byte[] data) {
      try {
         FileContents file;
         try {
            file = this.back.get(loc);
         } catch (FileNotFoundException var11) {
            this.back.create(loc, data.length);
            file = this.back.get(loc);
         }

         if (file.getMaxLength() < data.length && file.setMaxLength(data.length) < data.length) {
            this.back.delete(loc);
         } else {
            OutputStream s = file.getOutputStream(true);

            try {
               s.write(data);
            } finally {
               s.close();
            }
         }
      } catch (IOException var12) {
      } catch (Exception var13) {
      }
   }

   private void put(final URL loc, final byte[] data) {
      Utils.defer(new Runnable() {
         @Override
         public void run() {
            JnlpCache.this.realput(loc, data);
         }
      });
   }

   private InputStream get(URL loc) throws IOException {
      FileContents file = this.back.get(loc);
      return file.getInputStream();
   }

   @Override
   public OutputStream store(final String name) throws IOException {
      OutputStream ret = new ByteArrayOutputStream() {
         @Override
         public void close() {
            byte[] res = this.toByteArray();

            try {
               JnlpCache.this.put(new URL(JnlpCache.this.base, JnlpCache.mangle(name)), res);
            } catch (MalformedURLException var3) {
               throw new RuntimeException(var3);
            }
         }
      };
      return ret;
   }

   @Override
   public InputStream fetch(String name) throws IOException {
      try {
         URL loc = new URL(this.base, mangle(name));
         return this.get(loc);
      } catch (IOException var3) {
         throw var3;
      } catch (Exception var4) {
         throw (IOException)new IOException("Virtual NetX IO exception").initCause(var4);
      }
   }
}
