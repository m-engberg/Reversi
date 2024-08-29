import java.awt.*;

public class Player {
    private final Color color;

    protected Player(Color c){
        this.color = c;
    }
    protected Color getColor(){
        return this.color;
    }
    protected String getFarg(){return (color.equals(Color.BLACK))? "B" : "W";}
}
