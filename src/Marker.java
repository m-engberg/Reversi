import javax.swing.*;
import java.awt.*;

//Marker that is placed on Tiles
public class Marker extends JPanel {
    private Color color, backgroundColor;

    //Markers is set up as colorless
    protected Marker(Color bg){
        this.backgroundColor = bg;
    }

    //Returns color of marker, or blank if unclaimed
    protected String getColor(){
        if(this.color == null){
            return " _ ";
        }
        return this.color == Color.WHITE ? " W " : " B ";
    }
    @Override
    public void paintComponent(Graphics g){
        if(color == null){
            return;
        }
        super.paintComponent(g);
        setBackground(backgroundColor);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(this.color);
        g2d.fillOval(0,0,10,10);
    }

    //Claim uncolored, or flip already claimed marker
    public void claimMarker(Player p) {
        this.color = p.getColor();
    }
}
