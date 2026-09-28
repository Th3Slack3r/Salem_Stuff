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

public class ScriptDebug {
  private final ScriptEngine eng;
  
  public ScriptDebug(ScriptEngine eng) {
    this.eng = eng;
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
        Object ret;
        out.write("% ");
        out.flush();
        String ln = lin.readLine();
        if (ln == null)
          break; 
        try {
          ret = ScriptDebug.this.eng.eval(ln);
        } catch (Throwable e) {
          if (!(e instanceof javax.script.ScriptException) || e.getCause() == null) {
            e.printStackTrace(new PrintWriter(out));
            continue;
          } 
          out.write(e.getCause().toString() + "\r\n");
          continue;
        } 
        if (ret != null)
          out.write(ret.toString() + "\r\n"); 
      } 
    }
    
    public void run() {
      try {
        Reader in;
        Writer out;
        try {
          out = new OutputStreamWriter(this.sk.getOutputStream(), "utf-8");
          in = new InputStreamReader(this.sk.getInputStream(), "utf-8");
        } catch (IOException e) {
          throw new RuntimeException(e);
        } 
        try {
          run2(in, out);
        } catch (IOException e) {
          return;
        } 
      } finally {
        try {
          this.sk.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
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
    
    public void run() {
      try {
        do {
          ScriptDebug.Client cl;
          try {
            cl = new ScriptDebug.Client(this.sk.accept());
          } catch (IOException e) {
            break;
          } 
          cl.setDaemon(true);
          cl.start();
        } while (!this.one);
      } finally {
        try {
          this.sk.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        } 
      } 
    }
  }
  
  public static Server start(String type, int port, boolean one) throws IOException {
    ScriptEngine eng = (new ScriptEngineManager()).getEngineByName(type);
    if (eng == null)
      throw new RuntimeException("No such script engine installed: " + type); 
    ScriptDebug db = new ScriptDebug(eng);
    db.getClass();
    Server srv = new Server(port, one);
    srv.setDaemon(true);
    srv.start();
    return srv;
  }
  
  public static Client connect(String type, String host, int port) throws IOException {
    ScriptEngine eng = (new ScriptEngineManager()).getEngineByName(type);
    if (eng == null)
      throw new RuntimeException("No such script engine installed: " + type); 
    ScriptDebug db = new ScriptDebug(eng);
    HackSocket hackSocket = new HackSocket();
    try {
      hackSocket.connect(new InetSocketAddress(host, port));
      db.getClass();
      Client cl = new Client((Socket)hackSocket);
      cl.setDaemon(true);
      cl.start();
      hackSocket = null;
      return cl;
    } finally {
      if (hackSocket != null)
        hackSocket.close(); 
    } 
  }
}
