package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tempers extends SIWidget {
  static final RichText.Foundry tmprfnd = new RichText.Foundry(new Object[] { TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, TextAttribute.FOREGROUND, new Color(32, 32, 64), TextAttribute.SIZE, Integer.valueOf(12) });
  
  public static final BufferedImage[] bg = new BufferedImage[] { Resource.loadimg("gfx/hud/tempers/bg1"), Resource.loadimg("gfx/hud/tempers/bg2"), Resource.loadimg("gfx/hud/tempers/bg3"), Resource.loadimg("gfx/hud/tempers/bg4"), Resource.loadimg("gfx/hud/tempers/bg5"), Resource.loadimg("gfx/hud/tempers/bg6"), 
      Resource.loadimg("gfx/hud/tempers/bg7"), Resource.loadimg("gfx/hud/tempers/bg8"), Resource.loadimg("gfx/hud/tempers/bg9"), Resource.loadimg("gfx/hud/tempers/bg10") };
  
  public static final BufferedImage[] bars;
  
  public static final BufferedImage[] sbars;
  
  public static final BufferedImage[] fbars;
  
  public static final BufferedImage lcap = Resource.loadimg("gfx/hud/tempers/lcap");
  
  public static final BufferedImage rcap = Resource.loadimg("gfx/hud/tempers/rcap");
  
  public static final BufferedImage[] gbtni = new BufferedImage[] { Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn") };
  
  public static final Tex crbg = Resource.loadtex("gfx/hud/tempers/crframe");
  
  public static final Coord boxc = new Coord(96, 0);
  
  public static final Coord boxsz = new Coord(339, 62);
  
  public static final Color[] colors = new Color[] { new Color(255, 64, 64), new Color(0, 128, 255), new Color(255, 255, 64), new Color(160, 160, 160) };
  
  public static final String[] tcolors;
  
  static final Color softc = new Color(168, 128, 200);
  
  static final Color foodc = new Color(192, 160, 0);
  
  static final Coord[] mc = new Coord[] { new Coord(295, 11), new Coord(235, 11), new Coord(235, 35), new Coord(295, 35) };
  
  static final String[] anm = new String[] { "blood", "phlegm", "ybile", "bbile" };
  
  static final String[] rnm = new String[] { "Blood", "Phlegm", "Yellow Bile", "Black Bile" };
  
  int[] soft = new int[4], hard = new int[4];
  
  int[] lmax = new int[4];
  
  int insanity = 0;
  
  public boolean gavail = true;
  
  public Indir<Resource> cravail = null;
  
  Tex tt = null;
  
  public Widget gbtn;
  
  public Widget crimg;
  
  private Tex[] texts = null;
  
  private FoodInfo lfood;
  
  static final String NO_RECIPE = "NO_RECIPE";
  
  static final HashMap<String, String> knownRecipes;
  
  static final HashMap<String, String> knownFoodGroups;
  
  static String[] testStrings;
  
  static {
    int n = anm.length;
    BufferedImage[] b = new BufferedImage[n];
    BufferedImage[] s = new BufferedImage[n];
    BufferedImage[] f = new BufferedImage[n];
    for (int i = 0; i < n; i++) {
      b[i] = Resource.loadimg("gfx/hud/tempers/" + anm[i]);
      s[i] = PUtils.monochromize(b[i], softc);
      f[i] = PUtils.monochromize(b[i], foodc);
    } 
    bars = b;
    sbars = s;
    fbars = f;
    String[] buf = new String[colors.length];
    for (int j = 0; j < colors.length; j++) {
      buf[j] = String.format("%d,%d,%d", new Object[] { Integer.valueOf(colors[j].getRed()), Integer.valueOf(colors[j].getGreen()), Integer.valueOf(colors[j].getBlue()) });
    } 
    tcolors = buf;
    knownRecipes = new HashMap<>();
    knownFoodGroups = new HashMap<>();
    testStrings = new String[] { 
        "A Fish in the Reeds", "A Rabbit in the Cabbage", "A Side of Venison", "A Snake in the Grass", "A Walk on the Wild Side", "Absurdly Large Cookie", "Adventurer's Trailmix", "Anadama Bread", "Apple", "Argomoon", 
        "Argopelter Breast", "Argopelter Drumstick", "Argopelter Jerky", "Argopelter Thigh", "Argopelter Wing", "Aspen", "Autumn Delight", "Autumn Gold", "Aztec Abattoir", "Baby Bear", 
        "Baguette", "Bajgiel", "Baked Potato", "Beargarden Biscuit", "Beaver Dam", "Beefy Sandwich", "Beetle", "Berries Jubilee", "Berries-on-a-Straw", "Berry Bajgiel", 
        "Berry Bar", "Berry Cobbler", "Berry Jam", "Berry Salad", "Big Autumn", "Big Red", "Bigbell", "Blackberries", "Blackberry Pectin", "Blubber", 
        "Blue Cheese", "Blue Potato", "Blue Potato Chunk", "Bluebeary", "Boiled Gourd", "Boiled Potato", "Boiled Waning Toadstool", "Boiled Waxing Toadstool", "Boiled Witch's Hat", "Boiled Yellow Morel", 
        "Bottomfeeder Bajgiel", "Braised Beaver Brain", "Breaded Filets", "British Brittle", "Brown Bread", "Brown Potato", "Brown Potato Chunk", "Buddies on a Branch", "Buggy Bajgiel", "Bushbaby", 
        "Bushkin", "Butter", "Cabbage Cakes", "Cabbage Crumbs", "Cabbage Crush Salad", "Cabbage Rolls", "Calamari", "Candied Corn", "Candied Kale Chips", "Candied Oakworth Tart", 
        "Candied Yams", "Candy Gang Candy Cane", "Canterbury Egg", "Carrot", "Caterpillar", "Charred Something", "Cheddar Cheese", "Cherry", "Clam Dots", "Clover Rolls", 
        "Cod Rolls in Lobster Sauce", "Company Bacon", "Connecticut Field", "Cooked Carrot", "Cooked King Crab", "Cooked Suckling Pig", "Corn & Crab Chowder", "Corn Pudding", "Cornmash Gobbler", "Cornmeal Crusted Bluegill", 
        "Cornmeal Flatbread", "Cornucopia", "Crab Cakes", "Cranberry", "Cranberry Pectin", "Cranberry Sauce", "Cream and Cookies", "Crescent Croissant", "Crickebab", "Crispy Cricket", 
        "Crowberry Pectin", "Crowberry Stalk", "Crunchy Rabbit", "Crusted Crawdad", "Curious Cabbage", "Curious Curry", "Curious Grapes", "Curious Indian Corn", "Curious Potato", "Dehydrated Blackberry", 
        "Dehydrated Cranberry", "Dehydrated Crowberry", "Dehydrated Huckleberry", "Delectable Dumplings", "Deviled Beaver", "Domesticated Turkey Breast", "Domesticated Turkey Drumstick", "Domesticated Turkey Thigh", "Domesticated Turkey Wing", "Dragon's Breath Salad", 
        "Dried Angel-Winged Seabass", "Dried Blueback Tuna", "Dried Cape Codfish", "Dried Concord Croaker", "Dried Crimson Carp", "Dried Darkwater Bluegill", "Dried Ghostly Whitefish", "Dried Gold Pickerel", "Dried Hellish Halibut", "Dried Long-Whiskered Catfish", 
        "Dried Popham Pike", "Dried Raging Bullhead", "Dried Red Herring", "Dried Red-Finned Mullet", "Dried Sargasso Eel", "Dried Shin Spinner", "Dried Silt-Dwelling Mudsnapper", "Dried Tiger Trout", "Dried Trunk-Nosed Lake Perch", "Dry Pie", 
        "Eloped Gingerbread Man", "Empire Bluecakes", "English Muffins", "Farmer's Cheese", "Farmer's Omelette", "Filet on the Rocks", "Fish Taco", "Fleshcovered Gourd", "Forbidden Poult", "Franklin Bar", 
        "French Toast", "Fried Blewit", "Fried Frog Legs", "Fried Oysters", "Fried Toad Legs", "Frog Chowder", "Froghetti", "Frontier Stew", "Fruit Jelly", "Fruity Salad", 
        "Funky Pumpkins", "Fyne Salad", "Garden Veg", "Garlic Bread Slices", "Garlic Mashed Potatos", "Garlic Rabbit", "Garlic Spitroast", "Garlic Stuffed Mushrooms", "Ghost Loaf", "Ghost Rider", 
        "Goldtilla", "Grape Cornmeal Cake", "Green Bell Peppers", "Green Cabbage Chuck Roast", "Green Seafood Salad", "Grub", "Grub De La Grub", "Gummy Gills", "Hardboiled Egg", "Harvest Moon", 
        "Hash Browns", "Hickory Nut", "Homefries", "Huckleberries", "Huckleberry Pectin", "Humble Meat Pie", "Jack-o-Lantern", "Jalapeno", "Jam Sandwich", "Jelly Roll", 
        "Jellypoultry", "Johnnycake", "Jonah and the Whales", "Kralith/TemplateTest", "Ladybug", "Lavender Blewit", "Leaf of Colewort", "Leaf of Green Cabbage", "Leaf of Red Cabbage", "Leaf of White Cabbage", 
        "Lean Rabbit", "Lilypoultry", "Lobster Legs", "Lobster Mushroom", "Lobster Tail", "Loin Roast", "Loyalist Lolly", "Lumberjack Frikadel", "Marrow Dumplings", "Marshmallow Chirp", 
        "Meatballs on Noodles", "Meatloaf", "Milkweed Roots", "Monster Cheese", "Mulberry", "Mushroom Pie", "Myrtle Oak Acorn", "New England Dinner", "Nutcracker Suite", "Nutty Bajgiel", 
        "Oakworth", "Oatmeal Crackers", "Onion", "Ostrich Fiddlehead", "Papricharred Charcuterie", "Peach", "Pear", "Pemmican", "Peppered Seafood Medley", "Peppersauce Tuna", 
        "Persimmon", "Pest-Filled Morel", "Pine Nut", "Pisces Iscariot", "Plank Steak", "Plum", "Popcorn", "Popham Patty", "Potato Chunk", "Potato Salad", 
        "Potootie Stick", "Prickly Pear", "Pumpkin Bites", "Pumpkin Butter", "Pumpkin Flesh", "Pumpkin Gnochi", "Pumpkin Pie", "Pumpkin Sandwich", "Radish", "Raisins", 
        "Red Bell Peppers", "Red Grapes", "Red October", "Red Potato", "Red Potato Chunk", "Red Seafood Salad", "Rich Marrow Dumplings", "Roasted Angel-Winged Seabass", "Roasted Bear Cut", "Roasted Bear Steak", 
        "Roasted Beaver Cut", "Roasted Beaver Steak", "Roasted Beef Cut", "Roasted Beef Steak", "Roasted Blueback Tuna", "Roasted Cape Codfish", "Roasted Chestnut", "Roasted Chevon Cut", "Roasted Chevon Steak", "Roasted Concord Croaker", 
        "Roasted Corn on the Cob", "Roasted Cougar Cut", "Roasted Cougar Steak", "Roasted Crab Meat", "Roasted Crimson Carp", "Roasted Darkwater Bluegill", "Roasted Ghostly Whitefish", "Roasted Gold Pickerel", "Roasted Hellish Halibut", "Roasted Hickory Nut", 
        "Roasted Long-Whiskered Catfish", "Roasted Majestic Acorn", "Roasted Mutton Cut", "Roasted Mutton Steak", "Roasted Myrtle Acorns", "Roasted Pine Nut Stuffing", "Roasted Pine Nuts", "Roasted Popham Pike", "Roasted Pork Cut", "Roasted Pork Steak", 
        "Roasted Pumpkin Seeds", "Roasted Rabbit Cut", "Roasted Rabbit Steak", "Roasted Raging Bullhead", "Roasted Red Herring", "Roasted Red-Finned Mullet", "Roasted Sargasso Eel", "Roasted Shin Spinner", "Roasted Silt-Dwelling Mudsnapper", "Roasted Squirrel Cut", 
        "Roasted Testicles", "Roasted Tiger Trout", "Roasted Timber Rattler Cut", "Roasted Timber Rattler Steak", "Roasted Trunk-Nosed Lake Perch", "Roasted Venison Cut", "Roasted Venison Steak", "Roasted Walnut", "Rye Bread", "Salted Radish", 
        "Salty Nuts", "Sauce Chasseur", "Saurcraut", "Sausage Links", "Sauteed Shellshrooms", "Scary Stroganoff", "Scrambled Eggs", "Scuttling Crab Legs", "Sea Loaf", "Seabass in Berry Sauce", 
        "Seaweed Rolls", "Shellfish Omelette", "Shepherds Pie", "Shroom Patty", "Shroom Rolls", "Shroom-Legume Salad", "'Shrooms-on-a-Stick", "'Shroom-Stuffed Bellpepper", "Simple Sunday Steak", "Sizzled Grub", 
        "Sizzled Slug", "Sizzling Stirfry", "Slice of Pie", "Slow Roast", "Small Sugar", "Smoked Bear Cut", "Smoked Beaver Cut", "Smoked Cougar Cut", "Smoked Deer Cut", "Smoked Oyster Meat", 
        "Smoked Rabbit Cut", "Smoked Rattler Cut", "Smoked Squirrel Cut", "Snozberry", "Soulcake", "Spooktacular", "Spooky Ghosts", "Spooky Redcap", "Spore Bajgiel", "Stray Chestnut", 
        "Sugar Caps", "Sugar Delight", "Sugar Treat", "Surf & Turf", "Sweet Bambi", "Sweet Jerky", "Sweet Monarch", "Sweet Walnut Crusted Fish", "Swiss Cheese", "Tamale", 
        "Tasty Cakes", "Tasty Taco", "Tenderboiled Terrine", "Tendergrass Rump", "Things with Wings", "Three Virginians", "Tigerkraut", "Toad in the Hole", "Tomato", "Tortilla", 
        "Trailmix", "Truffle Taffy", "Turkey Jerky", "Turkey Sandwich", "Turkish Delight", "Turtoadit", "Valentine Candies", "Veg Sugar", "Venison with Pickled Nuts", "Virginia Snail", 
        "Virginian Shellroast", "Walnut", "Waning Toadstool", "Waxing Toadstool", "White Grapes", "Wild Garlic", "Wild Salad", "Wild Tuber", "Wild Turkey Breast", "Wild Turkey Drumstick", 
        "Wild Turkey Thigh", "Wild Turkey Wing", "Wild Wings", "Wildberry Pie", "Windy Pooh", "Witch's Hat", "Wortbaked Wartbite", "Yellow Morel", "Yellow Potato", "Yellow Potato Chunk" };
  }
  
  public Tempers(Coord c, Widget parent) {
    super(c, PUtils.imgsz(bg[0]), parent);
    knownRecipes.put("a fish in the reeds", "paginae/craft/inthereeds");
    knownRecipes.put("a rabbit in the cabbage", "paginae/craft/cabbagerabbit");
    knownRecipes.put("a side of venison", "paginae/craft/sideofvenison");
    knownRecipes.put("a snake in the grass", "paginae/craft/snakeingrass");
    knownRecipes.put("a walk on the wild side", "paginae/craft/wildwalk");
    knownRecipes.put("absurdly large cookie", "NO_RECIPE");
    knownRecipes.put("adventurers trailmix", "paginae/craft/advtrailmix");
    knownRecipes.put("anadama bread", "paginae/craft/anadamabread");
    knownRecipes.put("apple", "NO_RECIPE");
    knownRecipes.put("argomoon", "paginae/craft/unbakedargomoon");
    knownRecipes.put("argopelter breast", "paginae/craft/argostuffeddough");
    knownRecipes.put("argopelter drumstick", "paginae/craft/argostuffeddough");
    knownRecipes.put("argopelter jerky", "paginae/craft/argostuffeddough");
    knownRecipes.put("argopelter thigh", "paginae/craft/argostuffeddough");
    knownRecipes.put("argopelter wing", "paginae/craft/argostuffeddough");
    knownRecipes.put("aspen", "NO_RECIPE");
    knownRecipes.put("autumn delight", "paginae/craft/autumndough");
    knownRecipes.put("autumn gold", "NO_RECIPE");
    knownRecipes.put("aztec abattoir", "paginae/craft/aztecu");
    knownRecipes.put("baby bear", "NO_RECIPE");
    knownRecipes.put("baguette", "paginae/craft/baguettesdoughul");
    knownRecipes.put("bajgiel", "paginae/craft/bajgieldoughub");
    knownRecipes.put("baked potato", "NO_RECIPE");
    knownRecipes.put("beargarden biscuit", "NO_RECIPE");
    knownRecipes.put("beaver dam", "paginae/craft/beaverdam");
    knownRecipes.put("beefy sandwich", "NO_RECIPE");
    knownRecipes.put("beetle", "NO_RECIPE");
    knownRecipes.put("berry jubilee", "paginae/craft/berryjubilee");
    knownRecipes.put("berries-on-a-straw", "paginae/craft/berrystraw");
    knownRecipes.put("berry bajgiel", "paginae/craft/berrybajgiel");
    knownRecipes.put("berry bar", "paginae/craft/berrybar");
    knownRecipes.put("berry cobbler", "paginae/craft/uncookedcobbler");
    knownRecipes.put("berry jam", "paginae/craft/berryjam");
    knownRecipes.put("berry salad", "paginae/craft/berrysalad");
    knownRecipes.put("big autumn", "NO_RECIPE");
    knownRecipes.put("big red", "NO_RECIPE");
    knownRecipes.put("bigbell", "paginae/craft/unbakedbigbell");
    knownRecipes.put("blackberries", "NO_RECIPE");
    knownRecipes.put("blackberry pectin", "NO_RECIPE");
    knownRecipes.put("blubber", "NO_RECIPE");
    knownRecipes.put("blue cheese", "NO_RECIPE");
    knownRecipes.put("blue potato", "NO_RECIPE");
    knownRecipes.put("blue potato chunk", "NO_RECIPE");
    knownRecipes.put("bluebeary", "paginae/craft/bluebearyu");
    knownRecipes.put("boiled gourd", "NO_RECIPE");
    knownRecipes.put("boiled potato", "NO_RECIPE");
    knownRecipes.put("boiled waning toadstool", "NO_RECIPE");
    knownRecipes.put("boiled waxing toadstool", "NO_RECIPE");
    knownRecipes.put("boiled witchs hat", "NO_RECIPE");
    knownRecipes.put("boiled yellow morel", "NO_RECIPE");
    knownRecipes.put("bottomfeeder bajgiel", "paginae/craft/bottomfeederbajgiel");
    knownRecipes.put("braised beaver brain", "paginae/craft/beaverbrainu");
    knownRecipes.put("breaded filets", "paginae/craft/breadedfilets");
    knownRecipes.put("british brittle", "NO_RECIPE");
    knownRecipes.put("brown bread", "paginae/craft/brownbreaddough");
    knownRecipes.put("brown potato", "NO_RECIPE");
    knownRecipes.put("brown potato chunk", "NO_RECIPE");
    knownRecipes.put("buddies on a branch", "paginae/craft/branchbuddies");
    knownRecipes.put("buggy bajgiel", "paginae/craft/bugbajgiel");
    knownRecipes.put("bushbaby", "paginae/craft/unbakedbushbaby");
    knownRecipes.put("bushkin", "NO_RECIPE");
    knownRecipes.put("butter", "paginae/craft/butter");
    knownRecipes.put("cabbage cakes", "paginae/craft/cabbagecakesdough");
    knownRecipes.put("cabbage crumbs", "paginae/craft/cabbagecrumbs");
    knownRecipes.put("cabbage crush salad", "paginae/craft/cabbagecrush");
    knownRecipes.put("cabbage rolls", "paginae/craft/cabbagerolls");
    knownRecipes.put("calamari", "NO_RECIPE");
    knownRecipes.put("candied corn", "NO_RECIPE");
    knownRecipes.put("candied kale chips", "NO_RECIPE");
    knownRecipes.put("candied oakworth tart", "paginae/craft/owtartdoughul");
    knownRecipes.put("candied yams", "paginae/craft/candiedyams");
    knownRecipes.put("candy gang candy cane", "NO_RECIPE");
    knownRecipes.put("canterbury egg", "NO_RECIPE");
    knownRecipes.put("carrot", "NO_RECIPE");
    knownRecipes.put("caterpillar", "NO_RECIPE");
    knownRecipes.put("charred something", "paginae/craft/roastmeat");
    knownRecipes.put("cheddar cheese", "NO_RECIPE");
    knownRecipes.put("cherry", "NO_RECIPE");
    knownRecipes.put("clam dots", "NO_RECIPE");
    knownRecipes.put("clover rolls", "paginae/craft/cloverrolldoughul");
    knownRecipes.put("cod rolls in lobster sauce", "paginae/craft/codrolls");
    knownRecipes.put("company bacon", "NO_RECIPE");
    knownRecipes.put("connecticut field", "NO_RECIPE");
    knownRecipes.put("cooked carrot", "paginae/craft/cookedcarrot");
    knownRecipes.put("cooked king crab", "NO_RECIPE");
    knownRecipes.put("cooked suckling pig", "paginae/craft/sucklingpigcooked");
    knownRecipes.put("corn & crab chowder", "paginae/craft/cornchowder");
    knownRecipes.put("corn pudding", "paginae/craft/cornpuddingdough");
    knownRecipes.put("cornmash gobbler", "paginae/craft/cornmashgobbleru");
    knownRecipes.put("cornmeal crusted bluegill", "paginae/craft/cornmealbluegill");
    knownRecipes.put("cornmeal flatbread", "paginae/craft/cornmealflatbreaddough");
    knownRecipes.put("cornucopia", "NO_RECIPE");
    knownRecipes.put("crab cakes", "paginae/craft/crabcakedough");
    knownRecipes.put("cranberry", "NO_RECIPE");
    knownRecipes.put("cranberry pectin", "NO_RECIPE");
    knownRecipes.put("cranberry sauce", "paginae/craft/cranberrysauce");
    knownRecipes.put("cream and cookies", "NO_RECIPE");
    knownRecipes.put("crescent croissant", "paginae/craft/ccdoughul");
    knownRecipes.put("crickebab", "paginae/craft/crickebab");
    knownRecipes.put("crispy cricket", "paginae/craft/crispycricket");
    knownRecipes.put("crowberry pectin", "NO_RECIPE");
    knownRecipes.put("crowberry stalk", "NO_RECIPE");
    knownRecipes.put("crunchy rabbit", "paginae/craft/crunchyrabbit");
    knownRecipes.put("crusted crawdad", "paginae/craft/crustedcrawdad");
    knownRecipes.put("curious cabbage", "NO_RECIPE");
    knownRecipes.put("curious curry", "paginae/craft/curry");
    knownRecipes.put("curious grapes", "NO_RECIPE");
    knownRecipes.put("curious indian corn", "NO_RECIPE");
    knownRecipes.put("curious potato", "NO_RECIPE");
    knownRecipes.put("dehydrated blackberry", "NO_RECIPE");
    knownRecipes.put("dehydrated cranberry", "NO_RECIPE");
    knownRecipes.put("dehydrated crowberry", "NO_RECIPE");
    knownRecipes.put("dehydrated huckleberry", "NO_RECIPE");
    knownRecipes.put("delectable dumplings", "paginae/craft/ddumplingsu");
    knownRecipes.put("deviled beaver", "paginae/craft/deviledbeaver");
    knownRecipes.put("domesticated turkey breast", "paginae/craft/turkeystuffeddough");
    knownRecipes.put("domesticated turkey drumstick", "paginae/craft/turkeystuffeddough");
    knownRecipes.put("domesticated turkey thigh", "paginae/craft/turkeystuffeddough");
    knownRecipes.put("domesticated turkey wing", "paginae/craft/turkeystuffeddough");
    knownRecipes.put("dragons breath salad", "paginae/craft/dragonbreath");
    knownRecipes.put("dried angel-winged seabass", "NO_RECIPE");
    knownRecipes.put("dried blueback tuna", "NO_RECIPE");
    knownRecipes.put("dried cape codfish", "NO_RECIPE");
    knownRecipes.put("dried concord croaker", "NO_RECIPE");
    knownRecipes.put("dried crimson carp", "NO_RECIPE");
    knownRecipes.put("dried darkwater bluegill", "NO_RECIPE");
    knownRecipes.put("dried ghostly whitefish", "NO_RECIPE");
    knownRecipes.put("dried gold pickerel", "NO_RECIPE");
    knownRecipes.put("dried hellish halibut", "NO_RECIPE");
    knownRecipes.put("dried long-whiskered catfish", "NO_RECIPE");
    knownRecipes.put("dried popham pike", "NO_RECIPE");
    knownRecipes.put("dried raging bullhead", "NO_RECIPE");
    knownRecipes.put("dried red herring", "NO_RECIPE");
    knownRecipes.put("dried red-finned mullet", "NO_RECIPE");
    knownRecipes.put("dried sargasso eel", "NO_RECIPE");
    knownRecipes.put("dried shin spinner", "NO_RECIPE");
    knownRecipes.put("dried silt-dwelling mudsnapper", "NO_RECIPE");
    knownRecipes.put("dried tiger trout", "NO_RECIPE");
    knownRecipes.put("dried trunk-nosed lake perch", "NO_RECIPE");
    knownRecipes.put("dry pie", "paginae/craft/drypiedoughul");
    knownRecipes.put("eloped gingerbread man", "NO_RECIPE");
    knownRecipes.put("empire bluecakes", "paginae/craft/empirebluecakesdough");
    knownRecipes.put("english muffins", "paginae/craft/englishmuffinsdoughul");
    knownRecipes.put("farmers cheese", "NO_RECIPE");
    knownRecipes.put("farmers omelette", "paginae/craft/farmomelette");
    knownRecipes.put("filet on the rocks", "paginae/craft/filetonrocks");
    knownRecipes.put("fish taco", "paginae/craft/fishtacos");
    knownRecipes.put("fleshcovered gourd", "paginae/craft/fleshgourd");
    knownRecipes.put("forbidden poult", "NO_RECIPE");
    knownRecipes.put("franklin bar", "NO_RECIPE");
    knownRecipes.put("french toast", "paginae/craft/frenchtoast");
    knownRecipes.put("fried blewit", "paginae/craft/friedblewit");
    knownRecipes.put("fried frog legs", "paginae/craft/friedfroglegs");
    knownRecipes.put("fried oysters", "paginae/craft/friedoysters");
    knownRecipes.put("fried toad legs", "paginae/craft/friedfroglegs");
    knownRecipes.put("frog chowder", "paginae/craft/frogchowder");
    knownRecipes.put("froghetti", "paginae/craft/froghetti");
    knownRecipes.put("frontier stew", "paginae/craft/stew");
    knownRecipes.put("fruit jelly", "paginae/craft/fruitjelly");
    knownRecipes.put("fruity salad", "paginae/craft/fruitysalad");
    knownRecipes.put("funky pumpkins", "paginae/craft/funkypumpkins");
    knownRecipes.put("fyne salad", "paginae/craft/fynesalad");
    knownRecipes.put("garden veg", "paginae/craft/gardenveg");
    knownRecipes.put("garlic bread slices", "paginae/craft/garlicbread");
    knownRecipes.put("garlic mashed potatos", "paginae/craft/garlicmashedpotatos");
    knownRecipes.put("garlic rabbit", "paginae/craft/garlicrabbit");
    knownRecipes.put("garlic spitroast", "paginae/craft/garlicspitroast");
    knownRecipes.put("garlic stuffed mushrooms", "paginae/craft/garlicmushroom");
    knownRecipes.put("ghost loaf", "paginae/craft/ghostloafdough");
    knownRecipes.put("ghost rider", "NO_RECIPE");
    knownRecipes.put("goldtilla", "paginae/craft/goldtilladough");
    knownRecipes.put("grape cornmeal cake", "paginae/craft/grapecornmealcakedough");
    knownRecipes.put("green bell peppers", "NO_RECIPE");
    knownRecipes.put("green cabbage chuck roast", "paginae/craft/chuckroast");
    knownRecipes.put("green seafood salad", "paginae/craft/crabsalad");
    knownRecipes.put("grub", "NO_RECIPE");
    knownRecipes.put("grub de la grub", "paginae/craft/grubdelagrub");
    knownRecipes.put("gummy gills", "NO_RECIPE");
    knownRecipes.put("hardboiled egg", "NO_RECIPE");
    knownRecipes.put("harvest moon", "NO_RECIPE");
    knownRecipes.put("hash browns", "paginae/craft/hashbrowns");
    knownRecipes.put("hickory nut", "NO_RECIPE");
    knownRecipes.put("homefries", "paginae/craft/homefries");
    knownRecipes.put("huckleberries", "NO_RECIPE");
    knownRecipes.put("huckleberry pectin", "NO_RECIPE");
    knownRecipes.put("humble meat pie", "paginae/craft/humblemeatpie");
    knownRecipes.put("jack-o-lantern", "NO_RECIPE");
    knownRecipes.put("jalapeno", "NO_RECIPE");
    knownRecipes.put("jam sandwich", "paginae/craft/jamsandwitch");
    knownRecipes.put("jelly roll", "paginae/craft/jellyroll");
    knownRecipes.put("jellypoultry", "paginae/craft/jellypoultry");
    knownRecipes.put("johnnycake", "paginae/craft/johnnycake");
    knownRecipes.put("jonah and the whales", "paginae/craft/thewhales");
    knownRecipes.put("ladybug", "NO_RECIPE");
    knownRecipes.put("lavender blewit", "NO_RECIPE");
    knownRecipes.put("leaf of colewort", "NO_RECIPE");
    knownRecipes.put("leaf of green cabbage", "NO_RECIPE");
    knownRecipes.put("leaf of red cabbage", "NO_RECIPE");
    knownRecipes.put("leaf of white cabbage", "NO_RECIPE");
    knownRecipes.put("lean rabbit", "paginae/craft/leanrabbit");
    knownRecipes.put("lilypoultry", "paginae/craft/lilypoultry");
    knownRecipes.put("loaf of ryebread", "paginae/craft/ryedoughul");
    knownRecipes.put("lobster legs", "paginae/craft/lobsterlegs");
    knownRecipes.put("lobster mushroom", "NO_RECIPE");
    knownRecipes.put("lobster tail", "NO_RECIPE");
    knownRecipes.put("loin roast", "paginae/craft/loinroast");
    knownRecipes.put("loyalist lolly", "NO_RECIPE");
    knownRecipes.put("lumberjack frikadel", "paginae/craft/frikadel");
    knownRecipes.put("marrow dumplings", "paginae/craft/marrowdumplingsu");
    knownRecipes.put("marshmallow chirp", "NO_RECIPE");
    knownRecipes.put("meatballs on noodles", "paginae/craft/meatballs");
    knownRecipes.put("meatloaf", "paginae/craft/meatloafu");
    knownRecipes.put("milkweed roots", "NO_RECIPE");
    knownRecipes.put("monster cheese", "NO_RECIPE");
    knownRecipes.put("mulberry", "NO_RECIPE");
    knownRecipes.put("mushroom pie", "paginae/craft/mushroompiedough");
    knownRecipes.put("myrtle oak acorn", "NO_RECIPE");
    knownRecipes.put("new england dinner", "paginae/craft/newenglanddinner");
    knownRecipes.put("nutcracker suite", "paginae/craft/nutcrackeru");
    knownRecipes.put("nutty bajgiel", "paginae/craft/nuttybajgiel");
    knownRecipes.put("oakworth", "NO_RECIPE");
    knownRecipes.put("oatmeal crackers", "paginae/craft/oatmealcrackersdough");
    knownRecipes.put("onion", "NO_RECIPE");
    knownRecipes.put("ostrich fiddlehead", "NO_RECIPE");
    knownRecipes.put("papricharred charcuterie", "paginae/craft/papricharredu");
    knownRecipes.put("peach", "NO_RECIPE");
    knownRecipes.put("pear", "NO_RECIPE");
    knownRecipes.put("pemmican", "paginae/craft/pemmican");
    knownRecipes.put("peppered seafood medley", "paginae/craft/pepseafoodmedley");
    knownRecipes.put("peppersauce tuna", "paginae/craft/peppertuna");
    knownRecipes.put("persimmon", "NO_RECIPE");
    knownRecipes.put("pest-filled morel", "paginae/craft/pestmorel");
    knownRecipes.put("pine nut", "NO_RECIPE");
    knownRecipes.put("pisces iscariot", "paginae/craft/iscariot");
    knownRecipes.put("plank steak", "paginae/craft/planksteaku");
    knownRecipes.put("plum", "NO_RECIPE");
    knownRecipes.put("popcorn", "paginae/craft/popcorn");
    knownRecipes.put("popham patty", "paginae/craft/pophampatty");
    knownRecipes.put("potato chunk", "NO_RECIPE");
    knownRecipes.put("potato salad", "paginae/craft/potatosalad");
    knownRecipes.put("potootie stick", "NO_RECIPE");
    knownRecipes.put("prickly pear", "NO_RECIPE");
    knownRecipes.put("pumpkin bites", "NO_RECIPE");
    knownRecipes.put("pumpkin butter", "paginae/craft/pumpkinbutter");
    knownRecipes.put("pumpkin flesh", "NO_RECIPE");
    knownRecipes.put("pumpkin gnochi", "paginae/craft/pumpkingnochi");
    knownRecipes.put("pumpkin pie", "paginae/craft/pumpkinpiedough");
    knownRecipes.put("pumpkin sandwich", "paginae/craft/pumpkinsandwich");
    knownRecipes.put("radish", "NO_RECIPE");
    knownRecipes.put("raisins", "NO_RECIPE");
    knownRecipes.put("red bell peppers", "NO_RECIPE");
    knownRecipes.put("red grapes", "NO_RECIPE");
    knownRecipes.put("red october", "paginae/craft/unbakedredoctober");
    knownRecipes.put("red potato", "NO_RECIPE");
    knownRecipes.put("red potato chunk", "NO_RECIPE");
    knownRecipes.put("red seafood salad", "paginae/craft/redseafoodsalad");
    knownRecipes.put("rich marrow dumplings", "paginae/craft/marrowdumplingsu");
    knownRecipes.put("roasted angel-winged seabass", "paginae/craft/roastfish");
    knownRecipes.put("roasted bear cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted bear steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted beaver cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted beaver steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted beef cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted beef steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted blueback tuna", "paginae/craft/roastfish");
    knownRecipes.put("roasted cape codfish", "paginae/craft/roastfish");
    knownRecipes.put("roasted chestnut", "paginae/craft/roastnuts");
    knownRecipes.put("roasted chevon cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted chevon steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted concord croaker", "paginae/craft/roastfish");
    knownRecipes.put("roasted corn on the cob", "paginae/craft/roastcorn");
    knownRecipes.put("roasted cougar cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted cougar steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted crab meat", "paginae/craft/roastcrabmeat");
    knownRecipes.put("roasted crimson carp", "paginae/craft/roastfish");
    knownRecipes.put("roasted darkwater bluegill", "paginae/craft/roastfish");
    knownRecipes.put("roasted ghostly whitefish", "paginae/craft/roastfish");
    knownRecipes.put("roasted gold pickerel", "paginae/craft/roastfish");
    knownRecipes.put("roasted hellish halibut", "paginae/craft/roastfish");
    knownRecipes.put("roasted hickory nut", "paginae/craft/roastnuts");
    knownRecipes.put("roasted long-whiskered catfish", "paginae/craft/roastfish");
    knownRecipes.put("roasted majestic acorn", "paginae/craft/roastnuts");
    knownRecipes.put("roasted mutton cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted mutton steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted myrtle acorns", "paginae/craft/roastnuts");
    knownRecipes.put("roasted pine nut stuffing", "paginae/craft/nutstuffing");
    knownRecipes.put("roasted pine nuts", "paginae/craft/roastnuts");
    knownRecipes.put("roasted popham pike", "paginae/craft/roastfish");
    knownRecipes.put("roasted pork cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted pork steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted pumpkin seeds", "paginae/craft/roastedpumpkinseeds");
    knownRecipes.put("roasted rabbit cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted rabbit steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted raging bullhead", "paginae/craft/roastfish");
    knownRecipes.put("roasted red herring", "paginae/craft/roastfish");
    knownRecipes.put("roasted red-finned mullet", "paginae/craft/roastfish");
    knownRecipes.put("roasted sargasso eel", "paginae/craft/roastfish");
    knownRecipes.put("roasted shin spinner", "paginae/craft/roastfish");
    knownRecipes.put("roasted silt-dwelling mudsnapper", "paginae/craft/roastfish");
    knownRecipes.put("roasted squirrel cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted testicles", "paginae/craft/roastnuts");
    knownRecipes.put("roasted tiger trout", "paginae/craft/roastfish");
    knownRecipes.put("roasted timber rattler cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted timber rattler steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted trunk-nosed lake perch", "paginae/craft/roastfish");
    knownRecipes.put("roasted venison cut", "paginae/craft/roastmeat");
    knownRecipes.put("roasted venison steak", "paginae/craft/roastmeat");
    knownRecipes.put("roasted walnut", "paginae/craft/roastnuts");
    knownRecipes.put("rye bread", "paginae/craft/ryedoughul");
    knownRecipes.put("salted radish", "paginae/craft/saltedradish");
    knownRecipes.put("salty nuts", "paginae/craft/saltynuts");
    knownRecipes.put("sauce chasseur", "paginae/craft/saucechasseuru");
    knownRecipes.put("saurkraut", "paginae/craft/unfermentedcabbage");
    knownRecipes.put("sausage links", "paginae/craft/sausagelinks");
    knownRecipes.put("sauteed shellshrooms", "paginae/craft/shellshrooms");
    knownRecipes.put("scary stroganoff", "paginae/craft/scarystroganoff");
    knownRecipes.put("scrambled eggs", "paginae/craft/scrambledeggs");
    knownRecipes.put("scuttling crab legs", "NO_RECIPE");
    knownRecipes.put("sea loaf", "paginae/craft/sealoafu");
    knownRecipes.put("seabass in berry sauce", "paginae/craft/berryseabass");
    knownRecipes.put("seaweed rolls", "paginae/craft/searolls");
    knownRecipes.put("shellfish omelette", "paginae/craft/shellfishomelette");
    knownRecipes.put("shepherds pie", "paginae/craft/uncookedshepherdspie");
    knownRecipes.put("shroom patty", "paginae/craft/mushroomfrikadel");
    knownRecipes.put("shroom rolls", "paginae/craft/shroomrolls");
    knownRecipes.put("shroom-legume salad", "paginae/craft/shroomlegume");
    knownRecipes.put("shrooms-on-a-stick", "paginae/craft/shroomsonastick");
    knownRecipes.put("shroom-stuffed bellpepper", "paginae/craft/bakedbelldough");
    knownRecipes.put("simple sunday steak", "paginae/craft/sundaysteaku");
    knownRecipes.put("sizzled grub", "paginae/craft/sizzledgrub");
    knownRecipes.put("sizzled slug", "paginae/craft/sizzledslug");
    knownRecipes.put("sizzling stirfry", "paginae/craft/stirfry");
    knownRecipes.put("slice of pie", "NO_RECIPE");
    knownRecipes.put("slow roast", "paginae/craft/slowroastu");
    knownRecipes.put("small sugar", "NO_RECIPE");
    knownRecipes.put("smoked bear cut", "NO_RECIPE");
    knownRecipes.put("smoked beaver cut", "NO_RECIPE");
    knownRecipes.put("smoked cougar cut", "NO_RECIPE");
    knownRecipes.put("smoked deer cut", "NO_RECIPE");
    knownRecipes.put("smoked oyster meat", "NO_RECIPE");
    knownRecipes.put("smoked rabbit cut", "NO_RECIPE");
    knownRecipes.put("smoked rattler cut", "NO_RECIPE");
    knownRecipes.put("smoked squirrel cut", "NO_RECIPE");
    knownRecipes.put("snozberry", "NO_RECIPE");
    knownRecipes.put("soulcake", "paginae/craft/soulcakedough");
    knownRecipes.put("spooktacular", "NO_RECIPE");
    knownRecipes.put("spooky ghosts", "paginae/craft/spookyghosts");
    knownRecipes.put("spooky redcap", "NO_RECIPE");
    knownRecipes.put("spore bajgiel", "paginae/craft/sporebajgiel");
    knownRecipes.put("stray chestnut", "NO_RECIPE");
    knownRecipes.put("sugar caps", "NO_RECIPE");
    knownRecipes.put("sugar delight", "paginae/craft/sugardelight");
    knownRecipes.put("sugar treat", "NO_RECIPE");
    knownRecipes.put("surf & turf", "paginae/craft/surfturf");
    knownRecipes.put("sweet bambi", "paginae/craft/sweetbambi");
    knownRecipes.put("sweet jerky", "paginae/craft/sweetjerky");
    knownRecipes.put("sweet monarch", "NO_RECIPE");
    knownRecipes.put("sweet walnut crusted fish", "paginae/craft/walnutcatfish");
    knownRecipes.put("swiss cheese", "NO_RECIPE");
    knownRecipes.put("tamale", "paginae/craft/tamale");
    knownRecipes.put("tasty cakes", "paginae/craft/barleycakesdough");
    knownRecipes.put("tasty taco", "paginae/craft/taco");
    knownRecipes.put("tenderboiled terrine", "paginae/craft/tenderboiledu");
    knownRecipes.put("tendergrass rump", "paginae/craft/tendergrass");
    knownRecipes.put("things with wings", "paginae/craft/thingswithwings");
    knownRecipes.put("three virginians", "paginae/craft/dthreevirginians");
    knownRecipes.put("tigerkraut", "paginae/craft/tigerkraut");
    knownRecipes.put("toad in the hole", "paginae/craft/toadintheholeu");
    knownRecipes.put("tomato", "NO_RECIPE");
    knownRecipes.put("tortilla", "paginae/craft/tortilla");
    knownRecipes.put("trailmix", "paginae/craft/trailmix");
    knownRecipes.put("truffle taffy", "NO_RECIPE");
    knownRecipes.put("turkey jerky", "paginae/craft/turkeystuffeddough");
    knownRecipes.put("turkey sandwich", "paginae/craft/turkeysandwich");
    knownRecipes.put("turkish delight", "paginae/craft/turkishdelight");
    knownRecipes.put("turtoadit", "paginae/craft/turtoaditu");
    knownRecipes.put("valentine candies", "NO_RECIPE");
    knownRecipes.put("veg sugar", "NO_RECIPE");
    knownRecipes.put("venison with pickled nuts", "paginae/craft/pickledvenison");
    knownRecipes.put("virginia snail", "NO_RECIPE");
    knownRecipes.put("virginian shellroast", "paginae/craft/shellroast");
    knownRecipes.put("walnut", "NO_RECIPE");
    knownRecipes.put("waning toadstool", "NO_RECIPE");
    knownRecipes.put("waxing toadstool", "NO_RECIPE");
    knownRecipes.put("white grapes", "NO_RECIPE");
    knownRecipes.put("wild garlic", "NO_RECIPE");
    knownRecipes.put("wild salad", "paginae/craft/wildsalad");
    knownRecipes.put("wild tuber", "NO_RECIPE");
    knownRecipes.put("wild turkey breast", "paginae/craft/wildturkeystuffeddough");
    knownRecipes.put("wild turkey drumstick", "paginae/craft/wildturkeystuffeddough");
    knownRecipes.put("wild turkey thigh", "paginae/craft/wildturkeystuffeddough");
    knownRecipes.put("wild turkey wing", "paginae/craft/wildturkeystuffeddough");
    knownRecipes.put("wild wings", "paginae/craft/wildwings");
    knownRecipes.put("wildberry pie", "paginae/craft/wildberrypiedough");
    knownRecipes.put("windy pooh", "paginae/craft/windypoohu");
    knownRecipes.put("witchs hat", "NO_RECIPE");
    knownRecipes.put("wortbaked wartbite", "paginae/craft/dwortbaked");
    knownRecipes.put("yellow morel", "NO_RECIPE");
    knownRecipes.put("yellow potato", "NO_RECIPE");
    knownRecipes.put("yellow potato chunk", "NO_RECIPE");
    knownRecipes.put("darkenbonemeal", "paginae/craft/bonemeal");
    knownRecipes.put("planed board", "paginae/craft/planeboard");
    knownFoodGroups.put("a fish in the reeds", "Fishes");
    knownFoodGroups.put("a rabbit in the cabbage", "Cabbage and Kale");
    knownFoodGroups.put("a side of venison", "Game Meat");
    knownFoodGroups.put("a snake in the grass", "Vegetables and Greens");
    knownFoodGroups.put("a walk on the wild side", "Game Meat");
    knownFoodGroups.put("adventurer's trailmix", "Nuts and Seeds");
    knownFoodGroups.put("anadama bread", "Bread");
    knownFoodGroups.put("apple", "Fruits");
    knownFoodGroups.put("argomoon", "Pumpkins and Gourds");
    knownFoodGroups.put("argopelter breast", "Poultry");
    knownFoodGroups.put("argopelter drumstick", "Poultry");
    knownFoodGroups.put("argopelter jerky", "Poultry");
    knownFoodGroups.put("argopelter thigh", "Poultry");
    knownFoodGroups.put("argopelter wing", "Poultry");
    knownFoodGroups.put("aspen", "Pumpkins and Gourds");
    knownFoodGroups.put("autumn delight", "Nuts and Seeds");
    knownFoodGroups.put("autumn gold", "Pumpkins and Gourds");
    knownFoodGroups.put("aztec abattoir", "Pumpkins and Gourds");
    knownFoodGroups.put("baby bear", "Pumpkins and Gourds");
    knownFoodGroups.put("baguette", "Bread");
    knownFoodGroups.put("bajgiel", "Bread");
    knownFoodGroups.put("baked potato", "Potato Food");
    knownFoodGroups.put("beargarden biscuit", "Bread");
    knownFoodGroups.put("beaver dam", "Game Meat");
    knownFoodGroups.put("beefy sandwich", "Domesticated Meat");
    knownFoodGroups.put("beetle", "Slugs Bugs and Kritters");
    knownFoodGroups.put("berry jubilee", "Fruits");
    knownFoodGroups.put("berries-on-a-straw", "Berries");
    knownFoodGroups.put("berry bajgiel", "Berries");
    knownFoodGroups.put("berry bar", "Berries");
    knownFoodGroups.put("berry cobbler", "Berries");
    knownFoodGroups.put("berry jam", "Berries");
    knownFoodGroups.put("berry salad", "Berries");
    knownFoodGroups.put("big autumn", "Pumpkins and Gourds");
    knownFoodGroups.put("big red", "Vegetables and Greens");
    knownFoodGroups.put("bigbell", "Pumpkins and Gourds");
    knownFoodGroups.put("blackberries", "Berries");
    knownFoodGroups.put("blubber", "Fishes");
    knownFoodGroups.put("blue cheese", "Dairy Foods");
    knownFoodGroups.put("blue potato", "Potato Food");
    knownFoodGroups.put("blue potato chunk", "Potato Food");
    knownFoodGroups.put("bluebeary", "Berries");
    knownFoodGroups.put("boiled gourd", "Pumpkins and Gourds");
    knownFoodGroups.put("boiled potato", "Potato Food");
    knownFoodGroups.put("boiled waning toadstool", "Mushrooms");
    knownFoodGroups.put("boiled waxing toadstool", "Mushrooms");
    knownFoodGroups.put("boiled witch's hat", "Mushrooms");
    knownFoodGroups.put("boiled yellow morel", "Mushrooms");
    knownFoodGroups.put("bottomfeeder bajgiel", "Crustacea and Shellfish");
    knownFoodGroups.put("braised beaver brain", "Game Meat");
    knownFoodGroups.put("breaded filets", "Fishes");
    knownFoodGroups.put("british brittle", "Nuts and Seeds");
    knownFoodGroups.put("brown bread", "Bread");
    knownFoodGroups.put("brown potato", "Potato Food");
    knownFoodGroups.put("brown potato chunk", "Potato Food");
    knownFoodGroups.put("buddies on a branch", "Fishes");
    knownFoodGroups.put("buggy bajgiel", "Slugs Bugs and Kritters");
    knownFoodGroups.put("bushbaby", "Pumpkins and Gourds");
    knownFoodGroups.put("bushkin", "Pumpkins and Gourds");
    knownFoodGroups.put("butter", "Dairy Foods");
    knownFoodGroups.put("cabbage cakes", "Bread");
    knownFoodGroups.put("cabbage crumbs", "Cabbage and Kale");
    knownFoodGroups.put("cabbage crush salad", "Cabbage and Kale");
    knownFoodGroups.put("cabbage rolls", "Cabbage and Kale");
    knownFoodGroups.put("calamari", "Fishes");
    knownFoodGroups.put("candied corn", "Maize");
    knownFoodGroups.put("candied kale chips", "Cabbage and Kale");
    knownFoodGroups.put("candied oakworth tart", "Vegetables and Greens");
    knownFoodGroups.put("candied yams", "Potato Food");
    knownFoodGroups.put("canterbury egg", "Poultry");
    knownFoodGroups.put("carrot", "Vegetables and Greens");
    knownFoodGroups.put("caterpillar", "Slugs Bugs and Kritters");
    knownFoodGroups.put("cheddar cheese", "Dairy Foods");
    knownFoodGroups.put("cherry", "Fruits");
    knownFoodGroups.put("clam dots", "Crustacea and Shellfish");
    knownFoodGroups.put("clover rolls", "Bread");
    knownFoodGroups.put("cod rolls in lobster sauce", "Fishes");
    knownFoodGroups.put("connecticut field", "Pumpkins and Gourds");
    knownFoodGroups.put("cooked carrot", "Vegetables and Greens");
    knownFoodGroups.put("cooked king crab", "Crustacea and Shellfish");
    knownFoodGroups.put("cooked suckling pig", "Domesticated Meat");
    knownFoodGroups.put("corn & crab chowder", "Maize");
    knownFoodGroups.put("corn pudding", "Maize");
    knownFoodGroups.put("cornmash gobbler", "Poultry");
    knownFoodGroups.put("cornmeal crusted bluegill", "Maize");
    knownFoodGroups.put("cornmeal flatbread", "Maize");
    knownFoodGroups.put("crab cakes", "Crustacea and Shellfish");
    knownFoodGroups.put("cranberry", "Berries");
    knownFoodGroups.put("cranberry sauce", "Berries");
    knownFoodGroups.put("cream and cookies", "Dairy Foods");
    knownFoodGroups.put("crescent croissant", "Bread");
    knownFoodGroups.put("crickebab", "Slugs Bugs and Kritters");
    knownFoodGroups.put("crispy cricket", "Slugs Bugs and Kritters");
    knownFoodGroups.put("crowberry stalk", "Berries");
    knownFoodGroups.put("crunchy rabbit", "Nuts and Seeds");
    knownFoodGroups.put("crusted crawdad", "Crustacea and Shellfish");
    knownFoodGroups.put("curious cabbage", "Cabbage and Kale");
    knownFoodGroups.put("curious curry", "Domesticated Meat");
    knownFoodGroups.put("curious grapes", "Fruits");
    knownFoodGroups.put("curious indian corn", "Maize");
    knownFoodGroups.put("curious potato", "Potato Food");
    knownFoodGroups.put("dehydrated blackberry", "Berries");
    knownFoodGroups.put("dehydrated cranberry", "Berries");
    knownFoodGroups.put("dehydrated crowberry", "Berries");
    knownFoodGroups.put("dehydrated huckleberry", "Berries");
    knownFoodGroups.put("delectable dumplings", "Fruits");
    knownFoodGroups.put("deviled beaver", "Game Meat");
    knownFoodGroups.put("domesticated turkey breast", "Poultry");
    knownFoodGroups.put("domesticated turkey drumstick", "Poultry");
    knownFoodGroups.put("domesticated turkey thigh", "Poultry");
    knownFoodGroups.put("domesticated turkey wing", "Poultry");
    knownFoodGroups.put("dragon's breath salad", "Vegetables and Greens");
    knownFoodGroups.put("dried angel-winged seabass", "Fishes");
    knownFoodGroups.put("dried blueback tuna", "Fishes");
    knownFoodGroups.put("dried cape codfish", "Fishes");
    knownFoodGroups.put("dried concord croaker", "Fishes");
    knownFoodGroups.put("dried crimson carp", "Fishes");
    knownFoodGroups.put("dried darkwater bluegill", "Fishes");
    knownFoodGroups.put("dried ghostly whitefish", "Fishes");
    knownFoodGroups.put("dried gold pickerel", "Fishes");
    knownFoodGroups.put("dried hellish halibut", "Fishes");
    knownFoodGroups.put("dried long-whiskered catfish", "Fishes");
    knownFoodGroups.put("dried popham pike", "Fishes");
    knownFoodGroups.put("dried raging bullhead", "Fishes");
    knownFoodGroups.put("dried red herring", "Fishes");
    knownFoodGroups.put("dried red-finned mullet", "Fishes");
    knownFoodGroups.put("dried sargasso eel", "Fishes");
    knownFoodGroups.put("dried shin spinner", "Fishes");
    knownFoodGroups.put("dried silt-dwelling mudsnapper", "Fishes");
    knownFoodGroups.put("dried tiger trout", "Fishes");
    knownFoodGroups.put("dried trunk-nosed lake perch", "Fishes");
    knownFoodGroups.put("dry pie", "Bread");
    knownFoodGroups.put("eloped gingerbread man", "Bread");
    knownFoodGroups.put("empire bluecakes", "Maize");
    knownFoodGroups.put("english muffins", "Bread");
    knownFoodGroups.put("farmer's cheese", "Dairy Foods");
    knownFoodGroups.put("farmer's omelette", "Poultry");
    knownFoodGroups.put("filet on the rocks", "Fishes");
    knownFoodGroups.put("fish taco", "Fishes");
    knownFoodGroups.put("fleshcovered gourd", "Pumpkins and Gourds");
    knownFoodGroups.put("forbidden poult", "Poultry");
    knownFoodGroups.put("franklin bar", "Food");
    knownFoodGroups.put("french toast", "Bread");
    knownFoodGroups.put("fried blewit", "Mushrooms");
    knownFoodGroups.put("fried frog legs", "Slugs Bugs and Kritters");
    knownFoodGroups.put("fried oysters", "Crustacea and Shellfish");
    knownFoodGroups.put("fried toad legs", "Slugs Bugs and Kritters");
    knownFoodGroups.put("frog chowder", "Slugs Bugs and Kritters");
    knownFoodGroups.put("froghetti", "Slugs Bugs and Kritters");
    knownFoodGroups.put("frontier stew", "Potato Food");
    knownFoodGroups.put("fruit jelly", "Berries");
    knownFoodGroups.put("fruity salad", "Fruits");
    knownFoodGroups.put("funky pumpkins", "Pumpkins and Gourds");
    knownFoodGroups.put("fyne salad", "Cabbage and Kale");
    knownFoodGroups.put("garden veg", "Vegetables and Greens");
    knownFoodGroups.put("garlic bread slices", "Bread");
    knownFoodGroups.put("garlic mashed potatos", "Potato Food");
    knownFoodGroups.put("garlic rabbit", "Game Meat");
    knownFoodGroups.put("garlic spitroast", "Game Meat");
    knownFoodGroups.put("garlic stuffed mushrooms", "Mushrooms");
    knownFoodGroups.put("ghost loaf", "Maize");
    knownFoodGroups.put("ghost rider", "Pumpkins and Gourds");
    knownFoodGroups.put("goldtilla", "Maize");
    knownFoodGroups.put("grape cornmeal cake", "Maize");
    knownFoodGroups.put("green bell peppers", "Mushrooms");
    knownFoodGroups.put("green cabbage chuck roast", "Cabbage and Kale");
    knownFoodGroups.put("green seafood salad", "Crustacea and Shellfish");
    knownFoodGroups.put("grub", "Slugs Bugs and Kritters");
    knownFoodGroups.put("grub de la grub", "Slugs Bugs and Kritters");
    knownFoodGroups.put("gummy gills", "Fishes");
    knownFoodGroups.put("hardboiled egg", "Poultry");
    knownFoodGroups.put("harvest moon", "Pumpkins and Gourds");
    knownFoodGroups.put("hash browns", "Potato Food");
    knownFoodGroups.put("hickory nut", "Nuts and Seeds");
    knownFoodGroups.put("homefries", "Potato Food");
    knownFoodGroups.put("huckleberries", "Berries");
    knownFoodGroups.put("humble meat pie", "Game Meat");
    knownFoodGroups.put("jack-o-lantern", "Pumpkins and Gourds");
    knownFoodGroups.put("jalapeno", "Food");
    knownFoodGroups.put("jam sandwich", "Berries");
    knownFoodGroups.put("jelly roll", "Berries");
    knownFoodGroups.put("jellypoultry", "Poultry");
    knownFoodGroups.put("johnnycake", "Maize");
    knownFoodGroups.put("jonah and the whales", "Fishes");
    knownFoodGroups.put("kralith/templatetest", "Berries");
    knownFoodGroups.put("ladybug", "Slugs Bugs and Kritters");
    knownFoodGroups.put("lavender blewit", "Mushrooms");
    knownFoodGroups.put("leaf of colewort", "Cabbage and Kale");
    knownFoodGroups.put("leaf of green cabbage", "Cabbage and Kale");
    knownFoodGroups.put("leaf of red cabbage", "Cabbage and Kale");
    knownFoodGroups.put("leaf of white cabbage", "Cabbage and Kale");
    knownFoodGroups.put("lean rabbit", "Game Meat");
    knownFoodGroups.put("lilypoultry", "Poultry");
    knownFoodGroups.put("loaf of ryebread", "Bread");
    knownFoodGroups.put("lobster legs", "Crustacea and Shellfish");
    knownFoodGroups.put("lobster mushroom", "Mushrooms");
    knownFoodGroups.put("lobster tail", "Crustacea and Shellfish");
    knownFoodGroups.put("loin roast", "Domesticated Meat");
    knownFoodGroups.put("loyalist lolly", "Berries");
    knownFoodGroups.put("lumberjack frikadel", "Game Meat");
    knownFoodGroups.put("marrow dumplings", "Bread");
    knownFoodGroups.put("marshmallow chirp", "Domesticated Meat");
    knownFoodGroups.put("meatballs on noodles", "Domesticated Meat");
    knownFoodGroups.put("meatloaf", "Domesticated Meat");
    knownFoodGroups.put("milkweed roots", "Vegetables and Greens");
    knownFoodGroups.put("monster cheese", "Dairy Foods");
    knownFoodGroups.put("mulberry", "Fruits");
    knownFoodGroups.put("mushroom pie", "Mushrooms");
    knownFoodGroups.put("myrtle oak acorn", "Nuts and Seeds");
    knownFoodGroups.put("new england dinner", "Potato Food");
    knownFoodGroups.put("nutcracker suite", "Nuts and Seeds");
    knownFoodGroups.put("nutty bajgiel", "Nuts and Seeds");
    knownFoodGroups.put("oakworth", "Vegetables and Greens");
    knownFoodGroups.put("oatmeal crackers", "Bread");
    knownFoodGroups.put("onion", "Vegetables and Greens");
    knownFoodGroups.put("ostrich fiddlehead", "Vegetables and Greens");
    knownFoodGroups.put("papricharred charcuterie", "Slugs Bugs and Kritters");
    knownFoodGroups.put("peach", "Fruits");
    knownFoodGroups.put("pear", "Fruits");
    knownFoodGroups.put("pemmican", "Berries");
    knownFoodGroups.put("peppered seafood medley", "Crustacea and Shellfish");
    knownFoodGroups.put("peppersauce tuna", "Fishes");
    knownFoodGroups.put("persimmon", "Fruits");
    knownFoodGroups.put("pest-filled morel", "Slugs Bugs and Kritters");
    knownFoodGroups.put("pine nut", "Nuts and Seeds");
    knownFoodGroups.put("pisces iscariot", "Fishes");
    knownFoodGroups.put("plank steak", "Game Meat");
    knownFoodGroups.put("plum", "Fruits");
    knownFoodGroups.put("popcorn", "Maize");
    knownFoodGroups.put("popham patty", "Domesticated Meat");
    knownFoodGroups.put("potato chunk", "Potato Food");
    knownFoodGroups.put("potato salad", "Potato Food");
    knownFoodGroups.put("potootie stick", "Potato Food");
    knownFoodGroups.put("prickly pear", "Vegetables and Greens");
    knownFoodGroups.put("pumpkin bites", "Pumpkins and Gourds");
    knownFoodGroups.put("pumpkin butter", "Pumpkins and Gourds");
    knownFoodGroups.put("pumpkin flesh", "Pumpkins and Gourds");
    knownFoodGroups.put("pumpkin gnochi", "Pumpkins and Gourds");
    knownFoodGroups.put("pumpkin pie", "Pumpkins and Gourds");
    knownFoodGroups.put("pumpkin sandwich", "Pumpkins and Gourds");
    knownFoodGroups.put("radish", "Vegetables and Greens");
    knownFoodGroups.put("raisins", "Fruits");
    knownFoodGroups.put("red bell peppers", "Pumpkins and Gourds");
    knownFoodGroups.put("red grapes", "Fruits");
    knownFoodGroups.put("red october", "Pumpkins and Gourds");
    knownFoodGroups.put("red potato", "Potato Food");
    knownFoodGroups.put("red potato chunk", "Potato Food");
    knownFoodGroups.put("red seafood salad", "Crustacea and Shellfish");
    knownFoodGroups.put("rich marrow dumplings", "Bread");
    knownFoodGroups.put("roasted angel-winged seabass", "Fishes");
    knownFoodGroups.put("roasted bear cut", "Game Meat");
    knownFoodGroups.put("roasted bear steak", "Game Meat");
    knownFoodGroups.put("roasted beaver cut", "Game Meat");
    knownFoodGroups.put("roasted beaver steak", "Game Meat");
    knownFoodGroups.put("roasted beef cut", "Domesticated Meat");
    knownFoodGroups.put("roasted beef steak", "Domesticated Meat");
    knownFoodGroups.put("roasted blueback tuna", "Fishes");
    knownFoodGroups.put("roasted cape codfish", "Fishes");
    knownFoodGroups.put("roasted chestnut", "Nuts and Seeds");
    knownFoodGroups.put("roasted chevon cut", "Domesticated Meat");
    knownFoodGroups.put("roasted chevon steak", "Domesticated Meat");
    knownFoodGroups.put("roasted concord croaker", "Fishes");
    knownFoodGroups.put("roasted corn on the cob", "Maize");
    knownFoodGroups.put("roasted cougar cut", "Game Meat");
    knownFoodGroups.put("roasted cougar steak", "Game Meat");
    knownFoodGroups.put("roasted crab meat", "Crustacea and Shellfish");
    knownFoodGroups.put("roasted crimson carp", "Fishes");
    knownFoodGroups.put("roasted darkwater bluegill", "Fishes");
    knownFoodGroups.put("roasted ghostly whitefish", "Fishes");
    knownFoodGroups.put("roasted gold pickerel", "Fishes");
    knownFoodGroups.put("roasted hellish halibut", "Fishes");
    knownFoodGroups.put("roasted hickory nut", "Nuts and Seeds");
    knownFoodGroups.put("roasted long-whiskered catfish", "Fishes");
    knownFoodGroups.put("roasted majestic acorn", "Nuts and Seeds");
    knownFoodGroups.put("roasted mutton cut", "Domesticated Meat");
    knownFoodGroups.put("roasted mutton steak", "Domesticated Meat");
    knownFoodGroups.put("roasted myrtle acorns", "Nuts and Seeds");
    knownFoodGroups.put("roasted pine nut stuffing", "Nuts and Seeds");
    knownFoodGroups.put("roasted pine nuts", "Nuts and Seeds");
    knownFoodGroups.put("roasted popham pike", "Fishes");
    knownFoodGroups.put("roasted pork cut", "Domesticated Meat");
    knownFoodGroups.put("roasted pork steak", "Domesticated Meat");
    knownFoodGroups.put("roasted pumpkin seeds", "Pumpkins and Gourds");
    knownFoodGroups.put("roasted rabbit cut", "Game Meat");
    knownFoodGroups.put("roasted rabbit steak", "Game Meat");
    knownFoodGroups.put("roasted raging bullhead", "Fishes");
    knownFoodGroups.put("roasted red herring", "Fishes");
    knownFoodGroups.put("roasted red-finned mullet", "Fishes");
    knownFoodGroups.put("roasted sargasso eel", "Fishes");
    knownFoodGroups.put("roasted shin spinner", "Fishes");
    knownFoodGroups.put("roasted silt-dwelling mudsnapper", "Fishes");
    knownFoodGroups.put("roasted squirrel cut", "Game Meat");
    knownFoodGroups.put("roasted testicles", "Nuts and Seeds");
    knownFoodGroups.put("roasted tiger trout", "Fishes");
    knownFoodGroups.put("roasted timber rattler cut", "Game Meat");
    knownFoodGroups.put("roasted timber rattler steak", "Game Meat");
    knownFoodGroups.put("roasted trunk-nosed lake perch", "Fishes");
    knownFoodGroups.put("roasted venison cut", "Game Meat");
    knownFoodGroups.put("roasted venison steak", "Game Meat");
    knownFoodGroups.put("roasted walnut", "Nuts and Seeds");
    knownFoodGroups.put("rye bread", "Bread");
    knownFoodGroups.put("salted radish", "Vegetables and Greens");
    knownFoodGroups.put("salty nuts", "Nuts and Seeds");
    knownFoodGroups.put("sauce chasseur", "Mushrooms");
    knownFoodGroups.put("saurkraut", "Cabbage and Kale");
    knownFoodGroups.put("sausage links", "Domesticated Meat");
    knownFoodGroups.put("sauteed shellshrooms", "Crustacea and Shellfish");
    knownFoodGroups.put("scary stroganoff", "Domesticated Meat");
    knownFoodGroups.put("scrambled eggs", "Poultry");
    knownFoodGroups.put("scuttling crab legs", "Crustacea and Shellfish");
    knownFoodGroups.put("sea loaf", "Fishes");
    knownFoodGroups.put("seabass in berry sauce", "Berries");
    knownFoodGroups.put("seaweed rolls", "Fishes");
    knownFoodGroups.put("shellfish omelette", "Crustacea and Shellfish");
    knownFoodGroups.put("shepherds pie", "Potato Food");
    knownFoodGroups.put("shroom patty", "Mushrooms");
    knownFoodGroups.put("shroom rolls", "Mushrooms");
    knownFoodGroups.put("shroom-legume salad", "Mushrooms");
    knownFoodGroups.put("'shrooms-on-a-stick", "Mushrooms");
    knownFoodGroups.put("'shroom-stuffed bellpepper", "Mushrooms");
    knownFoodGroups.put("simple sunday steak", "Game Meat");
    knownFoodGroups.put("sizzled grub", "Slugs Bugs and Kritters");
    knownFoodGroups.put("sizzled slug", "Slugs Bugs and Kritters");
    knownFoodGroups.put("sizzling stirfry", "Domesticated Meat");
    knownFoodGroups.put("slow roast", "Domesticated Meat");
    knownFoodGroups.put("small sugar", "Pumpkins and Gourds");
    knownFoodGroups.put("smoked bear cut", "Game Meat");
    knownFoodGroups.put("smoked beaver cut", "Game Meat");
    knownFoodGroups.put("smoked cougar cut", "Game Meat");
    knownFoodGroups.put("smoked deer cut", "Game Meat");
    knownFoodGroups.put("smoked oyster meat", "Crustacea and Shellfish");
    knownFoodGroups.put("smoked rabbit cut", "Game Meat");
    knownFoodGroups.put("smoked rattler cut", "Game Meat");
    knownFoodGroups.put("smoked squirrel cut", "Game Meat");
    knownFoodGroups.put("snozberry", "Fruits");
    knownFoodGroups.put("soulcake", "Pumpkins and Gourds");
    knownFoodGroups.put("spooktacular", "Pumpkins and Gourds");
    knownFoodGroups.put("spooky ghosts", "Fishes");
    knownFoodGroups.put("spooky redcap", "Mushrooms");
    knownFoodGroups.put("spore bajgiel", "Mushrooms");
    knownFoodGroups.put("stray chestnut", "Nuts and Seeds");
    knownFoodGroups.put("sugar caps", "Mushrooms");
    knownFoodGroups.put("sugar delight", "Pumpkins and Gourds");
    knownFoodGroups.put("sugar treat", "Pumpkins and Gourds");
    knownFoodGroups.put("surf & turf", "Crustacea and Shellfish");
    knownFoodGroups.put("sweet bambi", "Mushrooms");
    knownFoodGroups.put("sweet jerky", "Fruits");
    knownFoodGroups.put("sweet monarch", "Slugs Bugs and Kritters");
    knownFoodGroups.put("sweet walnut crusted fish", "Nuts and Seeds");
    knownFoodGroups.put("swiss cheese", "Dairy Foods");
    knownFoodGroups.put("tamale", "Maize");
    knownFoodGroups.put("tasty cakes", "Bread");
    knownFoodGroups.put("tasty taco", "Domesticated Meat");
    knownFoodGroups.put("tenderboiled terrine", "Game Meat");
    knownFoodGroups.put("tendergrass rump", "Vegetables and Greens");
    knownFoodGroups.put("things with wings", "Poultry");
    knownFoodGroups.put("three virginians", "Slugs Bugs and Kritters");
    knownFoodGroups.put("tigerkraut", "Fishes");
    knownFoodGroups.put("toad in the hole", "Domesticated Meat");
    knownFoodGroups.put("tomato", "Vegetables and Greens");
    knownFoodGroups.put("tortilla", "Maize");
    knownFoodGroups.put("trailmix", "Nuts and Seeds");
    knownFoodGroups.put("truffle taffy", "Mushrooms");
    knownFoodGroups.put("turkey jerky", "Poultry");
    knownFoodGroups.put("turkey sandwich", "Poultry");
    knownFoodGroups.put("turkish delight", "Poultry");
    knownFoodGroups.put("turtoadit", "Slugs Bugs and Kritters");
    knownFoodGroups.put("veg sugar", "Vegetables and Greens");
    knownFoodGroups.put("venison with pickled nuts", "Nuts and Seeds");
    knownFoodGroups.put("virginia snail", "Slugs Bugs and Kritters");
    knownFoodGroups.put("virginian shellroast", "Slugs Bugs and Kritters");
    knownFoodGroups.put("walnut", "Nuts and Seeds");
    knownFoodGroups.put("waning toadstool", "Mushrooms");
    knownFoodGroups.put("waxing toadstool", "Mushrooms");
    knownFoodGroups.put("white grapes", "Fruits");
    knownFoodGroups.put("wild garlic", "Vegetables and Greens");
    knownFoodGroups.put("wild salad", "Vegetables and Greens");
    knownFoodGroups.put("wild tuber", "Potato Food");
    knownFoodGroups.put("wild turkey breast", "Poultry");
    knownFoodGroups.put("wild turkey drumstick", "Poultry");
    knownFoodGroups.put("wild turkey thigh", "Poultry");
    knownFoodGroups.put("wild turkey wing", "Poultry");
    knownFoodGroups.put("wild wings", "Poultry");
    knownFoodGroups.put("wildberry pie", "Berries");
    knownFoodGroups.put("windy pooh", "Game Meat");
    knownFoodGroups.put("witch's hat", "Mushrooms");
    knownFoodGroups.put("wortbaked wartbite", "Slugs Bugs and Kritters");
    knownFoodGroups.put("yellow morel", "Mushrooms");
    knownFoodGroups.put("yellow potato", "Potato Food");
    knownFoodGroups.put("yellow potato chunk", "Potato Food");
  }
  
  public void tick(double dt) {
    int[] max = new int[4];
    for (int i = 0; i < 4; i++) {
      max[i] = ((Glob.CAttr)this.ui.sess.glob.cattr.get(anm[i])).comp;
      if (max[i] == 0)
        return; 
      if (max[i] != this.lmax[i]) {
        redraw();
        this.texts = null;
        this.tt = null;
      } 
    } 
    this.lmax = max;
    if (this.gavail && this.gbtn == null) {
      this.gbtn = new IButton(Coord.z, this.parent, gbtni[0], gbtni[1], gbtni[2]) {
          public void reqdestroy() {
            new Widget.NormAnim(0.25D) {
                public void ntick(double a) {
                  Tempers.null.this.c = new Coord(Tempers.this.c.x + (Tempers.this.sz.x - Tempers.null.this.sz.x) / 2, (int)((Tempers.this.c.y + Tempers.boxsz.y) - a * Tempers.null.this.sz.y));
                  if (a == 1.0D)
                    Tempers.null.this.destroy(); 
                }
              };
          }
          
          public void click() {
            ((GameUI)getparent(GameUI.class)).act(new String[] { "gobble" });
          }
          
          public void presize() {
            this.c = new Coord(Tempers.this.c.x + (Tempers.this.sz.x - this.sz.x) / 2, Tempers.this.c.y + Tempers.boxsz.y);
          }
        };
      raise();
      this.ui.gui.updateRenderFilter();
    } else if (!this.gavail && this.gbtn != null) {
      this.gbtn.reqdestroy();
      this.gbtn = null;
    } 
    if (this.cravail != null && this.crimg == null) {
      final Indir<Resource> crres = this.cravail;
      this.crimg = new Widget(Coord.z, crbg.sz(), this.parent) {
          final int xoff = (Tempers.this.sz.x - Tempers.gbtni[0].getWidth()) / 2 - 10 - this.sz.x;
          
          Tex img = null;
          
          void move(double a) {
            this.c = new Coord(Tempers.this.c.x + this.xoff, (int)((Tempers.this.c.y + Tempers.boxsz.y) + (a - 1.0D) * this.sz.y));
          }
          
          public void draw(GOut g) {
            g.image(Tempers.crbg, Coord.z);
            try {
              if (this.img == null)
                this.img = ((Resource.Image)((Resource)crres.get()).<Resource.Image>layer(Resource.imgc)).tex(); 
              g.image(this.img, this.sz.sub(this.img.sz()).div(2));
            } catch (Loading loading) {}
          }
          
          Tex tip = null;
          
          public Object tooltip(Coord c, Widget prev) {
            try {
              if (this.tip == null)
                this.tip = RichText.render("Craving: " + ((Resource.Tooltip)((Resource)crres.get()).layer((Class)Resource.tooltip)).t + getFoodGroup() + "\n\nNEW: Click this Icon to open the recipe!\n(if it has a recipe and you know it)", 200, new Object[0]).tex(); 
              return this.tip;
            } catch (Loading l) {
              return "...";
            } 
          }
          
          private String getFoodGroup() {
            try {
              String text = ((Resource.Tooltip)((Resource)crres.get()).layer((Class)Resource.tooltip)).t.toLowerCase().replace("'", "").replace("\n", "");
              if (Tempers.knownFoodGroups.containsKey(text))
                return "\n\nFood Group: " + (String)Tempers.knownFoodGroups.get(text); 
            } catch (Exception exception) {}
            return "";
          }
          
          public void reqdestroy() {
            new Widget.NormAnim(0.25D) {
                public void ntick(double a) {
                  Tempers.null.this.move(1.0D - a);
                  if (a == 1.0D)
                    Tempers.null.this.destroy(); 
                }
              };
          }
          
          public void presize() {
            move(1.0D);
          }
          
          public boolean mousedown(Coord c, int button) {
            String text = "";
            boolean isCalledForCraving = true;
            try {
              text = ((Resource.Tooltip)((Resource)crres.get()).layer((Class)Resource.tooltip)).t.toLowerCase().replace("'", "");
            } catch (Exception exception) {}
            Tempers.this.getAndOpenRecipeByName(crres, text, true, true);
            return super.mousedown(c, button);
          }
        };
    } 
    FoodInfo food = null;
    if (this.ui.lasttip instanceof WItem.ItemTip)
      try {
        food = ItemInfo.<FoodInfo>find(FoodInfo.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
      } catch (Loading loading) {} 
    if (this.lfood != food) {
      this.lfood = food;
      redraw();
    } 
  }
  
  public static void testCravings() {
    Set<Glob.Pagina> paginae = UI.instance.sess.glob.paginae;
    for (Map.Entry<String, String> es : knownRecipes.entrySet()) {
      Glob.Pagina p = Utils.getPagina(es.getValue());
      if (es.getValue() != "NO_RECIPE" && p == null)
        Utils.msgLog((String)es.getKey() + "   " + (String)es.getValue()); 
    } 
  }
  
  public static void testCravings2() {
    Map<String, String> resultMap = new HashMap<>();
    for (String tString : testStrings) {
      try {
        Set<Glob.Pagina> paginae = UI.instance.sess.glob.paginae;
        String text = tString.toLowerCase();
        String[] split = text.split(" ");
        Set<Glob.Pagina> matches = new HashSet<>();
        Glob.Pagina target = null;
        int maxHits = 0;
        for (Glob.Pagina p : paginae) {
          int hits = 0;
          for (String string : split) {
            if (string.endsWith("ed"))
              string = string.substring(0, string.length() - 2); 
            if ((p.res()).name.toLowerCase().contains("/craft/") && (p.res()).name.toLowerCase().contains(string))
              hits++; 
          } 
          if (hits > 0 && hits >= maxHits) {
            if (hits > maxHits) {
              matches.clear();
              maxHits = hits;
            } 
            matches.add(p);
          } 
        } 
        if (matches.isEmpty())
          matches = paginae; 
        if (target == null) {
          if (!matches.isEmpty())
            paginae = matches; 
          maxHits = 0;
          int minLength = 0;
          char[] charArray = String.join("", (CharSequence[])split).toCharArray();
          for (Glob.Pagina p : paginae) {
            int hits = 0;
            for (char d : charArray) {
              if ((p.res()).name.toLowerCase().contains("/craft/") && (p.res()).name.toLowerCase().contains("" + d))
                hits++; 
            } 
            if (hits >= maxHits) {
              int length = (p.res()).name.toLowerCase().length();
              if (hits == maxHits) {
                if (minLength == 0 || minLength >= length) {
                  minLength = length;
                  maxHits = hits;
                  target = p;
                } 
                continue;
              } 
              minLength = length;
              maxHits = hits;
              target = p;
            } 
          } 
        } 
        resultMap.put(tString, (target.res()).name.toLowerCase());
      } catch (Exception exception) {}
    } 
    int counter = 0;
    Map<String, String> map1 = new HashMap<>();
    Map<String, String> map2 = new HashMap<>();
    Map<String, String> map3 = new HashMap<>();
    for (Map.Entry<String, String> e : resultMap.entrySet()) {
      if (counter < 150) {
        map1.put(e.getKey(), e.getValue());
      } else if (counter < 300) {
        map2.put(e.getKey(), e.getValue());
      } else {
        map3.put(e.getKey(), e.getValue());
      } 
      counter++;
    } 
    System.out.println();
  }
  
  public void cravail(Indir<Resource> res) {
    this.cravail = res;
    if (this.crimg != null) {
      this.crimg.reqdestroy();
      this.crimg = null;
    } 
  }
  
  public void show() {
    super.show();
    if (this.gbtn != null)
      this.gbtn.show(); 
  }
  
  public void hide() {
    super.hide();
    if (this.gbtn != null)
      this.gbtn.hide(); 
  }
  
  public static WritableRaster rmeter(Raster tex, int val, int max) {
    int w = 1 + Utils.clip(val, 0, max) * (tex.getWidth() - 1) / Math.max(max, 1);
    WritableRaster bar = PUtils.copy(tex);
    PUtils.gayblit(bar, 3, new Coord(w - rcap.getWidth(), 0), rcap.getRaster(), 0, Coord.z);
    for (int y = 0; y < bar.getHeight(); y++) {
      for (int x = w; x < bar.getWidth(); x++)
        bar.setSample(x, y, 3, 0); 
    } 
    return bar;
  }
  
  public static WritableRaster lmeter(Raster tex, int val, int max) {
    int w = 1 + Utils.clip(val, 0, max) * (tex.getWidth() - 1) / Math.max(max, 1);
    WritableRaster bar = PUtils.copy(tex);
    PUtils.gayblit(bar, 3, new Coord(bar.getWidth() - w, 0), lcap.getRaster(), 0, Coord.z);
    for (int y = 0; y < bar.getHeight(); y++) {
      for (int x = 0; x < bar.getWidth() - w; x++)
        bar.setSample(x, y, 3, 0); 
    } 
    return bar;
  }
  
  private WritableRaster rfmeter(FoodInfo food, int t) {
    return PUtils.alphablit(rmeter(fbars[t].getRaster(), this.soft[t] + food.tempers[t], this.lmax[t]), rmeter(sbars[t].getRaster(), this.soft[t], this.lmax[t]), Coord.z);
  }
  
  private WritableRaster lfmeter(FoodInfo food, int t) {
    return PUtils.alphablit(lmeter(fbars[t].getRaster(), this.soft[t] + food.tempers[t], this.lmax[t]), lmeter(sbars[t].getRaster(), this.soft[t], this.lmax[t]), Coord.z);
  }
  
  public void draw(BufferedImage buf) {
    WritableRaster dst = buf.getRaster();
    PUtils.blit(dst, bg[this.insanity].getRaster(), Coord.z);
    if (this.lfood != null) {
      PUtils.alphablit(dst, rfmeter(this.lfood, 0), mc[0]);
      PUtils.alphablit(dst, lfmeter(this.lfood, 1), mc[1].sub(bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, lfmeter(this.lfood, 2), mc[2].sub(bars[2].getWidth() - 1, 0));
      PUtils.alphablit(dst, rfmeter(this.lfood, 3), mc[3]);
    } else {
      if (this.soft[0] > this.hard[0])
        PUtils.alphablit(dst, rmeter(sbars[0].getRaster(), this.soft[0], this.lmax[0]), mc[0]); 
      if (this.soft[1] > this.hard[1])
        PUtils.alphablit(dst, lmeter(sbars[1].getRaster(), this.soft[1], this.lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0)); 
      if (this.soft[2] > this.hard[2])
        PUtils.alphablit(dst, lmeter(sbars[2].getRaster(), this.soft[2], this.lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0)); 
      if (this.soft[3] > this.hard[3])
        PUtils.alphablit(dst, rmeter(sbars[3].getRaster(), this.soft[3], this.lmax[3]), mc[3]); 
    } 
    PUtils.alphablit(dst, rmeter(bars[0].getRaster(), this.hard[0], this.lmax[0]), mc[0]);
    PUtils.alphablit(dst, lmeter(bars[1].getRaster(), this.hard[1], this.lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0));
    PUtils.alphablit(dst, lmeter(bars[2].getRaster(), this.hard[2], this.lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
    PUtils.alphablit(dst, rmeter(bars[3].getRaster(), this.hard[3], this.lmax[3]), mc[3]);
  }
  
  public void updinsanity(int n) {
    if (this.insanity != n) {
      String direction = (this.insanity > n) ? "decreased" : "increased";
      GameUI.MsgType type = (this.insanity > n) ? GameUI.MsgType.GOOD : GameUI.MsgType.BAD;
      this.ui.gui.message(String.format("Your madness %s to level %d!", new Object[] { direction, Integer.valueOf(n) }), type);
    } 
    this.insanity = n;
    redraw();
    this.tt = null;
  }
  
  public void draw(GOut g) {
    super.draw(g);
    if (Config.show_tempers) {
      if (this.texts == null) {
        this.texts = (Tex[])new TexI[4];
        for (int i = 0; i < 4; i++) {
          String str = String.format("%s / %s / %s", new Object[] { Utils.fpformat(this.hard[i], 3, 1), Utils.fpformat(this.soft[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1) });
          this.texts[i] = text(str);
        } 
      } 
      g.aimage(this.texts[0], mc[0].add(bars[0].getWidth() / 2, bars[0].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[1], mc[1].add(-bars[1].getWidth() / 2, bars[1].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[2], mc[2].add(-bars[2].getWidth() / 2, bars[2].getHeight() / 2 - 1), 0.5D, 0.5D);
      g.aimage(this.texts[3], mc[3].add(bars[3].getWidth() / 2, bars[3].getHeight() / 2 - 1), 0.5D, 0.5D);
    } 
  }
  
  public void upds(int[] n) {
    this.texts = null;
    this.soft = n;
    redraw();
    this.tt = null;
  }
  
  public void updh(int[] n) {
    this.texts = null;
    this.hard = n;
    redraw();
    this.tt = null;
  }
  
  public boolean mousedown(Coord c, int button) {
    if (bg[this.insanity].getRaster().getSample(c.x, c.y, 3) > 128)
      return true; 
    return super.mousedown(c, button);
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (c.isect(boxc, boxsz)) {
      if (this.tt == null) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 4; i++) {
          buf.append(String.format("%s: %s/%s/%s\n", new Object[] { rnm[i], Utils.fpformat(this.hard[i], 3, 1), Utils.fpformat(this.soft[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1) }));
        } 
        buf.append(String.format("Madness level: %d", new Object[] { Integer.valueOf(this.insanity) }));
        this.tt = RichText.render(buf.toString(), 0, new Object[0]).tex();
      } 
      return this.tt;
    } 
    return null;
  }
  
  public static TexI text(String str) {
    return new TexI(Utils.outline2((tmprfnd.render(str)).img, new Color(240, 240, 240), false));
  }
  
  public void getAndOpenRecipeByName(Indir<Resource> crres, String text, boolean isCalledForCraving, boolean alsoUseMaybeResult) {
    Set<Glob.Pagina> paginae = null;
    Glob.Pagina target = null;
    boolean foundSolidResult = false;
    text = text.toLowerCase();
    try {
      paginae = this.ui.sess.glob.paginae;
      if (!knownRecipes.containsKey(text)) {
        String textShorter = text.substring(0, text.length() - 1);
        if (knownRecipes.containsKey(textShorter))
          text = textShorter; 
      } 
      if (knownRecipes.containsKey(text)) {
        String recipe = knownRecipes.get(text);
        if (recipe != null && !"NO_RECIPE".equals(recipe)) {
          text = recipe;
          target = Utils.getPagina(recipe);
          if (target != null) {
            if (isCalledForCraving) {
              Utils.msgOut("Recipe found for: " + ((Resource.Tooltip)((Resource)crres.get()).layer((Class)Resource.tooltip)).t);
              Utils.msgLog("Recipe is: " + recipe);
            } 
            this.ui.gui.menu.use(target);
          } else if (isCalledForCraving) {
            Utils.msgOut("Looks like you do not know the recipe for this craving...");
          } else {
            Utils.msgOut("Looks like you do not know the recipe for this...");
          } 
          foundSolidResult = true;
        } 
        if ("NO_RECIPE".equals(recipe)) {
          if (isCalledForCraving) {
            Utils.msgOut("This craving is not made by a recipe");
          } else {
            Utils.msgOut("This is not made by a recipe");
          } 
          foundSolidResult = true;
        } 
      } 
    } catch (Exception exception) {}
    if (!foundSolidResult && alsoUseMaybeResult)
      serchForRecipeInPaginae(text, paginae, target); 
  }
  
  public void serchForRecipeInPaginae(String text, Set<Glob.Pagina> paginae, Glob.Pagina target) {
    try {
      String[] split = text.split(" ");
      Set<Glob.Pagina> matches = new HashSet<>();
      int maxHits = 0;
      for (Glob.Pagina p : paginae) {
        int hits = 0;
        for (String string : split) {
          if (string.endsWith("ed"))
            string = string.substring(0, string.length() - 2); 
          if ((p.res()).name.toLowerCase().contains("/craft/") && 
            (p.res()).name.toLowerCase().contains(string))
            hits++; 
        } 
        if (hits > 0 && 
          hits >= maxHits) {
          if (hits > maxHits) {
            matches.clear();
            maxHits = hits;
          } 
          matches.add(p);
        } 
      } 
      if (matches.isEmpty())
        matches = paginae; 
      if (target == null) {
        if (!matches.isEmpty())
          paginae = matches; 
        maxHits = 0;
        int minLength = 0;
        char[] charArray = String.join("", (CharSequence[])split).toCharArray();
        for (Glob.Pagina p : paginae) {
          int hits = 0;
          for (char d : charArray) {
            if ((p.res()).name.toLowerCase().contains("/craft/") && 
              (p.res()).name.toLowerCase().contains("" + d))
              hits++; 
          } 
          if (hits >= maxHits) {
            int length = (p.res()).name.toLowerCase().length();
            if (hits == maxHits) {
              if (minLength == 0 || minLength >= length) {
                minLength = length;
                maxHits = hits;
                target = p;
              } 
              continue;
            } 
            minLength = length;
            maxHits = hits;
            target = p;
          } 
        } 
      } 
      Utils.msgOut("Found a recipe that may be it...");
      this.ui.gui.menu.use(target);
    } catch (Exception exception) {}
  }
}
