package haven;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Debug {
   public static boolean kf1;
   public static boolean kf2;
   public static boolean kf3;
   public static boolean kf4;
   public static boolean pk1;
   public static boolean pk2;
   public static boolean pk3;
   public static boolean pk4;

   public static void cycle() {
      pk1 = kf1;
      pk2 = kf2;
      pk3 = kf3;
      pk4 = kf4;
   }

   public static void dumpimage(BufferedImage img, String fn) {
      try {
         ImageIO.write(img, "PNG", new File(fn));
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }
}
