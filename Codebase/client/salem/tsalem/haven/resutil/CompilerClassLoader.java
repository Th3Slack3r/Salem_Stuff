package haven.resutil;

import haven.Config;
import haven.Resource;
import haven.Utils;

public class CompilerClassLoader extends ClassLoader {
  private final Resource[] useres;
  
  static {
    Config.nopreload = true;
  }
  
  public CompilerClassLoader(ClassLoader parent) {
    super(parent);
    String[] useresnm = Utils.getprop("haven.resutil.classloader.useres", null).split(":");
    this.useres = new Resource[useresnm.length];
    for (int i = 0; i < useresnm.length; i++)
      this.useres[i] = Resource.load(useresnm[i]); 
  }
  
  public Class<?> findClass(String name) throws ClassNotFoundException {
    for (Resource res : this.useres) {
      res.loadwait();
      try {
        return ((Resource.CodeEntry)res.layer(Resource.CodeEntry.class)).loader(true).loadClass(name);
      } catch (ClassNotFoundException classNotFoundException) {}
    } 
    throw new ClassNotFoundException(name + " was not found in any of the requested resources.");
  }
}
