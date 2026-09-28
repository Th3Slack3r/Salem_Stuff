package haven.plugins;

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
class Listing {
    public int id;
    public String vendor = "";
    public String item = "";
    public int quantity;
    public int price;
}

class Contract {
    public int id;
    public int buyerId;
    public int sellerId;
    public String item;
    public int quantity;
    public int price;
    public String status;        
    public String role;           
    public int playerConfirmed;   
    public boolean feedbackLeft;
}

// ----------------------------------------------------------------------
// MAIN WINDOW
// ----------------------------------------------------------------------
class BBWindow extends Window {
    private final MyFirstPlugin plugin;
    private Tex bgImage = null;

    // Search tab widgets
    public static AutoFillEntry searchBox;
    public static Button searchButton;
    //public static ResultsWidget resultsWidget;
    public static Label quantityLabel;
    public static TextEntry quantityBox;
    public static Button buyButton;
    public static Button listButton;

    // Tabs
    public static Button tabSearchButton;
    public static Button tabContractsButton;

    // Contracts tab widgets
    public static ContractsWidget contractsWidget;
    public static Button confirmContractButton;
    public static Button feedbackButton; 

    // Feedback tab widget container
    public static Widget feedbackWidget; 
    public static int feedbackSelectedRating = 5;
    public static TextEntry feedbackCommentBox;
    public static Contract feedbackTargetContract = null;
    //widget coord
    public static int resultsX, resultsY, resultsW, resultsHeight;
    public static int centerXButton, buttonY, buttonWidth;
    public static int tabY;
    //Auth Widgets
    public static Widget authWidget; 
    public static Button authChoiceLoginButton;
    public static Button authChoiceCreateButton;
    public static Label loginUsernameLabel;
    public static TextEntry loginUsernameBox;
    public static Label loginPasswordLabel;
    public static TextEntry loginPasswordBox;
    public static Button loginSubmitButton;
    public static Label createUsernameLabel;
    public static TextEntry createUsernameBox;
    public static Label createPasswordLabel;
    public static TextEntry createPasswordBox;
    public static Label createEmailLabel;
    public static TextEntry createEmailBox;
    public static Label createHSLabel;
    public static TextEntry createHSBox;
    public static Button createSubmitButton;
    //Create Listing
    public static ListingsWidget listingsWidget;


    public static final CopyOnWriteArrayList<String> remoteOptions = new CopyOnWriteArrayList<>(); 
    
