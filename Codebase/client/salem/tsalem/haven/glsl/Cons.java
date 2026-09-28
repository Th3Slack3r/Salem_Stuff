package haven.glsl;

import haven.Coord;
import haven.Coord3f;
import haven.Utils;
import java.awt.Color;

public class Cons {
  public static Statement stmt(Expression e) {
    return Statement.expr(e);
  }
  
  public static LBinOp.Assign ass(LValue l, Expression r) {
    return new LBinOp.Assign(l, r);
  }
  
  public static LBinOp.Assign ass(Variable l, Expression r) {
    return ass(l.ref(), r);
  }
  
  public static Add add(Expression... terms) {
    return new Add(terms);
  }
  
  public static Mul mul(Expression... terms) {
    return new Mul(terms);
  }
  
  public static BinOp.Sub sub(Expression l, Expression r) {
    return new BinOp.Sub(l, r);
  }
  
  public static BinOp.Div div(Expression l, Expression r) {
    return new BinOp.Div(l, r);
  }
  
  public static LBinOp.AAdd aadd(LValue l, Expression r) {
    return new LBinOp.AAdd(l, r);
  }
  
  public static LBinOp.ASub asub(LValue l, Expression r) {
    return new LBinOp.ASub(l, r);
  }
  
  public static LBinOp.AMul amul(LValue l, Expression r) {
    return new LBinOp.AMul(l, r);
  }
  
  public static LBinOp.ADiv adiv(LValue l, Expression r) {
    return new LBinOp.ADiv(l, r);
  }
  
  public static BinOp.Div inv(Expression op) {
    return div(l(1.0D), op);
  }
  
  public static PreOp.Neg neg(Expression op) {
    return new PreOp.Neg(op);
  }
  
  public static LPreOp.Inc incl(LValue op) {
    return new LPreOp.Inc(op);
  }
  
  public static LPreOp.Dec decl(LValue op) {
    return new LPreOp.Dec(op);
  }
  
  public static LPostOp.Inc linc(LValue op) {
    return new LPostOp.Inc(op);
  }
  
  public static LPostOp.Dec ldec(LValue op) {
    return new LPostOp.Dec(op);
  }
  
  public static BinOp.Eq eq(Expression l, Expression r) {
    return new BinOp.Eq(l, r);
  }
  
  public static BinOp.Ne ne(Expression l, Expression r) {
    return new BinOp.Ne(l, r);
  }
  
  public static BinOp.Lt lt(Expression l, Expression r) {
    return new BinOp.Lt(l, r);
  }
  
  public static BinOp.Gt gt(Expression l, Expression r) {
    return new BinOp.Gt(l, r);
  }
  
  public static BinOp.Le le(Expression l, Expression r) {
    return new BinOp.Le(l, r);
  }
  
  public static BinOp.Ge ge(Expression l, Expression r) {
    return new BinOp.Ge(l, r);
  }
  
  public static BinOp.Or or(Expression l, Expression r) {
    return new BinOp.Or(l, r);
  }
  
  public static BinOp.And and(Expression l, Expression r) {
    return new BinOp.And(l, r);
  }
  
  public static LPick pick(LValue val, String el) {
    return new LPick(val, el);
  }
  
  public static Pick pick(Expression val, String el) {
    return new Pick(val, el);
  }
  
  public static LFieldRef fref(LValue val, String el) {
    return new LFieldRef(val, el);
  }
  
  public static FieldRef fref(Expression val, String el) {
    return new FieldRef(val, el);
  }
  
  public static Index idx(Expression val, Expression idx) {
    return new Index(val, idx);
  }
  
  public static IntLiteral l(int val) {
    return new IntLiteral(val);
  }
  
  public static FloatLiteral l(double val) {
    return new FloatLiteral(val);
  }
  
  public static Vec4Cons vec4(Expression... els) {
    return new Vec4Cons(els);
  }
  
  public static Vec3Cons vec3(Expression... els) {
    return new Vec3Cons(els);
  }
  
  public static Vec2Cons vec2(Expression... els) {
    return new Vec2Cons(els);
  }
  
  public static IVec4Cons ivec4(Expression... els) {
    return new IVec4Cons(els);
  }
  
  public static IVec3Cons ivec3(Expression... els) {
    return new IVec3Cons(els);
  }
  
  public static IVec2Cons ivec2(Expression... els) {
    return new IVec2Cons(els);
  }
  
  public static Mat3Cons mat3(Expression... els) {
    return new Mat3Cons(els);
  }
  
  public static Expression sin(Expression x) {
    return Function.Builtin.sin.call(new Expression[] { x });
  }
  
  public static Expression abs(Expression x) {
    return Function.Builtin.abs.call(new Expression[] { x });
  }
  
  public static Expression floor(Expression x) {
    return Function.Builtin.floor.call(new Expression[] { x });
  }
  
  public static Expression ceil(Expression x) {
    return Function.Builtin.ceil.call(new Expression[] { x });
  }
  
  public static Expression fract(Expression x) {
    return Function.Builtin.fract.call(new Expression[] { x });
  }
  
  public static Expression mod(Expression x, Expression y) {
    return Function.Builtin.mod.call(new Expression[] { x, y });
  }
  
