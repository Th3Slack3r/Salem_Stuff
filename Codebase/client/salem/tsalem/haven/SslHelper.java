package haven;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
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
    } catch (Exception e) {
      throw new Error(e);
    } 
  }
  
  private synchronized SSLContext ctx() {
    if (this.ctx == null)
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
      } catch (NoSuchAlgorithmException e) {
        throw new Error(e);
      } catch (KeyStoreException e) {
        throw new RuntimeException(e);
      } catch (UnrecoverableKeyException e) {
        throw new RuntimeException(e);
      } catch (KeyManagementException e) {
        throw new RuntimeException(e);
      }  
    return this.ctx;
  }
  
  private synchronized SSLSocketFactory sfac() {
    if (this.sfac == null)
      this.sfac = ctx().getSocketFactory(); 
    return this.sfac;
  }
  
  private void clear() {
    this.ctx = null;
    this.sfac = null;
  }
  
  public synchronized void trust(Certificate cert) {
    clear();
    try {
      this.trusted.setCertificateEntry("cert-" + this.tserial++, cert);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static Certificate loadX509(InputStream in) throws IOException, CertificateException {
    CertificateFactory fac = CertificateFactory.getInstance("X.509");
    return fac.generateCertificate(in);
  }
  
  public static Collection<? extends Certificate> loadX509s(InputStream in) throws IOException, CertificateException {
    CertificateFactory fac = CertificateFactory.getInstance("X.509");
    return fac.generateCertificates(in);
  }
  
  public void trust(InputStream in) throws IOException, CertificateException {
    for (Certificate cert : loadX509s(in))
      trust(cert); 
  }
  
  public synchronized void loadCredsPkcs12(InputStream in, char[] pw) throws IOException, CertificateException {
    clear();
    try {
      this.creds = KeyStore.getInstance("PKCS12");
      this.creds.load(in, pw);
      this.pw = pw;
    } catch (KeyStoreException e) {
      throw new Error(e);
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    } 
  }
  
  public HttpsURLConnection connect(URL url) throws IOException {
    if (!url.getProtocol().equals("https"))
      throw new MalformedURLException("Can only be used to connect to HTTPS servers"); 
    HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
    conn.setSSLSocketFactory(sfac());
    if (this.ver != null)
      conn.setHostnameVerifier(this.ver); 
    return conn;
  }
  
  public HttpsURLConnection connect(String url) throws IOException {
    return connect(new URL(url));
  }
  
  public void ignoreName() {
    this.ver = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession sess) {
          return true;
        }
      };
  }
  
  public SSLSocket connect(Socket sk, String host, int port, boolean autoclose) throws IOException {
    return (SSLSocket)sfac().createSocket(sk, host, port, autoclose);
  }
  
  public SSLSocket connect(String host, int port) throws IOException {
    IOException lerr = null;
    for (InetAddress haddr : InetAddress.getAllByName(host)) {
      try {
        Socket sk = new HackSocket();
        sk.connect(new InetSocketAddress(haddr, port), 5000);
        return connect(sk, host, port, true);
      } catch (IOException e) {
        if (lerr != null)
          e.addSuppressed(lerr); 
        lerr = e;
      } 
    } 
    if (lerr != null)
      throw lerr; 
    throw new UnknownHostException(host);
  }
  
  public boolean hasCreds() {
    return (this.creds != null);
  }
}
