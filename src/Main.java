import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;

public class Main {
    static Main main;
    JFrame mainFrame;
    JPanel middlePanel, bottomPanel, scorePanel;
    JLabel turnLabel, blackScore, whiteScore;
    Game game;
    ServerBrowser.ServerSender ss;

    public static void main(String[]args){
        main = new Main();
        main.initiate();
    }

    //Runs only once when program executes
    private void initiate() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Othello");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        openStartMenu();
        mainFrame.setVisible(true);
    }

    //Creating the main menu
    protected void openStartMenu() {
        GridLayout gridLayout = new GridLayout(3,1);
        JButton createGameButton = new JButton();
        createGameButton.setText("Create new game");
        createGameButton.addActionListener(new createGameListener());
        JButton connectPrivateGame = new JButton();
        connectPrivateGame.setText("Connect to private game");
        connectPrivateGame.addActionListener(new connectPrivateListener());
        JButton serverBrowse = new JButton();
        serverBrowse.setText("Browse servers");
        serverBrowse.addActionListener(new browseServerListener());
        JButton quitButton = new JButton();
        quitButton.setText("Exit");
        quitButton.addActionListener(new terminateProgramListener());
        middlePanel = new JPanel(gridLayout);
        middlePanel.add(createGameButton);
        middlePanel.add(connectPrivateGame);
        middlePanel.add(serverBrowse);
        mainFrame.add(middlePanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.add(quitButton);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        mainFrame.setSize(300, 300);
        mainFrame.setLocation(400,400);
        mainFrame.revalidate();
    }

    //Updates the score above the board for ongoing game, is also responsible for checking if the game is finished
    protected void updateScore(Board b){
        int white = 0;
        int black = 0;
        for(Tile[] row : b.tiles){
            for(Tile tile : row){
                if(tile.getColor().equals(" B ")){
                    black += 1;
                }else if(tile.getColor().equals(" W ")){
                    white += 1;
                }
            }
        }
        whiteScore.setText(String.valueOf(white));
        blackScore.setText(String.valueOf(black));

        //Shifts the arrow pointing at the players whose turn it is
        turnLabel.setText(turnLabel.getText().equals("\u21D0")? "\u21D2" : "\u21D0");
        if(white + black == 64){
            if(white == black){
                createGameOverPopUp("It's a tie", b.getGame());
            }else{
                createGameOverPopUp((white > black) ? "White is the winner" : "Black is the winner", b.getGame());
            }
        }
    }

    //Creates popup after the game has ended, displaying winner and sending the users back to the main menu
    protected void createGameOverPopUp(String s, Game g) {
        g.ended = true;
        JOptionPane.showMessageDialog(mainFrame, s,"Game over", JOptionPane.INFORMATION_MESSAGE);
        g.endGame();
        mainFrame.remove(scorePanel);
        mainFrame.remove(middlePanel);
        main.openStartMenu();
    }

    //Sets up the game window and the score above the board
    protected void setupGameWindow(Game game) {
        mainFrame.getContentPane().removeAll();
        middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(game);
        GridLayout layout = new GridLayout(2,3);
        scorePanel = new JPanel(layout);
        JLabel b = new JLabel("Black");
        b.setHorizontalAlignment(JLabel.CENTER);
        JLabel w = new JLabel("White");
        w.setHorizontalAlignment(JLabel.CENTER);
        turnLabel = new JLabel("\u231B");
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        blackScore = new JLabel("2");
        blackScore.setHorizontalAlignment(JLabel.CENTER);
        whiteScore = new JLabel("2");
        whiteScore.setHorizontalAlignment(JLabel.CENTER);
        scorePanel.add(b);
        scorePanel.add(turnLabel);
        scorePanel.add(w);
        scorePanel.add(blackScore);
        scorePanel.add(new JLabel(""));
        scorePanel.add(whiteScore);

        mainFrame.add(BorderLayout.NORTH,scorePanel);
        mainFrame.add(BorderLayout.CENTER, middlePanel);
        mainFrame.pack();
        mainFrame.setLocation(600, 250);
        mainFrame.revalidate();
    }

    //Create game-button handler
    private class createGameListener implements ActionListener {
         @Override
        public void actionPerformed(ActionEvent e) {
            Main.main.createNewGameMenu();
        }
    }

    //Sets up the option menu for creating a game
    private void createNewGameMenu() {
        JRadioButton jr1, jr2;
        ButtonGroup bGroup;
        mainFrame.remove(middlePanel);
        mainFrame.remove(bottomPanel);
        middlePanel = new JPanel(new GridLayout(3,3));

        //Color chooser
        middlePanel.add(new JLabel("Color:"));
        jr1 = new JRadioButton();
        jr2 = new JRadioButton();
        bGroup = new ButtonGroup();
        jr1.setText("");
        jr1.setBackground(Color.BLACK);
        jr2.setText("");
        jr2.setBackground(Color.WHITE);
        bGroup.add(jr1);
        bGroup.add(jr2);
        jr1.setSelected(true);
        middlePanel.add(jr1);
        middlePanel.add(jr2);

        //Type of game chooser
        JRadioButton jr3, jr4;
        ButtonGroup bGroup2;
        middlePanel.add(new JLabel("Type of game:"));
        jr3 = new JRadioButton();
        jr4 = new JRadioButton();
        bGroup2 = new ButtonGroup();
        jr3.setText("Public");
        jr4.setText("Private");
        bGroup2.add(jr3);
        bGroup2.add(jr4);
        jr3.setSelected(true);
        middlePanel.add(jr3);
        middlePanel.add(jr4);

        //Name of the game
        middlePanel.add(new JLabel("Name of game:"));
        JTextField nameField = new JTextField();
        nameField.setText("My new game" + new Random().nextInt(1000));
        middlePanel.add(nameField);
        middlePanel.add(new JLabel("Accepted character: a-Z, 0-9"));

        //Buttons at the bottom of window
        bottomPanel = new JPanel();
        JButton backButton = new JButton();
        backButton.setText("Back");
        backButton.addActionListener(new BackToMenu());
        bottomPanel.add(backButton);
        JButton b1 = new JButton();
        b1.setText("Create game");
        b1.addActionListener(new initiateNewGameHandler(bGroup, jr3, nameField));
        bottomPanel.add(b1);

        //Scorepanel
        scorePanel = new JPanel();
        scorePanel.add(new JLabel("New game"));

        mainFrame.add(scorePanel, BorderLayout.NORTH);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        mainFrame.add(middlePanel, BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.revalidate();

    }

    //Handler of the back-button
    protected class BackToMenu implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainFrame.remove(scorePanel);
            mainFrame.remove(middlePanel);
            mainFrame.remove(bottomPanel);
            main.openStartMenu();
        }
    }

    //Create game-button handler
    private class initiateNewGameHandler implements ActionListener {
        ButtonGroup bg;
        JRadioButton radio;
        JTextField nameField;
        public initiateNewGameHandler(ButtonGroup bGroup, JRadioButton radioButton, JTextField jtf) {
            this.bg = bGroup;
            this.radio = radioButton;
            this.nameField = jtf;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String gameName = nameField.getText().replaceAll("[^a-zA-Z0-9 ]","");
            int minimumLength = 4;
            if(gameName.length() < minimumLength){
                JOptionPane.showMessageDialog(mainFrame, "The name of the game must\n be atleast " + minimumLength + " characters long","Name length error", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            //Getting the color chosen by the creator of the game
            for(Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();){
                AbstractButton b = buttons.nextElement();
                if(b.isSelected()){
                    initiateNewGame(b.getBackground(), radio.isSelected(), gameName);
                }
            }
        }
    }

    //Initiating the game for the creator of the game
    private void initiateNewGame(Color c, boolean bool, String str) {
        mainFrame.getContentPane().removeAll();
        Player p1 = new Player(c);
        Player p2 = new Player((c.equals(Color.BLACK))? Color.WHITE : Color.BLACK);
        game = new Game(p1,p2, bool);
        setupGameWindow(game);

        if(bool){
            try{
                ss = new ServerBrowser.ServerSender(InetAddress.getLocalHost().getHostAddress(), str);
            }catch (UnknownHostException e) {
                System.out.println("COULD NOT CREATE PUBLIC GAME");
            }
        }
    }

    //Connect to private game-button handler
    private class connectPrivateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String gameIP = JOptionPane.showInputDialog(Main.main.mainFrame, "IP-adress", "127.0.0.1");
            if(gameIP != null){
                new Connector(gameIP);
            }
        }
    }

    //Browse servers-button handler
    private class browseServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ServerBrowser sb = new ServerBrowser();
        }
    }

    //Exit-button handler
    private class terminateProgramListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Main.main.mainFrame.dispose();
        }
    }
}
