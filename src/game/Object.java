
package game;

//import com.googlecode.lanterna.screen.Screen;
import java.awt.*;


class AnimationException extends Exception{
    
    AnimationException(String anim){
        super("ERROR: Animation '"+anim+"' could not be found");
    }
}

public abstract class Object {
    protected float[] coords, move;
    protected float size;
    protected int type;
    protected Game game;
    
    protected Animation[] animList;
    protected String[] animAlias;
    protected int currentAnim;
    protected int temporaryAnim;
    
    Object( Game game, int x, int y, float size, int type ){
        
        this.coords = newCoords(x,y);
        this.move = newCoords(0,0);
        this.game=game;
        this.size=size;
        this.type=type;
        
        animList = new Animation[0];
        animAlias = new String[0];
        currentAnim = -1;
        temporaryAnim = -1;
        
    }
    
    public void update(){
        move();
    }
    
    public static void loadImages(){
    }
    
    public void move(){
        if(!game.paused){
            coords[0]+=move[0];
            coords[1]+=move[1];
        }
    }
    
    public void movestop(){
        move[0]=0;
        move[1]=0;
    }
    
    public void forcePos(float x, float y){
        coords[0]=x;
        coords[1]=y;
    }
    
    public float getX(){
        return coords[0];
    }
    
    public float getY(){
        return coords[1];
    }
    
    public float getSize(){
        return size;
    }
    
    public String getID(){
        return "";
    }
    
    public int getDamage(){
        return 0;
    }
    
    public boolean compareID(String ID){
        return false;
    }
    
    public boolean isMoving(){
        return move[0]!=0 || move[1]!=0;
    }
    
    public boolean isHurt(){
        return false;
    }
    
    
    public boolean isAlive(){
        return true;
    }
    
    
    public void damage(int n){
    }
    
    public boolean walkable(){
        return true;
    }
    
    abstract void delete(); //NOTE: unused
    
    abstract void paint(Graphics2D g2d,float offsetx,float offsety);
    //abstract void paint(Screen screen,float offsetx,float offsety);
    
    
    //utility
    
    
    public static float[] newCoords(float x, float y){
        float[] newcoords = {x,y};
        return newcoords;
    }
    
    public static float[] vectorScale(float[] vec, float scale){
        return newCoords(vec[0]*scale,vec[1]*scale);
    }
    
    public static float[] vectorAdd(float[] vec1, float[] vec2){
        return newCoords(vec1[0]+vec2[0],vec1[1]+vec2[1]);
    }
    
    public static float abs(float f){
        return (f>=0)?f:f*-1;
    }
    
    public static int round(float x){
        return (x>=0.5f+(int)x)?(int)(x+1):(int)x;
    }
    
    public static int roundInt(float x){
        return (x<0)?(int)(x-1):(int)x;
    }
    
    public float length(float[] vec){
        return (float)Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]);
    }
    
    
    public float[] randomNormedVec(){
        float[] vec = {(float)Math.random(),(float)Math.random()};
        return vectorScale(vec,1/length(vec));
    }
    
    
    public float distanceNextInt(float t){
        t-=(int)t;
        if(t>0.5)
            return 1-t;
        return t;
    }
    
    
    
    
    //Animation
    
    public void loadAnimationImages(){
        for(int i=0;i<animList.length;i++)
            animList[i].loadImages();
        
    }
    
    public Animation addAnimationSet(String file, String alias, int scalex, int scaley, int duration, int setLength){
        Animation anim = new Animation();
        anim.addAnimationSet(file,scalex,scaley,duration,setLength);
        addAnimation( anim, alias );
        return anim;
    }
    
    public void addAnimation( Animation anim, String alias ){
        Animation[] newlist = new Animation[animList.length+1];
        String[] newAlias = new String[animAlias.length+1];
        for(int i=0;i<animList.length;i++){
            newlist[i]=animList[i];
            newAlias[i]=animAlias[i];
        }
        newlist[animList.length]=anim;
        newAlias[animList.length]=alias;
        animList = newlist;
        animAlias = newAlias;
    }
    
    
    
    public void animUpdate(){
        animList[currentAnim].update();
        if( temporaryAnim >=0 )
            if( animList[currentAnim].hasFinished() ){
                setAnimation(temporaryAnim);
                temporaryAnim = -1;
            }
    }
    
    public void animReset(){
        animList[currentAnim].reset();
    }
    
    public void setTemporaryAnimation( String anim ){
        temporaryAnim = currentAnim;
        setAnimation(anim);
    }
    
    public void setAnimation( String anim ){
        setAnimationNoReset(anim);
        animList[currentAnim].reset();
    }
    
    public void setAnimation( int anim ){
        setAnimationNoReset(anim);
        animList[currentAnim].reset();
    }
    
    public void setAnimationNoReset( String anim ){
        int animNr = -1;
        try{
            animNr = getAnimationNr(anim);
        } catch (AnimationException e) {
            e.printStackTrace();
            return;
        }
        
        currentAnim = animNr;
    }
    
    public void setAnimationNoReset( int anim ){
        currentAnim = anim;
    }
    
    public int getAnimationNr( String anim ) throws AnimationException{
        
        int nr = -1;
        
        for(int i=0;i<animAlias.length;i++){
            if(animAlias[i].compareTo(anim)==0)
                nr = i;
        }
        
        if(nr==-1)
            throw new AnimationException(anim);
        
        return nr;
    }
    
    public Image getAnimationImage(){
        return animList[currentAnim].getImage();
    }
    
    public String getCurrentAnimationAlias(){
        return animAlias[currentAnim];
    }
    
    public int getAnimationLength( String alias ){
        
        int length = 0;
        
        try{
            length = animList[getAnimationNr( alias )].getAnimationLength();
        }
        catch( AnimationException e ){}
        
        return length;
    }
    
}
