
package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
/*import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;*/
import java.util.Properties;
//import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
//import com.googlecode.lanterna.terminal.swing.SwingTerminal;
//import com.googlecode.lanterna.TerminalFacade;
//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;



//@SuppressWarnings("serial")
public class Game extends JPanel implements MouseListener{
    
    
    public static final int gamesizex = 15;
    public static final int gamesizey = gamesizex;
    public static final int scrollboarder = 3;
    private static final int loadDist = scrollboarder+1;    //distance to gameboarder where objects "offscreen" should be loaded
    public static int scale;
    public static int windowsizex, windowsizey;
    public static int cinematicTime = 20;   //frames per cinematic scroll
    private int levelheight, levelwidth;
    public boolean paused = false;
    public static final boolean debug = false;
    public int[][] level;
    private float[][] debugsq = new float[0][0];
    private boolean[][] grass;
    public int[] quadrant = {0,0};
    public int[] cinematic = {0,0};
    public int[] oldQuadrant = {0,0};
    
    public long randomSeed;
    
    //Buffer Images
    private Image image_floor, image_wall, image_wall_top, image_grass,
            image_entrance, image_exit;
    private int wall_yoffset;
    
    private Image image_top_gradient;
    private Image image_side_gradient;
    
    private AffineTransform bottom_transform, left_transform, right_transform;
    
    private int currentMenu;
    public Menu[] menu; //NOTE: MenuLoad needs acces @popup, change?
    public static int menuNone, menuMain, menuLoad, menuSave, menuControls,
            menuOptions, menuDifficulty, menuNewGame, menuGameOver, menuGameFinished;
    
    private Object[] objects = new Object[0];
    private Object[] old_objects = new Object[0];
    public Player player;
    public static final int TypeNone = -1;
    public static final int TypeWall = 0;
    public static final int TypeEntrance = 1;
    public static final int TypeExit = 2;
    public static final int TypeTrap = 3;
    public static final int TypeMinion = 4;
    public static final int TypeKey = 5;
    
    public static String savePath = "game/savegame/";
    
    private String theme;
    
    private JFrame frame;
    
    public static int max_keys;
    
    public static Font fontBig, fontSmall, fontLarge;
    
    public static boolean gamerunning, gameloaded, quadrantloaded, exitOpen;
    
    private String loadGameQueue, last_game;
    private int waitForFile;
    
    //private SwingTerminal terminal;
    //private Screen screen;
    
    private boolean isSwing;
        
    
    public static void main(String[] args) throws InterruptedException{
        
        int framerate = 40; //also gamespeed
        
        int waittime = (int)(1000f/(framerate+0f));
        
        Game game = new Game( new JFrame("Game") );
        
        while (game.gamerunning) {
            Thread.sleep(waittime);
            game.update();
            game.repaint();
            //game.getScreen().refresh();
        }
        
        System.exit(0);
    }
    
    /*public Screen getScreen(){
        return screen;
    }*/
    
    public Game( JFrame newFrame ){
        
        isSwing = true;
        
        gamerunning = true;
        quadrantloaded = true;
        exitOpen = false;
        
        frame = newFrame;
        
        player = new Player(this,1,1);
        addObject(player);
        
        //Menus
        
        menuNone = -1;
        menuMain = 0;
        menuSave = 1;
        menuLoad = 2;
        menuControls = 3;
        menuOptions = 4;
        menuDifficulty = 5;
        menuNewGame = 6;
        menuGameOver = 7;
        menuGameFinished = 8;
        
        currentMenu = menuNone;
        
        menu = new Menu[9];
        
        theme="cave";
        setDimensions(24);
        openMenu(menuMain);
        
        loadGameQueue = "";
        last_game = "";
        waitForFile = 0;
        
        
        //Set up frame
        
	frame.setVisible(true);
        frame.setResizable(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(this);
        
        KeyListener keylistener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                keyPressedGame(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                keyReleasedGame(e.getKeyCode());
            }

	};
        
        frame.addKeyListener(keylistener);
        
        frame.addMouseListener(this);
        
        /*this.terminal = new SwingTerminal(gamesizex+2*scrollboarder,
                gamesizey+2*scrollboarder);		
        this.terminal.setCursorVisible(false);

        this.screen = TerminalFacade.createScreen(this.terminal);
        this.screen.startScreen();

        JFrame sframe = this.terminal.getJFrame();		
        sframe.setTitle("Game");
        sframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        sframe.setResizable(false);
        
        sframe.addKeyListener(keylistener);
        
        sframe.addMouseListener(this);
        
        this.screen.stopScreen();*/
        //toggleLanterna();
        
        
    }
    
    /*public void toggleLanterna(){
        if(isSwing){
            frame.setVisible(false);
            screen.startScreen();
        }
        else{
            frame.setVisible(true);
            screen.stopScreen();
        }
    }*/
    
