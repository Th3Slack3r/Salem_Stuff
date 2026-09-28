package haven.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
   public static final Charset utf8 = Charset.forName("UTF-8");

   public static <E> E pop(Collection<E> c) {
      Iterator<E> i = c.iterator();
      E ret = i.next();
      i.remove();
      return ret;
   }

   public static char num2hex(int num) {
      return num < 10 ? (char)(48 + num) : (char)(65 + num - 10);
   }

   public static String byte2hex(byte[] in) {
      StringBuilder buf = new StringBuilder();

      for (byte b : in) {
         buf.append(num2hex((b & 240) >> 4));
         buf.append(num2hex(b & 15));
      }

      return buf.toString();
   }

   public static String basename(URI uri) {
      String path = uri.getPath();
      int p = path.lastIndexOf(47);
      return p < 0 ? path : path.substring(p + 1);
   }

   public static String[] splitwords(String text) {
      ArrayList<String> words = new ArrayList<>();
      StringBuilder buf = new StringBuilder();
      String st = "ws";
      int i = 0;

      while (i < text.length()) {
         char c = text.charAt(i);
         if (st == "ws") {
            if (!Character.isWhitespace(c)) {
               st = "word";
            } else {
               i++;
            }
         } else if (st == "word") {
            if (c == '"') {
               st = "quote";
               i++;
            } else if (c == '\\') {
               st = "squote";
               i++;
            } else if (Character.isWhitespace(c)) {
               words.add(buf.toString());
               buf = new StringBuilder();
               st = "ws";
            } else {
               buf.append(c);
               i++;
            }
         } else if (st == "quote") {
            if (c == '"') {
               st = "word";
               i++;
            } else if (c == '\\') {
               st = "sqquote";
               i++;
            } else {
               buf.append(c);
               i++;
            }
         } else if (st == "squote") {
            buf.append(c);
            i++;
            st = "word";
         } else if (st == "sqquote") {
            buf.append(c);
            i++;
            st = "quote";
         }
      }

      if (st == "word") {
         words.add(buf.toString());
      }

      return st != "ws" && st != "word" ? null : words.toArray(new String[0]);
   }

   public static Certificate[] checkjar(Path path, Status prog) throws IOException {
      Set<Certificate> ret = null;
      JarFile jar = new JarFile(path.toFile());

      Certificate[] var14;
      label98: {
         Certificate[] var16;
         label97: {
            label102: {
               try {
                  if (jar.getManifest() == null) {
                     var14 = new Certificate[0];
                     break label98;
                  }

                  byte[] buf = new byte[65536];
                  Enumeration<JarEntry> i = jar.entries();

                  while (i.hasMoreElements()) {
                     JarEntry ent = i.nextElement();
                     if (!ent.isDirectory()) {
                        if (prog != null) {
                           prog.progress();
                        }

                        InputStream st = jar.getInputStream(ent);

                        try {
                           while (st.read(buf, 0, buf.length) >= 0) {
                           }
                        } catch (Throwable var12) {
                           if (st != null) {
                              try {
                                 st.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (st != null) {
                           st.close();
                        }

                        if (!ent.getName().startsWith("META-INF")) {
                           Certificate[] entc = ent.getCertificates();
                           if (entc == null || entc.length < 1) {
                              var16 = new Certificate[0];
                              break label97;
                           }

                           if (ret == null) {
                              ret = new HashSet<>(Arrays.asList(entc));
                           } else {
                              ret.retainAll(Arrays.asList(entc));
                              if (ret.size() < 1) {
                                 var16 = new Certificate[0];
                                 break label102;
                              }
                           }
                        }
                     }
                  }
               } catch (Throwable var13) {
                  try {
                     jar.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }

                  throw var13;
               }

               jar.close();
               if (ret == null) {
                  return new Certificate[0];
               }

               return ret.toArray(new Certificate[0]);
            }

            jar.close();
            return var16;
         }

         jar.close();
         return var16;
      }

      jar.close();
      return var14;
   }

   public static Path path(String path) {
      return FileSystems.getDefault().getPath(path);
   }

   public static Path pj(Path base, String... els) {
      for (String el : els) {
         base = base.resolve(el);
      }

      return base;
   }
}
