//SetPackage
package haven.plugins;
//SetImports
import haven.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// ----------------------------------------------------------------------
// DECLARE PLUGIN
// ----------------------------------------------------------------------
public class MyFirstPlugin extends Plugin {
    private boolean shown = false;
    private BBWindow window;

    public void load(UI ui) {
        Glob glob = ui.sess.glob;
        Collection<Glob.Pagina> p = glob.paginae;
        p.add(glob.paginafor(Resource.load("paginae/add/hello")));
        XTendedPaginae.registerPlugin("hello", this);
    }

    public void execute(UI ui) {
        if (shown) return;
        shown = true;
        window = new BBWindow(ui.root, this);
    }

    public void onWindowClosed() {
        shown = false;
    }
}

// ----------------------------------------------------------------------
// LISTING + CONTRACT DATA STRUCTS
// ----------------------------------------------------------------------


// ----------------------------------------------------------------------
// MAIN WINDOW
// ----------------------------------------------------------------------
class BBWindow extends Window {
    private final MyFirstPlugin plugin;
    private Tex bgImage = null;

    // Search tab widgets


    // Tabs
    private Button tabSearchButton;
    private Button tabContractsButton;

    // Contracts tab widgets
    private ContractsList contractsList;
    private Button confirmContractButton;
    private Button feedbackButton; 

    // Feedback tab widget container
    private Widget feedbackWidget; 
    private int feedbackSelectedRating = 5;
    private TextEntry feedbackCommentBox;
    private Contract feedbackTargetContract = null;
    //widget coord
    private int resultsX, resultsY, resultsW, resultsHeight;
    private int centerXButton, buttonY, buttonWidth;
    private int tabY;
    //Auth Widgets

    //Create Listing
    

    private final CopyOnWriteArrayList<String> remoteOptions = new CopyOnWriteArrayList<>(); 
    
    //API URLs

    private final String createContractUrl = "http://45.139.50.11:6436/create_contract.php";
    private final String contractsUrl = "http://45.139.50.11:6436/contracts.php";
    private final String itemlistUrl = "http://45.139.50.11:6436/itemlist.php";
    private final String confirmContractUrl = "http://45.139.50.11:6436/confirm_contract.php";
    private final String createUserUrl = "http://45.139.50.11:6436/create_user.php";
    private final String loginUserUrl = "http://45.139.50.11:6436/login.php";
    private final String createFeedbackUrl = "http://45.139.50.11:6436/create_feedback.php"; 
    private final String loadFeedbackUrl = "http://45.139.50.11:6436/load_feedback.php";
        

    //Session Token
    private static final String loginConfig = "plugindata/BB.conf";
    private static final String loginTokenURL = "http://45.139.50.11:6436/validate_token.php";
    private String sessionUsername = null;
    private String sessionToken = null;
    private int sessionPlayerId = 0;

    //Main Class
    public BBWindow(Widget parent, MyFirstPlugin plugin) {
        super(new Coord(100, 100), new Coord(300, 400), parent, "Bent Broom");
        this.justclose = true;
        this.plugin = plugin;

        try {
            BufferedImage img = ImageIO.read(new URL("http://witchwatchers.club/img/bblogo300.png"));
            if (img != null) bgImage = new TexI(img);
        } catch (Exception e) {
            e.printStackTrace();
        }


        


      

        //Autocomplete
        new Thread(() -> {
            List<String> loaded = loadOptionsFromURL(itemlistUrl);
            if (!loaded.isEmpty()) {
                remoteOptions.clear();
                remoteOptions.addAll(loaded);
                ui.message("Loaded " + loaded.size() + " items for autocomplete.", GameUI.MsgType.INFO);
            }
        }).start();
   ///////////////make widgets!!!
        new Thread(this::tryAutoLogin).start();
    }

    @Override
    public void destroy() {
        super.destroy();
        plugin.onWindowClosed();
    }



    // --- Create Listing UI ---
    
    private void startstuff(){
            if (listItemWidget == null) {
                listItemWidget = new ListItemWidget(
                    new Coord(10, 60),
                    new Coord(asz.x - 20, 300),
                    this,               // parent = BBWindow (a Widget)
                    remoteOptions,      // pass the auto-complete list
                    sessionPlayerId     // current player id (0 if not logged)
                );
            };
        // IMPORTANT: build the UI AFTER construction
        listItemWidget.buildUI();

        listingWidget.makeListWidget();
    }







    


    
    

