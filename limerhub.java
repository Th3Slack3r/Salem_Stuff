package haven.plugins;

import haven.*;
import java.util.Collection;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MyFirstPlugin extends Plugin {
    private boolean shown = false;
    private HelloWorldWindow wnd = null;

    public void load(UI ui) {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/hello")));
        XTendedPaginae.registerPlugin("hello", this);
    }

    public void execute(UI ui) {
        if (!shown) {
            shown = true;
            wnd = new HelloWorldWindow(ui.root, () -> {
                shown = false;
                wnd = null;
            });
        }
    }
}

class HelloWorldWindow extends Window {
    private Tex bgImage;
    private final Runnable onClose;
    private final Button btnFront;
    private final Button btnDiscord;

    public HelloWorldWindow(Widget parent, Runnable onClose) {
        // Set window position coord, inner size; title handled by Window
        super(new Coord(300, 200), new Coord(300, 300), parent, "Limer's Hub");
        this.justclose = true;
        this.onClose = onClose;

        // Load remote image (200x200)
        try {
            BufferedImage img = ImageIO.read(new URL("https://limers.hstn.me/img/logo.png"));
            if (img != null)
                bgImage = new TexI(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * IMPORTANT:
         * - Do NOT call pack() here (pack would shrink the window).
         * - Use 'asz' (the inner content size) to center things inside the window.
         */

        int buttonWidth = 200;
        int buttonHeight = 25;
        int imgHeight = 200;
        int imgTop = 10;
        int buttonSpacing = 8;

        // compute center based on inner content area (asz)
        int centerX = (this.asz.x - buttonWidth) / 2;
        int firstButtonY = imgTop + imgHeight + 10; // inside content coords

        // Buttons: second parameter must be Integer (width)
        btnFront = new Button(new Coord(centerX, firstButtonY), Integer.valueOf(buttonWidth), this, "The Front") {
            public void click() {
                openLink("https://limers.hstn.me/index.php");
            }
        };

        btnDiscord = new Button(new Coord(centerX, firstButtonY + buttonHeight + buttonSpacing), Integer.valueOf(buttonWidth), this, "Discord") {
            public void click() {
                openLink("https://discord.com/invite/1180963100820779110");
            }
        };

        // DO NOT call pack(); we want a stable 300x300 window as set above
    }

    @Override
    public void cdraw(GOut g) {
        // Fill content background black
        g.chcolor(0, 0, 0, 255);
        g.frect(Coord.z, asz);
        g.chcolor();

        // Draw the 200x200 image centered horizontally inside the content area
        if (bgImage != null) {
            Coord imgPos = new Coord(
                (asz.x - 200) / 2,
                10
            );
            // draw with explicit size (200x200)
            g.image(bgImage, imgPos, new Coord(200, 200));
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (onClose != null)
            onClose.run();
    }

    private void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            e.printStackTrace();
        }
    }
}



















