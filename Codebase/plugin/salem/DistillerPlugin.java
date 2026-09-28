package haven.plugins;

import haven.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class DistillerPlugin extends Plugin implements Runnable{
    public void load(UI ui)
    {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/distillers")));
        XTendedPaginae.registerPlugin("distillers",this);
    }
    
    public void execute(UI ui){
        //introduce a new thread with a timer callback
        ui.message("[Distillers] Starting the 'distillers' plugin.", GameUI.MsgType.INFO);
        state = DistillerState.START;
        (new Thread(this)).start();
    }

    enum DistillerState {START,OPEN_DISTILLER,WAIT_DISTILLER_OPEN,WAIT_DISTILLER_EMPTY,WAIT_DISTILLER_FULL};
    DistillerState state = DistillerState.START;
    Iterator<GItem> distiller_iterator=null;
    int places = 0;
    
    @Override
    public void run() {
        List<GItem> distiller_items;
        while(true){
        System.out.println(""+state.toString());
        switch(state)
        {
            case START:
                List<GItem> distillers = searchTable();
                if(distillers==null)
                {
                    UI.instance.message("[Distillers] Please open an alchemy table to get started.", GameUI.MsgType.INFO);
                    return;
                }
                else
                {
                    UI.instance.message("[Distillers] Found " + distillers.size() + " distillers.", GameUI.MsgType.INFO);
                    distiller_iterator = distillers.iterator();
                    state = DistillerState.OPEN_DISTILLER;
                }
            case OPEN_DISTILLER:
                if(distiller_iterator.hasNext())
                {
                    GItem distiller = distiller_iterator.next();
                    distiller.wdgmsg("iact",Coord.z);
                    state = DistillerState.WAIT_DISTILLER_OPEN;
                    break;
                }
                else
                {
                    UI.instance.message("[Distillers] All distillers done; exiting.", GameUI.MsgType.INFO);
                    return;
                }
            case WAIT_DISTILLER_OPEN:
                distiller_items = searchDistiller();
                if(distiller_items == null)
                {
                    break;
                }
                places = countDehydratedItems(distiller_items)+(2-distiller_items.size());
                if(places < 0)
                {
                    break;
                }
                UI.instance.message("[Distillers] Placing "+places+" items.", GameUI.MsgType.INFO);
                takeDehydratedItems(distiller_items);
                state = DistillerState.WAIT_DISTILLER_EMPTY;
            case WAIT_DISTILLER_EMPTY:
                distiller_items = searchDistiller();
                if(distiller_items == null || countDehydratedItems(distiller_items)!=0)
                {
                    break;
                }
                placeFreshItems(places);
                state = DistillerState.WAIT_DISTILLER_FULL;
            case WAIT_DISTILLER_FULL:
                distiller_items = searchDistiller();
                if(distiller_items==null || distiller_items.size() != 2)
                {
                    break;
                }
                state = DistillerState.OPEN_DISTILLER;
                break;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            return;
        }
        }
    }
    
    void placeFreshItems(int places)
    {
        int count = 0;
        //search the maininv for <places> fresh items
        Inventory maininv = UI.instance.gui.maininv;
        for (Widget wdg = maininv.lchild; wdg != null && count < places; wdg = wdg.prev) {
            if (wdg.visible && wdg instanceof WItem) {
                String thatname = ((WItem) wdg).item.resname();
                if((thatname.endsWith("blackberry") || 
                    thatname.endsWith("crowberry") || 
                    thatname.endsWith("huckleberry") || 
                    thatname.endsWith("cranberry") || 
                    thatname.endsWith("redrose")) && !(thatname.contains("hydra")))
                {
                    ((WItem)wdg).item.wdgmsg("transfer", Coord.z);
                    count += 1;
                }
            }
        }
        if(count<places)
        {
            UI.instance.message("[Distillers] WARNING! Not enough fresh material!", GameUI.MsgType.INFO);
        }
    }
    
    int countDehydratedItems(List<GItem> items)
    {
        int count = 0;
        for(GItem gi : items)
        {
            String name = gi.resname();
            if(name.length() == 0)
            {
                return -1;
            }
            if(name.contains("ydra"))
            {
                count++ ;
            }
        }
        return count;
    }
    
    void takeDehydratedItems(List<GItem> items)
    {
        for(GItem gi : items)
        {
            if(gi.resname().contains("ydra"))
            {
                gi.wdgmsg("transfer",Coord.z);
            }
        }
    }
    
    List<GItem> searchDistiller(){
        try{
        List<GItem> item_list = new ArrayList<>();
        boolean founddistiller = false;
        for(Widget w : UI.instance.widgets.values())
        {
            if(Inventory.class.isInstance(w))
            {
                Window wp = ((Inventory)w).getparent(Window.class);
                if(wp.cap.text.contains("Dist"))
                {
                    founddistiller = true;
                    for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
                        if (wdg.visible && wdg instanceof WItem) {
                            item_list.add(((WItem) wdg).item);
                        }
                    }
                }
            }
        }
        if(founddistiller)
        {
            return item_list;
        }
        else
        {
            return null;
        }
        }catch(ConcurrentModificationException cme)
        {
            //this is bound to happen sometimes
            return null;
        }
    }
    
    List<GItem> searchTable(){
        try{
        //check for a table window somewhere
        List<GItem> distiller_list = new ArrayList<>();
        boolean foundtable = false;
        for(Widget w : UI.instance.widgets.values())
        {
            if(Inventory.class.isInstance(w))
            {
                Window wp = ((Inventory)w).getparent(Window.class);
                if(wp.cap.text.equals("Alchemy Table"))
                {
                    foundtable = true;
                    for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
                        if (wdg.visible && wdg instanceof WItem) {
                            String thatname = ((WItem) wdg).item.resname();
                            if(thatname.contains("istiller"))
                                distiller_list.add(((WItem) wdg).item);
                        }
                    }
                }
            }
        }
        if(foundtable)
        {
            return distiller_list;
        }
        else
        {
            return null;
        }
        }catch(ConcurrentModificationException cme)
        {
            //this is bound to happen sometimes
            return null;
        }
    }
}
