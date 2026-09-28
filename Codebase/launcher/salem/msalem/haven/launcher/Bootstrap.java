package haven.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Bootstrap {
   public static void bootstrap(OutputStream out, InputStream cfg) throws IOException {
      URL srcjar;
      try {
         srcjar = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
      } catch (Exception var10) {
         throw new RuntimeException("Could not locate source Jar file", var10);
      }

      ZipOutputStream outjar = new ZipOutputStream(out);
      byte[] buf = new byte[65536];
      Collection<String> seen = new ArrayList<>();
      ZipInputStream injar = new ZipInputStream(srcjar.openConnection().getInputStream());

      ZipEntry ent;
      try {
         for (; (ent = injar.getNextEntry()) != null; injar.closeEntry()) {
            if (ent.getName().equals("haven/launcher/bootstrap.hl")) {
               System.err.println("launcher: warning: removing current bootstrap");
            } else {
               outjar.putNextEntry(ent);

               for (int rv = injar.read(buf); rv >= 0; rv = injar.read(buf)) {
                  outjar.write(buf, 0, rv);
               }

               seen.add(ent.getName().toLowerCase());
            }
         }
      } catch (Throwable var11) {
         try {
            injar.close();
         } catch (Throwable var9) {
            var11.addSuppressed(var9);
         }

         throw var11;
      }

      injar.close();
      if (seen.contains("meta-inf/manifest.mf") && seen.contains("haven/launcher/driver.class")) {
         outjar.putNextEntry(new ZipEntry("haven/launcher/bootstrap.hl"));

         for (int rv = cfg.read(buf); rv >= 0; rv = cfg.read(buf)) {
            outjar.write(buf, 0, rv);
         }

         outjar.finish();
      } else {
         throw new RuntimeException("Source Jar file appears corrupt or incomplete");
      }
   }

   private static void usage(PrintStream out) {
      out.println("usage: Bootstrap [-h] [BOOT-CONFIG|-] OUTPUT");
   }

   public static void main(String[] args) {
      PosixArgs opt = PosixArgs.getopt(args, "h");
      if (opt == null) {
         usage(System.err);
         System.exit(1);
      }

      for (char c : opt.parsed()) {
         switch (c) {
            case 'h':
               usage(System.out);
               System.exit(0);
         }
      }

      if (opt.rest.length < 2) {
         usage(System.err);
         System.exit(1);
      }

      try {
         InputStream cl = null;
         InputStream cfg;
         if (opt.rest[0].equals("-")) {
            cfg = System.in;
         } else {
            cl = cfg = new BufferedInputStream(Files.newInputStream(Utils.path(opt.rest[0])));
         }

         Path outnm = Utils.path(opt.rest[1]);
         Path temp = outnm.resolveSibling(outnm.getFileName() + ".new");

         try {
            boolean done = false;

            try {
               OutputStream out = new BufferedOutputStream(Files.newOutputStream(temp));

               try {
                  bootstrap(out, cfg);
               } catch (Throwable var23) {
                  try {
                     out.close();
                  } catch (Throwable var22) {
                     var23.addSuppressed(var22);
                  }

                  throw var23;
               }

               out.close();
               Files.move(temp, outnm, StandardCopyOption.ATOMIC_MOVE);
               done = true;
            } finally {
               if (!done) {
                  Files.delete(temp);
               }
            }
         } finally {
            if (cl != null) {
               cl.close();
            }
         }
      } catch (Exception var26) {
         var26.printStackTrace();
         System.exit(1);
      }
   }
}