    public void setDimensions( int newScale ){
        
        if(newScale<16)
            newScale=16;
        else if(newScale>=64)
            newScale=64;
        
        scale = newScale;
        
        windowsizex = (gamesizex + 2*scrollboarder + 0)*scale;
        windowsizey = (gamesizey + 2*scrollboarder + 0)*scale;
        
	frame.setSize(windowsizex+0, windowsizey+28);
        
        
        
        //set up game boarder transforms
        bottom_transform = new AffineTransform();
        bottom_transform.translate(windowsizex,windowsizey);
        bottom_transform.rotate(Math.PI, 0, 0);
        
        left_transform = new AffineTransform();
        left_transform.translate(0,windowsizey);
        left_transform.rotate(3*Math.PI/2, 0, 0);
        
        right_transform = new AffineTransform();
        right_transform.translate(windowsizex,0);
        right_transform.rotate(Math.PI/2, 0, 0);
        
        
        //Set Fonts
        
        fontLarge = new Font("TimesRoman", Font.PLAIN, scale*2);
        fontBig = new Font("TimesRoman", Font.PLAIN, scale);
        fontSmall = new Font("TimesRoman", Font.PLAIN, (int)(scale*0.7));
        
        
        
        //Load Images
        
        image_top_gradient = BufferImageResized("gradient",windowsizex,scrollboarder*scale);
        image_side_gradient = BufferImageResized("gradient",windowsizey,scrollboarder*scale);
        
        loadTheme(theme);
        
        //for(int i=0;i<objects.length;i++){
        //    objects[i].loadImages();
        //}
        
        player.loadImagesPlayer();    //NOTE: should change to static
        Key.loadImages();
        Minion.loadImages();
        Trap.loadImages();
        
        //Load Menus
        
        loadMenus();
        
        for(int i=0;i<menu.length;i++){
            menu[i].setDimensions();
        }
        
    }
    
    public void loadMenus(){
        menu[menuMain] = new MenuMain(this);
        menu[menuSave] = new MenuSave(this);
        menu[menuLoad] = new MenuLoad(this);
        menu[menuControls] = new MenuControls(this);
        menu[menuOptions] = new MenuOptions(this);
        menu[menuDifficulty] = new MenuDifficulty(this);
        menu[menuNewGame] = new MenuNewGame(this);
        menu[menuGameOver] = new MenuGameOver(this);
        menu[menuGameFinished] = new MenuGameFinished(this);
    }
    
    public void loadTheme(String newTheme){
        
        image_exit = BufferImage("exit",1);
        image_entrance = image_exit;
        
        switch(newTheme){
            case "woods":
                theme = "woods";
                image_floor = BufferImage("t_grass",1);
                image_wall = BufferImage("t_tree",1,2);
                image_wall_top = BufferImage("t_tree_top",1,2);
                image_grass = BufferImage("t_grass1",1);
                wall_yoffset = -1;
                Player.setMaxHealth(12);
                Player.setDamage(1);
                Minion.setDamage(4);
                Trap.setDamage(4);
                break;
            case "snow":
                theme = "snow";
                image_floor = BufferImage("t_snow",1);
                image_wall = BufferImage("t_stree",1,2);
                image_wall_top = BufferImage("t_stree_top",1,2);
                image_grass = BufferImage("t_stomp",1);
                wall_yoffset = -1;
                Player.setMaxHealth(16);
                Player.setDamage(2);
                Minion.setDamage(4);
                Trap.setDamage(4);
                break;
            case "cave":
                theme = "cave";
                image_floor = BufferImage("t_rock",1);
                image_wall = BufferImage("t_stone",1);
                image_grass = BufferImage("t_rock1",1);
                wall_yoffset = 0;
                Player.setMaxHealth(16);
                Player.setDamage(4);
                Minion.setDamage(4);
                Trap.setDamage(2);
                break;
        }
    }
    
    
    
    public void update(){
        
        if(loadGameQueue.compareTo("")!=0){
            if( waitForFile>0 )  //needs to give the program time to delete old file
                waitForFile--;
            else
                if( loadGame(loadGameQueue) )
                    loadGameQueue = "";
        }
        else if( waitForFile>0 ){  //no Game in load Queue,=> Game was saved
            waitForFile--;
            
            if( waitForFile==0 )    //update the menus 
                loadMenus();
        }
        
        if(inCinematic()){
            cinematic[0]-=sign(cinematic[0]);
            cinematic[1]-=sign(cinematic[1]);
            
            paused = true;
        }
        else
            paused = false;
        
        if(!paused && !inMenu())
            updateObjects();
        
    }
    
