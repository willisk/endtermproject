
package game;

//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;
import static game.Game.round;
import java.awt.*;

public abstract class Menu{
    
    protected Game game;
    protected String[] list;
    protected int[] mList;
    protected int menuID;
    protected String popupText;
    protected static int popupID = -2;
    protected boolean inPopMenu, cinematicEnded;
    protected int popMenuX, popMenuY, popMenuSizeX, popMenuSizeY;
    protected String msgBox, cinematicText;
    protected int msgBoxTime;
    protected String hint;
    private int cinematic;
    private final int cinematicTime = 100;
    
    public Menu(Game game){
        this.game=game;
        
        list = new String[0];
        mList = new int[0];
        
        menuID = game.menuNone;
        
        popupText = "";
        inPopMenu = false;
        cinematicEnded = false;
        
        hint = "";
        cinematicText = "";
        
        setDimensions();
        
    }
    
    public void setDimensions(){
        popMenuX = game.scale*(game.scrollboarder);
        popMenuY = (int)(game.windowsizey/2);
        popMenuSizeX = game.scale*(game.gamesizex);
        popMenuSizeY = game.scale*4;
    }
    
    public void draw(Graphics2D g2d){
        g2d.setColor(Color.black);
        g2d.fillRect(0,0,game.windowsizex,game.windowsizey);
        //g2d.setColor(Color.white);
        
        g2d.setColor(Color.white);
        g2d.setFont(game.fontBig);
        
        for(int i=0;i<list.length;i++)
            g2d.drawString(list[i],game.scale*(game.scrollboarder),game.scale*(game.scrollboarder+1+i*2));
        
        
        g2d.setFont(game.fontSmall);
        g2d.drawString(hint,game.scale*(game.scrollboarder),game.scale*(game.gamesizey+game.scrollboarder));
        
        if(inPopMenu)
            drawPopMenu(g2d,popMenuX,popMenuY,popMenuSizeX,popMenuSizeY);
        
        if(msgBoxTime>0){
            msgBoxTime--;
            g2d.setFont(game.fontSmall);
            g2d.drawString(".. "+msgBox,game.scale*(game.scrollboarder+3),game.windowsizey-game.scale);
        }
        
        if(cinematic<=0){
            if(!cinematicEnded){
                cinematicEnded = true;
                onCinematicEnd();
            }
        }
        else
            cinematic--;
        
        g2d.setFont(game.fontLarge);
        g2d.drawString(cinematicText,game.scale*(game.scrollboarder),
                game.scale*(game.scrollboarder+2+game.gamesizey*cinematicCurve(cinematic)/2));
        //g2d.drawString("PAUSED",game.windowsizex/2-50,game.windowsizey/2);
    }
    
    
    public void drawPopMenu(Graphics g2d, int x, int y, int sizex, int sizey){
        g2d.setColor(Color.black);
        g2d.fillRect(x,y,sizex,sizey);
        
        int linew = 5;
        g2d.setColor(Color.white);
        g2d.fillRect(x+linew,y,linew,sizey);
        g2d.fillRect(x,y+linew,sizex,linew);
        g2d.fillRect(x+sizex-2*linew,y,linew,sizey);
        g2d.fillRect(x,y+sizey-2*linew,sizex,linew);
        
        g2d.setColor(Color.gray);
        g2d.fillRect(x+linew+2,y+2,linew-2,sizey-2);
        g2d.fillRect(x+2,y+linew+2,sizex-2,linew-2);
        g2d.fillRect(x+sizex-2*linew+2,y+2,linew-2,sizey-2);
        g2d.fillRect(x+2,y+sizey-2*linew+2,sizex-2,linew-2);
        
        g2d.setFont(game.fontSmall);
        g2d.setColor(Color.white);
        String[] parts = popupText.split("/n");
        g2d.drawString(parts[0],x+game.scale,y+(int)(1.5*game.scale));
        g2d.drawString("Yes!",(int)(x+sizex/4-game.scale),(y+sizey-game.scale));
        g2d.drawString("No!",(int)(x+3*sizex/4-game.scale),(y+sizey-game.scale));
        
        if(parts.length>1){
            g2d.setFont(game.fontSmall);
            g2d.drawString(parts[1],x+game.scale*2,y+(int)(2.2*game.scale));
        }
        
        update(g2d);
    }
    
    
    public void addPopButton(String buttonText, String Text){
        popupText = Text;
        addButton(buttonText, popupID);
    }
    
    public void openPopMenu(){
        inPopMenu = true;
    }
    
    public void closePopMenu(){
        inPopMenu = false;
    }
    
    public void addButton(String Text, int Menu){
        String[] listNew = new String[list.length+1];
        int[] mListNew = new int[list.length+1];
        for(int i=0;i<list.length;i++){
            listNew[i] = list[i];
            mListNew[i] = mList[i];
        }
        listNew[list.length] = Text;
        mListNew[list.length] = Menu;
        
        list = listNew;
        mList = mListNew;
    }
    
    public void addText(String Text){
        addButton(Text,menuID);
    }
    
    public void addSpacing(){
        addText("");
    }
    
    public void addCinematicText(String Text){
        cinematicText = Text;
    }
    

    //@Override
    public void mouseClicked(int x, int y) {
        
        if(!inPopMenu){
            if(x>game.scale*game.scrollboarder && x<2*game.windowsizex/3)
                for(int i=0;i<list.length;i++){
                    if(y>game.scale*(game.scrollboarder+i*2) && y<game.scale*(game.scrollboarder+1+i*2) &&
                            x<game.scale*(game.scrollboarder+list[i].length()*0.6)){
                        notifyClicked( i );
                        if(mList[i] == popupID)
                            openPopMenu();
                        else if(mList[i] != menuID)
                            game.openMenu(mList[i]);
                    }
                }
        }
        else{
            int Yesx = popMenuX+popMenuSizeX/4-game.scale;
            int Nox = popMenuX+3*popMenuSizeX/4-game.scale;
            int boundy = popMenuY+popMenuSizeY-2*game.scale;
            if(y>boundy && y<boundy+game.scale ){
                if(x>Yesx && x<Yesx+game.scale)
                    popupYes();
                else if(x>Nox && x<Nox+game.scale)
                    popupNo();
            }
        }
        
    }
    
    public void notifyClicked( int ID ){}
    
    public void onMenuOpen(){
        cinematic = cinematicTime;
    }
    public void update(Graphics g2d){}
    
    public void popupYes(){}
    
    public void popupNo(){
        closePopMenu();
    }
    
    public void msg( String Text, int time ){
        msgBox = Text;
        msgBoxTime = time;
    }
    
    public void onCinematicEnd(){}
    
    public float cinematicCurve(int n){
        return (float)((  Math.cos(Math.PI*(1-(n+0.0f)/(cinematicTime+0.0f)))  +1.0f)/2)*(game.sign(n)+0.0f);
    }
    
    public void select(String sel){}
}
