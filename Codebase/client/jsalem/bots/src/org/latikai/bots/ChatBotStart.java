package org.latikai.bots;

import haven.ChatUI;
import haven.ChatUI.EntryChannel;
import haven.GameUI;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "chat",
   step = "start"
)
public class ChatBotStart extends BotState {
   private int step = 0;
   private java.util.Map<ChatUI.Channel, Integer> seenCounts = new java.util.HashMap<>();
   private long lastReply = 0;

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      ChatUI chat = ui.gui.chat;
      if (chat == null) return null;

      if (step == 0) {
      EntryChannel ec = findEntryChannel(chat);
      if (ec != null) {
         ec.send("TownToon Logged In!");
         ui.message("[Chat] Sent welcome", GameUI.MsgType.INFO);
      }
      step = 1;
      bot.botSleep(5000);
      return null;
      }

      try {
         java.lang.reflect.Field msgsField = ChatUI.Channel.class.getDeclaredField("msgs");
         msgsField.setAccessible(true);

         Widget wdg = chat.child;
         while (wdg != null) {
            if (wdg instanceof ChatUI.Channel) {
               ChatUI.Channel ch = (ChatUI.Channel)wdg;
               java.util.List<?> msgs = (java.util.List<?>)msgsField.get(ch);
               int prev = seenCounts.getOrDefault(ch, 0);
               if (msgs.size() > prev) {
                  seenCounts.put(ch, msgs.size());
                  for (int i = prev; i < msgs.size(); i++) {
                     Object msg = msgs.get(i);
                     java.lang.reflect.Method textMethod = msg.getClass().getMethod("text");
                     haven.Text t = (haven.Text)textMethod.invoke(msg);
                     ui.message("[Chat] " + t.text, GameUI.MsgType.INFO);
                     long now = System.currentTimeMillis();
                     if (t.text.toLowerCase().contains("hello") && now - lastReply > 10000) {
                        EntryChannel ec = findEntryChannel(chat);
                        if (ec != null) { ec.send("hello"); lastReply = now; ui.message("[Chat] Responded", GameUI.MsgType.INFO); }
                     }
                  }
               }
            }
            wdg = wdg.next;
         }
      } catch (Exception e) {
         ui.message("[Chat] Read error: " + e.getMessage(), GameUI.MsgType.ERROR);
      }
      bot.botSleep(2000);
      return null;
   }

   private EntryChannel findEntryChannel(ChatUI chat) {
      Widget wdg = chat.child;
      while (wdg != null) {
         if (wdg instanceof EntryChannel) return (EntryChannel)wdg;
         wdg = wdg.next;
      }
      return null;
   }
}
