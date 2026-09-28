package haven;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URL;

public class DesktopBrowser extends WebBrowser {
   private final Desktop desktop;

   private DesktopBrowser(Desktop desktop) {
      this.desktop = desktop;
   }

   public static DesktopBrowser create() {
      try {
         Class.forName("java.awt.Desktop");
         if (!Desktop.isDesktopSupported()) {
            return null;
         } else {
            Desktop desktop = Desktop.getDesktop();
            return !desktop.isSupported(Action.BROWSE) ? null : new DesktopBrowser(desktop);
         }
      } catch (Exception var1) {
         return null;
      }
   }

   @Override
   public void show(URL url) {
      try {
         this.desktop.browse(url.toURI());
      } catch (Exception var3) {
         throw new WebBrowser.BrowserException(var3);
      }
   }
}
