package haven;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class Session implements ItemInfo.Owner {
  public static final int PVER = 36;
  
  public static final int MSG_SESS = 0;
  
  public static final int MSG_REL = 1;
  
  public static final int MSG_ACK = 2;
  
  public static final int MSG_BEAT = 3;
  
  public static final int MSG_MAPREQ = 4;
  
  public static final int MSG_MAPDATA = 5;
  
  public static final int MSG_OBJDATA = 6;
  
  public static final int MSG_OBJACK = 7;
  
  public static final int MSG_CLOSE = 8;
  
  public static final int OD_REM = 0;
  
  public static final int OD_MOVE = 1;
  
  public static final int OD_RES = 2;
  
  public static final int OD_LINBEG = 3;
  
  public static final int OD_LINSTEP = 4;
  
  public static final int OD_SPEECH = 5;
  
  public static final int OD_COMPOSE = 6;
  
  public static final int OD_ZOFF = 7;
  
  public static final int OD_LUMIN = 8;
  
  public static final int OD_AVATAR = 9;
  
  public static final int OD_FOLLOW = 10;
  
  public static final int OD_HOMING = 11;
  
  public static final int OD_OVERLAY = 12;
  
  public static final int OD_HEALTH = 14;
  
  public static final int OD_BUDDY = 15;
  
  public static final int OD_CMPPOSE = 16;
  
  public static final int OD_CMPMOD = 17;
  
  public static final int OD_CMPEQU = 18;
  
  public static final int OD_ICON = 19;
  
  public static final int OD_END = 255;
  
  public static final int SESSERR_AUTH = 1;
  
  public static final int SESSERR_BUSY = 2;
  
  public static final int SESSERR_CONN = 3;
  
  public static final int SESSERR_PVER = 4;
  
  public static final int SESSERR_EXPR = 5;
  
  static final int ackthresh = 30;
  
  DatagramSocket sk;
  
  SocketAddress server;
  
  Thread rworker;
  
  Thread sworker;
  
  Thread ticker;
  
  Object[] args;
  
  public int connfailed = 0;
  
  public String state = "conn";
  
  int tseq = 0;
  
  int rseq = 0;
  
  int ackseq;
  
  long acktime = -1L;
  
  LinkedList<Message> uimsgs = new LinkedList<>();
  
  Map<Integer, Message> waiting = new TreeMap<>();
  
  LinkedList<Message> pending = new LinkedList<>();
  
  Map<Long, ObjAck> objacks = new TreeMap<>();
  
  String username;
  
  byte[] cookie;
  
  final Map<Integer, CachedRes> rescache = new TreeMap<>();
  
  public final Glob glob;
  
  public byte[] sesskey;
  
  public class MessageException extends RuntimeException {
    public Message msg;
    
    public MessageException(String text, Message msg) {
      super(text);
      this.msg = msg;
    }
  }
  
  public static class LoadingIndir extends Loading {
    public final int resid;
    
    private final Session.CachedRes res;
    
    private LoadingIndir(Session.CachedRes res) {
      this.res = res;
      this.resid = res.resid;
    }
    
    public void waitfor() throws InterruptedException {
      synchronized (this.res) {
        while (this.res.resnm == null)
          this.res.wait(); 
      } 
    }
    
    public boolean canwait() {
      return true;
    }
  }
  
  private static class CachedRes {
    private final int resid;
    
    private String resnm = null;
    
    private int resver;
    
    private Reference<Indir<Resource>> ind;
    
    private CachedRes(int id) {
      this.resid = id;
    }
    
    private Indir<Resource> get() {
      Indir<Resource> ind = (this.ind == null) ? null : this.ind.get();
      if (ind == null) {
        ind = new Indir<Resource>() {
            private Resource res;
            
            public Resource get() {
              if (Session.CachedRes.this.resnm == null)
                throw new Session.LoadingIndir(Session.CachedRes.this); 
              if (this.res == null)
                this.res = Resource.load(Session.CachedRes.this.resnm, Session.CachedRes.this.resver, 0); 
              if (this.res.loading)
                throw new Resource.Loading(this.res); 
              return this.res;
            }
            
            public String toString() {
              if (this.res == null)
                return "<res:" + Session.CachedRes.this.resid + ">"; 
              if (this.res.loading)
                return "<!" + this.res + ">"; 
              return "<" + this.res + ">";
            }
          };
        this.ind = new WeakReference<>(ind);
      } 
      return ind;
    }
    
    public void set(String nm, int ver) {
      synchronized (this) {
        this.resnm = nm;
        this.resver = ver;
        notifyAll();
      } 
      Resource.load(nm, ver, -5);
    }
  }
  
  private CachedRes cachedres(int id) {
    synchronized (this.rescache) {
      CachedRes ret = this.rescache.get(Integer.valueOf(id));
      if (ret != null)
        return ret; 
      ret = new CachedRes(id);
      this.rescache.put(Integer.valueOf(id), ret);
      return ret;
    } 
  }
  
  public Indir<Resource> getres(int id) {
    return cachedres(id).get();
  }
  
  public int getresid(String name) {
    synchronized (this.rescache) {
      for (CachedRes cres : this.rescache.values()) {
        if (name.equals(cres.resnm))
          return cres.resid; 
      } 
    } 
    return 0;
  }
  
  private class ObjAck {
    long id;
    
    int frame;
    
    long recv;
    
    long sent;
    
    public ObjAck(long id, int frame, long recv) {
      this.id = id;
      this.frame = frame;
      this.recv = recv;
      this.sent = 0L;
    }
  }
  
  private class Ticker extends HackThread {
    public Ticker() {
      super("Server time ticker");
      setDaemon(true);
    }
    
    public void run() {
      try {
        while (true) {
          long then = System.currentTimeMillis();
          Session.this.glob.oc.tick();
          long now = System.currentTimeMillis();
          if (now - then < 70L)
            Thread.sleep(70L - now - then); 
        } 
      } catch (InterruptedException interruptedException) {
        return;
      } 
    }
  }
  
  private class RWorker extends HackThread {
    private static final int _1000 = 1000;
    
    private static final double _256_0 = 256.0D;
    
    private static final int _32768 = 32768;
    
    private static final int _65536 = 65536;
    
    private static final String STRING = "";
    
    private static final String CONN = "conn";
    
    boolean alive;
    
    public RWorker() {
      super("Session reader");
      setDaemon(true);
    }
    
    private void gotack(int seq) {
      synchronized (Session.this.pending) {
        for (ListIterator<Message> i = Session.this.pending.listIterator(); i.hasNext(); ) {
          Message msg = i.next();
          if (msg.seq <= seq)
            i.remove(); 
        } 
      } 
    }
    
    private void getobjdata(Message msg) {
      OCache oc = Session.this.glob.oc;
      while (msg.off < msg.blob.length) {
        int fl = msg.uint8();
        long id = msg.uint32();
        int frame = msg.int32();
        synchronized (oc) {
          int type;
          if ((fl & 0x1) != 0)
            oc.remove(id, frame - 1); 
          Gob gob = oc.getgob(id, frame);
          if (gob != null) {
            gob.frame = frame;
            gob.virtual = ((fl & 0x2) != 0);
          } 
          while (true) {
            type = msg.uint8();
            if (type == 0) {
              oc.remove(id, frame);
              continue;
            } 
            if (type == 1) {
              Coord c = msg.coord();
              int ia = msg.uint16();
              if (gob != null)
                oc.move(gob, c, ia / 65536.0D * Math.PI * 2.0D); 
              continue;
            } 
            if (type == 2) {
              Message sdt;
              int resid = msg.uint16();
              if ((resid & 0x8000) != 0) {
                resid &= 0xFFFF7FFF;
                sdt = msg.derive(0, msg.uint8());
              } else {
                sdt = new Message(0);
              } 
              if (gob != null)
                oc.cres(gob, Session.this.getres(resid), sdt); 
              continue;
            } 
            if (type == 3) {
              Coord s = msg.coord();
              Coord t = msg.coord();
              int c = msg.int32();
              if (gob != null)
                oc.linbeg(gob, s, t, c); 
              continue;
            } 
            if (type == 4) {
              int l = msg.int32();
              if (gob != null)
                oc.linstep(gob, l); 
              continue;
            } 
            if (type == 5) {
              float zo = msg.int16() / 100.0F;
              String text = msg.string();
              if (gob != null && !ChatUI.hasTags(text))
                oc.speak(gob, zo, text); 
              continue;
            } 
            if (type == 6) {
              Indir<Resource> base = Session.this.getres(msg.uint16());
              if (gob != null)
                oc.composite(gob, base); 
              continue;
            } 
            if (type == 16) {
              if (Config.remove_animations) {
                List<ResData> list1 = null, list2 = null;
                int i = msg.uint8();
                int j = msg.uint8();
                boolean bool = ((i & 0x1) != 0);
                if ((i & 0x2) != 0)
                  while (true) {
                    int resid = msg.uint16();
                    if (resid == 65535)
                      break; 
                    Message sdt = Message.nil;
                    if ((resid & 0x8000) != 0) {
                      resid &= 0xFFFF7FFF;
                      sdt = msg.derive(0, msg.uint8());
                    } 
                  }  
                float f = 0.0F;
                if ((i & 0x4) != 0) {
                  while (true) {
                    int resid = msg.uint16();
                    if (resid == 65535)
                      break; 
                    Message sdt = Message.nil;
                    if ((resid & 0x8000) != 0) {
                      resid &= 0xFFFF7FFF;
                      sdt = msg.derive(0, msg.uint8());
                    } 
                  } 
                  f = msg.uint8() / 10.0F;
                } 
                continue;
              } 
              List<ResData> poses = null, tposes = null;
              int pfl = msg.uint8();
              int seq = msg.uint8();
              boolean interp = ((pfl & 0x1) != 0);
              if ((pfl & 0x2) != 0) {
                poses = new LinkedList<>();
                while (true) {
                  int resid = msg.uint16();
                  if (resid == 65535)
                    break; 
                  Message sdt = Message.nil;
                  if ((resid & 0x8000) != 0) {
                    resid &= 0xFFFF7FFF;
                    sdt = msg.derive(0, msg.uint8());
                  } 
                  poses.add(new ResData(Session.this.getres(resid), sdt));
                } 
              } 
              float ttime = 0.0F;
              if ((pfl & 0x4) != 0) {
                tposes = new LinkedList<>();
                while (true) {
                  int resid = msg.uint16();
                  if (resid == 65535)
                    break; 
                  Message sdt = Message.nil;
                  if ((resid & 0x8000) != 0) {
                    resid &= 0xFFFF7FFF;
                    sdt = msg.derive(0, msg.uint8());
                  } 
                  tposes.add(new ResData(Session.this.getres(resid), sdt));
                } 
                ttime = msg.uint8() / 10.0F;
              } 
              if (gob != null)
                oc.cmppose(gob, seq, poses, tposes, interp, ttime); 
              continue;
            } 
            if (type == 17) {
              List<Composited.MD> mod = new LinkedList<>();
              while (true) {
                int modid = msg.uint16();
                if (modid == 65535)
                  break; 
                Indir<Resource> modr = Session.this.getres(modid);
                List<Indir<Resource>> tex = new LinkedList<>();
                while (true) {
                  int resid = msg.uint16();
                  if (resid == 65535)
                    break; 
                  tex.add(Session.this.getres(resid));
                } 
                mod.add(new Composited.MD(modr, tex));
              } 
              if (gob != null)
                oc.cmpmod(gob, mod); 
              continue;
            } 
            if (type == 18) {
              List<Composited.ED> equ = new LinkedList<>();
              while (true) {
                Indir<Resource> res;
                Coord3f off;
                int h = msg.uint8();
                if (h == 255)
                  break; 
                int ef = h & 0x80;
                int et = h & 0x7F;
                String at = msg.string();
                int resid = msg.uint16();
                if (resid == 65535) {
                  res = null;
                } else {
                  res = Session.this.getres(resid);
                } 
                if ((ef & 0x80) != 0) {
                  int x = msg.int16(), y = msg.int16(), z = msg.int16();
                  off = new Coord3f(x / 1000.0F, y / 1000.0F, z / 1000.0F);
                } else {
                  off = Coord3f.o;
                } 
                equ.add(new Composited.ED(et, at, res, off));
              } 
              if (gob != null)
                oc.cmpequ(gob, equ); 
              continue;
            } 
            if (type == 7) {
              float off = msg.int16() / 100.0F;
              if (gob != null)
                oc.zoff(gob, off); 
              continue;
            } 
            if (type == 8) {
              Coord off = msg.coord();
              int sz = msg.uint16();
              int str = msg.uint8();
              if (gob != null)
                oc.lumin(gob, off, sz, str); 
              continue;
            } 
            if (type == 9) {
              List<Indir<Resource>> layers = new LinkedList<>();
              while (true) {
                int layer = msg.uint16();
                if (layer == 65535)
                  break; 
                layers.add(Session.this.getres(layer));
              } 
              if (gob != null)
                oc.avatar(gob, layers); 
              continue;
            } 
            if (type == 10) {
              long oid = msg.uint32();
              Indir<Resource> xfres = null;
              String xfname = null;
              if (oid != 4294967295L) {
                xfres = Session.this.getres(msg.uint16());
                xfname = msg.string();
              } 
              if (gob != null)
                oc.follow(gob, oid, xfres, xfname); 
              continue;
            } 
            if (type == 11) {
              long oid = msg.uint32();
              if (oid == 4294967295L) {
                if (gob != null)
                  oc.homostop(gob); 
                continue;
              } 
              if (oid == 4294967294L) {
                Coord coord = msg.coord();
                int i = msg.uint16();
                if (gob != null)
                  oc.homocoord(gob, coord, i); 
                continue;
              } 
              Coord tgtc = msg.coord();
              int v = msg.uint16();
              if (gob != null)
                oc.homing(gob, oid, tgtc, v); 
              continue;
            } 
            if (type == 12) {
              Indir<Resource> res;
              Message sdt;
              int olid = msg.int32();
              boolean prs = ((olid & 0x1) != 0);
              olid >>= 1;
              int resid = msg.uint16();
              if (resid == 65535) {
                res = null;
                sdt = null;
              } else {
                if ((resid & 0x8000) != 0) {
                  resid &= 0xFFFF7FFF;
                  sdt = msg.derive(0, msg.uint8());
                } else {
                  sdt = new Message(0);
                } 
                res = Session.this.getres(resid);
              } 
              if (gob != null)
                oc.overlay(gob, olid, prs, res, sdt); 
              continue;
            } 
            if (type == 14) {
              int hp = msg.uint8();
              if (gob != null)
                oc.health(gob, hp); 
              continue;
            } 
            if (type == 15) {
              String name = msg.string();
              if (name.length() > 0) {
                int group = msg.uint8();
                int btype = msg.uint8();
                if (gob != null)
                  oc.buddy(gob, name, group, btype); 
                continue;
              } 
              if (gob != null)
                oc.buddy(gob, null, 0, 0); 
              continue;
            } 
            if (type == 19) {
              Indir<Resource> res;
              int resid = msg.uint16();
              if (resid == 65535) {
                res = null;
              } else {
                res = Session.this.getres(resid);
                int i = msg.uint8();
              } 
              if (gob != null)
                oc.icon(gob, res); 
              continue;
            } 
            break;
          } 
          if (type == 255) {
            synchronized (Session.this.objacks) {
              if (Session.this.objacks.containsKey(Long.valueOf(id))) {
                Session.ObjAck a = Session.this.objacks.get(Long.valueOf(id));
                a.frame = frame;
                a.recv = System.currentTimeMillis();
              } else {
                Session.this.objacks.put(Long.valueOf(id), new Session.ObjAck(id, frame, System.currentTimeMillis()));
              } 
            } 
            continue;
          } 
          throw new Session.MessageException("Unknown objdelta type: " + type, msg);
        } 
      } 
      synchronized (Session.this.sworker) {
        Session.this.sworker.notifyAll();
      } 
    }
    
    private void handlerel(Message msg) {
      if (msg.type == 0) {
        synchronized (Session.this.uimsgs) {
          Session.this.uimsgs.add(msg);
        } 
      } else if (msg.type == 1) {
        synchronized (Session.this.uimsgs) {
          Session.this.uimsgs.add(msg);
        } 
      } else if (msg.type == 2) {
        synchronized (Session.this.uimsgs) {
          Session.this.uimsgs.add(msg);
        } 
      } else if (msg.type == 3) {
        Session.this.glob.map.invalblob(msg);
      } else if (msg.type == 4) {
        Session.this.glob.blob(msg);
      } else if (msg.type == 5) {
        Session.this.glob.paginae(msg);
      } else if (msg.type == 6) {
        int resid = msg.uint16();
        String resname = msg.string();
        int resver = msg.uint16();
        String resname2 = resname;
        int resver2 = resver;
        Session.this.cachedres(resid).set(resname2, resver2);
      } else if (msg.type == 7) {
        Session.this.glob.party.msg(msg);
      } else if (msg.type == 8) {
        Indir<Resource> res = Session.this.getres(msg.uint16());
        double vol = msg.uint16() / 256.0D;
        double spd = msg.uint16() / 256.0D;
        Audio.play(res);
      } else if (msg.type == 9) {
        Session.this.glob.cattr(msg);
      } else if (msg.type == 10) {
        String resnm = msg.string();
        int resver = msg.uint16();
        boolean loop = (!msg.eom() && msg.uint8() != 0);
        if (resnm.equals("")) {
          Music.play(null, false);
        } else {
          Music.play(Resource.load(resnm, resver), loop);
        } 
      } else if (msg.type == 11) {
        Session.this.glob.map.tilemap(msg);
      } else if (msg.type == 12) {
        Session.this.glob.buffmsg(msg);
      } else if (msg.type == 13) {
        Session.this.sesskey = msg.bytes();
      } else {
        throw new Session.MessageException("Unknown rmsg type: " + msg.type, msg);
      } 
    }
    
    private void getrel(int seq, Message msg) {
      if (seq == Session.this.rseq) {
        int lastack;
        synchronized (Session.this.uimsgs) {
          handlerel(msg);
          while (true) {
            Session.this.rseq = ((lastack = Session.this.rseq) + 1) % 65536;
            if (!Session.this.waiting.containsKey(Integer.valueOf(Session.this.rseq)))
              break; 
            handlerel(Session.this.waiting.get(Integer.valueOf(Session.this.rseq)));
            Session.this.waiting.remove(Integer.valueOf(Session.this.rseq));
          } 
        } 
        Session.this.sendack(lastack);
        synchronized (Session.this) {
          Session.this.notifyAll();
        } 
      } else if (Utils.floormod(seq - Session.this.rseq, 65536) < 32768) {
        Session.this.waiting.put(Integer.valueOf(seq), msg);
      } 
    }
    
    public void run() {
      try {
        this.alive = true;
        try {
          Session.this.sk.setSoTimeout(1000);
        } catch (SocketException e) {
          throw new RuntimeException(e);
        } 
        while (this.alive) {
          DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
          try {
            Session.this.sk.receive(p);
          } catch (ClosedByInterruptException e) {
            break;
          } catch (SocketTimeoutException e) {
            continue;
          } catch (IOException e) {
            throw new RuntimeException(e);
          } 
          if (!p.getSocketAddress().equals(Session.this.server))
            continue; 
          Message msg = new Message(p.getData()[0], p.getData(), 1, p.getLength() - 1);
          if (msg.type == 0 && 
            Session.this.state == "conn") {
            int error = msg.uint8();
            synchronized (Session.this) {
              if (error == 0) {
                Session.this.state = "";
              } else {
                Session.this.connfailed = error;
                Session.this.close();
              } 
              Session.this.notifyAll();
            } 
          } 
          if (Session.this.state != "conn") {
            if (msg.type != 0)
              if (msg.type == 1) {
                int seq = msg.uint16();
                while (!msg.eom()) {
                  int len, type = msg.uint8();
                  if ((type & 0x80) != 0) {
                    type &= 0x7F;
                    len = msg.uint16();
                  } else {
                    len = msg.blob.length - msg.off;
                  } 
                  getrel(seq, new Message(type, msg.blob, msg.off, len));
                  msg.off += len;
                  seq++;
                } 
              } else if (msg.type == 2) {
                gotack(msg.uint16());
              } else if (msg.type == 5) {
                Session.this.glob.map.mapdata(msg);
              } else if (msg.type == 6) {
                getobjdata(msg);
              } else if (msg.type == 8) {
                synchronized (Session.this) {
                  Session.this.state = "fin";
                  Session.this.notifyAll();
                } 
                Session.this.close();
              } else {
                throw new Session.MessageException("Unknown message type: " + msg.type, msg);
              }  
            if (Config.autolog && UI.instance.timesinceactive() > 600000L)
              try {
                UI.instance.be_active();
                UI.instance.cons.run("act lo cs");
              } catch (Exception exception) {} 
          } 
        } 
      } finally {
        synchronized (Session.this) {
          Session.this.state = "dead";
          Session.this.notifyAll();
        } 
      } 
    }
    
    public void interrupt() {
      this.alive = false;
      super.interrupt();
    }
  }
  
  private class SWorker extends HackThread {
    public SWorker() {
      super("Session writer");
      setDaemon(true);
    }
    
    public void run() {
      try {
        long last = 0L, retries = 0L;
        while (true) {
          long now = System.currentTimeMillis();
          if (Session.this.state == "conn") {
            if (now - last > 2000L) {
              if (++retries > 5L)
                synchronized (Session.this) {
                  Session.this.connfailed = 3;
                  Session.this.notifyAll();
                  return;
                }  
              Message msg = new Message(0);
              msg.adduint16(2);
              msg.addstring("Salem");
              msg.adduint16(36);
              msg.addstring(Session.this.username);
              msg.adduint16(Session.this.cookie.length);
              msg.addbytes(Session.this.cookie);
              msg.addlist(Session.this.args);
              Session.this.sendmsg(msg);
              last = now;
            } 
            Thread.sleep(100L);
            continue;
          } 
          long to = 5000L;
          synchronized (Session.this.pending) {
            if (Session.this.pending.size() > 0)
              to = 60L; 
          } 
          synchronized (Session.this.objacks) {
            if (Session.this.objacks.size() > 0 && to > 120L)
              to = 200L; 
          } 
          synchronized (this) {
            if (Session.this.acktime > 0L)
              to = Session.this.acktime + 30L - now; 
            if (to > 0L)
              wait(to); 
          } 
          now = System.currentTimeMillis();
          boolean beat = true;
          synchronized (Session.this.pending) {
            if (Session.this.pending.size() > 0) {
              for (Message msg : Session.this.pending) {
                int txtime;
                if (msg.retx == 0) {
                  txtime = 0;
                } else if (msg.retx == 1) {
                  txtime = 80;
                } else if (msg.retx < 4) {
                  txtime = 200;
                } else if (msg.retx < 10) {
                  txtime = 620;
                } else {
                  txtime = 2000;
                } 
                if (now - msg.last > txtime) {
                  msg.last = now;
                  msg.retx++;
                  Message rmsg = new Message(1);
                  rmsg.adduint16(msg.seq);
                  rmsg.adduint8(msg.type);
                  rmsg.addbytes(msg.blob);
                  Session.this.sendmsg(rmsg);
                } 
              } 
              beat = false;
            } 
          } 
          synchronized (Session.this.objacks) {
            Message msg = null;
            for (Iterator<Session.ObjAck> i = Session.this.objacks.values().iterator(); i.hasNext(); ) {
              Session.ObjAck a = i.next();
              boolean send = false, del = false;
              if (now - a.sent > 200L)
                send = true; 
              if (now - a.recv > 120L)
                send = del = true; 
              if (send) {
                if (msg == null) {
                  msg = new Message(7);
                } else if (msg.blob.length > 992) {
                  Session.this.sendmsg(msg);
                  beat = false;
                  msg = new Message(7);
                } 
                msg.adduint32(a.id);
                msg.addint32(a.frame);
                a.sent = now;
              } 
              if (del)
                i.remove(); 
            } 
            if (msg != null) {
              Session.this.sendmsg(msg);
              beat = false;
            } 
          } 
          synchronized (this) {
            if (Session.this.acktime > 0L && now - Session.this.acktime >= 30L) {
              byte[] msg = { 2, 0, 0 };
              Utils.uint16e(Session.this.ackseq, msg, 1);
              Session.this.sendmsg(msg);
              Session.this.acktime = -1L;
              beat = false;
            } 
          } 
          if (beat && 
            now - last > 5000L) {
            Session.this.sendmsg(new byte[] { 3 });
            last = now;
          } 
        } 
      } catch (InterruptedException e) {
        for (int i = 0; i < 5; i++) {
          Session.this.sendmsg(new Message(8));
          long f = System.currentTimeMillis();
          while (true) {
            synchronized (Session.this) {
              if (Session.this.state == "conn" || Session.this.state == "fin" || Session.this.state == "dead")
                break; 
              Session.this.state = "close";
              long now = System.currentTimeMillis();
              if (now - f > 500L)
                break; 
              try {
                Session.this.wait(500L - now - f);
              } catch (InterruptedException interruptedException) {}
            } 
          } 
        } 
      } finally {
        Session.this.ticker.interrupt();
        Session.this.rworker.interrupt();
      } 
    }
  }
  
  public Session(SocketAddress server, String username, byte[] cookie, Object... args) {
    this.server = server;
    this.username = username;
    this.cookie = cookie;
    this.args = args;
    this.glob = new Glob(this);
    try {
      this.sk = new DatagramSocket();
    } catch (SocketException e) {
      throw new RuntimeException(e);
    } 
    this.rworker = new RWorker();
    this.rworker.start();
    this.sworker = new SWorker();
    this.sworker.start();
    this.ticker = new Ticker();
    this.ticker.start();
  }
  
  private void sendack(int seq) {
    synchronized (this.sworker) {
      if (this.acktime < 0L)
        this.acktime = System.currentTimeMillis(); 
      this.ackseq = seq;
      this.sworker.notifyAll();
    } 
  }
  
  public void close() {
    this.sworker.interrupt();
  }
  
  public synchronized boolean alive() {
    return (this.state != "dead");
  }
  
  public void queuemsg(Message msg) {
    msg.seq = this.tseq;
    this.tseq = (this.tseq + 1) % 65536;
    synchronized (this.pending) {
      this.pending.add(msg);
    } 
    synchronized (this.sworker) {
      this.sworker.notify();
    } 
  }
  
  public Message getuimsg() {
    synchronized (this.uimsgs) {
      if (this.uimsgs.size() == 0)
        return null; 
      return this.uimsgs.remove();
    } 
  }
  
  public void sendmsg(Message msg) {
    byte[] buf = new byte[msg.blob.length + 1];
    buf[0] = (byte)msg.type;
    System.arraycopy(msg.blob, 0, buf, 1, msg.blob.length);
    sendmsg(buf);
  }
  
  public void sendmsg(byte[] msg) {
    try {
      this.sk.send(new DatagramPacket(msg, msg.length, this.server));
    } catch (IOException iOException) {}
  }
  
  public Glob glob() {
    return this.glob;
  }
  
  public List<ItemInfo> info() {
    return null;
  }
}
