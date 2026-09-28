package haven.launcher;

import java.util.Arrays;

public interface Validator {
   void validate(Cached var1) throws ValidationException;

   static Validator parse(String spec) {
      int p = spec.indexOf(58);
      String type;
      String arg;
      if (p < 0) {
         type = spec;
         arg = "";
      } else {
         type = spec.substring(0, p);
         arg = spec.substring(p + 1);
      }

      switch (type) {
         case "tls-cert":
            return new Validator.TlsKeyValidator(arg);
         case "jar-cert":
            return new Validator.JarKeyValidator(arg);
         case "always":
            return file -> {};
         default:
            return null;
      }
   }

   public static class JarKeyValidator implements Validator {
      final String key;

      JarKeyValidator(String key) {
         this.key = key;
      }

      @Override
      public void validate(Cached file) {
         if (!file.props.containsKey("jar-certs") || !Arrays.asList(((String)file.props.get("jar-certs")).split(" ")).contains(this.key)) {
            throw new ValidationException("Jar file not signed with " + this.key);
         }
      }
   }

   public static class TlsKeyValidator implements Validator {
      final String key;

      TlsKeyValidator(String key) {
         this.key = key;
      }

      @Override
      public void validate(Cached file) {
         if (!file.props.containsKey("tls-certs") || !Arrays.asList(((String)file.props.get("tls-certs")).split(" ")).contains(this.key)) {
            throw new ValidationException("file not downloaded over tls connection signed with " + this.key);
         }
      }
   }
}