    public void updateObjects(){
        for(int i=0;i<objects.length;i++)
            objects[i].update();
    }
    
    
    public void collectKey(Object key){
        removeObject(key);
        level[(int)key.getX()+gamesizex*quadrant[0]][(int)key.getY()+gamesizey*quadrant[1]]=-1;
        if(player.getKeys()>=max_keys)
            exitOpen = true;
    }
    
    public void loadObjects(){
        
        //stores all old objects in old_objects and creates empty array for objects
        deleteObjects();    
        
        
        /*System.out.println("\n\nold_obj IDs:");
        for(int i=0;i<old_objects.length;i++){
            System.out.println("\t"+old_objects[i].getID()+" "+old_objects[i].getX()+" : "+old_objects[i].getY());
        }
        System.out.println(" --- END");*/
        
        
        //load all objects of new quadrant
        for(int n=0-loadDist;n<gamesizey+loadDist;n++){
            for(int k=0-loadDist;k<gamesizex+loadDist;k++){
                int x = k + gamesizex*quadrant[0];
                int y = n + gamesizey*quadrant[1];
                
                if(inGameBounds(x,y)){
                    
                    if(getType(x,y)==TypeMinion && inOverlappingQuadrant(k,n))
                        ;//Minion has already been loaded
                    else
                        loadDynObj(x,y,k,n);
                }
            }
        }
        
        //keep old dynamic Enemies that are in loadDist of new quadrant
        for(int i=0;i<old_objects.length;i++){
            if(old_objects[i].type==TypeMinion)
                if(/*old_objects[i].isAlive() &&*/
                        old_objects[i].getX()>(0-loadDist) &&
                        old_objects[i].getX()<(gamesizex+loadDist) &&
                        old_objects[i].getY()>(0-loadDist) &&
                        old_objects[i].getY()<(gamesizey+loadDist) ){
                    addObject(old_objects[i]);
                    //System.out.println(old_objects[i].getID()+" found in loadDist with x:"+old_objects[i].getX()+" y:"+old_objects[i].getY());
                }
        }
        
        /*System.out.println("\nnew obj IDs:");
        for(int i=0;i<objects.length;i++){
            System.out.println("\t"+objects[i].getID()+" "+objects[i].getX()+" : "+objects[i].getY());
        }
        System.out.println(" --- END");*/
        
    }
    
    public void deleteObjects(){
        for(int i=0;i<objects.length;i++){
            //objects[i].delete();
            if( !player.compareID(objects[i].getID()))
                old_objects = utility_addObject(old_objects,objects[i]);
        }
        //old_objects = objects;
        Object[] newobj = new Object[1];
        newobj[0] = player;
        
        objects = newobj;
    }
    
    public void loadDynObj(int x, int y,int k,int n){
        
        int type = level[x][y];
        //System.out.println("loading x "+k+" y "+n);
        
        if(type == TypeMinion){
            String ID = "Minion"+(x<10?"0":"")+x+(y<10?"0":"")+y;
            boolean already_exists = false;
            //System.out.println(ID);

            for(int i=0;i<old_objects.length;i++)
                if(old_objects[i].compareID(ID)){
                    if(old_objects[i].isAlive())
                        addObject(old_objects[i]);  //Minion has already spawned, keep it
                    already_exists = true;
                    break;
                }

            if(!already_exists)
                addObject((Object)new Minion(this,k,n,ID));
        }

        if(type == TypeKey){
            addObject((Object)new Key(this,k,n));
        }
        
        if(type == TypeTrap){
            addObject((Object)new Trap(this,k,n));
        }
        
    }
    
    
    
    public void movequadrant(int x,int y){
        
        quadrantloaded = false;
        
        oldQuadrant[0] = x*-1;
        oldQuadrant[1] = y*-1;
        
        cinematic[0]+=cinematicTime*x;
        cinematic[1]+=cinematicTime*y;
        
        quadrant[0]+=x;
        quadrant[1]+=y;
        
        //player.updatePos(player.getX()-gamesizex*x,player.getY()-gamesizey*y);
        
        for(int i=0;i<objects.length;i++)
            objects[i].forcePos(objects[i].getX()-gamesizex*x,objects[i].getY()-gamesizey*y);
        
        for(int i=0;i<old_objects.length;i++)
            old_objects[i].forcePos(old_objects[i].getX()-gamesizex*x,old_objects[i].getY()-gamesizey*y);
        
        loadObjects();
        
        //if(debug)
        //    System.out.println("moving dir:"+x+","+y+" to:"+quadrant[0]+","+quadrant[1]);
        
        
    }
    
    public boolean inOverlappingQuadrant(int x, int y){
        if(oldQuadrant[0]==-1)
            return x<loadDist;
        else if(oldQuadrant[0]==1)
            return x>gamesizex-loadDist;
        else if(oldQuadrant[1]==-1)
            return y<loadDist;
        else if(oldQuadrant[1]==1)
            return y>gamesizey-loadDist;
        
        return false;
    }
    
