package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class Store extends Window {
   public static final Tex bg = Resource.loadtex("gfx/hud/store/bg");
   public static final Text.Foundry textf = new Text.Foundry(new Font("Sans", 1, 16), Color.BLACK).aa(true);
   public static final Text.Foundry texto = new Text.Foundry(new Font("Sans", 1, 14), Color.BLACK).aa(true);
   public static final RichText.Foundry descfnd = new RichText.Foundry(
         TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Button.defcol
      )
      .aa(true);
   public static final SslHelper ssl = new SslHelper();
   public final URI base;

   public Store(Coord c, Widget parent, URI base) {
      super(c, new Coord(750, 450), parent, "Salem Store");
      Widget bg = new Img(Coord.z, Store.bg, this);
      bg.c = new Coord((this.sz.x - bg.sz.x) / 2, 0);
      this.base = base;
      new Store.Loader();
   }

   public static Object[] encode(Store.Cart cart) {
      List<Object> cbuf = new ArrayList<>();

      for (Store.Cart.Item item : cart.items) {
         cbuf.add(new Object[]{"offer", item.offer.id, "ver", item.offer.ver, "num", item.num});
      }

      return cbuf.toArray(new Object[0]);
   }

   private URL fun(String fun, String... pars) {
      try {
         URL ret = this.base.resolve(fun).toURL();
         if (pars.length > 0) {
            ret = Utils.urlparam(ret, pars);
         }

         return ret;
      } catch (IOException var4) {
         throw new Store.IOError(var4);
      }
   }

   private URLConnection req(URL url) {
      try {
         URLConnection conn;
         if (url.getProtocol().equals("https")) {
            conn = ssl.connect(url);
         } else {
            conn = url.openConnection();
         }

         Message auth = new Message(0);
         auth.addstring(this.ui.sess.username);
         auth.addbytes(this.ui.sess.sesskey);
         conn.setRequestProperty("Authorization", "Haven " + Utils.base64enc(auth.blob));
         return conn;
      } catch (IOException var4) {
         throw new Store.IOError(var4);
      }
   }

   private URLConnection req(String fun, String... pars) {
      return this.req(this.fun(fun, pars));
   }

   private void send(URLConnection conn, Object[] data) {
      Message buf = new Message(0);
      buf.addlist(data);
      conn.setDoOutput(true);

      try {
         OutputStream fp = conn.getOutputStream();

         try {
            fp.write(buf.blob);
         } catch (Throwable var8) {
            if (fp != null) {
               try {
                  fp.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (fp != null) {
            fp.close();
         }
      } catch (IOException var9) {
         throw new Store.IOError(var9);
      }
   }

   private Object[] fetch(URLConnection conn) {
      try {
         InputStream fp = conn.getInputStream();

         Object[] var3;
         try {
            if (!conn.getContentType().equals("application/x-haven-ttol")) {
               throw new IOException("unexpected content-type: " + conn.getContentType());
            }

            var3 = new Message(0, Utils.readall(fp)).list();
         } catch (Throwable var6) {
            if (fp != null) {
               try {
                  fp.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (fp != null) {
            fp.close();
         }

         return var3;
      } catch (IOException var7) {
         throw new Store.IOError(var7);
      }
   }

   private Object[] fetch(String fun, String... pars) {
      return this.fetch(this.req(fun, pars));
   }

   private Defer.Future<BufferedImage> image(URI uri) {
      return Defer.later(() -> {
         try {
            InputStream fp = this.req(uri.toURL()).getInputStream();

            BufferedImage var3;
            try {
               var3 = ImageIO.read(fp);
            } catch (Throwable var6) {
               if (fp != null) {
                  try {
                     fp.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (fp != null) {
               fp.close();
            }

            return var3;
         } catch (IOException var7) {
            throw new RuntimeException(var7);
         }
      }, false);
   }

   private Store.Catalog catalog() {
      List<Store.Offer> offers = new ArrayList<>();
      List<Store.Category> catgs = new ArrayList<>();
      Object[] ls = this.fetch("offers");
      int order = 0;
      Store.Price credit = null;

      for (Object item : ls) {
         Object[] enc = (Object[])item;
         String type = (String)enc[0];
         if (type.equals("offer")) {
            String id = (String)enc[1];
            String ver = (String)enc[2];
            Store.Offer offer = new Store.Offer(id, ver);
            offer.sortkey = order++;

            for (int a = 3; a < enc.length; a += 2) {
               String key = (String)enc[a];
               Object val = enc[a + 1];
               switch (key) {
                  case "name":
                     offer.name = (String)val;
                     break;
                  case "desc":
                     offer.desc = (String)val;
                     break;
                  case "img":
                     offer.img = this.image(this.base.resolve((String)val));
                     break;
                  case "cat":
                     offer.category = ((String)val).intern();
                     break;
                  case "price":
                     offer.price = Store.Price.parse((Object[])val);
                     break;
                  case "monad":
                     offer.singleton = true;
               }
            }

            offers.add(offer);
         } else if (!type.equals("cat")) {
            if (type.equals("error")) {
               throw new Store.MessageError((String)enc[1]);
            }

            if (type.equals("credit")) {
               credit = Store.Price.parse((Object[])enc[1]);
            }
         } else {
            String id = ((String)enc[1]).intern();
            Store.Category catg = new Store.Category(id);
            catg.sortkey = order++;

            for (int a = 2; a < enc.length; a += 2) {
               String key = (String)enc[a];
               Object val = enc[a + 1];
               switch (key) {
                  case "name":
                     catg.name = (String)val;
                     break;
                  case "cat":
                     catg.parent = ((String)val).intern();
               }
            }

            catgs.add(catg);
         }
      }

      Collections.sort(offers, (ax, b) -> ax.sortkey - b.sortkey);
      Collections.sort(catgs, (ax, b) -> ax.sortkey - b.sortkey);
      return new Store.Catalog(offers, catgs, credit);
   }

   static {
      try {
         ssl.trust(Resource.class.getResourceAsStream("ressrv.crt"));
      } catch (CertificateException var1) {
         throw new Error("Invalid built-in certificate", var1);
      } catch (IOException var2) {
         throw new Error(var2);
      }

      Store.Currency.decimal("USD", 2, ".", "$%s");
   }

   public class Browser extends Widget {
      public final Coord bsz = new Coord(175, 80);
      public final Store.Cart cart;
      public final Store.Catalog cat;
      public final HScrollport btns;
      private Img clbl;
      private IButton bbtn;

      public Browser(final Store.Catalog cat, Store.Cart cart) {
         super(Coord.z, Store.this.asz, Store.this);
         this.cat = cat;
         this.cart = cart;
         this.btns = new HScrollport(new Coord(10, this.sz.y - 180), new Coord(this.sz.x - 20, 180), this);
         Coord cartc = new Coord(this.sz.x - 200 - 10, 0);
         Coord cartsz = new Coord(200, this.sz.y - 200);
         if (cat.credit != null) {
            new Label(cartc, this, "Store credit: " + cat.credit).setcolor(Button.defcol);
            cartc = cartc.add(0, 15);
            cartsz = cartsz.sub(0, 15);
         }

         new Store.CartWidget(cartc, cartsz, this, cart) {
            @Override
            public boolean clickitem(Store.Cart.Item item, int btn) {
               Store.this.new Viewer(item.offer, Browser.this, this.cart);
               return true;
            }

            @Override
            public void checkout() {
               Steam steam = null;

               try {
                  steam = Steam.get();
               } catch (NoClassDefFoundError var3) {
               }

               if (cat.credit != null && cat.credit.a >= this.cart.total().a) {
                  Store.this.new CreditCheckouter(cat, this.cart);
               } else if (steam == null) {
                  Store.this.new BrowserCheckouter(cat, this.cart, cat.credit);
               } else {
                  Store.this.new SteamCheckouter(cat, this.cart, steam);
               }

               this.ui.destroy(Browser.this);
            }
         };
         this.point(null);
      }

      public Browser(Store.Catalog cat) {
         this(cat, new Store.Cart(cat));
      }

      private void linebtns(Widget p, List<? extends Widget> btns, int y) {
         int tw = 0;

         for (Widget w : btns) {
            tw += w.sz.x;
         }

         int e = 0;
         int x = 0;

         for (Widget w : btns) {
            e += p.sz.x - tw;
            int a = e / (btns.size() + 1);
            e -= a * (btns.size() + 1);
            x += a;
            w.c = new Coord(x, y);
            x += w.sz.x;
         }
      }

      public void point(String catg) {
         if (this.clbl != null) {
            this.ui.destroy(this.clbl);
            this.clbl = null;
         }

         if (this.bbtn != null) {
            this.ui.destroy(this.bbtn);
            this.bbtn = null;
         }

         while (this.btns.cont.child != null) {
            this.ui.destroy(this.btns.cont.child);
         }

         if (catg != null) {
            final Store.Category ccat = this.cat.catgid.get(catg);
            String catp = ccat.name;

            for (Store.Category pcat = this.cat.catgid.get(ccat.parent); pcat != null; pcat = this.cat.catgid.get(pcat.parent)) {
               catp = pcat.name + " / " + catp;
            }

            this.clbl = new Img(this.btns.c.add(25, -25), Store.textf.render(catp, Button.defcol).tex(), this);
            this.bbtn = new IButton(
               this.clbl.c.add(-25, 0).add(new Coord(25, this.clbl.sz.y).sub(Utils.imgsz(Window.lbtni[0])).div(2)),
               this,
               Window.lbtni[0],
               Window.lbtni[1],
               Window.lbtni[2]
            ) {
               @Override
               public void click() {
                  Browser.this.point(ccat.parent);
               }
            };
         }

         List<Store.OButton> nbtns = new ArrayList<>();
         int x = 0;
         int y = 0;

         for (Store.Category sub : this.cat.catgs) {
            if (sub.parent == catg) {
               nbtns.add(new Store.Browser.CategoryButton(new Coord(x, y).mul(this.bsz.add(10, 10)), this.bsz, this.btns.cont, sub));
               if (++y > 1) {
                  x++;
                  y = 0;
               }
            }
         }

         for (Store.Offer offer : this.cat.offers) {
            if (offer.category == catg) {
               nbtns.add(new Store.Browser.OfferButton(new Coord(x, y).mul(this.bsz.add(10, 10)), this.bsz, this.btns.cont, offer));
               if (++y > 1) {
                  x++;
                  y = 0;
               }
            }
         }

         if (nbtns.size() <= 4) {
            this.linebtns(this.btns.cont, nbtns, 0);
         } else if (nbtns.size() <= 8) {
            int fn = (nbtns.size() + 1) / 2;
            this.linebtns(this.btns.cont, nbtns.subList(0, fn), 0);
            this.linebtns(this.btns.cont, nbtns.subList(fn, nbtns.size()), this.bsz.y + 10);
         }

         this.btns.cont.update();
         this.btns.bar.ch(-this.btns.bar.val);
      }

      public class CategoryButton extends Store.OButton {
         public final Store.Category catg;

         public CategoryButton(Coord c, Coord sz, Widget parent, Store.Category catg) {
            super(c, sz, parent, catg.name, null);
            this.catg = catg;
         }

         @Override
         public void click() {
            Browser.this.point(this.catg.id);
         }
      }

      public class OfferButton extends Store.OButton {
         public final Store.Offer offer;

         public OfferButton(Coord c, Coord sz, Widget parent, Store.Offer offer) {
            super(c, sz, parent, offer.name, offer.img);
            this.offer = offer;
         }

         @Override
         public void click() {
            Store.this.new Viewer(this.offer, Browser.this, Browser.this.cart);
         }
      }
   }

   public class BrowserCheckouter extends Store.Checkouter {
      public final Store.Price credit;
      private Defer.Future<Object[]> submit;

      public BrowserCheckouter(Store.Catalog cat, Store.Cart cart, Store.Price credit) {
         super(cat, cart);
         this.credit = credit;
         this.submit = Defer.later(this::submit);
         this.status("Checking out...", null, null, null);
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.submit != null) {
            try {
               Map<Object, Object> stat = Utils.mapdecf(this.submit.get());
               this.submit = null;
               if (Utils.eq(stat.get("status"), "ok")) {
                  try {
                     URL url = new URL((String)stat.get("url"));
                     WebBrowser.sshow(url);
                     this.done();
                  } catch (IOException | WebBrowser.BrowserException var5) {
                     this.status("Could not launch web browser.", RichText.Parser.quote(String.valueOf(var5)), "Return", this::back);
                  }
               } else if (Utils.eq(stat.get("status"), "obsolete")) {
                  this.status("The catalog has changed while you were browsing.", null, "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "invalid")) {
                  this.status("The purchase has become invalid.", RichText.Parser.quote((String)stat.get("msg")), "Reload", this::reload);
               }
            } catch (Loading var6) {
            } catch (Defer.DeferredException var7) {
               this.submit = null;
               this.status("An unexpected error occurred.", RichText.Parser.quote(String.valueOf(var7.getCause())), "Return", this::back);
               var7.printStackTrace();
            }
         }
      }

      private Object[] submit() {
         Map<String, Object> data = new HashMap<>();
         data.put("cart", Store.encode(this.cart));
         data.put("method", "paypal");
         if (this.credit != null) {
            data.put("usecredit", this.credit.a);
         }

         URLConnection conn = Store.this.req("checkout");
         Store.this.send(conn, Utils.mapencf(data));
         return Store.this.fetch(conn);
      }

      private void done() {
         this.status("Thank you!", "Please follow the instructions in the web browser to complete your purchase.", "Return", this::reload);
      }
   }

   public static class Cart {
      public final List<Store.Cart.Item> items = new ArrayList<>();
      public final Store.Currency currency;

      public Cart(Store.Currency currency) {
         this.currency = currency;
      }

      public Cart(Store.Catalog cat) {
         this(Utils.el(cat.offers).price.c);
      }

      public Store.Cart.Item byoffer(Store.Offer offer, boolean creat) {
         for (Store.Cart.Item item : this.items) {
            if (item.offer == offer) {
               return item;
            }
         }

         if (creat) {
            Store.Cart.Item ret = new Store.Cart.Item(offer, 0);
            this.items.add(ret);
            return ret;
         } else {
            return null;
         }
      }

      public boolean remove(Store.Cart.Item item) {
         return this.items.remove(item);
      }

      public Store.Price total() {
         int a = 0;

         for (Store.Cart.Item item : this.items) {
            Store.Price ip = item.total();
            if (ip.c != this.currency) {
               throw new RuntimeException("conflicting currencies");
            }

            a += ip.a;
         }

         return new Store.Price(this.currency, a);
      }

      public static class Item {
         public final Store.Offer offer;
         public int num;

         public Item(Store.Offer offer, int num) {
            this.offer = offer;
            this.num = num;
         }

         public Store.Price total() {
            return new Store.Price(this.offer.price.c, this.offer.price.a * this.num);
         }
      }
   }

   public static class CartWidget extends Widget {
      public static final Text empty = Text.render("Cart is empty", Button.defcol);
      public static final int numw = 30;
      public static final int pricew = 50;
      public final Store.Cart cart;
      public Button checkout;
      private Text rtotal = null;

      public CartWidget(Coord c, Coord sz, Widget parent, Store.Cart cart) {
         super(c, sz, parent);
         this.cart = cart;
         new Store.CartWidget.ListWidget(Coord.z, sz.sub(0, 25), this);
      }

      protected boolean clickitem(Store.Cart.Item item, int btn) {
         return false;
      }

      protected void checkout() {
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.cart.items.isEmpty() && this.checkout != null) {
            this.ui.destroy(this.checkout);
            this.checkout = null;
         } else if (!this.cart.items.isEmpty() && this.checkout == null) {
            this.checkout = new Button(new Coord(5, this.sz.y - 23), 75, this, "Checkout") {
               @Override
               public void click() {
                  CartWidget.this.checkout();
               }
            };
         }
      }

      @Override
      public void draw(GOut g) {
         super.draw(g);
         if (!this.cart.items.isEmpty()) {
            String total = "Total: " + this.cart.total();
            if (this.rtotal == null || !this.rtotal.text.equals(total)) {
               this.rtotal = Text.render(total, Button.defcol);
            }

            g.image(this.rtotal.tex(), new Coord(this.sz.x - 5 - this.rtotal.sz().x, this.sz.y - (25 + this.rtotal.sz().y) / 2));
         }
      }

      public class ItemWidget extends Widget {
         public final int nmw;
         public final int numx;
         public final int pricex;
         public final Store.Cart.Item item;
         public final Widget rbtn;
         private Text rname;
         private Text rnum;
         private Text rprice;
         private int cnum;

         public ItemWidget(Coord c, int w, Widget parent, final Store.Cart.Item item) {
            super(c, new Coord(w, 20), parent);
            this.item = item;
            this.rbtn = new IButton(Coord.z, this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]) {
               @Override
               public void click() {
                  CartWidget.this.cart.remove(item);
               }
            };
            this.rbtn.c = new Coord(this.sz.x - this.rbtn.sz.x, (this.sz.y - this.rbtn.sz.y) / 2);
            this.pricex = this.rbtn.c.x - 5 - 50;
            this.numx = this.pricex - 5 - 30;
            this.nmw = this.numx - 7;
         }

         @Override
         public void draw(GOut g) {
            if (this.rname == null) {
               this.rname = Text.render(this.item.offer.name, Button.defcol);
            }

            g.image(this.rname.tex(), new Coord(5, (this.sz.y - this.rname.sz().y) / 2), Coord.z, new Coord(this.nmw, this.sz.y));
            if (!this.item.offer.singleton) {
               if (this.rnum == null || this.cnum != this.item.num) {
                  this.rnum = Text.render("×" + this.item.num, Button.defcol);
                  this.rprice = null;
                  this.cnum = this.item.num;
               }

               g.image(this.rnum.tex(), new Coord(this.numx, (this.sz.y - this.rnum.sz().y) / 2));
            }

            if (this.rprice == null) {
               this.rprice = Text.render(this.item.total().toString(), Button.defcol);
            }

            g.image(this.rprice.tex(), new Coord(this.pricex, (this.sz.y - this.rprice.sz().y) / 2));
            super.draw(g);
         }

         @Override
         public boolean mousedown(Coord c, int btn) {
            return super.mousedown(c, btn) ? true : CartWidget.this.clickitem(this.item, btn);
         }
      }

      public class ListWidget extends Widget {
         public final Scrollport scr;
         private final Map<Store.Cart.Item, Store.CartWidget.ItemWidget> items = new HashMap<>();

         public ListWidget(Coord c, Coord sz, Widget parent) {
            super(c, sz, parent);
            this.scr = new Scrollport(Window.fbox.btloff(), sz.sub(Window.fbox.bisz()), this);
         }

         private void update() {
            Map<Store.Cart.Item, Store.CartWidget.ItemWidget> old = new HashMap<>(this.items);
            int y = 0;
            boolean ch = false;

            for (Store.Cart.Item item : CartWidget.this.cart.items) {
               Store.CartWidget.ItemWidget wdg = this.items.get(item);
               if (wdg == null) {
                  wdg = CartWidget.this.new ItemWidget(new Coord(0, y), this.scr.cont.sz.x, this.scr.cont, item);
                  this.items.put(item, wdg);
                  ch = true;
               } else {
                  old.remove(item);
                  if (wdg.c.y != y) {
                     wdg.c = new Coord(wdg.c.x, y);
                     ch = true;
                  }
               }

               y += wdg.sz.y;
            }

            for (Store.CartWidget.ItemWidget wdg : old.values()) {
               this.ui.destroy(wdg);
               ch = true;
            }

            if (ch) {
               this.scr.cont.update();
            }
         }

         @Override
         public void tick(double dt) {
            this.update();
            super.tick(dt);
         }

         @Override
         public void draw(GOut g) {
            g.chcolor(0, 0, 0, 128);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
            if (CartWidget.this.cart.items.isEmpty()) {
               g.image(Store.CartWidget.empty.tex(), this.sz.sub(Store.CartWidget.empty.sz()).div(2));
            }

            Window.fbox.draw(g, Coord.z, this.sz);
         }
      }
   }

   public static class Catalog {
      public final List<Store.Offer> offers;
      public final List<Store.Category> catgs;
      public final Map<String, Store.Category> catgid;
      public final Store.Price credit;

      public Catalog(List<Store.Offer> offers, List<Store.Category> catgs, Store.Price credit) {
         this.offers = offers;
         this.catgs = catgs;
         this.catgid = new HashMap<>();

         for (Store.Category catg : catgs) {
            this.catgid.put(catg.id, catg);
         }

         this.credit = credit;
      }
   }

   public static class Category {
      public final String id;
      public String name = "";
      public String parent = null;
      public int sortkey;

      public Category(String id) {
         this.id = id;
      }
   }

   public abstract class Checkouter extends Widget {
      public final Store.Catalog cat;
      public final Store.Cart cart;
      private Widget status;
      private Widget detail;
      private Widget[] buttons;

      public Checkouter(Store.Catalog cat, Store.Cart cart) {
         super(Coord.z, Store.this.asz, Store.this);
         this.cat = cat;
         this.cart = cart;
      }

      public void reload() {
         this.ui.destroy(this);
         Store.this.new Loader();
      }

      public void back() {
         this.ui.destroy(this);
         Store.this.new Browser(this.cat, this.cart);
      }

      public void statusv(String msg, String detail, String[] buttons, Runnable[] actions) {
         if (this.status != null) {
            this.ui.destroy(this.status);
            this.status = null;
         }

         if (this.detail != null) {
            this.ui.destroy(this.detail);
            this.detail = null;
         }

         if (this.buttons != null) {
            for (Widget btn : this.buttons) {
               this.ui.destroy(btn);
            }

            this.buttons = null;
         }

         int y = this.sz.y / 3;
         if (msg != null) {
            this.status = new Img(Coord.z, Store.textf.render(msg, Button.defcol).tex(), this);
            this.status.c = new Coord((this.sz.x - this.status.sz.x) / 2, y);
            y += this.status.sz.y + 5;
         }

         if (detail != null) {
            this.detail = new Img(Coord.z, Store.descfnd.render(detail, 400).tex(), this);
            this.detail.c = new Coord((this.sz.x - this.detail.sz.x) / 2, y);
            y += this.detail.sz.y + 5;
         }

         if (buttons.length > 0) {
            this.buttons = new Widget[buttons.length];
            int x = (this.sz.x - (buttons.length * 100 + (buttons.length - 1) * 20)) / 2;

            for (int i = 0; i < buttons.length; i++) {
               final Runnable action = actions[i];
               this.buttons[i] = new Button(new Coord(x, y), 100, this, buttons[i]) {
                  @Override
                  public void click() {
                     action.run();
                  }
               };
               x += 120;
            }
         }
      }

      public void status(String msg, String detail, String button, Runnable action) {
         this.statusv(msg, detail, button == null ? new String[0] : new String[]{button}, button == null ? new Runnable[0] : new Runnable[]{action});
      }
   }

   public class CreditCheckouter extends Store.Checkouter {
      private Defer.Future<Object[]> submit = Defer.later(this::submit);
      private Defer.Future<Object[]> execute;
      private String txnid;

      public CreditCheckouter(Store.Catalog cat, Store.Cart cart) {
         super(cat, cart);
         this.status("Checking out...", null, null, null);
      }

      private void authorize() {
         this.status("Executing order...", null, null, null);
         this.execute = Defer.later(this::execute);
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.submit != null) {
            try {
               Map<Object, Object> stat = Utils.mapdecf(this.submit.get());
               this.submit = null;
               if (Utils.eq(stat.get("status"), "ok")) {
                  this.statusv(
                     "Your purchase can be completed on credit alone.",
                     RichText.Parser.quote("Do you wish to continue? " + this.cart.total() + " of store credit will be used."),
                     new String[]{"Confirm", "Return"},
                     new Runnable[]{this::authorize, this::back}
                  );
                  this.txnid = (String)stat.get("cart");
               } else if (Utils.eq(stat.get("status"), "obsolete")) {
                  this.status("The catalog has changed while you were browsing.", null, "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "invalid")) {
                  this.status("The purchase has become invalid.", RichText.Parser.quote((String)stat.get("msg")), "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "err")) {
                  this.status("An unexpected error occurred.", RichText.Parser.quote((String)stat.get("msg")), "Return", this::back);
               }
            } catch (Loading var6) {
            } catch (Defer.DeferredException var7) {
               this.submit = null;
               this.status("An unexpected error occurred.", RichText.Parser.quote(String.valueOf(var7.getCause())), "Return", this::back);
            }
         }

         if (this.execute != null) {
            try {
               Map<Object, Object> stat = Utils.mapdecf(this.execute.get());
               this.execute = null;
               if (Utils.eq(stat.get("status"), "ok")) {
                  this.done();
               } else if (Utils.eq(stat.get("status"), "obsolete")) {
                  this.status("The catalog has changed while you were browsing.", null, "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "invalid")) {
                  this.status("The purchase has become invalid.", RichText.Parser.quote((String)stat.get("msg")), "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "err")) {
                  this.status("An unexpected error occurred.", RichText.Parser.quote((String)stat.get("msg")), "Return", this::back);
               }
            } catch (Loading var4) {
            } catch (Defer.DeferredException var5) {
               this.execute = null;
               this.status("An unexpected error occurred.", RichText.Parser.quote(String.valueOf(var5.getCause())), "Return", this::back);
            }
         }
      }

      private Object[] submit() {
         Map<String, Object> data = new HashMap<>();
         data.put("cart", Store.encode(this.cart));
         data.put("method", "credit");
         data.put("usecredit", this.cart.total().a);
         URLConnection conn = Store.this.req("checkout");
         Store.this.send(conn, Utils.mapencf(data));
         return Store.this.fetch(conn);
      }

      private Object[] execute() {
         URLConnection conn = Store.this.req("creditfin", "cart", this.txnid);
         conn.setDoOutput(true);
         return Store.this.fetch(conn);
      }

      private void done() {
         this.status("Thank you!", "Your purchase has been completed.", "Return", this::reload);
      }
   }

   public abstract static class Currency {
      private static final Map<String, Store.Currency> defined = new HashMap<>();
      public final String symbol;

      public Currency(String symbol) {
         this.symbol = symbol;
      }

      public abstract String format(int var1);

      public static Store.Currency define(Store.Currency c) {
         defined.put(c.symbol, c);
         return c;
      }

      public static Store.Currency decimal(String symbol, final int dec, final String sep, final String fmt) {
         return define(new Store.Currency(symbol) {
            int u = (int)Math.round(Math.pow(10.0, dec));

            private String formata(int amount) {
               int a = amount / this.u;
               int b = amount - a * this.u;
               return String.format("%d%s%0" + dec + "d", a, sep, b);
            }

            @Override
            public String format(int amount) {
               String a = amount < 0 ? "-" + this.formata(-amount) : this.formata(amount);
               return String.format(fmt, a);
            }
         });
      }

      public static Store.Currency get(String symbol) {
         Store.Currency ret = defined.get(symbol);
         if (ret == null) {
            throw new IllegalArgumentException(symbol);
         } else {
            return ret;
         }
      }
   }

   public static class IOError extends RuntimeException {
      public IOError(Throwable cause) {
         super(cause);
      }
   }

   public class Loader extends Widget {
      private final Defer.Future<Store.Catalog> cat = Defer.later(() -> Store.this.catalog());

      public Loader() {
         super(Coord.z, Store.this.asz, Store.this);
         Label l = new Label(Coord.z, this, "Loading...", Store.textf);
         l.c = this.sz.sub(l.sz).div(2);
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.cat.done()) {
            Store.Catalog cat;
            try {
               cat = this.cat.get();
            } catch (Defer.DeferredException var6) {
               Throwable exc = var6.getCause();
               Store.this.new Reloader(exc);
               this.ui.destroy(this);
               return;
            }

            Store.this.new Browser(cat);
            this.ui.destroy(this);
         }
      }
   }

   public static class MessageError extends RuntimeException {
      public MessageError(String msg) {
         super(msg);
      }
   }

   public static class NumberEntry extends TextEntry {
      private String prev = this.text;

      public NumberEntry(Coord c, int w, Widget parent, int def) {
         super(c, w, parent, Integer.toString(def));
      }

      private boolean valid(String t) {
         if (t.equals("")) {
            return true;
         } else {
            try {
               Integer.parseInt(t);
               return true;
            } catch (NumberFormatException var3) {
               return false;
            }
         }
      }

      @Override
      protected void changed() {
         if (this.valid(this.text)) {
            this.prev = this.text;
         } else {
            this.settext(this.prev);
         }
      }

      public int get() {
         try {
            return Integer.parseInt(this.text);
         } catch (NumberFormatException var2) {
            return 0;
         }
      }
   }

   public abstract static class OButton extends Widget {
      public static final int imgsz = 40;
      public static final PUtils.Convolution filter = new PUtils.Lanczos(2.0);
      public final Text text;
      public final Defer.Future<BufferedImage> img;
      private boolean a;
      private Tex rimg;

      public OButton(Coord c, Coord sz, Widget parent, String text, Defer.Future<BufferedImage> img) {
         super(c, sz, parent);
         this.img = img;
         int w = img == null ? sz.x - 20 : sz.x - 25 - 40;
         this.text = Store.texto.renderwrap(text, Button.defcol, w);
      }

      @Override
      public void draw(GOut g) {
         Coord off = this.a ? new Coord(2, 2) : Coord.z;
         if (this.img == null) {
            g.image(this.text.tex(), this.sz.sub(this.text.sz()).div(2).add(off));
         } else {
            try {
               if (this.rimg == null) {
                  BufferedImage rimg = this.img.get();
                  Coord rsz = Utils.imgsz(rimg);
                  Coord ssz = rsz.x > rsz.y ? new Coord(40, rsz.y * 40 / rsz.x) : new Coord(rsz.x * 40 / rsz.y, 40);
                  BufferedImage simg = PUtils.convolvedown(rimg, ssz, filter);
                  this.rimg = new TexI(simg);
               }

               g.image(this.rimg, new Coord(10 + (40 - this.rimg.sz().x) / 2, (this.sz.y - this.rimg.sz().y) / 2).add(off));
            } catch (Loading var7) {
            } catch (Defer.DeferredException var8) {
            }

            g.image(this.text.tex(), new Coord(55, (this.sz.y - this.text.sz().y) / 2).add(off));
         }

         Window.wbox.draw(g, Coord.z, this.sz);
      }

      public abstract void click();

      @Override
      public boolean mousedown(Coord c, int btn) {
         if (btn != 1) {
            return false;
         } else {
            this.a = true;
            this.ui.grabmouse(this);
            return true;
         }
      }

      @Override
      public boolean mouseup(Coord c, int btn) {
         if (this.a && btn == 1) {
            this.a = false;
            this.ui.grabmouse(null);
            if (c.isect(Coord.z, this.sz)) {
               this.click();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static class Offer {
      public final String id;
      public final String ver;
      public String name = "";
      public String desc = null;
      public String category = null;
      public Store.Price price;
      public Defer.Future<BufferedImage> img = null;
      public boolean singleton;
      public int sortkey;

      public Offer(String id, String ver) {
         this.id = id;
         this.ver = ver;
      }
   }

   public static class Price {
      public final Store.Currency c;
      public final int a;

      public Price(Store.Currency c, int a) {
         this.c = c;
         this.a = a;
      }

      @Override
      public String toString() {
         return this.c.format(this.a);
      }

      public static Store.Price parse(Object[] enc) {
         return new Store.Price(Store.Currency.get((String)enc[0]), (Integer)enc[1]);
      }
   }

   public class Reloader extends Widget {
      private boolean done = false;

      public Reloader(Throwable exc) {
         super(Coord.z, Store.this.asz, Store.this);
         String msg = "Error loading catalog";
         if (exc instanceof Store.MessageError) {
            msg = exc.getMessage();
         }

         Label l = new Label(Coord.z, this, msg, Store.textf);
         l.c = this.sz.sub(l.sz).div(2);
         (new Button(Coord.z, 75, this, "Reload") {
            @Override
            public void click() {
               Reloader.this.done = true;
            }
         }).c = new Coord((this.sz.x - 75) / 2, l.c.y + l.sz.y + 10);
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.done) {
            Store.this.new Loader();
            this.ui.destroy(this);
         }
      }
   }

   public class SteamCheckouter extends Store.Checkouter {
      public final String safe = "Your account has not been charged.";
      public final Steam api;
      private Defer.Future<Object[]> submit;
      private Defer.Future<Object[]> execute;
      private Store.SteamCheckouter.Authorizer auth;
      private String txnid;

      public SteamCheckouter(Store.Catalog cat, Store.Cart cart, Steam api) {
         super(cat, cart);
         this.api = api;
         this.submit = Defer.later(this::submit);
         this.status("Checking out...", null, null, null);
      }

      private String unsafe() {
         return "Please check so that your account has not been charged, and contact support with the follow ID if it has: " + this.txnid;
      }

      private String safety(Map<Object, Object> stat) {
         return Utils.eq(stat.get("safe"), 1) ? "Your account has not been charged." : this.unsafe();
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.submit != null) {
            try {
               Map<Object, Object> stat = Utils.mapdecf(this.submit.get());
               this.submit = null;
               if (Utils.eq(stat.get("status"), "ok")) {
                  this.status("Waiting for authorization...", null, null, null);
                  this.api.add(this.auth = new Store.SteamCheckouter.Authorizer());
                  this.txnid = (String)stat.get("cart");
               } else if (Utils.eq(stat.get("status"), "obsolete")) {
                  this.status("The catalog has changed while you were browsing.", null, "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "invalid")) {
                  this.status("The purchase has become invalid.", RichText.Parser.quote((String)stat.get("msg")), "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "err")) {
                  this.status("An unexpected error occurred.", RichText.Parser.quote((String)stat.get("msg")), "Return", this::back);
               }
            } catch (Loading var6) {
            } catch (Defer.DeferredException var7) {
               this.submit = null;
               this.status("An unexpected error occurred.", RichText.Parser.quote(String.valueOf(var7.getCause())), "Return", this::back);
            }
         }

         if (this.auth != null && this.auth.done) {
            if (this.auth.approved) {
               this.status("Executing order...", null, null, null);
               this.execute = Defer.later(this::execute);
            } else {
               this.back();
            }

            this.auth = null;
         }

         if (this.execute != null) {
            try {
               Map<Object, Object> stat = Utils.mapdecf(this.execute.get());
               this.execute = null;
               if (Utils.eq(stat.get("status"), "ok")) {
                  this.done();
               } else if (Utils.eq(stat.get("status"), "obsolete")) {
                  this.status("The catalog has changed while you were browsing.", "Your account has not been charged.", "Reload", this::reload);
               } else if (Utils.eq(stat.get("status"), "invalid")) {
                  this.status(
                     "The purchase has become invalid.",
                     RichText.Parser.quote((String)stat.get("msg")) + "\n" + "Your account has not been charged.",
                     "Reload",
                     this::reload
                  );
               } else if (Utils.eq(stat.get("status"), "err")) {
                  this.status("An unexpected error occurred.", RichText.Parser.quote((String)stat.get("msg")) + "\n" + this.safety(stat), "Return", this::back);
               }
            } catch (Loading var4) {
            } catch (Defer.DeferredException var5) {
               this.execute = null;
               this.status("An unexpected error occurred.", RichText.Parser.quote(String.valueOf(var5.getCause())) + "\n" + this.unsafe(), "Return", this::back);
            }
         }
      }

      private Object[] submit() {
         Map<String, Object> data = new HashMap<>();
         data.put("cart", Store.encode(this.cart));
         data.put("method", "steam");
         URLConnection conn = Store.this.req("checkout");
         Store.this.send(conn, Utils.mapencf(data));
         return Store.this.fetch(conn);
      }

      private Object[] execute() {
         URLConnection conn = Store.this.req("steamfin", "cart", this.txnid);
         conn.setDoOutput(true);
         return Store.this.fetch(conn);
      }

      private void done() {
         this.status("Thank you!", "Your purchase has been completed.", "Return", this::reload);
      }

      public class Authorizer implements Steam.Listener {
         boolean approved;
         boolean done = false;

         @Override
         public void callback(String id, Object[] args) {
            if (id == "onMicroTxnAuthorization") {
               this.approved = (Boolean)args[2];
               this.done = true;
               SteamCheckouter.this.api.remove(this);
            }
         }
      }
   }

   public class Viewer extends Widget {
      public final Store.Offer offer;
      public final Widget back;
      public final Store.Cart cart;
      private Defer.Future<Object[]> status;
      private Tex rimg;

      public Viewer(Store.Offer offer, Widget back, Store.Cart cart) {
         super(Coord.z, Store.this.asz, Store.this);
         this.offer = offer;
         this.back = back;
         this.cart = cart;
         this.status = Defer.later(() -> Store.this.fetch("validate", "offer", offer.id, "ver", offer.ver));
         Widget prev = new Img(new Coord(25, 175), Store.textf.render(offer.name, Button.defcol).tex(), this);
         new IButton(
            new Coord(0, 175).add(new Coord(25, prev.sz.y).sub(Utils.imgsz(Window.lbtni[0])).div(2)), this, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]
         ) {
            @Override
            public void click() {
               Viewer.this.back();
            }
         };
         prev = new Img(new Coord(0, 175), Store.textf.render(offer.price.toString(), Button.defcol).tex(), this);
         prev.c = new Coord(500 - prev.sz.x, prev.c.y);
         if (offer.desc != null) {
            RichTextBox dbox = new RichTextBox(new Coord(0, 200), new Coord(500, 200), this, offer.desc, Store.descfnd);
            dbox.bg = null;
         }

         back.hide();
      }

      private void back() {
         this.ui.destroy(this);
         this.back.show();
      }

      @Override
      public void tick(double dt) {
         super.tick(dt);
         if (this.status != null) {
            Object[] status;
            try {
               status = this.status.get();
            } catch (Loading var7) {
               return;
            } catch (Defer.DeferredException var8) {
               status = new Object[]{"status", "ok"};
            }

            this.status = null;
            Map<String, Object> stat = Utils.mapdecf(status, String.class, Object.class);
            if (Utils.eq(stat.get("status"), "ok")) {
               Store.NumberEntry num = null;
               if (!this.offer.singleton) {
                  new Label(new Coord(300, 430), this, "Quantity:");
                  num = new Store.NumberEntry(new Coord(350, 427), 25, this, 1);
               }

               final Store.NumberEntry fnum = num;
               new Button(new Coord(400, 425), 100, this, "Add to cart") {
                  @Override
                  public void click() {
                     if (Viewer.this.offer.singleton) {
                        Viewer.this.cart.byoffer(Viewer.this.offer, true).num = 1;
                     } else {
                        int n = fnum.get();
                        if (n <= 0) {
                           Store.Cart.Item item = Viewer.this.cart.byoffer(Viewer.this.offer, false);
                           if (item != null) {
                              Viewer.this.cart.remove(item);
                           }
                        } else {
                           Viewer.this.cart.byoffer(Viewer.this.offer, true).num = Math.min(n, 99);
                        }
                     }

                     Viewer.this.back();
                  }
               };
            } else if (Utils.eq(stat.get("status"), "invalid")) {
               new Img(new Coord(200, 400), Text.std.renderwrap((String)stat.get("msg"), new Color(255, 64, 64), 200).tex(), this);
            } else if (Utils.eq(stat.get("status"), "obsolete")) {
               this.ui.destroy(this);
               this.ui.destroy(this.back);
               Store.this.new Loader();
            }
         }
      }

      @Override
      public void draw(GOut g) {
         if (this.offer.img != null) {
            try {
               if (this.rimg == null) {
                  this.rimg = new TexI(this.offer.img.get());
               }

               g.image(this.rimg, new Coord(this.sz.x - 25 - this.rimg.sz().x, 300 - this.rimg.sz().y / 2));
            } catch (Loading var3) {
            }
         }

         super.draw(g);
      }
   }
}
