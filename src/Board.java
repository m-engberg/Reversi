import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {
    public Tile[][] tiles;
    private Player[] players;
    private Connector c;
    private final Game game;

    public Board(Player player1, Player player2, Connector connector, Game g){
        this(player1,player2, g);
        c = connector;
    }

    public Board(Player player1, Player player2, Game g){
        setBackground(Color.BLACK);
        //Makes sure that the black player is added first in the Array
        if(player1.getFarg().equals("B")){
            players = new Player[]{player1, player2};
        }else{
            players = new Player[]{player2, player1};
        }
        this.game = g;

        //Creating of all of the tiles
        this.tiles = new Tile[8][8];
        this.setLayout(new GridLayout(8,8));
        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                Tile tile = new Tile(i, j, this);
                tiles[i][j] = tile;
                add(tile);
            }
        }

        //Setting up the start position
        tiles[3][3].occupyTile(players[0]);
        tiles[4][4].occupyTile(players[0]);
        tiles[3][4].occupyTile(players[1]);
        tiles[4][3].occupyTile(players[1]);

    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
    }

    protected Game getGame(){
        return game;
    }

}



