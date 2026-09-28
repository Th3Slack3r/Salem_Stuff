package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Party {
  Map<Long, Member> memb = new TreeMap<>();
  
  Member leader = null;
  
  public static final int PD_LIST = 0;
  
  public static final int PD_LEADER = 1;
  
  public static final int PD_MEMBER = 2;
  
  private final Glob glob;
  
  public Party(Glob glob) {
    this.glob = glob;
  }
  
  public class Member {
    long gobid;
    
    private Coord c = null;
    
    Color col = Color.BLACK;
    
    public Gob getgob() {
      return Party.this.glob.oc.getgob(this.gobid);
    }
    
    public Coord getc() {
      try {
        Gob gob;
        if ((gob = getgob()) != null)
          return new Coord(gob.getc()); 
      } catch (Loading loading) {}
      return this.c;
    }
  }
  
  public void msg(Message msg) {
    while (!msg.eom()) {
      int type = msg.uint8();
      if (type == 0) {
        ArrayList<Long> ids = new ArrayList<>();
        while (true) {
          long id = msg.int32();
          if (id < 0L)
            break; 
          ids.add(Long.valueOf(id));
        } 
        Map<Long, Member> nmemb = new TreeMap<>();
        for (Iterator<Long> iterator = ids.iterator(); iterator.hasNext(); ) {
          long id = ((Long)iterator.next()).longValue();
          Member m = this.memb.get(Long.valueOf(id));
          if (m == null) {
            m = new Member();
            m.gobid = id;
          } 
          nmemb.put(Long.valueOf(id), m);
        } 
        long lid = (this.leader == null) ? -1L : this.leader.gobid;
        this.memb = nmemb;
        this.leader = this.memb.get(Long.valueOf(lid));
        continue;
      } 
      if (type == 1) {
        Member m = this.memb.get(Long.valueOf(msg.int32()));
        if (m != null)
          this.leader = m; 
        continue;
      } 
      if (type == 2) {
        Member m = this.memb.get(Long.valueOf(msg.int32()));
        Coord c = null;
        boolean vis = (msg.uint8() == 1);
        if (vis)
          c = msg.coord(); 
        Color col = msg.color();
        if (m != null) {
          m.c = c;
          m.col = col;
        } 
      } 
    } 
  }
}
