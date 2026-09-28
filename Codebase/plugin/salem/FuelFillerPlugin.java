package haven.plugins;

import haven.*;
import static java.lang.Thread.sleep;
import java.util.Collection;
import java.util.Iterator;

public class FuelFillerPlugin extends Plugin{
    public void load(UI ui)
    {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/fuelfiller")));
        XTendedPaginae.registerPlugin("fuelfiller",this);
    }
    
    public void execute(final UI ui){
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                perform_task(ui);
            }
        },"Fuel filler").start();
    }
    
    public void perform_task(UI ui){
        if(ui.gui.hand.isEmpty()){
            ui.message("[FuelFillerPlugin] Take the fuel on your hand.", GameUI.MsgType.INFO);
            return;
        }
        //we find the closest object that fits
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
            if(nm.contains("terobjs/brazier") ||
               nm.contains("terobjs/torchpost") ||
               nm.contains("terobjs/babybrazier") ||
               nm.contains("terobjs/stove") ||
               nm.contains("terobjs/oven") ||
               nm.contains("terobjs/fireplace") ||
               nm.contains("terobjs/meatsmoker") ||
               nm.contains("terobjs/fineryforge") ||
               nm.contains("terobjs/oresmelter") ||
               nm.contains("terobjs/cementationfurnace") ||
               nm.contains("terobjs/kiln") ||
               nm.contains("terobjs/haystack") ||
               nm.contains("terobjs/field") ||
               nm.contains("terobjs/compost") ||
               nm.contains("terobjs/turkeycoop") ||
               nm.contains("terobjs/barrel") ||
               nm.contains("terobjs/bigbarrel")
              )
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
            for(int i=10; i>0 && !ui.gui.hand.isEmpty(); i--) {
                ui.wdgmsg(ui.gui.map, "itemact", closest_gob.sc, closest_gob.rc, 1, (int) closest_gob.id, closest_gob.rc, -1);
                try {
                    sleep(150);
                } catch (InterruptedException ignored) {}
            }
            ui.message("[FuelFillerPlugin] Finished.", GameUI.MsgType.INFO);
        }
        else{
            ui.message("[FuelFillerPlugin] Found no objects requiring fuel in your vicinity!", GameUI.MsgType.INFO);
        }
    }
}
