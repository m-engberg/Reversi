import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Game extends JPanel {
    private Board board;
    protected Player thisPlayer;
    protected Player otherPlayer;
    private GameServer gs;
    private Connector c;
    boolean thisPlayersTurn, hasStarted, isPublic, ended;

    //Constructor for the player creating the game
    public Game(Player player1, Player player2, boolean isPublic){
        this.thisPlayer = player1;
        this.otherPlayer = player2;
        this.isPublic = isPublic;
        this.gs = new GameServer(this);
        this.board = new Board(thisPlayer, otherPlayer, this);
        add(board);
    }

    //Constructor for the player joining another game
    public Game(Connector connector){
        this.c = connector;
    }

    protected void nextPlayer(){
        thisPlayersTurn = !thisPlayersTurn;
    }

    //The start of the recursive method of finding which markers to flip after a players turn
    public void startTraverseAndFlip(Tile tile, Player p) {
        for(int deltaX =-1; deltaX <=1; deltaX +=1){
            for(int deltaY =-1; deltaY <=1; deltaY +=1){
                if(!(deltaY == 0 && deltaX == 0)){
                    traverse(tile, deltaX, deltaY, p);
                }
            }
        }
    }

    //Recursive function that finds which markers to flip
    private boolean traverse(Tile tile, int deltaX, int deltaY, Player p) {
        int x = tile.getXCoordinate();
        int y = tile.getYCoordinate();
        if(x + deltaX < 0 || x + deltaX > 7 || y + deltaY < 0 || y + deltaY > 7){
            //Outside of board
            return false;
        }
        Tile nextTile = board.tiles[x+deltaX][y+deltaY];
        if(!nextTile.isOccupied()){
            //Next tile doesn't contain a marker
            return false;
        }else if(!nextTile.getOccupiedBy().getColor().equals(p.getColor())){
            //Next tile is occupied by the other player
            //Recursive call below to see the status of the next tile
            if(traverse(nextTile, deltaX, deltaY, p)){
                nextTile.occupyTile(p);
                return true;
            }
        }else if(nextTile.getOccupiedBy().getColor().equals(p.getColor())){
            //Next tile is controlled by the same player that placed the marker
            //All of the markers between this and the placed marker should flip,
            //returns true so the other markers can flip
            return true;
        }
        return false;
    }

    //Sets up the game after the connection request is approved
    protected void setPlayers(Color c1, Color c2){
        this.thisPlayer = new Player(c1);
        this.otherPlayer = new Player(c2);
        this.board = new Board(thisPlayer, otherPlayer,c, this);
        if(thisPlayer.getFarg().equals("B")){
            this.thisPlayersTurn = true;
        }
        this.hasStarted = true;
        add(board);
        Main.main.mainFrame.pack();
    }

    //Handles clicks registered by Tiles
    protected void handleClickedTile(Tile tile) {
        if(!hasStarted){
            JOptionPane.showMessageDialog(Main.main.mainFrame, "Game has not started yet","Alert", JOptionPane.INFORMATION_MESSAGE);
            return;
        } else if(!thisPlayersTurn){
            JOptionPane.showMessageDialog(Main.main.mainFrame, "Not yet your turn","Alert", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //Send info to the other player, depending on connection type
        if(this.c != null){
            c.sendMessage(Connector.MESSAGE.PlaceTile, tile.getXCoordinate(), tile.getYCoordinate());
        }else{
            gs.sendPlacedTileToPlayer(tile);
        }
        tile.placeOnTile(thisPlayer);
        board.repaint();
        nextPlayer();
    }

    protected Board getBoard(){
        return board;
    }

    //When two players are connected, start the game
    private void startGame() {
        this.hasStarted = true;
        Main.main.turnLabel.setText("\u21D0");
        this.thisPlayersTurn = thisPlayer.getFarg().equals("B");
    }

    //Ends game after all markers have been placed
    public void endGame() {
        if(this.c != null){
            this.c.killMe();
        }else{
            this.gs.killGameServer();
        }
    }

    //Used by the player setting up the game, to send and receive message to other player
    protected class GameServer implements Runnable{
        private ServerSocket ss;
        private Socket socketToPlayer;
        private PlayerHandler ph;
        private int port = 2000;
        private Game game;

        private Thread thread = new Thread(this);
        public GameServer(Game game){
            this.game = game;
            try {
                this.ss = new ServerSocket(port);
            } catch (IOException var2) {
                System.out.println("FAILED: COULD NOT LISTEN ON PORT: " + port);
                System.exit(1);
            }
            this.thread.start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    socketToPlayer = this.ss.accept();
                    this.ph = new PlayerHandler(socketToPlayer, this, this.game);
                } catch (IOException var3) {
                    System.out.println("FAILED: ACCEPTING CLIENT");
                    this.thread.stop();
                }
                //this.addClient(var1);
            }
        }

        //Sends info to other player about placed tiles
        public void sendPlacedTileToPlayer(Tile tile) {
            try{
                PrintWriter pw =new PrintWriter(socketToPlayer.getOutputStream(),true);
                pw.println("1," + tile.getXCoordinate() + "," + tile.getYCoordinate());
            } catch (IOException e) {
                System.out.println("FAILED: SEND TO OTHER PLAYER");
            }
        }

        //Replies to a connection request
        public void sendReplyConnectionRequest(String s) {
            try{
                PrintWriter pw =new PrintWriter(socketToPlayer.getOutputStream(),true);
                pw.println(s);
            } catch (IOException e) {
                System.out.println("FAILED: SEND TO OTHER PLAYER");
            }
        }

        //Terminates connection
        protected void killGameServer(){
            try{
                this.ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class PlayerHandler extends Thread{
        private GameServer gs;
        private final Socket socket;
        private Game game;

        public PlayerHandler(Socket s, GameServer gameServer, Game game) {
            this.socket = s;
            this.gs = gameServer;
            this.start();
            this.game = game;
        }
        @Override
        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException var4) {
                //fix?
                System.out.println("FAILED: GET INPUTSTREAM FROM CLIENT SOCKET");
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                String inString;
                while((inString = br.readLine()) != null){
                    String[] message = inString.split(",");
                    if(message[0].equals("0")){
                        if(!hasStarted){
                            gs.sendReplyConnectionRequest("0,0," + (otherPlayer.getFarg()));
                            startGame();
                            if(isPublic){
                                String str = "DELETE FROM othelloServers WHERE othelloServers.ip = '" + InetAddress.getLocalHost().getHostAddress() + "'";
                                Main.main.ss.sendToServer(str);
                            }
                        }else{
                            gs.sendReplyConnectionRequest("0,1," + (otherPlayer.getFarg()));
                        }
                    }else if(message[0].equals("1")){
                        int x = Integer.parseInt(message[1]);
                        int y = Integer.parseInt(message[2]);
                        board.tiles[x][y].placeOnTile(otherPlayer);
                        nextPlayer();
                    }
                    board.repaint();
                }
                br.close();
                this.socket.close();
                if(!game.ended){
                    Main.main.createGameOverPopUp("Opponent disconnected", this.game);
                }
            } catch (IOException var5) {
                if(!game.ended){
                    Main.main.createGameOverPopUp("Opponent disconnected", this.game);
                }
            }
        }
    }
}
