package haven.plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Button;
import haven.CheckBox;
import haven.Config;
import haven.Coord;
import haven.GOut;
import haven.Glob;
import haven.IBox;
import haven.IButton;
import haven.Listbox;
import haven.Resource;
import haven.Scrollport;
import haven.TextEntry;
import haven.UI;
import haven.Widget;
import haven.Window;
import haven.ChatUI.PrivChat;
import haven.ChatUI.Channel.Message;
import haven.ChatUI.PrivChat.OutMessage;
import haven.GameUI.MsgType;
import haven.Glob.Pagina;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public class ChatPlugin extends Plugin {
   public static boolean isRunning = false;
   public static boolean signalToStop = false;
   public static boolean INFO = false;
   public static UI ui = null;
   public static ChatPlugin.AMConfigWindow configfWindow = null;
   public static boolean useWindow = true;
   public static Map<String, Boolean> textMap = null;
   public static final String SPACE = " ";

   public void load(UI ui) {
      Glob glob = ui.sess.glob;
      Collection<Pagina> p = glob.paginae;
      p.add(glob.paginafor(Resource.load("paginae/add/chatplugin")));
      XTendedPaginae.registerPlugin("chatplugin", this);
   }

   public void execute(UI ui) {
      ChatPlugin.ui = ui;
      if (isRunning) {
         if (configfWindow != null && useWindow) {
            configfWindow.ui.destroy(configfWindow);
            configfWindow = null;
         } else {
            ui.message("[ChatPlugin] Stop Signal received.", MsgType.INFO);
            signalToStop = true;
         }
      } else {
         if (configfWindow == null && useWindow) {
            configfWindow = ChatPlugin.AMConfigWindow.getInstance(ui);
         }

         isRunning = true;
         ui.message("[ChatPlugin] ChatPlugin started.", MsgType.INFO);
         new Thread(new Runnable() {
            @Override
            public void run() {
               ChatPlugin.this.perfomTask();
            }
         }, "Chat Plugin").start();
      }
   }

   protected void perfomTask() {
      this.autoMessage();
      this.exitPlugin();
   }

   void autoMessage() {
      ArrayList<Integer> wdgIDList = new ArrayList<>();

      while (!signalToStop) {
         label109:
         for (Widget wdg = ui.gui.chat.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg instanceof PrivChat && !wdgIDList.contains(wdg.wdgid())) {
               wdgIDList.add(wdg.wdgid());
               PrivChat priv = (PrivChat)wdg;
               List<Message> msg = priv.msgs;
               if (msg.isEmpty()) {
                  continue;
               }

               for (Message mess : msg) {
                  if (mess instanceof OutMessage) {
                     continue label109;
                  }
               }

               ArrayList<String> textArray = new ArrayList<>();
               ArrayList<Character> numbers = new ArrayList<>();
               numbers.add("0".toCharArray()[0]);
               numbers.add("1".toCharArray()[0]);
               numbers.add("2".toCharArray()[0]);
               numbers.add("3".toCharArray()[0]);
               numbers.add("4".toCharArray()[0]);
               numbers.add("5".toCharArray()[0]);
               numbers.add("6".toCharArray()[0]);
               numbers.add("7".toCharArray()[0]);
               numbers.add("8".toCharArray()[0]);
               numbers.add("9".toCharArray()[0]);

               for (Entry<String, Boolean> entry : textMap.entrySet()) {
                  if (entry.getValue()) {
                     textArray.add(entry.getKey());
                  }
               }

               Collections.sort(textArray);
               boolean doSplit = true;
               boolean msgSent = false;

               for (String text : textArray) {
                  try {
                     if (text != null && text.length() > 0) {
                        if (text.length() > 2 && text.contains(" ")) {
                           String front = text.split(" ", 2)[0];

                           for (char oneChar : front.toCharArray()) {
                              if (!numbers.contains(oneChar)) {
                                 doSplit = false;
                                 break;
                              }
                           }
                        } else {
                           doSplit = false;
                        }

                        String msgToSend = null;
                        if (doSplit) {
                           msgToSend = text.split(" ", 2)[1];
                        } else {
                           msgToSend = text;
                        }

                        ByteBuffer byteBuffer = Charset.forName("Windows-1252").encode(msgToSend);
                        msgToSend = new String(byteBuffer.array(), Charset.forName("Windows-1252"));
                        priv.send(msgToSend);
                        msgSent = true;
                     }
                  } catch (Exception var17) {
                     UI.instance.message("Error while trying to send auto-message", MsgType.INFO);
                     UI.instance.message("Error reads: " + var17.getMessage(), MsgType.INFO);
                  }
               }

               if (msgSent) {
                  UI.instance.message("Sent auto message to: " + priv.name(), MsgType.INFO);
               }
            }

            try {
               Thread.sleep(200L);
            } catch (InterruptedException var16) {
            }
         }
      }
   }

   void testingChat() {
      ArrayList<Integer> wdgIDList = new ArrayList<>();

      for (Widget wdg = ui.gui.chat.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg instanceof PrivChat && !wdgIDList.contains(wdg.wdgid())) {
            PrivChat priv = (PrivChat)wdg;

            for (Message mess : priv.msgs) {
               if (mess instanceof OutMessage) {
                  OutMessage outMess = (OutMessage)mess;
                  UI.instance.message("outMessage: " + outMess.text().text, MsgType.INFO);
                  UI.instance.message("outMessage: " + outMess.tex().toString(), MsgType.INFO);
               }

               UI.instance.message("message: " + mess.toString(), MsgType.INFO);
               UI.instance.message("message: " + mess.tex(), MsgType.INFO);
               UI.instance.message("message: " + mess.text(), MsgType.INFO);
            }
         }

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var8) {
         }
      }
   }

   private void exitPlugin() {
      ui.message("[ChatPlugin] Exiting Plugin", MsgType.INFO);
      isRunning = false;
      signalToStop = false;
   }

   static class AMConfigWindow extends Window {
      Button add = null;
      TextEntry value = null;
      ChatPlugin.MyFlowerList list = null;

      public static ChatPlugin.AMConfigWindow getInstance(UI ui) {
         if (ChatPlugin.configfWindow == null || ChatPlugin.configfWindow.ui != ui) {
            ChatPlugin.configfWindow = new ChatPlugin.AMConfigWindow(new Coord(100, 100), Coord.z, UI.instance.gui, "Auto-Message-Plugin");
         }

         return ChatPlugin.configfWindow;
      }

      public void wdgmsg(Widget sender, String msg, Object... args) {
         if ((sender == this.add || sender == this.value) && msg.equals("activate")) {
            this.list.add(this.value.text);
            this.value.settext("");
         } else {
            super.wdgmsg(sender, msg, args);
         }

         if (sender == this.cbtn) {
            ChatPlugin.configfWindow = null;
         }

         super.wdgmsg(sender, msg, args);
      }

      public AMConfigWindow(Coord c, Coord sz, Widget parent, String cap) {
         super(new Coord(250, 100), new Coord(1000, 450), parent, cap);
         this.justclose = true;
         this.list = new ChatPlugin.MyFlowerList(new Coord(0, 0), this);
         this.add = new Button(new Coord(955, 420), 45, this, "Add");
         this.value = new TextEntry(new Coord(0, 420), 950, this, "");
         this.value.canactivate = true;
      }
   }

   public static class MyFlowerList extends Scrollport {
      private final IBox box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");

      public MyFlowerList(Coord c, Widget parent) {
         super(c, new Coord(990, 400), parent);
         int i = 0;

         for (Entry<String, Boolean> entry : ChatPlugin.textMap.entrySet()) {
            new ChatPlugin.MyFlowerList.Item(new Coord(0, 25 * i++), entry.getKey(), this.cont);
         }

         this.update();
      }

      public static void saveListToFile() {
         synchronized (ChatPlugin.textMap) {
            Gson gson = new GsonBuilder().create();
            saveFile("textmap.json", gson.toJson(ChatPlugin.textMap));
         }
      }

      public static void saveFile(String name, String data) {
         File file = getFile(name);
         boolean exists = file.exists();
         if (!exists) {
            try {
               new File(file.getParent()).mkdirs();
               exists = file.createNewFile();
            } catch (IOException var13) {
            }
         }

         if (exists && file.canWrite()) {
            PrintWriter out = null;

            try {
               out = new PrintWriter(file, "Windows-1252");
               out.print(data);
            } catch (FileNotFoundException var11) {
            } catch (UnsupportedEncodingException var12) {
               UI.instance.message("Error encoding to file: " + var12.getMessage(), MsgType.INFO);
            } finally {
               if (out != null) {
                  out.close();
               }
            }
         }
      }

      public static File getFile(String name) {
         return new File(Config.userhome, name);
      }

      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (msg.equals("changed")) {
            String name = (String)args[0];
            boolean val = (Boolean)args[1];
            synchronized (ChatPlugin.textMap) {
               ChatPlugin.textMap.put(name, val);
            }

            saveListToFile();
         } else if (msg.equals("delete")) {
            String name = (String)args[0];
            synchronized (ChatPlugin.textMap) {
               ChatPlugin.textMap.remove(name);
            }

            saveListToFile();
            this.ui.destroy(sender);
            this.update();
         } else {
            super.wdgmsg(sender, msg, args);
         }
      }

      public void add(String name) {
         if (name != null && !name.isEmpty() && !ChatPlugin.textMap.containsKey(name)) {
            synchronized (ChatPlugin.textMap) {
               ChatPlugin.textMap.put(name, true);
            }

            saveListToFile();
            new ChatPlugin.MyFlowerList.Item(new Coord(0, 0), name, this.cont);
            this.update();
         }
      }

      private void update() {
         LinkedList<String> order = new LinkedList<>(ChatPlugin.textMap.keySet());
         Collections.sort(order);

         for (Widget wdg = this.cont.lchild; wdg != null; wdg = wdg.prev) {
            int i = order.indexOf(((ChatPlugin.MyFlowerList.Item)wdg).name);
            wdg.c.y = 25 * i;
         }

         this.cont.update();
      }

      public void draw(GOut g) {
         super.draw(g);
         this.box.draw(g, Coord.z, this.sz);
      }

      public static String loadFile(String name) {
         InputStream inputStream = null;
         File file = Config.getFile(name);
         if (file.exists() && file.canRead()) {
            try {
               inputStream = new FileInputStream(file);
            } catch (FileNotFoundException var16) {
            }
         } else {
            inputStream = Config.class.getResourceAsStream("/" + name);
         }

         if (inputStream != null) {
            String var4;
            try {
               Scanner s = new Scanner(inputStream, "Windows-1252");
               if (!s.hasNextLine()) {
                  return null;
               }

               var4 = s.nextLine();
            } catch (Exception var17) {
               return null;
            } finally {
               try {
                  inputStream.close();
               } catch (IOException var15) {
               }
            }

            return var4;
         } else {
            return null;
         }
      }

      public static String stream2str(InputStream is) {
         Scanner s = new Scanner(is).useDelimiter("\\A");
         return s.hasNext() ? s.next() : "";
      }

      static {
         String json = loadFile("textmap.json");
         if (json != null) {
            try {
               Gson gson = new GsonBuilder().create();
               Type collectionType = (new TypeToken<HashMap<String, Boolean>>() {}).getType();
               ChatPlugin.textMap = (Map<String, Boolean>)gson.fromJson(json, collectionType);
            } catch (Exception var3) {
            }
         }

         if (ChatPlugin.textMap == null) {
            ChatPlugin.textMap = new HashMap<>();
            ChatPlugin.textMap.put("001 This is a tutorial text.", false);
            ChatPlugin.textMap.put("002 Numbers to the left, separated from the actual text by the first SPACE will not be sent", false);
            ChatPlugin.textMap.put("003 This way you can order your messages, but note that it is alphabetical sorting, not numerical", false);
            ChatPlugin.textMap.put("00333 As you can see, this number looks odd, but is compared to the others one number at a time,", false);
            ChatPlugin.textMap.put("00334 and not by the number as a whole, therefore \"00334\" is smaller than \"004\".", false);
            ChatPlugin.textMap.put("004 You can delete all these messages using the \"x\" to their right.", false);
            ChatPlugin.textMap.put("005 This list is actually SCROLL-able! Meaning you can put as many lines here as you want!", false);
            ChatPlugin.textMap.put("006 The checkbox to the left is meant to decide if one line is active.", false);
            ChatPlugin.textMap.put("007 If a line is not active, it will not be sent, so you can deactivate it without deleting it!", false);
            ChatPlugin.textMap.put("008 The following line is active, and the default afk message.", false);
            ChatPlugin.textMap.put("009 I am currently afk, I'll respond to you as soon as I am back! :-)", true);
            ChatPlugin.textMap.put("010 But ofc there are more uses than just afk messages, be creative! ;)", false);
            ChatPlugin.textMap.put("011 All these lines are saved in textmap.json in your Salem folder, in case you want to make a backup.", false);
            ChatPlugin.textMap.put("012 Click the plugin-icon or use the hotkey \"m\" to: turn on plugin | close this window | turn off plugin.", false);
            ChatPlugin.textMap.put("013 Just closing the window does not exit the plugin, a note in system chat will tell you when it did exit.", false);
            ChatPlugin.textMap.put("014 One last thing: Make sure to turn OFF this plugin before logging out, or the client may crash while doing so.", false);
         }
      }

      private static class Item extends Widget {
         public final String name;
         private final CheckBox cb;
         private boolean highlight = false;
         private boolean a = false;

         public Item(Coord c, String name, Widget parent) {
            super(c, new Coord(980, 25), parent);
            this.name = name;
            this.cb = new CheckBox(new Coord(3, 3), this, name);
            this.cb.a = ChatPlugin.textMap.get(name);
            this.cb.canactivate = true;
            new IButton(new Coord(958, 5), this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]);
         }

         public void draw(GOut g) {
            if (this.highlight) {
               g.chcolor(255, 255, 0, 128);
               g.poly2(
                  new Object[]{Coord.z, Listbox.selr, new Coord(0, this.sz.y), Listbox.selr, this.sz, Listbox.overr, new Coord(this.sz.x, 0), Listbox.overr}
               );
               g.chcolor();
            }

            super.draw(g);
         }

         public void mousemove(Coord c) {
            this.highlight = c.isect(Coord.z, this.sz);
            super.mousemove(c);
         }

         public boolean mousedown(Coord c, int button) {
            if (super.mousedown(c, button)) {
               return true;
            } else if (button != 1) {
               return false;
            } else {
               this.a = true;
               this.ui.grabmouse(this);
               return true;
            }
         }

         public boolean mouseup(Coord c, int button) {
            if (this.a && button == 1) {
               this.a = false;
               this.ui.grabmouse(null);
               if (c.isect(new Coord(0, 0), this.sz)) {
                  this.click();
               }

               return true;
            } else {
               return false;
            }
         }

         private void click() {
            this.cb.a = !this.cb.a;
            this.wdgmsg("changed", new Object[]{this.name, this.cb.a});
         }

         public void wdgmsg(Widget sender, String msg, Object... args) {
            if (msg.equals("ch")) {
               this.wdgmsg("changed", new Object[]{this.name, args[0]});
            } else if (msg.equals("activate")) {
               this.wdgmsg("delete", new Object[]{this.name});
            } else {
               super.wdgmsg(sender, msg, args);
            }
         }
      }
   }
}
