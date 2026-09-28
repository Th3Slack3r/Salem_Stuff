package haven.plugins;

import haven.*;
import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;

public class PickupPlugin extends Plugin{
    public void load(UI ui)
    {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/pickup")));
        XTendedPaginae.registerPlugin("pickup",this);
    }
    
    public void execute(UI ui){
        //we find the closest item on the ground
        Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
        double distance = 0.0;
        Gob closest_gob = null;
        Iterator<Gob> gobs_iterator = gobs.iterator();
        Gob current_gob = null;
        Coord player_location = ui.gui.map.player().rc;
        while(gobs_iterator.hasNext()) {
            current_gob = gobs_iterator.next();
            Coord gob_location = current_gob.rc;
            ResDrawable rd = null;
            String nm = "";
            try{
                rd = current_gob.getattr(ResDrawable.class);
                if(rd!=null)
                    nm = rd.res.get().name;
            }catch(Loading l){}
            if(nm.contains("terobjs/herbs") || nm.contains("terobjs/item"))
            {
                double this_distance = gob_location.dist(player_location);
                if((this_distance < distance)||closest_gob==null) {
                    closest_gob = current_gob;
                    distance = this_distance;
                }
            }
        }
        //and right click it if one was found
        if(closest_gob!=null){
            ui.wdgmsg(ui.gui.map, "click", closest_gob.sc, closest_gob.rc, 3, 0, 0, (int) closest_gob.id, closest_gob.rc, 0, -1);
        }
        else{
            ui.message("[PickupPlugin] No forageable items found in the vicinity!", GameUI.MsgType.INFO);
        }
    }
}
