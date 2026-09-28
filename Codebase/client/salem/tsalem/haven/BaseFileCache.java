package haven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

public class BaseFileCache implements ResCache {
  public final URI id;
  
  private final Path base;
  
  public static Path findroot() {
    try {
      String path = System.getenv("APPDATA");
      if (path != null) {
        Path appdata = Utils.path(path);
        if (Files.exists(appdata, new java.nio.file.LinkOption[0]) && Files.isDirectory(appdata, new java.nio.file.LinkOption[0]) && Files.isReadable(appdata) && Files.isWritable(appdata)) {
          Path base = Utils.pj(appdata, new String[] { "Salem", "cache" });
          if (!Files.exists(base, new java.nio.file.LinkOption[0]))
            try {
              Files.createDirectories(base, (FileAttribute<?>[])new FileAttribute[0]);
            } catch (IOException e) {} 
          return base;
        } 
      } 
      path = System.getProperty("user.home", null);
      if (path != null) {
        Path home = Utils.path(path);
        if (Files.exists(home, new java.nio.file.LinkOption[0]) && Files.isDirectory(home, new java.nio.file.LinkOption[0]) && Files.isReadable(home) && Files.isWritable(home)) {
          Path base = Utils.pj(home, new String[] { ".salem", "cache" });
          if (!Files.exists(base, new java.nio.file.LinkOption[0]))
            try {
              Files.createDirectories(base, (FileAttribute<?>[])new FileAttribute[0]);
            } catch (IOException e) {} 
          return base;
        } 
      } 
    } catch (SecurityException securityException) {}
    throw new UnsupportedOperationException("Found no reasonable place to store local files");
  }
  
  public static Path findbase(URI id) throws IOException {
    String idstr = id.toString();
    int idhash = 0;
    for (int i = 0; i < idstr.length(); i++)
      idhash = idhash * 31 + idstr.charAt(i); 
    Path root = findroot();
    Path lfn = Utils.pj(root, new String[] { ".index-lock" });
    synchronized (BaseFileCache.class) {
      try (LockedFile lock = LockedFile.lock(lfn)) {} {
      
      } 
    }
    
    public BaseFileCache(URI id) throws IOException {
      this.id = id;
      this.base = findbase(id);
    }
    
    private static final Map<URI, BaseFileCache> current = new CacheMap<>();
    
    public static BaseFileCache get(URI id) throws IOException {
      synchronized (current) {
        BaseFileCache ret = current.get(id);
        if (ret == null)
          current.put(id, ret = new BaseFileCache(id)); 
        return ret;
      } 
    }
    
    private static URI mkurn(String id) {
      return Utils.uri("urn:haven-cache:" + id);
    }
    
    public static BaseFileCache get(String id) throws IOException {
      return get(mkurn(id));
    }
    
    public static BaseFileCache create() {
      try {
        if (Config.cachebase != null)
          return get(Config.cachebase); 
        if (Config.resurl != null)
          return get(Config.resurl.toURI()); 
        return get("default");
      } catch (Exception e) {
        return null;
      } 
    }
    
    private String mangle(String el) {
      if (Resource.FileSource.windows) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < el.length(); i++) {
          char c = el.charAt(i);
          if (c != '@' && Resource.FileSource.winsafechar(c)) {
            buf.append(c);
          } else {
            buf.append('@');
            buf.append(Utils.num2hex((c & 0xF000) >> 12));
            buf.append(Utils.num2hex((c & 0xF00) >> 8));
            buf.append(Utils.num2hex((c & 0xF0) >> 4));
            buf.append(Utils.num2hex((c & 0xF) >> 0));
          } 
        } 
        el = buf.toString();
      } 
      if (Resource.FileSource.windows && (el.startsWith("windows-special-") || Resource.FileSource.wintraps.contains(el)))
        return "windows-special-" + el; 
      return el;
    }
    
    private Path forres(String nm) {
      Path res = this.base;
      String[] comp = nm.split("/");
      for (int i = 0; i < comp.length - 1; i++)
        res = res.resolve(mangle(comp[i])); 
      return res.resolve(comp[comp.length - 1] + ".cached");
    }
    
    public InputStream fetch(String name) throws IOException {
      try {
        return Files.newInputStream(forres(name), new java.nio.file.OpenOption[0]);
      } catch (NoSuchFileException e) {
        throw (FileNotFoundException)(new FileNotFoundException(name)).initCause(e);
      } 
    }
    
    public OutputStream store(String name) throws IOException {
      final Path path = forres(name);
      Path dir = path.getParent();
      if (!Files.exists(dir, new java.nio.file.LinkOption[0]))
        Files.createDirectories(dir, (FileAttribute<?>[])new FileAttribute[0]); 
      final Path tmp = Files.createTempFile(dir, "cache", ".new", (FileAttribute<?>[])new FileAttribute[0]);
      final OutputStream fp = Files.newOutputStream(tmp, new java.nio.file.OpenOption[0]);
      return new OutputStream() {
          private boolean closed = false;
          
          public void write(int b) throws IOException {
            fp.write(b);
          }
          
          public void write(byte[] buf, int off, int len) throws IOException {
            fp.write(buf, off, len);
          }
          
          public void close() throws IOException {
            fp.close();
            try {
              Files.move(tmp, path, new CopyOption[] { StandardCopyOption.ATOMIC_MOVE });
            } catch (AtomicMoveNotSupportedException e) {
              Files.move(tmp, path, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
            } 
            this.closed = true;
          }
          
          protected void finalize() {
            if (!this.closed)
              try {
                fp.close();
                Files.delete(tmp);
              } catch (IOException iOException) {} 
          }
        };
    }
    
    public void remove(String name) throws IOException {
      try {
        Files.delete(forres(name));
      } catch (NoSuchFileException e) {
        throw (FileNotFoundException)(new FileNotFoundException(name)).initCause(e);
      } 
    }
    
    public String toString() {
      return "BaseFileCache(" + this.base + ")";
    }
  }