    //API URLs
    private final String listingsBaseUrl = "http://45.139.50.11:6436/listings.php";
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
    public static String sessionUsername = null;
    public static String sessionToken = null;
    public static int sessionPlayerId = 0;

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
        makeTabHeader();
        makeFeedbackWidget();
        makeSearchWidget();
        makeContractsWidget();
        startListWidget();
        makeAuthWidget();
        showAuthChoice();
        new Thread(this::tryAutoLogin).start();
    }

    @Override
    public void destroy() {
        super.destroy();
        plugin.onWindowClosed();
    }



    // --- Create Listing UI ---
    private void startListWidget() {
        this.listingsWidget = new ListingsWidget(new Coord(0, 0), this.sz, this);
        listingsWidget.makeListingsWidget(asz.x,asz.y);
        listingsWidget.visible = false;
    }




    private void makeTabHeader(){   
                // Tab Header
        int tabWidth = 100, tabSpacing = 10, tabCount = 2;
        int totalTabsW = tabCount * tabWidth + (tabCount - 1) * tabSpacing;
        int tabStartX = (asz.x - totalTabsW) / 2;
        int tabY = 10;

        tabSearchButton = new Button(new Coord(tabStartX, tabY), tabWidth, this, "Search") {
            @Override
            public void click() {
                showSearchTab();
            }
        };
        tabContractsButton = new Button(new Coord(tabStartX + tabWidth + tabSpacing, tabY), tabWidth, this, "Contracts") {
            @Override
            public void click() {
                showContractsTab();
            }
        };

    }
    private void makeContractsWidget() {
    int tabY = 10;

    // --- Contracts Widget ---
    int resultsX = 10;
    int resultsW = asz.x - 20;
    int resultsY = tabY + 50;
    int resultsHeight = 200;

    contractsWidget = new ContractsWidget(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), this);
    contractsWidget.visible = false;

    // --- Buttons ---
    int buttonWidth = 120;
    int buttonHeight = 25;
    int buttonY = resultsY + resultsHeight + 10;
    int centerXButton = (asz.x - buttonWidth) / 2;

    confirmContractButton = new Button(new Coord(centerXButton, buttonY), buttonWidth, this, "Confirm Contract") {
        @Override
        public void click() {
            handleConfirmContract();
        }
    };
    confirmContractButton.visible = false;

    feedbackButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, this, "Leave Feedback") {
        @Override
        public void click() {
            Contract sel = contractsWidget.getSelectedContract();
            if (sel != null) showFeedbackTab(sel);
        }
    };
    feedbackButton.visible = false;

    // --- Contracts Selection Listener ---
    contractsWidget.setContractSelectionListener(new ContractsWidget.ContractSelectionListener() {
        public void onSelected(Contract c) {
            if (c == null) {
                confirmContractButton.visible = false;
                feedbackButton.visible = false;
                return;
            }

            int playerId = sessionPlayerId;
            boolean needsConfirmation = false;
            boolean needsFeedback = false;

            // Active contract confirmation
            if (((c.role.equals("buyer") && c.buyerId == playerId) ||
                 (c.role.equals("seller") && c.sellerId == playerId)) &&
                 c.playerConfirmed == 0 &&
                 c.status.equals("active")) {
                needsConfirmation = true;
            }

            // Feedback for completed contract
            if (((c.role.equals("buyer") && c.buyerId == playerId) ||
                 (c.role.equals("seller") && c.sellerId == playerId)) &&
                 !c.feedbackLeft &&
                 c.status.equals("completed")) {
                needsFeedback = true;
            }

            // Update buttons visibility
            confirmContractButton.visible = needsConfirmation;
            feedbackButton.visible = needsFeedback;
        }
    });
}

    
    private void makeAuthWidget() {
        int wX = 10;
        int wY = 50;
        int wW = asz.x - 20;
        int wH = asz.y - 120;

        // Main container for authentication
        authWidget = new Widget(new Coord(wX, wY), new Coord(wW, wH), this) {
            @Override
            public void draw(GOut g) {
                g.chcolor(0, 0, 0, 150);
                g.frect(Coord.z, this.sz);
                g.chcolor();
                super.draw(g);
            }
        };
        authWidget.visible = false;

        // Add the inner list widget
        authWidget = new Widget(new Coord(0, 0), authWidget.sz, authWidget);

        int authW = 150, authH = 25, authSpacing = 35;
        int authYStart = asz.y / 2 + 10;
        int authX = (asz.x - authW) / 2;

        // Auth selection buttons
        authChoiceLoginButton = new Button(new Coord(authX, authYStart), authW, authWidget, "Login") {
            @Override
            public void click() { showAuthLogin(); }
        };
        authChoiceCreateButton = new Button(new Coord(authX, authYStart + authSpacing), authW, authWidget, "Create Account") {
            @Override
            public void click() { showAuthCreate(); }
        };

        // Login section
        loginUsernameLabel = new Label(new Coord(authX - 60, authYStart + 2), authWidget, "Username:");
        loginUsernameBox = new TextEntry(new Coord(authX, authYStart), authW, authWidget, "");
        loginPasswordLabel = new Label(new Coord(authX - 60, authYStart + authSpacing + 2), authWidget, "Password:");
        loginPasswordBox = new TextEntry(new Coord(authX, authYStart + authSpacing), authW, authWidget, "");
        loginSubmitButton = new Button(new Coord(authX, authYStart + authSpacing * 2), authW, authWidget, "Login") {
            @Override
            public void click() { handleLogin(); }
        };

        // Create account section
        createUsernameLabel = new Label(new Coord(authX - 60, authYStart + 2), authWidget, "Username:");
        createUsernameBox = new TextEntry(new Coord(authX, authYStart), authW, authWidget, "");
        createPasswordLabel = new Label(new Coord(authX - 60, authYStart + authSpacing + 2), authWidget, "Password:");
        createPasswordBox = new TextEntry(new Coord(authX, authYStart + authSpacing), authW, authWidget, "");
        createEmailLabel = new Label(new Coord(authX - 60, authYStart + authSpacing * 2 + 2), authWidget, "Email:");
        createEmailBox = new TextEntry(new Coord(authX, authYStart + authSpacing * 2), authW, authWidget, "");
        createHSLabel = new Label(new Coord(authX - 60, authYStart + authSpacing * 3 + 2), authWidget, "HS:");
        createHSBox = new TextEntry(new Coord(authX, authYStart + authSpacing * 3), authW, authWidget, "");
        createSubmitButton = new Button(new Coord(authX, authYStart + authSpacing * 4), authW, authWidget, "Create") {
            @Override
            public void click() { handleCreateAccount(); }
        };
    }

    private void makeSearchWidget() {
        int tabY = 10;
        // --- Results Widget ---
        int resultsX = 10;
        int resultsW = asz.x - 20;
        int resultsY = tabY + 50;
        int resultsHeight = 200;

        resultsWidget = new ResultsWidget(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), this);
        resultsWidget.visible = true;

        // --- Search Box ---
        int boxWidth = 200;
        int boxHeight = 20;
        int boxY = resultsY + resultsHeight + 20;
        int centerXBox = (asz.x - boxWidth) / 2;

        searchBox = new AutoFillEntry(new Coord(centerXBox, boxY), new Coord(boxWidth, boxHeight), this, remoteOptions);

        // --- Search Button ---
        int buttonWidth = 120;
        int buttonHeight = 25;
        int buttonY = boxY + boxHeight + 8;
        int centerXButton = (asz.x - buttonWidth) / 2;

        searchButton = new Button(new Coord(centerXButton, buttonY), buttonWidth, this, "Search") {
            @Override
            public void click() {
                final String query = searchBox.text.trim();
                if (query.isEmpty()) {
                    ui.message("Enter something to search for.", GameUI.MsgType.INFO);
                    return;
                }
                ui.message("Searching for: " + query, GameUI.MsgType.INFO);
                fetchListings(query);
            }
        };
        // --- List Item --- //
        listButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, this, "My Listings") {
            @Override
            public void click() {
                listingsWidget.showListItemTab();
            }
        };
        // --- Buy Quantity Widgets ---
        quantityLabel = new Label(new Coord(centerXBox, boxY - 25), this, "Quantity:");
        quantityBox = new TextEntry(new Coord(centerXBox + 60, boxY - 25), 60, this, "");
        buyButton = new Button(new Coord(centerXBox + 130, boxY - 27), 60, this, "Buy") {
            @Override
            public void click() {
                handleBuy();
            }
        };

        quantityLabel.visible = false;
        quantityBox.visible = false;
        buyButton.visible = false;

        // --- Results Selection Listener ---
        resultsWidget.setSelectionListener(new ResultsWidget.SelectionListener() {
            public void onSelected(int idx) {
                if (resultsWidget.visible) {
                    quantityLabel.visible = true;
                    quantityBox.visible = true;
                    buyButton.visible = true;
                    Listing l = resultsWidget.getListing(idx);
                    if (l != null)
                        quantityBox.settext(String.valueOf(l.quantity));
                }
            }
        });
    }

    private void makeFeedbackWidget() {
        // Build feedbackWidget but keep it hidden by default
        int wX = 10;
        int wY = 50;
        int wW = asz.x - 20;
        int wH = asz.y - 120;
        feedbackWidget = new Widget(new Coord(wX, wY), new Coord(wW, wH), this) {
            @Override
            public void draw(GOut g) {
                // background for the tab area
                g.chcolor(0,0,0,150);
                g.frect(Coord.z, this.sz);
                g.chcolor();
                super.draw(g);
            }
        };
        feedbackWidget.visible = false;

        // Title
        new Label(new Coord(10, 8), feedbackWidget, "Leave feedback for the selected contract:");

        // Rating buttons 1-5 (act like radio buttons)
        int rx = 10, ry = 30;
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            final Button rb = new Button(new Coord(rx + (i-1)*42, ry), 36, feedbackWidget, String.valueOf(i)) {
                @Override public void click() {
                    feedbackSelectedRating = r;
                    // visual feedback: we will redraw to show selection (we'll render nothing fancy here, but the selected value is used)
                }
            };
        }

        // Comment label + box
        new Label(new Coord(10, ry + 50), feedbackWidget, "Comments:");
        feedbackCommentBox = new TextEntry(new Coord(10, ry + 70), feedbackWidget.sz.x - 20, feedbackWidget, "");
        feedbackCommentBox.settext("");
        // Submit button
        Button submit = new Button(new Coord((feedbackWidget.sz.x - 120)/2, feedbackWidget.sz.y - 50), 120, feedbackWidget, "Submit Feedback") {
            @Override public void click() {
                if (feedbackTargetContract == null) {
                    ui.message("No contract selected.", GameUI.MsgType.ERROR);
                    return;
                }
                final String comment = feedbackCommentBox.text.trim();
                final int rating = feedbackSelectedRating;
                if (rating < 1 || rating > 5) {
                    ui.message("Select a rating 1-5.", GameUI.MsgType.INFO);
                    return;
                }
                // submit and return to contracts tab
                submitFeedback(feedbackTargetContract, rating, comment);
                // clear comment box for next use
                feedbackCommentBox.settext("");
                feedbackSelectedRating = 5;
                // show contracts tab
                showContractsTab();
            }
        };
    }

    //Tab Functions
    private void showAuthChoice(){
        hideAllWidgets();
        authChoiceLoginButton.visible=true; authChoiceCreateButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    public static void hideAllWidgets(){
        tabSearchButton.visible=false; tabContractsButton.visible=false;
        resultsWidget.visible=false; searchBox.visible=false; searchButton.visible=false;
        quantityLabel.visible=false; quantityBox.visible=false; buyButton.visible=false;
        contractsWidget.visible=false; confirmContractButton.visible=false; feedbackButton.visible=false;
        listButton.visible = false;

        authChoiceLoginButton.visible=false; authChoiceCreateButton.visible=false;
        loginUsernameLabel.visible=false; loginUsernameBox.visible=false;
        loginPasswordLabel.visible=false; loginPasswordBox.visible=false;
        loginSubmitButton.visible=false;

        createUsernameLabel.visible=false; createUsernameBox.visible=false;
        createPasswordLabel.visible=false; createPasswordBox.visible=false;
        createEmailLabel.visible=false; createEmailBox.visible=false;
        createHSLabel.visible=false; createHSBox.visible=false;
        createSubmitButton.visible=false;

        feedbackWidget.visible=false;
        resultsWidget.visible = false;
        contractsWidget.visible =false;
        feedbackWidget.visible=false;
        listingsWidget.visible =false;
        authWidget.visible=false;
        resultsWidget.visible = false;

    }

    private void showAuthLogin(){
        hideAllWidgets();
        loginUsernameLabel.visible=true; loginUsernameBox.visible=true;
        loginPasswordLabel.visible=true; loginPasswordBox.visible=true;
        loginSubmitButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    private void showAuthCreate(){
        hideAllWidgets();
        createUsernameLabel.visible=true; createUsernameBox.visible=true;
        createPasswordLabel.visible=true; createPasswordBox.visible=true;
        createEmailLabel.visible=true; createEmailBox.visible=true;
        createHSLabel.visible=true; createHSBox.visible=true;
        ui.root.wdgmsg("redraw");
    }
    private void showSearchTab() {
        hideAllWidgets();
        tabSearchButton.visible = true;
        tabContractsButton.visible = true;
        resultsWidget.visible = true;
        searchBox.visible = true;
        searchButton.visible = true;   
        resultsWidget.visible = true;   
        resultsWidget.setSelectedIndex(-1); 
        //searchWidget.visible =true;
        listButton.visible = true;
    }
    private void showContractsTab() {
        hideAllWidgets();
        tabSearchButton.visible = true;
        tabContractsButton.visible = true;
        contractsWidget.visible = true;
        fetchContracts();
    }
    private void showFeedbackTab(Contract c) {
        hideAllWidgets();
        feedbackWidget.visible = true;
        // set the contract target if provided; else use currently selected contract in contractsWidget
        if (c != null) feedbackTargetContract = c;
        else feedbackTargetContract = contractsWidget.getSelectedContract();

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


    private void handleCreateAccount(){
        final String username = createUsernameBox.text.trim();
        final String password = createPasswordBox.text.trim();
        final String email = createEmailBox.text.trim();
        final String hs = createHSBox.text.trim();

        if(username.isEmpty()||password.isEmpty()||email.isEmpty()||hs.isEmpty()){
            ui.message("All fields are required.", GameUI.MsgType.ERROR);
            return;
        }

        createSubmitButton.change("Creating...");
        new Thread(()->{
            try{
                String derivedKey = sha256(username+password+hs);
                String postData = "username="+URLEncoder.encode(username,"UTF-8")+
                                  "&password="+URLEncoder.encode(password,"UTF-8")+
                                  "&email="+URLEncoder.encode(email,"UTF-8")+
                                  "&hs="+URLEncoder.encode(hs,"UTF-8")+
                                  "&key="+URLEncoder.encode(derivedKey,"UTF-8");

                URL url = new URL(createUserUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length",String.valueOf(postData.getBytes(StandardCharsets.UTF_8).length));

                try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())){
                    wr.write(postData.getBytes(StandardCharsets.UTF_8));
                }

                StringBuilder response = new StringBuilder();
                try(Scanner sc = new Scanner(conn.getInputStream())){
                    while(sc.hasNextLine()) response.append(sc.nextLine());
                }

                String jsonResponse = response.toString().trim();
                if(jsonResponse.contains("\"error\"")){
                    Pattern p = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");
                    Matcher m = p.matcher(jsonResponse);
                    String msg = m.find()?m.group(1):"Unknown error";
                    ui.message("Account creation failed: "+msg, GameUI.MsgType.ERROR);
                } else {
                    File folder = new File("plugindata");
                    if(!folder.exists()) folder.mkdir();
                    File keyFile = new File(folder,"userkey.json");
                    try(FileWriter fw = new FileWriter(keyFile)){
                        fw.write("{\"key\":\""+derivedKey+"\"}");
                    }
                    ui.message("✅ Account created successfully!", GameUI.MsgType.INFO);
                    loginUsernameBox.settext(username);
                    loginPasswordBox.settext(password);
                    handleLogin();
                }

            }catch(Exception e){ e.printStackTrace(); ui.message("Error communicating with server: "+e.getMessage(), GameUI.MsgType.ERROR); }
            finally{ createSubmitButton.change("Create"); }
        }).start();
    }

    private void handleLogin() {
        final String username = (loginUsernameBox != null && loginUsernameBox.text != null)
                ? loginUsernameBox.text.trim()
                : "";
        final String password = (loginPasswordBox != null && loginPasswordBox.text != null)
                ? loginPasswordBox.text.trim()
                : "";

        if (username.isEmpty() || password.isEmpty()) {
            ui.message("Username and password are required.", GameUI.MsgType.ERROR);
            return;
        }

        new Thread(() -> {
            try {
                String postData = "username=" + URLEncoder.encode(username, "UTF-8") +
                                "&password=" + URLEncoder.encode(password, "UTF-8");

                URL url = new URL(loginUserUrl); 
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(postData.getBytes(StandardCharsets.UTF_8));
                }

                StringBuilder response = new StringBuilder();
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    while (sc.hasNextLine()) response.append(sc.nextLine());
                }

                String jsonResponse = response.toString().trim();

                if (jsonResponse.contains("\"error\"")) {
                    Pattern p = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");
                    Matcher m = p.matcher(jsonResponse);
                    String msg = m.find() ? m.group(1) : "Unknown error";
                    ui.message("Login failed: " + msg, GameUI.MsgType.ERROR);
                    return;
                }

                // Extract token and player_id from response if available
                Pattern pToken = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
                Matcher mToken = pToken.matcher(jsonResponse);
                String token = mToken.find() ? mToken.group(1) : "";

                Pattern pId = Pattern.compile("\"player_id\"\\s*:\\s*([0-9]+)");
                Matcher mId = pId.matcher(jsonResponse);
                int playerId = mId.find() ? Integer.parseInt(mId.group(1)) : 0;

                if (token.isEmpty()) {
                    ui.message("Error: Missing token in server response.", GameUI.MsgType.ERROR);
                    return;
                }

                ui.message("✅ Logged in as " + username, GameUI.MsgType.INFO);
                showSearchTab();

                // Save BB.conf
                File folder = new File("plugindata");
                if (!folder.exists()) folder.mkdir();

                File confFile = new File(folder, "BB.conf");
                String jsonContent = "{\n" +
                                    "  \"username\": \"" + username + "\",\n" +
                                    "  \"token\": \"" + token + "\",\n" +
                                    "  \"player_id\": " + playerId + "\n" +
                                    "}";
                try (FileWriter writer = new FileWriter(confFile)) {
                    writer.write(jsonContent);
                }

                // Store session info
                sessionUsername = username;
                sessionToken = token;
                sessionPlayerId = playerId;
                // ✅ Log confirmation for debug


                // ✅ Fetch contracts now that sessionPlayerId is valid
                fetchContracts();

            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error communicating with server: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
        }).start();
    }

    private void tryAutoLogin() {
        try {
            File confFile = new File(loginConfig);
            if (!confFile.exists()) {
                System.out.println("[AutoLogin] No BB.conf found — showing login screen.");
                return;
            }

            // Read BB.conf
            String content;
            try (Scanner sc = new Scanner(confFile)) {
                StringBuilder sb = new StringBuilder();
                while (sc.hasNextLine()) sb.append(sc.nextLine());
                content = sb.toString();
            }

            // Extract username, token and player_id
            Pattern pUser = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]+)\"");
            Pattern pToken = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
            Pattern pPlayer = Pattern.compile("\"player_id\"\\s*:\\s*([0-9]+)");

            Matcher mUser = pUser.matcher(content);
            Matcher mToken = pToken.matcher(content);
            Matcher mPlayer = pPlayer.matcher(content);

            if (!mUser.find() || !mToken.find() || !mPlayer.find()) {
                System.out.println("[AutoLogin] Missing username or token or player_id in BB.conf.");
                return;
            }

            final String username = mUser.group(1);
            final String token = mToken.group(1);
            final int playerId = Integer.parseInt(mPlayer.group(1));

            // Send token to PHP for validation
            String postData = "token=" + URLEncoder.encode(token, "UTF-8");
            URL url = new URL(loginTokenURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (Scanner sc = new Scanner(conn.getInputStream())) {
                while (sc.hasNextLine()) response.append(sc.nextLine());
            }

            String jsonResponse = response.toString().trim();
            System.out.println("[AutoLogin] Response: " + jsonResponse);

            if (jsonResponse.contains("\"error\"")) {
                ui.message("Session expired. Please log in again.", GameUI.MsgType.INFO);
                return;
            }

            // Success: store session info
            this.sessionUsername = username;
            this.sessionToken = token;
            this.sessionPlayerId = playerId;

            // ✅ Fetch contracts now that sessionPlayerId is valid
            //fetchContracts();

            ui.message("✅ Auto-logged in as " + username, GameUI.MsgType.INFO);

            // Switch to your main tab on UI thread
            haven.Defer.later(() -> {
                showSearchTab();
                return null;
            });



        } catch (Exception e) {
            e.printStackTrace();
            ui.message("Auto-login failed: " + e.getMessage(), GameUI.MsgType.ERROR);
        }
    }



    private void fetchListings(final String itemName) {
        new Thread(() -> {
            try {
                String q = URLEncoder.encode(itemName, "UTF-8");
                URL url = new URL(listingsBaseUrl + "?item=" + q);
                StringBuilder sb = new StringBuilder();
                try (Scanner sc = new Scanner(url.openStream())) {
                    while (sc.hasNextLine()) sb.append(sc.nextLine());
                }
                
                List<Listing> list = parseListingsJson(sb.toString()); 
                resultsWidget.setListings(list);
            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error fetching listings: " + e.getMessage(), GameUI.MsgType.ERROR);
                resultsWidget.setListings(Collections.emptyList());
            }
        }).start();
    }
    private void fetchContracts() {
        new Thread(() -> {
            try {
                int playerId = sessionPlayerId;
                URL url = new URL(contractsUrl + "?player_id=" + playerId);
                StringBuilder sb = new StringBuilder();
                try (Scanner sc = new Scanner(url.openStream())) {
                    while (sc.hasNextLine()) sb.append(sc.nextLine());
                }
                List<Contract> list = ContractsWidget.parseContractsJson(sb.toString(), playerId);
                contractsWidget.setContracts(list);
            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error fetching contracts: " + e.getMessage(), GameUI.MsgType.ERROR);
                contractsWidget.setContracts(Collections.emptyList());
            }
        }).start();
    }

    private void handleBuy() {
        int sel = resultsWidget.getSelectedIndex();
        if (sel < 0) {
            ui.message("Select a listing first.", GameUI.MsgType.INFO);
            return;
        }
        Listing chosen = resultsWidget.getListing(sel);
        if (chosen == null) return;

        String qtyStr = quantityBox.text.trim();
        if (qtyStr.isEmpty()) {
            ui.message("Enter quantity first.", GameUI.MsgType.INFO);
            return;
        }

        int qty;
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException e) {
            ui.message("Quantity must be a number.", GameUI.MsgType.INFO);
            return;
        }
        
        int playerId = sessionPlayerId; 

        createContract(chosen.id, playerId, qty);
        
        resultsWidget.setSelectedIndex(-1);
        quantityLabel.visible = false;
        quantityBox.visible = false;
        buyButton.visible = false;
        quantityBox.settext("");
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
        Contract sel = contractsWidget.getSelectedContract();
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
                    contractsWidget.updateContract(c);
          
                    Contract sel = contractsWidget.getSelectedContract();
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
    
    public static class AutoFillEntry extends TextEntry {
        private final List<String> options;
        private Widget popup;

        public AutoFillEntry(Coord c, Coord sz, Widget parent, List<String> options) {
            super(c, sz, parent, "");
            this.options = options;
        }

        @Override
        public void changed() {
            if (popup != null) { popup.reqdestroy(); popup = null; }
            String text = this.text.trim().toLowerCase();
            if (text.isEmpty()) return;

            List<String> matches = new ArrayList<>();
            for (String s : options)
                if (s.toLowerCase().contains(text)) matches.add(s);
            if (matches.isEmpty() || matches.size() > 10) return; 

            final List<String> useMatches = matches;
            popup = new Widget(this.c.add(0, this.sz.y),
                    new Coord(this.sz.x, Math.min(useMatches.size()*18, 180)), this.parent) {
                @Override
                public void draw(GOut g) {
                    g.chcolor(30,30,30,220);
                    g.frect(Coord.z, sz);
                    g.chcolor();
                    int y=0;
                    for (String s : useMatches) {
                        String matchText = AutoFillEntry.this.text.trim();
                        int idx = s.toLowerCase().indexOf(matchText.toLowerCase());
                        
                        if (idx != -1) {
                            g.text(s.substring(0, idx), new Coord(5, y+3));
                            g.chcolor(200, 200, 50, 255);
                            g.text(s.substring(idx, idx + matchText.length()), new Coord(5 + idx*6, y+3));
                            g.chcolor(255, 255, 255, 255);
                            g.text(s.substring(idx + matchText.length()), new Coord(5 + (idx + matchText.length())*6, y+3));
                        } else {
                            g.text(s, new Coord(5, y+3));
                        }
                        
                        y+=18;
                    }
                }

                @Override
                public boolean mousedown(Coord c, int button) {
                    int idx = c.y/18;
                    if (idx >=0 && idx < useMatches.size()) {
                        AutoFillEntry.this.settext(useMatches.get(idx));
                        reqdestroy(); popup=null;
                        searchButton.click(); 
                        return true;
                    }
                    return false;
                }
                
                @Override
                public void destroy() {
                    if (popup == this) popup = null; 
                    super.destroy();
                }
            };
        }

        @Override
        public void destroy() {
            if (popup != null) popup.reqdestroy();
            super.destroy();
        }
    }

    private static String sha256(String base) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(base.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for(byte b : hash) hexString.append(String.format("%02x",b));
        return hexString.toString();
    }
}

