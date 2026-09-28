package org.ender.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater {
   public UpdaterConfig cfg;
   private IUpdaterListener listener;

   public Updater(IUpdaterListener listener) {
      this.listener = listener;
      this.cfg = new UpdaterConfig();
   }

   public void update() {
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            List<UpdaterConfig.Item> update = new ArrayList<>();

            for (UpdaterConfig.Item item : Updater.this.cfg.items) {
               if (Updater.this.correct_platform(item)) {
                  Updater.this.set_date(item);
                  if (Updater.this.has_update(item)) {
                     Updater.this.listener.log(String.format("Updates found for '%s'", item.file.getName()));
                     update.add(item);
                  } else {
                     Updater.this.listener.log(String.format("No updates for '%s'", item.file.getName()));
                  }
               }
            }

            for (UpdaterConfig.Item itemx : update) {
               Updater.this.download(itemx);
               if (itemx.extract != null) {
                  Updater.this.extract(itemx);
               }
            }

            Updater.this.listener.fisnished();
         }
      });
      t.setDaemon(true);
      t.start();
   }

   private boolean correct_platform(UpdaterConfig.Item item) {
      String os = System.getProperty("os.name");
      String arch = System.getProperty("os.arch");
      return os.indexOf(item.os) >= 0 && (arch.equals(item.arch) || item.arch.length() == 0);
   }

   private void set_date(UpdaterConfig.Item item) {
      if (item.file.exists()) {
         item.date = item.file.lastModified();
      }
   }

   private boolean has_update(UpdaterConfig.Item item) {
      try {
         URL url = new URL(item.link);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("HEAD");
         conn.setIfModifiedSince(item.date);
         HttpURLConnection.setFollowRedirects(true);

         try {
            if (conn.getResponseCode() == 200) {
               item.size = Long.parseLong(conn.getHeaderField("Content-Length"));
               return true;
            }

            if (conn.getResponseCode() == 301) {
               item.link = conn.getHeaderField("Location");
               return true;
            }
         } catch (Exception var5) {
            this.listener.log("error1: " + var5);
         }

         conn.disconnect();
      } catch (Exception var6) {
         this.listener.log("error2: " + var6);
      }

      return false;
   }

   private boolean has_update_etag(UpdaterConfig.Item item) {
      try {
         URL url = new URL(item.link);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("HEAD");
         conn.setIfModifiedSince(item.date);

         try {
            if (conn.getResponseCode() == 200) {
               boolean etagdiff = true;
               if (conn.getHeaderField("etag") != null) {
                  File etagfile = new File(item.file.getPath() + ".etag");
                  if (etagfile.exists() && etagfile.isFile()) {
                     try {
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(etagfile));
                        String etag = String.valueOf(ois.readObject());
                        ois.close();
                        if (etag.equals(conn.getHeaderField("etag"))) {
                           etagdiff = false;
                        } else {
                           ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(etagfile));
                           oos.writeObject(conn.getHeaderField("etag"));
                           oos.close();
                        }
                     } catch (ClassNotFoundException var9) {
                     } catch (IOException var10) {
                     }
                  } else {
                     ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(etagfile));
                     oos.writeObject(conn.getHeaderField("etag"));
                     oos.close();
                  }
               }

               if (etagdiff) {
                  item.size = Long.parseLong(conn.getHeaderField("Content-Length"));
                  return true;
               }
            }
         } catch (NumberFormatException var11) {
         }

         conn.disconnect();
      } catch (MalformedURLException var12) {
         var12.printStackTrace();
      } catch (IOException var13) {
         var13.printStackTrace();
      }

      return false;
   }

   private void download(UpdaterConfig.Item item) {
      this.listener.log(String.format("Downloading '%s'", item.file.getName()));

      try {
         URL link = new URL(item.link);
         ReadableByteChannel rbc = Channels.newChannel(link.openStream());
         FileOutputStream fos = new FileOutputStream(item.file);
         long position = 0L;
         int step = 20480;
         this.listener.progress(position, item.size);

         while (position < item.size) {
            position += fos.getChannel().transferFrom(rbc, position, step);
            this.listener.progress(position, item.size);
         }

         this.listener.progress(0L, item.size);
         fos.close();
      } catch (MalformedURLException var8) {
         var8.printStackTrace();
      } catch (IOException var9) {
         var9.printStackTrace();
      }
   }

   private void extract(UpdaterConfig.Item item) {
      this.listener.log(String.format("Unpacking '%s'", item.file.getName()));

      try {
         ZipFile zip = new ZipFile(item.file);
         Enumeration<? extends ZipEntry> contents = zip.entries();

         while (contents.hasMoreElements()) {
            ZipEntry file = contents.nextElement();
            String name = file.getName();
            if (name.indexOf("META-INF") != 0) {
               this.listener.log("\t" + name);
               ReadableByteChannel rbc = Channels.newChannel(zip.getInputStream(file));
               FileOutputStream fos = new FileOutputStream(new File(item.extract, name));
               long position = 0L;
               long size = file.getSize();
               int step = 20480;

               while (position < size) {
                  position += fos.getChannel().transferFrom(rbc, position, step);
               }

               fos.close();
            }
         }
      } catch (IOException var13) {
         var13.printStackTrace();
      }
   }
}
