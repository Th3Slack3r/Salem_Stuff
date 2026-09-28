package haven;

import java.awt.Color;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Message implements Serializable {
  public static final int RMSG_NEWWDG = 0;
  
  public static final int RMSG_WDGMSG = 1;
  
  public static final int RMSG_DSTWDG = 2;
  
  public static final int RMSG_MAPIV = 3;
  
  public static final int RMSG_GLOBLOB = 4;
  
  public static final int RMSG_PAGINAE = 5;
  
  public static final int RMSG_RESID = 6;
  
  public static final int RMSG_PARTY = 7;
  
  public static final int RMSG_SFX = 8;
  
  public static final int RMSG_CATTR = 9;
  
  public static final int RMSG_MUSIC = 10;
  
  public static final int RMSG_TILES = 11;
  
  public static final int RMSG_BUFF = 12;
  
  public static final int RMSG_SESSKEY = 13;
  
  public static final int T_END = 0;
  
  public static final int T_INT = 1;
  
  public static final int T_STR = 2;
  
  public static final int T_COORD = 3;
  
  public static final int T_UINT8 = 4;
  
  public static final int T_UINT16 = 5;
  
  public static final int T_COLOR = 6;
  
  public static final int T_TTOL = 8;
  
  public static final int T_INT8 = 9;
  
  public static final int T_INT16 = 10;
  
  public static final int T_NIL = 12;
  
  public static final int T_BYTES = 14;
  
  public static final int T_FLOAT32 = 15;
  
  public static final int T_FLOAT64 = 16;
  
  public static final Message nil = new Message(0);
  
  public int type;
  
  public byte[] blob;
  
  public long last = 0L;
  
  public int retx = 0;
  
  public int seq;
  
  public int off = 0;
  
  public Message(int type, byte[] blob) {
    this.type = type;
    this.blob = blob;
  }
  
  public Message(int type, byte[] blob, int offset, int len) {
    this.type = type;
    this.blob = new byte[len];
    System.arraycopy(blob, offset, this.blob, 0, len);
  }
  
  public Message(int type) {
    this.type = type;
    this.blob = new byte[0];
  }
  
  public boolean equals(Object o2) {
    if (!(o2 instanceof Message))
      return false; 
    Message m2 = (Message)o2;
    if (m2.blob.length != this.blob.length)
      return false; 
    for (int i = 0; i < this.blob.length; i++) {
      if (m2.blob[i] != this.blob[i])
        return false; 
    } 
    return true;
  }
  
  public Message clone() {
    return new Message(this.type, this.blob);
  }
  
  public Message derive(int type, int len) {
    int ooff = this.off;
    this.off += len;
    return new Message(type, this.blob, ooff, len);
  }
  
  public void addbytes(byte[] src, int off, int len) {
    byte[] n = new byte[this.blob.length + len];
    System.arraycopy(this.blob, 0, n, 0, this.blob.length);
    System.arraycopy(src, off, n, this.blob.length, len);
    this.blob = n;
  }
  
  public void addbytes(byte[] src) {
    addbytes(src, 0, src.length);
  }
  
  public void adduint8(int num) {
    addbytes(new byte[] { Utils.sb(num) });
  }
  
  public void adduint16(int num) {
    byte[] buf = new byte[2];
    Utils.uint16e(num, buf, 0);
    addbytes(buf);
  }
  
  public void addint32(int num) {
    byte[] buf = new byte[4];
    Utils.int32e(num, buf, 0);
    addbytes(buf);
  }
  
  public void adduint32(long num) {
    byte[] buf = new byte[4];
    Utils.uint32e(num, buf, 0);
    addbytes(buf);
  }
  
  public void addstring2(String str) {
    byte[] buf;
    try {
      buf = str.getBytes("utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } 
    addbytes(buf);
  }
  
  public void addstring(String str) {
    addstring2(str);
    addbytes(new byte[] { 0 });
  }
  
  public void addcoord(Coord c) {
    addint32(c.x);
    addint32(c.y);
  }
  
  public void addlist(Object... args) {
    for (Object o : args) {
      if (o == null) {
        adduint8(12);
      } else if (o instanceof Integer) {
        adduint8(1);
        addint32(((Integer)o).intValue());
      } else if (o instanceof String) {
        adduint8(2);
        addstring((String)o);
      } else if (o instanceof Coord) {
        adduint8(3);
        addcoord((Coord)o);
      } else if (o instanceof byte[]) {
        byte[] b = (byte[])o;
        adduint8(14);
        if (b.length < 128) {
          adduint8(b.length);
        } else {
          adduint8(128);
          addint32(b.length);
        } 
        addbytes(b);
      } else if (o instanceof Object[]) {
        adduint8(8);
        addlist((Object[])o);
        adduint8(0);
      } else {
        throw new RuntimeException("Cannot encode a " + o.getClass() + " as TTO");
      } 
    } 
  }
  
  public boolean eom() {
    return (this.off >= this.blob.length);
  }
  
  public int int8() {
    return this.blob[this.off++];
  }
  
  public int uint8() {
    return Utils.ub(this.blob[this.off++]);
  }
  
  public int int16() {
    this.off += 2;
    return Utils.int16d(this.blob, this.off - 2);
  }
  
  public int uint16() {
    this.off += 2;
    return Utils.uint16d(this.blob, this.off - 2);
  }
  
  public int int32() {
    this.off += 4;
    return Utils.int32d(this.blob, this.off - 4);
  }
  
  public long uint32() {
    this.off += 4;
    return Utils.uint32d(this.blob, this.off - 4);
  }
  
  public long int64() {
    this.off += 8;
    return Utils.int64d(this.blob, this.off - 8);
  }
  
  public String string() {
    int[] ob = { this.off };
    String ret = Utils.strd(this.blob, ob);
    this.off = ob[0];
    return ret;
  }
  
  public byte[] bytes(int n) {
    byte[] ret = new byte[n];
    System.arraycopy(this.blob, this.off, ret, 0, n);
    this.off += n;
    return ret;
  }
  
  public byte[] bytes() {
    return bytes(this.blob.length - this.off);
  }
  
  public Coord coord() {
    return new Coord(int32(), int32());
  }
  
  public Color color() {
    return new Color(uint8(), uint8(), uint8(), uint8());
  }
  
  public float float32() {
    this.off += 4;
    return Utils.float32d(this.blob, this.off - 4);
  }
  
  public double float64() {
    this.off += 8;
    return Utils.float64d(this.blob, this.off - 8);
  }
  
  public Object[] list() {
    ArrayList<Object> ret = new ArrayList();
    while (this.off < this.blob.length) {
      int len, t = uint8();
      switch (t) {
        case 0:
          break;
        case 1:
          ret.add(Integer.valueOf(int32()));
          continue;
        case 2:
          ret.add(string());
          continue;
        case 3:
          ret.add(coord());
          continue;
        case 4:
          ret.add(Integer.valueOf(uint8()));
          continue;
        case 5:
          ret.add(Integer.valueOf(uint16()));
          continue;
        case 9:
          ret.add(Integer.valueOf(int8()));
          continue;
        case 10:
          ret.add(Integer.valueOf(int16()));
          continue;
        case 6:
          ret.add(color());
          continue;
        case 8:
          ret.add(list());
          continue;
        case 12:
          ret.add(null);
          continue;
        case 14:
          len = uint8();
          if ((len & 0x80) != 0)
            len = int32(); 
          ret.add(bytes(len));
          continue;
        case 15:
          ret.add(Float.valueOf(float32()));
          continue;
        case 16:
          ret.add(Double.valueOf(float64()));
          continue;
      } 
      throw new RuntimeException("Encountered unknown type " + t + " in TTO list.");
    } 
    return ret.toArray();
  }
  
  public Message inflate(int length) {
    Message ret = new Message(0);
    Inflater z = new Inflater();
    z.setInput(this.blob, this.off, length);
    byte[] buf = new byte[10000];
    try {
      while (true) {
        int len;
        if ((len = z.inflate(buf)) == 0) {
          if (!z.finished())
            throw new RuntimeException("Got unterminated gzip blob"); 
          break;
        } 
        ret.addbytes(buf, 0, len);
      } 
    } catch (DataFormatException e) {
      throw new RuntimeException("Got malformed gzip blob", e);
    } 
    return ret;
  }
  
  public Message inflate() {
    return inflate(this.blob.length - this.off);
  }
  
  public String toString() {
    String ret = "";
    for (byte b : this.blob) {
      ret = ret + String.format("%02x ", new Object[] { Byte.valueOf(b) });
    } 
    return "Message(" + this.type + "): " + ret;
  }
}
