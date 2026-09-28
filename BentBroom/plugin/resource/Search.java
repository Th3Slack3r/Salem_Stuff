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
    public static BBWindow.AutoFillEntry searchBox;
    public static Button searchButton;
    public Widget resultsWidget;
    public static Label quantityLabel;
    public static TextEntry quantityBox;
    public static Button buyButton;
    public static Button listButton;
    public static SelectionListener listener;
    public static ListingsWidget listingsWidget;
    private List<Listing> listings = Collections.emptyList();
    private int selected = -1;
    //private ResultsSelectionListener resultsListener;
    private final int rowHeight = 20;
    
    /////////
    /// API
    private final String listingsBaseUrl = "http://45.139.50.11:6436/listings.php";
    private final String createContractUrl = "http://45.139.50.11:6436/create_contract.php";
    //////////
    /// Tabs
    public void showSearchTab() {
        BBWindow.hideAllWidgets();
        BBWindow.tabSearchButton.visible=true; 
        BBWindow.tabContractsButton.visible=true;
        resultsWidget.visible = true;
        searchBox.visible = true;
        searchButton.visible = true;      
        //resultsWidget.setSelectedIndex(-1); 
        listButton.visible = true;
    }
    /////////////
    /// Functions
    private String safe(String s) { return (s == null) ? "" : s; }

    public void makeSearchWidget(int aszX) {
        int tabY = 10;

        // --- Results Widget geometry ---
        int resultsX = 10;
        int resultsW = aszX - 20;
        int resultsY = tabY + 50;
        int resultsHeight = 200;

        resultsWidget = new Widget(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), this) {
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

                if (listener != null) listener.onSelected(selected);
                ui.root.wdgmsg("redraw");
                return true;
            }
        };
        resultsWidget.visible = true;

        // --- Search Box ---
        int boxWidth = 200;
        int boxHeight = 20;
        int boxY = resultsY + resultsHeight + 20;
        int centerXBox = (aszX - boxWidth) / 2;

        searchBox = new BBWindow.AutoFillEntry(new Coord(centerXBox, boxY), new Coord(boxWidth, boxHeight), this, BBWindow.remoteOptions);

        // --- Search Button ---
        int buttonWidth = 120;
        int buttonHeight = 25;
        int buttonY = boxY + boxHeight + 8;
        int centerXButton = (aszX - buttonWidth) / 2;

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

        // --- List Button ---
        listButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, this, "My Listings") {
            @Override
            public void click() {
                listingsWidget.showListItemTab();
            }
        };

        // --- Buy widgets ---
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
        setSelectionListener(idx -> {
            if (resultsWidget.visible) {
                quantityLabel.visible = true;
                quantityBox.visible = true;
                buyButton.visible = true;
                Listing l = getListing(idx);
                if (l != null) quantityBox.settext(String.valueOf(l.quantity));
            }
        });
    }

    // --- Flattened ResultsWidget methods ---
    public void setSelectionListener(SelectionListener l) {
        this.listener = l;
    }

    public void setSelectedIndex(int idx) {
        this.selected = idx;
    }

    public void setListings(List<Listing> newList) {
        this.listings = newList;
        this.selected = -1;
        if (listener != null) listener.onSelected(selected);
    }

    public int getSelectedIndex() {
        return selected;
    }

    public Listing getListing(int idx) {
        if (idx >= 0 && idx < listings.size())
            return listings.get(idx);
        return null;
    }

    // --- Interface ---
    public interface SelectionListener {
        void onSelected(int idx);
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
                setListings(list);
            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error fetching listings: " + e.getMessage(), GameUI.MsgType.ERROR);
                setListings(Collections.emptyList());
            }
        }).start();
    }

    private void handleBuy() {
        int sel = getSelectedIndex();
        if (sel < 0) {
            ui.message("Select a listing first.", GameUI.MsgType.INFO);
            return;
        }
        Listing chosen = getListing(sel);
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
        
        int playerId = BBWindow.sessionPlayerId; 

        createContract(chosen.id, playerId, qty);
        
        setSelectedIndex(-1);
        quantityLabel.visible = false;
        quantityBox.visible = false;
        buyButton.visible = false;
        quantityBox.settext("");
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
            Listing l = BBWindow.parseListingObject(obj);
            if (l != null) out.add(l);
        }
        return out;
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
                    //fetchContracts();
                }

            } catch (Exception e) {
                e.printStackTrace();
                ui.message("Error communicating with server: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
        }).start();
    }
}

