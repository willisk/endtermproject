
package game;

//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;
import static game.Object.round;
import java.awt.Graphics2D;
import java.awt.Image;

public class Trap extends Object{

    private static int damage;
    private static Image image;
    
    public Trap(Game game, int x, int y) {
        super(game, x, y, 1, game.TypeTrap);
    }

    public static void loadImages(){
        image = Animation.BufferImageResized("trap",Game.scale,Game.scale);
    }
    
    
    public static void setDamage( int newDamage ){
        damage = newDamage;
    }
    
    public int getDamage(){
        return damage;
    }
    
    @Override
    void paint(Graphics2D g2d, float offsetx, float offsety) {
        g2d.drawImage(image,
                (int)((getX()+Game.scrollboarder+offsetx)*Game.scale),
                (int)((getY()+Game.scrollboarder+offsety)*Game.scale),null);
    }

    @Override
    void delete() {
        
    }
    
    
    
}
