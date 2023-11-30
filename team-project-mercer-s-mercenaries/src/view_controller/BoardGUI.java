/**
 * This class implements the GUI for the start menu and Battleship game itself
 * @authors Ryan Bullard, Elliot Seo, Gijeong Lee, and Kartikey Bihani
 */

package view_controller;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.stage.Stage;

import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Color;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;

import javafx.scene.input.KeyCode;

import javafx.stage.WindowEvent;

import Networking.Client.Client;

import model.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Objects;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;

import javafx.util.Duration;

public class BoardGUI extends Application {
	// menuScene Components
	private VBox menuLayout;
    private Button easyButton;
	private Button hardButton;
	private Button onlineButton;
	private Button tutorialButton;
    private TextArea placementText;
    private Label gameSummaryLabel;
    private Board playerBoard;
    private Board enemyBoard;
    private GridPane opponentBoard;
    private Scene placementScene;
    private Scene menuScene;
    private Scene tutorialScene;

    private ComputerPlayer computerPlayer;
    private Rectangle[][] playerCells;
    private Rectangle[][] enemyCells;
    private HashMap<Integer, Integer> shipLens;
    private HashMap<Point, Ship> ships;
    private HashMap<Point, Ship> enemyShips;
    private int currLen;
    private int currRow;
    private int currCol;
    private boolean horizontal = true;
    private boolean gameReady; // Flag to tell if the user has placed all of their ships
    private boolean shotReady;
    private boolean online = false;
    private boolean host = false;
    private String username;
    private Client client = null;
    private Stage stage;
    private GridPane yourBoard;

    private Stage chatWindow;
    private TextArea chatArea;

    private Label prompt;
    private TextField userText;
    private final String imagesFolder = "view_controller/images/";
    private final String animationFolder = "view_controller/animations/";

    private final ImagePattern waterPattern = new ImagePattern(new Image(imagesFolder + "water.png"));
    private final ImagePattern missPattern = new ImagePattern(new Image(imagesFolder + "water_miss.png"));
    private final ImagePattern noseHPattern = new ImagePattern(new Image(imagesFolder + "nose_h.png"));
    private final ImagePattern noseVPattern = new ImagePattern(new Image(imagesFolder + "nose_v.png"));
    private final ImagePattern bodyHPattern = new ImagePattern(new Image(imagesFolder + "body_h.png"));
    private final ImagePattern bodyVPattern = new ImagePattern(new Image(imagesFolder + "body_v.png"));
    private final ImagePattern noseVSunk = new ImagePattern(new Image(imagesFolder + "nose_v_sunk.png"));
    private final ImagePattern bodyVSunk = new ImagePattern(new Image(imagesFolder + "body_v_sunk.png"));
    private final ImagePattern noseHSunk = new ImagePattern(new Image(imagesFolder + "nose_h_sunk.png"));
    private final ImagePattern bodyHSunk = new ImagePattern(new Image(imagesFolder + "body_h_sunk.png"));
    private final ImagePattern splash = new ImagePattern(new Image(animationFolder + "splash.gif"));
    private final ImagePattern noseVExplosion = new ImagePattern(new Image(animationFolder + "nose_v_explosion.gif"));
    private final ImagePattern bodyVExplosion = new ImagePattern(new Image(animationFolder + "body_v_explosion.gif"));
    private final ImagePattern noseHExplosion = new ImagePattern(new Image(animationFolder + "nose_h_explosion.gif"));
    private final ImagePattern bodyHExplosion = new ImagePattern(new Image(animationFolder + "body_h_explosion.gif"));
    private final ImagePattern noseVSink = new ImagePattern(new Image(animationFolder + "nose_v.gif"));
    private final ImagePattern bodyVSink = new ImagePattern(new Image(animationFolder + "body_v.gif"));
    private final ImagePattern noseHSink = new ImagePattern(new Image(animationFolder + "nose_h.gif"));
    private final ImagePattern bodyHSink = new ImagePattern(new Image(animationFolder + "body_h.gif"));


    private final String filePath = Objects.requireNonNull(getClass().getResource("/sound_effects/wav/explosion.wav")).toString();
    private final Media media = new Media(filePath);
    private final MediaPlayer mediaPlayer = new MediaPlayer(media);

