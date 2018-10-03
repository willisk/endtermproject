
package game;


public class MenuControls extends Menu{
    
    public MenuControls(Game game){
        super(game);
        
        menuID = game.menuControls;
        
        addText(" - Controls - ");
        addSpacing();
        addText(" W,A,S,D - Move ");
        addText(" J - Attack ");
        addSpacing();
        addButton(".return",game.menuMain);
    }
    
}