    public void onQuadrantLoaded(){
        old_objects = new Object[0];    //not needed anymore after paintjob is done
    }
    
    public void spawnPlayer( float x, float y ){
        
        quadrant[0] = 0;
        quadrant[1] = 0;
        cinematic[0] = 0;
        cinematic[1] = 0;
        
        while( x > gamesizex ){
            x-=gamesizex;
            quadrant[0]++;
        }
        while( y > gamesizey ){
            y-=gamesizey;
            quadrant[1]++;
        }
        
        player.spawnPlayer(x,y);
    }
    
    public boolean isMenuOpen(){
        return currentMenu>=0;
    }
    
    @Override
    public void paint(Graphics g) {
        
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(gameloaded){

            float cinematicxoffset = 0;
            float cinematicyoffset = 0;

            if(cinematic[0]!=0)
                cinematicxoffset = cinematicCurve(cinematic[0])*gamesizex;
            if(cinematic[1]!=0)
                cinematicyoffset = cinematicCurve(cinematic[1])*gamesizey;

            int renderstartx = quadrant[0]*gamesizex-scrollboarder-(int)cinematicxoffset;
            int renderstarty = quadrant[1]*gamesizey-scrollboarder-(int)cinematicyoffset;

            int renderstopx = (quadrant[0]+1)*gamesizex+scrollboarder-(int)(cinematicxoffset);
            int renderstopy = (quadrant[1]+1)*gamesizey+scrollboarder-(int)(cinematicyoffset);

            if( inCinematic() ){
                renderstartx--;
                renderstopx++;
                renderstarty--;
                renderstopy++;
            }

            //if(debug && cinematic[0]!=0)
            //    System.out.println(renderstartx+", "+renderstopx+" : "+renderstarty+", "+renderstopy);

            for(int n=renderstarty;n<renderstopy;n++)
                for(int k=renderstartx;k<renderstopx;k++){
                    //if(isSwing)
                        paintStaticObject(getType(k,n),
                                (k-gamesizex*quadrant[0]),
                                (n-gamesizey*quadrant[1]),
                                (cinematicxoffset),
                                (cinematicyoffset),
                                g2d);
                    /*else
                        paintStaticObject(getType(k,n),
                                (k-gamesizex*quadrant[0]),
                                (n-gamesizey*quadrant[1]),
                                (cinematicxoffset),
                                (cinematicyoffset),
                                screen);*/
                }



            for(int i=1;i<objects.length;i++){
                //if(isSwing)
                    paintDynObject(objects[i],cinematicxoffset,cinematicyoffset,g2d);
                //else
                //    paintDynObject(objects[i],cinematicxoffset,cinematicyoffset,screen);
            }
            
            //paint player last
                paintDynObject(player,cinematicxoffset,cinematicyoffset,g2d);

            if( inCinematic() )
                for(int i=0;i<old_objects.length;i++){
                    //if(old_objects[i].isAlive())
                            paintDynObject(old_objects[i],cinematicxoffset,cinematicyoffset,g2d);
                }
            
            if( isSwing ){
                //game bounds gradient
                g2d.drawImage(image_top_gradient,0,0,null);
                g2d.drawImage(image_top_gradient,bottom_transform, null);
                g2d.drawImage(image_side_gradient,left_transform, null);
                g2d.drawImage(image_side_gradient,right_transform, null);
            }

            //interface
            player.painthearts(g2d);
            
            if(isSwing && debug){
                g2d.setColor(Color.red);
                g2d.drawRect(scrollboarder*scale,scrollboarder*scale,gamesizex*scale,gamesizey*scale);


                //g2d.setColor(Color.green);
                g2d.drawRect(((int)(player.getX()+scrollboarder)*scale),((int)(player.getY()+scrollboarder)*scale),scale,scale);
                g2d.drawRect(((int)(player.getX()+player.size+scrollboarder)*scale),((int)(player.getY()+scrollboarder)*scale),scale,scale);
                g2d.drawRect(((int)(player.getX()+scrollboarder)*scale),((int)(player.getY()+player.size+scrollboarder)*scale),scale,scale);
                g2d.drawRect(((int)(player.getX()+player.size+scrollboarder)*scale),((int)(player.getY()+player.size+scrollboarder)*scale),scale,scale);

                g2d.setColor(Color.magenta);
                for(int i=0;i<debugsq.length;i++){
                    g2d.drawRect((int)((debugsq[i][0]+scrollboarder)*scale),(int)((debugsq[i][1]+scrollboarder)*scale),(int)(debugsq[i][2]*scale),(int)(debugsq[i][2]*scale));
                    if(debugsq[i][3]==1)
                        removeDebugSquare(i);
                    else if(debugsq[i][3]>1)
                        debugsq[i][3]--;
                }

                float[] coords = localToAbsoluteCoords(player.getX(),player.getY());
                g2d.setColor(Color.white);
                g2d.drawString((int)coords[0]+" : "+(int)coords[1],scale*scrollboarder,scale*(scrollboarder+gamesizey));

                //ObjectTrace(player.getX(),player.getY(),player.size,);
            }
        }

        //Menu
        if(isMenuOpen())
            menu[currentMenu].draw(g2d);

        if( !inCinematic() && !quadrantloaded ){
            quadrantloaded = true;
            onQuadrantLoaded();
        }
        
    }
    
