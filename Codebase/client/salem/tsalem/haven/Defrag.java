package haven;

public class Defrag {
  byte[] blob;
  
  int len;
  
  long last = 0L;
  
  final int[] ms1 = new int[20], ms2 = new int[20];
  
  public Defrag(int len) {
    this.len = len;
    this.blob = new byte[len];
    this.ms1[0] = 0;
    this.ms2[0] = len;
    for (int i = 1; i < 20; i++)
      this.ms1[i] = -1; 
  }
  
  private void addm(int m1, int m2) {
    for (int i = 0; i < this.ms1.length; i++) {
      if (this.ms1[i] == -1) {
        this.ms1[i] = m1;
        this.ms2[i] = m2;
        return;
      } 
    } 
    throw new RuntimeException("Ran out of segment buffers!");
  }
  
  public void add(byte[] blob, int boff, int blen, int off) {
    System.arraycopy(blob, boff, this.blob, off, blen);
    for (int i = 0; i < this.ms1.length; i++) {
      if (this.ms1[i] != -1) {
        int m1 = this.ms1[i], m2 = this.ms2[i];
        int s1 = off, s2 = off + blen;
        if (m1 >= s1 && m2 <= s2) {
          this.ms1[i] = -1;
        } else if (m1 >= s1 && m1 < s2 && m2 >= s2) {
          this.ms1[i] = s2;
        } else if (m1 < s1 && m2 >= s1 && m2 <= s2) {
          this.ms2[i] = s1;
        } else if (m1 < s1 && m2 > s2) {
          this.ms2[i] = s1;
          addm(s2, m2);
        } 
      } 
    } 
  }
  
  public boolean done() {
    for (int i = 0; i < this.ms1.length; i++) {
      if (this.ms1[i] != -1)
        return false; 
    } 
    return true;
  }
  
  public Message msg() {
    return new Message(0, this.blob);
  }
}
