package haven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;

public class HackSocket extends Socket {
   private InputStream in = null;
   private OutputStream out = null;
   private ThreadLocal<HackSocket.InterruptAction> ia = new ThreadLocal<>();

   private void hook() {
      Thread ct = Thread.currentThread();
      if (!(ct instanceof HackThread)) {
         throw new RuntimeException("Tried to use an HackSocket on a non-hacked thread.");
      } else {
         HackThread ut = (HackThread)ct;
         HackSocket.InterruptAction ia = new HackSocket.InterruptAction();
         ut.addil(ia);
         this.ia.set(ia);
      }
   }

   private void release() throws ClosedByInterruptException {
      HackThread ut = (HackThread)Thread.currentThread();
      HackSocket.InterruptAction ia = this.ia.get();
      if (ia == null) {
         throw new Error("Tried to release a hacked thread without an interrupt handler.");
      } else {
         ut.remil(ia);
         if (ia.interrupted) {
            ut.interrupt();
            throw new ClosedByInterruptException();
         }
      }
   }

   @Override
   public void connect(SocketAddress address, int timeout) throws IOException {
      this.hook();

      try {
         super.connect(address, timeout);
      } finally {
         this.release();
      }
   }

   @Override
   public void connect(SocketAddress address) throws IOException {
      this.connect(address, 0);
   }

   @Override
   public InputStream getInputStream() throws IOException {
      synchronized (this) {
         if (this.in == null) {
            this.in = new HackSocket.HackInputStream(super.getInputStream());
         }

         return this.in;
      }
   }

   @Override
   public OutputStream getOutputStream() throws IOException {
      synchronized (this) {
         if (this.out == null) {
            this.out = new HackSocket.HackOutputStream(super.getOutputStream());
         }

         return this.out;
      }
   }

   private class HackInputStream extends InputStream {
      private InputStream bk;

      private HackInputStream(InputStream bk) {
         this.bk = bk;
      }

      @Override
      public void close() throws IOException {
         this.bk.close();
      }

      @Override
      public int read() throws IOException {
         HackSocket.this.hook();

         int var1;
         try {
            var1 = this.bk.read();
         } finally {
            HackSocket.this.release();
         }

         return var1;
      }

      @Override
      public int read(byte[] buf) throws IOException {
         HackSocket.this.hook();

         int var2;
         try {
            var2 = this.bk.read(buf);
         } finally {
            HackSocket.this.release();
         }

         return var2;
      }

      @Override
      public int read(byte[] buf, int off, int len) throws IOException {
         HackSocket.this.hook();

         int var4;
         try {
            var4 = this.bk.read(buf, off, len);
         } finally {
            HackSocket.this.release();
         }

         return var4;
      }
   }

   private class HackOutputStream extends OutputStream {
      private OutputStream bk;

      private HackOutputStream(OutputStream bk) {
         this.bk = bk;
      }

      @Override
      public void close() throws IOException {
         this.bk.close();
      }

      @Override
      public void flush() throws IOException {
         HackSocket.this.hook();

         try {
            this.bk.flush();
         } finally {
            HackSocket.this.release();
         }
      }

      @Override
      public void write(int b) throws IOException {
         HackSocket.this.hook();

         try {
            this.bk.write(b);
         } finally {
            HackSocket.this.release();
         }
      }

      @Override
      public void write(byte[] buf) throws IOException {
         HackSocket.this.hook();

         try {
            this.bk.write(buf);
         } finally {
            HackSocket.this.release();
         }
      }

      @Override
      public void write(byte[] buf, int off, int len) throws IOException {
         HackSocket.this.hook();

         try {
            this.bk.write(buf, off, len);
         } finally {
            HackSocket.this.release();
         }
      }
   }

   private class InterruptAction implements Runnable {
      private boolean interrupted;

      private InterruptAction() {
      }

      @Override
      public void run() {
         this.interrupted = true;

         try {
            HackSocket.this.close();
         } catch (IOException var2) {
         }
      }
   }
}
