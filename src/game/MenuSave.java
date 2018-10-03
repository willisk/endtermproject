
package game;


public class MenuSave extends Menu{
    
    private String selection;
    
    public MenuSave(Game game){
        super(game);
        
        selection = "A";
        
        menuID = game.menuSave;
        
        addText(" - Save Game? - ");
        addSpacing();
        addText("Select Slot to save Game:");
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
            popupText = "Are you sure you want to save Game to Slot?/n(this will overwrite the existing savefile)";
        else
            popupText = "Save Game to Slot?";
    }
    
    public void popupYes(){
        
        game.saveGame("Slot"+selection);
        msg(".. Saving Game to Slot "+selection/*+", do not turn off device"*/, 100);
        closePopMenu();
    }
}
