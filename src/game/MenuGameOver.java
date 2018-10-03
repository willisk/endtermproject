
package game;

import java.awt.*;

public class MenuGameOver extends Menu{
    

    public MenuGameOver(Game game) {
        super(game);
        
        addCinematicText("GAME OVER");
        
    }
    
    public void onCinematicEnd(){
        inPopMenu = true;
        popupText = "    Restart?";
    }
    
    public void popupYes(){
        game.loadLastGame();
    }
    
    public void popupNo(){
        game.openMenu(game.menuMain);
    }
    
    
    
}
