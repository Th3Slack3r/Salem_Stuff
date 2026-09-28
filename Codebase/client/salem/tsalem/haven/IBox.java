package haven;

public class IBox {
  public final Tex ctl;
  
  public final Tex ctr;
  
  public final Tex cbl;
  
  public final Tex cbr;
  
  public final Tex bl;
  
  public final Tex br;
  
  public final Tex bt;
  
  public final Tex bb;
  
  public IBox(Tex ctl, Tex ctr, Tex cbl, Tex cbr, Tex bl, Tex br, Tex bt, Tex bb) {
    this.ctl = ctl;
    this.ctr = ctr;
    this.cbl = cbl;
    this.cbr = cbr;
    this.bl = bl;
    this.br = br;
    this.bt = bt;
    this.bb = bb;
  }
  
  public IBox(String base, String ctl, String ctr, String cbl, String cbr, String bl, String br, String bt, String bb) {
    this(Resource.loadtex(base + "/" + ctl), Resource.loadtex(base + "/" + ctr), Resource.loadtex(base + "/" + cbl), Resource.loadtex(base + "/" + cbr), Resource.loadtex(base + "/" + bl), Resource.loadtex(base + "/" + br), Resource.loadtex(base + "/" + bt), Resource.loadtex(base + "/" + bb));
  }
  
  public Coord btloff() {
    return new Coord((this.bl.sz()).x, (this.bt.sz()).y);
  }
  
  public Coord ctloff() {
    return this.ctl.sz();
  }
  
  public Coord bisz() {
    return new Coord((this.bl.sz()).x + (this.br.sz()).x, (this.bt.sz()).y + (this.bb.sz()).y);
  }
  
  public Coord cisz() {
    return this.ctl.sz().add(this.cbr.sz());
  }
  
  @Deprecated
  public Coord tloff() {
    return btloff();
  }
  
  @Deprecated
  public Coord bsz() {
    return cisz();
  }
  
  public void draw(GOut g, Coord tl, Coord sz) {
    g.image(this.bt, tl.add(new Coord((this.ctl.sz()).x, 0)), new Coord(sz.x - (this.ctr.sz()).x - (this.ctl.sz()).x, (this.bt.sz()).y));
    g.image(this.bb, tl.add(new Coord((this.cbl.sz()).x, sz.y - (this.bb.sz()).y)), new Coord(sz.x - (this.cbr.sz()).x - (this.cbl.sz()).x, (this.bb.sz()).y));
    g.image(this.bl, tl.add(new Coord(0, (this.ctl.sz()).y)), new Coord((this.bl.sz()).x, sz.y - (this.cbl.sz()).y - (this.ctl.sz()).y));
    g.image(this.br, tl.add(new Coord(sz.x - (this.br.sz()).x, (this.ctr.sz()).y)), new Coord((this.br.sz()).x, sz.y - (this.cbr.sz()).y - (this.ctr.sz()).y));
    g.image(this.ctl, tl);
    g.image(this.ctr, tl.add(sz.x - (this.ctr.sz()).x, 0));
    g.image(this.cbl, tl.add(0, sz.y - (this.cbl.sz()).y));
    g.image(this.cbr, new Coord(sz.x - (this.cbr.sz()).x + tl.x, sz.y - (this.cbr.sz()).y + tl.y));
  }
}
