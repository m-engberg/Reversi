import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

//The tiles that make up the Board
public class Tile extends JPanel implements MouseListener {
    private Player occupiedBy;
    private Marker marker;
    private Color backgroundColor;
    private Color borderColor;
    private final int x,y;
    private Board b;

    protected Tile(int i, int j, Board bb){
        this.b = bb;
        this.backgroundColor = Color.GREEN;
        setBackground(backgroundColor);
        borderColor = Color.DARK_GRAY;
        Border border = BorderFactory.createLineBorder(borderColor);
        setBorder(border);
        this.x = i;
        this.y = j;
        addMouseListener(this);
        //Every tile is occupied by an invisible and unclaimed marker
        this.marker = new Marker(backgroundColor);
        add(marker);
    }

    protected int getXCoordinate(){
        return x;
    }
    protected int getYCoordinate(){
        return y;
    }

    protected Player getOccupiedBy(){
        return occupiedBy;
    }

    //Flipping a already claimed Marker, or used when setting up the first 4 markers on a new game
    protected void occupyTile(Player p){
        this.occupiedBy = p;
        this.marker.claimMarker(p);
    }

    //Used when a player places a Marker on an unclaimed Tile
    public void placeOnTile(Player player) {
        this.occupiedBy = player;
        this.marker.claimMarker(player);
        b.getGame().startTraverseAndFlip(this, player);
        Main.main.updateScore(b);
    }

    protected boolean isOccupied(){
        return this.occupiedBy != null;
    }

    protected String getColor(){
        return marker.getColor();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(this.isOccupied()){
            //Ignores clicks on already claimed Tiles
            return;
        }
        b.getGame().handleClickedTile(this);
    }


    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
