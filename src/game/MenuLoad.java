
package game;

public class MenuLoad extends Menu{
    
    private String selection;
    
    public MenuLoad(Game game){
        super(game);
        
        selection = "A";
        
        menuID = game.menuLoad;
        
        addText(" - Load Game? - ");
        addSpacing();
        addText("Select Slot:");
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
            popupText = "Are you sure you want to load Slot?"+(game.gameloaded?"/n(unsaved games will be lost)":"");
        else
            popupText = "Create a new Game?"+(game.gameloaded?"/n(unsaved games will be lost)":"");
    }
    
    public void popupYes(){
        if( game.slotExists(selection) )
            game.loadGame("Slot"+selection);
        else{
            game.openMenu(game.menuDifficulty);
            game.menu[game.menuDifficulty].select(selection);
        }
    }
    
}