    public boolean inCinematic(){
        return cinematic[0]!=0 || cinematic[1]!=0;
    }
    
    public void paintDynObject(Object obj, float offsX, float offsY, Graphics2D g2d){
        obj.paint(g2d,offsX,offsY);
        
        //draw Trees "below" Object
        
        if(wall_yoffset!=0){

            float[] coords = {obj.getX(),obj.getY()+1};//localToAbsoluteCoords(obj.getX(),obj.getY()+1);

            int x = (int)coords[0];
            int y = (int)coords[1];

            int[] points = {x,y, x-1,y, x+1,y, x+2,y, x,y+1, x-1,y+1, x+1,y+1, x+2,y+1};

            for(int k=0;k<points.length;k+=2)
                if(getType(points[k]+gamesizex*quadrant[0],points[k+1]+gamesizey*quadrant[1])==0)
                    g2d.drawImage(image_wall_top, 
                    (int)((scrollboarder + points[k] + offsX)*scale), 
                    (int)((scrollboarder + points[k+1] + offsY+wall_yoffset)*scale), null);

        }
    }
    
    
    public void paintStaticObject(int type, int x, int y, float offsX, float offsY, Graphics2D g2d){
        
        int drawX = (int)((scrollboarder + x + offsX)*scale);
        int drawY = (int)((scrollboarder + y + offsY)*scale);
        
        switch(type){
            case TypeMinion:
            case TypeKey:
            case TypeTrap: 
            case TypeNone: 
                g2d.drawImage(image_floor, drawX, drawY, null);
                if(isGrass(x+gamesizex*quadrant[0],y+gamesizey*quadrant[1]))
                    g2d.drawImage(image_grass, drawX, drawY, null);
                break;
            case TypeWall: 
                g2d.drawImage(image_floor, drawX, drawY, null);
                g2d.drawImage(image_wall, drawX, drawY+wall_yoffset*scale, null);
                break;
            case TypeEntrance:
            case TypeExit: 
                g2d.drawImage(image_floor, drawX, drawY, null);
                g2d.drawImage(image_exit, drawX, drawY, null);
                break;
        }
    }
    
    
    
    public static int round(float x){
        return (int)(x+0.5f);
    }
    
    
    public int getType(int x, int y){
        return inGameBounds(x,y)?level[x][y]:TypeNone;
    }
    
    public Object getPlayer(){
        return player;
    }
    
    public Object[] objectTrace(float[] coords, float size, Object ignoreObject){
        return objectTrace(coords[0], coords[1], size, ignoreObject);
    }
    
    public Object[] objectTrace(float x, float y, float size, Object ignoreObject){
        Object[] ignore = {ignoreObject};
        return objectTrace(x, y, size, ignore);
    }
    
    public Object[] objectTrace(float x, float y, float size, Object[] ignoreObject){
        
        Object[] traced = new Object[0];
        
        for(int i=0;i<objects.length;i++){
            
            boolean ignore = false;
            
            for(int k=0;k<ignoreObject.length && !ignore;k++)
                if(ignoreObject[k]==objects[i])
                    ignore=true;
            
            if(!ignore)
                if(squareIntersects(x,y,size,objects[i].getX(),objects[i].getY(),objects[i].size))
                    traced = utility_addObject(traced,objects[i]);
                
        }
        
        return traced;
        
    }
    
    
    public boolean collisionTrace(float x, float y, float size){
        float[] coords = {x,y};
        return collisionTrace(coords,size);
    }
    
    //trace checking whether object collides with level
    //check just involves checking for walls (type:0)
    //NOTE: only works for objects with size smaller/eq to 1
    //      + out of level bounds collision incorrect (fix?)
    public boolean collisionTrace(float[] coords, float size){
        
        //NOTE: fixes the bug when size is 1 (due to conversion to int)
        size-=0.0001f;
        
        //check walls
        coords = localToAbsoluteCoords(coords);
        
        //System.out.println("x: "+coords[0]+"y: "+coords[1]);
        
        int[] checkPoints={
            (int)(coords[0]),(int)(coords[1]),          //Up-Left
            (int)(coords[0]+size),(int)(coords[1]),     //Up-Right
            (int)(coords[0]),(int)(coords[1]+size),     //Down-Left
            (int)(coords[0]+size),(int)(coords[1]+size) //Down-Right
        };
        
        for(int i=0;i<8;i+=2){
            if(!walkable(getType(checkPoints[i],checkPoints[i+1])))
                return false;
        }
        
        //check dyn objects
        
        return true;
        
    }
    
