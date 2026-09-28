package haven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class AccountList extends Widget {
   private static final Coord SZ = new Coord(230, 30);
   private static final Comparator<AccountList.Account> accountComparator = new Comparator<AccountList.Account>() {
      public int compare(AccountList.Account o1, AccountList.Account o2) {
         return o1.name.compareTo(o2.name);
      }
   };
   public int height;
   public int y;
   public final List<AccountList.Account> accounts = new ArrayList<>();

   public AccountList(Coord c, Widget parent, int height) {
      super(c, new Coord(SZ.x, SZ.y * height), parent);
      this.height = height;
      this.y = 0;

      for (Entry<String, String> entry : Config.accounts.entrySet()) {
         this.add(entry.getKey(), entry.getValue());
      }

      Collections.sort(this.accounts, accountComparator);
   }

   public void scroll(int amount) {
      this.y += amount;
      synchronized (this.accounts) {
         if (this.y > this.accounts.size() - this.height) {
            this.y = this.accounts.size() - this.height;
         }
      }

      if (this.y < 0) {
         this.y = 0;
      }
   }

   @Override
   public void draw(GOut g) {
      Coord cc = new Coord(5, 5);
      synchronized (this.accounts) {
         for (AccountList.Account account : this.accounts) {
            account.plb.hide();
            account.del.hide();
         }

         for (int i = 0; i < this.height && i + this.y < this.accounts.size(); i++) {
            AccountList.Account account = this.accounts.get(i + this.y);
            account.plb.show();
            account.plb.c = cc;
            account.del.show();
            account.del.c = cc.add(account.plb.sz.x + 5, 0);
            cc = cc.add(0, SZ.y);
         }
      }

      super.draw(g);
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      this.scroll(amount);
      return true;
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender instanceof Button) {
         synchronized (this.accounts) {
            for (AccountList.Account account : this.accounts) {
               if (sender == account.plb) {
                  Utils.setpref("savedtoken", account.token);
                  Utils.setpref("tokenname", account.name);
                  Utils.setpref("tokenname", account.name);
                  super.wdgmsg("account", account.name, account.token);
                  break;
               }

               if (sender == account.del) {
                  this.remove(account);
                  break;
               }
            }
         }
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   public void add(String name, String token) {
      AccountList.Account c = new AccountList.Account(name, token);
      c.plb = new Button(Coord.z, 200, this, name);
      c.plb.hide();
      c.del = new Button(Coord.z, 20, this, "X");
      c.del.hide();
      synchronized (this.accounts) {
         this.accounts.add(c);
      }
   }

   public void remove(AccountList.Account account) {
      synchronized (this.accounts) {
         this.accounts.remove(account);
      }

      this.scroll(0);
      Config.removeAccount(account.name);
      this.ui.destroy(account.plb);
      this.ui.destroy(account.del);
   }

   public static class Account {
      public String name;
      public String token;
      Button plb;
      Button del;

      public Account(String name, String token) {
         this.name = name;
         this.token = token;
      }
   }
}
