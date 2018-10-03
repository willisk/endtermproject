
package game;

public class MenuMain extends Menu{

    public MenuMain(Game game) {
        super(game);
        
        menuID = game.menuMain;
        
        if(game.gameloaded){
            addText(" - Main Menu - ");
            addSpacing();
        }
        else{
            addSpacing();
            addText("   New Game ");
        }
        addText("Continue");
        addButton("Controls",game.menuControls);
        addButton("Load Game",game.menuLoad);
        addText("Save Game");
        addPopButton("Quit","Do you really want to quit?");
        
        hint = "(use mouse to navigate)";
        
    }
    
    public void popupYes(){
        game.quit();
    }
    
    public void notifyClicked( int ID ){
        if(ID==1 && !game.gameloaded){   //"New Game"
            game.openMenu(game.menuNewGame);
        }
        if(ID==2){   //"Continue"
            if( !game.gameloaded )
                msg("no Game loaded!",100);
            else
                game.openMenu(game.menuNone);
        }
        else if(ID==5){   //"Save Game"
            if( !game.gameloaded )
                msg("no Game loaded!",100);
            else
                game.openMenu(game.menuSave);
        }
    }
}