    public boolean inGameBounds( int[] coords ){
        return inGameBounds(coords[0],coords[1]);
    }
    public boolean inGameBounds( int x, int y ){
        return x>=0 && x<levelwidth && y>=0 && y<levelheight;
    }
    
    public boolean inScreenBounds( float[] coords ){
        return inScreenBounds((int)coords[0],(int)coords[1]);
    }
    public boolean inScreenBounds( int x, int y ){  //assuming size of object is 1
        return x>=0 && x<(gamesizex-1) && y>=0 && y<(gamesizey-1);
    }
    
    public static boolean walkable(int type){
        if(type==TypeWall || type==TypeEntrance || (type==TypeExit && !exitOpen))
            return false;
        return true;
    }
    
    
    
    public void addObject(Object object){
        this.objects=utility_addObject(this.objects,object);
    }
    
    public Object[] utility_addObject(Object[] objlist,Object object){
        Object[] newobj = new Object[objlist.length+1];
        for(int i=0;i<objlist.length;i++){
            if(objlist[i].compareID(object.getID()))
                return objlist; //do not allow duplicates
            newobj[i]=objlist[i];
        }
        newobj[objlist.length]=object;
        return newobj;
    }
    
    public void removeObject(Object obj){
        for(int i=0;i<objects.length;i++)
            if(obj == objects[i]){
                removeObject(i);
                break;
            }
    }
    
    public void removeObject(int n){
        
        //save object in old_objects, in case it is needed later
        //old_objects = utility_addObject(old_objects,objects[n]);
        
        //objects[n].delete();
        
        Object[] newobj = new Object[this.objects.length-1];
        
        for(int i=0;i<n;i++)
            newobj[i]=objects[i];
        
        for(int i=n;i<newobj.length;i++)
            newobj[i]=objects[i+1];
        
        objects=newobj;
    }
    
    
    
    public void keyPressedGame(int KeyCode){
        //System.out.println(e.getKeyCode() + "+");
        if(!paused){
            if (KeyCode == KeyEvent.VK_W){
                player.movestart(0,-1);
            }
            else if (KeyCode == KeyEvent.VK_A){
                player.movestart(-1,0);
            }
            else if (KeyCode == KeyEvent.VK_S){
                player.movestart(0,1);
            }
            else if (KeyCode == KeyEvent.VK_D){
                player.movestart(1,0);
            }
            else if (KeyCode == KeyEvent.VK_J){
                player.attack();
            }
        }
    }
    
    
    public void keyReleasedGame(int KeyCode){
        //System.out.println(e.getKeyCode() + "-");
        if (KeyCode == KeyEvent.VK_W)
            player.movestop(0,-1);
            
        if (KeyCode == KeyEvent.VK_A)
            player.movestop(-1,0);
            
        if (KeyCode == KeyEvent.VK_S)
            player.movestop(0,1);
            
        if (KeyCode == KeyEvent.VK_D)
            player.movestop(1,0);
            
        if (KeyCode == KeyEvent.VK_ESCAPE)
            toggleMenu();
            
        
    }
    
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() - 3;
        int y = e.getY() - 27;
        
