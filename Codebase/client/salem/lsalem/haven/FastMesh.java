package haven;

import java.nio.Buffer;
import java.nio.ShortBuffer;
import javax.media.opengl.GL2;

public class FastMesh implements FRendered, Disposable {
   public static final GLState.Slot<GLState> vstate = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class);
   public final VertexBuf vert;
   public final ShortBuffer indb;
   public final int num;
   public FastMesh from;
   private FastMesh.Compiler compiler;
   private Coord3f nb;
   private Coord3f pb;
   private GLSettings.MeshMode curmode = null;

   public FastMesh(VertexBuf vert, ShortBuffer ind) {
      this.vert = vert;
      this.num = ind.capacity() / 3;
      if (ind.capacity() != this.num * 3) {
         throw new RuntimeException("Invalid index array length");
      } else {
         this.indb = ind;
      }
   }

   public FastMesh(VertexBuf vert, short[] ind) {
      this(vert, Utils.bufcp(ind));
   }

   public FastMesh(FastMesh from, VertexBuf vert) {
      this.from = from;
      if (from.vert.num != vert.num) {
         throw new RuntimeException("V-buf sizes must match");
      } else {
         this.vert = vert;
         this.indb = from.indb;
         this.num = from.num;
      }
   }

   private void cbounds() {
      Coord3f nb = null;
      Coord3f pb = null;
      VertexBuf.VertexArray vbuf = null;

      for (VertexBuf.AttribArray buf : this.vert.bufs) {
         if (buf instanceof VertexBuf.VertexArray) {
            vbuf = (VertexBuf.VertexArray)buf;
            break;
         }
      }

      for (int i = 0; i < this.indb.capacity(); i++) {
         int vi = this.indb.get(i) * 3;
         float x = vbuf.data.get(vi);
         float y = vbuf.data.get(vi + 1);
         float z = vbuf.data.get(vi + 2);
         if (nb == null) {
            nb = new Coord3f(x, y, z);
            pb = new Coord3f(x, y, z);
         } else {
            nb.x = Math.min(nb.x, x);
            pb.x = Math.max(pb.x, x);
            nb.y = Math.min(nb.y, y);
            pb.y = Math.max(pb.y, y);
            nb.z = Math.min(nb.z, z);
            pb.z = Math.max(pb.z, z);
         }
      }

      this.nb = nb;
      this.pb = pb;
   }

   public Coord3f nbounds() {
      if (this.nb == null) {
         this.cbounds();
      }

      return this.nb;
   }

   public Coord3f pbounds() {
      if (this.pb == null) {
         this.cbounds();
      }

      return this.pb;
   }

   public void cdraw(GOut g) {
      g.apply();
      ((Buffer)this.indb).rewind();

      for (int i = 0; i < this.vert.bufs.length; i++) {
         if (this.vert.bufs[i] instanceof VertexBuf.GLArray) {
            ((VertexBuf.GLArray)this.vert.bufs[i]).bind(g, false);
         }
      }

      g.gl.glDrawElements(4, this.num * 3, 5123, this.indb);

      for (int ix = 0; ix < this.vert.bufs.length; ix++) {
         if (this.vert.bufs[ix] instanceof VertexBuf.GLArray) {
            ((VertexBuf.GLArray)this.vert.bufs[ix]).unbind(g);
         }
      }
   }

   @Override
   public void draw(GOut g) {
      GL2 gl = g.gl;
      if (this.compile()) {
         if (this.curmode != g.gc.pref.meshmode.val) {
            if (this.compiler != null) {
               this.compiler.dispose();
               this.compiler = null;
            }

            switch ((GLSettings.MeshMode)g.gc.pref.meshmode.val) {
               case VAO:
                  this.compiler = new FastMesh.VAOCompiler();
                  break;
               case DLIST:
                  this.compiler = new FastMesh.DLCompiler();
            }

            this.curmode = g.gc.pref.meshmode.val;
         }
      } else if (this.compiler != null) {
         this.compiler.dispose();
         this.compiler = null;
         this.curmode = null;
      }

      if (this.compiler != null) {
         this.compiler.get(g).draw(g);
      } else {
         this.cdraw(g);
      }

      GOut.checkerr(gl);
   }

   protected boolean compile() {
      return true;
   }

   @Override
   public void dispose() {
      if (this.compiler != null) {
         this.compiler.dispose();
         this.compiler = null;
      }

      this.vert.dispose();
   }

   @Override
   public void drawflat(GOut g) {
      this.draw(g);
      GOut.checkerr(g.gl);
   }

   @Override
   public boolean setup(RenderList r) {
      return true;
   }

   public abstract static class Compiled {
      public abstract void draw(GOut var1);

      public abstract void dispose();
   }

   public abstract class Compiler {
      private GLProgram[] kcache = new GLProgram[0];
      private FastMesh.Compiled[] vcache = new FastMesh.Compiled[0];
      private Object[] ids = new Object[0];

      private Object[] getid(GOut g) {
         Object[] id = new Object[FastMesh.this.vert.bufs.length];

         for (int i = 0; i < id.length; i++) {
            if (FastMesh.this.vert.bufs[i] instanceof VertexBuf.GLArray) {
               id[i] = ((VertexBuf.GLArray)FastMesh.this.vert.bufs[i]).progid(g);
            } else {
               id[i] = null;
            }
         }

         return ArrayIdentity.intern(id);
      }

      public FastMesh.Compiled get(GOut g) {
         GLProgram prog = g.st.prog;

         int i;
         for (i = 0; i < this.kcache.length; i++) {
            if (this.kcache[i] == prog && i < this.vcache.length) {
               return this.vcache[i];
            }
         }

         g.apply();
         Object[] id = this.getid(g);
         int o = 0;

         FastMesh.Compiled ret;
         while (true) {
            if (o >= this.kcache.length) {
               ret = this.create(g);
               break;
            }

            if (this.ids[o] == id) {
               ret = this.vcache[o];
               break;
            }

            o++;
         }

         this.kcache = Utils.extend(this.kcache, i + 1);
         this.vcache = Utils.extend(this.vcache, i + 1);
         this.ids = Utils.extend(this.ids, i + 1);
         this.kcache[i] = prog;
         this.vcache[i] = ret;
         this.ids[i] = id;
         return ret;
      }

      public abstract FastMesh.Compiled create(GOut var1);

      public void dispose() {
         for (FastMesh.Compiled c : this.vcache) {
            c.dispose();
         }

         this.kcache = new GLProgram[0];
         this.vcache = new FastMesh.Compiled[0];
      }
   }

   public class DLCompiler extends FastMesh.Compiler {
      public FastMesh.DLCompiler.DLCompiled create(GOut g) {
         return new FastMesh.DLCompiler.DLCompiled();
      }

      public class DLCompiled extends FastMesh.Compiled {
         private DisplayList list;

         @Override
         public void draw(GOut g) {
            g.apply();
            GL2 gl = g.gl;
            if (this.list != null && this.list.gl != gl) {
               this.list.dispose();
               this.list = null;
            }

            if (this.list == null) {
               this.list = new DisplayList(gl);
               gl.glNewList(this.list.id, 4864);
               FastMesh.this.cdraw(g);
               gl.glEndList();
            }

            gl.glCallList(this.list.id);
         }

         @Override
         public void dispose() {
            if (this.list != null) {
               this.list.dispose();
               this.list = null;
            }
         }
      }
   }

   @Resource.LayerName("mesh")
   public static class MeshRes extends Resource.Layer implements Resource.IDLayer<Integer> {
      public transient FastMesh m;
      public transient Material.Res mat;
      private transient short[] tmp;
      public final int id;
      public final int ref;
      private int matid;

      public MeshRes(Resource res, byte[] buf) {
         res.getClass();
         super();
         int[] off = new int[]{0};
         int fl = Utils.ub(buf[off[0]]);
         off[0]++;
         int num = Utils.uint16d(buf, off[0]);
         off[0] += 2;
         this.matid = Utils.int16d(buf, off[0]);
         off[0] += 2;
         if ((fl & 2) != 0) {
            this.id = Utils.int16d(buf, off[0]);
            off[0] += 2;
         } else {
            this.id = -1;
         }

         if ((fl & 4) != 0) {
            this.ref = Utils.int16d(buf, off[0]);
            off[0] += 2;
         } else {
            this.ref = -1;
         }

         if ((fl & -8) != 0) {
            throw new Resource.LoadException("Unsupported flags in fastmesh: " + fl, this.getres());
         } else {
            short[] ind = new short[num * 3];

            for (int i = 0; i < num * 3; i++) {
               ind[i] = (short)Utils.int16d(buf, off[0] + i * 2);
            }

            this.tmp = ind;
         }
      }

      @Override
      public void init() {
         VertexBuf v = this.getres().layer(VertexBuf.VertexRes.class, false).b;
         this.m = new FastMesh.ResourceMesh(v, this.tmp, this);
         this.tmp = null;
         if (this.matid >= 0) {
            for (Material.Res mr : this.getres().layers(Material.Res.class, false)) {
               if (mr.id == this.matid) {
                  this.mat = mr;
               }
            }

            if (this.mat == null) {
               throw new Resource.LoadException("Could not find specified material: " + this.matid, this.getres());
            }
         }
      }

      public Integer layerid() {
         return this.id;
      }
   }

   public static class ResourceMesh extends FastMesh implements ResPart {
      public final int id;
      public final Resource res;

      public ResourceMesh(VertexBuf vert, short[] ind, FastMesh.MeshRes info) {
         super(vert, ind);
         this.id = info.id;
         this.res = info.getres();
      }

      @Override
      public int partid() {
         return this.id;
      }

      @Override
      public String toString() {
         return "FastMesh(" + this.res.name + ", " + this.id + ")";
      }
   }

   public class VAOCompiler extends FastMesh.Compiler {
      public FastMesh.VAOCompiler.VAOCompiled create(GOut g) {
         return new FastMesh.VAOCompiler.VAOCompiled();
      }

      public class VAOCompiled extends FastMesh.Compiled {
         private FastMesh.VAOState st = FastMesh.this.new VAOState();

         @Override
         public void draw(GOut g) {
            GL2 gl = g.gl;
            g.state(this.st);
            g.apply();
            gl.glDrawElements(4, FastMesh.this.num * 3, 5123, 0L);
         }

         @Override
         public void dispose() {
            this.st.dispose();
         }
      }
   }

   public class VAOState extends GLState {
      private GLBuffer ind;
      private GLVertexArray vao;

      private void bindindbo(GL2 gl) {
         if (this.ind != null && this.ind.gl != gl) {
            this.ind.dispose();
            this.ind = null;
         }

         if (this.ind == null) {
            this.ind = new GLBuffer(gl);
            gl.glBindBuffer(34963, this.ind.id);
            ((java.nio.Buffer)FastMesh.this.indb).rewind();
            gl.glBufferData(34963, FastMesh.this.indb.remaining() * 2, FastMesh.this.indb, 35044);
            GOut.checkerr(gl);
         } else {
            gl.glBindBuffer(34963, this.ind.id);
         }
      }

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         if (this.vao != null && this.vao.gl != gl) {
            this.vao.dispose();
            this.vao = null;
         }

         if (this.vao == null) {
            this.vao = new GLVertexArray(gl);
            gl.glBindVertexArray(this.vao.id);

            for (VertexBuf.AttribArray buf : FastMesh.this.vert.bufs) {
               if (buf instanceof VertexBuf.GLArray) {
                  ((VertexBuf.GLArray)buf).bind(g, true);
               }
            }

            this.bindindbo(gl);
         } else {
            gl.glBindVertexArray(this.vao.id);
         }
      }

      @Override
      public void unapply(GOut g) {
         GL2 gl = g.gl;
         gl.glBindVertexArray(0);
         gl.glBindBuffer(34963, 0);
      }

      @Override
      public int capplyfrom(GLState o) {
         return o instanceof FastMesh.VAOState ? 1 : -1;
      }

      @Override
      public void applyfrom(GOut g, GLState from) {
         this.apply(g);
      }

      public void dispose() {
         if (this.vao != null) {
            this.vao.dispose();
            this.vao = null;
         }

         if (this.ind != null) {
            this.ind.dispose();
            this.ind = null;
         }
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(FastMesh.vstate, this);
      }
   }
}
