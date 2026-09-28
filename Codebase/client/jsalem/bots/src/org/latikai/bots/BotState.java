package org.latikai.bots;

import haven.UI;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

   public static final List<String> getBotNames() {
      List<String> names = new ArrayList<>();
      try {
         Iterable<Class> classes = getClasses("org.latikai.bots");
         for (Class<? extends BotState> next : classes) {
            BotAnnotation a = next.getAnnotation(BotAnnotation.class);
            if (a != null && !names.contains(a.bot())) {
               names.add(a.bot());
            }
         }
      } catch (IOException | ClassNotFoundException e) {
      }
      Sort(names);
      return names;
   }

   private static void Sort(java.util.List<String> list) {
      java.util.Collections.sort(list);
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
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      Enumeration<URL> roots = cl.getResources(path);

      while (roots.hasMoreElements()) {
         URL root = roots.nextElement();
         String rootStr = root.toString();

         if (rootStr.startsWith("jar:")) {
            String jarPath = rootStr.substring(4, rootStr.indexOf("!/"));
            if (jarPath.startsWith("file:"))
               jarPath = jarPath.substring(5);
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            try (JarFile jar = new JarFile(jarPath)) {
               Enumeration<JarEntry> entries = jar.entries();
               while (entries.hasMoreElements()) {
                  JarEntry entry = entries.nextElement();
                  String name = entry.getName();
                  if (name.startsWith(path) && name.endsWith(".class")) {
                     String cn = name.substring(0, name.length() - 6).replace('/', '.');
                     classes.add(Class.forName(cn, false, cl));
                  }
               }
            }
         } else if (rootStr.startsWith("file:")) {
            try {
               File dir = new File(root.toURI());
               scanDir(dir, dir, packageName, classes, cl);
            } catch (java.net.URISyntaxException e) {
            }
         }
      }

      return classes;
   }

   private static void scanDir(File base, File dir, String pkg, ArrayList<Class> classes, ClassLoader cl) {
      File[] files = dir.listFiles();
      if (files == null) return;
      for (File f : files) {
         if (f.isDirectory()) {
            scanDir(base, f, pkg, classes, cl);
         } else if (f.getName().endsWith(".class")) {
            String rel = base.toURI().relativize(f.toURI()).getPath();
            String cn = rel.substring(0, rel.length() - 6).replace('/', '.');
            try {
               classes.add(Class.forName(cn, false, cl));
            } catch (ClassNotFoundException e) {
            }
         }
      }
   }
}
