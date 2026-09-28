package org.latikai.bots;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiScraper {
   private static final String USER_AGENT = "jsalemBot/1.0";
   private static final String WAYBACK = "https://web.archive.org/web/20260119061830/";
   private static final Pattern OBJECTS_REQUIRED = Pattern.compile("\\|\\s*Objects required\\s*=\\s*(.*?)(?:\\n|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

   public static String fetchSkill(String skillName) {
      try {
         String wikiName = skillName.trim().replaceAll("\\s+", "_");
         String live = "https://salemthegame.wiki/page/" + URLEncoder.encode(wikiName, "UTF-8");
         String wayback = WAYBACK + "https://salemthegame.wiki/page/" + URLEncoder.encode(wikiName, "UTF-8");

         String html = fetchUrl(live);
         if (html == null) html = fetchUrl(wayback);
         if (html == null) return null;

         String text = html.toString();
         StringBuilder result = new StringBuilder();
         // Extract the description from <td ...>"..." pattern
         java.util.regex.Matcher dm = Pattern.compile("center\">\"([^\"]+)\"", Pattern.DOTALL).matcher(text);
         if (dm.find()) {
            result.append(dm.group(1).trim());
         }
         // Extract Requirements
         java.util.regex.Matcher rm = Pattern.compile("Skill\\(s\\) required[^<]*<[^>]*>[^<]*<[^>]*>([^<]+)", Pattern.CASE_INSENSITIVE).matcher(text);
         if (rm.find()) {
            String req = rm.group(1).trim().replaceAll("\\s+", " ").replaceAll("\\s+$", "");
            if (!req.isEmpty()) result.append(". Requires: ").append(req);
         }
         String r = result.toString().trim();
         return r.isEmpty() ? null : r;

      } catch (java.net.ConnectException e) {
         System.out.println("[Wiki] Connection failed");
      } catch (Exception e) {
         System.out.println("[Wiki] Error: " + e.getMessage());
      }
      return null;
   }

   private static String fetchUrl(String url) throws Exception {
      HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", USER_AGENT);
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(15000);
      if (conn.getResponseCode() != 200) return null;
      StringBuilder html = new StringBuilder();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
         String line; while ((line = br.readLine()) != null) html.append(line).append("\n");
      }
      return html.toString();
   }

   public static String fetchRecipe(String itemName) {
      try {
         String wikiName = itemName.trim().replaceAll("\\s+", "_");
         String url = "https://salemthegame.wiki/index.php?title=" + URLEncoder.encode(wikiName, "UTF-8") + "&action=edit";
         String wayback = WAYBACK + "https://salemthegame.wiki/index.php?title=" + URLEncoder.encode(wikiName, "UTF-8") + "&action=edit";

         String html = fetchUrl(url);
         if (html == null) html = fetchUrl(wayback);
         if (html == null) return null;

         Matcher m = OBJECTS_REQUIRED.matcher(html);
         if (!m.find()) return null;

         String raw = m.group(1).trim();
         StringBuilder result = new StringBuilder("Requires: ");
         for (String part : raw.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            String[] pieces = part.split(";");
            if (pieces.length == 2) {
               result.append(pieces[1].trim()).append("x ").append(pieces[0].trim()).append(", ");
            } else {
               result.append("1x ").append(pieces[0].trim()).append(", ");
            }
         }
         return result.toString().replaceAll(", $", "");

      } catch (java.net.ConnectException e) {
         System.out.println("[Wiki] Connection failed");
      } catch (java.net.SocketTimeoutException e) {
         System.out.println("[Wiki] Timeout");
      } catch (Exception e) {
         System.out.println("[Wiki] Error: " + e.getMessage());
      }
      return null;
   }
}
