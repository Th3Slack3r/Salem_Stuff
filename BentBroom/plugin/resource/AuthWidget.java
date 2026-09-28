/////////
/// Auth Widget
/////////
class AuthWidget extends Widget{
    //public static final MyFirstPlugin plugin;
    public AuthWidget(Coord c,Coord sz,Widget parent) {
        super(c,sz,parent);
    }
    //////////////////
    /// Components 
    private Button authChoiceLoginButton;
    private Button authChoiceCreateButton;
    private Label loginUsernameLabel;
    private TextEntry loginUsernameBox;
    private Label loginPasswordLabel;
    private TextEntry loginPasswordBox;
    private Button loginSubmitButton;
    private Label createUsernameLabel;
    private TextEntry createUsernameBox;
    private Label createPasswordLabel;
    private TextEntry createPasswordBox;
    private Label createEmailLabel;
    private TextEntry createEmailBox;
    private Label createHSLabel;
    private TextEntry createHSBox;
    private Button createSubmitButton;

    /////////
    /// API
    private static final String loginConfig = "plugindata/BB.conf";
    private static final String loginTokenURL = "http://45.139.50.11:6436/validate_token.php";
    //////////
    /// Tabs
    private void showAuthChoice(){
        BBWindow.hideAllWidgets();
        resetAuth();
        authChoiceLoginButton.visible=true; authChoiceCreateButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    private void showAuthLogin(){
        BBQWindow.hideAllWidgets();
        resetAuth();
        loginUsernameLabel.visible=true; loginUsernameBox.visible=true;
        loginPasswordLabel.visible=true; loginPasswordBox.visible=true;
        loginSubmitButton.visible=true;
        ui.root.wdgmsg("redraw");
    }
    private void showAuthCreate(){
        BBWindow.hideAllWidgets();
        resetAuth();
        createUsernameLabel.visible=true; createUsernameBox.visible=true;
        createPasswordLabel.visible=true; createPasswordBox.visible=true;
        createEmailLabel.visible=true; createEmailBox.visible=true;
        createHSLabel.visible=true; createHSBox.visible=true;
        ui.root.wdgmsg("redraw");
    }
    private viod resetAuth(){
            authChoiceLoginButton.visible=false; authChoiceCreateButton.visible=false;
            loginUsernameLabel.visible=false; loginUsernameBox.visible=false;
            loginPasswordLabel.visible=false; loginPasswordBox.visible=false;
            loginSubmitButton.visible=false;

            createUsernameLabel.visible=false; createUsernameBox.visible=false;
            createPasswordLabel.visible=false; createPasswordBox.visible=false;
            createEmailLabel.visible=false; createEmailBox.visible=false;
            createHSLabel.visible=false; createHSBox.visible=false;
            createSubmitButton.visible=false; 
            BBWindow.authWidget.visible=true;
    }
    /////////////
    /// Functions
    /// 
    /// 
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
                searchWidget.showSearchTab();

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
                contractsWidget.fetchContracts();

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
                searchWidget.showSearchTab();
                return null;
            });



        } catch (Exception e) {
            e.printStackTrace();
            ui.message("Auto-login failed: " + e.getMessage(), GameUI.MsgType.ERROR);
        }
    }