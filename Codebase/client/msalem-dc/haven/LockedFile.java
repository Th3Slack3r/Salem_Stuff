package haven;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LockedFile implements AutoCloseable {
   public FileChannel f;
   public FileLock l;

   private LockedFile(FileChannel f, FileLock l) {
      this.f = f;
      this.l = l;
   }

   public void release() throws IOException {
      if (this.l != null) {
         this.l.release();
         this.l = null;
      }
   }

   @Override
   public void close() throws IOException {
      this.release();
      if (this.f != null) {
         this.f.close();
         this.f = null;
      }
   }

   public static LockedFile lock(Path path, long pos, long len, boolean shared) throws IOException {
      boolean intr = false;

      try {
         while (true) {
            try {
               FileChannel fp = null;

               try {
                  fp = Utils.ioretry(() -> FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
                  FileLock lk = fp.lock(pos, len, shared);
                  LockedFile ret = new LockedFile(fp, lk);
                  fp = null;
                  return ret;
               } finally {
                  if (fp != null) {
                     fp.close();
                  }
               }
            } catch (FileLockInterruptionException var20) {
               Thread.currentThread();
               Thread.interrupted();
               intr = true;
            }
         }
      } finally {
         if (intr) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public static LockedFile lock(Path path) throws IOException {
      return lock(path, 0L, 1L, false);
   }
}