        if(isMenuOpen())
            menu[currentMenu].mouseClicked(x,y);
    }
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    
    
    
    public void toggleMenu(){
        if(isMenuOpen() && gameloaded)
            closeMenu();
        else
            openMenu(menuMain);
    }
    
    public boolean inMenu(){
        return currentMenu != menuNone;
    }
    
    public void openMenu(int menuNew){
        //System.out.println("opening menu"+menuNew);
        if(menuNew==menuNone)
            closeMenu();
        else{
            paused=true;
            currentMenu = menuNew;
            menu[currentMenu].onMenuOpen();
        }
    }
    
    public void closeMenu(){
        paused=false;
        if(currentMenu!=menuNone)
            menu[currentMenu].closePopMenu();
        currentMenu = menuNone;
    }
    
    public void quit(){
        gamerunning=false;
    }
    
    public void createNewGame(String slot, String difficulty){
        String theme = "cave";
        
        if(difficulty.compareTo("Easy")==0)
            theme="cave";
        else if(difficulty.compareTo("Medium")==0)
            theme="snow";
        else if(difficulty.compareTo("Hard")==0)
            theme="woods";
        //if(slotExists(slot))
        //    deleteSlot(slot);
        Generate.newMaze(savePath+"Slot"+slot,this.getClass().getResourceAsStream("parameters"+difficulty+".txt"), theme);
        
        loadGameQueue = "Slot"+slot;
        waitForFile = 100;
    }
    
    public void deleteSlot(String slot){
        
	//File file = new File(this.getClass().getResource("savegame/Slot"+slot+".properties").getFile());
        
        File file = new File(savePath+"Slot"+slot+".properties");
        if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
        }else{
                System.out.println("Delete operation is failed.");
        }
        System.out.println("ADD THIS!");    //NOTE: ADD
    }
    
    public boolean loadGame( String filename ){
        
        //filename = "level_small";
        
        last_game = filename;
        
        
        Properties prop = new Properties();
        
        InputStream input = this.getClass().getResourceAsStream("savegame/"+filename+".properties");
        
        if(input==null)
            return false;   //game couldnt be loaded

	try{
            prop.load(input);
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
	}


        levelheight=Integer.parseInt(prop.getProperty("Height"));
        levelwidth=Integer.parseInt(prop.getProperty("Width"));

        level = new int[levelwidth][levelheight];

        int keys=0;
        int health=0;
        float posx=0;
        float posy=0;
        String theme;

        theme = prop.getProperty("Theme");

        try{
            keys = Integer.parseInt(prop.getProperty("Keys"));
        } catch(NumberFormatException e){}

        try{
            health = Integer.parseInt(prop.getProperty("Health"));
        } catch(NumberFormatException e){}
        
        try{
            posx = Float.parseFloat(prop.getProperty("posX"));
        } catch(NullPointerException e){}

        try{
            posy = Float.parseFloat(prop.getProperty("posY"));
        } catch(NullPointerException e){}

        max_keys = keys;

        for(int n=0;n<levelheight;n++){
            for(int k=0;k<levelwidth;k++){
                String p = prop.getProperty(k+","+n);
                if(p==null)
                    level[k][n]=-1;
                else{
                    int q = Integer.parseInt(p);
                    level[k][n]=q;
                    if(q==TypeKey)
                        max_keys++;
                    if(q==TypeEntrance && posx==0 && posy==0){
                        posx = k;
                        posy = n;
                    }
                }
            }
        }
        
        
        if(theme!=null)
            loadTheme(theme);
        else
            loadTheme("cave");
        
        
        deleteObjects();
        old_objects = new Object[0];

        spawnPlayer(posx,posy);

        if(health>0)
            player.setHealth(health);
        else
            player.restoreHealth();

        player.keys = keys;


        calculateGrass();
        gameloaded = true;

        loadMenus();    //need to be updated after another slot has been added

        closeMenu();

        loadObjects();
        
        return true;
    }
    
    public void calculateGrass(){
        
        java.util.Random rand = new java.util.Random();
        
        long seed = System.nanoTime();
        
        int mHeight = levelheight+2*scrollboarder;
        int mWidth = levelwidth+2*scrollboarder;
        
        grass = new boolean[mWidth][mHeight];
        
        int[][] matrix = new int[mWidth+2][mHeight+2];
        
        for(int n=0;n<mHeight+2;n++)
            for( int k=0;k<mWidth+2;k++){
                rand.setSeed(seed+n*gamesizey+k);
                matrix[k][n] = rand.nextInt(10);
            }
        
        
        int grassprob = 0;
        int grassfieldprob = 4;
        
        for(int n=1;n<mHeight+1;n++)
            for( int k=1;k<mWidth+1;k++){
                grass[k-1][n-1] = matrix[k][n] <= grassprob || 
                    (matrix[k][n]<=grassfieldprob && (
                    matrix[k-1][n] <= grassprob || 
                    matrix[k][n-1] <= grassprob ||
                    matrix[k+1][n] <= grassprob ||
                    matrix[k][n+1] <= grassprob ||
                    matrix[k-1][n-1] < grassprob ||
                    matrix[k+1][n-1] < grassprob ||
                    matrix[k-1][n+1] < grassprob ||
                    matrix[k+1][n+1] < grassprob ) 
                    );
            }
    }
    
    public boolean isGrass(int x, int y){
        return (x>=0-scrollboarder && x<levelwidth+scrollboarder && 
                y>=0-scrollboarder && y<levelheight+scrollboarder)?
                grass[x+scrollboarder][y+scrollboarder]:false;
    }
    
    public boolean slotExists( String slot ){
        return this.getClass().getResourceAsStream("savegame/Slot"+slot+".properties")!=null;
    }
    
    public String slotStat( String slot ){
        InputStream input = this.getClass().getResourceAsStream("savegame/Slot"+slot+".properties");
        if(input==null)
            return "(empty)";
        
        Properties prop = new Properties();
        try{ prop.load(input);
        } catch (IOException ex) {}
        
        String theme = prop.getProperty("Theme");
        
        if(theme!=null){
            int keys = 0;
            try{
                keys = Integer.parseInt(prop.getProperty("Keys"));
            } catch(NumberFormatException e){}
            
            int max_keys;
            switch(theme){
                case "cave": max_keys=5;break;
                case "snow": max_keys=10;break;
                default: max_keys=15;
            }
            return theme+" ("+keys+"/"+max_keys+" keys)";
        }
        
        return theme==null?"n/a":theme;
    }
    
    public void saveGame( String filename ){
        

        Properties prop = new Properties();

        prop.setProperty("Height",levelheight+"");
        prop.setProperty("Width",levelwidth+"");

        prop.setProperty("Theme",theme);

        for(int n=0;n<levelheight;n++)
            for(int k=0;k<levelwidth;k++)
                if(level[k][n]!=TypeNone)
                    prop.setProperty(k+","+n,level[k][n]+"");

        prop.setProperty("Keys",player.getKeys()+"");
        prop.setProperty("Health",player.getHealth()+"");

        float[] pos = localToAbsoluteCoords(player.getX(),player.getY());

        prop.setProperty("posX",pos[0]+"");
        prop.setProperty("posY",pos[1]+"");

        try {
            FileOutputStream file = new FileOutputStream( new java.io.File(savePath+filename+".properties") );
            
            prop.store(file, "Properties");
            file.close();
        }
        catch (Exception e ) {
            e.printStackTrace();
        }
        
        waitForFile = 150;
    }
    
    public void loadLastGame(){
        loadGame(last_game);
    }
    
    public void gameOver(){
        gameloaded = false;
        openMenu(menuGameOver);
    }
    
    public void gameFinished(){
        gameloaded = false;
        openMenu(menuGameFinished);
    }
    
    
    //Images
    
    public static Image BufferImage(String filename, float size_scale){
        return BufferImageResized(filename,(int)(scale*size_scale),(int)(scale*size_scale));
    }
    
    public static Image BufferImage(String filename, float size_scalex, float size_scaley){
        return BufferImageResized(filename,(int)(scale*size_scalex),(int)(scale*size_scaley));
    }
    
    public static Image BufferImageResized(String filename, int sizex, int sizey){
        Image image = null;
        try {                
            image = ImageIO.read(Game.class.getResource("images/"+filename+".png"))
                  .getScaledInstance(sizex, sizey, image.SCALE_DEFAULT);
        } catch (IOException e) {
           System.out.println("Image read Error");
        }
        
        return image;
    }
    
    
    
    
    
    
    //Debug
    
    
    
    public void addDebugSquare(float[] coords,float size,float time){
        addDebugSquare(coords[0], coords[1], size, time);
    }
    
    public void addDebugSquare(float x,float y,float size,float time){
        float[][] newdebug = new float[debugsq.length+1][4];
        for(int i=0;i<debugsq.length;i++)
            newdebug[i]=debugsq[i];
        newdebug[debugsq.length][0]=x;
        newdebug[debugsq.length][1]=y;
        newdebug[debugsq.length][2]=size;
        newdebug[debugsq.length][3]=time;
        debugsq = newdebug;
    }
    
    public void removeDebugSquare(int n){
        
        float[][] newdebug = new float[debugsq.length-1][4];
        
        for(int i=0;i<n;i++)
            newdebug[i]=debugsq[i];
        
        for(int i=n;i<(debugsq.length-1);i++)
            newdebug[i]=debugsq[i+1];
        
        
        debugsq=newdebug;
    }
    
    
    //Utility
    
    public static int sign(int n){
        if(n<0)
            return -1;
        if(n>0)
            return 1;
        return 0;
    }
    
    //Sinecurve 1, when n==cinematicTime; 0, when n==0
    public float cinematicCurve(int n){
        return (float)((  Math.cos(Math.PI*(1-(n+0.0f)/(cinematicTime+0.0f)))  +1.0f)/2)*(sign(n)+0.0f);
    }
    
    public float[] localToAbsoluteCoords( float[] coords ){
        return localToAbsoluteCoords(coords[0],coords[1]);
    }
    
    public float[] localToAbsoluteCoords( float x, float y ){
        float[] coordsAbsolute={x+gamesizex*quadrant[0],y+gamesizey*quadrant[1]};
        return coordsAbsolute;
    }
    
    public float[] absoluteToLocalCoords( float x, float y ){
        float[] coordsLocal={x-gamesizex*quadrant[0],y-gamesizey*quadrant[1]};
        return coordsLocal;
    }
    
    public boolean squareIntersects(float x1, float y1, float size1,
            float x2, float y2, float size2){
        
        size1 /= 2;
        size2 /= 2;
        
        float vx = abs(x2-x1+size2-size1);
        float vy = abs(y2-y1+size2-size1);
        
        return (vx<size1+size2 &&
                vy<size1+size2 );
        
    }
    
    public static float abs(float f){
        return (f>=0)?f:f*-1;
    }
    
    
}
