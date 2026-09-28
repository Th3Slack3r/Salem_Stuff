package dolda.jglob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Loader {
   private final Class<? extends Annotation> an;
   private final ClassLoader cl;

   private Loader(Class<? extends Annotation> annotation, ClassLoader loader) {
      this.an = annotation;
      this.cl = loader;
   }

   public Iterable<String> names() {
      return new Iterable<String>() {
         @Override
         public Iterator<String> iterator() {
            return new Iterator<String>() {
               private Enumeration<URL> rls;
               private Iterator<String> cur = null;

               private Iterator<String> parse(URL url) {
                  try {
                     List<String> buf = new LinkedList<>();
                     InputStream in = url.openStream();

                     Iterator var6;
                     try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));

                        String ln;
                        while ((ln = r.readLine()) != null) {
                           ln = ln.trim();
                           if (ln.length() >= 1) {
                              buf.add(ln);
                           }
                        }

                        var6 = buf.iterator();
                     } finally {
                        in.close();
                     }

                     return var6;
                  } catch (IOException var11) {
                     throw new GlobAccessException(var11);
                  }
               }

               @Override
               public boolean hasNext() {
                  if (this.cur == null || !this.cur.hasNext()) {
                     if (this.rls == null) {
                        try {
                           this.rls = Loader.this.cl.getResources("META-INF/glob/" + Loader.this.an.getName());
                        } catch (IOException var2) {
                           throw new GlobAccessException(var2);
                        }
                     }

                     if (!this.rls.hasMoreElements()) {
                        return false;
                     }

                     URL u = this.rls.nextElement();
                     this.cur = this.parse(u);
                  }

                  return true;
               }

               public String next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     return this.cur.next();
                  }
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public Iterable<Class<?>> classes() {
      return new Iterable<Class<?>>() {
         @Override
         public Iterator<Class<?>> iterator() {
            return new Iterator<Class<?>>() {
               private final Iterator<String> names = Loader.this.names().iterator();
               private Class<?> n = null;

               @Override
               public boolean hasNext() {
                  while (this.n == null) {
                     if (!this.names.hasNext()) {
                        return false;
                     }

                     String nm = this.names.next();

                     Class<?> c;
                     try {
                        c = Loader.this.cl.loadClass(nm);
                     } catch (ClassNotFoundException var4) {
                        continue;
                     }

                     if (c.getAnnotation(Loader.this.an) != null) {
                        this.n = c;
                     }
                  }

                  return true;
               }

               public Class<?> next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     Class<?> r = this.n;
                     this.n = null;
                     return r;
                  }
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public <T> Iterable<T> instances(final Class<T> cast) {
      return new Iterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return new Iterator<T>() {
               private final Iterator<Class<?>> classes = Loader.this.classes().iterator();
               private T n = (T)null;

               @Override
               public boolean hasNext() {
                  while (this.n == null) {
                     if (!this.classes.hasNext()) {
                        return false;
                     }

                     Class<?> cl = this.classes.next();

                     T inst;
                     try {
                        inst = cast.cast(cl.newInstance());
                     } catch (InstantiationException var4) {
                        throw new GlobInstantiationException(var4);
                     } catch (IllegalAccessException var5) {
                        throw new GlobInstantiationException(var5);
                     }

                     this.n = inst;
                  }

                  return true;
               }

               @Override
               public T next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     T r = this.n;
                     this.n = null;
                     return r;
                  }
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public Iterable<?> instances() {
      return this.instances(Object.class);
   }

   public static Loader get(Class<? extends Annotation> annotation, ClassLoader loader) {
      return new Loader(annotation, loader);
   }

   public static Loader get(Class<? extends Annotation> annotation) {
      return get(annotation, annotation.getClassLoader());
   }
}
