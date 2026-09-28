package org.json;

public class JSONException extends Exception {
   private static final long serialVersionUID = 0L;
   private Throwable cause;

   public JSONException(String message) {
      super(message);
   }

   public JSONException(Throwable cause) {
      super(cause.getMessage());
      this.cause = cause;
   }

   @Override
   public Throwable getCause() {
      return this.cause;
   }
}
