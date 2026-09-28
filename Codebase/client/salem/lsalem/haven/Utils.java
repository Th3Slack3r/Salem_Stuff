package haven;

import haven.test.ScriptDebug;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.prefs.Preferences;

public class Utils {
   private static final SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
   public static final Charset utf8 = Charset.forName("UTF-8");
   public static final Charset ascii = Charset.forName("US-ASCII");
   public static final ColorModel rgbm = ColorModel.getRGBdefault();
   private static Preferences prefs = null;
   private static final String base64set = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   private static final int[] base64rev;

   static Coord imgsz(BufferedImage img) {
      return new Coord(img.getWidth(), img.getHeight());
   }

   public static void defer(final Runnable r) {
      Defer.later(new Defer.Callable<Object>() {
         @Override
         public Object call() {
            r.run();
            return null;
         }
      });
   }

   static void drawgay(BufferedImage t, BufferedImage img, Coord c) {
      Coord sz = imgsz(img);

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            int p = img.getRGB(x, y);
            if (rgbm.getAlpha(p) > 128) {
               if ((p & 16777215) == 16711808) {
                  t.setRGB(x + c.x, y + c.y, 0);
               } else {
                  t.setRGB(x + c.x, y + c.y, p);
               }
            }
         }
      }
   }

   public static int drawtext(Graphics g, String text, Coord c) {
      FontMetrics m = g.getFontMetrics();
      g.drawString(text, c.x, c.y + m.getAscent());
      return m.getHeight();
   }

   static Coord textsz(Graphics g, String text) {
      FontMetrics m = g.getFontMetrics();
      Rectangle2D ts = m.getStringBounds(text, g);
      return new Coord((int)ts.getWidth(), (int)ts.getHeight());
   }

   static void aligntext(Graphics g, String text, Coord c, double ax, double ay) {
      FontMetrics m = g.getFontMetrics();
      Rectangle2D ts = m.getStringBounds(text, g);
      g.drawString(text, (int)(c.x - ts.getWidth() * ax), (int)(c.y + m.getAscent() - ts.getHeight() * ay));
   }

   public static String datef(long time) {
      return datef.format(new Date(time));
   }

   public static String current_date() {
      return datef(System.currentTimeMillis());
   }

   public static String fpformat(int num, int div, int dec) {
      StringBuilder buf = new StringBuilder();
      boolean s = false;
      if (num < 0) {
         num = -num;
         s = true;
      }

      for (int i = 0; i < div - dec; i++) {
         num /= 10;
      }

      for (int i = 0; i < dec; i++) {
         buf.append((char)(48 + num % 10));
         num /= 10;
      }

      buf.append('.');
      if (num == 0) {
         buf.append('0');
      } else {
         while (num > 0) {
            buf.append((char)(48 + num % 10));
            num /= 10;
         }
      }

      if (s) {
         buf.append('-');
      }

      return buf.reverse().toString();
   }

   static void line(Graphics g, Coord c1, Coord c2) {
      g.drawLine(c1.x, c1.y, c2.x, c2.y);
   }

   static void AA(Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   }

   public static String getClipboard() {
      Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();

      try {
         Transferable t = c.getContents(null);
         if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String)t.getTransferData(DataFlavor.stringFlavor);
         }
      } catch (UnsupportedFlavorException var3) {
      } catch (IOException var4) {
      } catch (IllegalStateException var5) {
      }

      return "";
   }

   static synchronized Preferences prefs() {
      if (prefs == null) {
         Preferences node = Preferences.userNodeForPackage(Utils.class);
         if (Config.prefspec != null) {
            node = node.node(Config.prefspec);
         }

         prefs = node;
      }

      return prefs;
   }

   static String getpref(String prefname, String def) {
      try {
         return prefs().get(prefname, def);
      } catch (SecurityException var3) {
         return def;
      }
   }

   static void setpref(String prefname, String val) {
      try {
         prefs().put(prefname, val);
      } catch (SecurityException var3) {
      }
   }

   static boolean getprefb(String prefname, boolean def) {
      try {
         return prefs().getBoolean(prefname, def);
      } catch (SecurityException var3) {
         return def;
      }
   }

   static void setprefb(String prefname, boolean val) {
      try {
         prefs().putBoolean(prefname, val);
      } catch (SecurityException var3) {
      }
   }

   static void setpreff(String prefname, float val) {
      try {
         prefs().putFloat(prefname, val);
      } catch (SecurityException var3) {
      }
   }

   static Coord getprefc(String prefname, Coord def) {
      try {
         String val = prefs().get(prefname, null);
         if (val == null) {
            return def;
         } else {
            int x = val.indexOf(120);
            return x < 0 ? def : new Coord(Integer.parseInt(val.substring(0, x)), Integer.parseInt(val.substring(x + 1)));
         }
      } catch (SecurityException var4) {
         return def;
      }
   }

   static void setprefc(String prefname, Coord val) {
      try {
         prefs().put(prefname, val.x + "x" + val.y);
      } catch (SecurityException var3) {
      }
   }

   static byte[] getprefb(String prefname, byte[] def) {
      try {
         return prefs().getByteArray(prefname, def);
      } catch (SecurityException var3) {
         return def;
      }
   }

   static void setprefb(String prefname, byte[] val) {
      try {
         prefs().putByteArray(prefname, val);
      } catch (SecurityException var3) {
      }
   }

   static float getpreff(String prefname, float def) {
      try {
         return prefs().getFloat(prefname, def);
      } catch (SecurityException var3) {
         return def;
      }
   }

   public static String getprop(String propname, String def) {
      try {
         String ret;
         if ((ret = System.getProperty(propname)) != null) {
            return ret;
         } else {
            return (ret = System.getProperty("jnlp." + propname)) != null ? ret : def;
         }
      } catch (SecurityException var3) {
         return def;
      }
   }

   public static int ub(byte b) {
      return b & 0xFF;
   }

   public static byte sb(int b) {
      return (byte)b;
   }

   public static int uint16d(byte[] buf, int off) {
      return ub(buf[off]) | ub(buf[off + 1]) << 8;
   }

   public static int int16d(byte[] buf, int off) {
      return (short)uint16d(buf, off);
   }

   public static long uint32d(byte[] buf, int off) {
      return ub(buf[off]) | (long)ub(buf[off + 1]) << 8 | (long)ub(buf[off + 2]) << 16 | (long)ub(buf[off + 3]) << 24;
   }

   public static void uint32e(long num, byte[] buf, int off) {
      buf[off] = (byte)(num & 255L);
      buf[off + 1] = (byte)((num & 65280L) >> 8);
      buf[off + 2] = (byte)((num & 16711680L) >> 16);
      buf[off + 3] = (byte)((num & -16777216L) >> 24);
   }

   public static int int32d(byte[] buf, int off) {
      return (int)uint32d(buf, off);
   }

   public static long int64d(byte[] buf, int off) {
      long b = 0L;

      for (int i = 0; i < 8; i++) {
         b |= (long)ub(buf[i]) << i * 8;
      }

      return b;
   }

   public static void int32e(int num, byte[] buf, int off) {
      uint32e(num & -1L, buf, off);
   }

   public static void uint16e(int num, byte[] buf, int off) {
      buf[off] = sb(num & 0xFF);
      buf[off + 1] = sb((num & 0xFF00) >> 8);
   }

   public static String strd(byte[] buf, int[] off) {
      int i = off[0];

      while (buf[i] != 0) {
         i++;
      }

      String ret;
      try {
         ret = new String(buf, off[0], i - off[0], "utf-8");
      } catch (UnsupportedEncodingException var5) {
         throw new IllegalArgumentException(var5);
      }

      off[0] = i + 1;
      return ret;
   }

   public static double floatd(byte[] buf, int off) {
      int e = buf[off];
      long t = uint32d(buf, off + 1);
      int m = (int)(t & 2147483647L);
      boolean s = (t & 2147483648L) != 0L;
      if (e == -128) {
         if (m == 0) {
            return 0.0;
         } else {
            throw new RuntimeException("Invalid special float encoded (" + m + ")");
         }
      } else {
         double v = m / 2.1474836E9F + 1.0;
         if (s) {
            v = -v;
         }

         return Math.pow(2.0, e) * v;
      }
   }

   public static float float32d(byte[] buf, int off) {
      return Float.intBitsToFloat(int32d(buf, off));
   }

   public static double float64d(byte[] buf, int off) {
      return Double.longBitsToDouble(int64d(buf, off));
   }

   public static float hfdec(short bits) {
      int b = bits & '\uffff';
      int e = (b & 31744) >> 10;
      int m = b & 1023;
      int ee;
      if (e == 0) {
         if (m == 0) {
            ee = 0;
         } else {
            int n = Integer.numberOfLeadingZeros(m) - 22;
            ee = -15 - n + 127;
            m = m << n + 1 & 1023;
         }
      } else if (e == 31) {
         ee = 255;
      } else {
         ee = e - 15 + 127;
      }

      int f32 = (b & 32768) << 16 | ee << 23 | m << 13;
      return Float.intBitsToFloat(f32);
   }

   public static short hfenc(float f) {
      int b = Float.floatToIntBits(f);
      int e = (b & 2139095040) >> 23;
      int m = b & 8388607;
      int ee;
      if (e == 0) {
         ee = 0;
         m = 0;
      } else if (e == 255) {
         ee = 31;
      } else if (e < 113) {
         ee = 0;
         m = (m | 8388608) >> 113 - e;
      } else {
         if (e > 142) {
            return (short)((b & -2147483648) == 0 ? 31744 : -1024);
         }

         ee = e - 127 + 15;
      }

      int f16 = b >> 16 & 32768 | ee << 10 | m >> 13;
      return (short)f16;
   }

   static char num2hex(int num) {
      return num < 10 ? (char)(48 + num) : (char)(65 + num - 10);
   }

   static int hex2num(char hex) {
      if (hex >= '0' && hex <= '9') {
         return hex - 48;
      } else if (hex >= 'a' && hex <= 'f') {
         return hex - 97 + 10;
      } else if (hex >= 'A' && hex <= 'F') {
         return hex - 65 + 10;
      } else {
         throw new IllegalArgumentException();
      }
   }

   static String byte2hex(byte[] in) {
      StringBuilder buf = new StringBuilder();

      for (byte b : in) {
         buf.append(num2hex((b & 240) >> 4));
         buf.append(num2hex(b & 15));
      }

      return buf.toString();
   }

   static byte[] hex2byte(String hex) {
      if (hex.length() % 2 != 0) {
         throw new IllegalArgumentException("Invalid hex-encoded string");
      } else {
         byte[] ret = new byte[hex.length() / 2];
         int i = 0;

         for (int o = 0; i < hex.length(); o++) {
            ret[o] = (byte)(hex2num(hex.charAt(i)) << 4 | hex2num(hex.charAt(i + 1)));
            i += 2;
         }

         return ret;
      }
   }

   public static String base64enc(byte[] in) {
      StringBuilder buf = new StringBuilder();

      int p;
      for (p = 0; in.length - p >= 3; p += 3) {
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 252) >> 2));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 3) << 4 | (in[p + 1] & 240) >> 4));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 1] & 15) << 2 | (in[p + 2] & 192) >> 6));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(in[p + 2] & 63));
      }

      if (in.length == p + 1) {
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 252) >> 2));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 3) << 4));
         buf.append("==");
      } else if (in.length == p + 2) {
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 252) >> 2));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 3) << 4 | (in[p + 1] & 240) >> 4));
         buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 1] & 15) << 2));
         buf.append("=");
      }

      return buf.toString();
   }

   public static byte[] base64dec(String in) {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      int cur = 0;
      int b = 8;

      for (int i = 0; i < in.length(); i++) {
         char c = in.charAt(i);
         if (c >= 128) {
            throw new IllegalArgumentException();
         }

         if (c == '=') {
            break;
         }

         int d = base64rev[c];
         if (d == -1) {
            throw new IllegalArgumentException();
         }

         b -= 6;
         if (b <= 0) {
            cur |= d >> -b;
            buf.write(cur);
            b += 8;
            cur = 0;
         }

         cur |= d << b;
      }

      return buf.toByteArray();
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

   public static String[] splitlines(String text) {
      ArrayList<String> ret = new ArrayList<>();
      int p = 0;

      while (true) {
         int p2 = text.indexOf(10, p);
         if (p2 < 0) {
            ret.add(text.substring(p));
            return ret.toArray(new String[0]);
         }

         ret.add(text.substring(p, p2));
         p = p2 + 1;
      }
   }

   static int atoi(String a) {
      try {
         return Integer.parseInt(a);
      } catch (NumberFormatException var2) {
         return 0;
      }
   }

   static void readtileof(InputStream in) throws IOException {
      byte[] buf = new byte[4096];

      while (in.read(buf, 0, buf.length) >= 0) {
      }
   }

   static byte[] readall(InputStream in) throws IOException {
      byte[] buf = new byte[4096];
      int off = 0;

      while (true) {
         if (off == buf.length) {
            byte[] n = new byte[buf.length * 2];
            System.arraycopy(buf, 0, n, 0, buf.length);
            buf = n;
         }

         int ret = in.read(buf, off, buf.length - off);
         if (ret < 0) {
            byte[] n = new byte[off];
            System.arraycopy(buf, 0, n, 0, off);
            return n;
         }

         off += ret;
      }
   }

   private static void dumptg(ThreadGroup tg, PrintWriter out, int indent) {
      for (int o = 0; o < indent; o++) {
         out.print("    ");
      }

      out.println("G: \"" + tg.getName() + "\"");
      Thread[] ths = new Thread[tg.activeCount() * 2];
      ThreadGroup[] tgs = new ThreadGroup[tg.activeGroupCount() * 2];
      int nt = tg.enumerate(ths, false);
      int ng = tg.enumerate(tgs, false);

      for (int i = 0; i < nt; i++) {
         Thread ct = ths[i];

         for (int o = 0; o < indent + 1; o++) {
            out.print("    ");
         }

         out.println("T: \"" + ct.getName() + "\"");
      }

      for (int i = 0; i < ng; i++) {
         ThreadGroup cg = tgs[i];
         dumptg(cg, out, indent + 1);
      }
   }

   public static void dumptg(ThreadGroup tg, PrintWriter out) {
      if (tg == null) {
         tg = Thread.currentThread().getThreadGroup();

         while (tg.getParent() != null) {
            tg = tg.getParent();
         }
      }

      dumptg(tg, out, 0);
      out.flush();
   }

   public static Resource myres(Class<?> c) {
      ClassLoader cl = c.getClassLoader();
      return cl instanceof Resource.ResClassLoader ? ((Resource.ResClassLoader)cl).getres() : null;
   }

   public static String titlecase(String str) {
      return Character.toTitleCase(str.charAt(0)) + str.substring(1);
   }

   public static Color contrast(Color col) {
      int max = Math.max(col.getRed(), Math.max(col.getGreen(), col.getBlue()));
      if (max > 128) {
         return new Color(col.getRed() / 2, col.getGreen() / 2, col.getBlue() / 2, col.getAlpha());
      } else if (max == 0) {
         return Color.WHITE;
      } else {
         int f = 128 / max;
         return new Color(col.getRed() * f, col.getGreen() * f, col.getBlue() * f, col.getAlpha());
      }
   }

   public static Color clipcol(int r, int g, int b, int a) {
      if (r < 0) {
         r = 0;
      }

      if (r > 255) {
         r = 255;
      }

      if (g < 0) {
         g = 0;
      }

      if (g > 255) {
         g = 255;
      }

      if (b < 0) {
         b = 0;
      }

      if (b > 255) {
         b = 255;
      }

      if (a < 0) {
         a = 0;
      }

      if (a > 255) {
         a = 255;
      }

      return new Color(r, g, b, a);
   }

   public static BufferedImage outline(BufferedImage img, Color col) {
      return outline(img, col, false);
   }

   public static BufferedImage outline(BufferedImage img, Color col, boolean thick) {
      Coord sz = imgsz(img).add(2, 2);
      BufferedImage ol = TexI.mkbuf(sz);
      Object fcol = ol.getColorModel().getDataElements(col.getRGB(), null);
      Raster src = img.getRaster();
      WritableRaster dst = ol.getRaster();

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            boolean t;
            if (y != 0 && x != 0 && y != sz.y - 1 && x != sz.x - 1) {
               t = src.getSample(x - 1, y - 1, 3) < 250;
            } else {
               t = true;
            }

            if (t) {
               if (x > 1 && y > 0 && y < sz.y - 1 && src.getSample(x - 2, y - 1, 3) >= 250
                  || x > 0 && y > 1 && x < sz.x - 1 && src.getSample(x - 1, y - 2, 3) >= 250
                  || x < sz.x - 2 && y > 0 && y < sz.y - 1 && src.getSample(x, y - 1, 3) >= 250
                  || x > 0 && y < sz.y - 2 && x < sz.x - 1 && src.getSample(x - 1, y, 3) >= 250) {
                  dst.setDataElements(x, y, fcol);
               }

               if (thick
                  && (
                     x > 1 && y > 1 && src.getSample(x - 2, y - 2, 3) >= 250
                        || x < sz.x - 2 && y < sz.y - 2 && src.getSample(x, y, 3) >= 250
                        || x < sz.x - 2 && y > 1 && src.getSample(x, y - 2, 3) >= 250
                        || x > 1 && y < sz.y - 2 && src.getSample(x - 2, y, 3) >= 250
                  )) {
                  dst.setDataElements(x, y, fcol);
               }
            }
         }
      }

      return ol;
   }

   public static BufferedImage outline2(BufferedImage img, Color col) {
      return outline2(img, col, false);
   }

   public static BufferedImage outline2(BufferedImage img, Color col, boolean thick) {
      BufferedImage ol = outline(img, col, thick);
      Graphics g = ol.getGraphics();
      g.drawImage(img, 1, 1, null);
      g.dispose();
      return ol;
   }

   public static int floordiv(int a, int b) {
      return a < 0 ? (a + 1) / b - 1 : a / b;
   }

   public static int floormod(int a, int b) {
      int r = a % b;
      if (r < 0) {
         r += b;
      }

      return r;
   }

   public static int floordiv(float a, float b) {
      float q = a / b;
      return q < 0.0F ? (int)q - 1 : (int)q;
   }

   public static float floormod(float a, float b) {
      float r = a % b;
      return a < 0.0F ? r + b : r;
   }

   public static double cangle(double a) {
      while (a > Math.PI) {
         a -= Math.PI * 2;
      }

      while (a < -Math.PI) {
         a += Math.PI * 2;
      }

      return a;
   }

   public static double cangle2(double a) {
      while (a > Math.PI * 2) {
         a -= Math.PI * 2;
      }

      while (a < 0.0) {
         a += Math.PI * 2;
      }

      return a;
   }

   public static double clip(double d, double min, double max) {
      if (d < min) {
         return min;
      } else {
         return d > max ? max : d;
      }
   }

   public static float clip(float d, float min, float max) {
      if (d < min) {
         return min;
      } else {
         return d > max ? max : d;
      }
   }

   public static int clip(int i, int min, int max) {
      if (i < min) {
         return min;
      } else {
         return i > max ? max : i;
      }
   }

   public static Color blendcol(Color in, Color bl) {
      int f1 = bl.getAlpha();
      int f2 = 255 - bl.getAlpha();
      return new Color(
         (in.getRed() * f2 + bl.getRed() * f1) / 255,
         (in.getGreen() * f2 + bl.getGreen() * f1) / 255,
         (in.getBlue() * f2 + bl.getBlue() * f1) / 255,
         in.getAlpha()
      );
   }

   public static void serialize(Object obj, OutputStream out) throws IOException {
      ObjectOutputStream oout = new ObjectOutputStream(out);
      oout.writeObject(obj);
      oout.flush();
   }

   public static byte[] serialize(Object obj) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      try {
         serialize(obj, out);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }

      return out.toByteArray();
   }

   public static Object deserialize(InputStream in) throws IOException {
      ObjectInputStream oin = new ObjectInputStream(in);

      try {
         return oin.readObject();
      } catch (ClassNotFoundException var3) {
         return null;
      }
   }

   public static Object deserialize(byte[] buf) {
      if (buf == null) {
         return null;
      } else {
         InputStream in = new ByteArrayInputStream(buf);

         try {
            return deserialize(in);
         } catch (IOException var3) {
            return null;
         }
      }
   }

   public static boolean parsebool(String s) {
      if (s == null) {
         throw new IllegalArgumentException(s);
      } else if (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")) {
         return true;
      } else if (!s.equalsIgnoreCase("0") && !s.equalsIgnoreCase("off") && !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("no")) {
         throw new IllegalArgumentException(s);
      } else {
         return false;
      }
   }

   public static boolean eq(Object a, Object b) {
      return a == null && b == null || a != null && b != null && a.equals(b);
   }

   public static boolean parsebool(String s, boolean def) {
      try {
         return parsebool(s);
      } catch (IllegalArgumentException var3) {
         return def;
      }
   }

   public static FloatBuffer bufcp(float[] a) {
      FloatBuffer b = mkfbuf(a.length);
      b.put(a);
      ((Buffer)b).rewind();
      return b;
   }

   public static ShortBuffer bufcp(short[] a) {
      ShortBuffer b = mksbuf(a.length);
      b.put(a);
      ((Buffer)b).rewind();
      return b;
   }

   public static FloatBuffer bufcp(FloatBuffer a) {
      ((Buffer)a).rewind();
      FloatBuffer ret = mkfbuf(a.remaining());
      ((Buffer)ret.put(a)).rewind();
      return ret;
   }

   public static IntBuffer bufcp(IntBuffer a) {
      ((Buffer)a).rewind();
      IntBuffer ret = mkibuf(a.remaining());
      ((Buffer)ret.put(a)).rewind();
      return ret;
   }

   public static ByteBuffer mkbbuf(int n) {
      try {
         return ByteBuffer.allocateDirect(n).order(ByteOrder.nativeOrder());
      } catch (OutOfMemoryError var2) {
         System.gc();
         return ByteBuffer.allocateDirect(n).order(ByteOrder.nativeOrder());
      }
   }

   public static FloatBuffer mkfbuf(int n) {
      return mkbbuf(n * 4).asFloatBuffer();
   }

   public static ShortBuffer mksbuf(int n) {
      return mkbbuf(n * 2).asShortBuffer();
   }

   public static IntBuffer mkibuf(int n) {
      return mkbbuf(n * 4).asIntBuffer();
   }

   public static ByteBuffer wbbuf(int n) {
      return ByteBuffer.wrap(new byte[n]);
   }

   public static IntBuffer wibuf(int n) {
      return IntBuffer.wrap(new int[n]);
   }

   public static FloatBuffer wfbuf(int n) {
      return FloatBuffer.wrap(new float[n]);
   }

   public static ShortBuffer wsbuf(int n) {
      return ShortBuffer.wrap(new short[n]);
   }

   public static float[] c2fa(Color c) {
      return new float[]{c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F};
   }

   public static <T> T[] mkarray(Class<T> cl, int len) {
      return (T[])((Object[])Array.newInstance(cl, len));
   }

   public static <T> T[] splice(T[] src, int off, int len) {
      T[] dst = (T[])Array.newInstance(src.getClass().getComponentType(), len);
      System.arraycopy(src, off, dst, 0, len);
      return dst;
   }

   public static void rgb2hsl(int r, int g, int b, int[] hsl) {
      float var_R = r / 255.0F;
      float var_G = g / 255.0F;
      float var_B = b / 255.0F;
      float var_Min;
      float var_Max;
      if (var_R > var_G) {
         var_Min = var_G;
         var_Max = var_R;
      } else {
         var_Min = var_R;
         var_Max = var_G;
      }

      if (var_B > var_Max) {
         var_Max = var_B;
      }

      if (var_B < var_Min) {
         var_Min = var_B;
      }

      float del_Max = var_Max - var_Min;
      float H = 0.0F;
      float L = (var_Max + var_Min) / 2.0F;
      float S;
      if (del_Max == 0.0F) {
         H = 0.0F;
         S = 0.0F;
      } else {
         if (L < 0.5) {
            S = del_Max / (var_Max + var_Min);
         } else {
            S = del_Max / (2.0F - var_Max - var_Min);
         }

         float del_R = ((var_Max - var_R) / 6.0F + del_Max / 2.0F) / del_Max;
         float del_G = ((var_Max - var_G) / 6.0F + del_Max / 2.0F) / del_Max;
         float del_B = ((var_Max - var_B) / 6.0F + del_Max / 2.0F) / del_Max;
         if (var_R == var_Max) {
            H = del_B - del_G;
         } else if (var_G == var_Max) {
            H = 0.33333334F + del_R - del_B;
         } else if (var_B == var_Max) {
            H = 0.6666667F + del_G - del_R;
         }

         if (H < 0.0F) {
            H++;
         }

         if (H > 1.0F) {
            H--;
         }
      }

      hsl[0] = (int)(360.0F * H);
      hsl[1] = (int)(S * 100.0F);
      hsl[2] = (int)(L * 100.0F);
   }

   public static int[] hsl2rgb(int[] hsl) {
      double h = hsl[0] / 360.0;
      double s = hsl[1] / 100.0;
      double l = hsl[2] / 100.0;
      double r = 0.0;
      double g = 0.0;
      if (s > 0.0) {
         if (h >= 1.0) {
            h = 0.0;
         }

         h *= 6.0;
         double f = h - Math.floor(h);
         double a = Math.round(l * 255.0 * (1.0 - s));
         double b = Math.round(l * 255.0 * (1.0 - s * f));
         double c = Math.round(l * 255.0 * (1.0 - s * (1.0 - f)));
         l = Math.round(l * 255.0);
         switch ((int)Math.floor(h)) {
            case 0:
               r = l;
               g = c;
               b = a;
               break;
            case 1:
               r = b;
               g = l;
               b = a;
               break;
            case 2:
               r = a;
               g = l;
               b = c;
               break;
            case 3:
               r = a;
               g = b;
               b = l;
               break;
            case 4:
               r = c;
               g = a;
               b = l;
               break;
            case 5:
               r = l;
               g = a;
         }

         return new int[]{(int)Math.round(r), (int)Math.round(g), (int)Math.round(b)};
      } else {
         l = Math.round(l * 255.0);
         return new int[]{(int)l, (int)l, (int)l};
      }
   }

   public static <T> T[] splice(T[] src, int off) {
      return (T[])splice(src, off, src.length - off);
   }

   public static <T> T[] extend(T[] src, int off, int nl) {
      T[] dst = (T[])Array.newInstance(src.getClass().getComponentType(), nl);
      System.arraycopy(src, off, dst, 0, Math.min(src.length - off, dst.length));
      return dst;
   }

   public static <T> T[] extend(T[] src, int nl) {
      return (T[])extend(src, 0, nl);
   }

   public static <T> T el(Iterable<T> c) {
      return c.iterator().next();
   }

   public static <T> T construct(Constructor<T> cons, Object... args) {
      try {
         return cons.newInstance(args);
      } catch (InstantiationException var3) {
         throw new RuntimeException(var3);
      } catch (IllegalAccessException var4) {
         throw new RuntimeException(var4);
      } catch (InvocationTargetException var5) {
         if (var5.getCause() instanceof RuntimeException) {
            throw (RuntimeException)var5.getCause();
         } else {
            throw new RuntimeException(var5.getCause());
         }
      }
   }

   public static String urlencode(String in) {
      StringBuilder buf = new StringBuilder();

      byte[] enc;
      try {
         enc = in.getBytes("utf-8");
      } catch (UnsupportedEncodingException var7) {
         throw new Error(var7);
      }

      for (byte c : enc) {
         if ((c < 97 || c > 122) && (c < 65 || c > 90) && (c < 48 || c > 57) && c != 46) {
            buf.append("%" + num2hex((c & 240) >> 4) + num2hex(c & 15));
         } else {
            buf.append((char)c);
         }
      }

      return buf.toString();
   }

   public static URL urlparam(URL base, String... pars) {
      String file = base.getFile();
      int p = file.indexOf(63);
      StringBuilder buf = new StringBuilder();
      if (p >= 0) {
         buf.append('&');
      } else {
         buf.append('?');
      }

      for (int i = 0; i < pars.length; i += 2) {
         if (i > 0) {
            buf.append('&');
         }

         buf.append(urlencode(pars[i]));
         buf.append('=');
         buf.append(urlencode(pars[i + 1]));
      }

      try {
         return new URL(base.getProtocol(), base.getHost(), base.getPort(), file + buf.toString());
      } catch (MalformedURLException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static String timestamp() {
      return new SimpleDateFormat("HH:mm").format(new Date());
   }

   public static String timestamp(String text) {
      return String.format("[%s] %s", timestamp(), text);
   }

   public static String stream2str(InputStream is) {
      Scanner s = new Scanner(is).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
   }

   public static Color hex2color(String hex, Color def) {
      Color c = def;
      if (hex != null) {
         try {
            int col = (int)Long.parseLong(hex, 16);
            boolean hasAlpha = (0xFF000000 & col) != 0;
            c = new Color(col, hasAlpha);
         } catch (Exception var5) {
         }
      }

      return c;
   }

   public static String color2hex(Color col) {
      return col != null ? Integer.toHexString(col.getRGB()) : null;
   }

   static {
      int[] rev = new int[128];
      int i = 0;

      while (i < 128) {
         rev[i++] = -1;
      }

      i = 0;

      while (i < "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".length()) {
         rev["ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(i)] = i++;
      }

      base64rev = rev;
      Console.setscmd("die", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            throw new Error("Triggered death");
         }
      });
      Console.setscmd("threads", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Utils.dumptg(null, cons.out);
         }
      });
      Console.setscmd("gc", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            System.gc();
         }
      });
      Console.setscmd("cscript", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) throws IOException {
            ScriptDebug.connect(args[1], Config.defserv, Integer.parseInt(args[2]));
         }
      });
   }
}
