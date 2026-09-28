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
  
  private final ThreadLocal<InterruptAction> ia = new ThreadLocal<>();
  
  private class InterruptAction implements Runnable {
    private boolean interrupted;
    
    private InterruptAction() {}
    
    public void run() {
      this.interrupted = true;
      try {
        HackSocket.this.close();
      } catch (IOException iOException) {}
    }
  }
  
  private void hook() {
    Thread ct = Thread.currentThread();
    if (!(ct instanceof HackThread))
      throw new RuntimeException("Tried to use an HackSocket on a non-hacked thread."); 
    HackThread ut = (HackThread)ct;
    InterruptAction ia = new InterruptAction();
    ut.addil(ia);
    this.ia.set(ia);
  }
  
  private void release() throws ClosedByInterruptException {
    HackThread ut = (HackThread)Thread.currentThread();
    InterruptAction ia = this.ia.get();
    if (ia == null)
      throw new Error("Tried to release a hacked thread without an interrupt handler."); 
    ut.remil(ia);
    if (ia.interrupted) {
      ut.interrupt();
      throw new ClosedByInterruptException();
    } 
  }
  
  public void connect(SocketAddress address, int timeout) throws IOException {
    hook();
    try {
      super.connect(address, timeout);
    } finally {
      release();
    } 
  }
  
  public void connect(SocketAddress address) throws IOException {
    connect(address, 0);
  }
  
  private class HackInputStream extends InputStream {
    private final InputStream bk;
    
    private HackInputStream(InputStream bk) {
      this.bk = bk;
    }
    
    public void close() throws IOException {
      this.bk.close();
    }
    
    public int read() throws IOException {
      HackSocket.this.hook();
      try {
        return this.bk.read();
      } finally {
        HackSocket.this.release();
      } 
    }
    
    public int read(byte[] buf) throws IOException {
      HackSocket.this.hook();
      try {
        return this.bk.read(buf);
      } finally {
        HackSocket.this.release();
      } 
    }
    
    public int read(byte[] buf, int off, int len) throws IOException {
      HackSocket.this.hook();
      try {
        return this.bk.read(buf, off, len);
      } finally {
        HackSocket.this.release();
      } 
    }
  }
  
  private class HackOutputStream extends OutputStream {
    private final OutputStream bk;
    
    private HackOutputStream(OutputStream bk) {
      this.bk = bk;
    }
    
    public void close() throws IOException {
      this.bk.close();
    }
    
    public void flush() throws IOException {
      HackSocket.this.hook();
      try {
        this.bk.flush();
      } finally {
        HackSocket.this.release();
      } 
    }
    
    public void write(int b) throws IOException {
      HackSocket.this.hook();
      try {
        this.bk.write(b);
      } finally {
        HackSocket.this.release();
      } 
    }
    
    public void write(byte[] buf) throws IOException {
      HackSocket.this.hook();
      try {
        this.bk.write(buf);
      } finally {
        HackSocket.this.release();
      } 
    }
    
    public void write(byte[] buf, int off, int len) throws IOException {
      HackSocket.this.hook();
      try {
        this.bk.write(buf, off, len);
      } finally {
        HackSocket.this.release();
      } 
    }
  }
  
  public InputStream getInputStream() throws IOException {
    synchronized (this) {
      if (this.in == null)
        this.in = new HackInputStream(super.getInputStream()); 
      return this.in;
    } 
  }
  
  public OutputStream getOutputStream() throws IOException {
    synchronized (this) {
      if (this.out == null)
        this.out = new HackOutputStream(super.getOutputStream()); 
      return this.out;
    } 
  }
}
