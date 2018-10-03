
package game;


public class MenuDifficulty extends Menu{
    
    private String selection;
    
    public MenuDifficulty(Game game) {
        super(game);
        
        menuID = game.menuDifficulty;
        
        selection = "A";
        
        addSpacing();
        addText("Select Difficulty:");
        addText(" Easy (Cave)");
        addText(" Medium (Snow)");
        addText(" Hard (Woods)");
    }
    
    public void select(String slot){
        selection = slot;
    }
    
    public void notifyClicked( int ID ){
        if(ID==2){
            msg(".. loading new Game", 500);
            game.createNewGame(selection,"Easy");
        }
        else if(ID==3){
            msg(".. loading new Game", 500);
            game.createNewGame(selection,"Medium");
        }
        else if(ID==4){
            msg(".. loading new Game", 500);
            game.createNewGame(selection,"Hard");
        }
    }
    
    
    
}