// ----------------------------------------------------------------------
// RESULTS WIDGET
// ----------------------------------------------------------------------
class ResultsWidget extends Widget {
    private List<Listing> listings = Collections.emptyList();
    private int selected = -1;
    private SelectionListener listener;
    private final int rowHeight = 20;

    public ResultsWidget(Coord c, Coord sz, Widget parent) {
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
// CONTRACTS WIDGET
// ----------------------------------------------------------------------
class ContractsWidget extends Widget {
    private List<Contract> contracts = Collections.emptyList();
    private int selected = -1;
    private ContractSelectionListener listener;
    private final int rowHeight = 20;

    public ContractsWidget(Coord c, Coord sz, Widget parent) {
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
/////////
/// Listings Widget
/////////
class ListingsWidget extends Widget{
    //public static final MyFirstPlugin plugin;
    public ListingsWidget(Coord c,Coord sz,Widget parent) {
        super(c,sz,parent);
    }
    //(Widget parent, MyFirstPlugin plugin
    //////////////////
    /// Components 
    public static BBWindow.AutoFillEntry listitemBox;
    public static TextEntry listQuantityBox;
    public static TextEntry listPriceBox;
    public static Button createListingButton;
    public static Label listILabel;
    public static Label listQtyLabel;
    public static Label listPrLabel;
    public static Button listLButton;
    public static Button listCButton;
    /////////
    /// API
    public static final String createListingUrl = "http://45.139.50.11:6436/create_listing.php";
    //////////
    /// Tabs
    public void showListItemTab() {
        BBWindow.hideAllWidgets();
        resetListingsTab();
        listLButton.visible=true;
        listCButton.visible=true;
        BBWindow.tabSearchButton.visible=true; 
        BBWindow.tabContractsButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    public void showListCItemTab() {
        BBWindow.hideAllWidgets();
        resetListingsTab();
        listitemBox.visible=true;
        listQuantityBox.visible=true;
        listPriceBox.visible=true;
        createListingButton.visible=true;
        listILabel.visible=true;
        listQtyLabel.visible=true;
        listPrLabel.visible=true;
        BBWindow.tabSearchButton.visible=true; 
        BBWindow.tabContractsButton.visible=true;

        ui.root.wdgmsg("redraw");
    }
    public void showListLItemTab() {
        BBWindow.hideAllWidgets();
        resetListingsTab();
        BBWindow.tabSearchButton.visible=true; 
        BBWindow.tabContractsButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    public void resetListingsTab(){
        BBWindow.listingsWidget.visible = true;
        listitemBox.visible=false;
        listQuantityBox.visible=false;
        listPriceBox.visible=false;
        createListingButton.visible=false;
        listILabel.visible=false;
        listQtyLabel.visible=false;
        listPrLabel.visible=false;
        listLButton.visible=false;
        listCButton.visible=false;
        ui.root.wdgmsg("redraw");
    }
    /////////////
    /// Functions
    
    public void makeListingsWidget(int aszX,int aszY) { 
        int startY = 100;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 35;
        int centerX = (aszX - fieldWidth) / 2;
        int centerY = (aszY - fieldHeight) / 2;

        // Create Listing
        listILabel = new Label(new Coord(centerX, startY - 15), this, "Item:"){
            @Override
            public void draw(GOut g) {
                // background for the tab area
                g.chcolor(0,0,0,150);
                g.frect(Coord.z, this.sz);
                g.chcolor();
                super.draw(g);
            }
        };
        listitemBox = new BBWindow.AutoFillEntry(new Coord(centerX, startY), new Coord(fieldWidth, fieldHeight), this,BBWindow.remoteOptions);

        listQtyLabel = new Label(new Coord(centerX, startY + spacing - 15), this, "Quantity:"){
            @Override
            public void draw(GOut g) {
                // background for the tab area
                g.chcolor(0,0,0,150);
                g.frect(Coord.z, this.sz);
                g.chcolor();
                super.draw(g);
            }
        };
        listQuantityBox = new TextEntry(new Coord(centerX, startY + spacing), new Coord(fieldWidth, fieldHeight), this, "");

        listPrLabel = new Label(new Coord(centerX, startY + spacing * 2 - 15), this, "Price:"){
            @Override
            public void draw(GOut g) {
                // background for the tab area
                g.chcolor(0,0,0,150);
                g.frect(Coord.z, this.sz);
                g.chcolor();
                super.draw(g);
            }
        };
        listPriceBox = new TextEntry(new Coord(centerX, startY + spacing * 2), new Coord(fieldWidth, fieldHeight), this, "");

        createListingButton = new Button(new Coord(centerX, startY + spacing * 3 + 5), fieldWidth, this, "Create Listing") {
            @Override
            public void click() {
                handleCreateListing();
            }
        };
        /////Select Create/List
        listCButton = new Button(new Coord(centerX, centerY + 20), fieldWidth, this, "New Listing") {
            @Override
            public void click() {
                showListCItemTab();
            }
        };
        listLButton = new Button(new Coord(centerX, centerY -20), fieldWidth, this, "View Listings") {
            @Override
            public void click() {
                showListLItemTab();
            }
        };
    }


    public static void handleCreateListing(){
        String item = listitemBox.text.trim();
        String qty = listQuantityBox.text.trim();
        String price = listPriceBox.text.trim();

        if (item.isEmpty() || qty.isEmpty() || price.isEmpty()) {
            //ui.message("All fields are required.", GameUI.MsgType.ERROR);
            return;
        }
        createListing(price, item, qty);

    }

    public static void createListing(String price, String item, String quantity) {
        String sessionKey = BBWindow.sessionToken;
        new Thread(() -> {
            try {
                String encodedPrice = URLEncoder.encode(price, "UTF-8");
                String encodedItem = URLEncoder.encode(item, "UTF-8");
                String encodedQuantity = URLEncoder.encode(quantity, "UTF-8");
                String postData = "price=" + encodedPrice + 
                                  "&item=" + encodedItem + 
                                  "&quantity=" + encodedQuantity +
                                  "&key=" + sessionKey;
                // --- FIX END ---
                //ui.message("data--" + postData, GameUI.MsgType.INFO);
                URL url = new URL(createListingUrl);
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
                    //ui.message("Contract Failed: " + errorMsg, GameUI.MsgType.ERROR);
                } else {
                    Pattern p = Pattern.compile("\"contract_id\"\\s*:\\s*([0-9]+)");
                    Matcher m = p.matcher(jsonResponse);
                    String contractId = m.find() ? m.group(1) : "???";
                    //ui.message("✅ " + jsonResponse, GameUI.MsgType.INFO);
                    //ui.message("✅ Listing created successfully!", GameUI.MsgType.INFO);
                    // refresh contracts
                    //BBWindow.fetchContracts();
                }

            } catch (Exception e) {
                e.printStackTrace();
                //ui.message("Error communicating with server: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
        }).start();
    }
} 
/////////
/// Search Widget
/////////
class SearchWidget extends Widget{
    //public static final MyFirstPlugin plugin;
    public SearchWidget(Coord c,Coord sz,Widget parent) {
        super(c,sz,parent);
    }
    //////////////////
    /// Components 
    public static BBWindow.AutoFillEntryAutoFillEntry searchBox;
    public static Button searchButton;
    public static ResultsWidget resultsWidget;
    public static Label quantityLabel;
    public static TextEntry quantityBox;
    public static Button buyButton;
    public static Button listButton;
    /////////
    /// API
    private final String listingsBaseUrl = "http://45.139.50.11:6436/listings.php";
    //////////
    /// Tabs
    public void showSearchTab() {
        BBWindow.hideAllWidgets();
        BBWindow.tabSearchButton.visible=true; 
        BBWindow.tabContractsButton.visible=true;
        resultsWidget.visible = true;
        searchBox.visible = true;
        searchButton.visible = true;      
        resultsWidget.setSelectedIndex(-1); 
        listButton.visible = true;
    /////////////
    /// Functions

    private void makeSearchWidget() {
        int tabY = 10;

        // --- Results Widget (inline version) ---
        int resultsX = 10;
        int resultsW = asz.x - 20;
        int resultsY = tabY + 50;
        int resultsHeight = 200;

        resultsWidget = new Widget(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), this) {
            private List<Listing> listings = Collections.emptyList();
            private int selected = -1;
            private final int rowHeight = 20;
            private SelectionListener listener;

            private String safe(String s) { return (s == null) ? "" : s; }

            @Override
            public void draw(GOut g) {
                g.chcolor(0, 0, 0, 180);
                g.frect(Coord.z, sz);
                g.chcolor();

                int y = 5;
                int maxRows = (sz.y - 10) / rowHeight;
                int rows = Math.min(maxRows, listings.size());

                int colQtyX    = 5;
                int colPriceX  = 80;
                int colVendorX = 160;

                // headers
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

            @Override
            public boolean mousedown(Coord c, int button) {
                int idx = (c.y - 5) / rowHeight;
                int maxRows = (sz.y - 10) / rowHeight;
                if (idx < 0 || idx >= Math.min(maxRows, listings.size())) {
                    selected = -1;
                } else {
                    selected = idx;
                }
                if (listener != null)
                    listener.onSelected(selected);
                ui.root.wdgmsg("redraw");
                return true;
            }

            public void setSelectionListener(SelectionListener l) { this.listener = l; }
            public void setListings(List<Listing> newList) {
                listings = newList;
                selected = -1;
                if (listener != null) listener.onSelected(selected);
            }
            public Listing getListing(int idx) {
                if (idx >= 0 && idx < listings.size()) return listings.get(idx);
                return null;
            }
        };
        resultsWidget.visible = true;

        // --- Search Box ---
        int boxWidth = 200;
        int boxHeight = 20;
        int boxY = resultsY + resultsHeight + 20;
        int centerXBox = (asz.x - boxWidth) / 2;

        searchBox = new AutoFillEntry(new Coord(centerXBox, boxY), new Coord(boxWidth, boxHeight), this, remoteOptions);

        // --- Search Button ---
        int buttonWidth = 120;
        int buttonHeight = 25;
        int buttonY = boxY + boxHeight + 8;
        int centerXButton = (asz.x - buttonWidth) / 2;

        searchButton = new Button(new Coord(centerXButton, buttonY), buttonWidth, this, "Search") {
            @Override
            public void click() {
                final String query = searchBox.text.trim();
                if (query.isEmpty()) {
                    ui.message("Enter something to search for.", GameUI.MsgType.INFO);
                    return;
                }
                ui.message("Searching for: " + query, GameUI.MsgType.INFO);
                fetchListings(query);
            }
        };

        // --- List Item Button ---
        listButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, this, "My Listings") {
            @Override
            public void click() {
                listingsWidget.showListItemTab();
            }
        };

        // --- Buy Quantity Widgets ---
        quantityLabel = new Label(new Coord(centerXBox, boxY - 25), this, "Quantity:");
        quantityBox = new TextEntry(new Coord(centerXBox + 60, boxY - 25), 60, this, "");
        buyButton = new Button(new Coord(centerXBox + 130, boxY - 27), 60, this, "Buy") {
            @Override
            public void click() {
                handleBuy();
            }
        };

        quantityLabel.visible = false;
        quantityBox.visible = false;
        buyButton.visible = false;

        // --- Results Selection Listener ---
        ((SelectionListenerSetter) resultsWidget).setSelectionListener(new SelectionListener() {
            public void onSelected(int idx) {
                if (resultsWidget.visible) {
                    quantityLabel.visible = true;
                    quantityBox.visible = true;
                    buyButton.visible = true;
                    Listing l = ((ListingGetter) resultsWidget).getListing(idx);
                    if (l != null)
                        quantityBox.settext(String.valueOf(l.quantity));
                }
            }
        });
    }

    // Small interfaces for type safety
    private interface SelectionListener {
        void onSelected(int idx);
    }
    private interface SelectionListenerSetter {
        void setSelectionListener(SelectionListener l);
    }
    private interface ListingGetter {
        Listing getListing(int idx);
    }


    private void fetchListings(final String itemName) {
        new Thread(() -> {
            try {
                String q = URLEncoder.encode(itemName, "UTF-8");
                URL url = new URL(listingsBaseUrl + "?item=" + q);
                StringBuilder sb = new StringBuilder();
                try (Scanner sc = new Scanner(url.openStream())) {
                    while (sc.hasNextLine()) sb.append(sc.nextLine());
                }
                
                List<Listing> list = parseListingsJson(sb.toString()); 
                resultsWidget.setListings(list);
            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error fetching listings: " + e.getMessage(), GameUI.MsgType.ERROR);
                resultsWidget.setListings(Collections.emptyList());
            }
        }).start();
    }

    private void handleBuy() {
        int sel = resultsWidget.getSelectedIndex();
        if (sel < 0) {
            ui.message("Select a listing first.", GameUI.MsgType.INFO);
            return;
        }
        Listing chosen = resultsWidget.getListing(sel);
        if (chosen == null) return;

        String qtyStr = quantityBox.text.trim();
        if (qtyStr.isEmpty()) {
            ui.message("Enter quantity first.", GameUI.MsgType.INFO);
            return;
        }

        int qty;
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException e) {
            ui.message("Quantity must be a number.", GameUI.MsgType.INFO);
            return;
        }
        
        int playerId = sessionPlayerId; 

        createContract(chosen.id, playerId, qty);
        
        resultsWidget.setSelectedIndex(-1);
        quantityLabel.visible = false;
        quantityBox.visible = false;
        buyButton.visible = false;
        quantityBox.settext("");
    }

    private void makeList (){
        private List<Listing> listings = Collections.emptyList();
        private int selected = -1;
        private SelectionListener listener;
        private final int rowHeight = 20;
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
}

