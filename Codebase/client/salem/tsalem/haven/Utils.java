package haven;

import haven.test.ScriptDebug;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
    if (img == null)
      return new Coord(0, 0); 
    return new Coord(img.getWidth(), img.getHeight());
  }
  
  public static void defer(final Runnable r) {
    Defer.later(new Defer.Callable() {
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
        if (rgbm.getAlpha(p) > 128)
          if ((p & 0xFFFFFF) == 16711808) {
            t.setRGB(x + c.x, y + c.y, 0);
          } else {
            t.setRGB(x + c.x, y + c.y, p);
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
    g.drawString(text, (int)(c.x - ts.getWidth() * ax), (int)((c.y + m.getAscent()) - ts.getHeight() * ay));
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
    int i;
    for (i = 0; i < div - dec; i++)
      num /= 10; 
    for (i = 0; i < dec; i++) {
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
    if (s)
      buf.append('-'); 
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
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    try {
      if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        String text = (String)t.getTransferData(DataFlavor.stringFlavor);
        return text;
      } 
    } catch (UnsupportedFlavorException unsupportedFlavorException) {
    
    } catch (IOException iOException) {}
    return "";
  }
  
  static synchronized Preferences prefs() {
    if (prefs == null) {
      Preferences node = Preferences.userNodeForPackage(Utils.class);
      if (Config.prefspec != null)
        node = node.node(Config.prefspec); 
      prefs = node;
    } 
    return prefs;
  }
  
  public static String getpref(String prefname, String def) {
    try {
      return prefs().get(prefname, def);
    } catch (SecurityException e) {
      return def;
    } 
  }
  
  public static void setpref(String prefname, String val) {
    try {
      prefs().put(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  public static boolean getprefb(String prefname, boolean def) {
    try {
      return prefs().getBoolean(prefname, def);
    } catch (SecurityException e) {
      return def;
    } 
  }
  
  public static void setprefb(String prefname, boolean val) {
    try {
      prefs().putBoolean(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  static void setpreff(String prefname, float val) {
    try {
      prefs().putFloat(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  static Coord getprefc(String prefname, Coord def) {
    try {
      String val = prefs().get(prefname, null);
      if (val == null)
        return def; 
      int x = val.indexOf('x');
      if (x < 0)
        return def; 
      return new Coord(Integer.parseInt(val.substring(0, x)), Integer.parseInt(val.substring(x + 1)));
    } catch (SecurityException e) {
      return def;
    } 
  }
  
  static void setprefc(String prefname, Coord val) {
    try {
      prefs().put(prefname, val.x + "x" + val.y);
    } catch (SecurityException securityException) {}
  }
  
  static byte[] getprefb(String prefname, byte[] def) {
    try {
      return prefs().getByteArray(prefname, def);
    } catch (SecurityException e) {
      return def;
    } 
  }
  
  static void setprefb(String prefname, byte[] val) {
    try {
      prefs().putByteArray(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  static float getpreff(String prefname, float def) {
    try {
      return prefs().getFloat(prefname, def);
    } catch (SecurityException e) {
      return def;
    } 
  }
  
  static int getprefi(String prefname, int def) {
    try {
      return prefs().getInt(prefname, def);
    } catch (SecurityException arg2) {
      return def;
    } 
  }
  
  static void setprefi(String prefname, int val) {
    try {
      prefs().putInt(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  public static long getprefl(String prefname, long def) {
    try {
      return prefs().getLong(prefname, def);
    } catch (SecurityException arg2) {
      return def;
    } 
  }
  
  public static void setprefl(String prefname, long val) {
    try {
      prefs().putLong(prefname, val);
    } catch (SecurityException securityException) {}
  }
  
  public static String getprop(String propname, String def) {
    try {
      String ret;
      if ((ret = System.getProperty(propname)) != null)
        return ret; 
      if ((ret = System.getProperty("jnlp." + propname)) != null)
        return ret; 
      return def;
    } catch (SecurityException e) {
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
    return ub(buf[off]) | ub(buf[off + 1]) << 8L | ub(buf[off + 2]) << 16L | ub(buf[off + 3]) << 24L;
  }
  
  public static void uint32e(long num, byte[] buf, int off) {
    buf[off] = (byte)(int)(num & 0xFFL);
    buf[off + 1] = (byte)(int)((num & 0xFF00L) >> 8L);
    buf[off + 2] = (byte)(int)((num & 0xFF0000L) >> 16L);
    buf[off + 3] = (byte)(int)((num & 0xFFFFFFFFFF000000L) >> 24L);
  }
  
  public static int int32d(byte[] buf, int off) {
    return (int)uint32d(buf, off);
  }
  
  public static long int64d(byte[] buf, int off) {
    long b = 0L;
    for (int i = 0; i < 8; i++)
      b |= ub(buf[i]) << i * 8; 
    return b;
  }
  
  public static void int32e(int num, byte[] buf, int off) {
    uint32e(num & 0xFFFFFFFFFFFFFFFFL, buf, off);
  }
  
  public static void uint16e(int num, byte[] buf, int off) {
    buf[off] = sb(num & 0xFF);
    buf[off + 1] = sb((num & 0xFF00) >> 8);
  }
  
  public static String strd(byte[] buf, int[] off) {
    String ret;
    int i;
    for (i = off[0]; buf[i] != 0; i++);
    try {
      ret = new String(buf, off[0], i - off[0], "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    } 
    off[0] = i + 1;
    return ret;
  }
  
  public static double floatd(byte[] buf, int off) {
    int e = buf[off];
    long t = uint32d(buf, off + 1);
    int m = (int)(t & 0x7FFFFFFFL);
    boolean s = ((t & 0x80000000L) != 0L);
    if (e == -128) {
      if (m == 0)
        return 0.0D; 
      throw new RuntimeException("Invalid special float encoded (" + m + ")");
    } 
    double v = m / 2.147483648E9D + 1.0D;
    if (s)
      v = -v; 
    return Math.pow(2.0D, e) * v;
  }
  
  public static float float32d(byte[] buf, int off) {
    return Float.intBitsToFloat(int32d(buf, off));
  }
  
  public static double float64d(byte[] buf, int off) {
    return Double.longBitsToDouble(int64d(buf, off));
  }
  
  public static float hfdec(short bits) {
    int ee, b = bits & 0xFFFF;
    int e = (b & 0x7C00) >> 10;
    int m = b & 0x3FF;
    if (e == 0) {
      if (m == 0) {
        ee = 0;
      } else {
        int n = Integer.numberOfLeadingZeros(m) - 22;
        ee = -15 - n + 127;
        m = m << n + 1 & 0x3FF;
      } 
    } else if (e == 31) {
      ee = 255;
    } else {
      ee = e - 15 + 127;
    } 
    int f32 = (b & 0x8000) << 16 | ee << 23 | m << 13;
    return Float.intBitsToFloat(f32);
  }
  
  public static short hfenc(float f) {
    int ee, b = Float.floatToIntBits(f);
    int e = (b & 0x7F800000) >> 23;
    int m = b & 0x7FFFFF;
    if (e == 0) {
      ee = 0;
      m = 0;
    } else if (e == 255) {
      ee = 31;
    } else if (e < 113) {
      ee = 0;
      m = (m | 0x800000) >> 113 - e;
    } else {
      if (e > 142)
        return ((b & Integer.MIN_VALUE) == 0) ? 31744 : -1024; 
      ee = e - 127 + 15;
    } 
    int f16 = b >> 16 & 0x8000 | ee << 10 | m >> 13;
    return (short)f16;
  }
  
  static char num2hex(int num) {
    if (num < 10)
      return (char)(48 + num); 
    return (char)(65 + num - 10);
  }
  
  static int hex2num(char hex) {
    if (hex >= '0' && hex <= '9')
      return hex - 48; 
    if (hex >= 'a' && hex <= 'f')
      return hex - 97 + 10; 
    if (hex >= 'A' && hex <= 'F')
      return hex - 65 + 10; 
    throw new IllegalArgumentException();
  }
  
  static String byte2hex(byte[] in) {
    StringBuilder buf = new StringBuilder();
    for (byte b : in) {
      buf.append(num2hex((b & 0xF0) >> 4));
      buf.append(num2hex(b & 0xF));
    } 
    return buf.toString();
  }
  
  static byte[] hex2byte(String hex) {
    if (hex.length() % 2 != 0)
      throw new IllegalArgumentException("Invalid hex-encoded string"); 
    byte[] ret = new byte[hex.length() / 2];
    for (int i = 0, o = 0; i < hex.length(); i += 2, o++)
      ret[o] = (byte)(hex2num(hex.charAt(i)) << 4 | hex2num(hex.charAt(i + 1))); 
    return ret;
  }
  
  static {
    int[] rev = new int[128];
    int i;
    for (i = 0; i < 128; rev[i++] = -1);
    for (i = 0; i < "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".length(); i++)
      rev["ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(i)] = i; 
    base64rev = rev;
    Console.setscmd("die", new Console.Command() {
          public void run(Console cons, String[] args) {
            throw new Error("Triggered death");
          }
        });
    Console.setscmd("threads", new Console.Command() {
          public void run(Console cons, String[] args) {
            Utils.dumptg(null, cons.out);
          }
        });
    Console.setscmd("gc", new Console.Command() {
          public void run(Console cons, String[] args) {
            System.gc();
          }
        });
    Console.setscmd("cscript", new Console.Command() {
          public void run(Console cons, String[] args) throws IOException {
            ScriptDebug.connect(args[1], Config.defserv, Integer.parseInt(args[2]));
          }
        });
  }
  
  public static String base64enc(byte[] in) {
    StringBuilder buf = new StringBuilder();
    int p = 0;
    while (in.length - p >= 3) {
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0xFC) >> 2));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0x3) << 4 | (in[p + 1] & 0xF0) >> 4));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 1] & 0xF) << 2 | (in[p + 2] & 0xC0) >> 6));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(in[p + 2] & 0x3F));
      p += 3;
    } 
    if (in.length == p + 1) {
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0xFC) >> 2));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0x3) << 4));
      buf.append("==");
    } else if (in.length == p + 2) {
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0xFC) >> 2));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 0] & 0x3) << 4 | (in[p + 1] & 0xF0) >> 4));
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt((in[p + 1] & 0xF) << 2));
      buf.append("=");
    } 
    return buf.toString();
  }
  
  public static byte[] base64dec(String in) {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    int cur = 0, b = 8;
    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      if (c >= '')
        throw new IllegalArgumentException(); 
      if (c == '=')
        break; 
      int d = base64rev[c];
      if (d == -1)
        throw new IllegalArgumentException(); 
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
          continue;
        } 
        i++;
        continue;
      } 
      if (st == "word") {
        if (c == '"') {
          st = "quote";
          i++;
          continue;
        } 
        if (c == '\\') {
          st = "squote";
          i++;
          continue;
        } 
        if (Character.isWhitespace(c)) {
          words.add(buf.toString());
          buf = new StringBuilder();
          st = "ws";
          continue;
        } 
        buf.append(c);
        i++;
        continue;
      } 
      if (st == "quote") {
        if (c == '"') {
          st = "word";
          i++;
          continue;
        } 
        if (c == '\\') {
          st = "sqquote";
          i++;
          continue;
        } 
        buf.append(c);
        i++;
        continue;
      } 
      if (st == "squote") {
        buf.append(c);
        i++;
        st = "word";
        continue;
      } 
      if (st == "sqquote") {
        buf.append(c);
        i++;
        st = "quote";
      } 
    } 
    if (st == "word")
      words.add(buf.toString()); 
    if (st != "ws" && st != "word")
      return null; 
    return words.<String>toArray(new String[0]);
  }
  
  public static String[] splitlines(String text) {
    ArrayList<String> ret = new ArrayList<>();
    int p = 0;
    while (true) {
      int p2 = text.indexOf('\n', p);
      if (p2 < 0) {
        ret.add(text.substring(p));
        break;
      } 
      ret.add(text.substring(p, p2));
      p = p2 + 1;
    } 
    return ret.<String>toArray(new String[0]);
  }
  
  static int atoi(String a) {
    try {
      return Integer.parseInt(a);
    } catch (NumberFormatException e) {
      return 0;
    } 
  }
  
  static void readtileof(InputStream in) throws IOException {
    byte[] buf = new byte[4096];
    do {
    
    } while (in.read(buf, 0, buf.length) >= 0);
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
    for (int o = 0; o < indent; o++)
      out.print("    "); 
    out.println("G: \"" + tg.getName() + "\"");
    Thread[] ths = new Thread[tg.activeCount() * 2];
    ThreadGroup[] tgs = new ThreadGroup[tg.activeGroupCount() * 2];
    int nt = tg.enumerate(ths, false);
    int ng = tg.enumerate(tgs, false);
    int i;
    for (i = 0; i < nt; i++) {
      Thread ct = ths[i];
      for (int j = 0; j < indent + 1; j++)
        out.print("    "); 
      out.println("T: \"" + ct.getName() + "\"");
    } 
    for (i = 0; i < ng; i++) {
      ThreadGroup cg = tgs[i];
      dumptg(cg, out, indent + 1);
    } 
  }
  
  public static void dumptg(ThreadGroup tg, PrintWriter out) {
    if (tg == null) {
      tg = Thread.currentThread().getThreadGroup();
      while (tg.getParent() != null)
        tg = tg.getParent(); 
    } 
    dumptg(tg, out, 0);
    out.flush();
  }
  
  public static Resource myres(Class<?> c) {
    ClassLoader cl = c.getClassLoader();
    if (cl instanceof Resource.ResClassLoader)
      return ((Resource.ResClassLoader)cl).getres(); 
    return null;
  }
  
  public static String titlecase(String str) {
    return Character.toTitleCase(str.charAt(0)) + str.substring(1);
  }
  
  public static Color contrast(Color col) {
    int max = Math.max(col.getRed(), Math.max(col.getGreen(), col.getBlue()));
    if (max > 128)
      return new Color(col.getRed() / 2, col.getGreen() / 2, col.getBlue() / 2, col.getAlpha()); 
    if (max == 0)
      return Color.WHITE; 
    int f = 128 / max;
    return new Color(col.getRed() * f, col.getGreen() * f, col.getBlue() * f, col.getAlpha());
  }
  
  public static Color clipcol(int r, int g, int b, int a) {
    if (r < 0)
      r = 0; 
    if (r > 255)
      r = 255; 
    if (g < 0)
      g = 0; 
    if (g > 255)
      g = 255; 
    if (b < 0)
      b = 0; 
    if (b > 255)
      b = 255; 
    if (a < 0)
      a = 0; 
    if (a > 255)
      a = 255; 
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
        if (y == 0 || x == 0 || y == sz.y - 1 || x == sz.x - 1) {
          t = true;
        } else {
          t = (src.getSample(x - 1, y - 1, 3) < 250);
        } 
        if (t) {
          if ((x > 1 && y > 0 && y < sz.y - 1 && src.getSample(x - 2, y - 1, 3) >= 250) || (x > 0 && y > 1 && x < sz.x - 1 && src.getSample(x - 1, y - 2, 3) >= 250) || (x < sz.x - 2 && y > 0 && y < sz.y - 1 && src.getSample(x, y - 1, 3) >= 250) || (x > 0 && y < sz.y - 2 && x < sz.x - 1 && src.getSample(x - 1, y, 3) >= 250))
            dst.setDataElements(x, y, fcol); 
          if (thick && ((x > 1 && y > 1 && src.getSample(x - 2, y - 2, 3) >= 250) || (x < sz.x - 2 && y < sz.y - 2 && src.getSample(x, y, 3) >= 250) || (x < sz.x - 2 && y > 1 && src.getSample(x, y - 2, 3) >= 250) || (x > 1 && y < sz.y - 2 && src.getSample(x - 2, y, 3) >= 250)))
            dst.setDataElements(x, y, fcol); 
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
    if (a < 0)
      return (a + 1) / b - 1; 
    return a / b;
  }
  
  public static int floormod(int a, int b) {
    int r = a % b;
    if (r < 0)
      r += b; 
    return r;
  }
  
  public static int floordiv(float a, float b) {
    float q = a / b;
    return (q < 0.0F) ? ((int)q - 1) : (int)q;
  }
  
  public static float floormod(float a, float b) {
    float r = a % b;
    return (a < 0.0F) ? (r + b) : r;
  }
  
  public static double cangle(double a) {
    while (a > Math.PI)
      a -= 6.283185307179586D; 
    while (a < -3.141592653589793D)
      a += 6.283185307179586D; 
    return a;
  }
  
  public static double cangle2(double a) {
    while (a > 6.283185307179586D)
      a -= 6.283185307179586D; 
    while (a < 0.0D)
      a += 6.283185307179586D; 
    return a;
  }
  
  public static double clip(double d, double min, double max) {
    if (d < min)
      return min; 
    if (d > max)
      return max; 
    return d;
  }
  
  public static float clip(float d, float min, float max) {
    if (d < min)
      return min; 
    if (d > max)
      return max; 
    return d;
  }
  
  public static int clip(int i, int min, int max) {
    if (i < min)
      return min; 
    if (i > max)
      return max; 
    return i;
  }
  
  public static Color blendcol(Color in, Color bl) {
    int f1 = bl.getAlpha();
    int f2 = 255 - bl.getAlpha();
    return new Color((in.getRed() * f2 + bl.getRed() * f1) / 255, (in.getGreen() * f2 + bl.getGreen() * f1) / 255, (in.getBlue() * f2 + bl.getBlue() * f1) / 255, in.getAlpha());
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return out.toByteArray();
  }
  
  public static Object deserialize(InputStream in) throws IOException {
    ObjectInputStream oin = new ObjectInputStream(in);
    try {
      return oin.readObject();
    } catch (ClassNotFoundException e) {
      return null;
    } 
  }
  
  public static Object deserialize(byte[] buf) {
    if (buf == null)
      return null; 
    InputStream in = new ByteArrayInputStream(buf);
    try {
      return deserialize(in);
    } catch (IOException e) {
      return null;
    } 
  }
  
  public static boolean parsebool(String s) {
    if (s == null)
      throw new IllegalArgumentException(s); 
    if (s.equalsIgnoreCase("1") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"))
      return true; 
    if (s.equalsIgnoreCase("0") || s.equalsIgnoreCase("off") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no"))
      return false; 
    throw new IllegalArgumentException(s);
  }
  
  public static boolean eq(Object a, Object b) {
    return ((a == null && b == null) || (a != null && b != null && a.equals(b)));
  }
  
  public static boolean parsebool(String s, boolean def) {
    try {
      return parsebool(s);
    } catch (IllegalArgumentException e) {
      return def;
    } 
  }
  
  public static FloatBuffer bufcp(float[] a) {
    FloatBuffer b = mkfbuf(a.length);
    b.put(a);
    b.rewind();
    return b;
  }
  
  public static ShortBuffer bufcp(short[] a) {
    ShortBuffer b = mksbuf(a.length);
    b.put(a);
    b.rewind();
    return b;
  }
  
  public static FloatBuffer bufcp(FloatBuffer a) {
    a.rewind();
    FloatBuffer ret = mkfbuf(a.remaining());
    ret.put(a).rewind();
    return ret;
  }
  
  public static IntBuffer bufcp(IntBuffer a) {
    a.rewind();
    IntBuffer ret = mkibuf(a.remaining());
    ret.put(a).rewind();
    return ret;
  }
  
  public static ByteBuffer mkbbuf(int n) {
    try {
      return ByteBuffer.allocateDirect(n).order(ByteOrder.nativeOrder());
    } catch (OutOfMemoryError e) {
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
    return new float[] { c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F };
  }
  
  public static <T> T[] mkarray(Class<T> cl, int len) {
    return (T[])Array.newInstance(cl, len);
  }
  
  public static <T> T[] splice(T[] src, int off, int len) {
    T[] dst = (T[])Array.newInstance(src.getClass().getComponentType(), len);
    System.arraycopy(src, off, dst, 0, len);
    return dst;
  }
  
  public static void rgb2hsl(int r, int g, int b, int[] hsl) {
    float var_Min, var_Max, S, var_R = r / 255.0F;
    float var_G = g / 255.0F;
    float var_B = b / 255.0F;
    if (var_R > var_G) {
      var_Min = var_G;
      var_Max = var_R;
    } else {
      var_Min = var_R;
      var_Max = var_G;
    } 
    if (var_B > var_Max)
      var_Max = var_B; 
    if (var_B < var_Min)
      var_Min = var_B; 
    float del_Max = var_Max - var_Min;
    float H = 0.0F;
    float L = (var_Max + var_Min) / 2.0F;
    if (del_Max == 0.0F) {
      H = 0.0F;
      S = 0.0F;
    } else {
      if (L < 0.5D) {
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
      if (H < 0.0F)
        H++; 
      if (H > 1.0F)
        H--; 
    } 
    hsl[0] = (int)(360.0F * H);
    hsl[1] = (int)(S * 100.0F);
    hsl[2] = (int)(L * 100.0F);
  }
  
  public static int[] hsl2rgb(int[] hsl) {
    double h = hsl[0] / 360.0D;
    double s = hsl[1] / 100.0D;
    double l = hsl[2] / 100.0D;
    double r = 0.0D;
    double g = 0.0D;
    if (s > 0.0D) {
      if (h >= 1.0D)
        h = 0.0D; 
      h *= 6.0D;
      double f = h - Math.floor(h);
      double a = Math.round(l * 255.0D * (1.0D - s));
      double b = Math.round(l * 255.0D * (1.0D - s * f));
      double c = Math.round(l * 255.0D * (1.0D - s * (1.0D - f)));
      l = Math.round(l * 255.0D);
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
          break;
      } 
      return new int[] { (int)Math.round(r), (int)Math.round(g), (int)Math.round(b) };
    } 
    l = Math.round(l * 255.0D);
    return new int[] { (int)l, (int)l, (int)l };
  }
  
  public static <T> T[] splice(T[] src, int off) {
    return splice(src, off, src.length - off);
  }
  
  public static <T> T[] extend(T[] src, int off, int nl) {
    T[] dst = (T[])Array.newInstance(src.getClass().getComponentType(), nl);
    System.arraycopy(src, off, dst, 0, Math.min(src.length - off, dst.length));
    return dst;
  }
  
  public static <T> T[] extend(T[] src, int nl) {
    return extend(src, 0, nl);
  }
  
  public static <T> T el(Iterable<T> c) {
    return c.iterator().next();
  }
  
  public static <T> T construct(Constructor<T> cons, Object... args) {
    try {
      return cons.newInstance(args);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException)
        throw (RuntimeException)e.getCause(); 
      throw new RuntimeException(e.getCause());
    } 
  }
  
  public static String urlencode(String in) {
    byte[] enc;
    StringBuilder buf = new StringBuilder();
    try {
      enc = in.getBytes("utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    } 
    for (byte c : enc) {
      if ((c >= 97 && c <= 122) || (c >= 65 && c <= 90) || (c >= 48 && c <= 57) || c == 46) {
        buf.append((char)c);
      } else {
        buf.append("%" + num2hex((c & 0xF0) >> 4) + num2hex(c & 0xF));
      } 
    } 
    return buf.toString();
  }
  
  public static URL urlparam(URL base, String... pars) {
    String file = base.getFile();
    int p = file.indexOf('?');
    StringBuilder buf = new StringBuilder();
    if (p >= 0) {
      buf.append('&');
    } else {
      buf.append('?');
    } 
    for (int i = 0; i < pars.length; i += 2) {
      if (i > 0)
        buf.append('&'); 
      buf.append(urlencode(pars[i]));
      buf.append('=');
      buf.append(urlencode(pars[i + 1]));
    } 
    try {
      return new URL(base.getProtocol(), base.getHost(), base.getPort(), file + buf.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static String timestamp() {
    return (new SimpleDateFormat("HH:mm")).format(new Date());
  }
  
  public static String timestamp(String text) {
    return String.format("[%s] %s", new Object[] { timestamp(), text });
  }
  
  public static String stream2str(InputStream is) {
    Scanner s = (new Scanner(is)).useDelimiter("\\A");
    String r = s.hasNext() ? s.next() : "";
    s.close();
    try {
      is.close();
    } catch (IOException e) {
      msgLog("Exception in stream2str");
    } 
    return r;
  }
  
  public static Color hex2color(String hex, Color def) {
    Color c = def;
    if (hex != null)
      try {
        int col = (int)Long.parseLong(hex, 16);
        boolean hasAlpha = ((0xFF000000 & col) != 0);
        c = new Color(col, hasAlpha);
      } catch (Exception exception) {} 
    return c;
  }
  
  public static String color2hex(Color col) {
    if (col != null)
      return Integer.toHexString(col.getRGB()); 
    return null;
  }
  
  public static void msgOut(String msg, Color color) {
    UI.instance.message(msg, color);
  }
  
  public static void msgLog(String msg, Color color) {
    if (UI.instance.gui != null)
      UI.instance.gui.messageToLog(msg, color); 
  }
  
  public static void msgLog(String msg) {
    if (UI.instance.gui != null)
      UI.instance.gui.messageToLog(msg, Color.CYAN); 
  }
  
  public static void msgOut(String msg, GameUI.MsgType msgType) {
    UI.instance.message(msg, msgType);
  }
  
  public static void msgOut(String msg) {
    UI.instance.message(msg, GameUI.MsgType.INFO);
  }
  
  public static void debugMsgOut(String msg) {
    if (RemoteUI.debug)
      UI.instance.message(msg, GameUI.MsgType.INFO); 
  }
  
  public static void errOut(Exception e) {
    msgOut(e.getMessage());
    for (StackTraceElement i : e.getStackTrace())
      msgOut("" + i.getLineNumber() + ": " + i.toString()); 
  }
  
  public static void sleep(int i) {
    try {
      Thread.sleep(i);
    } catch (InterruptedException interruptedException) {}
  }
  
  public static Collection<Gob> getGobsWithinX(double maxDist) {
    Collection<Gob> gobs = UI.instance.sess.glob.oc.getGobs();
    Collection<Gob> returnGobs = new ArrayList<>();
    Coord player_location = (UI.instance.gui.map.player()).rc;
    for (Gob gob : gobs) {
      if (gob.rc.dist(player_location) <= maxDist)
        returnGobs.add(gob); 
    } 
    return returnGobs;
  }
  
  public static Collection<Gob> getAllGobs() {
    Collection<Gob> gobs = UI.instance.sess.glob.oc.getGobs();
    Collection<Gob> returnGobs = new ArrayList<>();
    returnGobs.addAll(gobs);
    return returnGobs;
  }
  
  public static String getGobName(Gob gob) {
    if (gob == null)
      return ""; 
    try {
      ResDrawable rd = gob.<ResDrawable>getattr(ResDrawable.class);
      Composite c = gob.<Composite>getattr(Composite.class);
      if (rd != null && rd.res != null)
        return ((Resource)rd.res.get()).name; 
      if (c != null && c.base != null)
        return ((Resource)c.base.get()).name; 
    } catch (Exception exception) {}
    return "";
  }
  
  public static double dist(Gob gob) {
    return getCoord().dist(getCoord(gob));
  }
  
  public static double dist(Gob gob, Coord mouseCoord) {
    if (mouseCoord != null)
      return mouseCoord.dist(getCoord(gob)); 
    return dist(gob);
  }
  
  public static double distx(Gob gob) {
    return getCoord().distx(getCoord(gob));
  }
  
  public static double disty(Gob gob) {
    return getCoord().disty(getCoord(gob));
  }
  
  public static double dist(Coord c) {
    return getCoord().dist(c);
  }
  
  public static double distx(Coord c) {
    return getCoord().distx(c);
  }
  
  public static double disty(Coord c) {
    return getCoord().disty(c);
  }
  
  public static Tiler getTile(Coord c) {
    return mcache().tiler(mcache().gettile(c.div(11)));
  }
  
  public static Coord getCoord() {
    return getCoord(getPlayer());
  }
  
  public static Coord getCoord(Gob gob) {
    if (gob != null)
      return gob.rc; 
    return null;
  }
  
  public static Gob getPlayer() {
    return UI.instance.sess.glob.oc.getgob(UI.instance.gui.map.plgob);
  }
  
  private static MCache mcache() {
    return UI.instance.sess.glob.map;
  }
  
  public static void gobClick(Gob gob, int mouseBtn) {
    gobClick(gob, mouseBtn, -1);
  }
  
  public static void gobClick(Gob gob, int mouseBtn, int gobPart) {
    gobClick(gob, mouseBtn, gobPart, 0);
  }
  
  public static void gobClick(Gob gob, int mouseBtn, int gobPart, int modFlags) {
    if (gob != null)
      mv().wdgmsg("click", new Object[] { getScreenCenter(), getCoord(gob), Integer.valueOf(mouseBtn), Integer.valueOf(modFlags), Integer.valueOf(0), Integer.valueOf((int)gob.id), getCoord(gob), Integer.valueOf(0), Integer.valueOf(gobPart) }); 
  }
  
  public static void gobItemActClick(Gob gob) {
    mv().customItemAct(getScreenCenter(), getCoord(gob), 1, (int)gob.id, getCoord(gob), 0);
  }
  
  protected static MapView mv() {
    return UI.instance.gui.map;
  }
  
  private static Coord getScreenCenter() {
    return new Coord((int)Math.round(Math.random() * 200.0D + (UI.instance.gui.sz.x / 2) - 100.0D), (int)Math.round(Math.random() * 200.0D + (UI.instance.gui.sz.y / 2) - 100.0D));
  }
  
  public static String[] getPoseList() {
    return getPoseList(getPlayer());
  }
  
  public static String[] getPoseList(Gob gob) {
    Composite cmp = gob.<Composite>getattr(Composite.class);
    ArrayList<String> result = new ArrayList<>();
    if (cmp == null || cmp.rusty_lastposes == null)
      return null; 
    for (ResData rd : cmp.rusty_lastposes) {
      try {
        if (rd.res != null && ((Resource)rd.res.get()).name != null)
          result.add(((Resource)rd.res.get()).name); 
      } catch (Exception exception) {}
    } 
    return result.<String>toArray(new String[result.size()]);
  }
  
  public static boolean isCarrying() {
    return isCarrying(getPlayer());
  }
  
  public static boolean isCarrying(Gob gob) {
    return hasPose("banzai", gob);
  }
  
  public static boolean hasPose(String pose, Gob gob) {
    String[] poses = getPoseList(gob);
    if (poses == null || poses.length == 0)
      return false; 
    for (String p : poses) {
      if (p.toUpperCase().contains(pose.toUpperCase()))
        return true; 
    } 
    return false;
  }
  
  public static Gob getCarryObject() {
    Collection<Gob> gobs = getGobsWithinX(11.0D);
    for (Gob gob : gobs) {
      if (gob.getattr(Following.class) != null)
        return gob; 
    } 
    return null;
  }
  
  public static boolean isCustomHotkey(KeyEvent ev) {
    if (Config.h_char_list.contains(Integer.valueOf(ev.getKeyCode()))) {
      boolean ctrl = ev.isControlDown();
      boolean shift = ev.isShiftDown();
      boolean alt = ev.isAltDown();
      for (HotkeyList.HotkeyJItem hkji : Config.HOTKEYLIST) {
        try {
          if (hkji.alt != alt)
            continue; 
          if (hkji.shift != shift)
            continue; 
          if (hkji.ctrl != ctrl)
            continue; 
          if (hkji.key.length() == 1 && ev.getKeyCode() == hkji.key.charAt(0))
            return true; 
        } catch (Exception ex) {
          System.out.println("Error in hotkey handling");
        } 
      } 
    } 
    return false;
  }
  
  private static Map<Integer, Object> tMap = new HashMap<>();
  
  private static Map<Integer, Long> idTime = new HashMap<>();
  
  private static int counter = 0;
  
  public static Object getFromCache(Object... o) {
    Object rO = null;
    long cur = System.currentTimeMillis();
    int h = 0;
    for (Object object : o)
      h += object.hashCode(); 
    if (tMap.containsKey(Integer.valueOf(h))) {
      rO = tMap.get(Integer.valueOf(h));
      idTime.put(Integer.valueOf(h), Long.valueOf(cur + 10000L));
    } 
    if (counter >= 1000) {
      Iterator<Map.Entry<Integer, Long>> it = idTime.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Long> en = it.next();
        if (((Long)en.getValue()).longValue() <= cur)
          it.remove(); 
      } 
      counter = 0;
    } else {
      counter++;
    } 
    return rO;
  }
  
  public static void putIntoCache(Object o, Object... os) {
    int h = 0;
    for (Object object : os)
      h += object.hashCode(); 
    tMap.put(Integer.valueOf(h), o);
    idTime.put(Integer.valueOf(h), Long.valueOf(System.currentTimeMillis() + 10000L));
  }
  
  private static Map<String, Long> tStamps = new HashMap<>();
  
  private static Map<String, Long> tSums = new HashMap<>();
  
  public static long cur() {
    return System.currentTimeMillis();
  }
  
  public static void timeStampStart(String s) {
    tStamps.put(s, Long.valueOf(cur()));
  }
  
  public static void timeStampEnd(String s) {
    try {
      Long l1 = tStamps.get(s);
      Long l2 = Long.valueOf(cur() - l1.longValue());
      if (tSums.containsKey(s)) {
        Long l3 = tSums.get(s);
        l2 = Long.valueOf(l2.longValue() + l3.longValue());
      } 
      tSums.put(s, l2);
      tStamps.remove(s);
    } catch (Exception e) {
      msgLog("error in timestamps on String: " + s);
    } 
  }
  
  private static long outputTime = cur();
  
  public static void timeStampOutputAll(int i) {
    if (outputTime + i > cur())
      return; 
    outputTime = cur();
    String output = "";
    int count = 0;
    for (Map.Entry<String, Long> en : tSums.entrySet()) {
      count++;
      if (count > 1)
        output = output + ", "; 
      output = output + (String)en.getKey() + ": " + en.getValue();
      if (count > 10) {
        msgLog(output);
        output = "";
        count = 0;
      } 
    } 
    if (count > 0)
      msgLog(output); 
    tSums.clear();
  }
  
  private static Map<String, Integer> countMap = new HashMap<>();
  
  public static void countMe(String s) {
    int count1 = 1;
    for (Map.Entry<String, Integer> en : countMap.entrySet()) {
      if (s.equals(en.getKey()))
        count1 += ((Integer)en.getValue()).intValue(); 
    } 
    countMap.put(s, Integer.valueOf(count1));
  }
  
  public static void countOutputAll(int i) {
    if (outputTime + i > cur())
      return; 
    outputTime = cur();
    String output = "";
    int count = 0;
    for (Map.Entry<String, Integer> en : countMap.entrySet()) {
      count++;
      if (count > 1)
        output = output + ", "; 
      output = output + (String)en.getKey() + ": " + en.getValue();
      if (count > 10) {
        msgLog(output);
        output = "";
        count = 0;
      } 
    } 
    if (count > 0)
      msgLog(output); 
    countMap.clear();
  }
  
  public static Glob.Pagina getPagina(Set<Glob.Pagina> paginae, String filter, String name) {
    try {
      for (Glob.Pagina p : paginae) {
        if ((filter == null || filter.equals("") || (p.res()).name.toLowerCase().contains(filter.toLowerCase())) && 
          (p.res()).name.toLowerCase().contains(name))
          return p; 
      } 
    } catch (Exception exception) {}
    return null;
  }
  
  public static Glob.Pagina getPagina(String filter, String name) {
    Set<Glob.Pagina> paginae = UI.instance.sess.glob.paginae;
    return getPagina(paginae, filter, name);
  }
  
  public static Glob.Pagina getPagina(String name) {
    return getPagina("", name);
  }
  
  public static boolean isBackPack(Window win) {
    String name = win.cap.text.toLowerCase().trim();
    if (name.endsWith("pack"))
      return true; 
    if (name.endsWith("big autumn"))
      return true; 
    return false;
  }
  
  public static Inet4Address in4_pton(CharSequence as) {
    int dbuf = -1, o = 0;
    byte[] abuf = new byte[4];
    for (int i = 0; i < as.length(); i++) {
      char c = as.charAt(i);
      if (c >= '0' && c <= '9') {
        dbuf = ((dbuf < 0) ? 0 : dbuf) * 10 + c - 48;
        if (dbuf >= 256)
          throw new AddressFormatException("illegal octet", as, "in4"); 
      } else if (c == '.') {
        if (dbuf < 0)
          throw new AddressFormatException("dot without preceding octet", as, "in4"); 
        if (o >= 3)
          throw new AddressFormatException("too many address octets", as, "in4"); 
        abuf[o++] = (byte)dbuf;
        dbuf = -1;
      } else {
        throw new AddressFormatException("illegal address character", as, "in4");
      } 
    } 
    if (dbuf < 0)
      throw new AddressFormatException("end without preceding octet", as, "in4"); 
    if (o != 3)
      throw new AddressFormatException("too few address octets", as, "in4"); 
    abuf[o++] = (byte)dbuf;
    try {
      return (Inet4Address)InetAddress.getByAddress(abuf);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static InetAddress in6_pton(CharSequence as) {
    byte[] fbuf;
    int hbuf = -1, dbuf = -1, p = 0, v4map = -1;
    int[] o = { 0, 0 };
    byte[][] abuf = { new byte[16], new byte[16] };
    String scope = null;
    for (int i = 0; i < as.length(); i++) {
      char c = as.charAt(i);
      int dv = -1;
      if (c >= '0' && c <= '9') {
        dv = c - 48;
      } else if (c >= 'A' && c <= 'F') {
        dv = c + 10 - 65;
      } else if (c >= 'a' && c <= 'f') {
        dv = c + 10 - 97;
      } 
      if (dv >= 0) {
        if (hbuf < 0)
          hbuf = dbuf = 0; 
        hbuf = hbuf * 16 + dv;
        if (hbuf >= 65536)
          throw new AddressFormatException("illegal address number", as, "in6"); 
        if (dbuf >= 0)
          dbuf = (dv >= 10) ? -1 : (dbuf * 10 + dv); 
        if (dbuf >= 256)
          dbuf = -1; 
      } else if (c == ':') {
        if (v4map >= 0)
          throw new AddressFormatException("illegal embedded v4 address", as, "in6"); 
        if (hbuf < 0) {
          if (p == 0) {
            if (o[p] == 0) {
              if (i < as.length() - 1 && as.charAt(i + 1) == ':') {
                p = 1;
                i++;
              } else {
                throw new AddressFormatException("colon without preceeding address number", as, "in6");
              } 
            } else {
              p = 1;
            } 
          } else {
            throw new AddressFormatException("duplicate zero-string", as, "in6");
          } 
        } else {
          if (o[p] >= 14)
            throw new AddressFormatException("too many address numbers", as, "in6"); 
          o[p] = o[p] + 1;
          abuf[p][o[p]] = (byte)((hbuf & 0xFF00) >> 8);
          o[p] = o[p] + 1;
          abuf[p][o[p]] = (byte)(hbuf & 0xFF);
          hbuf = -1;
        } 
      } else if (c == '.') {
        if (hbuf < 0 || dbuf < 0)
          throw new AddressFormatException("illegal embedded v4 octet", as, "in6"); 
        if (p == 0 && o[p] == 0)
          throw new AddressFormatException("embedded v4 at start of address", as, "in6"); 
        if (v4map++ >= 2)
          throw new AddressFormatException("too many embedded v4 octets", as, "in6"); 
        if (o[p] >= 15)
          throw new AddressFormatException("too many address numbers", as, "in6"); 
        o[p] = o[p] + 1;
        abuf[p][o[p]] = (byte)dbuf;
        hbuf = -1;
      } else {
        if (c == '%') {
          scope = as.subSequence(i + 1, as.length()).toString();
          break;
        } 
        throw new AddressFormatException("illegal address character", as, "in6");
      } 
    } 
    if (hbuf < 0) {
      if (p < 1 || o[p] > 0)
        throw new AddressFormatException("unterminated address", as, "in6"); 
    } else if (v4map < 0) {
      if (o[p] >= 15)
        throw new AddressFormatException("too many address numbers", as, "in6"); 
      o[p] = o[p] + 1;
      abuf[p][o[p]] = (byte)((hbuf & 0xFF00) >> 8);
      o[p] = o[p] + 1;
      abuf[p][o[p]] = (byte)(hbuf & 0xFF);
    } else {
      if (dbuf < 0)
        throw new AddressFormatException("illegal embedded v4 octet", as, "in6"); 
      if (v4map != 2)
        throw new AddressFormatException("too few embedded v4 octets", as, "in6"); 
      if (o[p] >= 16)
        throw new AddressFormatException("too many address numbers", as, "in6"); 
      o[p] = o[p] + 1;
      abuf[p][o[p]] = (byte)dbuf;
    } 
    if (p == 0) {
      if (o[0] != 16)
        throw new AddressFormatException("too few address numbers", as, "in6"); 
      fbuf = abuf[0];
    } else {
      if (o[0] + o[1] >= 16)
        throw new AddressFormatException("illegal zero-string", as, "in6"); 
      fbuf = new byte[16];
      System.arraycopy(abuf[0], 0, fbuf, 0, o[0]);
      System.arraycopy(abuf[1], 0, fbuf, 16 - o[1], o[1]);
    } 
    try {
      if (scope == null)
        return InetAddress.getByAddress(fbuf); 
      try {
        return Inet6Address.getByAddress((String)null, fbuf, Integer.parseInt(scope));
      } catch (NumberFormatException e) {
        try {
          NetworkInterface iface = NetworkInterface.getByName(scope);
          if (iface == null)
            throw new AddressFormatException("could not resolve scoped interface: " + scope, as, "in6"); 
          return Inet6Address.getByAddress((String)null, fbuf, iface);
        } catch (SocketException e2) {
          throw new AddressFormatException("could not resolve scoped interface: " + scope, as, "in6", e);
        } 
      } 
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static InetAddress inet_pton(CharSequence as) {
    try {
      return in4_pton(as);
    } catch (IllegalArgumentException e) {
      try {
        return in6_pton(as);
      } catch (IllegalArgumentException e2) {
        e2.addSuppressed(e);
        throw e2;
      } 
    } 
  }
  
  public static class AddressFormatException extends IllegalArgumentException {
    public final String addr;
    
    public final String type;
    
    public AddressFormatException(String message, CharSequence addr, String type) {
      super(message);
      this.addr = addr.toString();
      this.type = type;
    }
    
    public AddressFormatException(String message, CharSequence addr, String type, Throwable cause) {
      super(message, cause);
      this.addr = addr.toString();
      this.type = type;
    }
    
    public String getMessage() {
      return super.getMessage() + ": " + this.addr + " (" + this.type + ")";
    }
  }
  
  public static URI uri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(uri, e);
    } 
  }
  
  public static URL url(String url) {
    try {
      return uri(url).toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(url, e);
    } 
  }
  
  public static Path path(String path) {
    if (path == null)
      return null; 
    return FileSystems.getDefault().getPath(path, new String[0]);
  }
  
  public static Path pj(Path base, String... els) {
    for (String el : els)
      base = base.resolve(el); 
    return base;
  }
  
  public static <T> T ioretry(IOFunction<? extends T> task) throws IOException {
    double[] retimes = { 0.01D, 0.1D, 0.5D, 1.0D, 5.0D };
    Throwable last = null;
    boolean intr = false;
    try {
    
    } finally {
      if (intr)
        Thread.currentThread().interrupt(); 
    } 
  }
  
  public static <K, V> Map<K, V> mapdecn(Object ob, Class<K> kt, Class<V> vt) {
    Map<K, V> ret = new HashMap<>();
    Object[] enc = (Object[])ob;
    for (Object sob : enc) {
      Object[] ent = (Object[])sob;
      ret.put(kt.cast(ent[0]), vt.cast(ent[1]));
    } 
    return ret;
  }
  
  public static Map<Object, Object> mapdecn(Object ob) {
    return mapdecn(ob, Object.class, Object.class);
  }
  
  public static Object[] mapencn(Map<?, ?> map) {
    Object[] ret = new Object[map.size()];
    int a = 0;
    for (Map.Entry<?, ?> ent : map.entrySet()) {
      (new Object[2])[0] = ent.getKey();
      (new Object[2])[1] = ent.getValue();
      ret[a++] = new Object[2];
    } 
    return ret;
  }
  
  public static <K, V> Map<K, V> mapdecf(Object ob, Class<K> kt, Class<V> vt) {
    Map<K, V> ret = new HashMap<>();
    Object[] enc = (Object[])ob;
    for (int a = 0; a < enc.length - 1; a += 2)
      ret.put(kt.cast(enc[a]), vt.cast(enc[a + 1])); 
    return ret;
  }
  
  public static Map<Object, Object> mapdecf(Object ob) {
    return mapdecf(ob, Object.class, Object.class);
  }
  
  public static Object[] mapencf(Map<?, ?> map) {
    Object[] ret = new Object[map.size() * 2];
    int a = 0;
    for (Map.Entry<?, ?> ent : map.entrySet()) {
      ret[a + 0] = ent.getKey();
      ret[a + 1] = ent.getValue();
      a += 2;
    } 
    return ret;
  }
  
  private static Boolean serverIsNew = null;
  
  private static boolean isNewServer() {
    Boolean result = Boolean.valueOf(true);
    if (serverIsNew == null) {
      try {
        long refTimeStamp = 1709163763000L;
        long refTimeStamp2 = -9935310759000L;
        long now = System.currentTimeMillis();
        long timeOffset = (now - 1709163763000L) * 3L;
        long serverTime = UI.instance.sess.glob.globtime() + SeasonImg.EPOCH;
        long checkTime = serverTime - timeOffset;
        result = Boolean.valueOf((checkTime < -9935310759000L));
      } catch (Exception e) {
        return true;
      } 
    } else {
      result = serverIsNew;
    } 
    serverIsNew = result;
    return result.booleanValue();
  }
  
  public static int getInvX() {
    if (isNewServer())
      return 20; 
    return 32;
  }
  
  public static int getInvY() {
    if (isNewServer())
      return 10; 
    return 32;
  }
  
  public static int getInvMaxSize() {
    if (isNewServer())
      return 200; 
    return 1024;
  }
  
  public static interface IOFunction<T> {
    T run() throws IOException;
  }
}
