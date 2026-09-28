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
   private Socket sk;
   private InputStream skin;
   private OutputStream skout;

   public AuthClient(String host, int port) throws IOException {
      boolean fin = false;
      SSLSocket sk = ssl.connect(host, port);

      try {
         if (Config.authcertstrict) {
            this.checkname(host, sk.getSession());
         }

         this.sk = sk;
         this.skin = sk.getInputStream();
         this.skout = sk.getOutputStream();
         fin = true;
      } finally {
         if (!fin) {
            sk.close();
         }
      }
   }

   private void checkname(String host, SSLSession sess) throws IOException {
      Certificate peer = sess.getPeerCertificates()[0];
      String dns = null;
      InetAddress ip = null;

      try {
         ip = Utils.inet_pton(host);
      } catch (IllegalArgumentException var13) {
         dns = host;
      }

      if (peer instanceof X509Certificate) {
         X509Certificate xc = (X509Certificate)peer;

         try {
            Collection<List<?>> altnames = xc.getSubjectAlternativeNames();
            if (altnames == null) {
               throw new SSLException("Unnamed authentication server certificate");
            }

            for (List<?> name : altnames) {
               int type = ((Number)name.get(0)).intValue();
               if (type == 2 && dns != null) {
                  if (Utils.eq(name.get(1), dns)) {
                     return;
                  }
               } else if (type == 7 && ip != null) {
                  try {
                     if (Utils.eq(Utils.inet_pton((String)name.get(1)), ip)) {
                        return;
                     }
                  } catch (IllegalArgumentException var12) {
                  }
               }
            }
         } catch (CertificateException var14) {
            throw new SSLException("Illegal authentication server certificate", var14);
         }

         throw new SSLException("Authentication server name mismatch");
      } else {
         throw new SSLException("Unknown certificate type, cannot validate: " + peer.getClass().getName());
      }
   }

   private static byte[] digest(byte[] pw) {
      MessageDigest dig;
      try {
         dig = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var3) {
         throw new RuntimeException(var3);
      }

      dig.update(pw);
      return dig.digest();
   }

   public String trypasswd(String user, byte[] phash) throws IOException {
      Message rpl = this.cmd("pw", user, phash);
      String stat = rpl.string();
      if (stat.equals("ok")) {
         return rpl.string();
      } else if (stat.equals("no")) {
         return null;
      } else {
         throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
      }
   }

   public String trytoken(String user, byte[] token) throws IOException {
      Message rpl = this.cmd("token", user, token);
      String stat = rpl.string();
      if (stat.equals("ok")) {
         return rpl.string();
      } else if (stat.equals("no")) {
         return null;
      } else {
         throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
      }
   }

   public byte[] getcookie() throws IOException {
      Message rpl = this.cmd("cookie");
      String stat = rpl.string();
      if (stat.equals("ok")) {
         return rpl.bytes(32);
      } else {
         throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
      }
   }

   public byte[] gettoken() throws IOException {
      Message rpl = this.cmd("mktoken");
      String stat = rpl.string();
      if (stat.equals("ok")) {
         return rpl.bytes(32);
      } else {
         throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
      }
   }

   public void close() throws IOException {
      this.sk.close();
   }

   private void sendmsg(Message msg) throws IOException {
      if (msg.blob.length > 65535) {
         throw new RuntimeException("Too long message in AuthClient (" + msg.blob.length + " bytes)");
      } else {
         byte[] buf = new byte[msg.blob.length + 2];
         buf[0] = (byte)((msg.blob.length & 0xFF00) >> 8);
         buf[1] = (byte)(msg.blob.length & 0xFF);
         System.arraycopy(msg.blob, 0, buf, 2, msg.blob.length);
         this.skout.write(buf);
      }
   }

   private void esendmsg(Object... args) throws IOException {
      Message buf = new Message(0);

      for (Object arg : args) {
         if (arg instanceof String) {
            buf.addstring((String)arg);
         } else {
            if (!(arg instanceof byte[])) {
               throw new RuntimeException("Illegal argument to esendmsg: " + arg.getClass());
            }

            buf.addbytes((byte[])arg);
         }
      }

      this.sendmsg(buf);
   }

   private static void readall(InputStream in, byte[] buf) throws IOException {
      int i = 0;

      while (i < buf.length) {
         int rv = in.read(buf, i, buf.length - i);
         if (rv < 0) {
            throw new IOException("Premature end of input");
         }

         i += rv;
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
      this.esendmsg(args);
      return this.recvmsg();
   }

   public static void main(final String[] args) throws Exception {
      Thread t = new HackThread(new Runnable() {
         @Override
         public void run() {
            try {
               AuthClient test = new AuthClient("127.0.0.1", 1871);

               try {
                  String acct = new AuthClient.NativeCred(args[0], args[1]).tryauth(test);
                  if (acct != null) {
                     System.out.println(acct);
                     System.out.println(Utils.byte2hex(test.getcookie()));
                     return;
                  }

                  System.err.println("failed");
               } finally {
                  test.close();
               }
            } catch (Exception var7) {
               throw new RuntimeException(var7);
            }
         }
      }, "Test");
      t.start();
      t.join();
   }

   static {
      try {
         ssl.trust(Resource.class.getResourceAsStream("authsrv.crt"));
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }

   public abstract static class Credentials {
      public abstract String tryauth(AuthClient var1) throws IOException;

      public abstract String name();

      public void discard() {
      }

      @Override
      protected void finalize() {
         this.discard();
      }

      public static class AuthException extends RuntimeException {
         public AuthException(String msg) {
            super(msg);
         }
      }
   }

   public static class NativeCred extends AuthClient.Credentials {
      public final String username;
      private byte[] phash;

      public NativeCred(String username, byte[] phash) {
         this.username = username;
         if ((this.phash = phash).length != 32) {
            throw new IllegalArgumentException("Password hash must be 32 bytes");
         }
      }

      private static byte[] ohdearjava(String a) {
         try {
            return AuthClient.digest(a.getBytes("utf-8"));
         } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException(var2);
         }
      }

      public NativeCred(String username, String pw) {
         this(username, ohdearjava(pw));
      }

      @Override
      public String name() {
         return this.username;
      }

      @Override
      public String tryauth(AuthClient cl) throws IOException {
         Message rpl = cl.cmd("pw", this.username, this.phash);
         String stat = rpl.string();
         if (stat.equals("ok")) {
            return rpl.string();
         } else if (stat.equals("no")) {
            String err = rpl.string();
            throw new AuthClient.Credentials.AuthException(err);
         } else {
            throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
         }
      }

      @Override
      public void discard() {
         if (this.phash != null) {
            for (int i = 0; i < this.phash.length; i++) {
               this.phash[i] = 0;
            }

            this.phash = null;
         }
      }
   }

   public static class TokenCred extends AuthClient.Credentials implements Serializable {
      public final String acctname;
      public final byte[] token;

      public TokenCred(String acctname, byte[] token) {
         this.acctname = acctname;
         if ((this.token = token).length != 32) {
            throw new IllegalArgumentException("Token must be 32 bytes");
         }
      }

      @Override
      public String name() {
         throw new UnsupportedOperationException();
      }

      @Override
      public String tryauth(AuthClient cl) throws IOException {
         Message rpl = cl.cmd("token", this.acctname, this.token);
         String stat = rpl.string();
         if (stat.equals("ok")) {
            return rpl.string();
         } else if (stat.equals("no")) {
            String err = rpl.string();
            throw new AuthClient.Credentials.AuthException(err);
         } else {
            throw new RuntimeException("Unexpected reply `" + stat + "' from auth server");
         }
      }
   }
}
