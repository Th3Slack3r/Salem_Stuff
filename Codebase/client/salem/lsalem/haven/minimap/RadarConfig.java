package haven.minimap;

import haven.Config;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RadarConfig {
   public static final String def_config = Config.userhome + "/radar.xml";
   private final File file;
   private List<ConfigGroup> groups = new ArrayList<>();

   public RadarConfig(String configfile) {
      this(new File(configfile));
   }

   public RadarConfig(File configfile) {
      this.file = configfile;
      if (!this.file.exists()) {
         try {
            FileOutputStream out = new FileOutputStream(this.file);
            InputStream in = RadarConfig.class.getResourceAsStream("/radar.xml");
            int k = 512;
            byte[] b = new byte[512];

            while (k > 0) {
               k = in.read(b, 0, 512);
               if (k > 0) {
                  out.write(b, 0, k);
               }
            }

            out.close();
            in.close();
         } catch (FileNotFoundException var7) {
         } catch (IOException var8) {
         }
      }

      try {
         this.load();
      } catch (Exception var6) {
         System.out.println(var6.getMessage());
      }
   }

   public RadarConfig() {
      this(new File(def_config));
   }

   public Iterable<ConfigGroup> getGroups() {
      return this.groups;
   }

   private void load() throws Exception {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(this.file);
      this.groups.clear();
      NodeList groupNodes = doc.getElementsByTagName("group");

      for (int i = 0; i < groupNodes.getLength(); i++) {
         ConfigGroup g = parseGroup(groupNodes.item(i));
         if (g != null) {
            this.groups.add(g);
         }
      }
   }

   private static ConfigGroup parseGroup(Node node) {
      if (node.getNodeType() != 1) {
         return null;
      } else {
         Element el = (Element)node;
         ConfigGroup group = new ConfigGroup();
         group.name = el.getAttribute("name");
         group.show = !el.getAttribute("show").equals("false");
         group.color = Utils.parseColor(el.getAttribute("color"));
         group.shape = Marker.Shape.get(el.getAttribute("shape"));
         group.order = el.hasAttribute("order") ? Integer.parseInt(el.getAttribute("order")) : 0;
         group.showicon = el.hasAttribute("showicon") ? el.getAttribute("showicon").equals("true") : true;
         NodeList markerNodes = el.getElementsByTagName("marker");

         for (int i = 0; i < markerNodes.getLength(); i++) {
            ConfigMarker m = parseMarker(group, markerNodes.item(i));
            if (m != null) {
               group.markers.add(m);
            }
         }

         return group;
      }
   }

   private static ConfigMarker parseMarker(ConfigGroup group, Node node) {
      if (node.getNodeType() != 1) {
         return null;
      } else {
         Element el = (Element)node;
         ConfigMarker marker = new ConfigMarker();
         marker.text = el.getAttribute("text");
         marker.show = el.hasAttribute("show") ? el.getAttribute("show").equals("true") : group.show;
         marker.tooltip = el.hasAttribute("tooltip") && el.getAttribute("tooltip").equals("true");
         marker.shape = el.hasAttribute("shape") ? Marker.Shape.get(el.getAttribute("shape")) : group.shape;
         marker.order = el.hasAttribute("order") ? Integer.parseInt(el.getAttribute("order")) : group.order;
         marker.showicon = el.hasAttribute("showicon") ? el.getAttribute("showicon").equals("true") : group.showicon;
         if (el.hasAttribute("match")) {
            marker.match = el.getAttribute("match");
         } else if (el.hasAttribute("pattern")) {
            marker.match = el.getAttribute("pattern");
            marker.ispattern = true;
         }

         Color c = Utils.parseColor(el.getAttribute("color"));
         if (c == null) {
            c = group.color;
         }

         if (c == null) {
            c = Color.WHITE;
         }

         marker.color = c;
         return marker;
      }
   }
}
