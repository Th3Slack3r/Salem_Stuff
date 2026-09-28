package haven;

public class Loading extends RuntimeException {
   public Loading() {
   }

   public Loading(String msg) {
      super(msg);
   }

   public Loading(Throwable cause) {
      super(cause);
   }

   public Loading(String msg, Throwable cause) {
      super(msg, cause);
   }

   public boolean canwait() {
      return false;
   }

   public void waitfor() throws InterruptedException {
      throw new RuntimeException("Tried to wait for unwaitable event", this);
   }
}
