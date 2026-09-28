package dolda.jglob;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;

public class TypeMap {
   private final Map<String, TypeElement> types;

   public TypeMap(Collection<? extends Element> roots, final Elements eu) {
      final Map<String, TypeElement> types = new HashMap<>();
      ElementVisitor<Void, Void> v = new ElementScanner6<Void, Void>() {
         public Void visitType(TypeElement el, Void p) {
            if (types.put(eu.getBinaryName(el).toString(), el) != null) {
               throw new RuntimeException("type name conflict: " + eu.getBinaryName(el));
            } else {
               return (Void)super.visitType(el, p);
            }
         }
      };

      for (Element el : roots) {
         v.visit(el);
      }

      this.types = types;
   }

   public TypeElement get(String name) {
      return this.types.get(name);
   }
}
