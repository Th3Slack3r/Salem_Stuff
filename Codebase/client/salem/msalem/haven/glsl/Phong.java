package haven.glsl;

import java.util.Objects;

public class Phong extends ValBlock.Group {
   private final ProgramContext prog;
   private final Expression vert;
   private final Expression edir;
   private final Expression norm;
   public final ValBlock.Group.GValue bcol;
   public final ValBlock.Group.GValue scol;
   public final boolean pfrag;
   public static final Uniform nlights = new Uniform(Type.INT);
   public final Phong.DoLight dolight;

   @Override
   public void cons1() {
   }

   @Override
   public void cons2(Block blk) {
      this.bcol.var = blk.local(Type.VEC3, Cons.pick((LValue)Cons.fref((LValue)ProgramContext.gl_FrontMaterial.ref(), "emission"), "rgb"));
      this.scol.var = blk.local(Type.VEC3, Vec3Cons.z);
      boolean unroll = true;
      if (!unroll) {
         Variable i = blk.local(Type.INT, "i", null);
         blk.add(
            new For(
               Cons.ass(i, Cons.l(0)),
               Cons.lt(i.ref(), nlights.ref()),
               Cons.linc(i.ref()),
               Cons.stmt(this.dolight.call(new Expression[]{i.ref(), this.vert, this.edir, this.norm, this.bcol.var.ref(), this.scol.var.ref()}))
            )
         );
      } else {
         for (int i = 0; i < 4; i++) {
            blk.add(
               new If(
                  Cons.gt(nlights.ref(), Cons.l(i)),
                  Cons.stmt(this.dolight.call(new Expression[]{Cons.l(i), this.vert, this.edir, this.norm, this.bcol.var.ref(), this.scol.var.ref()}))
               )
            );
         }
      }

      this.bcol.addmods(blk);
      this.scol.addmods(blk);
   }

   private static void fmod(final FragmentContext fctx, final Expression bcol, final Expression scol) {
      fctx.fragcol
         .mod(
            new Macro1<Expression>() {
               public Expression expand(Expression in) {
                  return Cons.add(
                     Cons.mul(in, Cons.vec4(bcol, Cons.pick((LValue)Cons.fref((LValue)ProgramContext.gl_FrontMaterial.ref(), "diffuse"), "a"))),
                     Cons.vec4(scol, Cons.l(0.0))
                  );
               }
            },
            500
         );
   }

   public Phong(VertexContext vctx) {
      ValBlock var10001 = vctx.mainvals;
      Objects.requireNonNull(vctx.mainvals);
      super();
      this.bcol = new ValBlock.Group.GValue(Type.VEC3);
      this.scol = new ValBlock.Group.GValue(Type.VEC3);
      this.pfrag = false;
      this.prog = vctx.prog;
      ValBlock.Value edir = MiscLib.vertedir(vctx);
      this.depend(vctx.eyev);
      this.depend(edir);
      this.depend(vctx.eyen);
      this.vert = Cons.pick(vctx.eyev.ref(), "xyz");
      this.edir = edir.ref();
      this.norm = vctx.eyen.ref();
      Expression bcol = (new AutoVarying(Type.VEC3) {
         @Override
         public Expression root(VertexContext vctx) {
            return Phong.this.bcol.depref();
         }
      }).ref();
      Expression scol = (new AutoVarying(Type.VEC3) {
         @Override
         public Expression root(VertexContext vctx) {
            return Phong.this.scol.depref();
         }
      }).ref();
      fmod(vctx.prog.fctx, bcol, scol);
      this.dolight = new Phong.DoLight();
      this.prog.module(this);
   }

   public Phong(FragmentContext fctx) {
      ValBlock var10001 = fctx.mainvals;
      Objects.requireNonNull(fctx.mainvals);
      super();
      this.bcol = new ValBlock.Group.GValue(Type.VEC3);
      this.scol = new ValBlock.Group.GValue(Type.VEC3);
      this.pfrag = true;
      this.prog = fctx.prog;
      ValBlock.Value edir = MiscLib.fragedir(fctx);
      ValBlock.Value norm = MiscLib.frageyen(fctx);
      this.depend(edir);
      this.depend(norm);
      this.vert = MiscLib.frageyev.ref();
      this.edir = edir.ref();
      this.norm = norm.ref();
      fmod(fctx, this.bcol.ref(), this.scol.ref());
      fctx.fragcol.depend(this.bcol);
      this.dolight = new Phong.DoLight();
      this.prog.module(this);
   }

