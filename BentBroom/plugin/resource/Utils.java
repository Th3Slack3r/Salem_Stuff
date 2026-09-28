import haven.GameUI;
import haven.Widget;

/////////////////////////////
///Makers
private ContractsWidget contractsWidget;

private void makeContractsWidget() {
    contractWidget = new Widget(new Coord(10, 50), new Coord(asz.x - 20, asz.y - 120), this) {
        @Override
        public void draw(GOut g) {
            // background for the tab area
            g.chcolor(0,0,0,150);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
        }
    };
    contractWidget.visible = false;
    int tabY = 10;
    // --- Contracts List ---
    int resultsX = 10;
    int resultsW = asz.x - 20;
    int resultsY = tabY + 50;
    int resultsHeight = 200;
    contractList = new ContractsList(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), contractWidget);
    contractsList.visible = false;

    // --- Buttons ---
    int buttonWidth = 120;
    int buttonHeight = 25;
    int buttonY = resultsY + resultsHeight + 10;
    int centerXButton = (asz.x - buttonWidth) / 2;

    confirmContractButton = new Button(new Coord(centerXButton, buttonY), buttonWidth, contractWidget, "Confirm Contract") {
        @Override
        public void click() {
            handleConfirmContract();
        }
    };
    confirmContractButton.visible = false;

    feedbackButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, contractWidget, "Leave Feedback") {
        @Override
        public void click() {
            Contract sel = contractsList.getSelectedContract();
            if (sel != null) showFeedbackTab(sel);
        }
    };
    feedbackButton.visible = false;

    // --- Contracts Selection Listener ---
    contractsList.setContractSelectionListener(new ContractsList.ContractSelectionListener() {
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

private AuthgWidget authWidget;

private void makeAuthWidget() {
    authWidget = new Widget(new Coord(10, 50), new Coord(asz.x - 20, asz.y - 120), this) {
        @Override
        public void draw(GOut g) {
            // background for the tab area
            g.chcolor(0,0,0,150);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
        }
    };
    authWidget.visible = false;

    // Add the inner list widget
    listWidget = new Widget(new Coord(0, 0), authWidget.sz, authWidget);

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

private SearchWidget searchWidget;

private void makeSearchWidget() {
    
    // Build Widget and hide it
    searchWidget = new Widget(new Coord(10, 50), new Coord(asz.x - 20, asz.y - 120), this) {
        @Override
        public void draw(GOut g) {
            // background for the tab area
            g.chcolor(0,0,0,150);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
        }
    };
    searchWidget.visible = false;
    int tabY = 10;
    // --- Results List---
    int resultsX = 10;
    int resultsW = asz.x - 20;
    int resultsY = tabY + 50;
    int resultsHeight = 200;

    resultsList = new ResultsList(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), searchWidget);
    resultsWidget.visible = true;

    // --- Search Box ---
    int boxWidth = 200;
    int boxHeight = 20;
    int boxY = resultsY + resultsHeight + 20;
    int centerXBox = (asz.x - boxWidth) / 2;

    searchBox = new AutoFillEntry(new Coord(centerXBox, boxY), new Coord(boxWidth, boxHeight), searchWidget, remoteOptions);

    // --- Search Button ---
    int buttonWidth = 120;
    int buttonHeight = 25;
    int buttonY = boxY + boxHeight + 8;
    int centerXButton = (asz.x - buttonWidth) / 2;

    searchButton = new Button(new Coord(centerXButton, buttonY), buttonWidth, searchWidget, "Search") {
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
    listButton = new Button(new Coord(centerXButton, buttonY + buttonHeight + 8), buttonWidth, searchWidget, "My Listings") {
        @Override
        public void click() {
            listingWidget.showListItemTab();
        }
    };
    // --- Buy Quantity Widgets ---
    quantityLabel = new Label(new Coord(centerXBox, boxY - 25),searchWidget, "Quantity:");
    quantityBox = new TextEntry(new Coord(centerXBox + 60, boxY - 25), 60, searchWidget, "");
    buyButton = new Button(new Coord(centerXBox + 130, boxY - 27), 60, searchWidget, "Buy") {
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

private FeedbackWidget feedbackWidget;

private void makeFeedbackWidget() {
    // Build feedbackWidget but keep it hidden by default
    feedbackWidget = new Widget(new Coord(10, 50), new Coord(asz.x - 20, asz.y - 120), this) {
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

private ListingsWidget listingsWidget;

private void makeListingsWidget() {
    listingsWidget = new Widget(new Coord(10, 50), new Coord(asz.x - 20, asz.y - 120), this) {
        @Override
        public void draw(GOut g) {
            // background for the tab area
            g.chcolor(0,0,0,150);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
        }
    };
    listingsWidget.visible = false;
        int startY = 100;
        int labelOffset = 100;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 35;
        int centerX = (asz.x - fieldWidth) / 2;
        int centerY = (asz.y - fieldHeight) / 2;

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
      listitemBox = new AutoFillEntry(new Coord(centerX, startY), new Coord(fieldWidth, fieldHeight), this,remoteOptions);

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
//////////////////////////////////
///Utilities

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


///Header
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

///Hide All
private void hideWidget(){
  listingsWidget.visible = false;
  feedbackWidget.visible = false;
  searchWidget.visible = false;
  authWidget.visible = false;
  contractsWidget.visible = false;
}
////Auto Fill
private class AutoFillEntry extends TextEntry {
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
///Hash
private static String sha256(String base) throws Exception{
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(base.getBytes("UTF-8"));
    StringBuilder hexString = new StringBuilder();
    for(byte b : hash) hexString.append(String.format("%02x",b));
    return hexString.toString();
}


