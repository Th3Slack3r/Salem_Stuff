package dolda.xiphutil;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import java.io.IOException;

public class PacketStream {
   private StreamState strm = null;
   private Page page = null;
   private final PageStream in;
   private boolean eos = false;

   public PacketStream(PageStream in) {
      this.in = in;
   }

   public Packet packet() throws IOException {
      if (this.eos) {
         return null;
      } else {
         if (this.strm == null) {
            this.strm = new StreamState();
            this.page = this.in.page();
            this.strm.init(this.page.serialno());
         }

         Packet pkt = new Packet();

         while (true) {
            int ret = this.strm.packetout(pkt);
            if (ret < 0) {
               throw new OggException();
            }

            if (ret == 1) {
               return pkt;
            }

            if (this.page == null && (this.page = this.in.page()) == null) {
               this.eos = true;
               return null;
            }

            if (this.strm.pagein(this.page) != 0) {
               throw new OggException();
            }

            this.page = null;
         }
      }
   }

   public void close() throws IOException {
      this.in.close();
   }
}