    private final String filePath2 = Objects.requireNonNull(getClass().getResource("/sound_effects/wav/splash.wav")).toString();
    private final Media media2 = new Media(filePath2);
    private final MediaPlayer mediaPlayer2 = new MediaPlayer(media2);
    
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        mainMenu();
    }

    private void mainMenu() {

        stage.setOnCloseRequest((event) -> {
            if(client != null) {
                client.disconnect(username);
                chatWindow.close();
            }
            Platform.exit();
            System.exit(0);
        });

        layoutMenuGUI();
        reset();

        // Shows the start menu first
        menuScene = new Scene(menuLayout, 400, 300);
        stage.setScene(menuScene);
        stage.setTitle("Battleship");
        stage.show();

        // local handlers to swap the stage from menuScene to gameScene
        easyButton.setOnAction(e -> {
            placementScene();
            computerPlayer.setStrategy(new RandomAI());
        });
        hardButton.setOnAction(e -> {
            placementScene();
            computerPlayer.setStrategy(new SmartAI());
        });
        onlineButton.setOnAction(e -> {
            connectionScene(); // Scene that sets username.
        });
        tutorialButton.setOnAction(e -> {
        	tutorialScene = tutorialScene();
        	stage.setScene(tutorialScene);
        });
    }
    
    /**
     * This method lays out the components for the menuScene
     */
    private void layoutMenuGUI() {
    	menuLayout = new VBox(10); 
    	menuLayout.setAlignment(Pos.CENTER);
        Label menuTitle = new Label("Battleship");
        menuTitle.setFont(Font.font("Impact", FontWeight.BOLD, 36));
        
        Image battleshipImage = new Image("view_controller/images/battleship.png");
        ImageView imageView = new ImageView(battleshipImage);
        imageView.setFitWidth(300);  // Adjust the width as needed
        imageView.setPreserveRatio(true);

        HBox menuButtons = new HBox(10);  // using better naming
        easyButton = new Button("Easy");
        easyButton.setFont(Font.font("Impact"));
		hardButton = new Button("Hard");
		hardButton.setFont(Font.font("Impact"));
		onlineButton = new Button("Online");
		onlineButton.setFont(Font.font("Impact"));
		tutorialButton = new Button("Game Tutorial");
		tutorialButton.setFont(Font.font("Impact"));
		
		menuButtons.getChildren().addAll(easyButton, hardButton, onlineButton, tutorialButton);
		menuButtons.setAlignment(Pos.CENTER);

    	menuLayout.getChildren().addAll(menuTitle, imageView, menuButtons);
    	menuLayout.setStyle("-fx-background-color: grey;");
    }
    

    /**
     * This method lays out the components for the tutorialScene
     */
    private Scene tutorialScene() {
        VBox tutorialLayout = new VBox(10);
        tutorialLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Battleship Game Tutorial");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        Label instructionLabel = new Label(
                """
                        Welcome to the Battleship Game!

                        Objective:
                        Sink all enemy ships before they sink yours.

                        Instructions:
                        1. Use 'Z' to toggle ship orientation (horizontal/vertical).
                        2. Use keys 2-5 to select ship length.
                        3. Click on your board to place ships.
                        4. Game starts when all ships are placed.
                        5. Click on enemy board to take shots.
                        6. Game ends when all enemy ships are sunk or yours are.

                        Good luck and have fun!"""
        );
        instructionLabel.setStyle("-fx-font-size: 16;");

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), instructionLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), titleLabel);
        translateTransition.setByY(5);
        translateTransition.setCycleCount(TranslateTransition.INDEFINITE);
        translateTransition.setAutoReverse(true);
        translateTransition.play();

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14;");
        backButton.setOnAction(e -> mainMenu());

        tutorialLayout.getChildren().addAll(titleLabel, instructionLabel, backButton);
        tutorialScene = new Scene(tutorialLayout, 600, 450);

        return tutorialScene;
    }

    /**
     * Creates the scene to get the user's preferred name and if they want to host a game or join an existing one.
     */
    private void connectionScene() {
        VBox connectionLayout = new VBox(15);
        Label ipPrompt = new Label("Enter the IP of the server");
        ipPrompt.setFont(Font.font("Impact", 16));
        ipPrompt.setAlignment(Pos.CENTER); // Center the label
        
        TextField IP = new TextField();
        prompt = new Label("Enter your username");
        prompt.setFont(Font.font("Impact"));
        prompt.setFont(Font.font("Impact", 16));
        userText = new TextField();

        Button host = new Button("Host Game");
        host.setFont(Font.font("Impact"));
        Button join = new Button("Join Game");
        join.setFont(Font.font("Impact"));

        host.setOnAction((event) -> {
            if (userText.getText().contains(";")) {
                prompt.setText("Nice try. No semicolons!");
            } else if (userText.getText().length() < 3 || userText.getText().length() > 20) {
                prompt.setText("Name must be between 3 and 20 long.");
            } else {
                try {
                    client = new Client(IP.getText(), this);
                    this.host = true;
                    client.sendPacket("init;" + userText.getText());
                } catch (UnknownHostException e) {
                    ipPrompt.setText("Unknown host!");
                }
            }
        });

        join.setOnAction((event) -> {
            if (userText.getText().contains(";")) {
                prompt.setText("Nice try. No semicolons!");
            } else if (userText.getText().length() < 3 || userText.getText().length() > 20) {
                prompt.setText("Name must be between 3 and 20 long.");
            } else {
                try {
                    client = new Client(IP.getText(), this);
                    this.host = false;
                    client.sendPacket("init;" + userText.getText());
                } catch (UnknownHostException e) {
                    ipPrompt.setText("Unknown host!");
                }
            }
        });

        HBox buttons = new HBox(20);
        buttons.getChildren().add(host);
        buttons.getChildren().add(join);
        buttons.setAlignment(Pos.CENTER);

        connectionLayout.getChildren().add(ipPrompt);
        connectionLayout.getChildren().add(IP);
        connectionLayout.getChildren().add(prompt);
        connectionLayout.getChildren().add(userText);
        connectionLayout.getChildren().add(buttons);

        connectionLayout.setStyle("-fx-background-color: grey;");
        Scene scene = new Scene(connectionLayout, 220, 200);
        stage.setScene(scene);
    }

    private void createChatWindow() {
        VBox chatLayout = new VBox();
        chatArea = new TextArea();
        chatArea.setEditable(false);
        TextField chatBox = new TextField();
        chatBox.setOnKeyPressed((event) -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                client.sendPacket("chat;" + username + ";" + chatBox.getText());
                chatArea.appendText(username + ": " + chatBox.getText() + "\n");
                chatBox.setText("");
            }
        });
        chatLayout.getChildren().add(chatArea);
        chatLayout.getChildren().add(chatBox);
        Scene chatScene = new Scene(chatLayout, 500, 200);
        chatArea.textProperty().addListener((observable, oldValue, newValue) -> chatArea.setScrollTop(Double.MAX_VALUE));
        chatWindow = new Stage();
        chatWindow.setTitle("Battleship Game Chat");
        chatWindow.setScene(chatScene);
        chatWindow.show();
    }

    /**
     * Scene to show to game hosts.
     */
    private void hostScene() {
        Scene hostScene = new Scene(new Label("Waiting for another user"));
        stage.setScene(hostScene);
    }

    /**
     * Scene to show to players trying to join existing games.
     * @param hosts The list of current game hosts, separated by a semicolon.
     */
    private void joinScene(String hosts) {
        StringBuilder buf = new StringBuilder();
        ListView<String> listView = new ListView<>();
        for(int i = 5; i < hosts.length(); i++) {
            if(hosts.charAt(i) == ';') {
                listView.getItems().add(buf.toString());
                buf = new StringBuilder();
            } else {
                buf.append(hosts.charAt(i));
            }
        }
        listView.setOnMouseClicked((event) -> client.sendPacket("jngm;" + username + ";" + listView.getSelectionModel().getSelectedItem()));
        Scene joinScene = new Scene(listView, 200, 300);
        stage.setScene(joinScene);
    }

    /**
     * Creates and displays the scene used for placement.
     */
    private void placementScene() {
        // Create your board at the bottom
        playerCells = new Rectangle[10][10];
        VBox gameLayout = new VBox(50);

        yourBoard = createBoard(true);
        yourBoard.setAlignment(Pos.CENTER);
        VBox board = new VBox(yourBoard);
        board.setMaxSize(700, 410);
        gameLayout.getChildren().add(board);
        VBox.setMargin(yourBoard, new Insets(30,0,0,0));

        placementText = new TextArea();
        placementText.setEditable(false);
        placementText.setText("Ships Available to Place:\nLength 2: " + shipLens.get(2) + "\nLength 3: " + shipLens.get(3) +
                "\nLength 4: " + shipLens.get(4) + "\nLength 5: " + shipLens.get(5));
        placementText.setFont(Font.font("San Serif", 20));
        placementText.setOnKeyPressed(null);

        gameLayout.getChildren().add(placementText);
        gameLayout.setBackground(Background.fill(new ImagePattern(new Image(imagesFolder + "ocean_numbers_placement.jpg"))));
        placementText.addEventFilter(KeyEvent.KEY_PRESSED, (event) -> { // Pass on events to the placement scene handler
            if(event != null) {
                placementScene.getOnKeyPressed().handle(event); // Probably a better way to do this
            }
        });
        placementScene = new Scene(gameLayout, 700, 700);
        registerPlacementHandlers();
        stage.setScene(placementScene);
    }


    /**
     * This method lays out the components for the gameScene
     */
    private Scene createGameScene() {

        // gameScene Components
        VBox gameLayout = new VBox(50);
        Scene gameScene = new Scene(gameLayout, 700, 820);
        gameLayout.setBackground(Background.fill(new ImagePattern(new Image(imagesFolder + "ocean_numbers.jpg"))));

        // Create the opponent board at the top
        if(!gameReady) {
            enemyCells = new Rectangle[10][10];
            opponentBoard = createBoard(false);
            if(!online) {
                placeEnemyBoard();
            }
        }

        opponentBoard.setAlignment(Pos.CENTER);
        // Add player's board to the bottom
        VBox.setMargin(yourBoard, new Insets(0,0,0,0));
        VBox.setMargin(opponentBoard, new Insets(20, 0, 10, 0));
        gameLayout.getChildren().addAll(opponentBoard, yourBoard);

        return gameScene;
    }

    /**
     * Creates a grid of Rectangles to represent the boards.
     * @param player If this board is being created for the player. Adds cells to the playerCells if true, enemyCells otherwise.
     * @return The board created.
     */
    private GridPane createBoard(Boolean player) {
    	GridPane board = new GridPane();

        int cellSize = 35;
        int boardSize = 10;

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setFill(waterPattern);
                cell.setStroke(Color.BLACK);

                if (player) {
                    playerCells[i][j] = cell;
                } else {
                    enemyCells[i][j] = cell;
                }

                board.add(cell, j, i + 1);
            }
        }
        return board;
    }

    /**
     * Randomly generates the board for the enemy player. Guaranteed to place 5 ships of the 5 specified sizes.
     * Has a 50% chance to place ships horizontally, 50% vertically.
     */
    private void placeEnemyBoard() {
        Random generator = new Random();
        int[] sizes = {2, 3, 3, 4, 5};
        for(int i = 0; i < sizes.length; i++) {
            int row = generator.nextInt(10);
            int col = generator.nextInt(10);
            boolean horizontal = Math.random() > 0.5;

            if (!enemyBoard.placeShip(horizontal, sizes[i], row, col)) {
                i--;
            } else {
                addShip(horizontal, sizes[i], row, col, enemyShips);
            }
        }
    }

    /**
     * Shows a preview of what the ship placement will look like at a given cell by changing the color of the cells.
     */
    private void showPlacementCells() {
        if(currCol < 0 || currRow < 0) {
            return;
        }

        int row = currRow;
        int col = currCol;
        if(horizontal && col > (10 - currLen)) {
            col = 10 - currLen;
        } else if (!horizontal && row > (10 - currLen)){
            row = 10 - currLen;
        }

        char[][] board = playerBoard.getBoard();
        for(int i = 0; i < currLen; i++) { // Don't show new ships that overlap
            if(horizontal) {
                if(board[row][col + i] == 'S') {
                    return;
                }
            } else {
                if(board[row + i][col] == 'S') {
                    return;
                }
            }
        }

        if(horizontal) {
            for (int i = 0; i < currLen; i++) { // Check the placement isn't used already
                if(i == 0) {
                    playerCells[row][col + i].setFill(noseHPattern);
                } else {
                    playerCells[row][col + i].setFill(bodyHPattern);
                }
            }
        } else {
            for (int i = 0; i < currLen; i++) { // Check the placement isn't used already
                if(i == 0) {
                    playerCells[row + i][col].setFill(noseVPattern);
                } else {
                    playerCells[row + i][col].setFill(bodyVPattern);
                }
            }
        }
    }

    /**
     * Resets the color of the current board to the expected colors after the mouse is moved during placement.
     */
    private void resetBoardToCurr() {
        char[][] myBoard = playerBoard.getBoard();
        setPlayerBoard(myBoard, null);
    }

    /**
     * Adds handlers for placement.
     */
    private void registerPlacementHandlers() {
        placementScene.setOnKeyPressed((event) -> { // Let the first cell handle placement key logic
            if(gameReady || currRow < 0 || currCol < 0) { // Don't progress if the game isn't ready or off board.
                return;
            }
            if(event.getCode() == KeyCode.getKeyCode("Z")) {
                horizontal = !horizontal;
            } else if (event.getCode() == KeyCode.getKeyCode("2")) {
                currLen = 2;
            } else if (event.getCode() == KeyCode.getKeyCode("3")) {
                currLen = 3;
            } else if (event.getCode() == KeyCode.getKeyCode("4")) {
                currLen = 4;
            } else if (event.getCode() == KeyCode.getKeyCode("5")) {
                currLen = 5;
            }
            resetBoardToCurr();
            showPlacementCells();
        });
    	for(Rectangle[] cellArr : playerCells) {
            for(Rectangle cell : cellArr) {
                cell.setOnMouseEntered((event) -> { // Shows the placement without
                    if(gameReady) {
                        return;
                    }
                    for(int i = 0; i < playerCells.length; i++) {
                        for(int j = 0; j < playerCells[i].length; j++) {
                            if(playerCells[i][j] == event.getSource()) {
                                currRow = i;
                                currCol = j;
                                showPlacementCells();
                                return;
                            }
                        }
                    }
                });
                cell.setOnMouseClicked((event) -> { // Don't place until the player clicks
                    if(gameReady) {
                        return;
                    }
                    for(int i = 0; i < playerCells.length; i++) {
                        for(int j = 0; j < playerCells[i].length; j++) {
                            if(playerCells[i][j] == event.getSource()) {
                                if(shipLens.get(currLen) == 0) {
                                    // Limit player's placements to 5
                                    return;
                                }
                                if(!playerBoard.placeShip(horizontal, currLen, i, j)) {
                                    System.out.println("lol ur dum");
                                }
                            }
                        }
                    }
                });
                cell.setOnMouseExited((event) -> { // Needs to reset the color of the board after the mouse leaves a cell
                    currRow = -1; // Set it to -1 so if we go off the board the ship
                    currCol = -1; // preview goes away.
                    resetBoardToCurr();
                });
            }
        }
    }

    /**
     * Adds a ship to the internal mapping of positions and ships. Used to quickly look up what animation to use.
     * @param horizontal If the ship was placed horizontally or vertically.
     * @param currLen The length of the ship that was just placed.
     * @param row The row of the nose.
     * @param col The col of the nose.
     * @param map The mapping to add this ship to.
     */
    private void addShip(boolean horizontal, int currLen, int row, int col, HashMap<Point, Ship> map) {
        ArrayList<Point> pos = new ArrayList<>();
        Point nose = null;
        if(horizontal && col > (10 - currLen)) {
            col = 10 - currLen;
        } else if (!horizontal && row > (10 - currLen)){
            row = 10 - currLen;
        }

        if(horizontal) {
            for (int i = 0; i < currLen; i++) {
                if(i == 0) {
                    nose = new Point(row, col + i);
                }
                pos.add(new Point(row, col + i));
            }
        } else {
            for (int i = 0; i < currLen; i++) {
                if(i == 0) {
                    nose = new Point(row + i, col);
                }
                pos.add(new Point(row + i, col));
            }
        }
        Ship ship;
        if(map == ships) {
            ship = new Ship(nose, pos, playerBoard, horizontal);
        } else {
            ship = new Ship(nose, pos, enemyBoard, horizontal);
        }
        for(Point point : pos) {
            map.put(point, ship);
        }
    }

    /**
     * Sets up the game for gameplay after placement is finished.
     */
    private void playSetup() {
        Scene gameScene = createGameScene();
        stage.setScene(gameScene);
        if(!online) {
            for (Rectangle[] cellArr : enemyCells) {
                for (Rectangle cell : cellArr) {
                    cell.setOnMouseClicked((event) -> { // React to the player's clicks
                        if (!shotReady) { // Wait until the computer has made its move
                            return;
                        }
                        for (int i = 0; i < enemyCells.length; i++) {
                            for (int j = 0; j < enemyCells[i].length; j++) {
                                if (enemyCells[i][j] == event.getSource() && enemyBoard.tryShot(i, j) && !enemyBoard.gameOver()) {
                                    getNextCPUMove();
                                }
                            }
                        }
                    });
                }
            }
        } else {
            for (Rectangle[] cellArr : enemyCells) {
                for (Rectangle cell : cellArr) {
                    cell.setOnMouseClicked((event) -> { // React to the player's clicks
                        if (!shotReady) { // Wait until the other player has moved.
                            return;
                        }
                        for (int i = 0; i < enemyCells.length; i++) {
                            for (int j = 0; j < enemyCells[i].length; j++) {
                                if (enemyCells[i][j] == event.getSource() && enemyBoard.tryShot(i, j)) {
                                    client.sendPacket("move;" + username + ";" + i + ";" + j + ";");
                                    shotReady = false;
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void getNextCPUMove() {
        Point point = computerPlayer.desiredMove(playerBoard);
        ExecutorService shotExec = Executors.newSingleThreadExecutor();
        shotExec.submit(() -> {
            try {
                shotReady = false;
                Thread.sleep(500);
                playerBoard.tryShot(point.row, point.col);
                Thread.sleep(100); // So the user can skip the animation
                shotReady = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            shotExec.shutdown();
        });
    }

    /**
     * Generates and displays the end of game stats screen.
     * @param loser True if this player is the loser, false otherwise.
     */
    private void gameOver(boolean loser) {
        VBox gameOverPane = new VBox();
        HBox buttonPane = new HBox();
        Label statsText = getStyledStatsTextArea(loser);
        Button playAgainButton = new Button();
        Button quitButton = new Button();

        configureGamePane(gameOverPane, statsText, buttonPane);
        configureButtonPane(buttonPane, playAgainButton, quitButton);

        Scene scene = createStyledScene(gameOverPane, "Game Over!", 500, 390);

        stage.setScene(scene);
    }

    /**
     * Creates a styled stats text area and returns a Label containing the stats.
     *
     * @param loser True if the player lost, false if the player won.
     * @return Label containing styled game stats.
     */
    private Label getStyledStatsTextArea(boolean loser) {
        TextArea statsTextArea = new TextArea();
        gameSummaryLabel = new Label();
        statsTextArea.setEditable(false);
        String playerHitRate = calculateHitRate(enemyBoard);
        String enemyHitRate = calculateHitRate(playerBoard);

        configureStatsTextArea(statsTextArea, loser, playerHitRate, enemyHitRate);

        return gameSummaryLabel;
    }
    
    private String calculateHitRate(Board board) {
        double hitRate = ((double) board.getNumHits() / board.getNumMoves()) * 100;
        String hitRateString = String.format("%.2f", hitRate);

        return hitRateString.length() >= 5 ?
                (hitRateString.charAt(4) >= '5' ? hitRateString.substring(0, 3) + (hitRateString.charAt(4) + 1) : hitRateString.substring(0, 4)) :
                hitRateString;
    }

    /**
     * Configures the layout of the game pane.
     *
     * @param gameOverPane The main game over pane.
     * @param statsLabel   The label displaying game stats.
     * @param buttonPane   The pane containing buttons.
     */
    private void configureGamePane(VBox gameOverPane, Label statsLabel, HBox buttonPane) {
        gameOverPane.setSpacing(10);  // Added padding below the title
        gameOverPane.setAlignment(Pos.CENTER);

        // Center the statsLabel
        StackPane statsPane = new StackPane(statsLabel);
        statsPane.setAlignment(Pos.CENTER);

        gameOverPane.getChildren().addAll(statsPane, buttonPane);
    }

    /**
     * Configures the game stats text area.
     *
     * @param statsTextArea The text area to display game stats.
     * @param loser         True if the player lost, false if the player won.
     * @param playerHitRate Player's hit rate as a percentage.
     * @param enemyHitRate  Enemy's hit rate as a percentage.
     */
    private void configureStatsTextArea(TextArea statsTextArea, boolean loser, String playerHitRate, String enemyHitRate) {
        // Removed "Game Over!" from gameSummaryLabel
        gameSummaryLabel.setText((loser ? "You lost!" : "You won!") +
                "\n\nSummary:\n\n" +
                "Your Hits: " + enemyBoard.getNumHits() + "\n" +
                "Your Misses: " + enemyBoard.getNumMisses() + "\n" +
                "Your Hit Rate: " + playerHitRate + "% \n" +
                "Your Max Hits in a Row: " + enemyBoard.getMaxHitsInARow() + "\n\n" +
                "Enemy hits: " + playerBoard.getNumHits() + "\n" +
                "Enemy Misses: " + playerBoard.getNumMisses() + "\n" +
                "Enemy Hit Rate: " + enemyHitRate + "%\n" +
                "Enemy Max Hits in a Row: " + playerBoard.getMaxHitsInARow());

        gameSummaryLabel.setFont(Font.font("Serif Regular", 14));
        gameSummaryLabel.setAlignment(Pos.CENTER);
    }

    /**
     * Creates a styled scene with a title and specified dimensions.
     *
     * @param gameOverPane The main game over pane.
     * @param title        The title of the scene.
     * @param width        The width of the scene.
     * @param height       The height of the scene.
     * @return The styled Scene.
     */
    private Scene createStyledScene(VBox gameOverPane, String title, int width, int height) {
        VBox styledLayout = new VBox(10);
        styledLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        styledLayout.getChildren().addAll(titleLabel, gameOverPane);

        Scene scene = new Scene(styledLayout, width, height);

        // Apply float animation to the titleLabel
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), titleLabel);
        translateTransition.setByY(5);
        translateTransition.setCycleCount(TranslateTransition.INDEFINITE);
        translateTransition.setAutoReverse(true);
        translateTransition.play();

        return scene;
    }

    /**
     * Configures the button pane with styled buttons and margins.
     *
     * @param buttonPane     The pane containing buttons.
     * @param playAgainButton The "Play Again" button.
     * @param quitButton      The "Quit" button.
     */
    private void configureButtonPane(HBox buttonPane, Button playAgainButton, Button quitButton) {
        // Set styles with fixed width and margin
        playAgainButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-min-width: 100;");
        quitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-min-width: 100;");

        playAgainButton.setText("Play Again");
        quitButton.setText("Quit");

        // Set event handlers
        playAgainButton.setOnAction(e -> handlePlayAgain());
        quitButton.setOnAction(e -> handleQuit());

        // Configure button pane with margin
        buttonPane.getChildren().addAll(playAgainButton, quitButton);
        buttonPane.setSpacing(50);
        buttonPane.setAlignment(Pos.CENTER);
        HBox.setMargin(playAgainButton, new Insets(20, 0, 0, 0));
        HBox.setMargin(quitButton, new Insets(20, 0, 0, 0));
    }

    /**
     * Handles the "Play Again" action.
     */
    private void handlePlayAgain() {
        if (online) {
            chatArea.appendText("Waiting for the opponent to respond to the rematch request...\n");
            client.sendPacket("rmch;" + username);
        } else {
            reset();
            mainMenu();
        }
    }

    /**
     * Handles the "Quit" action.
     */
    private void handleQuit() {
        stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
    }


    /**
     * Reacts to updates on the enemy and player boards.
     * @param newBoard The new state of the board.
     * @param source The source of the update. Should be passed as "this".
     * @param pos The position the update occurred on, if any.
     */
    public void updateBoard(char[][] newBoard, Board source, Point pos) {
        if(!gameReady && pos != null) {
            addShip(horizontal, currLen, pos.row, pos.col, ships);
        }
        if(source == playerBoard) { // Update the player's array, not the enemy's array.
            if(!gameReady) { // Checking for placement amounts left.
                shipLens.put(currLen, shipLens.get(currLen) - 1); // Will always be valid for the last placed ship
                placementText.setText("Ships Available to Place:\nLength 2: " + shipLens.get(2) + "\nLength 3: " + shipLens.get(3) +
                        "\nLength 4: " + shipLens.get(4) + "\nLength 5: " + shipLens.get(5));
                for(int i = 2; i <= 5; i++) {
                    if(shipLens.get(i) > 0) {
                        break;
                    }
                    if(i == 5) {
                        if(online) {
                            client.sendPacket("redy;" + username);
                        }
                        for(Rectangle[] cellArr : playerCells) { // Remove handlers from the cells and game scene
                            for (Rectangle cell : cellArr) {
                                cell.setOnMouseEntered(null);
                                cell.setOnMouseExited(null);
                            }
                        }
                        placementScene.setOnKeyPressed(null);
                        if(!online) { // Need to wait for start signal
                            playSetup();
                            gameReady = true;
                        }
                    }
                }
            }
            setPlayerBoard(newBoard, pos);
            if(playerBoard.gameOver()) {
                for(Rectangle[] cellArr : enemyCells) {
                    for (Rectangle cell : cellArr) {
                        cell.setOnMouseClicked(null);
                    }
                }
                Platform.runLater(()-> gameOver(true));
            }
        } else if (source == enemyBoard) { // Update the enemy array, not the player array.
            setEnemyBoard(newBoard, pos);
            if(enemyBoard.gameOver()) {
                for(Rectangle[] cellArr : enemyCells) {
                    for (Rectangle cell : cellArr) {
                        cell.setOnMouseClicked(null);
                    }
                }
                gameOver(false);
            }
        }
    }

    /**
     * Sets the player board gui element to a specific board array.
     * @param newBoard The board array to use for the new gui.
     * @param pos The position of any new shots. This will be null in any case where shots were not made.
     */
    private void setPlayerBoard(char[][] newBoard, Point pos) {
        for(int i = 0; i < playerCells.length; i++) {
            for(int j = 0; j < playerCells[i].length; j++) {
                if (newBoard[i][j] == 'S') {
                    Point point = new Point(i, j);
                    Ship ship = ships.get(point);
                    if(ship != null) {
                        chooseImageOnOrientation(playerCells, ship, point, noseHPattern, noseVPattern, bodyHPattern, bodyVPattern);
                    }
                } else {
                    chooseImage(newBoard, pos, i, j, ships, playerCells);
                }
            }
        }
    }

    /**
     * Chooses the image to use for an update to the board. Also handles setting lambdas for animations if needed.
     * @param newBoard The board after the latest change.
     * @param pos The position the change occurred on.
     * @param row The row to look at.
     * @param col The column to look at.
     * @param updateShips The mapping of ships for this board.
     * @param cells The cells to change.
     */
    private void chooseImage(char[][] newBoard, Point pos, int row, int col, HashMap<Point, Ship> updateShips, Rectangle[][] cells) {
        if (newBoard[row][col] == 'H') {
            if(row == pos.row && col == pos.col) {
                if(updateShips.get(pos).sunk()) {
                    Ship ship = updateShips.get(pos);
                    for(Point point : ship.positions) {
                        ExecutorService execSink = Executors.newSingleThreadExecutor();
                        chooseImageOnOrientation(cells, ship, point, noseHExplosion, noseVExplosion, bodyHExplosion, bodyVExplosion);
                        mediaPlayer.stop();
                        mediaPlayer.play();
                        execSink.submit(() -> {
                            try {
                                Thread.sleep(500);
                                chooseImageOnOrientation(cells, ship, point, noseHSink, noseVSink, bodyHSink, bodyVSink);
                                Thread.sleep(120);
                                chooseImageOnOrientation(cells, ship, point, noseHSunk, noseVSunk, bodyHSunk, bodyVSunk);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            execSink.shutdown();
                        });
                    }
                } else {
                    Point point = new Point(row, col);
                    Ship ship = updateShips.get(pos);
                    chooseImageOnOrientation(cells, ship, point, noseHExplosion, noseVExplosion, bodyHExplosion, bodyVExplosion);
                    mediaPlayer.stop();
                    mediaPlayer.play();
                    ExecutorService exec = Executors.newSingleThreadExecutor();
                    exec.submit(() -> {
                        try {
                            Thread.sleep(500);
                            cells[point.row][point.col].setFill(Color.RED);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        exec.shutdown();
                    });
                }
            }
        } else if (newBoard[row][col] == 'M') {
            if (pos.row == row && pos.col == col) {
                cells[row][col].setFill(splash);
                mediaPlayer2.stop();
                mediaPlayer2.play();
                
                ExecutorService exec = Executors.newSingleThreadExecutor();
                exec.submit(() -> {
                    try {
                        Thread.sleep(250);
                        cells[row][col].setFill(missPattern);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    exec.shutdown();
                });
            }
        } else {
            cells[row][col].setFill(waterPattern);
        }
    }

    /**
     * Chooses which image to use based on the orientation of a given ship.
     * @param cells The cells the ship exists on.
     * @param ship The ship to compare to.
     * @param point The coordinate of the update.
     * @param noseH The image to use for the horizontal nose.
     * @param noseV The image to use for the vertical nose.
     * @param bodyH The image to use for the horizontal body.
     * @param bodyV The image to use for the vertical body.
     */
    private void chooseImageOnOrientation(Rectangle[][] cells, Ship ship, Point point, ImagePattern noseH,
                                          ImagePattern noseV, ImagePattern bodyH, ImagePattern bodyV) {
        if(point.equals(ship.nose)) {
            if(ship.horizontal) {
                cells[point.row][point.col].setFill(noseH);
            } else {
                cells[point.row][point.col].setFill(noseV);
            }
        } else {
            if(ship.horizontal) {
                cells[point.row][point.col].setFill(bodyH);
            } else {
                cells[point.row][point.col].setFill(bodyV);
            }
        }
    }

    /**
     * Sets the player board gui element to a specific board array.
     * @param newBoard The board array to use for the new gui.
     * @param pos The position of any new shots. This will be null in any case where shots were not made.
     */
    private void setEnemyBoard(char[][] newBoard, Point pos) {
        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard[i].length; j++ ) {
                chooseImage(newBoard, pos, i, j, enemyShips, enemyCells);
            }
        }
    }

    /**
     * Handles messages sent by the game server.
     * @param message The message to handle.
     */
    public void handle(String message) {
        if(client == null) {
            return;
        }
        switch (message.substring(0, 5)) {
            case "info;" -> {
                switch (message.substring(5)) {
                    case "Name registered" -> {
                        this.username = userText.getText();
                        if (host) {
                            client.sendPacket("host;" + username + ";");
                        } else {
                            client.sendPacket("join;" + username + ";");
                        }

                        online = true; // Once we are acknowledged by the server, switch to online state

                        Platform.runLater(this::createChatWindow);
                    }
                    case "Name used" -> Platform.runLater(() -> { // Needed to prevent illegal state exception
                        prompt.setText("Name already in use!");
                    });
                    case "Host received", "No hosts found" -> Platform.runLater(() -> {
                        host = true;
                        shotReady = true;
                        stage.setTitle("Battleship - Host");
                        hostScene();
                    });
                    case "server offline;" -> Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("The server is offline! Returning to the main menu.");
                        client = null;
                        online = false;
                        alert.setOnCloseRequest((event) -> stage.setScene(menuScene));
                        alert.showAndWait();
                    });
                }
            } // Break this case of the switch statement
            case "list;" -> Platform.runLater(() -> {
                stage.setTitle("Battleship - Client");
                shotReady = false;
                joinScene(message);
            });
            case "join;" -> Platform.runLater(() -> {
                String[] info = message.split(";");
                chatArea.appendText(info[1] + " joined " + info[2] + "'s game.\n");
                if (shotReady) {
                    chatArea.appendText("You have the first shot.\n");
                } else {
                    chatArea.appendText("Your opponent has the first shot.\n");
                }
                placementScene();
            });
            case "rmch;" -> Platform.runLater(() -> {
                String[] info = message.split(";");
                chatArea.appendText(info[1] + " and " + info[2] + " started a rematch!\n");
                if (shotReady) {
                    chatArea.appendText("You have the first shot.\n");
                } else {
                    chatArea.appendText("Your opponent has the first shot.\n");
                }
                reset();
                placementScene();
            });
            case "strt;" -> {
                client.sendPacket("rqst;" + username + ";board");
                Platform.runLater(() -> {
                    playSetup();
                    gameReady = true;
                });
            }
            case "rqst;" -> client.sendPacket("rspn;" + username + ";" + serializeBoard());
            case "rspn;" -> Platform.runLater(() -> parseResponse(message));
            case "move;" -> Platform.runLater(() -> {
                String[] moveInfo = message.split(";");
                int row = Integer.parseInt(moveInfo[2]);
                int col = Integer.parseInt(moveInfo[3]);
                playerBoard.tryShot(row, col);
                shotReady = true;
            });
            case "chat;" -> Platform.runLater(() -> {
                String[] info = message.split(";");
                String name = info[1];
                String chat = info[2];
                chatArea.appendText(name + ": " + chat + "\n");
            });
            case "dsct;" -> Platform.runLater(() -> {
                String[] info = message.split(";");
                chatArea.appendText(info[1] + " left the game.\n");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                if ((!playerBoard.gameOver() && !enemyBoard.gameOver()) || !gameReady) { // Game is still running
                    alert.setContentText("Your opponent has disconnected. Switching opposing player to AI.");
                } else {
                    alert.setContentText("Your opponent has disconnected. Returning to the main menu.");
                }
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.setOnCloseRequest((event) -> handleDisconnect());
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    handleDisconnect();
                }
            });
        }
    }

    /**
     * Handles a disconnect based on the state of the game. Used for the disconnect alerts.
     */
    private void handleDisconnect() {
        if(client == null) {
            return;
        }
        client.disconnect(username);
        client = null;
        chatWindow.close();
        online = false;
        if(!playerBoard.gameOver() && !enemyBoard.gameOver()) { // Game is still running
            updateBoard(playerBoard.getBoard(), playerBoard, null);
            computerPlayer = new ComputerPlayer();
            computerPlayer.setStrategy(new RandomAI());
            if(gameReady) {
                playSetup();
            }
            if(!shotReady && gameReady) {
                getNextCPUMove();
            }
        } else {
            reset();
            mainMenu();
        }
    }

    /**
     * Creates a string representation of this player's board to send to the other player.
     * @return A string representation of the board.
     */
    private String serializeBoard() {
        StringBuilder message = new StringBuilder();
        HashSet<Ship> used = new HashSet<>();
        for(Ship ship : ships.values()) {
            if(!used.contains(ship)) {
                message.append(ship.nose).append(",");
                message.append(ship.positions.size()).append(",");
                message.append(ship.horizontal).append(",");
                message.append(";");
                used.add(ship);
            }
        }
        return message.toString();
    }

    /**
     * Reads in the text representation from the request for the board
     * @param response The response from the other player to read in.
     */
    private void parseResponse(String response) {
        String[] tokens = response.split(";");
        for(int i = 2; i < tokens.length; i++) {
            String[] shipInfo = tokens[i].split(",");
            Point nose = new Point(Integer.parseInt(shipInfo[0].split(" ")[0]), Integer.parseInt(shipInfo[0].split(" ")[1]));
            int len = Integer.parseInt(shipInfo[1]);
            boolean horiz = Boolean.parseBoolean(shipInfo[2]);
            enemyBoard.placeShip(horiz, len, nose.row, nose.col);
            addShip(horiz, len, nose.row, nose.col, enemyShips);
        }
        gameReady = true;
    }

    /**
     * Sets up the game for a new game. Used on play again and on the first run.
     */
    private void reset() {
        gameReady = false;
        computerPlayer = new ComputerPlayer();
        shipLens = new HashMap<>();
        shipLens.put(5, 1);
        shipLens.put(4, 1);
        shipLens.put(3, 2);
        shipLens.put(2, 1);
        if(!online) {
            shotReady = true;
        }
        currLen = 5;
        horizontal = true;
        currRow = -1;
        currCol = -1;

        playerBoard = new Board(this);
        enemyBoard = new Board(this);

        ships = new HashMap<>();
        enemyShips = new HashMap<>();
    }

    /**
     * Represents a ship on the board. Used in a hashmap to quickly get the needed info about a position, if it has a
     * ship.
     */
    private static class Ship {
        Point nose;
        ArrayList<Point> positions;
        Board board;
        boolean horizontal;

        /**
         * Creates a new ship with the given information.
         * @param nose The location of this ship's nose.
         * @param positions The positions this ship occupies.
         * @param sourceBoard The board this ship is located on.
         * @param horizontal The orientation of this ship. True means horizontal, false means vertical.
         */
        public Ship(Point nose, ArrayList<Point> positions, Board sourceBoard, boolean horizontal) {
            this.nose = nose;
            this.positions = positions;
            this.board = sourceBoard;
            this.horizontal = horizontal;
        }

        /**
         * Returns if every position of this ship has been hit.
         * @return True if all locations of this ship have been hit, false otherwise.
         */
        public boolean sunk() {
            char[][] boardVal = board.getBoard();
            for(Point point : positions) {
                if(boardVal[point.row][point.col] == 'S') {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof Ship oShip)) {
                return false;
            }
            return nose.equals(oShip.nose) && positions.equals(oShip.positions) && board == oShip.board && horizontal == oShip.horizontal;
        }

        @Override
        public int hashCode() {
            return positions.hashCode();
        }
    }
}