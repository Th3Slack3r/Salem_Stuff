package haven.plugins;

import haven.*;
import java.util.Collection;

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
    private final Text helloText;
    private final Runnable onClose;

    public HelloWorldWindow(Widget parent, Runnable onClose) {
        super(new Coord(300, 200), new Coord(400, 200), parent, "Hello World");
        this.justclose = true;
        this.onClose = onClose;

        helloText = Text.render("Hello World!");

        this.asz = new Coord(Math.max(asz.x, helloText.sz().x + 20),
                             Math.max(asz.y, helloText.sz().y + 20));
    }

    @Override
    public void cdraw(GOut g) {
        g.chcolor(0, 0, 0, 255);
        g.frect(Coord.z, asz);
        g.chcolor();

        Coord textPos = new Coord(
            (asz.x - helloText.sz().x) / 2,
            (asz.y - helloText.sz().y) / 2
        );
        g.image(helloText.tex(), textPos);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (onClose != null) {
            onClose.run();
        }
    }
}