    //Tab Functions

    private void hideAllWidgets(){
        tabSearchButton.visible=false; tabContractsButton.visible=false;
        ResultsList.visible=false; searchBox.visible=false; searchButton.visible=false;
        quantityLabel.visible=false; quantityBox.visible=false; buyButton.visible=false;
        contractsList.visible=false; confirmContractButton.visible=false; feedbackButton.visible=false;
        listButton.visible = false;




    }


    }

    }
    private void showContractsTab() {
        hideAllWidgets();
        tabSearchButton.visible = true;
        tabContractsButton.visible = true;
        contractsList.visible = true;
        fetchContracts();
    }
    private void showFeedbackTab(Contract c) {
        hideAllWidgets();
        feedbackWidget.visible = true;
        // set the contract target if provided; else use currently selected contract in contractsList
        if (c != null) feedbackTargetContract = c;
        else feedbackTargetContract = contractsList.getSelectedContract();

        if (feedbackTargetContract == null) {
            ui.message("No contract selected for feedback.", GameUI.MsgType.INFO);
            // still show the tab but disable submit implicitly
        } else {
            // Prepopulate the comment box with empty and default rating
            feedbackCommentBox.settext("");
            feedbackSelectedRating = 5;
        }
    }



    @Override
    public void cdraw(GOut g) {
        g.chcolor(0, 0, 0, 255);
        g.frect(Coord.z, sz);
        g.chcolor();
        if (bgImage != null)
            g.image(bgImage, new Coord((asz.x - 200) / 2, 40), new Coord(200, 200)); 
    }

    //Network


    








    private void fetchContracts() {
        new Thread(() -> {
            try {
                int playerId = sessionPlayerId;
                URL url = new URL(contractsUrl + "?player_id=" + playerId);
                StringBuilder sb = new StringBuilder();
                try (Scanner sc = new Scanner(url.openStream())) {
                    while (sc.hasNextLine()) sb.append(sc.nextLine());
                }
                List<Contract> list = ContractsList.parseContractsJson(sb.toString(), playerId);
                contractsList.setContracts(list);
            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error fetching contracts: " + e.getMessage(), GameUI.MsgType.ERROR);
                contractsList.setContracts(Collections.emptyList());
            }
        }).start();
    }



    private void createContract(final int listingId, final int buyerId, final int quantity) {
        new Thread(() -> {
            try {
                String postData = "listing_id=" + listingId + 
                                  "&buyer_id=" + buyerId + 
                                  "&quantity=" + quantity;

                URL url = new URL(createContractUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.writeBytes(postData);
                }

                StringBuilder response = new StringBuilder();
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    while (sc.hasNextLine()) response.append(sc.nextLine());
                }
                
                final String jsonResponse = response.toString().trim();
                
                if (jsonResponse.contains("\"error\"")) {
                    Pattern p = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");
                    Matcher m = p.matcher(jsonResponse);
                    String errorMsg = m.find() ? m.group(1) : "Unknown error creating contract.";
                    ui.message("Contract Failed: " + errorMsg, GameUI.MsgType.ERROR);
                } else {
                    Pattern p = Pattern.compile("\"contract_id\"\\s*:\\s*([0-9]+)");
                    Matcher m = p.matcher(jsonResponse);
                    String contractId = m.find() ? m.group(1) : "???";
                    
                    ui.message("✅ Contract #" + contractId + " created successfully!", GameUI.MsgType.INFO);
                    // refresh contracts
                    fetchContracts();
                }

            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error communicating with server: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
        }).start();
    }

    private void handleConfirmContract() {
        Contract sel = contractsList.getSelectedContract();
        if (sel == null) return;
        
        int playerId = sessionPlayerId; // Use the actual player ID
        
        // Before sending, ensure the button is hidden to prevent double-clicks
        confirmContractButton.visible = false;
        
        // Pass the contract ID, player ID, and role to the network method
        confirmContract(sel.id, playerId, sel.role);
    }

