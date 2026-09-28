package haven.error;

import haven.Config;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Map.Entry;

public class ErrorHandler extends ThreadGroup {
   private final URL errordest;
   private static final String[] sysprops = new String[]{"java.version", "java.vendor", "os.name", "os.arch", "os.version"};
   private final ThreadGroup initial;
   private Map<String, Object> props = new HashMap<>();
   private ErrorHandler.Reporter reporter;

   public static ErrorHandler find() {
      for (ThreadGroup tg = Thread.currentThread().getThreadGroup(); tg != null; tg = tg.getParent()) {
         if (tg instanceof ErrorHandler) {
            return (ErrorHandler)tg;
         }
      }

      return null;
   }

   public static void setprop(String key, Object val) {
      ErrorHandler tg = find();
      if (tg != null) {
         tg.lsetprop(key, val);
      }
   }

   public void lsetprop(String key, Object val) {
      this.props.put(key, val);
   }

   private void defprops() {
      for (String p : sysprops) {
         this.props.put(p, System.getProperty(p));
      }

      Runtime rt = Runtime.getRuntime();
      this.props.put("cpus", rt.availableProcessors());
      InputStream in = ErrorHandler.class.getResourceAsStream("/buildinfo");

      try {
         try {
            if (in != null) {
               Properties info = new Properties();
               info.load(in);

               for (Entry<Object, Object> e : info.entrySet()) {
                  this.props.put("jar." + (String)e.getKey(), e.getValue());
               }
            }
         } finally {
            in.close();
         }
      } catch (IOException var10) {
         throw new Error(var10);
      }
   }

   public ErrorHandler(ErrorStatus ui, URL errordest) {
      super("Haven client");
      this.errordest = errordest;
      this.initial = Thread.currentThread().getThreadGroup();
      this.reporter = new ErrorHandler.Reporter(ui);
      this.reporter.start();
      this.defprops();
   }

   public ErrorHandler(URL errordest) {
      this(new ErrorStatus.Simple(), errordest);
   }

   public ErrorHandler(ErrorStatus ui) {
      this(ui, null);
   }

   public ErrorHandler() {
      this(new ErrorStatus.Simple());
   }

   public void sethandler(ErrorStatus handler) {
      this.reporter.status = handler;
   }

   @Override
   public void uncaughtException(Thread t, Throwable e) {
      this.reporter.report(t, e);
   }

   private class Reporter extends Thread {
      private Queue<Report> errors = new LinkedList<>();
      private ErrorStatus status;

      public Reporter(ErrorStatus status) {
         super(ErrorHandler.this.initial, "Error reporter");
         this.setDaemon(true);
         this.status = status;
      }

      @Override
      public void run() {
         while (true) {
            synchronized (this.errors) {
               try {
                  this.errors.wait();
               } catch (InterruptedException var6) {
                  return;
               }

               Report r;
               while ((r = this.errors.poll()) != null) {
                  try {
                     this.doreport(r);
                  } catch (Exception var5) {
                     r.t.printStackTrace(System.out);
                     this.status.senderror(var5);
                  }
               }
            }
         }
      }

      private void doreport(Report r) throws IOException {
         if (this.status.goterror(r.t)) {
            if (ErrorHandler.this.errordest != null) {
               this.dourlreport(r);
            } else {
               this.dolocalreport(r);
            }
         }
      }

      private void dolocalreport(Report r) throws IOException {
         FileOutputStream fs = new FileOutputStream(Config.userhome + "/error.html");
         HtmlReporter.makereport(fs, r);
         fs.close();
         this.status.done(null, null);
      }

      private void dourlreport(Report r) throws IOException {
         URLConnection c = ErrorHandler.this.errordest.openConnection();
         this.status.connecting();
         c.setDoOutput(true);
         c.addRequestProperty("Content-Type", "application/x-java-error");
         c.connect();
         ObjectOutputStream o = new ObjectOutputStream(c.getOutputStream());
         this.status.sending();
         o.writeObject(r);
         o.close();
         String ctype = c.getContentType();
         StringWriter buf = new StringWriter();
         Reader i = new InputStreamReader(c.getInputStream(), "utf-8");
         char[] dbuf = new char[1024];

         while (true) {
            int len = i.read(dbuf);
            if (len < 0) {
               i.close();
               if (ctype.equals("text/x-report-info")) {
                  this.status.done("text/x-report-info", buf.toString());
               } else {
                  if (ctype.equals("text/x-report-error")) {
                     throw new ReportException(buf.toString());
                  }

                  this.status.done(null, null);
               }

               return;
            }

            buf.write(dbuf, 0, len);
         }
      }

      public void report(Thread th, Throwable t) {
         Report r = new Report(t);
         r.props.putAll(ErrorHandler.this.props);
         r.props.put("thnm", th.getName());
         r.props.put("thcl", th.getClass().getName());
         synchronized (this.errors) {
            this.errors.add(r);
            this.errors.notifyAll();
         }

         try {
            r.join();
         } catch (InterruptedException var6) {
         }
      }
   }
}
