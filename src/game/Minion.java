

package game;

//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.terminal.Terminal;
import static game.Object.round;
import java.awt.Graphics2D;
import java.awt.*;


public class Minion extends Object{
    
    private int health;
    private static int damage;
    private final float steps = 0.05f;
    // float[] moveDir = {0,0};
    private final int thinkTime = 8;
    private int thinkTimetoPass = 0;
    private final int damageFeedbackTime = 20;
    private int damageFeedback = 0;
    private String ID;
    
    
    public Minion(Game game,int x, int y, String ID){
        super(game,x,y,1f,game.TypeMinion);
        
        health = 4;
        this.ID = ID;
        thinkmovedir();
        
        //NOTE: make this static, so it will only be loaded once
        addAnimationSet("slime/slime","walk",1,2,5,16);
        setAnimation("walk");
        loadAnimationImages();
    }
    
    public void update(){
        move();
        
        if(damageFeedback>0)
            damageFeedback--;
        
        if(isMoving())
            animUpdate();
        else
            animReset();
    }
    
    
    public boolean isMoving(){
        return thinkTimetoPass==0;
    }
    
    public static void setDamage( int newDamage ){
        damage = newDamage;
    }
    
    
    public boolean compareID(String ID){
        return getID().compareTo(ID)==0;
    }
    
    public String getID(){
        return ID;
    }
    
    public void move(){
        
        if( thinkTimetoPass == 0){
            
            float[] newcoords = newCoords(getX() + move[0]*steps,getY() + move[1]*steps);
            
            boolean walkable = game.collisionTrace(newcoords,size);
            
            Object[] obj = game.objectTrace(newcoords,size,(Object)this);
            
            for(int i=0;i<obj.length;i++)
                if(obj[i].type==4){
                    walkable=false;
                    if(obj[i]==game.getPlayer()){
                        if(obj[i].isHurt())
                            walkable=true&&walkable;
                        game.getPlayer().damage(damage);
                    }
                }
            
            if(walkable)
                updatePos(vectorAdd(coords,vectorScale(move,steps)));
            else
            {
                move = vectorScale(move,-1);
                thinkTimetoPass = thinkTime;
            }
        
        } else {
            thinkTimetoPass--;
        }
            
    }
    
    public void updatePos(float[] coords){
        this.coords = coords;
        
        
    }
    
    
    public boolean isAlive(){
        return health>0;
    }
    
    public void damage(int damage){
        health-=damage;
        damageFeedback = damageFeedbackTime;
        if(!isAlive())
            kill();
    }
    
    public int getDamage(){
        return damage;
    }
    
    
    public void kill(){
        game.removeObject(this);
    }
    
    public void paint(Graphics2D g2d, float offsetx, float offsety) {
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
        
        g2d.drawImage(getAnimationImage(),(int)((getX()+game.scrollboarder+offsetx)*game.scale),(int)((getY()-1+game.scrollboarder+offsety)*game.scale),null);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        
    }
    
    public float getAlpha(){
        float frequency = 2;
        return 1-2*distanceNextInt(((damageFeedback+0f)/(damageFeedbackTime+0f))*(frequency+1));
    }
    
    public void delete() {
        System.out.println("Deleting Minion");
    }
    
    
    
    public void thinkmovedir(){
        
        int x = game.gamesizex*game.quadrant[0]+(int)getX();
        int y = game.gamesizey*game.quadrant[1]+(int)getY();
        
        int preferredSteps = 6;
        
        int horizontal = 0; //steps of freedom in x direction
        //game.addDebugSquare((game.gamesizex*game.quadrant[0]+x),y,1,500);
        for(int k=x, ke = k+5;k<ke && game.walkable(game.getType(k,y));k++)
            horizontal++;
        
        for(int k=x-1, ke = k-5;k>ke && game.walkable(game.getType(k,y));k--)
            horizontal++;
        
        int vertical = 0; //steps of freedom in y direction
        
        for(int n=y, ne = n+5;n<ne && game.walkable(game.getType(x,n));n++)
            vertical++;
        
        for(int n=y-1, ne = n-5;n>ne && game.walkable(game.getType(x,n));n--)
            vertical++;
        
        if( (x + y)%2==0 ){ //supposed to move horizontal, unless the number of possible moves is eq/less than preferredSteps &&..
            if( horizontal <= preferredSteps && horizontal < vertical)
                move[1] = (y%2==1)?1:-1;
            else
                move[0] = (x%2==1)?1:-1;
        }
        else{
            if( vertical <= preferredSteps && vertical < horizontal)
                move[0] = (x%2==1)?1:-1;
            else
                move[1] = (y%2==1)?1:-1;
        }
        //System.out.println(horizontal+" "+vertical+" "+move[0]+" "+move[1]);
        
    }

}
