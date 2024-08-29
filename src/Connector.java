import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

//Used by the player connecting to another players game
public class Connector implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;
    private Game game;
    public enum MESSAGE {PlaceTile, Chat, Connect}

    public Connector(String server){
        try{
            this.socket = new Socket(server, 2000);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"), true);
            Thread thread = new Thread(this);
            this.game = new Game(this);
            Main.main.setupGameWindow(game);
            thread.start();
            sendMessage(MESSAGE.Connect, 0,0);
        } catch (ConnectException | UnknownHostException e){
            JOptionPane.showMessageDialog(Main.main.mainFrame, "Couldn't connect to given IP","Error", JOptionPane.INFORMATION_MESSAGE);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Sends messages to the other player(the creator of the game)
    public void sendMessage(MESSAGE mess, int x, int y){
        if(mess == MESSAGE.PlaceTile){
            this.out.println("1," + x + "," + y);
        }else if(mess == MESSAGE.Connect){
            this.out.println("0");
        }
    }

    @Override
    public void run() {
        while(running){
            try{
                String[] message = this.in.readLine().split(",");
                if(message[0].equals("0")){
                    //Reply on a connection request
                    handleConnectionResponse(message);
                }else if(message[0].equals("1")){
                    //Information about the other players turn
                    handleOtherPlayerPlacement(message);
                }
                //Had plans to also include Emotes or Chat, it could have been added in the if-statements above
            } catch (SocketException e){
                Main.main.createGameOverPopUp("Opponent disconnected", this.game);
                this.running = false;
                this.killMe();
            } catch (NullPointerException e){
                this.killMe();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Handles the other players placement, and makes sure to update board
    private void handleOtherPlayerPlacement(String[] message) {
        int x = Integer.parseInt(message[1]);
        int y = Integer.parseInt(message[2]);
        game.getBoard().tiles[x][y].placeOnTile(game.otherPlayer);
        game.getBoard().repaint();
        game.nextPlayer();
    }

    //Handles the response of the connection request
    private void handleConnectionResponse(String[] message) {
        if(message[1].equals("1")){
            killMe();
        }else if(message[1].equals("0")){
            Color c = (message[2].equals("B")) ? Color.BLACK : Color.WHITE;
            Color c2 = (c.equals(Color.BLACK)) ? Color.WHITE : Color.BLACK;
            game.setPlayers(c,c2);
            Main.main.turnLabel.setText("\u21D0");
        }
    }

    //Kills this thread
    protected void killMe() {
        this.running = false;
        try {
            this.out.close();
            this.in.close();
            this.socket.close();
        } catch (IOException var2) {
            System.out.println("IOException generated: " + var2);
        } catch (NullPointerException e){
        }
        Main.main.mainFrame.remove(Main.main.scorePanel);
        Main.main.mainFrame.remove(Main.main.middlePanel);
        Main.main.openStartMenu();
    }
}
