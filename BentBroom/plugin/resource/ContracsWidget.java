/////////
/// Template Widget
/////////
class ContractsWidget extends Widget{
    //public static final MyFirstPlugin plugin;
    public ContractsWidget(Coord c,Coord sz,Widget parent) {
        super(c,sz,parent);
    }
    //////////////////
    /// Components 
    public static ContractsWidget contractsWidget;
    public static Button confirmContractButton;
    public static Button feedbackButton;

    /////////
    /// API
    private final String contractsUrl = "http://45.139.50.11:6436/contracts.php";
    private final String confirmContractUrl = "http://45.139.50.11:6436/confirm_contract.php";
    //////////
    /// Tabs
    private void showContractsTab() {
        BBWindow.hideAllWidgets();
        tabSearchButton.visible = true;
        tabContractsButton.visible = true;
        contractsWidget.visible = true;
        fetchContracts();
    }
    /////////////
    /// Functions
    public void makeContractsWidget() {
        int tabY = 10;

        // --- Contracts Widget ---
        int resultsX = 10;
        int resultsW = asz.x - 20;
        int resultsY = tabY + 50;
        int resultsHeight = 200;

        contractsWidget = new Widget(new Coord(resultsX, resultsY), new Coord(resultsW, resultsHeight), this) {
            private List<Contract> contracts = Collections.emptyList();
            private int selected = -1;
            private final int rowHeight = 20;
            private ContractSelectionListener listener;

            @Override
            public void draw(GOut g) {
                g.chcolor(0, 0, 0, 180);
                g.frect(Coord.z, sz);
                g.chcolor();

                int y = 5;
                int maxRows = (sz.y - 10) / rowHeight;
                int rows = Math.min(maxRows, contracts.size());

                int colItemX = 5;
                int colQtyX = 100;
                int colPriceX = 140;
                int colStatusX = 180;

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

            public void setContractSelectionListener(ContractSelectionListener l) {
                this.listener = l;
            }

            public interface ContractSelectionListener {
                void onSelected(Contract c);
            }
        };

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
                Contract sel = ((ContractSelectionListener) contractsWidget).getSelectedContract();
                if (sel != null) showFeedbackTab(sel);
            }
        };
        feedbackButton.visible = false;

        // --- Selection Listener ---
        ((ContractsWidget.ContractSelectionListener) contractsWidget).setContractSelectionListener(new ContractsWidget.ContractSelectionListener() {
            public void onSelected(Contract c) {
                if (c == null) {
                    confirmContractButton.visible = false;
                    feedbackButton.visible = false;
                    return;
                }

                int playerId = sessionPlayerId;
                boolean needsConfirmation = false;
                boolean needsFeedback = false;

                if (((c.role.equals("buyer") && c.buyerId == playerId) ||
                    (c.role.equals("seller") && c.sellerId == playerId)) &&
                    c.playerConfirmed == 0 &&
                    c.status.equals("active")) {
                    needsConfirmation = true;
                }

                if (((c.role.equals("buyer") && c.buyerId == playerId) ||
                    (c.role.equals("seller") && c.sellerId == playerId)) &&
                    !c.feedbackLeft &&
                    c.status.equals("completed")) {
                    needsFeedback = true;
                }

                confirmContractButton.visible = needsConfirmation;
                feedbackButton.visible = needsFeedback;
            }
        });
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

    private void handleConfirmContract() {
        Contract sel = contractsWidget.getSelectedContract();
        if (sel == null) return;
        
        int playerId = sessionPlayerId; // Use the actual player ID
        
        // Before sending, ensure the button is hidden to prevent double-clicks
        confirmContractButton.visible = false;
        
        // Pass the contract ID, player ID, and role to the network method
        confirmContract(sel.id, playerId, sel.role);
    }

}