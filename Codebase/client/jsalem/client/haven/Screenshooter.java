package haven;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Screenshooter extends Window {
   public static final ComponentColorModel outcm = new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8}, false, false, 1, 0);
   public final URL tgt;
   public final Screenshooter.Shot shot;
   private final TextEntry comment;
   private final CheckBox decobox;
   private final CheckBox pub;
   private final int w;
   private final int h;
   private Label prog;
   private Coord btnc;
   private Button btn;
   public static final Screenshooter.ImageFormat png = new Screenshooter.ImageFormat() {
      @Override
      public String ctype() {
         return "image/png";
      }

      void cmt(Node tlist, String key, String val) {
         Element cmt = new IIOMetadataNode("TextEntry");
         cmt.setAttribute("keyword", key);
         cmt.setAttribute("value", val);
         cmt.setAttribute("encoding", "utf-8");
         cmt.setAttribute("language", "");
         cmt.setAttribute("compression", "none");
         tlist.appendChild(cmt);
      }

      @Override
      public void write(OutputStream out, BufferedImage img, Screenshooter.Shot info) throws IOException {
         ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
         ImageWriter wr = ImageIO.getImageWriters(type, "PNG").next();
         IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
         Node root = dat.getAsTree("javax_imageio_1.0");
         Node tlist = new IIOMetadataNode("Text");
         if (info.comment != null) {
            this.cmt(tlist, "Comment", info.comment);
         }

         this.cmt(tlist, "haven.fsaa", info.fsaa ? "y" : "n");
         this.cmt(tlist, "haven.flight", info.fl ? "y" : "n");
         this.cmt(tlist, "haven.sdw", info.sdw ? "y" : "n");
         this.cmt(tlist, "haven.conf", "default/1");
         root.appendChild(tlist);
         dat.setFromTree("javax_imageio_1.0", root);
         ImageOutputStream iout = ImageIO.createImageOutputStream(out);
         wr.setOutput(iout);
         wr.write(new IIOImage(img, null, dat));
      }
   };
   public static final Screenshooter.ImageFormat jpeg = new Screenshooter.ImageFormat() {
      @Override
      public String ctype() {
         return "image/jpeg";
      }

      @Override
      public void write(OutputStream out, BufferedImage img, Screenshooter.Shot info) throws IOException {
         ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
         ImageWriter wr = ImageIO.getImageWriters(type, "JPEG").next();
         IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
         Node root = dat.getAsTree("javax_imageio_jpeg_image_1.0");
         Node mseq = root.getFirstChild();

         while (mseq != null && !mseq.getLocalName().equals("markerSequence")) {
            mseq = mseq.getNextSibling();
         }

         if (mseq == null) {
            mseq = new IIOMetadataNode("markerSequence");
            root.appendChild(mseq);
         }

         if (info.comment != null) {
            IIOMetadataNode cmt = new IIOMetadataNode("com");
            cmt.setUserObject(info.comment.getBytes("utf-8"));
            mseq.appendChild(cmt);
         }

         Message hdat = new Message(0);
         hdat.addstring2("HSSI1");
         hdat.addstring("fsaa");
         hdat.addstring(info.fsaa ? "y" : "n");
         hdat.addstring("flight");
         hdat.addstring(info.fl ? "y" : "n");
         hdat.addstring("sdw");
         hdat.addstring(info.sdw ? "y" : "n");
         hdat.addstring("conf");
         hdat.addstring("default/1");
         IIOMetadataNode app4 = new IIOMetadataNode("unknown");
         app4.setAttribute("MarkerTag", "228");
         app4.setUserObject(hdat.blob);
         mseq.appendChild(app4);
         dat.setFromTree("javax_imageio_jpeg_image_1.0", root);
         ImageOutputStream iout = ImageIO.createImageOutputStream(out);
         wr.setOutput(iout);
         wr.write(new IIOImage(img, null, dat));
      }
   };

   public Screenshooter(Coord c, Widget parent, URL tgt, Screenshooter.Shot shot) {
      super(c, Coord.z, parent, "Screenshot");
      this.tgt = tgt;
      this.shot = shot;
      this.w = Math.min(200 * shot.sz.x / shot.sz.y, 150);
      this.h = this.w * shot.sz.y / shot.sz.x;
      this.decobox = new CheckBox(new Coord(this.w, (this.h - CheckBox.box.sz().y) / 2), this, "Include interface");
      Label clbl = new Label(new Coord(0, this.h + 5), this, "If you wish, leave a comment:");
      this.comment = new TextEntry(new Coord(0, clbl.c.y + clbl.sz.y + 5), this.w + 130, this, "") {
         @Override
         public void activate(String text) {
            Screenshooter.this.upload();
         }
      };
      this.pub = new CheckBox(new Coord(0, this.comment.c.y + this.comment.sz.y + 5), this, "Make public");
      this.pub.a = true;
      this.btnc = new Coord((this.comment.sz.x - 125) / 2, this.pub.c.y + this.pub.sz.y + 20);
      this.btn = new Button(this.btnc, 125, this, "Upload") {
         @Override
         public void click() {
            Screenshooter.this.upload();
         }
      };
      this.pack();
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this && msg == "close") {
         this.ui.destroy(this);
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void cdraw(GOut g) {
      TexI tex = this.decobox.a ? this.shot.ui : this.shot.map;
      g.image(tex, Coord.z, new Coord(this.w, this.h));
   }

   public void upload() {
      this.shot.comment = this.comment.text;
      final Screenshooter.Uploader th = new Screenshooter.Uploader(this.decobox.a ? this.shot.ui : this.shot.map, this.shot, jpeg);
      th.start();
      this.ui.destroy(this.btn);
      this.btn = new Button(this.btnc, 125, this, "Cancel") {
         @Override
         public void click() {
            th.interrupt();
         }
      };
   }

   public static void take(final GameUI gameui, final URL tgt) {
      new Object() {
         TexI[] ss = new TexI[]{null, null};

         {
            gameui.map.delay2(new MapView.Delayed() {
               @Override
               public void run(GOut g) {
                  ss[0] = new TexI(g.getimage(Coord.z, g.sz));
                  ss[0].minfilter = 9729;
                  checkcomplete(g);
               }
            });
            gameui.ui.drawafter(new UI.AfterDraw() {
               @Override
               public void draw(GOut g) {
                  ss[1] = new TexI(g.getimage(Coord.z, g.sz));
                  checkcomplete(g);
               }
            });
         }

         private void checkcomplete(GOut g) {
            if (this.ss[0] != null && this.ss[1] != null) {
               Screenshooter.Shot shot = new Screenshooter.Shot(this.ss[0], this.ss[1]);
               shot.fl = g.gc.pref.flight.val;
               shot.sdw = g.gc.pref.lshadow.val;
               shot.fsaa = g.gc.pref.fsaa.val;
               new Screenshooter(new Coord(100, 100), gameui, tgt, shot);
            }
         }
      };
   }

   public interface ImageFormat {
      String ctype();

      void write(OutputStream var1, BufferedImage var2, Screenshooter.Shot var3) throws IOException;
   }

   public static class Shot {
      public final TexI map;
      public final TexI ui;
      public final Coord sz;
      public String comment;
      public boolean fsaa;
      public boolean fl;
      public boolean sdw;

      public Shot(TexI map, TexI ui) {
         this.map = map;
         this.ui = ui;
         this.sz = map.sz();
      }
   }

   public class Uploader extends HackThread {
      private final TexI img;
      private final Screenshooter.Shot info;
      private final Screenshooter.ImageFormat fmt;

      public Uploader(TexI img, Screenshooter.Shot info, Screenshooter.ImageFormat fmt) {
         super("Screenshot uploader");
         this.img = img;
         this.info = info;
         this.fmt = fmt;
      }

      @Override
      public void run() {
         try {
            this.upload(this.img, this.info, this.fmt);
         } catch (InterruptedIOException var7) {
            this.setstate("Cancelled");
            synchronized (Screenshooter.this.ui) {
               Screenshooter.this.ui.destroy(Screenshooter.this.btn);
               Screenshooter.this.btn = new Button(Screenshooter.this.btnc, 125, Screenshooter.this, "Retry") {
                  @Override
                  public void click() {
                     Screenshooter.this.upload();
                  }
               };
            }
         } catch (IOException var8) {
            this.setstate("Could not upload image");
            synchronized (Screenshooter.this.ui) {
               Screenshooter.this.ui.destroy(Screenshooter.this.btn);
               Screenshooter.this.btn = new Button(Screenshooter.this.btnc, 125, Screenshooter.this, "Retry") {
                  @Override
                  public void click() {
                     Screenshooter.this.upload();
                  }
               };
            }
         }
      }

      private void setstate(String t) {
         synchronized (Screenshooter.this.ui) {
            if (Screenshooter.this.prog != null) {
               Screenshooter.this.ui.destroy(Screenshooter.this.prog);
            }

            Screenshooter.this.prog = new Label(Screenshooter.this.btnc.sub(0, 15), Screenshooter.this, t);
         }
      }

      private BufferedImage convert(BufferedImage img) {
         WritableRaster buf = PUtils.byteraster(PUtils.imgsz(img), 3);
         BufferedImage ret = new BufferedImage(Screenshooter.outcm, buf, false, null);
         Graphics g = ret.getGraphics();
         g.drawImage(img, 0, 0, null);
         g.dispose();
         return ret;
      }

      public void upload(TexI ss, Screenshooter.Shot info, Screenshooter.ImageFormat fmt) throws IOException {
         this.setstate("Preparing image...");
         ByteArrayOutputStream buf = new ByteArrayOutputStream();
         fmt.write(buf, this.convert(ss.back), info);
         byte[] data = buf.toByteArray();
         ByteArrayOutputStream var28 = null;
         this.setstate("Connecting...");
         URL pared = Utils.urlparam(Screenshooter.this.tgt, "p", Screenshooter.this.pub.a ? "y" : "n");
         HttpURLConnection conn = (HttpURLConnection)pared.openConnection();
         conn.setDoOutput(true);
         conn.setFixedLengthStreamingMode(data.length);
         conn.addRequestProperty("Content-Type", fmt.ctype());
         Message auth = new Message(0);
         auth.addstring2(Screenshooter.this.ui.sess.username + "/");
         auth.addbytes(Screenshooter.this.ui.sess.sesskey);
         conn.addRequestProperty("Authorization", "Haven " + Utils.base64enc(auth.blob));
         conn.connect();
         OutputStream out = conn.getOutputStream();

         try {
            int off = 0;

            while (off < data.length) {
               this.setstate(String.format("Uploading (%d%%)...", off * 100 / data.length));
               int len = Math.min(1024, data.length - off);
               out.write(data, off, len);
               off += len;
            }
         } finally {
            out.close();
         }

         this.setstate("Awaiting response...");
         InputStream var29 = conn.getInputStream();

         final URL result;
         try {
            if (!conn.getContentType().equals("text/x-target-url")) {
               throw new IOException("Unexpected type of reply from server");
            }

            byte[] b = Utils.readall(var29);

            try {
               result = new URL(new String(b, "utf-8"));
            } catch (MalformedURLException var25) {
               throw (IOException)new IOException("Unexpected reply from server").initCause(var25);
            }
         } finally {
            var29.close();
         }

         this.setstate("Done");
         synchronized (Screenshooter.this.ui) {
            Screenshooter.this.ui.destroy(Screenshooter.this.btn);
            Screenshooter.this.btn = new Button(Screenshooter.this.btnc, 125, Screenshooter.this, "Open in browser") {
               @Override
               public void click() {
                  if (WebBrowser.self != null) {
                     WebBrowser.self.show(result);
                  }
               }
            };
         }
      }
   }
}
