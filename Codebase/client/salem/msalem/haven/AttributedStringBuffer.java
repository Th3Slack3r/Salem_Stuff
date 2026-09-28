package haven;

import java.io.PrintStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

public class AttributedStringBuffer {
   private AttributedString current = new AttributedString("");

   public static String gettext(AttributedCharacterIterator s) {
      StringBuilder tbuf = new StringBuilder();

      for (int i = s.getBeginIndex(); i < s.getEndIndex(); i++) {
         tbuf.append(s.setIndex(i));
      }

      return tbuf.toString();
   }

   public static void dump(AttributedCharacterIterator s, PrintStream out) {
      int cl = 0;
      Map<? extends Attribute, ?> attrs = null;

      for (int i = s.getBeginIndex(); i < s.getEndIndex(); i++) {
         char c = s.setIndex(i);
         if (i >= cl) {
            attrs = s.getAttributes();
            out.println();
            out.println(attrs);
            cl = s.getRunLimit();
         }

         out.print(c);
      }

      out.println();
   }

   public static AttributedString concat(AttributedCharacterIterator... strings) {
      StringBuilder tbuf = new StringBuilder();

      for (int i = 0; i < strings.length; i++) {
         AttributedCharacterIterator s = strings[i];

         for (int o = s.getBeginIndex(); o < s.getEndIndex(); o++) {
            tbuf.append(s.setIndex(o));
         }
      }

      AttributedString res = new AttributedString(tbuf.toString());
      int ro = 0;

      for (int i = 0; i < strings.length; i++) {
         AttributedCharacterIterator s = strings[i];
         int o = s.getBeginIndex();

         while (o < s.getEndIndex()) {
            s.setIndex(o);
            int n = s.getRunLimit();
            int l = n - o;
            res.addAttributes(s.getAttributes(), ro, ro + l);
            o = n;
            ro += l;
         }
      }

      return res;
   }

   public static AttributedString concat(AttributedString... strings) {
      AttributedCharacterIterator[] its = new AttributedCharacterIterator[strings.length];

      for (int i = 0; i < strings.length; i++) {
         its[i] = strings[i].getIterator();
      }

      return concat(its);
   }

   public void append(AttributedString string) {
      this.current = concat(this.current, string);
   }

   public void append(String string, Map<? extends Attribute, ?> attrs) {
      this.append(new AttributedString(string, attrs));
   }

   public AttributedString result() {
      return this.current;
   }
}
