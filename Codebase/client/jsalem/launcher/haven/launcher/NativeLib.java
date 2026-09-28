package haven.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class NativeLib {
   public final Pattern os;
   public final Pattern arch;
   public final Resource jar;
   public final String prefix;

   public NativeLib(Pattern os, Pattern arch, Resource jar, String subdir) {
      this.os = os;
      this.arch = arch;
      this.jar = jar;

      while (subdir.startsWith("/")) {
         subdir = subdir.substring(1);
      }

      while (subdir.endsWith("/")) {
         subdir = subdir.substring(0, subdir.length() - 1);
      }

      if (subdir.length() > 0) {
         subdir = subdir + "/";
      }

      this.prefix = subdir;
   }

   public boolean use() {
      return this.os.matcher(System.getProperty("os.name")).matches() && this.arch.matcher(System.getProperty("os.arch")).matches();
   }

   public Path extract() throws IOException {
      Path jar = this.jar.update();
      Path dir = this.jar.metafile("lib");
      boolean fresh = false;
      if (!Files.isDirectory(dir)) {
         fresh = true;
         Files.createDirectories(dir);
      }

      if (fresh || Files.getLastModifiedTime(jar).compareTo(Files.getLastModifiedTime(dir)) > 0) {
         JarFile fp = new JarFile(jar.toFile());
         Enumeration<JarEntry> i = fp.entries();

         while (i.hasMoreElements()) {
            JarEntry ent = i.nextElement();
            if (!ent.isDirectory()) {
               String nm = ent.getName();
               if (nm.charAt(0) != '.' && nm.startsWith(this.prefix)) {
                  nm = nm.substring(this.prefix.length());
                  if (nm.indexOf(47) < 0) {
                     InputStream in = fp.getInputStream(ent);

                     try {
                        OutputStream out = Files.newOutputStream(dir.resolve(nm));

                        try {
                           byte[] buf = new byte[65536];

                           int rv;
                           while ((rv = in.read(buf)) >= 0) {
                              out.write(buf, 0, rv);
                           }
                        } catch (Throwable var14) {
                           if (out != null) {
                              try {
                                 out.close();
                              } catch (Throwable var13) {
                                 var14.addSuppressed(var13);
                              }
                           }

                           throw var14;
                        }

                        if (out != null) {
                           out.close();
                        }
                     } catch (Throwable var15) {
                        if (in != null) {
                           try {
                              in.close();
                           } catch (Throwable var12) {
                              var15.addSuppressed(var12);
                           }
                        }

                        throw var15;
                     }

                     if (in != null) {
                        in.close();
                     }
                  }
               }
            }
         }

         Files.setLastModifiedTime(dir, FileTime.from(Instant.now()));
      }

      return dir;
   }
}
