
package game;

//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.AlphaComposite;

public class Player extends Object {
    
    //private int x, y, movex, movey;
    
    private static int health, damage, maxhealth;
    private float steps = 0.1f;
    private int currmove = 0;
    private int invincibleTime = 40;
    private int invincibleTimetoPass = 0;
    private int attackTimetoPass = 0;
    private float attackRange = 0.8f;
    protected int keys = 0;
    private float[] cornering;
    private Image heart_full, heart_half, heart_empty, key_interface;
    private java.awt.geom.AffineTransform key_at;
    private final String[] directions = {"left","right","forward","back"};
    private final int[][] directionsVec = {{-1,0},{1,0},{0,1},{0,-1}};
    
    
    public Player(Game game,int x, int y){
        super(game,x,y,0.85f,game.TypeMinion);
        
        health=16;
        damage=4;
        maxhealth = health;
        
        cornering = newCoords(0,0);
        
        
        for(int i=0;i<4;i++){
            
            //walk animations
            addAnimationSet("link/link_sword_walk_"+directions[i],
                    "walk_"+directions[i],4,4,5,8).skipFirstFrame();
            
            //attack animations
            addAnimationSet("link/link_sword_attack_"+directions[i],
                    "attack_"+directions[i],4,4,2,7);
        }
        
        setAnimation("walk_forward");
        
        
    }
    
    public void spawnPlayer(float x, float y){
        
        forcePos(x,y);
        
        float[] absCoords = game.localToAbsoluteCoords(x,y);
        
        if(!game.walkable(game.getType((int)absCoords[0],(int)absCoords[1]))){
            
            //player was spawned at entrance
            if(game.walkable(game.getType((int)(absCoords[0]+1),(int)absCoords[1]))){
                cornering[0]=1;
                cornering[1]=0;
                setAnimation("walk_right");
            }
            else{
                cornering[0]=0;
                cornering[1]=1;
                setAnimation("walk_forward");
            }
            
        }
        //add check for entrance, use cornering
    }
    
    public void setHealth( int health ){
        this.health = health;
    }
    
    public float getHealth(){
        return health;
    }
    
    public static void setMaxHealth( int newMaxhealth ){
        maxhealth = newMaxhealth;
    }
    
    public void restoreHealth(){
        health = maxhealth;
    }
    
    public static void setDamage( int newDamage ){
        damage = newDamage;
    }
    
    public int getKeys(){
        return keys;
    }
    
    public void loadImagesPlayer(){
        
        loadAnimationImages();
        
        heart_full = Game.BufferImageResized("heart_full",Game.scale,Game.scale);
        heart_half = Game.BufferImageResized("heart_half",Game.scale,Game.scale);
        heart_empty = Game.BufferImageResized("heart_empty",Game.scale,Game.scale);
        
        key_interface = Game.BufferImageResized("key",(int)(Game.scale*1.5),(int)(Game.scale*1.5));
        key_at = new java.awt.geom.AffineTransform();
        key_at.translate(Game.scale*(Game.gamesizex+Game.scrollboarder-.3),Game.scale*0.5);
        key_at.rotate(Math.PI/6);
    }
    
    
    public void update(){
        
        
        
        if(invincibleTimetoPass>0)
            invincibleTimetoPass--;
        
        if(attackTimetoPass>0){
            attackTimetoPass--;
            animUpdate();
        }
        
        move();
        
    }
    
    public boolean isHurt(){
        return invincibleTimetoPass>0;
    }
    
    public void attack(){
        if(isAttacking() || isCornering())
            return;
        
        for(int i=0;i<4;i++)
            if( getCurrentAnimationAlias().compareTo("walk_"+directions[i])==0 ){
                attackTimetoPass = getAnimationLength("attack_"+directions[i])+4;
                setTemporaryAnimation("attack_"+directions[i]);
                attackDir(directionsVec[i]);
                break;
            }
    }
    
