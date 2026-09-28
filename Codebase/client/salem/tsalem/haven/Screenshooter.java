package haven;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
  public static final ComponentColorModel outcm = new ComponentColorModel(ColorSpace.getInstance(1000), new int[] { 8, 8, 8 }, false, false, 1, 0);
  
  public final URL tgt;
  
  public final Shot shot;
  
  private final TextEntry comment;
  
  private final CheckBox decobox;
  
  private final CheckBox pub;
  
  private final int w;
  
  private final int h;
  
  private Label prog;
  
  private Label progSave;
  
  private final Coord btnc;
  
  private final Coord btns;
  
  private Button btn;
  
  private Button btnSave;
  
  public Screenshooter(Coord c, Widget parent, URL tgt, Shot shot) {
    super(c, Coord.z, parent, "Screenshot");
    this.tgt = tgt;
    this.shot = shot;
    this.w = Math.min(200 * shot.sz.x / shot.sz.y, 150);
    this.h = this.w * shot.sz.y / shot.sz.x;
    this.decobox = new CheckBox(new Coord(this.w, (this.h - (CheckBox.box.sz()).y) / 2), this, "Include interface");
    this.decobox.a = Config.ss_ui;
    Label clbl = new Label(new Coord(0, this.h + 5), this, "If you wish, leave a comment:");
    this.comment = new TextEntry(new Coord(0, clbl.c.y + clbl.sz.y + 5), this.w + 130, this, "") {
        public void activate(String text) {
          Screenshooter.this.upload();
        }
      };
    this.pub = new CheckBox(new Coord(0, this.comment.c.y + this.comment.sz.y + 5), this, "Make public");
    this.pub.a = false;
    this.btnc = new Coord((this.comment.sz.x - 125) / 2, this.pub.c.y + this.pub.sz.y + 20);
    this.btns = this.btnc.add(130, 0);
    this.btn = new Button(this.btnc, Integer.valueOf(125), this, "Upload") {
        public void click() {
          Screenshooter.this.upload();
        }
      };
    this.btnSave = new Button(this.btns, Integer.valueOf(125), this, "Save") {
        public void click() {
          Screenshooter.this.save();
        }
      };
    pack();
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this && msg == "close") {
      this.ui.destroy(this);
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void cdraw(GOut g) {
    TexI tex = this.decobox.a ? this.shot.ui : this.shot.map;
    g.image(tex, Coord.z, new Coord(this.w, this.h));
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
  
  public static final ImageFormat png = new ImageFormat() {
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
      
      public void write(OutputStream out, BufferedImage img, Screenshooter.Shot info) throws IOException {
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
        ImageWriter wr = ImageIO.getImageWriters(type, "PNG").next();
        IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
        Node root = dat.getAsTree("javax_imageio_1.0");
        Node tlist = new IIOMetadataNode("Text");
        if (info.comment != null)
          cmt(tlist, "Comment", info.comment); 
        cmt(tlist, "haven.fsaa", info.fsaa ? "y" : "n");
        cmt(tlist, "haven.flight", info.fl ? "y" : "n");
        cmt(tlist, "haven.sdw", info.sdw ? "y" : "n");
        cmt(tlist, "haven.conf", "");
        root.appendChild(tlist);
        dat.setFromTree("javax_imageio_1.0", root);
        ImageOutputStream iout = ImageIO.createImageOutputStream(out);
        wr.setOutput(iout);
        wr.write(new IIOImage(img, null, dat));
      }
    };
  
  public static final ImageFormat jpeg = new ImageFormat() {
      public String ctype() {
        return "image/jpeg";
      }
      
      public void write(OutputStream out, BufferedImage img, Screenshooter.Shot info) throws IOException {
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
        ImageWriter wr = ImageIO.getImageWriters(type, "JPEG").next();
        IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
        Node root = dat.getAsTree("javax_imageio_jpeg_image_1.0");
        Node mseq;
        for (mseq = root.getFirstChild(); mseq != null && !mseq.getLocalName().equals("markerSequence"); mseq = mseq.getNextSibling());
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
        hdat.addstring("");
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
    
    public void run() {
      try {
        upload(this.img, this.info, this.fmt);
      } catch (InterruptedIOException e) {
        setstate("Cancelled");
        synchronized (Screenshooter.this.ui) {
          Screenshooter.this.ui.destroy(Screenshooter.this.btn);
          Screenshooter.this.btn = new Button(Screenshooter.this.btnc, Integer.valueOf(125), Screenshooter.this, "Retry") {
              public void click() {
                Screenshooter.this.upload();
              }
            };
        } 
      } catch (IOException e) {
        setstate("Could not upload image");
        synchronized (Screenshooter.this.ui) {
          Screenshooter.this.ui.destroy(Screenshooter.this.btn);
          Screenshooter.this.btn = new Button(Screenshooter.this.btnc, Integer.valueOf(125), Screenshooter.this, "Retry") {
              public void click() {
                Screenshooter.this.upload();
              }
            };
        } 
      } 
    }
    
    private void setstate(String t) {
      synchronized (Screenshooter.this.ui) {
        if (Screenshooter.this.prog != null)
          Screenshooter.this.ui.destroy(Screenshooter.this.prog); 
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
      final URL result;
      setstate("Preparing image...");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      fmt.write(buf, convert(ss.back), info);
      byte[] data = buf.toByteArray();
      buf = null;
      setstate("Connecting...");
      URL pared = Utils.urlparam(Screenshooter.this.tgt, new String[] { "p", (Screenshooter.access$300(this.this$0)).a ? "y" : "n" });
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
          setstate(String.format("Uploading (%d%%)...", new Object[] { Integer.valueOf(off * 100 / data.length) }));
          int len = Math.min(1024, data.length - off);
          out.write(data, off, len);
          off += len;
        } 
      } finally {
        out.close();
      } 
      setstate("Awaiting response...");
      InputStream in = conn.getInputStream();
      try {
        if (!conn.getContentType().equals("text/x-target-url"))
          throw new IOException("Unexpected type of reply from server"); 
        byte[] b = Utils.readall(in);
        try {
          result = new URL(new String(b, "utf-8"));
        } catch (MalformedURLException e) {
          throw (IOException)(new IOException("Unexpected reply from server")).initCause(e);
        } 
      } finally {
        in.close();
      } 
      setstate("Done");
      synchronized (Screenshooter.this.ui) {
        Screenshooter.this.ui.destroy(Screenshooter.this.btn);
        Screenshooter.this.btn = new Button(Screenshooter.this.btnc, Integer.valueOf(125), Screenshooter.this, "Open in browser") {
            public void click() {
              if (WebBrowser.self != null)
                WebBrowser.self.show(result); 
            }
          };
      } 
    }
  }
  
  public void upload() {
    this.shot.comment = this.comment.text;
    final Uploader th = new Uploader(this.decobox.a ? this.shot.ui : this.shot.map, this.shot, Config.ss_compress ? jpeg : png);
    th.start();
    this.ui.destroy(this.btn);
    this.btn = new Button(this.btnc, Integer.valueOf(125), this, "Cancel") {
        public void click() {
          th.interrupt();
        }
      };
  }
  
  public class Saver extends HackThread {
    private final TexI img;
    
    private final Screenshooter.Shot info;
    
    private final Screenshooter.ImageFormat fmt;
    
    public Saver(TexI img, Screenshooter.Shot info, Screenshooter.ImageFormat fmt) {
      super("Screenshot saver");
      this.img = img;
      this.info = info;
      this.fmt = fmt;
    }
    
    public void run() {
      try {
        save(this.img, this.info, this.fmt);
      } catch (IOException e) {
        setstate("Could not save image");
        synchronized (Screenshooter.this.ui) {
          Screenshooter.this.ui.destroy(Screenshooter.this.btn);
          Screenshooter.this.btn = new Button(Screenshooter.this.btns, Integer.valueOf(125), Screenshooter.this, "Retry") {
              public void click() {
                Screenshooter.this.save();
              }
            };
        } 
      } 
    }
    
    private void setstate(String t) {
      synchronized (Screenshooter.this.ui) {
        if (Screenshooter.this.progSave != null)
          Screenshooter.this.ui.destroy(Screenshooter.this.progSave); 
        Screenshooter.this.progSave = new Label(Screenshooter.this.btns.sub(0, 15), Screenshooter.this, t);
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
    
    public void save(TexI ss, Screenshooter.Shot info, Screenshooter.ImageFormat fmt) throws IOException {
      setstate("Preparing image...");
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      fmt.write(buf, convert(ss.back), info);
      byte[] data = buf.toByteArray();
      buf = null;
      File ssfolder = Config.getFile("screenshots");
      if (!ssfolder.exists())
        ssfolder.mkdirs(); 
      String fname = String.format("shot_%s_%d.%s", new Object[] { Utils.current_date(), Long.valueOf(System.currentTimeMillis()), Config.ss_compress ? "jpeg" : "png" });
      File f = new File(ssfolder, fname);
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(data);
      fos.close();
      setstate("Done");
      final URL result = f.toURI().toURL();
      synchronized (Screenshooter.this.ui) {
        Screenshooter.this.ui.destroy(Screenshooter.this.btnSave);
        Screenshooter.this.btnSave = new Button(Screenshooter.this.btns, Integer.valueOf(125), Screenshooter.this, "Open") {
            public void click() {
              if (WebBrowser.self != null)
                WebBrowser.self.show(result); 
            }
          };
        Screenshooter.this.ui.message("Screenshot saved", GameUI.MsgType.INFO);
      } 
    }
  }
  
  protected void save() {
    Saver th = new Saver(this.decobox.a ? this.shot.ui : this.shot.map, this.shot, Config.ss_compress ? jpeg : png);
    th.start();
    this.ui.destroy(this.btnSave);
  }
  
  public static void take(final GameUI gameui, final URL tgt) {
    if (gameui == null)
      return; 
    new Object() {
        TexI[] ss;
        
        private void checkcomplete(GOut g) {
          if (this.ss[0] != null && this.ss[1] != null) {
            Screenshooter.Shot shot = new Screenshooter.Shot(this.ss[0], this.ss[1]);
            shot.fl = g.gc.pref.flight.val.booleanValue();
            shot.sdw = g.gc.pref.lshadow.val.booleanValue();
            shot.fsaa = g.gc.pref.fsaa.val.booleanValue();
            Screenshooter s = new Screenshooter(new Coord(100, 100), gameui, tgt, shot);
            if (Config.ss_silent) {
              s.visible = false;
              s.save();
              s.wdgmsg(s, "close", (Object[])null);
            } 
          } 
        }
      };
  }
  
  public static interface ImageFormat {
    String ctype();
    
    void write(OutputStream param1OutputStream, BufferedImage param1BufferedImage, Screenshooter.Shot param1Shot) throws IOException;
  }
}
