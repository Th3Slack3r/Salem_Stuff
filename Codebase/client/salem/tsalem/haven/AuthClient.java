package haven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class AuthClient {
  private static final SslHelper ssl = new SslHelper();
  
  private final Socket sk;
  
  private final InputStream skin;
  
  private final OutputStream skout;
  
  static {
    try {
      ssl.trust(Resource.class.getResourceAsStream("authsrv.crt"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public AuthClient(String host, int port) throws IOException {
    boolean fin = false;
    SSLSocket sk = ssl.connect(host, port);
    try {
      if (Config.authcertstrict)
        checkname(host, sk.getSession()); 
      this.sk = sk;
      this.skin = sk.getInputStream();
      this.skout = sk.getOutputStream();
      fin = true;
    } finally {
      if (!fin)
        sk.close(); 
    } 
  }
  
  private void checkname(String host, SSLSession sess) throws IOException {
    Certificate peer = sess.getPeerCertificates()[0];
    String dns = null;
    InetAddress ip = null;
    try {
      ip = Utils.inet_pton(host);
    } catch (IllegalArgumentException e) {
      dns = host;
    } 
    if (peer instanceof X509Certificate) {
      X509Certificate xc = (X509Certificate)peer;
      try {
        Collection<List<?>> altnames = xc.getSubjectAlternativeNames();
        if (altnames == null)
          throw new SSLException("Unnamed authentication server certificate"); 
        for (List<?> name : altnames) {
          int type = ((Number)name.get(0)).intValue();
          if (type == 2 && dns != null) {
            if (Utils.eq(name.get(1), dns))
              return; 
            continue;
          } 
          if (type == 7 && ip != null)
            try {
              if (Utils.eq(Utils.inet_pton((String)name.get(1)), ip))
                return; 
            } catch (IllegalArgumentException illegalArgumentException) {} 
        } 
      } catch (CertificateException e) {
        throw new SSLException("Illegal authentication server certificate", e);
      } 
      throw new SSLException("Authentication server name mismatch");
    } 
    throw new SSLException("Unknown certificate type, cannot validate: " + peer.getClass().getName());
  }
  
  private static byte[] digest(byte[] pw) {
    MessageDigest dig;
    try {
      dig = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } 
    dig.update(pw);
    return dig.digest();
  }
  
  public String trypasswd(String user, byte[] phash) throws IOException {
    Message rpl = cmd(new Object[] { "pw", user, phash });
    String stat = rpl.string();
    if (stat.equals("ok")) {
      String acct = rpl.string();
      return acct;
    } 
    if (stat.equals("no"))
      return null; 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
  
  public String trytoken(String user, byte[] token) throws IOException {
    Message rpl = cmd(new Object[] { "token", user, token });
    String stat = rpl.string();
    if (stat.equals("ok")) {
      String acct = rpl.string();
      return acct;
    } 
    if (stat.equals("no"))
      return null; 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
  
  public byte[] getcookie() throws IOException {
    Message rpl = cmd(new Object[] { "cookie" });
    String stat = rpl.string();
    if (stat.equals("ok"))
      return rpl.bytes(32); 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
  
  public byte[] gettoken() throws IOException {
    Message rpl = cmd(new Object[] { "mktoken" });
    String stat = rpl.string();
    if (stat.equals("ok"))
      return rpl.bytes(32); 
    throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
  }
  
  public void close() throws IOException {
    this.sk.close();
  }
  
  private void sendmsg(Message msg) throws IOException {
    if (msg.blob.length > 65535)
      throw new RuntimeException("Too long message in AuthClient (" + msg.blob.length + " bytes)"); 
    byte[] buf = new byte[msg.blob.length + 2];
    buf[0] = (byte)((msg.blob.length & 0xFF00) >> 8);
    buf[1] = (byte)(msg.blob.length & 0xFF);
    System.arraycopy(msg.blob, 0, buf, 2, msg.blob.length);
    this.skout.write(buf);
  }
  
  private void esendmsg(Object... args) throws IOException {
    Message buf = new Message(0);
    for (Object arg : args) {
      if (arg instanceof String) {
        buf.addstring((String)arg);
      } else if (arg instanceof byte[]) {
        buf.addbytes((byte[])arg);
      } else {
        throw new RuntimeException("Illegal argument to esendmsg: " + arg.getClass());
      } 
    } 
    sendmsg(buf);
  }
  
  private static void readall(InputStream in, byte[] buf) throws IOException {
    for (int i = 0; i < buf.length; i += rv) {
      int rv = in.read(buf, i, buf.length - i);
      if (rv < 0)
        throw new IOException("Premature end of input"); 
    } 
  }
  
  private Message recvmsg() throws IOException {
    byte[] header = new byte[2];
    readall(this.skin, header);
    int len = Utils.ub(header[0]) << 8 | Utils.ub(header[1]);
    byte[] buf = new byte[len];
    readall(this.skin, buf);
    return new Message(0, buf);
  }
  
  public Message cmd(Object... args) throws IOException {
    esendmsg(args);
    return recvmsg();
  }
  
  public static abstract class Credentials implements Serializable {
    public abstract String tryauth(AuthClient param1AuthClient) throws IOException;
    
    public abstract String name();
    
    public void discard() {}
    
    protected void finalize() {
      discard();
    }
    
    public static class AuthException extends RuntimeException {
      public AuthException(String msg) {
        super(msg);
      }
    }
  }
  
  public static class NativeCred extends Credentials {
    public final String username;
    
    private byte[] phash;
    
    public NativeCred(String username, byte[] phash) {
      this.username = username;
      if ((this.phash = phash).length != 32)
        throw new IllegalArgumentException("Password hash must be 32 bytes"); 
    }
    
    private static byte[] ohdearjava(String a) {
      try {
        return AuthClient.digest(a.getBytes("utf-8"));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      } 
    }
    
    public NativeCred(String username, String pw) {
      this(username, ohdearjava(pw));
    }
    
    public String name() {
      return this.username;
    }
    
    public String tryauth(AuthClient cl) throws IOException {
      Message rpl = cl.cmd(new Object[] { "pw", this.username, this.phash });
      String stat = rpl.string();
      if (stat.equals("ok")) {
        String acct = rpl.string();
        return acct;
      } 
      if (stat.equals("no")) {
        String err = rpl.string();
        throw new AuthClient.Credentials.AuthException(err);
      } 
      throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
    }
    
    public void discard() {
      if (this.phash != null) {
        for (int i = 0; i < this.phash.length; i++)
          this.phash[i] = 0; 
        this.phash = null;
      } 
    }
  }
  
  public static class TokenCred extends Credentials {
    public final String acctname;
    
    public final byte[] token;
    
    public TokenCred(String acctname, byte[] token) {
      this.acctname = acctname;
      if ((this.token = token).length != 32)
        throw new IllegalArgumentException("Token must be 32 bytes"); 
    }
    
    public String name() {
      return this.acctname;
    }
    
    public String tryauth(AuthClient cl) throws IOException {
      Message rpl = cl.cmd(new Object[] { "token", this.acctname, this.token });
      String stat = rpl.string();
      if (stat.equals("ok")) {
        String acct = rpl.string();
        return acct;
      } 
      if (stat.equals("no")) {
        String err = rpl.string();
        throw new AuthClient.Credentials.AuthException(err);
      } 
      throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
    }
  }
  
  public static void main(final String[] args) throws Exception {
    Thread t = new HackThread(new Runnable() {
          public void run() {
            try {
              AuthClient test = new AuthClient("127.0.0.1", 1871);
              try {
                String acct = (new AuthClient.NativeCred(args[0], args[1])).tryauth(test);
                if (acct == null) {
                  System.err.println("failed");
                  return;
                } 
                System.out.println(acct);
                System.out.println(Utils.byte2hex(test.getcookie()));
              } finally {
                test.close();
              } 
            } catch (Exception e) {
              throw new RuntimeException(e);
            } 
          }
        }"Test");
    t.start();
    t.join();
  }
}
