
package game;

//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;
import static game.Object.round;
import java.awt.Image;
import java.awt.Graphics2D;

public class Key extends Object{
    
    private static Image image;
    
    public Key(Game game,int x, int y){
        super(game,x,y,1,game.TypeKey);
    }
    
    public static void loadImages(){
        image = Animation.BufferImageResized("key",Game.scale,Game.scale);
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