  public static Expression length(Expression x) {
    return Function.Builtin.length.call(new Expression[] { x });
  }
  
  public static Expression normalize(Expression x) {
    return Function.Builtin.normalize.call(new Expression[] { x });
  }
  
  public static Expression distance(Expression x, Expression y) {
    return Function.Builtin.distance.call(new Expression[] { x, y });
  }
  
  public static Expression dot(Expression x, Expression y) {
    return Function.Builtin.dot.call(new Expression[] { x, y });
  }
  
  public static Expression pow(Expression x, Expression y) {
    return Function.Builtin.pow.call(new Expression[] { x, y });
  }
  
  public static Expression exp(Expression x) {
    return Function.Builtin.exp.call(new Expression[] { x });
  }
  
  public static Expression log(Expression x) {
    return Function.Builtin.log.call(new Expression[] { x });
  }
  
  public static Expression exp2(Expression x) {
    return Function.Builtin.exp2.call(new Expression[] { x });
  }
  
  public static Expression log2(Expression x) {
    return Function.Builtin.log2.call(new Expression[] { x });
  }
  
  public static Expression sqrt(Expression x) {
    return Function.Builtin.sqrt.call(new Expression[] { x });
  }
  
  public static Expression inversesqrt(Expression x) {
    return Function.Builtin.inversesqrt.call(new Expression[] { x });
  }
  
  public static Expression cross(Expression x, Expression y) {
    return Function.Builtin.cross.call(new Expression[] { x, y });
  }
  
  public static Expression reflect(Expression x, Expression y) {
    return Function.Builtin.reflect.call(new Expression[] { x, y });
  }
  
  public static Expression texture2D(Expression s, Expression c) {
    return Function.Builtin.texture2D.call(new Expression[] { s, c });
  }
  
  public static Expression texture3D(Expression s, Expression c) {
    return Function.Builtin.texture3D.call(new Expression[] { s, c });
  }
  
  public static Expression textureCube(Expression s, Expression c) {
    return Function.Builtin.textureCube.call(new Expression[] { s, c });
  }
  
  public static Expression texelFetch(Expression s, Expression c, Expression l) {
    return Function.Builtin.texelFetch.call(new Expression[] { s, c, l });
  }
  
  public static Expression mix(Expression x, Expression y, Expression a) {
    return Function.Builtin.mix.call(new Expression[] { x, y, a });
  }
  
  public static Expression clamp(Expression x, Expression a, Expression b) {
    return Function.Builtin.clamp.call(new Expression[] { x, a, b });
  }
  
  public static Expression smoothstep(Expression a, Expression b, Expression x) {
    return Function.Builtin.smoothstep.call(new Expression[] { a, b, x });
  }
  
  public static Expression reduce(Function fun, Expression... es) {
    if (es.length < 1)
      throw new IllegalArgumentException("args < 1"); 
    if (es.length == 1)
      return es[0]; 
    return fun.call(new Expression[] { es[0], reduce(fun, (Expression[])Utils.splice((Object[])es, 1)) });
  }
  
  public static Expression min(Expression... es) {
    return reduce(Function.Builtin.min, es);
  }
  
  public static Expression max(Expression... es) {
    return reduce(Function.Builtin.max, es);
  }
  
  public static Expression col4(Color c) {
    return vec4(new Expression[] { l(c.getRed() / 255.0D), l(c.getGreen() / 255.0D), l(c.getBlue() / 255.0D), l(c.getAlpha() / 255.0D) });
  }
  
  public static Expression col3(Color c) {
    return vec3(new Expression[] { l(c.getRed() / 255.0D), l(c.getGreen() / 255.0D), l(c.getBlue() / 255.0D) });
  }
  
  public static Expression vec2(Coord c) {
    return vec2(new Expression[] { l(c.x), l(c.y) });
  }
  
  public static Expression vec3(Coord3f c) {
    return vec3(new Expression[] { l(c.x), l(c.y), l(c.z) });
  }
  
  public static Expression vec2(double a, double b) {
    return vec2(new Expression[] { l(a), l(b) });
  }
  
  public static Expression vec3(double a, double b, double c) {
    return vec3(new Expression[] { l(a), l(b), l(c) });
  }
  
  public static Expression vec4(double a, double b, double c, double d) {
    return vec4(new Expression[] { l(a), l(b), l(c), l(d) });
  }
  
  public static Expression ivec2(Coord c) {
    return ivec2(new Expression[] { l(c.x), l(c.y) });
  }
  
  public static Expression ivec2(int a, int b) {
    return ivec2(new Expression[] { l(a), l(b) });
  }
  
  public static Expression ivec3(int a, int b, int c) {
    return ivec3(new Expression[] { l(a), l(b), l(c) });
  }
  
  public static Expression ivec4(int a, int b, int c, int d) {
    return ivec4(new Expression[] { l(a), l(b), l(c), l(d) });
  }
  
  public static <T> T id(T a) {
    return a;
  }
  
  public static final Macro1<Expression> idm = new Macro1<Expression>() {
      public Expression expand(Expression in) {
        return in;
      }
    };
}