private void confirmContract(final int contractId, final int playerId, final String role) {
        new Thread(() -> {
            try {
                String postData = "contract_id=" + contractId + 
                                  "&player_id=" + playerId + 
                                  "&role=" + role;

                URL url = new URL(confirmContractUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postData.length()));

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.writeBytes(postData);
                }

                StringBuilder response = new StringBuilder();
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    while (sc.hasNextLine()) response.append(sc.nextLine());
                }
                
                final String jsonResponse = response.toString().trim();
                
                if (jsonResponse.contains("\"error\"")) {
                    Pattern p = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");
                    Matcher m = p.matcher(jsonResponse);
                    String errorMsg = m.find() ? m.group(1) : "Unknown error confirming contract.";
                    ui.message("Confirmation Failed: " + errorMsg, GameUI.MsgType.ERROR);
                } else {
                    ui.message("✅ Contract #" + contractId + " confirmed successfully!", GameUI.MsgType.INFO);
                    // Refresh the contract list on success
                    fetchContracts();
                }

            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error communicating with server: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
        }).start();
    }


    //Feedback
    private void checkFeedbackLeftAsync(final Contract c) {
        new Thread(() -> {
            try {
                String q = loadFeedbackUrl + "?contract_id=" + URLEncoder.encode(String.valueOf(c.id), "UTF-8")
                        + "&from_user_id=" + URLEncoder.encode(String.valueOf(sessionPlayerId), "UTF-8");
                URL url = new URL(q);
                StringBuilder sb = new StringBuilder();
                try (Scanner sc = new Scanner(url.openStream())) {
                    while (sc.hasNextLine()) sb.append(sc.nextLine());
                }
                String resp = sb.toString().trim();
                boolean exists = false;

                String cleaned = resp.replaceAll("\\s+","");
                if (cleaned.startsWith("[") && !cleaned.equals("[]")) exists = true;

                if (!exists) {
                    Matcher m = Pattern.compile("\"feedbacks\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(resp);
                    if (m.find()) {
                        String inner = m.group(1).trim();
                        if (!inner.isEmpty()) exists = true;
                    }
                }
                c.feedbackLeft = exists;

      
                haven.Defer.later(() -> {
                    contractsList.updateContract(c);
          
                    Contract sel = contractsList.getSelectedContract();
                    if (sel != null && sel.id == c.id) {
                        feedbackButton.visible = !c.feedbackLeft && c.status.equals("completed")
                                && ((c.role.equals("buyer") && c.buyerId == sessionPlayerId) || (c.role.equals("seller") && c.sellerId == sessionPlayerId));
                    }
                    return null;
                });

            } catch (Exception e) {
                System.out.println("[checkFeedbackLeftAsync] error: " + e.getMessage());
            }
        }).start();
    }

    private void submitFeedback(Contract c, int rating, String comment) {
        if (c == null) return;


        int fromUser = sessionPlayerId;
        int toUser = (sessionPlayerId == c.buyerId) ? c.sellerId : c.buyerId;

        // Build POST data
        String postData = "contract_id=" + c.id +
                        "&from_user_id=" + fromUser +
                        "&to_user_id=" + toUser +
                        "&rating=" + rating +
                        "&comment=" + urlEncode(comment) + 
                        "&buyer=" + c.buyerId;

        new Thread(() -> {
            try {
                URL url = new URL(createFeedbackUrl); 
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes("UTF-8"));
                }

                int respCode = conn.getResponseCode();
                if (respCode == 200) {
        
                    InputStream is = conn.getInputStream();
                    is.close();
                  showContractsTab();  
                } else {
                    System.out.println("Feedback submission failed: HTTP " + respCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    // Simple URL encoding helper
    public String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }


    private List<String> loadOptionsFromURL(String urlString) {
        List<String> options = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            try (Scanner scanner = new Scanner(url.openStream())) {
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                String json = sb.toString().trim();
                json = json.replaceAll("^\\s*\\[\\s*", "").replaceAll("\\s*\\]\\s*$", "");
                if (!json.isEmpty()) {
                    for (String s : json.split(",")) {
                        String item = s.trim().replaceAll("^\"|\"$", "");
                        if (!item.isEmpty()) options.add(item);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return options;
    }

    private static List<Listing> parseListingsJson(String json) {
        List<Listing> out = new ArrayList<>();
        if (json == null || json.isEmpty()) return out;
        json = json.trim();
        if (!json.startsWith("[")) return out;

        String inner = json.replaceAll("^\\s*\\[", "").replaceAll("\\]\\s*$", "").trim();
        if (inner.isEmpty()) return out;

        String[] objs = inner.split("\\},\\s*\\{");
        for (int i = 0; i < objs.length; i++) {
            String obj = objs[i];
            if (i == 0) obj = obj.replaceFirst("^\\s*\\{", "");
            if (i == objs.length - 1) obj = obj.replaceFirst("\\}\\s*$", "");
            obj = "{" + obj + "}";
            Listing l = parseListingObject(obj);
            if (l != null) out.add(l);
        }
        return out;
    }

    private static Listing parseListingObject(String obj) {
        try {
            Listing l = new Listing();
            l.id = intFromField(obj, "id");
            l.vendor = stringFromField(obj, "vendor");
            l.price = intFromField(obj, "price");
            l.item = stringFromField(obj, "item");
            
            l.quantity = intFromField(obj, "quantity"); 
            
            return l;
        } catch (Exception e) { return null; }
    }

    private static int intFromField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*([0-9]+)");
        Matcher m = p.matcher(obj);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    private static String stringFromField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(obj);
        if (m.find()) return m.group(1);
        return "";
    }
    
    
}

// ----------------------------------------------------------------------
// RESULTS WIDGET
// ----------------------------------------------------------------------
class ResultsList extends Widget {
    private List<Listing> listings = Collections.emptyList();
    private int selected = -1;
    private SelectionListener listener;
    private final int rowHeight = 20;

    public ResultsList(Coord c, Coord sz, Widget parent) {
        super(c, sz, parent);
    }

    public void setSelectionListener(SelectionListener l) { this.listener = l; }
    public void setSelectedIndex(int idx) { this.selected = idx; }

    @Override
    public void draw(GOut g) {
        g.chcolor(0, 0, 0, 180);
        g.frect(Coord.z, sz);
        g.chcolor();
        
        int y = 5;
        int maxRows = (sz.y - 10) / rowHeight; 
        int rows = Math.min(maxRows, listings.size());

        // Column X positions (adjust for your font width)
        int colQtyX    = 5;
        int colPriceX  = 80;
        int colVendorX = 160;

        // Optional: draw headers
        g.chcolor(255, 255, 0, 200);
        g.text("Qty", new Coord(colQtyX, y));
        g.text("Price", new Coord(colPriceX, y));
        g.text("Vendor", new Coord(colVendorX, y));
        g.chcolor();
        y += rowHeight;

        for (int i = 0; i < rows; i++) {
            Listing L = listings.get(i);
            
            if (i == selected) { 
                g.chcolor(50, 50, 50, 220);
                g.frect(new Coord(0, y - 2), new Coord(sz.x, rowHeight));
                g.chcolor();
            }

            // Draw each field in its own column
            g.text(String.valueOf(L.quantity), new Coord(colQtyX, y));
            g.text(String.valueOf(L.price), new Coord(colPriceX, y));
            g.text(safe(L.vendor), new Coord(colVendorX, y));

            y += rowHeight;
        }
        
        if (listings.isEmpty()) {
            g.text("No listings found.", new Coord(5, 20));
        } else if (listings.size() > maxRows) {
            g.text("... (" + listings.size() + " total)", new Coord(5, y));
        }
    }
    
    private String safe(String s) { return (s==null) ? "" : s; }

    @Override
    public boolean mousedown(Coord c, int button) {
        int idx = (c.y - 5) / rowHeight; 
        int maxRows = (sz.y - 10) / rowHeight;
        
        if (idx < 0 || idx >= Math.min(maxRows, listings.size())) {
            selected = -1;
        } else {
            selected = idx;
        }
        
        if (listener != null) listener.onSelected(selected);
        ui.root.wdgmsg("redraw");
        return true;
    }

    public void setListings(List<Listing> newList) {
        listings = newList;
        selected = -1;
        if (listener != null) listener.onSelected(selected);
    }

    public int getSelectedIndex() { return selected; }

    public Listing getListing(int idx) {
        if (idx >= 0 && idx < listings.size()) return listings.get(idx);
        return null;
    }

    public interface SelectionListener { void onSelected(int idx); }
}

// ----------------------------------------------------------------------
// CONTRACTS List
// ----------------------------------------------------------------------
class ContractsList extends Widget {
    private List<Contract> contracts = Collections.emptyList();
    private int selected = -1;
    private ContractSelectionListener listener;
    private final int rowHeight = 20;

    public ContractsList(Coord c, Coord sz, Widget parent) {
        super(c, sz, parent);
    }

    public void setContractSelectionListener(ContractSelectionListener l) {
        this.listener = l;
    }
    
    public void setSelectedIndex(int idx) { this.selected = idx; }

    @Override
    public void draw(GOut g) {
        g.chcolor(0, 0, 0, 180);
        g.frect(Coord.z, sz);
        g.chcolor();
        
        int y = 5;
        int maxRows = (sz.y - 10) / rowHeight; 
        int rows = Math.min(maxRows, contracts.size());

        // Column positions (tweak as needed for spacing)
        int colItemX = 5;
        int colQtyX = 100;
        int colPriceX = 140;
        int colStatusX = 180;

        // Optional: draw column headers
        g.chcolor(255, 255, 0, 200);
        g.text("Item", new Coord(colItemX, y));
        g.text("Qty", new Coord(colQtyX, y));
        g.text("Price", new Coord(colPriceX, y));
        g.text("Status", new Coord(colStatusX, y));
        g.chcolor();
        y += rowHeight;

        for (int i = 0; i < rows; i++) {
            Contract c = contracts.get(i);
            
            if (i == selected) { 
                g.chcolor(50, 50, 50, 220);
                g.frect(new Coord(0, y - 2), new Coord(sz.x, rowHeight));
                g.chcolor();
            }

            // draw text in columns
            g.text(c.item, new Coord(colItemX, y));
            g.text(String.valueOf(c.quantity), new Coord(colQtyX, y));
            g.text(String.valueOf(c.price), new Coord(colPriceX, y));

            String statusText = c.status;
            if (c.status.equals("completed")) {
                statusText += c.feedbackLeft ? " [✔]" : " [!]";
            }
            g.text(statusText, new Coord(colStatusX, y));

            y += rowHeight;
        }

        if (contracts.isEmpty())
            g.text("No active contracts.", new Coord(5, 15));
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
        int idx = (c.y - 5) / rowHeight; 
        int maxRows = (sz.y - 10) / rowHeight;
        
        if (idx < 0 || idx >= Math.min(maxRows, contracts.size())) {
            selected = -1;
        } else {
            selected = idx;
        }
        
        Contract selectedContract = (selected != -1) ? contracts.get(selected) : null;

        if (listener != null) listener.onSelected(selectedContract);
        ui.root.wdgmsg("redraw");
        return true;
    }

    public void setContracts(List<Contract> newList) {
        contracts = newList;
        selected = -1; 
        if (listener != null) listener.onSelected(null);
    }

    public void updateContract(Contract c) {
        if (c == null) return;
        for (int i = 0; i < contracts.size(); i++) {
            if (contracts.get(i).id == c.id) {
                contracts.set(i, c);
                ui.root.wdgmsg("redraw");
                return;
            }
        }
    }

    public Contract getSelectedContract() {
        if (selected >= 0 && selected < contracts.size()) {
            return contracts.get(selected);
        }
        return null;
    }

    public interface ContractSelectionListener {
        void onSelected(Contract c);
    }


    public static List<Contract> parseContractsJson(String json, int playerId) {
        List<Contract> out = new ArrayList<>();
        Matcher m = Pattern.compile("\\{[^}]*\\}").matcher(json);

        while (m.find()) {
            String o = m.group();
            System.out.println("Parsing object: " + o); // debug

            Contract c = new Contract();
            c.id = getInt(o, "id");
            c.buyerId = getInt(o, "buyer_id");
            c.sellerId = getInt(o, "vendor_id");
            c.item = getString(o, "item_name");
            c.quantity = getInt(o, "quantity");
            c.price = getInt(o, "price");
            c.status = getString(o, "status");

            // Determine role for the current player
            c.role = (c.buyerId == playerId) ? "buyer" : "seller";

            // Set playerConfirmed based on role
            String fieldName = c.role.equals("buyer") ? "buyer_confirmed" : "vendor_confirmed";
            c.playerConfirmed = getInt(o, fieldName);

            // Set feedbackLeft
            c.feedbackLeft = getInt(o, "feedback_left") == 1;

            System.out.println("Parsed Contract -> id: " + c.id + ", item: " + c.item + ", quantity: " + c.quantity + ", price: " + c.price + ", role: " + c.role + ", confirmed: " + c.playerConfirmed + ", feedbackLeft: " + c.feedbackLeft);
            out.add(c);
        }

        return out;
    }



    private static int getInt(String o, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?(\\d+)\"?");
        Matcher m = p.matcher(o);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse int for " + key + " in: " + o);
            }
        }
        return 0;
    }

    private static String getString(String o, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(o);
        return m.find() ? m.group(1) : "";
    }
}
////////////////////////////////////////////////
/// Listing Widget
///////////////////////////////////////////////
