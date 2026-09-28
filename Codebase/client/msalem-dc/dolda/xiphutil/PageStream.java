package dolda.xiphutil;

import com.jcraft.jogg.Page;
import com.jcraft.jogg.SyncState;
import java.io.IOException;
import java.io.InputStream;

public class PageStream {
   private SyncState sync = new SyncState();
   private final InputStream in;
   private boolean eos = false;

   public PageStream(InputStream in) {
      this.in = in;
      this.sync.init();
   }

   public Page page() throws IOException {
      if (this.eos) {
         return null;
      } else {
         Page page = new Page();

         while (true) {
            int ret = this.sync.pageout(page);
            if (ret < 0) {
               throw new OggException();
            }

            if (ret == 1) {
               if (page.eos() != 0) {
                  this.eos = true;
               }

               return page;
            }

            int off = this.sync.buffer(4096);
            int len = this.in.read(this.sync.data, off, 4096);
            if (len < 0) {
               return null;
            }

            this.sync.wrote(len);
         }
      }
   }

   public void close() throws IOException {
      this.in.close();
   }
}
