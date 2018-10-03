
package game;

public class MenuOptions extends Menu{
    
    public MenuOptions(Game game){
        super(game);
        
        menuID = game.menuOptions;
        
        addText(" - Options - ");
        addSpacing();
        addText("Switch to Lanterna View");
        addSpacing();
        addButton(".return",game.menuMain);
    }
    
    public void notifyClicked( int ID ){
        if(ID==2){
            //game.LanternaToggle();
        }
    }
    
}
