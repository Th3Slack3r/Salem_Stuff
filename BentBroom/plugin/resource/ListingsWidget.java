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