   public static class CelShade implements ShaderMacro {
      public static final Function celramp = new Function.Def(Type.VEC3) {
         {
            Expression c = this.param(Function.PDir.IN, Type.VEC3).ref();
            Block.Local m = this.code.local(Type.FLOAT, Cons.max(Cons.pick(c, "r"), Cons.pick(c, "g"), Cons.pick(c, "b")));
            this.code.add(new If(Cons.lt(m.ref(), Cons.l(0.01)), new Return(Vec3Cons.z)));
            Block.Local v = this.code.local(Type.FLOAT, null);
            this.code
               .add(
                  new If(
                     Cons.gt(m.ref(), Cons.l(0.5)),
                     Cons.stmt(Cons.ass(v, Cons.l(1.0))),
                     new If(Cons.gt(m.ref(), Cons.l(0.1)), Cons.stmt(Cons.ass(v, Cons.l(0.5))), Cons.stmt(Cons.ass(v, Cons.l(0.0))))
                  )
               );
            this.code.add(new Return(Cons.mul(c, Cons.div(v.ref(), m.ref()))));
         }
      };

      @Override
      public void modify(ProgramContext prog) {
         Phong ph = prog.getmod(Phong.class);
         Macro1<Expression> cel = new Macro1<Expression>() {
            public Expression expand(Expression in) {
               return Phong.CelShade.celramp.call(in);
            }
         };
         ph.bcol.mod(cel, 0);
         ph.scol.mod(cel, 0);
      }
   }

   public class DoLight extends Function.Def {
      public final Expression i = this.param(Function.PDir.IN, Type.INT).ref();
      public final Expression vert = this.param(Function.PDir.IN, Type.VEC3).ref();
      public final Expression edir = this.param(Function.PDir.IN, Type.VEC3).ref();
      public final Expression norm = this.param(Function.PDir.IN, Type.VEC3).ref();
      public final LValue diff = this.param(Function.PDir.INOUT, Type.VEC3).ref();
      public final LValue spec = this.param(Function.PDir.INOUT, Type.VEC3).ref();
      public final Expression ls;
      public final Expression mat;
      public final Expression shine;
      public final ValBlock.Value lvl;
      public final ValBlock.Value dir;
      public final ValBlock.Value dl;
      public final ValBlock.Value sl;
      public final ValBlock dvals;
      public final ValBlock svals;
      private final OrderList<Runnable> mods;
      public Block dcalc;
      public Block scalc;
      public Statement dcurs;
      public Statement scurs;

