package haven;

import java.util.Iterator;

public class BuffAlertOptWnd extends Window {
  private static Window instance;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new BuffAlertOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public BuffAlertOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Buff/Alert Configuration");
    this.justclose = true;
    int y = 0;
    y += 25;
    (new CheckBox(new Coord(0, y), this, "Activate Buff Alert") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_activate = val;
          Utils.setprefb("buff_alert_activate", val);
        }
      }).a = Config.buff_alert_activate;
    y += 25;
    (new CheckBox(new Coord(0, y), this, "Activate Buff Alert Sound") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_sound_activate = val;
          Utils.setprefb("buff_alert_sound_activate", val);
        }
      }).a = Config.buff_alert_sound_activate;
    y += 25;
    (new CheckBox(new Coord(0, y), this, "Buff duration as pie chart") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_show_pie_chart_duration = val;
          Utils.setprefb("buff_alert_show_pie_chart_duration", val);
        }
      }).a = Config.buff_alert_show_pie_chart_duration;
    y += 25;
    (new CheckBox(new Coord(0, y), this, "Buff duration as line") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_show_line_duration = val;
          Utils.setprefb("buff_alert_show_line_duration", val);
        }
      }).a = Config.buff_alert_show_line_duration;
    y += 25;
    (new CheckBox(new Coord(0, y), this, "Buff duration as number") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_show_percent = val;
          Utils.setprefb("buff_alert_show_percent", val);
        }
      }).a = Config.buff_alert_show_line_duration;
    (new CheckBox(new Coord(150, y), this, "Bigger Duration-Number") {
        public void changed(boolean val) {
          super.changed(val);
          Config.buff_alert_bigger_percentage = val;
          Utils.setprefb("buff_alert_bigger_percentage", val);
          Iterator<Buff> i$ = this.ui.sess.glob.buffs.values().iterator();
          while (i$.hasNext()) {
            Buff b = i$.next();
            b.refreshCMeterTex();
          } 
        }
      }).a = Config.buff_alert_bigger_percentage;
    y += 50;
    new Label(new Coord(0, y), this, "percentage value for alert activation:");
    y += 25;
    new TextEntry(new Coord(0, y), 50, this, "" + Config.buff_alert_activation_value) {
        protected void changed() {
          if (this.text.length() > 0) {
            int value = 0;
            try {
              value = Integer.parseInt(this.text);
            } catch (Exception exception) {}
            if (value > 0) {
              Utils.setprefi("buff_alert_activation_value", value);
              Config.buff_alert_activation_value = value;
            } 
          } 
        }
      };
    y += 50;
    new Label(new Coord(0, y), this, "resource for buff alert audio:");
    y += 25;
    new TextEntry(new Coord(0, y), 300, this, Config.buff_alert_sound_resource) {
        protected void changed() {
          if (this.text.length() > 0) {
            Utils.setpref("buff_alert_sound_resource", this.text);
            Config.buff_alert_sound_resource = this.text;
            Bufflist.reloadAlarmSound(this.text);
          } 
        }
      };
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
