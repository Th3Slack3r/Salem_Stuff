package org.latikai.bots;

import haven.UI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class BotState {
   public abstract Stack<BotState> update(UI var1, Bot var2);

   public static final Stack<BotState> initializeStack(String bot, String step) {
      Stack<BotState> new_stack = new Stack<>();
      BotState constructed = getBotState(bot, step);
      if (constructed != null) {
         new_stack.push(constructed);
         return new_stack;
      } else {
         System.out.println("Botting error! Bot references an unexisting step (" + bot + "," + step + ").");
         return null;
      }
   }

   public static final BotState getBotState(String bot, String step) {
      Iterable<Class> classes;
      try {
         classes = getClasses("org.latikai.bots");
      } catch (IOException | ClassNotFoundException var7) {
         return null;
      }

      for (Class<? extends BotState> next : classes) {
         BotAnnotation a = next.getAnnotation(BotAnnotation.class);
         if (a != null && a.bot().equals(bot) && a.step().equals(step)) {
            try {
               return next.getConstructor().newInstance();
            } catch (Exception var8) {
               var8.printStackTrace();
            }
         }
      }

      return null;
   }

   private static Iterable<Class> getClasses(String packageName) throws IOException, ClassNotFoundException {
      ArrayList<Class> classes = new ArrayList<>();
      JarFile jarFile = new JarFile("lsalem.jar");
      Enumeration<JarEntry> entries = jarFile.entries();
      JarEntry jarEntry = null;

      while (entries.hasMoreElements() && (jarEntry = entries.nextElement()) != null) {
         String name = jarEntry.getName();
         if (name.contains(".class")) {
            name = name.substring(0, name.length() - 6).replace('/', '.');
            if (name.contains(packageName)) {
               classes.add(Class.forName(name));
            }
         }
      }

      return classes;
   }
}
