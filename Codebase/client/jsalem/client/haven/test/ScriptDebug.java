package haven.test;

import haven.HackSocket;
import haven.HackThread;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptDebug {
   private final ScriptEngine eng;

   public ScriptDebug(ScriptEngine eng) {
      this.eng = eng;
   }

   public static ScriptDebug.Server start(String type, int port, boolean one) throws IOException {
      ScriptEngine eng = new ScriptEngineManager().getEngineByName(type);
      if (eng == null) {
         throw new RuntimeException("No such script engine installed: " + type);
      } else {
         ScriptDebug db = new ScriptDebug(eng);
         ScriptDebug.Server srv = db.new Server(port, one);
         srv.setDaemon(true);
         srv.start();
         return srv;
      }
   }

   public static ScriptDebug.Client connect(String type, String host, int port) throws IOException {
      ScriptEngine eng = new ScriptEngineManager().getEngineByName(type);
      if (eng == null) {
         throw new RuntimeException("No such script engine installed: " + type);
      } else {
         ScriptDebug db = new ScriptDebug(eng);
         Socket sk = new HackSocket();

         ScriptDebug.Client var7;
         try {
            sk.connect(new InetSocketAddress(host, port));
            ScriptDebug.Client cl = db.new Client(sk);
            cl.setDaemon(true);
            cl.start();
            sk = null;
            var7 = cl;
         } finally {
            if (sk != null) {
               sk.close();
            }
         }

         return var7;
      }
   }

   public class Client extends HackThread {
      private final Socket sk;

      public Client(Socket sk) {
         super("Debug client");
         this.sk = sk;
      }

      private void run2(Reader in, Writer out) throws IOException {
         BufferedReader lin = new BufferedReader(in);

         while (true) {
            out.write("% ");
            out.flush();
            String ln = lin.readLine();
            if (ln == null) {
               return;
            }

            Object ret;
            try {
               ret = ScriptDebug.this.eng.eval(ln);
            } catch (Throwable var7) {
               if (var7 instanceof ScriptException && var7.getCause() != null) {
                  out.write(var7.getCause().toString() + "\r\n");
                  continue;
               }

               var7.printStackTrace(new PrintWriter(out));
               continue;
            }

            if (ret != null) {
               out.write(ret.toString() + "\r\n");
            }
         }
      }

      @Override
      public void run() {
         try {
            Reader in;
            Writer out;
            try {
               out = new OutputStreamWriter(this.sk.getOutputStream(), "utf-8");
               in = new InputStreamReader(this.sk.getInputStream(), "utf-8");
            } catch (IOException var14) {
               throw new RuntimeException(var14);
            }

            try {
               this.run2(in, out);
               return;
            } catch (IOException var15) {
            }
         } finally {
            try {
               this.sk.close();
            } catch (IOException var13) {
               throw new RuntimeException(var13);
            }
         }
      }
   }

   public class Server extends HackThread {
      private final ServerSocket sk;
      private final boolean one;

      public Server(int port, boolean one) throws IOException {
         super("Debug server");
         this.sk = new ServerSocket(port);
         this.one = one;
      }

      @Override
      public void run() {
         try {
            do {
               ScriptDebug.Client cl;
               try {
                  cl = ScriptDebug.this.new Client(this.sk.accept());
               } catch (IOException var10) {
                  break;
               }

               cl.setDaemon(true);
               cl.start();
            } while (!this.one);
         } finally {
            try {
               this.sk.close();
            } catch (IOException var9) {
               throw new RuntimeException(var9);
            }
         }
      }
   }
}
