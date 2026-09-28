package haven.rs;

import haven.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server extends Thread {
  public static final Map<String, Command> commands = new HashMap<>();
  
  private final ServerSocket sk;
  
  private final Random rng;
  
  private final byte[] key;
  
  static {
    commands.put("ava", AvaRender.call);
  }
  
  public class Client extends Thread {
    private final Socket sk;
    
    private boolean auth;
    
    private final byte[] nonce;
    
    private final byte[] ckey;
    
    private Client(Socket sk) {
      super("Render server handler");
      MessageDigest dig;
      this.auth = false;
      this.nonce = new byte[32];
      Server.this.rng.nextBytes(this.nonce);
      try {
        dig = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
        throw new Error(e);
      } 
      dig.update(Server.this.key);
      dig.update(this.nonce);
      this.ckey = dig.digest();
      this.sk = sk;
      setDaemon(true);
      start();
    }
    
    byte[] read(InputStream in, int bytes) throws IOException {
      byte[] ret = new byte[bytes];
      int n = 0;
      while (n < bytes) {
        int rv = in.read(ret, n, bytes - n);
        if (rv < 0)
          throw new IOException("Unexpected end-of-file"); 
        n += rv;
      } 
      return ret;
    }
    
    public void run() {
      try {
        InputStream in;
        OutputStream out;
        try {
          in = this.sk.getInputStream();
          out = this.sk.getOutputStream();
        } catch (IOException e) {
          throw new RuntimeException(e);
        } 
      } catch (InterruptedException in) {
        try {
          this.sk.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
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
  
  public Server(int port, byte[] key) throws IOException {
    super("Render server");
    try {
      this.rng = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    } 
    this.key = key;
    this.sk = new ServerSocket(port);
    start();
  }
  
  public void run() {
    try {
      while (true) {
        Socket nsk;
        try {
          nsk = this.sk.accept();
        } catch (IOException e) {
          break;
        } 
        new Client(nsk);
      } 
    } finally {
      try {
        this.sk.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
    } 
  }
  
  public static void main(String[] args) throws Exception {
    new Server(Integer.parseInt(args[0]), Utils.base64dec(System.getenv("AUTHKEY")));
  }
  
  public static interface Command {
    Object[] run(Server.Client param1Client, Object... param1VarArgs) throws InterruptedException;
  }
}
