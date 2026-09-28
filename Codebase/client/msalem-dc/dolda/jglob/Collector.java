package dolda.jglob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes({"*"})
public class Collector extends AbstractProcessor {
   private ProcessingEnvironment cfg;
   private Elements eu;
   private boolean verbose = false;

   @Override
   public void init(ProcessingEnvironment cfg) {
      this.cfg = cfg;
      this.eu = cfg.getElementUtils();
   }

   private String tn(TypeElement el) {
      return this.eu.getBinaryName(el).toString();
   }

   private Set<String> getprev(TypeElement annotation) {
      Set<String> prev = new HashSet<>();

      try {
         FileObject lf = this.cfg.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/glob/" + this.tn(annotation));

         InputStream in;
         try {
            in = lf.openInputStream();
         } catch (NoSuchFileException | FileNotFoundException var12) {
            return prev;
         }

         Object var7;
         try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));

            String ln;
            while ((ln = r.readLine()) != null) {
               prev.add(ln);
            }

            var7 = prev;
         } finally {
            in.close();
         }

         return (Set<String>)var7;
      } catch (IOException var14) {
         this.cfg.getMessager().printMessage(Kind.ERROR, "could not read previous globlist for " + this.tn(annotation) + ": " + var14);
         return Collections.emptySet();
      }
   }

   private void writenew(TypeElement annotation, Collection<String> names) {
      try {
         FileObject lf = this.cfg.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/glob/" + this.tn(annotation));
         OutputStream out = lf.openOutputStream();

         try {
            Writer w = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));

            for (String nm : names) {
               w.write(nm + "\n");
            }

            w.flush();
         } finally {
            out.close();
         }
      } catch (IOException var12) {
         this.cfg.getMessager().printMessage(Kind.ERROR, "could not write new globlist for " + this.tn(annotation) + ": " + var12);
      }
   }

   private void process(TypeElement annotation, RoundEnvironment round, TypeMap types) {
      Set<String> prev = this.getprev(annotation);
      Set<String> carry = new HashSet<>(prev);
      Set<String> found = new HashSet<>();

      for (Element e : round.getElementsAnnotatedWith(annotation)) {
         if (!(e instanceof TypeElement)) {
            this.cfg.getMessager().printMessage(Kind.ERROR, this.tn(annotation) + " must annotate types", e);
         } else {
            TypeElement type = (TypeElement)e;
            String nm = this.tn(type);
            if (!prev.contains(nm) && this.verbose) {
               this.cfg.getMessager().printMessage(Kind.NOTE, "added " + nm, type);
            }

            found.add(nm);
            carry.remove(nm);
         }
      }

      Iterator<String> i = carry.iterator();

      while (i.hasNext()) {
         String nm = i.next();
         TypeElement el = types.get(nm);
         if (el != null) {
            i.remove();
            if (this.verbose) {
               this.cfg.getMessager().printMessage(Kind.NOTE, "removed " + nm, el);
            }
         }
      }

      List<String> all = new ArrayList<>();
      all.addAll(carry);
      all.addAll(found);
      Collections.sort(all);
      this.writenew(annotation, all);
   }

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
      for (TypeElement a : annotations) {
         if (a.getAnnotation(Discoverable.class) != null) {
            this.process(a, round, new TypeMap(round.getRootElements(), this.eu));
         }
      }

      return false;
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latest();
   }
}
