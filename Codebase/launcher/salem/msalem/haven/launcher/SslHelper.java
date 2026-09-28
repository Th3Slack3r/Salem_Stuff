package haven.launcher;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslHelper {
   private SSLContext ctx = null;
   private SSLSocketFactory sfac = null;
   private HostnameVerifier ver = null;

   private synchronized SSLContext ctx() {
      if (this.ctx == null) {
         try {
            this.ctx = SSLContext.getInstance("TLS");
            this.ctx.init(null, new TrustManager[]{new X509TrustManager() {
               @Override
               public void checkClientTrusted(X509Certificate[] certs, String type) {
                  throw new RuntimeException();
               }

               @Override
               public void checkServerTrusted(X509Certificate[] certs, String type) {
               }

               @Override
               public X509Certificate[] getAcceptedIssuers() {
                  return new X509Certificate[0];
               }
            }}, new SecureRandom());
         } catch (NoSuchAlgorithmException var4) {
            throw new Error(var4);
         } catch (KeyManagementException var5) {
            throw new RuntimeException(var5);
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

   public HttpsURLConnection connect(URL url) throws IOException {
      if (!url.getProtocol().equals("https")) {
         return null;
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

   public SslHelper ignorename() {
      this.ver = new HostnameVerifier() {
         @Override
         public boolean verify(String hostname, SSLSession sess) {
            return true;
         }
      };
      return this;
   }
}
