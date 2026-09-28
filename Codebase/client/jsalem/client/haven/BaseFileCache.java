package haven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;

public class BaseFileCache implements ResCache {
   public final URI id;
   private final Path base;
   private static final Map<URI, BaseFileCache> current = new CacheMap<>();

   public static Path findroot() {
      try {
         String path = System.getenv("APPDATA");
         if (path != null) {
            Path appdata = Utils.path(path);
            label54:
            if (Files.exists(appdata) && Files.isDirectory(appdata) && Files.isReadable(appdata) && Files.isWritable(appdata)) {
               Path base = Utils.pj(appdata, "Salem", "cache");
               if (!Files.exists(base)) {
                  try {
                     Files.createDirectories(base);
                  } catch (IOException var5) {
                     break label54;
                  }
               }

               return base;
            }
         }

         path = System.getProperty("user.home", null);
         if (path != null) {
            Path home = Utils.path(path);
            if (Files.exists(home) && Files.isDirectory(home) && Files.isReadable(home) && Files.isWritable(home)) {
               Path base = Utils.pj(home, ".salem", "cache");
               if (!Files.exists(base)) {
                  try {
                     Files.createDirectories(base);
                  } catch (IOException var4) {
                     throw new UnsupportedOperationException("Found no reasonable place to store local files");
                  }
               }

               return base;
            }
         }
      } catch (SecurityException var6) {
      }

      throw new UnsupportedOperationException("Found no reasonable place to store local files");
   }

   public static Path findbase(URI id) throws IOException {
      String idstr = id.toString();
      int idhash = 0;

      for (int i = 0; i < idstr.length(); i++) {
         idhash = idhash * 31 + idstr.charAt(i);
      }

      Path root = findroot();
      Path lfn = Utils.pj(root, ".index-lock");
      synchronized (BaseFileCache.class) {
         LockedFile lock = LockedFile.lock(lfn);

         Path var44;
         label243: {
            Path var46;
            try {
               int idx = 0;

               while (true) {
                  Path base = Utils.pj(root, String.format("%08x.%d", idhash, idx));
                  Path propname = Utils.pj(base, "cache.properties");
                  if (!Files.exists(base) || !Files.isDirectory(base)) {
                     boolean done = false;

                     try {
                        Files.createDirectory(base);
                        Properties props = new Properties();
                        props.put("base", idstr);
                        OutputStream fp = Files.newOutputStream(propname);

                        try {
                           props.store(fp, null);
                        } catch (Throwable var36) {
                           if (fp != null) {
                              try {
                                 fp.close();
                              } catch (Throwable var34) {
                                 var36.addSuppressed(var34);
                              }
                           }

                           throw var36;
                        }

                        if (fp != null) {
                           fp.close();
                        }

                        done = true;
                        var46 = base;
                        break;
                     } finally {
                        if (!done) {
                           try {
                              Files.delete(propname);
                           } catch (NoSuchFileException var33) {
                           }

                           try {
                              Files.delete(base);
                           } catch (NoSuchFileException var32) {
                           }
                        }
                     }
                  }

                  Properties props = new Properties();

                  label217: {
                     try {
                        InputStream fp = Files.newInputStream(propname);

                        try {
                           props.load(fp);
                        } catch (Throwable var37) {
                           if (fp != null) {
                              try {
                                 fp.close();
                              } catch (Throwable var35) {
                                 var37.addSuppressed(var35);
                              }
                           }

                           throw var37;
                        }

                        if (fp != null) {
                           fp.close();
                        }
                     } catch (NoSuchFileException var38) {
                        break label217;
                     }

                     if (Utils.eq(props.get("base"), idstr)) {
                        var44 = base;
                        break label243;
                     }
                  }

                  idx++;
               }
            } catch (Throwable var40) {
               if (lock != null) {
                  try {
                     lock.close();
                  } catch (Throwable var31) {
                     var40.addSuppressed(var31);
                  }
               }

               throw var40;
            }

            if (lock != null) {
               lock.close();
            }

            return var46;
         }

         if (lock != null) {
            lock.close();
         }

         return var44;
      }
   }

   public BaseFileCache(URI id) throws IOException {
      this.id = id;
      this.base = findbase(id);
   }

   public static BaseFileCache get(URI id) throws IOException {
      synchronized (current) {
         BaseFileCache ret = current.get(id);
         if (ret == null) {
            current.put(id, ret = new BaseFileCache(id));
         }

         return ret;
      }
   }

   private static URI mkurn(String id) {
      return Utils.uri("urn:haven-cache:" + id);
   }

   public static BaseFileCache get(String id) throws IOException {
      return get(mkurn(id));
   }

   public static BaseFileCache create() {
      try {
         if (Config.cachebase != null) {
            return get(Config.cachebase);
         } else {
            return Config.resurl != null ? get(Config.resurl.toURI()) : get("default");
         }
      } catch (Exception var2) {
         return null;
      }
   }

   private String mangle(String el) {
      if (Resource.FileSource.windows) {
         StringBuilder buf = new StringBuilder();

         for (int i = 0; i < el.length(); i++) {
            char c = el.charAt(i);
            if (c != '@' && Resource.FileSource.winsafechar(c)) {
               buf.append(c);
            } else {
               buf.append('@');
               buf.append(Utils.num2hex((c & '\uf000') >> 12));
               buf.append(Utils.num2hex((c & 3840) >> 8));
               buf.append(Utils.num2hex((c & 240) >> 4));
               buf.append(Utils.num2hex((c & 15) >> 0));
            }
         }

         el = buf.toString();
      }

      return !Resource.FileSource.windows || !el.startsWith("windows-special-") && !Resource.FileSource.wintraps.contains(el) ? el : "windows-special-" + el;
   }

   private Path forres(String nm) {
      Path res = this.base;
      String[] comp = nm.split("/");

      for (int i = 0; i < comp.length - 1; i++) {
         res = res.resolve(this.mangle(comp[i]));
      }

      return res.resolve(comp[comp.length - 1] + ".cached");
   }

   @Override
   public InputStream fetch(String name) throws IOException {
      try {
         return Files.newInputStream(this.forres(name));
      } catch (NoSuchFileException var3) {
         throw (FileNotFoundException)new FileNotFoundException(name).initCause(var3);
      }
   }

   @Override
   public OutputStream store(String name) throws IOException {
      final Path path = this.forres(name);
      Path dir = path.getParent();
      if (!Files.exists(dir)) {
         Files.createDirectories(dir);
      }

      final Path tmp = Files.createTempFile(dir, "cache", ".new");
      final OutputStream fp = Files.newOutputStream(tmp);
      return new OutputStream() {
         private boolean closed = false;

         @Override
         public void write(int b) throws IOException {
            fp.write(b);
         }

         @Override
         public void write(byte[] buf, int off, int len) throws IOException {
            fp.write(buf, off, len);
         }

         @Override
         public void close() throws IOException {
            fp.close();

            try {
               Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException var2) {
               Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }

            this.closed = true;
         }

         @Override
         protected void finalize() {
            if (!this.closed) {
               try {
                  fp.close();
                  Files.delete(tmp);
               } catch (IOException var2) {
               }
            }
         }
      };
   }

   public void remove(String name) throws IOException {
      try {
         Files.delete(this.forres(name));
      } catch (NoSuchFileException var3) {
         throw (FileNotFoundException)new FileNotFoundException(name).initCause(var3);
      }
   }

   @Override
   public String toString() {
      return "BaseFileCache(" + this.base + ")";
   }
}
