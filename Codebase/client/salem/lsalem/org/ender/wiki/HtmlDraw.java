package org.ender.wiki;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HtmlDraw {
   private static final Color BG_NULL = new Color(0, 0, 0, 0);
   private static final Color BG_COL = new Color(200, 200, 200, 200);
   private JTextPane htmlpane;
   private HyperlinkListener links;

   public HtmlDraw(String text, HyperlinkListener links) {
      this.links = links;
      if (text == null) {
         text = "<H1>Nothing found, sorry :(</H1>";
      }

      text = text.replaceAll("=\"/", "=\"http://salemwiki.info/");
      Pattern rows = Pattern.compile("<tr style=\"background:(.*?);(.*?)\">(.*?)</tr>", 40);
      Matcher mr = rows.matcher(text);

      for (int i = 0; mr.find(i); mr = rows.matcher(text)) {
         String col = mr.group(1);
         col = col.replace("darkred", "#8b0000");
         col = col.replace("darkgreen", "#006400");
         col = col.replace("darkblue", "#00008b");
         col = col.replace("darkgoldenrod", "#b8860b");
         String tmp = mr.group(2);
         String cols = mr.group(3);
         cols = cols.replaceAll("<td>", String.format("<td style=\"background-color: %s;\">", col));
         tmp = String.format("<TR style=\"background:%s;%s\">%s</TR>", col, tmp, cols);
         i = mr.start() + tmp.length() - 1;
         text = mr.replaceFirst(tmp);
      }

      this.htmlpane = new JTextPane();
      this.htmlpane.setEditable(false);
      this.htmlpane.setBackground(BG_NULL);
      this.htmlpane.setOpaque(false);
      HTMLEditorKit kit = new HTMLEditorKit();
      StyleSheet ss = kit.getStyleSheet();
      ss.addRule(".searchmatch {color: green; font: bold}");
      this.htmlpane.setEditorKit(kit);
      HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();
      this.htmlpane.setDocument(doc);
      this.htmlpane.setText(text);
      this.htmlpane.setSize(this.htmlpane.getPreferredSize());
      layoutComponent(this.htmlpane);
      this.htmlpane.addHyperlinkListener(links);
   }

   public void setWidth(int w) {
      if (this.htmlpane != null) {
         Dimension d = this.htmlpane.getSize();
         d.width = w;
         this.htmlpane.setSize(d);
         d = this.htmlpane.getPreferredScrollableViewportSize();
         this.htmlpane.setSize(w, (int)d.getHeight());
      }
   }

   public int getHeight() {
      return this.htmlpane.getHeight();
   }

   public static void doClickInRectangle(Component component, int x, int y, boolean useRightClick, int keyMods) {
      int modifiers = useRightClick ? 4 : 16;
      modifiers |= keyMods;
      int nbClicks = 1;
      component.dispatchEvent(new MouseEvent(component, 500, 1L, modifiers, x, y, 1, useRightClick));
   }

   public static BufferedImage createImage(JComponent component) {
      Dimension d = component.getSize();
      if (d.width == 0 || d.height == 0) {
         d = component.getPreferredSize();
         component.setSize(d);
      }

      Rectangle region = new Rectangle(0, 0, d.width, d.height);
      return createImage(component, region);
   }

   public static BufferedImage createImage(JComponent component, Rectangle region) {
      BufferedImage image = new BufferedImage(region.width, region.height, 2);
      return createImage(component, region, image);
   }

   public static BufferedImage createImage(JComponent component, Rectangle region, BufferedImage image) {
      if (!component.isDisplayable()) {
         Dimension d = component.getSize();
         if (d.width == 0 || d.height == 0) {
            d = component.getPreferredSize();
            component.setSize(d);
         }

         layoutComponent(component);
      }

      Graphics2D g2d = image.createGraphics();
      if (!component.isOpaque()) {
         g2d.setColor(BG_COL);
         g2d.fillRect(region.x, region.y, region.width, region.height);
      }

      g2d.translate(-region.x, -region.y);
      component.paint(g2d);
      g2d.dispose();
      return image;
   }

   public void get(BufferedImage image, int w, int h) {
      createImage(this.htmlpane, new Rectangle(w, h), image);
   }

   static void layoutComponent(Component component) {
      synchronized (component.getTreeLock()) {
         component.doLayout();
         if (component instanceof Container) {
            for (Component child : ((Container)component).getComponents()) {
               layoutComponent(child);
            }
         }
      }
   }

   public void mouseup(int x, int y, int button) {
      doClickInRectangle(this.htmlpane, x, y, button != 1, 0);
   }

   public void destroy() {
      this.htmlpane.removeHyperlinkListener(this.links);
      this.links = null;
   }
}