    public void attackDir(int[] dir){
        
        game.addDebugSquare(getX()+dir[0]*attackRange,getY()+dir[1]*attackRange,size,50);
        
        Object[] objects = game.objectTrace(getX()+dir[0]*attackRange,getY()+dir[1]*attackRange,size,this);
        
        for(int i=0;i<objects.length;i++)
            objects[i].damage(damage);
    }
    
    public boolean isAttacking(){
        return attackTimetoPass>0;
    }
    
    
    public void move() {
        
        if(isAttacking())
            return;
        
        if(isCornering()){
            
            updatePos(vectorAdd(coords,vectorScale(cornering,steps)));
            
            if(game.collisionTrace(coords,size)){
                cornering[0]=0;
                cornering[1]=0;
                if(!isMoving())
                    animReset();
            }
            
            return;
            
        }
        
        
        
        //if(move[0]==0&&move[1]==0)
        //    return;
        
        float[] newcoords = newCoords(getX(),getY());
        
        //player cannot move diagonally, only let player move in one direction
        newcoords[currmove] += move[currmove]*steps;
        
        
        if( !game.collisionTrace(coords,size) || game.collisionTrace(newcoords,size))   //player is in a wall, ignore collision, or can walk on newcoords
            updatePos(newcoords);
        else{   //player hit a wall, check if cornering is possible
            
            float corneringSize = (moving2directions())?0.41f:0.31f;
            
            float[] CornerCoords1 = newCoords(0,0);
            CornerCoords1[currmove] = roundInt(coords[currmove]+move[currmove]);
            CornerCoords1[1-currmove] = roundInt(coords[1-currmove]);

            float[] CornerCoords2 = newCoords(0,0);
            CornerCoords2[currmove] = roundInt(coords[currmove]+move[currmove]);
            CornerCoords2[1-currmove] = roundInt(coords[1-currmove] + 1);


            boolean up = game.collisionTrace(CornerCoords1[0],CornerCoords1[1],1);
            boolean down = game.collisionTrace(CornerCoords2[0],CornerCoords2[1],1);

            if(!up && !down || (up && down)){ //player hit wall and cornering is not possible
                animReset();
                return;
            }

            float cornerdistance;

            if(up)
                cornerdistance = abs(coords[1-currmove]-CornerCoords1[1-currmove]);
            else
                cornerdistance = abs(coords[1-currmove]-CornerCoords2[1-currmove]);

            if(round(cornerdistance*10f)/10f<=corneringSize){
                cornering[currmove] = move[currmove];
                cornering[1-currmove] = (up)?-1:1;
            }
            else
                animReset();

            game.addDebugSquare(CornerCoords1,1,50);
            game.addDebugSquare(CornerCoords2,1,50);
            
            //System.out.println(CornerCoords1[0]+":"+CornerCoords1[1]+"  "+CornerCoords2[0]+":"+CornerCoords2[1]);
                
            
        }
        
        
        
    }
    
    
    public boolean moving2directions(){
        return move[0]!=0 && move[1]!=0;
    }
    
    
    public void updatePos(float[] coords){
        updatePos(coords[0],coords[1]);
    }
    
    public void updatePos(float x, float y){
        
        //compensate float rounding error
        //x = ((float)(int)(x*10f)+0f)/10f;
        //y = ((float)(int)(y*10f)+0f)/10f;
        
        coords[0]=x;
        coords[1]=y;
        
        //check if player went out of the screen bounds
        
        if((x+size/2)<0)
            game.movequadrant(-1,0);
        else if((x+size/2)>game.gamesizex)
            game.movequadrant(1,0);
        
        if((y+size/2)<0)
            game.movequadrant(0,-1);
        else if((y+size/2)>game.gamesizey)
            game.movequadrant(0,1);
        
        
        //check if player hit an object
        
        Object[] obj = game.objectTrace(getX(),getY(),size,(Object)this);
        
        for(int i=0;i<obj.length;i++){
            
            if(obj[i].type==game.TypeMinion || obj[i].type==game.TypeTrap){
                if(obj[i].getDamage()>0)
                    damage(obj[i].getDamage());
            }
                
            else if(obj[i].type==game.TypeKey){
                keys++;
                game.collectKey(obj[i]);
            }
            
        }
        
        
        
        if(isMoving() || isCornering())
            animUpdate();
                    //System.out.println( round(coords[0]*10f)/10f + "  " + coords[1] );
        
        float[] absCoords = game.localToAbsoluteCoords((int)x,(int)y);
        if(!game.inGameBounds((int)absCoords[0],(int)absCoords[1]))
            game.gameFinished();
        
        
    }
    
    
    public String getID(){
        return "player";
    }
    