      private DoLight() {
         super(Type.VOID);
         Phong.this.prog;
         this.ls = Cons.idx(ProgramContext.gl_LightSource.ref(), this.i);
         Phong.this.prog;
         this.mat = ProgramContext.gl_FrontMaterial.ref();
         this.shine = Cons.fref(this.mat, "shininess");
         this.dvals = new ValBlock();
         this.svals = new ValBlock();
         this.mods = new OrderList<>();
         ValBlock var10003 = this.dvals;
         Objects.requireNonNull(this.dvals);
         ValBlock.Group tdep = new ValBlock.Group(var10003) {
            {
               Objects.requireNonNull(x0);
            }

            @Override
            public void cons1() {
            }

            @Override
            public void cons2(Block blk) {
               DoLight.this.lvl.var = blk.local(Type.FLOAT, null);
               DoLight.this.dir.var = blk.local(Type.VEC3, null);
               Block.Local rel = new Block.Local(Type.VEC3);
               Block.Local dst = new Block.Local(Type.FLOAT);
               DoLight.this.code
                  .add(
                     new If(
                        Cons.eq(Cons.pick(Cons.fref(DoLight.this.ls, "position"), "w"), Cons.l(0.0)),
                        new Block(
                           Cons.stmt(Cons.ass(DoLight.this.lvl.var, Cons.l(1.0))),
                           Cons.stmt(Cons.ass(DoLight.this.dir.var, Cons.pick(Cons.fref(DoLight.this.ls, "position"), "xyz")))
                        ),
                        new Block(
                           rel.new Def(Cons.sub(Cons.pick(Cons.fref(DoLight.this.ls, "position"), "xyz"), DoLight.this.vert)),
                           Cons.stmt(Cons.ass(DoLight.this.dir.var, Cons.normalize(rel.ref()))),
                           dst.new Def(Cons.length(rel.ref())),
                           Cons.stmt(
                              Cons.ass(
                                 DoLight.this.lvl.var,
                                 Cons.inv(
                                    Cons.add(
                                       Cons.fref(DoLight.this.ls, "constantAttenuation"),
                                       Cons.mul(Cons.fref(DoLight.this.ls, "linearAttenuation"), dst.ref()),
                                       Cons.mul(Cons.fref(DoLight.this.ls, "quadraticAttenuation"), dst.ref(), dst.ref())
                                    )
                                 )
                              )
                           )
                        )
                     )
                  );
            }
         };
         this.lvl = tdep.new GValue(Type.FLOAT);
         this.dir = tdep.new GValue(Type.VEC3);
         ValBlock var10004 = this.dvals;
         Objects.requireNonNull(this.dvals);
         this.dl = new ValBlock.Value(var10004, Type.FLOAT) {
            {
               Objects.requireNonNull(x0);
            }

            @Override
            public Expression root() {
               return Cons.dot(DoLight.this.norm, DoLight.this.dir.depref());
            }
         };
         var10004 = this.svals;
         Objects.requireNonNull(this.svals);
         this.sl = new ValBlock.Value(var10004, Type.FLOAT) {
            {
               Objects.requireNonNull(x0);
            }

            @Override
            public Expression root() {
               Expression reflvl = Cons.pow(
                  Cons.max(Cons.dot(DoLight.this.edir, Cons.reflect(Cons.neg(DoLight.this.dir.ref()), DoLight.this.norm)), Cons.l(0.0)), DoLight.this.shine
               );
               Expression hvlvl = Cons.pow(
                  Cons.max(Cons.dot(DoLight.this.norm, Cons.normalize(Cons.add(DoLight.this.edir, DoLight.this.dir.ref())))), DoLight.this.shine
               );
               return reflvl;
            }
         };
         this.lvl.force();
         this.dl.force();
         this.sl.force();
      }

      @Override
      protected void cons() {
         this.dvals.cons(this.code);
         this.code
            .add(
               Cons.stmt(
                  Cons.aadd(
                     this.diff, Cons.mul(Cons.pick(Cons.fref(this.mat, "ambient"), "rgb"), Cons.pick(Cons.fref(this.ls, "ambient"), "rgb"), this.lvl.ref())
                  )
               )
            );
         this.code.add(new If(Cons.gt(this.dl.ref(), Cons.l(0.0)), this.dcalc = new Block()));
         this.dcalc.add(this.dcurs = new Placeholder());
         this.dcalc
            .add(
               Cons.aadd(
                  this.diff,
                  Cons.mul(Cons.pick(Cons.fref(this.mat, "diffuse"), "rgb"), Cons.pick(Cons.fref(this.ls, "diffuse"), "rgb"), this.dl.ref(), this.lvl.ref())
               )
            );
         this.dcalc.add(new If(Cons.gt(this.shine, Cons.l(0.5)), this.scalc = new Block()));
         this.svals.cons(this.scalc);
         this.scalc.add(this.scurs = new Placeholder());
         this.scalc
            .add(
               Cons.aadd(
                  this.spec, Cons.mul(Cons.pick(Cons.fref(this.mat, "specular"), "rgb"), Cons.pick(Cons.fref(this.ls, "specular"), "rgb"), this.sl.ref())
               )
            );

         for (Runnable mod : this.mods) {
            mod.run();
         }
      }

      public void mod(Runnable mod, int order) {
         this.mods.add(mod, order);
      }
   }
}
