package haven.launcher;

public interface ErrorMessage {
   String usermessage();

   static ErrorMessage get(Throwable exc) {
      for (Throwable t = exc; t != null; t = t.getCause()) {
         if (t instanceof ErrorMessage) {
            return (ErrorMessage)t;
         }
      }

      return null;
   }

   static String getmessage(Throwable exc) {
      ErrorMessage msg = get(exc);
      return msg == null ? null : msg.usermessage();
   }
}
