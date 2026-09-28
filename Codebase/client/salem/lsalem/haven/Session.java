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
   public static final int OD_DRAWOFF = 7;
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
   Map<Long, Session.ObjAck> objacks = new TreeMap<>();
   public String username;
   byte[] cookie;
   final Map<Integer, Session.CachedRes> rescache = new TreeMap<>();
   public final Glob glob;
   public byte[] sesskey;

   public Session.CachedRes cachedres(int id) {
      synchronized (this.rescache) {
         Session.CachedRes ret = this.rescache.get(id);
         if (ret != null) {
            return ret;
         } else {
            ret = new Session.CachedRes(id);
            this.rescache.put(id, ret);
            return ret;
         }
      }
   }

   public Indir<Resource> getres(int id) {
      return this.cachedres(id).get();
   }

   public int getresid(String name) {
      synchronized (this.rescache) {
         for (Session.CachedRes cres : this.rescache.values()) {
            if (name.equals(cres.resnm)) {
               return cres.resid;
            }
         }

         return 0;
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
      } catch (SocketException var6) {
         throw new RuntimeException(var6);
      }

      this.rworker = new Session.RWorker();
      this.rworker.start();
      this.sworker = new Session.SWorker();
      this.sworker.start();
      this.ticker = new Session.Ticker();
      this.ticker.start();
   }

   private void sendack(int seq) {
      synchronized (this.sworker) {
         if (this.acktime < 0L) {
            this.acktime = System.currentTimeMillis();
         }

         this.ackseq = seq;
         this.sworker.notifyAll();
      }
   }

   public void close() {
      this.sworker.interrupt();
   }

   public synchronized boolean alive() {
      return this.state != "dead";
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
         return this.uimsgs.size() == 0 ? null : this.uimsgs.remove();
      }
   }

   public void sendmsg(Message msg) {
      byte[] buf = new byte[msg.blob.length + 1];
      buf[0] = (byte)msg.type;
      System.arraycopy(msg.blob, 0, buf, 1, msg.blob.length);
      this.sendmsg(buf);
   }

   public void sendmsg(byte[] msg) {
      try {
         this.sk.send(new DatagramPacket(msg, msg.length, this.server));
      } catch (IOException var3) {
      }
   }

   @Override
   public Glob glob() {
      return this.glob;
   }

   @Override
   public List<ItemInfo> info() {
      return null;
   }

   public static class CachedRes {
      private final int resid;
      public String resnm = null;
      private int resver;
      private Reference<Indir<Resource>> ind;

      private CachedRes(int id) {
         this.resid = id;
      }

      public Indir<Resource> get() {
         Indir<Resource> ind = this.ind == null ? null : this.ind.get();
         if (ind == null) {
            ind = new Indir<Resource>() {
               private Resource res;

               public Resource get() {
                  if (CachedRes.this.resnm == null) {
                     throw new Session.LoadingIndir(CachedRes.this);
                  } else {
                     if (this.res == null) {
                        this.res = Resource.load(CachedRes.this.resnm, CachedRes.this.resver, 0);
                     }

                     if (this.res.loading) {
                        throw new Resource.Loading(this.res);
                     } else {
                        return this.res;
                     }
                  }
               }

               @Override
               public String toString() {
                  if (this.res == null) {
                     return "<res:" + CachedRes.this.resid + ">";
                  } else {
                     return this.res.loading ? "<!" + this.res + ">" : "<" + this.res + ">";
                  }
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
            this.notifyAll();
         }

         Resource.load(nm, ver, -5);
      }
   }

   public static class LoadingIndir extends Loading {
      public final int resid;
      private final Session.CachedRes res;

      private LoadingIndir(Session.CachedRes res) {
         this.res = res;
         this.resid = res.resid;
      }

      @Override
      public void waitfor() throws InterruptedException {
         synchronized (this.res) {
            while (this.res.resnm == null) {
               this.res.wait();
            }
         }
      }

      @Override
      public boolean canwait() {
         return true;
      }
   }

   public class MessageException extends RuntimeException {
      public Message msg;

      public MessageException(String text, Message msg) {
         super(text);
         this.msg = msg;
      }
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

   private class RWorker extends HackThread {
      boolean alive;

      public RWorker() {
         super("Session reader");
         this.setDaemon(true);
      }

      private void gotack(int seq) {
         synchronized (Session.this.pending) {
            ListIterator<Message> i = Session.this.pending.listIterator();

            while (i.hasNext()) {
               Message msg = i.next();
               if (msg.seq <= seq) {
                  i.remove();
               }
            }
         }
      }

      protected void getobjdata(Message msg) {
         OCache oc = Session.this.glob.oc;

         while (msg.off < msg.blob.length) {
            int fl = msg.uint8();
            long id = msg.uint32();
            int frame = msg.int32();
            synchronized (oc) {
               if ((fl & 1) != 0) {
                  oc.remove(id, frame - 1);
               }

               Gob gob = oc.getgob(id, frame);
               if (gob != null) {
                  gob.frame = frame;
                  gob.virtual = (fl & 2) != 0;
               }

               while (true) {
                  int type = msg.uint8();
                  if (type == 0) {
                     oc.remove(id, frame);
                  } else if (type == 1) {
                     Coord c = msg.coord();
                     int ia = msg.uint16();
                     if (gob != null) {
                        oc.move(gob, c, ia / 65536.0 * Math.PI * 2.0);
                     }
                  } else if (type == 2) {
                     int resid = msg.uint16();
                     Message sdt;
                     if ((resid & 32768) != 0) {
                        resid &= -32769;
                        sdt = msg.derive(0, msg.uint8());
                     } else {
                        sdt = new Message(0);
                     }

                     if (gob != null) {
                        oc.cres(gob, Session.this.getres(resid), sdt);
                     }
                  } else if (type == 3) {
                     Coord s = msg.coord();
                     Coord t = msg.coord();
                     int c = msg.int32();
                     if (gob != null) {
                        oc.linbeg(gob, s, t, c);
                     }
                  } else if (type == 4) {
                     int l = msg.int32();
                     if (gob != null) {
                        oc.linstep(gob, l);
                     }
                  } else if (type == 5) {
                     float zo = msg.int16() / 100.0F;
                     String text = msg.string();
                     if (gob != null && !ChatUI.hasTags(text)) {
                        oc.speak(gob, zo, text);
                     }
                  } else if (type != 6) {
                     if (type != 16) {
                        if (type != 17) {
                           if (type != 18) {
                              if (type != 7) {
                                 if (type != 8) {
                                    if (type != 9) {
                                       if (type == 10) {
                                          long oid = msg.uint32();
                                          Indir<Resource> xfres = null;
                                          String xfname = null;
                                          if (oid != 4294967295L) {
                                             xfres = Session.this.getres(msg.uint16());
                                             xfname = msg.string();
                                          }

                                          if (gob != null) {
                                             oc.follow(gob, oid, xfres, xfname);
                                          }
                                       } else if (type == 11) {
                                          long oidx = msg.uint32();
                                          if (oidx == 4294967295L) {
                                             if (gob != null) {
                                                oc.homostop(gob);
                                             }
                                          } else if (oidx == 4294967294L) {
                                             Coord tgtc = msg.coord();
                                             int v = msg.uint16();
                                             if (gob != null) {
                                                oc.homocoord(gob, tgtc, v);
                                             }
                                          } else {
                                             Coord tgtc = msg.coord();
                                             int v = msg.uint16();
                                             if (gob != null) {
                                                oc.homing(gob, oidx, tgtc, v);
                                             }
                                          }
                                       } else if (type == 12) {
                                          int olid = msg.int32();
                                          boolean prs = (olid & 1) != 0;
                                          olid >>= 1;
                                          int residx = msg.uint16();
                                          Indir<Resource> res;
                                          Message sdtx;
                                          if (residx == 65535) {
                                             res = null;
                                             sdtx = null;
                                          } else {
                                             if ((residx & 32768) != 0) {
                                                residx &= -32769;
                                                sdtx = msg.derive(0, msg.uint8());
                                             } else {
                                                sdtx = new Message(0);
                                             }

                                             res = Session.this.getres(residx);
                                          }

                                          if (gob != null) {
                                             oc.overlay(gob, olid, prs, res, sdtx);
                                          }
                                       } else if (type == 14) {
                                          int hp = msg.uint8();
                                          if (gob != null) {
                                             oc.health(gob, hp);
                                          }
                                       } else if (type != 15) {
                                          if (type != 19) {
                                             if (type != 255) {
                                                throw Session.this.new MessageException("Unknown objdelta type: " + type, msg);
                                             }
                                             break;
                                          }

                                          int residxx = msg.uint16();
                                          Indir<Resource> resx;
                                          if (residxx == 65535) {
                                             resx = null;
                                          } else {
                                             resx = Session.this.getres(residxx);
                                             int var67 = msg.uint8();
                                          }

                                          if (gob != null) {
                                             oc.icon(gob, resx);
                                          }
                                       } else {
                                          String name = msg.string();
                                          if (name.length() > 0) {
                                             int group = msg.uint8();
                                             int btype = msg.uint8();
                                             if (gob != null) {
                                                oc.buddy(gob, name, group, btype);
                                             }
                                          } else if (gob != null) {
                                             oc.buddy(gob, null, 0, 0);
                                          }
                                       }
                                    } else {
                                       List<Indir<Resource>> layers = new LinkedList<>();

                                       while (true) {
                                          int layer = msg.uint16();
                                          if (layer == 65535) {
                                             if (gob != null) {
                                                oc.avatar(gob, layers);
                                             }
                                             break;
                                          }

                                          layers.add(Session.this.getres(layer));
                                       }
                                    }
                                 } else {
                                    Coord off = msg.coord();
                                    int sz = msg.uint16();
                                    int str = msg.uint8();
                                    if (gob != null) {
                                       oc.lumin(gob, off, sz, str);
                                    }
                                 }
                              } else {
                                 Coord off = msg.coord();
                                 if (gob != null) {
                                    oc.drawoff(gob, off);
                                 }
                              }
                           } else {
                              List<Composited.ED> equ = new LinkedList<>();

                              while (true) {
                                 int h = msg.uint8();
                                 if (h == 255) {
                                    if (gob != null) {
                                       oc.cmpequ(gob, equ);
                                    }
                                    break;
                                 }

                                 int ef = h & 128;
                                 int et = h & 127;
                                 String at = msg.string();
                                 int residxxx = msg.uint16();
                                 Indir<Resource> resxx;
                                 if (residxxx == 65535) {
                                    resxx = null;
                                 } else {
                                    resxx = Session.this.getres(residxxx);
                                 }

                                 Coord3f off;
                                 if ((ef & 128) != 0) {
                                    int x = msg.int16();
                                    int y = msg.int16();
                                    int z = msg.int16();
                                    off = new Coord3f(x / 1000.0F, y / 1000.0F, z / 1000.0F);
                                 } else {
                                    off = Coord3f.o;
                                 }

                                 equ.add(new Composited.ED(et, at, resxx, off));
                              }
                           }
                        } else {
                           List<Composited.MD> mod = new LinkedList<>();

                           while (true) {
                              int modid = msg.uint16();
                              if (modid == 65535) {
                                 if (gob != null) {
                                    oc.cmpmod(gob, mod);
                                 }
                                 break;
                              }

                              Indir<Resource> modr = Session.this.getres(modid);
                              List<Indir<Resource>> tex = new LinkedList<>();

                              while (true) {
                                 int residxxxx = msg.uint16();
                                 if (residxxxx == 65535) {
                                    mod.add(new Composited.MD(modr, tex));
                                    break;
                                 }

                                 tex.add(Session.this.getres(residxxxx));
                              }
                           }
                        }
                     } else {
                        List<ResData> poses = null;
                        List<ResData> tposes = null;
                        int pfl = msg.uint8();
                        int seq = msg.uint8();
                        boolean interp = (pfl & 1) != 0;
                        if ((pfl & 2) != 0) {
                           poses = new LinkedList<>();

                           while (true) {
                              int residxxxx = msg.uint16();
                              if (residxxxx == 65535) {
                                 break;
                              }

                              Message sdtxx = Message.nil;
                              if ((residxxxx & 32768) != 0) {
                                 residxxxx &= -32769;
                                 sdtxx = msg.derive(0, msg.uint8());
                              }

                              poses.add(new ResData(Session.this.getres(residxxxx), sdtxx));
                           }
                        }

                        float ttime = 0.0F;
                        if ((pfl & 4) != 0) {
                           tposes = new LinkedList<>();

                           while (true) {
                              int residxxxxx = msg.uint16();
                              if (residxxxxx == 65535) {
                                 ttime = msg.uint8() / 10.0F;
                                 break;
                              }

                              Message sdtxx = Message.nil;
                              if ((residxxxxx & 32768) != 0) {
                                 residxxxxx &= -32769;
                                 sdtxx = msg.derive(0, msg.uint8());
                              }

                              tposes.add(new ResData(Session.this.getres(residxxxxx), sdtxx));
                           }
                        }

                        if (gob != null) {
                           oc.cmppose(gob, seq, poses, tposes, interp, ttime);
                        }
                     }
                  } else {
                     Indir<Resource> base = Session.this.getres(msg.uint16());
                     if (gob != null) {
                        oc.composite(gob, base);
                     }
                  }
               }
            }

            synchronized (Session.this.objacks) {
               if (Session.this.objacks.containsKey(id)) {
                  Session.ObjAck a = Session.this.objacks.get(id);
                  a.frame = frame;
                  a.recv = System.currentTimeMillis();
               } else {
                  Session.this.objacks.put(id, Session.this.new ObjAck(id, frame, System.currentTimeMillis()));
               }
            }
         }

         synchronized (Session.this.sworker) {
            Session.this.sworker.notifyAll();
         }
      }

      protected void handlerel(Message msg) {
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
            Session.this.cachedres(resid).set(resname, resver);
         } else if (msg.type == 7) {
            Session.this.glob.party.msg(msg);
         } else if (msg.type == 8) {
            Indir<Resource> res = Session.this.getres(msg.uint16());
            double vol = msg.uint16() / 256.0;
            double spd = msg.uint16() / 256.0;
            Audio.play(res);
         } else if (msg.type == 9) {
            Session.this.glob.cattr(msg);
         } else if (msg.type == 10) {
            String resnm = msg.string();
            int resver = msg.uint16();
            boolean loop = !msg.eom() && msg.uint8() != 0;
            if (resnm.equals("")) {
               Music.play(null, false);
            } else {
               Music.play(Resource.load(resnm, resver), loop);
            }
         } else if (msg.type == 11) {
            Session.this.glob.map.tilemap(msg);
         } else if (msg.type == 12) {
            Session.this.glob.buffmsg(msg);
         } else {
            if (msg.type != 13) {
               throw Session.this.new MessageException("Unknown rmsg type: " + msg.type, msg);
            }

            Session.this.sesskey = msg.bytes();
         }
      }

      private void getrel(int seq, Message msg) {
         if (seq == Session.this.rseq) {
            int lastack;
            synchronized (Session.this.uimsgs) {
               this.handlerel(msg);

               while (true) {
                  lastack = Session.this.rseq;
                  Session.this.rseq = (Session.this.rseq + 1) % 65536;
                  if (!Session.this.waiting.containsKey(Session.this.rseq)) {
                     break;
                  }

                  this.handlerel(Session.this.waiting.get(Session.this.rseq));
                  Session.this.waiting.remove(Session.this.rseq);
               }
            }

            Session.this.sendack(lastack);
            synchronized (Session.this) {
               Session.this.notifyAll();
            }
         } else if (Utils.floormod(seq - Session.this.rseq, 65536) < 32768) {
            Session.this.waiting.put(seq, msg);
         }
      }

      @Override
      public void run() {
         try {
            this.alive = true;

            try {
               Session.this.sk.setSoTimeout(1000);
            } catch (SocketException var25) {
               throw new RuntimeException(var25);
            }

            while (this.alive) {
               DatagramPacket p = new DatagramPacket(new byte[65536], 65536);

               try {
                  Session.this.sk.receive(p);
               } catch (ClosedByInterruptException var26) {
                  break;
               } catch (SocketTimeoutException var27) {
                  continue;
               } catch (IOException var28) {
                  throw new RuntimeException(var28);
               }

               if (p.getSocketAddress().equals(Session.this.server)) {
                  Message msg = new Message(p.getData()[0], p.getData(), 1, p.getLength() - 1);
                  if (msg.type == 0 && Session.this.state == "conn") {
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
                     if (msg.type != 0) {
                        if (msg.type == 1) {
                           for (int seq = msg.uint16(); !msg.eom(); seq++) {
                              int type = msg.uint8();
                              int len;
                              if ((type & 128) != 0) {
                                 type &= 127;
                                 len = msg.uint16();
                              } else {
                                 len = msg.blob.length - msg.off;
                              }

                              this.getrel(seq, new Message(type, msg.blob, msg.off, len));
                              msg.off += len;
                           }
                        } else if (msg.type == 2) {
                           this.gotack(msg.uint16());
                        } else if (msg.type == 5) {
                           Session.this.glob.map.mapdata(msg);
                        } else if (msg.type == 6) {
                           this.getobjdata(msg);
                        } else {
                           if (msg.type != 8) {
                              throw Session.this.new MessageException("Unknown message type: " + msg.type, msg);
                           }

                           synchronized (Session.this) {
                              Session.this.state = "fin";
                              Session.this.notifyAll();
                           }

                           Session.this.close();
                        }
                     }

                     if (Config.autolog && UI.instance.timesinceactive() > 600000L) {
                        try {
                           UI.instance.be_active();
                           UI.instance.cons.run("act lo cs");
                        } catch (Exception var22) {
                        }
                     }
                  }
               }
            }
         } finally {
            synchronized (Session.this) {
               Session.this.state = "dead";
               Session.this.notifyAll();
            }
         }
      }

      @Override
      public void interrupt() {
         this.alive = false;
         super.interrupt();
      }
   }

   private class SWorker extends HackThread {
      public SWorker() {
         super("Session writer");
         this.setDaemon(true);
      }

      @Override
      public void run() {
         try {
            long last = 0L;
            long retries = 0L;

            while (true) {
               long now = System.currentTimeMillis();
               if (Session.this.state == "conn") {
                  if (now - last > 2000L) {
                     if (++retries > 5L) {
                        synchronized (Session.this) {
                           Session.this.connfailed = 3;
                           Session.this.notifyAll();
                           return;
                        }
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
               } else {
                  long to = 5000L;
                  synchronized (Session.this.pending) {
                     if (Session.this.pending.size() > 0) {
                        to = 60L;
                     }
                  }

                  synchronized (Session.this.objacks) {
                     if (Session.this.objacks.size() > 0 && to > 120L) {
                        to = 200L;
                     }
                  }

                  synchronized (this) {
                     if (Session.this.acktime > 0L) {
                        to = Session.this.acktime + 30L - now;
                     }

                     if (to > 0L) {
                        this.wait(to);
                     }
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
                     Iterator<Session.ObjAck> i = Session.this.objacks.values().iterator();

                     while (i.hasNext()) {
                        Session.ObjAck a = i.next();
                        boolean send = false;
                        boolean del = false;
                        if (now - a.sent > 200L) {
                           send = true;
                        }

                        if (now - a.recv > 120L) {
                           del = true;
                           send = true;
                        }

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

                        if (del) {
                           i.remove();
                        }
                     }

                     if (msg != null) {
                        Session.this.sendmsg(msg);
                        beat = false;
                     }
                  }

                  synchronized (this) {
                     if (Session.this.acktime > 0L && now - Session.this.acktime >= 30L) {
                        byte[] msg = new byte[]{2, 0, 0};
                        Utils.uint16e(Session.this.ackseq, msg, 1);
                        Session.this.sendmsg(msg);
                        Session.this.acktime = -1L;
                        beat = false;
                     }
                  }

                  if (beat && now - last > 5000L) {
                     Session.this.sendmsg(new byte[]{3});
                     last = now;
                  }
               }
            }
         } catch (InterruptedException var40) {
            for (int i = 0; i < 5; i++) {
               Session.this.sendmsg(new Message(8));
               long f = System.currentTimeMillis();

               while (true) {
                  synchronized (Session.this) {
                     if (Session.this.state != "conn" && Session.this.state != "fin" && Session.this.state != "dead") {
                        Session.this.state = "close";
                        long now = System.currentTimeMillis();
                        if (now - f <= 500L) {
                           try {
                              Session.this.wait(500L - (now - f));
                           } catch (InterruptedException var31) {
                           }
                           continue;
                        }
                     }
                     break;
                  }
               }
            }
         } finally {
            Session.this.ticker.interrupt();
            Session.this.rworker.interrupt();
         }
      }
   }

   private class Ticker extends HackThread {
      public Ticker() {
         super("Server time ticker");
         this.setDaemon(true);
      }

      @Override
      public void run() {
         try {
            while (true) {
               long then = System.currentTimeMillis();
               Session.this.glob.oc.tick();
               long now = System.currentTimeMillis();
               if (now - then < 70L) {
                  Thread.sleep(70L - (now - then));
               }
            }
         } catch (InterruptedException var5) {
         }
      }
   }
}
