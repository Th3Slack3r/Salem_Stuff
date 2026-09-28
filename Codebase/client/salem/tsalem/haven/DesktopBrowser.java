package haven;

import java.awt.Desktop;
import java.net.URL;

public class DesktopBrowser extends WebBrowser {
  private final Desktop desktop;
  
  private DesktopBrowser(Desktop desktop) {
    this.desktop = desktop;
  }
  
  public static DesktopBrowser create() {
    try {
      Class.forName("java.awt.Desktop");
      if (!Desktop.isDesktopSupported())
        return null; 
      Desktop desktop = Desktop.getDesktop();
      if (!desktop.isSupported(Desktop.Action.BROWSE))
        return null; 
      return new DesktopBrowser(desktop);
    } catch (Exception e) {
      return null;
    } 
  }
  
  public void show(URL url) {
    try {
      this.desktop.browse(url.toURI());
    } catch (Exception e) {
      throw new WebBrowser.BrowserException(e);
    } 
  }
}
