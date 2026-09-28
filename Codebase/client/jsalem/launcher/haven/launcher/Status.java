package haven.launcher;

public interface Status extends CommandHandler, AutoCloseable {
   ThreadLocal<Status> current = new ThreadLocal<>();
   Status dummy = new Status() {
      @Override
      public void message(String text) {
      }

      @Override
      public void transfer(long size, long cur) {
      }

      @Override
      public void progress() {
      }

      @Override
      public boolean command(String[] argv, Config cfg, Config.Environment env) {
         return false;
      }

      @Override
      public void error(Throwable exc) {
         exc.printStackTrace();
      }
   };

   void message(String var1);

   default void messagef(String fmt, Object... args) {
      this.message(String.format(fmt, args));
   }

   void transfer(long var1, long var3);

   void progress();

   void error(Throwable var1);

   default void launch(Launcher l) {
   }

   @Override
   default void close() {
   }

   default void dispose() {
   }

   static Status current() {
      Status ret = current.get();
      return ret == null ? dummy : ret;
   }

   static void use(Status st) {
      Status cur = current.get();
      current.set(st);
      if (cur != null) {
         cur.dispose();
      }
   }
}