    public boolean compareID(String ID){
        return getID().compareTo(ID)==0;
    }
    
    public boolean isCornering(){
        return cornering[0]!=0;
    }
    
    public void movestart(int x, int y){
        if(x!=0){
            currmove = 0;
            move[0]=x;
        }
        if(y!=0){
            currmove = 1;
            move[1]=y;
        }
        
        playMoveAnimation();
    }
    
    //setAnimationNoReset must be called, since movestart is being called multiple times
    public void playMoveAnimation(){
        
        if(isAttacking())
            return;
        
        if(currmove==0){
            if(move[0]>0)
                setAnimationNoReset("walk_right");
            else if(move[0]<0)
                setAnimationNoReset("walk_left");
        }
        else{
            if(move[1]>0)
                setAnimationNoReset("walk_forward");
            else if(move[1]<0)
                setAnimationNoReset("walk_back");
        }
    }
    
    public void damage(int damage){
        
        if(invincibleTimetoPass!=0)
            return;
        
        health-=damage;
        
        if(health<=0 && !Game.debug)
            game.gameOver();
        else
            invincibleTimetoPass=invincibleTime;
    }
    
    public void movestop(int x, int y){
        if(move[0]==x){
            move[0]=0;
            currmove = 1;
        }
        if(move[1]==y){
            move[1]=0;
            currmove = 0;
        }
        
        playMoveAnimation();
        
        if(!isMoving() && !isAttacking())
            animReset();
        
    }
    
   
    public void paint(Graphics2D g2d,float offsetx,float offsety) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
        
        g2d.drawImage(getAnimationImage(),
                (int)((getX()+Game.scrollboarder+offsetx-1.5)*Game.scale),
                (int)((getY()+Game.scrollboarder+offsety-1.5)*Game.scale),null);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        
        //painthearts(g2d);
    }
    
    public void painthearts(Graphics2D g2d){
        for(int i=0;i<maxhealth/4;i++){
            if((health-i*4)>=4)
                g2d.drawImage(heart_full,Game.scale*(1+i),Game.scale,null);
            else if(health-i*4>=2)
                g2d.drawImage(heart_half,Game.scale*(1+i),Game.scale,null);
            else
                g2d.drawImage(heart_empty,Game.scale*(1+i),Game.scale,null);
        }
        
        g2d.drawImage(key_interface,key_at,null);
        g2d.setColor(java.awt.Color.white);
        g2d.setFont(Game.fontBig);
        g2d.drawString("x"+keys,(int)(Game.scale*(Game.gamesizex+Game.scrollboarder+0.5)),(int)(Game.scale*(Game.scrollboarder*0.5+0.25)));
        g2d.setFont(Game.fontSmall);
        g2d.drawString("/"+Game.max_keys,(int)(Game.scale*(Game.gamesizex+Game.scrollboarder+1.7)),(int)(Game.scale*(Game.scrollboarder*0.5+0.25)));
    }
    
    public float getAlpha(){
        float frequency = 4;
        return 1-2*distanceNextInt(((invincibleTimetoPass+0f)/(invincibleTime+0f))*(frequency+1));
    }
    
    public void delete(){
        System.out.println("Cant delete Player");
    }

}
