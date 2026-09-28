package haven.rs;

import haven.Camera;
import haven.Composited;
import haven.Coord;
import haven.Coord3f;
import haven.DirLight;
import haven.Drawn;
import haven.GLState;
import haven.GOut;
import haven.Indir;
import haven.Light;
import haven.Loading;
import haven.Location;
import haven.LocationCam;
import haven.PUtils;
import haven.PView;
import haven.Projection;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Skeleton;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

public class AvaRender {
  public static Composited compose(Resource base, List<Composited.MD> mod, List<Composited.ED> equ) {
    Composited comp = new Composited(((Skeleton.Res)base.layer(Skeleton.Res.class)).s);
    comp.chmod(mod);
    comp.chequ(equ);
    comp.changes(true);
    return comp;
  }
  
  public static BufferedImage render(Coord sz, Resource base, String camnm, List<Composited.MD> mod, List<Composited.ED> equ) throws InterruptedException {
    Composited tcomp;
    LocationCam locationCam1;
    while (true) {
      try {
        Skeleton.BoneOffset camoff = (Skeleton.BoneOffset)base.layer(Skeleton.BoneOffset.class, camnm);
        tcomp = compose(base, mod, equ);
        GLState.Buffer buffer = new GLState.Buffer(null);
        camoff.forpose(tcomp.pose).prep(buffer);
        locationCam1 = new LocationCam((Location.Chain)buffer.get(PView.loc));
        break;
      } catch (Loading ev) {
        ev.waitfor();
      } 
    } 
    final Composited comp = tcomp;
    final LocationCam cam = locationCam1;
    final GBuffer buf = new GBuffer(sz);
    final BufferedImage[] ret = { null };
    buf.render(new Drawn() {
          public void draw(GOut g) {
            float field = 0.5F;
            float aspect = buf.sz.y / buf.sz.x;
            Projection proj = Projection.frustum(-0.5F, 0.5F, -aspect * 0.5F, aspect * 0.5F, 1.0F, 5000.0F);
            Light.Model lmod = new Light.Model();
            lmod.cc = 33274;
            BufView view = new BufView(buf, GLState.compose(new GLState[] { (GLState)proj, (GLState)this.val$cam, (GLState)lmod, (GLState)new Light.LightList() }));
            view.render(new Rendered() {
                  public void draw(GOut g) {}
                  
                  public boolean setup(RenderList rl) {
                    rl.add((Rendered)comp, null);
                    rl.add((Rendered)new DirLight(Color.WHITE, Color.WHITE, Color.WHITE, (new Coord3f(1.0F, 1.0F, 1.0F)).norm()), null);
                    return false;
                  }
                }g);
            ret[0] = g.getimage();
          }
        });
    buf.dispose();
    return ret[0];
  }
  
  public static final Server.Command call = new Server.Command() {
      public Object[] run(Server.Client cl, Object... args) throws InterruptedException {
        Coord sz = (Coord)args[0];
        Resource base = Resource.load((String)args[1]);
        String camnm = (String)args[2];
        Object[] amod = (Object[])args[3];
        Object[] aequ = (Object[])args[4];
        List<Composited.MD> mod = new LinkedList<>();
        for (int i = 0; i < amod.length; i += 2) {
          Resource mr = Resource.load((String)amod[i]);
          Object[] atex = (Object[])amod[i + 1];
          List<Indir<Resource>> tex = new LinkedList<>();
          for (int o = 0; o < atex.length; o++)
            tex.add(Resource.load((String)atex[o]).indir()); 
          mod.add(new Composited.MD(mr.indir(), tex));
        } 
        List<Composited.ED> equ = new LinkedList<>();
        for (int j = 0; j < aequ.length; j += 6) {
          int t = ((Integer)aequ[j]).intValue();
          String at = (String)aequ[j + 1];
          Resource er = Resource.load((String)aequ[j + 2]);
          Coord3f off = new Coord3f(((Float)aequ[j + 3]).floatValue(), ((Float)aequ[j + 4]).floatValue(), ((Float)aequ[j + 5]).floatValue());
          equ.add(new Composited.ED(t, at, er.indir(), off));
        } 
        BufferedImage ava = AvaRender.render(sz.mul(4), base, camnm, mod, equ);
        ava = PUtils.convolvedown(ava, sz, (PUtils.Convolution)new PUtils.Lanczos(2.0D));
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
          ImageIO.write(ava, "PNG", buf);
        } catch (IOException e) {
          throw new Error(e);
        } 
        return new Object[] { "ok", buf.toByteArray() };
      }
    };
  
  public static void main(String[] args) throws Exception {
    Resource base = Resource.load("gfx/borka/body");
    List<Composited.MD> mod = Arrays.asList(new Composited.MD[] { new Composited.MD(Resource.load("gfx/borka/male").indir(), Arrays.asList(new Indir[] { Resource.load("gfx/borka/male").indir() })) });
    List<Composited.ED> equ = new LinkedList<>();
    BufferedImage img = render(new Coord(512, 512), base, "avacam", mod, equ);
    img = PUtils.convolvedown(img, new Coord(128, 128), (PUtils.Convolution)new PUtils.Lanczos(2.0D));
    ImageIO.write(img, "PNG", new File("/tmp/bard.png"));
  }
}
