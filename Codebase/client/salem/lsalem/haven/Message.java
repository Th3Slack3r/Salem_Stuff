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

   @Override
   public boolean equals(Object o2) {
      if (!(o2 instanceof Message)) {
         return false;
      } else {
         Message m2 = (Message)o2;
         if (m2.blob.length != this.blob.length) {
            return false;
         } else {
            for (int i = 0; i < this.blob.length; i++) {
               if (m2.blob[i] != this.blob[i]) {
                  return false;
               }
            }

            return true;
         }
      }
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
      this.addbytes(src, 0, src.length);
   }

   public void adduint8(int num) {
      this.addbytes(new byte[]{Utils.sb(num)});
   }

   public void adduint16(int num) {
      byte[] buf = new byte[2];
      Utils.uint16e(num, buf, 0);
      this.addbytes(buf);
   }

   public void addint32(int num) {
      byte[] buf = new byte[4];
      Utils.int32e(num, buf, 0);
      this.addbytes(buf);
   }

   public void adduint32(long num) {
      byte[] buf = new byte[4];
      Utils.uint32e(num, buf, 0);
      this.addbytes(buf);
   }

   public void addstring2(String str) {
      byte[] buf;
      try {
         buf = str.getBytes("utf-8");
      } catch (UnsupportedEncodingException var4) {
         throw new RuntimeException(var4);
      }

      this.addbytes(buf);
   }

   public void addstring(String str) {
      this.addstring2(str);
      this.addbytes(new byte[]{0});
   }

   public void addcoord(Coord c) {
      this.addint32(c.x);
      this.addint32(c.y);
   }

   public void addlist(Object... args) {
      for (Object o : args) {
         if (o == null) {
            this.adduint8(12);
         } else if (o instanceof Integer) {
            this.adduint8(1);
            this.addint32((Integer)o);
         } else if (o instanceof String) {
            this.adduint8(2);
            this.addstring((String)o);
         } else if (o instanceof Coord) {
            this.adduint8(3);
            this.addcoord((Coord)o);
         } else {
            if (!(o instanceof byte[])) {
               throw new RuntimeException("Cannot encode a " + o.getClass() + " as TTO");
            }

            byte[] b = (byte[])o;
            this.adduint8(14);
            if (b.length < 128) {
               this.adduint8(b.length);
            } else {
               this.adduint8(128);
               this.addint32(b.length);
            }

            this.addbytes(b);
         }
      }
   }

   public boolean eom() {
      return this.off >= this.blob.length;
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
      int[] ob = new int[]{this.off};
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
      return this.bytes(this.blob.length - this.off);
   }

   public Coord coord() {
      return new Coord(this.int32(), this.int32());
   }

   public Color color() {
      return new Color(this.uint8(), this.uint8(), this.uint8(), this.uint8());
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
      ArrayList<Object> ret = new ArrayList<>();

      while (this.off < this.blob.length) {
         int t = this.uint8();
         switch (t) {
            case 0:
               return ret.toArray();
            case 1:
               ret.add(this.int32());
               break;
            case 2:
               ret.add(this.string());
               break;
            case 3:
               ret.add(this.coord());
               break;
            case 4:
               ret.add(this.uint8());
               break;
            case 5:
               ret.add(this.uint16());
               break;
            case 6:
               ret.add(this.color());
               break;
            case 7:
            case 11:
            case 13:
            default:
               throw new RuntimeException("Encountered unknown type " + t + " in TTO list.");
            case 8:
               ret.add(this.list());
               break;
            case 9:
               ret.add(this.int8());
               break;
            case 10:
               ret.add(this.int16());
               break;
            case 12:
               ret.add(null);
               break;
            case 14:
               int len = this.uint8();
               if ((len & 128) != 0) {
                  len = this.int32();
               }

               ret.add(this.bytes(len));
               break;
            case 15:
               ret.add(this.float32());
               break;
            case 16:
               ret.add(this.float64());
         }
      }

      return ret.toArray();
   }

   public Message inflate(int length) {
      Message ret = new Message(0);
      Inflater z = new Inflater();
      z.setInput(this.blob, this.off, length);
      byte[] buf = new byte[10000];

      while (true) {
         try {
            int len;
            if ((len = z.inflate(buf)) == 0) {
               if (!z.finished()) {
                  throw new RuntimeException("Got unterminated gzip blob");
               }

               return ret;
            }

            ret.addbytes(buf, 0, len);
         } catch (DataFormatException var6) {
            throw new RuntimeException("Got malformed gzip blob", var6);
         }
      }
   }

   public Message inflate() {
      return this.inflate(this.blob.length - this.off);
   }

   @Override
   public String toString() {
      String ret = "";

      for (byte b : this.blob) {
         ret = ret + String.format("%02x ", b);
      }

      return "Message(" + this.type + "): " + ret;
   }
}
