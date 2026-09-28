package haven.error;

public interface ErrorStatus {
   boolean goterror(Throwable var1);

   void connecting();

   void sending();

   void done(String var1, String var2);

   void senderror(Exception var1);

   public static class Simple implements ErrorStatus {
      @Override
      public boolean goterror(Throwable t) {
         System.err.println("Caught error: " + t);
         return true;
      }

      @Override
      public void connecting() {
         System.err.println("Connecting to error server");
      }

      @Override
      public void sending() {
         System.err.println("Sending error");
      }

      @Override
      public void done(String ctype, String info) {
         if (ctype != null) {
            System.err.println(ctype + ": " + info);
         } else {
            System.err.println("Done");
         }
      }

      @Override
      public void senderror(Exception e) {
         System.err.println("Error while sending error:");
         e.printStackTrace(System.err);
      }
   }
}
