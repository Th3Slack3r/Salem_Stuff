package haven;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslHelper {
   private KeyStore creds;
   private KeyStore trusted;
   private SSLContext ctx = null;
   private SSLSocketFactory sfac = null;
   private int tserial = 0;
   private char[] pw;
   private HostnameVerifier ver = null;

   public SslHelper() {
      this.creds = null;

      try {
         this.trusted = KeyStore.getInstance(KeyStore.getDefaultType());
         this.trusted.load(null, null);
      } catch (Exception var2) {
         throw new Error(var2);
      }
   }

   private synchronized SSLContext ctx() {
      if (this.ctx == null) {
         try {
            this.ctx = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyManager[] kms = null;
            tmf.init(this.trusted);
            if (this.creds != null) {
               kmf.init(this.creds, this.pw);
               kms = kmf.getKeyManagers();
            }

            this.ctx.init(kms, tmf.getTrustManagers(), new SecureRandom());
         } catch (NoSuchAlgorithmException var4) {
            throw new Error(var4);
         } catch (KeyStoreException var5) {
            throw new RuntimeException(var5);
         } catch (UnrecoverableKeyException var6) {
            throw new RuntimeException(var6);
         } catch (KeyManagementException var7) {
            throw new RuntimeException(var7);
         }
      }

      return this.ctx;
   }

   private synchronized SSLSocketFactory sfac() {
      if (this.sfac == null) {
         this.sfac = this.ctx().getSocketFactory();
      }

      return this.sfac;
   }

   private void clear() {
      this.ctx = null;
      this.sfac = null;
   }

   public synchronized void trust(Certificate cert) {
      this.clear();

      try {
         this.trusted.setCertificateEntry("cert-" + this.tserial++, cert);
      } catch (KeyStoreException var3) {
         throw new RuntimeException(var3);
      }
   }

   public static Certificate loadX509(InputStream in) throws IOException, CertificateException {
      CertificateFactory fac = CertificateFactory.getInstance("X.509");
      return fac.generateCertificate(in);
   }

   public synchronized void loadCredsPkcs12(InputStream in, char[] pw) throws IOException, CertificateException {
      this.clear();

      try {
         this.creds = KeyStore.getInstance("PKCS12");
         this.creds.load(in, pw);
         this.pw = pw;
      } catch (KeyStoreException var4) {
         throw new Error(var4);
      } catch (NoSuchAlgorithmException var5) {
         throw new Error(var5);
      }
   }

   public HttpsURLConnection connect(URL url) throws IOException {
      if (!url.getProtocol().equals("https")) {
         throw new MalformedURLException("Can only be used to connect to HTTPS servers");
      } else {
         HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
         conn.setSSLSocketFactory(this.sfac());
         if (this.ver != null) {
            conn.setHostnameVerifier(this.ver);
         }

         return conn;
      }
   }

   public HttpsURLConnection connect(String url) throws IOException {
      return this.connect(new URL(url));
   }

   public void ignoreName() {
      this.ver = new HostnameVerifier() {
         @Override
         public boolean verify(String hostname, SSLSession sess) {
            return true;
         }
      };
   }

   public SSLSocket connect(Socket sk, String host, int port, boolean autoclose) throws IOException {
      return (SSLSocket)this.sfac().createSocket(sk, host, port, autoclose);
   }

   public SSLSocket connect(String host, int port) throws IOException {
      Socket sk = new HackSocket();
      sk.connect(new InetSocketAddress(host, port));
      return this.connect(sk, host, port, true);
   }

   public boolean hasCreds() {
      return this.creds != null;
   }
}
