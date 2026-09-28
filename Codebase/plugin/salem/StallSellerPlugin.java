package haven.plugins;

import haven.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StallSellerPlugin extends Plugin {

    public static boolean sellAll = false;

    public void load(UI ui) {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/stallseller")));
        XTendedPaginae.registerPlugin("stallseller", this);
    }

    public void execute(final UI ui) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String text = " \nTo switch between sell-modes, activate the plugin (hotkey = \"g\") while holding any item in the hand";

                if (!ui.gui.hand.isEmpty()) {

                    Iterator<GItem> handContent = ui.gui.hand.iterator();
                    while (handContent.hasNext()) {
                        GItem item = handContent.next();
                        ui.message("The item in your hand is: " + item.name() + "\nResource name: " + item.resname(),
                                GameUI.MsgType.INFO);
                    }

                    if (sellAll) {
                        ui.message("[StallSeller] Sell mode is now: Sell selected only \n(yellow cotton, mixed cotton, savage charms, arrowheads, gold bars, bear skins, beaver skins and cricket teams)" + text, GameUI.MsgType.INFO);
                        sellAll = false;
                    } else {
                        ui.message("[StallSeller] Sell mode is now: Sell everything" + text, GameUI.MsgType.INFO);
                        sellAll = true;
                    }

                } else {

                    if (sellAll) {
                        ui.message("[StallSeller] Selling items to stall. Sell mode is: \"Sell everything\". Please wait until further notice." + text, GameUI.MsgType.INFO);
                    } else {
                        ui.message("[StallSeller] Selling items to stall. Sell mode is: \"Sell selected only\". Please wait until further notice." + text, GameUI.MsgType.INFO);
                    }

                    List<WItem> items = ui.gui.maininv.getSameName("", true);

                    for (int i = items.size() - 1; i < items.size() && i >= 0; i--) {

                        Resource curs = ui.root.cursor;
                        if (!curs.name.contains("kreuz")) {
                            ui.message("[StallSeller] You are not selling to a stall! Aborting.", GameUI.MsgType.BAD);
                            return;
                        }

                        WItem subject = items.get(i);
                        if (!sellAll) {
                            if ((subject.item.resname().contains("cottoncloth")
                                    || subject.item.resname().contains("savagecharm")
                                    || subject.item.resname().contains("arrowhead")
                                    || subject.item.resname().contains("goldbar")
                                    || subject.item.resname().contains("hide-bear-prep")
                                    //                           || subject.item.resname().contains("cricketteam")
                                    || subject.item.resname().contains("hide-beaver-prep"))
                                    && !subject.item.resname().contains("cottoncloth1")
                                    && !subject.item.resname().contains("cottoncloth2")
                                    && !subject.item.resname().contains("cottoncloth3")) {

                                subject.mousedown(Coord.z, 1);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                }
                            }

                        } else {
                            subject.mousedown(Coord.z, 1);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                    ui.message("[StallSeller] Stopped selling items.", GameUI.MsgType.INFO);
                }
            }
        }, "Stall seller").start();

    }
}
