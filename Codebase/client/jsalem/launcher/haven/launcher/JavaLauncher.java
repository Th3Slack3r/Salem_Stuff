package haven.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JavaLauncher implements Launcher {
   public final Collection<Resource> classpath = new ArrayList<>();
   public final Collection<String> jvmargs = new ArrayList<>();
   public final Collection<String> cmdargs = new ArrayList<>();
   public final Collection<NativeLib> libraries = new ArrayList<>();
   public final Map<String, String> sysprops = new HashMap<>();
   public String mainclass = null;
   public Resource execjar = null;
   public int heapsize = 0;

   public JavaLauncher() {
   }

   public JavaLauncher(JavaLauncher that) {
      this.copy(that);
   }

   public void copy(JavaLauncher that) {
      this.classpath.addAll(that.classpath);
      this.jvmargs.addAll(that.jvmargs);
      this.cmdargs.addAll(that.cmdargs);
      this.libraries.addAll(that.libraries);
      this.sysprops.putAll(that.sysprops);
      this.mainclass = that.mainclass;
      this.execjar = that.execjar;
      this.heapsize = that.heapsize;
   }

   protected Path findjvm() {
      Path javadir = Utils.pj(Utils.path(System.getProperty("java.home")), "bin");
      Path jvm;
      if (Files.exists(jvm = Utils.pj(javadir, "java"))) {
         return jvm;
      } else if (Files.exists(jvm = Utils.pj(javadir, "javaw.exe"))) {
         return jvm;
      } else if (Files.exists(jvm = Utils.pj(javadir, "java.exe"))) {
         return jvm;
      } else {
         throw new RuntimeException("could not find a Java executable");
      }
   }

   protected Process launch(ProcessBuilder spec) throws IOException {
      Status st = Status.current();

      Process var3;
      try {
         st.message("Launching...");
         var3 = spec.start();
      } catch (Throwable var6) {
         if (st != null) {
            try {
               st.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (st != null) {
         st.close();
      }

      return var3;
   }

   @Override
   public void launch() throws IOException {
      List<String> args = new ArrayList<>();
      args.add(this.findjvm().toFile().toString());
      Collection<Path> classpath = new ArrayList<>();

      for (Resource res : this.classpath) {
         classpath.add(res.update());
      }

      if (this.heapsize > 0) {
         args.add(String.format("-Xmx%dm", this.heapsize));
      }

      for (String arg : this.jvmargs) {
         args.add(arg);
      }

      for (Entry<String, String> prop : this.sysprops.entrySet()) {
         args.add(String.format("-D%s=%s", prop.getKey(), prop.getValue()));
      }

      if (!classpath.isEmpty()) {
         args.add("-classpath");
         args.add(String.join(File.pathSeparator, classpath.stream().map(Path::toFile).map(File::toString)::iterator));
      }

      Collection<String> libdirs = new ArrayList<>();

      for (NativeLib lib : this.libraries) {
         if (lib.use()) {
            libdirs.add(lib.extract().toFile().toString());
         }
      }

      if (libdirs.size() > 0) {
         String dirs = String.join(File.pathSeparator, libdirs);
         String cur = System.getProperty("java.library.path");
         if (cur != null && cur.length() > 0) {
            dirs = dirs + File.pathSeparator + cur;
         }

         args.add(String.format("-Djava.library.path=%s", dirs));
      }

      if (this.mainclass != null) {
         args.add(this.mainclass);
      } else {
         if (this.execjar == null) {
            throw new RuntimeException("neither main-class nor exec-jar specified for Java launcher");
         }

         args.add("-jar");
         args.add(this.execjar.update().toString());
      }

      for (String arg : this.cmdargs) {
         args.add(arg);
      }

      ProcessBuilder spec = new ProcessBuilder(args);
      spec.inheritIO();
      this.launch(spec);
   }

   @Override
   public boolean command(String[] words, Config cfg, Config.Environment env) {
      String var4 = words[0];
      switch (var4) {
         case "main-class":
            if (words.length < 2) {
               throw new RuntimeException("usage: main-class CLASS-NAME");
            }

            this.mainclass = Config.expand(words[1], env);
            return true;
         case "exec-jar":
            if (words.length < 2) {
               throw new RuntimeException("usage: exec-jar URL");
            } else {
               try {
                  this.execjar = new Resource(env.rel.resolve(new URI(Config.expand(words[1], env))), env.val).referrer(env.src);
                  return true;
               } catch (URISyntaxException var13) {
                  throw new RuntimeException("usage: exec-jar URL", var13);
               }
            }
         case "class-path":
            if (words.length < 2) {
               throw new RuntimeException("usage: classpath URL");
            } else {
               try {
                  this.classpath.add(new Resource(env.rel.resolve(new URI(Config.expand(words[1], env))), env.val).referrer(env.src));
                  return true;
               } catch (URISyntaxException var12) {
                  throw new RuntimeException("usage: classpath URL", var12);
               }
            }
         case "property":
            if (words.length < 3) {
               throw new RuntimeException("usage: property NAME VALUE");
            }

            this.sysprops.put(Config.expand(words[1], env), Config.expand(words[2], env));
            return true;
         case "heap-size":
            if (words.length < 2) {
               throw new RuntimeException("usage: heap-size MBYTES");
            } else {
               try {
                  this.heapsize = Integer.parseInt(Config.expand(words[1], env));
                  return true;
               } catch (NumberFormatException var11) {
                  throw new RuntimeException("usage: heap-size MBYTES", var11);
               }
            }
         case "jvm-arg":
            if (words.length < 2) {
               throw new RuntimeException("usage: jvm-arg ARG...");
            }

            for (int i = 1; i < words.length; i++) {
               this.jvmargs.add(Config.expand(words[i], env));
            }

            return true;
         case "arguments":
            if (words.length < 2) {
               throw new RuntimeException("usage: arguments ARG...");
            }

            for (int i = 1; i < words.length; i++) {
               this.cmdargs.add(Config.expand(words[i], env));
            }

            return true;
         case "native-lib":
            if (words.length < 4) {
               throw new RuntimeException("usage: native-lib OS ARCH URL [SUB-DIR]");
            } else {
               try {
                  Pattern os = Pattern.compile(words[1], 2);
                  Pattern arch = Pattern.compile(words[2], 2);
                  Resource lib = new Resource(env.rel.resolve(new URI(Config.expand(words[3], env))), env.val).referrer(env.src);
                  String subdir = "";
                  if (words.length > 4) {
                     subdir = Config.expand(words[4], env);
                  }

                  this.libraries.add(new NativeLib(os, arch, lib, subdir));
                  return true;
               } catch (URISyntaxException | PatternSyntaxException var10) {
                  throw new RuntimeException("usage: native-lib OS ARCH URL [SUB-DIR]", var10);
               }
            }
         default:
            return false;
      }
   }
}
