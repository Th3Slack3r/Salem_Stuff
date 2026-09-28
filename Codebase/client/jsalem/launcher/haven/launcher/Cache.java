package haven.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;

public class Cache {
   public static final String USER_AGENT;
   private final Path base = findbase();
   private static Cache global;
   private static final String safe = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_. ";
   private static final SslHelper ssl;

   private static Path findbase() {
      try {
         String path = System.getProperty("haven.launcher.cache-path");
         if (path == null) {
            path = System.getenv("LAUNCHER_CACHE");
         }

         if (path != null) {
            Path base = Utils.path(path);
            if (Files.exists(base) && Files.isDirectory(base) && Files.isReadable(base) && Files.isWritable(base)) {
               return base;
            }

            throw new UnsupportedOperationException(base + ": cache does not exist or is not usable");
         }

         path = System.getenv("LOCALAPPDATA");
         if (path == null) {
            path = System.getenv("APPDATA");
         }

         if (path != null) {
            Path appdata = Utils.path(path);
            label75:
            if (Files.exists(appdata) && Files.isDirectory(appdata) && Files.isReadable(appdata) && Files.isWritable(appdata)) {
               Path base = Utils.pj(appdata, "Haven Launcher");
               if (!Files.exists(base)) {
                  try {
                     Files.createDirectories(base);
                  } catch (IOException var5) {
                     break label75;
                  }
               }

               return base;
            }
         }

         path = System.getProperty("user.home", null);
         if (path != null) {
            Path home = Utils.path(path);
            if (Files.exists(home) && Files.isDirectory(home) && Files.isReadable(home) && Files.isWritable(home)) {
               Path base = Utils.pj(home, ".cache", "haven-launcher");
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

   public static synchronized Cache get() {
      if (global == null) {
         global = new Cache();
      }

      return global;
   }

   private static String mangle(String el) {
      StringBuilder buf = new StringBuilder();

      for (int i = 0; i < el.length(); i++) {
         char c = el.charAt(i);
         if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_. ".indexOf(c) < 0 || i == 0 && c == '.') {
            if (c < 256) {
               buf.append(String.format("%%%02x", Integer.valueOf(c)));
            } else {
               buf.append(String.format("%%%04x", Integer.valueOf(c)));
            }
         } else {
            buf.append(c);
         }
      }

      return buf.toString();
   }

   public Path mangle(URI uri) {
      Path ret = Utils.pj(this.base, "cache", mangle(uri.getScheme()));
      if (uri.getAuthority() != null) {
         ret = Utils.pj(ret, mangle(uri.getAuthority()));
      }

      String path = uri.getPath();
      int p = 0;

      while (true) {
         int n = path.indexOf(47, p);
         if (n < 0) {
            n = path.length();
         }

         if (n > p) {
            ret = Utils.pj(ret, mangle(path.substring(p, n)));
         }

         if (n >= path.length()) {
            if (uri.getQuery() != null) {
               ret = Utils.pj(ret, mangle(uri.getQuery()));
            }

            return ret;
         }

         p = n + 1;
      }
   }

   public Path metafile(URI uri, String var) {
      Path ret = this.mangle(uri);
      return ret.resolveSibling("." + ret.getFileName() + "." + var);
   }

   private void addcert(Collection<String> buf, Certificate cert) {
      try {
         buf.add("dig:sha256:" + Utils.byte2hex(MessageDigest.getInstance("SHA-256").digest(cert.getEncoded())));
         PublicKey key = cert.getPublicKey();
         if (key instanceof RSAPublicKey) {
            RSAPublicKey rsakey = (RSAPublicKey)key;
            MessageDigest dig = MessageDigest.getInstance("SHA-256");
            dig.update(rsakey.getPublicExponent().toString().getBytes(Utils.utf8));
            dig.update(new byte[]{58});
            dig.update(rsakey.getModulus().toString().getBytes(Utils.utf8));
            buf.add("key:rsa:" + Utils.byte2hex(dig.digest()));
         }

         if (key instanceof DSAPublicKey) {
            DSAPublicKey dsakey = (DSAPublicKey)key;
            MessageDigest dig = MessageDigest.getInstance("SHA-256");
            DSAParams dsapar = dsakey.getParams();
            dig.update(dsapar.getG().toString().getBytes(Utils.utf8));
            dig.update(new byte[]{58});
            dig.update(dsapar.getP().toString().getBytes(Utils.utf8));
            dig.update(new byte[]{58});
            dig.update(dsapar.getQ().toString().getBytes(Utils.utf8));
            dig.update(new byte[]{58});
            dig.update(dsakey.getY().toString().getBytes(Utils.utf8));
            buf.add("key:dsa:" + Utils.byte2hex(dig.digest()));
         }
      } catch (CertificateEncodingException var7) {
      } catch (NoSuchAlgorithmException var8) {
         throw new AssertionError(var8);
      }
   }

   private static boolean dokludgerepl() {
      String os = System.getProperty("os.name");
      return os != null && os.startsWith("Windows");
   }

   private static void overwrite(Path dst, Path src) throws IOException {
      InputStream in = Files.newInputStream(src);

      try {
         OutputStream out = Files.newOutputStream(dst);

         try {
            byte[] buf = new byte[65536];

            for (int rv = in.read(buf); rv >= 0; rv = in.read(buf)) {
               out.write(buf, 0, rv);
            }
         } catch (Throwable var8) {
            if (out != null) {
               try {
                  out.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (out != null) {
            out.close();
         }
      } catch (Throwable var9) {
         if (in != null) {
            try {
               in.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (in != null) {
         in.close();
      }
   }

   private Cached update0(Resource res, boolean force) throws IOException {
      URI uri = res.uri;
      Status st = Status.current();

      Cached var45;
      label238: {
         Cached var48;
         try {
            FileLock lk;
            label248: {
               st.messagef("Checking %s...", Utils.basename(uri));
               Path path = this.mangle(uri);
               Path infop = this.metafile(uri, "info");
               Path newp = this.metafile(uri, "new");
               Path dir = path.getParent();
               if (!Files.isDirectory(dir)) {
                  Files.createDirectories(dir);
               }

               FileChannel fp = FileChannel.open(infop, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
               Properties props = new Properties();
               Properties nprops = new Properties();
               nprops.put("source", uri.toString());
               lk = fp.lock();

               try {
                  fp.position(0L);
                  props.load(new BufferedReader(new InputStreamReader(Channels.newInputStream(fp), Utils.utf8)));
                  URL url = uri.toURL();
                  URLConnection conn = null;
                  if (conn == null) {
                     conn = ssl.connect(url);
                  }

                  if (conn == null) {
                     conn = uri.toURL().openConnection();
                  }

                  conn.setConnectTimeout(5000);
                  conn.setReadTimeout(5000);
                  HttpURLConnection http = conn instanceof HttpURLConnection ? (HttpURLConnection)conn : null;
                  conn.addRequestProperty("User-Agent", USER_AGENT);
                  if (res.referrer != null) {
                     conn.addRequestProperty("Referer", String.valueOf(res.referrer));
                  }

                  if (http != null) {
                     http.setUseCaches(false);
                     if (!force && props.containsKey("mtime")) {
                        http.setRequestProperty("If-Modified-Since", (String)props.get("mtime"));
                     }
                  }

                  conn.connect();
                  if (conn instanceof HttpsURLConnection) {
                     Collection<String> certinfo = new ArrayList<>();

                     for (Certificate cert : ((HttpsURLConnection)conn).getServerCertificates()) {
                        this.addcert(certinfo, cert);
                     }

                     nprops.put("tls-certs", String.join(" ", certinfo));
                  }

                  long bytes = 0L;
                  long expected = -1L;
                  InputStream in = conn.getInputStream();

                  label243: {
                     try {
                        if (http != null) {
                           expected = http.getContentLengthLong();
                           if (!force && http.getResponseCode() == 304) {
                              var45 = new Cached(path, props, false);
                              break label243;
                           }

                           if (http.getResponseCode() != 200) {
                              throw new IOException("Unexpected HTTP response code: " + http.getResponseCode());
                           }
                        }

                        st.messagef("Fetching %s...", Utils.basename(uri));
                        st.transfer(expected, 0L);
                        byte[] buf = new byte[65536];
                        OutputStream out = Files.newOutputStream(newp);

                        try {
                           for (int rv = in.read(buf); rv >= 0; rv = in.read(buf)) {
                              out.write(buf, 0, rv);
                              bytes += rv;
                              st.transfer(expected, bytes);
                           }
                        } catch (Throwable var34) {
                           if (out != null) {
                              try {
                                 out.close();
                              } catch (Throwable var29) {
                                 var34.addSuppressed(var29);
                              }
                           }

                           throw var34;
                        }

                        if (out != null) {
                           out.close();
                        }
                     } catch (Throwable var35) {
                        if (in != null) {
                           try {
                              in.close();
                           } catch (Throwable var28) {
                              var35.addSuppressed(var28);
                           }
                        }

                        throw var35;
                     }

                     if (in != null) {
                        in.close();
                     }

                     if (http != null) {
                        long clen = http.getContentLengthLong();
                        if (clen != bytes) {
                           throw new IOException("Premature EOF");
                        }

                        String mtime = http.getHeaderField("Last-Modified");
                        if (mtime != null) {
                           nprops.put("mtime", mtime);
                        }
                     }

                     String ctype = conn.getContentType();
                     if (ctype != null) {
                        nprops.put("ctype", ctype);
                     }

                     if (ctype.equals("application/java-archive")) {
                        st.messagef("Verifying %s...", Utils.basename(uri));
                        Collection<String> certinfo = new ArrayList<>();

                        for (Certificate cert : Utils.checkjar(newp, st)) {
                           this.addcert(certinfo, cert);
                        }

                        if (!certinfo.isEmpty()) {
                           nprops.put("jar-certs", String.join(" ", certinfo));
                        }
                     }

                     fp.position(0L);
                     fp.truncate(0L);

                     try {
                        try {
                           Files.move(newp, path, StandardCopyOption.ATOMIC_MOVE);
                        } catch (AtomicMoveNotSupportedException var32) {
                           Files.move(newp, path, StandardCopyOption.REPLACE_EXISTING);
                        }
                     } catch (IOException var33) {
                        if (!dokludgerepl()) {
                           throw var33;
                        }

                        try {
                           overwrite(path, newp);

                           try {
                              Files.delete(newp);
                           } catch (IOException var30) {
                           }
                        } catch (IOException var31) {
                           var31.addSuppressed(var33);
                           throw new Cache.FileReplaceException(var31);
                        }
                     }

                     Writer propout = new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(fp), Utils.utf8));
                     nprops.store(propout, null);
                     propout.flush();
                     var48 = new Cached(path, nprops, true);
                     break label248;
                  }

                  if (in != null) {
                     in.close();
                  }
               } catch (Throwable var36) {
                  if (lk != null) {
                     try {
                        lk.close();
                     } catch (Throwable var27) {
                        var36.addSuppressed(var27);
                     }
                  }

                  throw var36;
               }

               if (lk != null) {
                  lk.close();
               }
               break label238;
            }

            if (lk != null) {
               lk.close();
            }
         } catch (Throwable var37) {
            if (st != null) {
               try {
                  st.close();
               } catch (Throwable var26) {
                  var37.addSuppressed(var26);
               }
            }

            throw var37;
         }

         if (st != null) {
            st.close();
         }

         return var48;
      }

      if (st != null) {
         st.close();
      }

      return var45;
   }

   public Cached update(Resource res, boolean force) throws IOException {
      List<IOException> errors = new ArrayList<>();

      for (int retry = 0; retry < 3; retry++) {
         try {
            return this.update0(res, force);
         } catch (IOException var6) {
            errors.add(var6);
            force = true;
         }
      }

      IOException first = errors.get(0);

      for (int i = 1; i < errors.size(); i++) {
         first.addSuppressed(errors.get(i));
      }

      throw first;
   }

   static {
      StringBuilder buf = new StringBuilder();
      buf.append(String.format("Haven-Launcher/%d.%d", Config.MAJOR_VERSION, Config.MINOR_VERSION));
      String jv = System.getProperty("java.version");
      if (jv != null && jv.length() > 0) {
         buf.append(String.format(" Java/%s", jv));
      }

      USER_AGENT = buf.toString();
      global = null;
      ssl = new SslHelper();
   }

   public static class FileReplaceException extends IOException implements ErrorMessage {
      public FileReplaceException(Throwable cause) {
         super("could not replace out-of-date file with newly downloaded file", cause);
      }

      @Override
      public String usermessage() {
         return "Could not replace out-of-date file with newly downloaded file. If the program is currently running, please quit it and try again.";
      }
   }
}
