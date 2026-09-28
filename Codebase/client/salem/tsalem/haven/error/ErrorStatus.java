package haven.error;

public interface ErrorStatus {
  boolean goterror(Throwable paramThrowable);
  
  void connecting();
  
  void sending();
  
  void done(String paramString1, String paramString2);
  
  void senderror(Exception paramException);
  
  public static class Simple implements ErrorStatus {
    public boolean goterror(Throwable t) {
      System.err.println("Caught error: " + t);
      return true;
    }
    
    public void connecting() {
      System.err.println("Connecting to error server");
    }
    
    public void sending() {
      System.err.println("Sending error");
    }
    
    public void done(String ctype, String info) {
      if (ctype != null) {
        System.err.println(ctype + ": " + info);
      } else {
        System.err.println("Done");
      } 
    }
    
    public void senderror(Exception e) {
      System.err.println("Error while sending error:");
      e.printStackTrace(System.err);
    }
  }
}
