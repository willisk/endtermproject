
package game;

public class MenuNewGame extends Menu{

    private String selection;
    
    public MenuNewGame(Game game) {
        super(game);
        
        selection = "A";
        
        menuID = game.menuNewGame;
        
        addText(" - Create a new Game? - ");
        addSpacing();
        addText("Select Save-Slot:");
        addPopButton("    [A]  "+game.slotStat("A"),"");
        addPopButton("    [B]  "+game.slotStat("B"),"");
        addPopButton("    [C]  "+game.slotStat("C"),"");
        addSpacing();
        addButton(".return",game.menuMain);
    }
    
    public void notifyClicked( int ID ){
        if(ID==3)
            select("A");
        else if(ID==4)
            select("B");
        else if(ID==5)
            select("C");
    }
    
    public void select(String slot){
        selection = slot;
        if( game.slotExists(slot) )
            popupText = "Are you sure you want to overwrite/n existing Savestate?";
        else
            popupText = "Create a new Game?";
    }
    
    public void popupYes(){
        game.openMenu(game.menuDifficulty);
        game.menu[game.menuDifficulty].select(selection);
    }
    
}
