
package game;

import java.awt.*;

public class MenuGameFinished extends Menu{
    

    public MenuGameFinished(Game game) {
        super(game);
        
        addCinematicText("GAME FINISHED");
        
    }
    
    public void onCinematicEnd(){
        System.out.println("cin end");
        addSpacing();
        addSpacing();
        addSpacing();
        addButton("return to Main Menu",game.menuMain);
    }
    
}
