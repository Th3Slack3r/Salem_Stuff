/////////
/// Template Widget
/////////
class FeedbackWidget extends Widget{
    //public static final MyFirstPlugin plugin;
    public FeedbackWidget(Coord c,Coord sz,Widget parent) {
        super(c,sz,parent);
    }
    //////////////////
    /// Components 
    public static Widget feedbackWidget; 
    public static int feedbackSelectedRating = 5;
    public static TextEntry feedbackCommentBox;
    public static Contract feedbackTargetContract = null;
    /////////
    /// API
    private final String createFeedbackUrl = "http://45.139.50.11:6436/create_feedback.php"; 
    private final String loadFeedbackUrl = "http://45.139.50.11:6436/load_feedback.php";  
    //////////
    /// Tabs
    public void showFeedbackTab(Contract c) {
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
    /////////////
    /// Functions
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
                contractsWidget.showContractsTab();
            }
        };
    }
    public void showFeedbackTab(Contract c) {
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
                contractsWidget.showContractsTab();  
                } else {
                    System.out.println("Feedback submission failed: HTTP " + respCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

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
                        contractsWidget.feedbackButton.visible = !c.feedbackLeft && c.status.equals("completed")
                                && ((c.role.equals("buyer") && c.buyerId == sessionPlayerId) || (c.role.equals("seller") && c.sellerId == sessionPlayerId));
                    }
                    return null;
                });

            } catch (Exception e) {
                System.out.println("[checkFeedbackLeftAsync] error: " + e.getMessage());
            }
        }).start();
    }



}