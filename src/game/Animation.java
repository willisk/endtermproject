
package game;

import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Animation {
    
    private Image[] list;
    private int[] dList;
    private String[] files;
    private int[] scalex, scaley;
    private int iterator,iteration;
    private boolean skipFirstFrame, isFirstFrame;
    
    public Animation(){
        list = new Image[0];
        dList = new int[0];
        skipFirstFrame = false;
        files = new String[0];
        scalex = new int[0];
        scaley = new int[0];
        reset();
        
    }
    
    public void update(){
        
        //System.out.println("or: "+iterator+" on: "+iteration+" hf: "+hasFinished()+" ff: "+isFirstFrame);
        
        if(skipFirstFrame&&isFirstFrame){
            iterator++;
        }
        else
            iteration++;
        
        if(iteration>=dList[iterator]){
            iteration = 0;
            iterator++;
        }
        
        if(iterator>=list.length)
            iterator = 0;
        
        isFirstFrame=false;
    }
    
    public Image getImage(){
        return list[iterator];
    }
    
    public boolean hasFinished(){
        return !isFirstFrame && iterator == 0 && iteration == 0;
    }
    
    public void reset(){
        iterator = 0;
        iteration = 0;
        isFirstFrame=true;
    }
    
    public void loadImages(){
        for(int i=0;i<list.length;i++)
            list[i] = BufferImageResized(files[i],scalex[i]*Game.scale,scaley[i]*Game.scale);
    }
    
    public void addAnimation(String file, int sizex, int sizey, int duration){
        Image[] newlist = new Image[list.length+1];
        int[] newdList = new int[dList.length+1];
        String[] newFiles = new String[files.length+1];
        int[] newScalex = new int[scalex.length+1];
        int[] newScaley = new int[scaley.length+1];
        for(int i=0;i<list.length;i++){
            newlist[i]=list[i];
            newdList[i]=dList[i];
            newFiles[i]=files[i];
            newScalex[i]=scalex[i];
            newScaley[i]=scaley[i];
        }
        newlist[list.length]=null;
        newdList[dList.length]=duration;
        newFiles[files.length]=file;
        newScalex[scalex.length]=sizex;
        newScaley[scaley.length]=sizey;
        list = newlist;
        dList = newdList;
        files = newFiles;
        scalex = newScalex;
        scaley = newScaley;
    }
    
    public void addAnimationSet(String file, int sizex, int sizey, int duration, int setLength){
        for(int i=1;i<=setLength;i++)
            addAnimation(file+"_"+i,sizex,sizey,duration);
    }
    
    public void skipFirstFrame(){
        skipFirstFrame = true;
    }
    
    
    public static Image BufferImageResized(String filename, int sizex, int sizey){
        Image image = null;
        try {                
            image = ImageIO.read(Game.class.getResource("images/"+filename+".png"))
                  .getScaledInstance(sizex, sizey, image.SCALE_DEFAULT);
        } catch (IOException e) {
            System.out.println("Image read Error");
            e.printStackTrace();
        }
        
        return image;
    }
    
    public int getAnimationLength(){
        int length = 0;
        
        for(int i=0;i<dList.length;i++)
            length+=dList[i];
        
        return length;
    }
    
}
