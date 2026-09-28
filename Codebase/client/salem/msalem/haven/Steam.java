package haven;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLibraryLoader;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.jogamp.common.jvm.JNILibLoaderBase;
import com.jogamp.common.os.Platform;
import com.jogamp.common.os.Platform.OSType;
import com.jogamp.common.util.cache.TempJarCache;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Steam {
   private final Collection<Steam.Listener> listening = new HashSet<>();
   private static boolean loaded = false;
   private static boolean inited = false;
   private static Steam instance = null;
   private final SteamFriends friends = new SteamFriends(new SteamFriendsCallback() {});
   private final SteamUser user = new SteamUser(new SteamUserCallback() {
      public void onGetTicketForWebApi(SteamAuthTicket tkt, SteamResult result, byte[] data) {
         Steam.this.post("onGetTicketForWebApi", tkt, result, data);
      }

      public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {
         Steam.this.post("onMicroTxnAuthorization", appID, orderID, authorized);
      }
   });

   public void add(Steam.Listener l) {
      synchronized (this.listening) {
         this.listening.add(l);
      }
   }

   public void remove(Steam.Listener l) {
      synchronized (this.listening) {
         this.listening.remove(l);
      }
   }

   private void post(String cbid, Object... args) {
      synchronized (this.listening) {
         for (Steam.Listener l : this.listening) {
            l.callback(cbid, args);
         }
      }
   }

   private static boolean init() {
      if (!loaded) {
         if (!SteamAPI.loadLibraries(new Steam.SteamLibraryLoaderJogl())) {
            return false;
         }

         loaded = true;
      }

      if (!inited) {
         try {
            if (!SteamAPI.init()) {
               return false;
            }

            inited = true;
         } catch (SteamException var1) {
            return false;
         }
      }

      return true;
   }

   private Steam() {
      Thread th = new HackThread(new Runnable() {
         @Override
         public void run() {
            Steam.this.listen();
         }
      }, "Steam callback thread");
      th.setDaemon(true);
      th.start();
   }

   private void listen() {
      try {
         while (true) {
            SteamAPI.runCallbacks();
            Thread.sleep(100L);
         }
      } catch (InterruptedException var2) {
      }
   }

   public static synchronized Steam get() {
      if (instance == null) {
         if (!init()) {
            return null;
         }

         instance = new Steam();
      }

      return instance;
   }

   public synchronized int userid() {
      SteamID id = this.user.getSteamID();
      return !id.isValid() ? -1 : id.getAccountID();
   }

   public synchronized String displayname() {
      return this.friends.getPersonaName();
   }

   public Steam.WebTicket webticket() throws InterruptedException, SteamException {
      Steam.Waiter w = new Steam.Waiter("onGetTicketForWebApi");

      Steam.WebTicket var4;
      try {
         SteamAuthTicket tkt;
         synchronized (this) {
            tkt = this.user.getAuthTicketForWebApi();
         }

         Object[] cb;
         do {
            cb = w.get();
         } while (!tkt.equals(cb[0]));

         if (cb[1] != SteamResult.OK) {
            throw new SteamException("GetAuthTicketForWebApi failed: " + cb[1]);
         }

         var4 = new Steam.WebTicket(tkt, (byte[])cb[2]);
      } catch (Throwable var7) {
         try {
            w.close();
         } catch (Throwable var5) {
            var7.addSuppressed(var5);
         }

         throw var7;
      }

      w.close();
      return var4;
   }

   public static void main(String[] args) throws Exception {
      Steam s = get();
      System.out.printf("%x `%s'\n", s.userid(), s.displayname());
      Steam.WebTicket tkt = s.webticket();
      System.out.println(Utils.byte2hex(tkt.data));
      tkt.cancel();
   }

   public interface Listener {
      void callback(String var1, Object[] var2);
   }

   private static class SteamLibraryLoaderJogl extends JNILibLoaderBase implements SteamLibraryLoader {
      private SteamLibraryLoaderJogl() {
      }

      public boolean loadLibrary(String nm) {
         if (Platform.getOSType() == OSType.WINDOWS && Platform.is64Bit()) {
            nm = nm + "64";
         }

         TempJarCache.initSingleton();
         addNativeJarLibs(new Class[]{SteamAPI.class}, null, null);
         return loadLibrary(nm, false, SteamAPI.class.getClassLoader());
      }
   }

   public class Waiter implements Steam.Listener, AutoCloseable {
      private final String id;
      private final Queue<Object[]> got = new LinkedList<>();

      public Waiter(String id) {
         this.id = id;
         Steam.this.add(this);
      }

      @Override
      public void callback(String id, Object[] args) {
         if (id == this.id) {
            synchronized (this) {
               this.got.add(args);
               this.notifyAll();
            }
         }
      }

      public Object[] get() throws InterruptedException {
         synchronized (this) {
            while (true) {
               Object[] ret = this.got.poll();
               if (ret != null) {
                  return ret;
               }

               this.wait();
            }
         }
      }

      @Override
      public void close() {
         Steam.this.remove(this);
      }
   }

   public class WebTicket {
      public final byte[] data;
      private final SteamAuthTicket handle;
      private boolean cancelled = false;

      private WebTicket(SteamAuthTicket handle, byte[] data) {
         this.handle = handle;
         this.data = data;
      }

      public void cancel() {
         synchronized (Steam.this) {
            if (!this.cancelled) {
               Steam.this.user.cancelAuthTicket(this.handle);
               this.cancelled = true;
            }
         }
      }
   }
}
