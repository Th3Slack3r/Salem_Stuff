package haven.rs;

import haven.Message;
import haven.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server extends Thread {
   public static final Map<String, Server.Command> commands = new HashMap<>();
   private final ServerSocket sk;
   private final Random rng;
   private final byte[] key;

   public Server(int port, byte[] key) throws IOException {
      super("Render server");

      try {
         this.rng = SecureRandom.getInstance("SHA1PRNG");
      } catch (NoSuchAlgorithmException var4) {
         throw new Error(var4);
      }

      this.key = key;
      this.sk = new ServerSocket(port);
      this.start();
   }

   @Override
   public void run() {
      try {
         while (true) {
            Socket nsk;
            try {
               nsk = this.sk.accept();
            } catch (IOException var10) {
               return;
            }

            new Server.Client(nsk);
         }
      } finally {
         try {
            this.sk.close();
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }
      }
   }

   public static void main(String[] args) throws Exception {
      new Server(Integer.parseInt(args[0]), Utils.base64dec(System.getenv("AUTHKEY")));
   }

   static {
      commands.put("ava", AvaRender.call);
   }

   public class Client extends Thread {
      private final Socket sk;
      private boolean auth = false;
      private final byte[] nonce = new byte[32];
      private final byte[] ckey;

      private Client(Socket sk) {
         super("Render server handler");
         Server.this.rng.nextBytes(this.nonce);

         MessageDigest dig;
         try {
            dig = MessageDigest.getInstance("SHA-256");
         } catch (NoSuchAlgorithmException var5) {
            throw new Error(var5);
         }

         dig.update(Server.this.key);
         dig.update(this.nonce);
         this.ckey = dig.digest();
         this.sk = sk;
         this.setDaemon(true);
         this.start();
      }

      byte[] read(InputStream in, int bytes) throws IOException {
         byte[] ret = new byte[bytes];
         int n = 0;

         while (n < bytes) {
            int rv = in.read(ret, n, bytes - n);
            if (rv < 0) {
               throw new IOException("Unexpected end-of-file");
            }

            n += rv;
         }

         return ret;
      }

      @Override
      public void run() {
         try {
            InputStream in;
            OutputStream out;
            try {
               in = this.sk.getInputStream();
               out = this.sk.getOutputStream();
            } catch (IOException var22) {
               throw new RuntimeException(var22);
            }

            while (true) {
               try {
                  int len = Utils.int32d(this.read(in, 4), 0);
                  if (!this.auth && len > 256) {
                     return;
                  }

                  Message msg = new Message(0, this.read(in, len));
                  String cmd = msg.string();
                  Object[] args = msg.list();
                  Object[] reply;
                  if (this.auth) {
                     Server.Command cc = Server.commands.get(cmd);
                     if (cc != null) {
                        reply = cc.run(this, args);
                     } else {
                        reply = new Object[]{"nocmd"};
                     }
                  } else if (cmd.equals("nonce")) {
                     reply = new Object[]{this.nonce};
                  } else {
                     if (!cmd.equals("auth")) {
                        return;
                     }

                     if (Arrays.equals((byte[])args[0], this.ckey)) {
                        reply = new Object[]{"ok"};
                        this.auth = true;
                     } else {
                        reply = new Object[]{"no"};
                     }
                  }

                  Message rb = new Message(0);
                  rb.addlist(reply);
                  byte[] rbuf = new byte[4 + rb.blob.length];
                  Utils.uint32e(rb.blob.length, rbuf, 0);
                  System.arraycopy(rb.blob, 0, rbuf, 4, rb.blob.length);
                  out.write(rbuf);
               } catch (IOException var23) {
                  return;
               }
            }
         } catch (InterruptedException var24) {
         } finally {
            try {
               this.sk.close();
            } catch (IOException var21) {
               throw new RuntimeException(var21);
            }
         }
      }
   }

   public interface Command {
      Object[] run(Server.Client var1, Object... var2) throws InterruptedException;
   }
}
