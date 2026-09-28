package haven.launcher;

import java.awt.HeadlessException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driver {
   public static void execute(Config cfg) {
      try {
         Launcher l = cfg.launcher;
         Status.current().launch(l);
         l.launch();
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   public static void run(Config cfg) {
      try {
         while (!cfg.include.isEmpty()) {
            Resource res = Utils.pop(cfg.include);
            if (!cfg.included.contains(res.uri)) {
               cfg.included.add(res.uri);
               Path path = res.update();
               InputStream src = Files.newInputStream(path);

               try {
                  cfg.read(new InputStreamReader(src, Utils.utf8), Config.Environment.from(res));
               } catch (Throwable var7) {
                  if (src != null) {
                     try {
                        src.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (src != null) {
                  src.close();
               }
            }
         }
      } catch (IOException var8) {
         throw new RuntimeException(var8);
      }

      execute(cfg);
   }

   private static void usage(PrintStream out) {
      out.println("usage: launcher.jar [-hq] [-x EXTENSION] [CONFIG-URL|FILE]");
   }

   public static void main(String[] args) {
      try {
         boolean quiet = false;
         PosixArgs opt = PosixArgs.getopt(args, "hqx:");
         if (opt == null) {
            usage(System.err);
            System.exit(1);
         }

         List<String> exts = new ArrayList<>();

         for (char c : opt.parsed()) {
            switch (c) {
               case 'h':
                  usage(System.out);
                  System.exit(0);
                  break;
               case 'q':
                  quiet = true;
                  break;
               case 'x':
                  exts.add(opt.arg);
            }
         }

         if (!quiet) {
            try {
               Status.use(new TTYStatus());
            } catch (IOException var29) {
               try {
                  Status.use(new AWTStatus());
               } catch (HeadlessException var28) {
               }
            }
         }

         Config cfg = new Config();

         for (String extn : exts) {
            try {
               if (extn.indexOf("://") < 0) {
                  for (Extension ext : Extension.load(Utils.path(extn))) {
                     ext.init(cfg);
                  }
               } else {
                  for (Extension ext : Extension.load(new Resource(new URI(extn), Collections.emptyList()))) {
                     ext.init(cfg);
                  }
               }
            } catch (IOException var33) {
               System.err.printf("launcher: could not load extension %s: %s\n", extn, var33);
            }
         }

         if (opt.rest.length > 0) {
            try {
               if (opt.rest[0].indexOf("://") < 0) {
                  Path p = Utils.path(opt.rest[0]);
                  InputStream src = Files.newInputStream(p);

                  try {
                     cfg.read(new InputStreamReader(src, Utils.utf8), new Config.Environment().rel(p.toUri()));
                  } catch (Throwable var31) {
                     if (src != null) {
                        try {
                           src.close();
                        } catch (Throwable var24) {
                           var31.addSuppressed(var24);
                        }
                     }

                     throw var31;
                  }

                  if (src != null) {
                     src.close();
                  }
               } else {
                  URI uri;
                  try {
                     uri = new URI(opt.rest[0]);
                  } catch (URISyntaxException var27) {
                     System.err.printf("launcher: invalid url: %s\n", opt.rest[0]);
                     System.exit(1);
                     return;
                  }

                  Resource res = new Resource(uri, Collections.emptyList());
                  InputStream src = Files.newInputStream(res.update());

                  try {
                     cfg.read(new InputStreamReader(src, Utils.utf8), Config.Environment.from(res));
                  } catch (Throwable var30) {
                     if (src != null) {
                        try {
                           src.close();
                        } catch (Throwable var23) {
                           var30.addSuppressed(var23);
                        }
                     }

                     throw var30;
                  }

                  if (src != null) {
                     src.close();
                  }
               }
            } catch (IOException var32) {
               System.err.printf("launcher: could not read %s: %s\n", opt.rest[0], var32);
               System.exit(1);
               return;
            }
         } else {
            InputStream src = Driver.class.getResourceAsStream("bootstrap.hl");
            if (src == null) {
               System.err.println("launcher: no bootstreap config found\n");
               usage(System.err);
               System.exit(1);
            }

            try {
               try {
                  cfg.read(new InputStreamReader(src, Utils.utf8), new Config.Environment());
               } finally {
                  src.close();
               }
            } catch (IOException var26) {
               throw new AssertionError(var26);
            }
         }

         run(cfg);
      } catch (Throwable var34) {
         Status.current().error(var34);
      }

      System.exit(0);
   }
}
