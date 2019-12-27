/*
Credits

 //https://github.com/monceaux/java-mjpeg
 //https://github.com/intrack/BoofCV-master
 //https://github.com/plantuml/plantuml-mit
 //https://github.com/mattdesl/slim
 
 */
  
package org.smp.player;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.BufferUtils;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
 

/**
 *
 * @author xxx
 */
public class SimpleMediaPlayer {
    
    //
    Application app;
    private String screenName="";
    //def qHD
    private int movieWidth=960;
    private int movieHeight=540;
    //Indicates if video in online
    private boolean playing=false;
    //Indicates if player is listening to audio being ready
    private boolean syncing=false;
    //Main texture used for playing video
    private Texture2D texture;
    //Temp empty image
     private Image emptyImage = new Image(Image.Format.ABGR8, 1, 1, BufferUtils.createByteBuffer(4), ColorSpace.sRGB);
     //Video file stream  
    private InputStream videoStream;
    //COnverter
    private AWTLoader aWTLoader = new AWTLoader();
  
     
    //Main audio player
    private AudioNode audioBG;
    private   Material screenMat;
    //Main list for all the frames - raw data
    ArrayList<byte[]> frames=new ArrayList();
   //debug 
   //private int frameCount=0;
  // private int count=0;
   private long startTime=0;
   private long pauseTime=0;
    private long pausePeriod=0;
   
   private long timeSinceStart=0;
   private float fps=1f/25f;
   private int currentFrameIndex=-1;
   private int prevFrameIndex=-1;
   private boolean running=false;
   private String videoAssetPath;
   private String audioAssetPath;
   private String idleImageAssetPath;
   private String loadingImageAssetPath;
   private String pausedImageAssetPath;
   private ColorRGBA screenColor;
   private boolean paused=false;
   //Loading variables
    private  Future  loadingResult;
   private boolean loading=false;
   private boolean playOnLoad=false;
   private boolean loaded=false;
   //

    /**
     *
     */
   static public int PB_MODE_ONCE=0;

    /**
     *
     */
    static public  int PB_MODE_LOOP=1;
    
   private int playBackMode=PB_MODE_ONCE;
   //Node used in state
    private Node guiNode;
   //Internal listener 
    private VideoScreenListener videoScreenListener=null;
     //
    ExecutorService executor;
    LoadingTask loadingTask;
    //Targets
    private final int DISPLAY_UNSET=-1;
    private final int DISPLAY_MODE_GEOMETRY=0;
    private final int DISPLAY_MODE_STATE=1;
    private final int DISPLAY_MODE_MATERIAL=2;
    private int displayMode=DISPLAY_UNSET;
    //
    private Geometry geometry=null;
    private BaseAppState state=null;
    private Material material=null; 
    //
    private boolean isAndroid=false;

    /**
     *Main contructor
     * @param app
     */
    public SimpleMediaPlayer(Application app  )
  {
      this.app=app;   
      //Ugly way to detects if run on android - matters in setImage
     //  isAndroid=detectAndroid();
  }
      
    /**
     * Generates a geometry with indicated parameters. Fixes the player with this kind of object. Any next call returns the same object. 
     * @param screenName - unique name for Quad
     * @param movieWidth - width of the quad
     * @param movieHeight - height of the quad
     * @param idleImageAssetPath - image to display when nothing is loaded. Null if you want to use screenColor
     * @param loadingImageAssetPath - image to display when video is loading. Null if you want to use screenColor
     * @param pausedImageAssetPath - image to display when video is paused. Null if you want to display last frame.
     * @param screenColor - color to use if any idleImageAssetPath or loadingImageAssetPath is null.
     * @param videoAssetPath - path to video(mjpg) file in assets for example Media/video.mjpg. Cannot be null.
     * @param audioAssetPath - path to audio(ogg) file in assets for example Media/audio.ogg. Null for no audio.
     * @param framesPerSec - FPS for the video file. Should be consistent with the source FPS. In most cases 25 or 30. 
     * @param playBackMode - decides what to do after video is finished. Use predefined PB_MODE_ONCE or PB_MODE_LOOP
     * @param alpha - alpha for the whole material. Useful for displaying in GUI.
     * @return Geometry containing the quad(the same object for subsequent calls ) or null if other display mode was used before.
     */
    public Geometry  genGeometry( String screenName,int movieWidth, int movieHeight,String idleImageAssetPath,String loadingImageAssetPath,String pausedImageAssetPath,ColorRGBA screenColor, String videoAssetPath, String audioAssetPath, int framesPerSec, int playBackMode, float alpha)
       {
           //No type change allowed
           if(displayMode==DISPLAY_MODE_STATE || displayMode==DISPLAY_MODE_MATERIAL)
               return null;
           
           if(displayMode==DISPLAY_MODE_GEOMETRY)
              return geometry;
           
           //Sets display mode
           displayMode=DISPLAY_MODE_GEOMETRY;
          
            // Common actions
           this.screenName=screenName;
           this.movieWidth =movieWidth;
           this.movieHeight =movieHeight;
           initMediaPlayer(    idleImageAssetPath,  loadingImageAssetPath,  pausedImageAssetPath,  screenColor,   videoAssetPath,   audioAssetPath,   framesPerSec,   playBackMode,   alpha);
             //Geometry init  
           geometry=new Geometry(screenName,new Quad( movieWidth  , movieHeight  ));
            //set material generated in initMediaPlayer
           geometry.setMaterial(screenMat);
            //
           return geometry;
        }
  
    /**
     * Generates a material with indicated parameters. Fixes the player with this kind of object. Any next call returns the same object. 
     * @see org.smp.player.Class#genGeometry()
     * @return Material contating the media(the same object for subsequent calls ) or null if other display mode was used before.
     */
    public Material  genMaterial(  String idleImageAssetPath,String loadingImageAssetPath,String pausedImageAssetPath,ColorRGBA screenColor, String videoAssetPath, String audioAssetPath, int framesPerSec, int playBackMode, float alpha)
       {
           //No type change allowed
           if(displayMode==DISPLAY_MODE_STATE || displayMode==DISPLAY_MODE_GEOMETRY)
               return null;
           
           if(displayMode==DISPLAY_MODE_MATERIAL)
              return material;
           
             
           //Sets display mode
           displayMode=DISPLAY_MODE_MATERIAL;
           
            // Common actions
           initMediaPlayer(   idleImageAssetPath,  loadingImageAssetPath,  pausedImageAssetPath,  screenColor,   videoAssetPath,   audioAssetPath,   framesPerSec,   playBackMode,   alpha);
            //set material  
          material=screenMat;
            //
           return material;
        }
  
    /**
    * Generates state with indicated parameters. Fixes the player with this kind of object. Any next call returns the same object. 
     * @param node - Node to add the geometry in the state. In general is should be the guiNode.
     * @param movieWidth -  Original movie dimentions - relevant only for keeping aspect ratio.   
     * @param movieHeight - @see movieWidth.
     * @param keepAspect - True if aspect ratio should be kept. False is the movie should be stretched to the screen. screenColor is used for background color 
     * @see org.smp.player.Class#genGeometry().
     * @return State to display the media(the same object for subsequent calls ) or null if other display mode was used before.
     */
   public  BaseAppState  genState(Node node,int movieWidth, int movieHeight,boolean keepAspect, String screenName,String idleImageAssetPath,String loadingImageAssetPath,String pausedImageAssetPath,ColorRGBA screenColor, String videoAssetPath, String audioAssetPath, int framesPerSec, int playBackMode, float alpha)
       {
           
             //No type change allowed
           if(displayMode==DISPLAY_MODE_MATERIAL || displayMode==DISPLAY_MODE_GEOMETRY)
               return null;
           
           if(displayMode==DISPLAY_MODE_STATE )
              return state;
            
            //Sets display mode
           displayMode=DISPLAY_MODE_STATE;
           //Calculates dimentions of the geometry - 
            int width=app.getCamera().getWidth();
            int height=app.getCamera().getHeight();
            //if keepAspect true the screen is not strchted. Instead it is centered according to the width. ScreenColor is used to fill the screen
            float scaleRatio=1.0f;
            if(keepAspect)
              {
                   scaleRatio=((float)width)/((float)movieWidth);
                // width=(int)(width*scaleRatio);
                  height=(int)(height*scaleRatio);
                  //Background color
                  app.getViewPort().setBackgroundColor(screenColor);
              }
             
           // Common actions
           this.screenName=screenName;
           this.movieWidth =width;
           this.movieHeight =height;
           initMediaPlayer(   idleImageAssetPath,  loadingImageAssetPath,  pausedImageAssetPath,  screenColor,   videoAssetPath,   audioAssetPath,   framesPerSec,   playBackMode,   alpha);
             //Geometry init  
           geometry=new Geometry(screenName,new Quad( width  , height  ));
           //set material generated in initMediaPlayer
           geometry.setMaterial(screenMat);
           //Add
           this.guiNode=node;
          //shifts if aspect is enabled
         if(keepAspect)
            geometry.setLocalTranslation(0, (app.getCamera().getHeight()-height)/2  , 0);
           
             //Gen state
            state=new BaseAppState() {
               @Override
               protected void initialize(Application app) {
                }

               @Override
               protected void cleanup(Application app) {
               }

               @Override
               protected void onEnable() {
                   guiNode.attachChild(geometry);
                   loadAndPlayMedia();
                }

               @Override
               protected void onDisable()
                {
                     stopMedia(); 
                     guiNode.detachChild(geometry);
                   
                }
           };
           
            //
           return state;
        }
 
 /**
  * Common actions for all generators.
  * @see org.smp.player.Class#genGeometry()
  */   
private void initMediaPlayer( String idleImageAssetPath,String loadingImageAssetPath,String pausedImageAssetPath,ColorRGBA screenColor, String videoAssetPath, String audioAssetPath, int framesPerSec, int playBackMode, float alpha)
{
    
          //Basic param testing
          if (videoAssetPath==null) 
            throw new IllegalArgumentException("videoAssetPath cannot be null");
   
          
           this.videoAssetPath=videoAssetPath;
           this.audioAssetPath=audioAssetPath;
           this. idleImageAssetPath=idleImageAssetPath;
           this.loadingImageAssetPath=loadingImageAssetPath;
           this.pausedImageAssetPath=pausedImageAssetPath;
           this.screenColor=screenColor;
         
           this.playBackMode=playBackMode;
           //calculate time for single frame
           fps=1f/framesPerSec;
             
          //material and texture
          // screenMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            screenMat = new Material(app.getAssetManager(), "MatDefs/SimpleMediaPlayer/SimpleMediaPlayer.j3md");
          
            //Set transparency
            if(alpha<1.0f)
               {
                 screenMat.setFloat("Alpha", alpha);
                 screenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); 
               }
            
           //Default color - back
            if(screenColor==null)
                screenColor=ColorRGBA.Black;
           //Initial idle
           if(idleImageAssetPath!=null) 
             screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(idleImageAssetPath));
           else
               screenMat.setColor("Color",screenColor);
}

    /**
     * Sets new data (video and audio) for the player. Used mainly with genGeometry or genMaterial. Required reloading to take effect. 
     * @param videoAssetPath
     * @param audioAssetPath
     */
    public void setMedia(String videoAssetPath, String audioAssetPath)
 {
    this.videoAssetPath=videoAssetPath;
    this.audioAssetPath=audioAssetPath;
 }
    
    /**
     *
     * @return Screen name
     */
    public String getScreenName()
 {
     return screenName;
 }

    /**
     *Sets alpha for the display
     * @param alpha
     */
    public void setAlpha(float alpha)
 {
     if(alpha<1.0f)
       {
        screenMat.setFloat("Alpha", alpha);
        screenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); 
       }
 }
    
    /**
     *
     * @return width of the quad
     */
    public int getWidth()
 {
   return movieWidth ;
    
 }

    /**
     *
     * @return height of the quad
     */
    public int getHeight()
 {
   return  movieHeight;
 }

    /**
     *
     * @return material sed for displaying media regardless of chosen display mode. 
     */
    public Material getMaterial()
{
    return screenMat;
}

    /**
     *Sets volume for audio
     * @param newVolume
     */
    public void setAudioVolume(float newVolume)
    {
        if(audioAssetPath!=null)  
          audioBG.setVolume(newVolume);
    }
    private InputStream  openAsset(String assetname, AssetManager assetManager)
    {
         AssetInfo aInfo = assetManager.locateAsset(new AssetKey(assetname));
        // //1////System.out.println("Asset Info = " + aInfo.getKey().);
         return  aInfo.openStream();
    }
 private void startMedia ()
       {
      
         //If audio is present starts syncing - listening for audio to start
         if(audioAssetPath!=null)  
         {
               //enable syncing 
             syncing=true;
             //play audio
             audioBG.play();
         }
         else
             startVideo();
       
       
    }
  
private void syncAudioAndVideo()
{
      
     if(audioBG.getStatus()==AudioSource.Status.Playing)
       {
            //disable testing
           syncing=false;
           //play video
           startVideo();
        
       }
}    

private void  startVideo()
{
         //inform listener
         if(videoScreenListener!=null)
           videoScreenListener.onPrePlay(screenName);   
            
        //remove static image
         screenMat.clearParam("Color");
         //set image th1at will receive data from runner via emptyImage
         screenMat.setTexture("ColorMap",  getTexture());
        // 
         setupRunner();
        //
        playing=true;
}
 private boolean playFrame()
   {
         
        return calcFrame();
    }

//Main method to convert jpg to texture.    
private void setImage(byte[] jpegBytes)
      {
   
           // //1////System.out.println("SET="+ jpegBytes.length);
            if(jpegBytes==null)
                return;
            BufferedImage image=null;
            //If desktop      
           // if(!isAndroid)
               {    
                 try {
                      image = ImageIO.read(new ByteArrayInputStream(jpegBytes));
                     } 
                 catch (IOException ex) 
                       {
                        ex.printStackTrace();
                        return;
                       }
               }
            // else
            //    {
            //        //TODO
            //        //https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-android/src/main/java/com/jme3/texture/plugins/AndroidBufferImageLoader.java
            //    }
           
            //Powrot do tex
            texture.setImage(aWTLoader.load(image, true));
             
   }
   /**
    * Android Runtime or OpenJDK/Oracle Runtime Environment
    * @return true if run on Android 
    */
   private boolean detectAndroid()
   {
     //  System.out.println("SSS="+System.getProperty("java.runtime.name"));
     String data= System.getProperty("java.runtime.name");
      return data!=null &&  System.getProperty("java.runtime.name").toLowerCase().contains("android");
             
   } 
   
   
      private Texture2D getTexture() {
        if (texture == null)
            {
            texture = new Texture2D(emptyImage);
            }
        return texture;
    }
    
  //stop playback and release or replay  
 private void stopPlayBack()
    {
         //prevents doble stop
        if(!playing)
            return;
        
        //Disable audio and video. Release data
         if(audioAssetPath!=null)   
           audioBG.stop();
         //
         stopRunner();
    
        //Release  
        emptyImage=null;
        playing=false;
        playOnLoad=false;
         syncing=false;
         //clean threads - therwise it may still be alive
          if(loadingResult!=null)
             loadingResult.cancel(true);
          if(executor!=null)
             executor.shutdownNow();   
          
        //If played once - reset idle image. With loop play again
        if(playBackMode==PB_MODE_ONCE )
          {
             //Idle again
            if(idleImageAssetPath!=null)  
               screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(idleImageAssetPath));
            else
               screenMat.setColor("Color",screenColor);
            
             if(videoScreenListener!=null)
               videoScreenListener.onEnd(screenName);
          }
        else if( playBackMode==PB_MODE_LOOP )
            {
            if(videoScreenListener!=null)
               videoScreenListener.onLoopEnd(screenName);
                
             playMedia();
            }
    }
 
 //   

/**
 *Stops media in any state. Also releases any memory reources.
 */
 public void stopMedia()
    {
         //prevents stop if already stopped - unless during laoding
        if(!playing)
          {
           if(loading)   
              cleanLoading(); 
            
            return;
          }
        
        //Disable audio and video. Release data
         if(audioAssetPath!=null)   
           audioBG.stop();
         
         //always release
         stopRunner();
         frames.clear();
         loaded=false;
         
        //Release  
        emptyImage=null;
        playing=false;
         playOnLoad=false;
        syncing=false;
         //clean threads - therwise it may still be alive
          if(loadingResult!=null)
            loadingResult.cancel(true);
          if(executor!=null)
             executor.shutdownNow();   
        
          
        //Idle again
        if(idleImageAssetPath!=null)  
             screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(idleImageAssetPath));
       else
            screenMat.setColor("Color",screenColor);   
         
         if(videoScreenListener!=null)
               videoScreenListener.onEnd(screenName);
         
       
    }
    

    /**
     *Initiatie loading task and play afterwards. Prefered way to start playback. May be splited into andMedia and playMedia 
     */
   public void loadAndPlayMedia()
   {
        //prevents double play, play during loading
        if(playing || loading)
            return;
       
      //if play once - always load. If loop - the first time
        //System.out.println("PALYED ONCE "+frames.size());
         prepareLoading();
         //Also play once loaded 
        playOnLoad=true;
       
     
   }
    //

    /**
     *Initiatie loading task without playing. Use playMedia  afterwards.
     */
   public void loadMedia()
   {
        //prevents double play, play during loading
        if(playing || loading)
            return;
       
        //if play once - always load. If loop - the first time
          prepareLoading();
         //Also play once loaded 
        playOnLoad=false;
       
     
   }
   

    /**
     *Play already loaded media. Do not use for unpausing - no effect.
     */
   public void playMedia()
   {
        //prevents double play, play during loading
        if(playing || loading ||  !loaded)
            return;
           //
          startMedia(); 
         
   }
   
  
    /**
     *
     * @return Is currently loading
     */
   public boolean isLoading()
   {
        return loading;
   }
   
    /**
     *
     * @return Are frames data preloaded. 
     */
   public boolean isLoaded()
   {
        return loaded;
   }
  

    /**
     *
     * @return Is playback in action - playing
     */
   public boolean isPlaying()
   {
        return playing;
   }
   //

    /**
     *
     * @return Is paused
     */
   public boolean isPaused()
   {
        return paused;
   }
   private void prepareLoading()
   {
         //inform listener
      if(videoScreenListener!=null)
         videoScreenListener.onPreLoad(screenName);
       //Loading image
      
        if(loadingImageAssetPath!=null)
         screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(loadingImageAssetPath));
       else
           screenMat.setColor("Color",screenColor);
        
          //init audio - if any 
          if(audioAssetPath!=null)
            {
            audioBG = new AudioNode(app.getAssetManager(), audioAssetPath, AudioData.DataType.Buffer);
            audioBG.setPositional(false);
            audioBG.setLooping(false);
            audioBG.setVolume(1);
            }
          
          //For async loading - each time
          executor = Executors.newSingleThreadExecutor();
          //Init loading. Check in update
          loadingTask=new LoadingTask();
        
         //
         loadingResult =executor.submit(loadingTask);
         //start testing for loaded in update
         loading=true;
       
   }

    /**
     *Pauses the media. Dispalys predefined image or last frame. Unpause with Unpause and not Play
     */
    public void pauseMedia()
   {
       //cannot pause not playing 
        if(!playing)
           return;
        
       if(paused)
           return;
     
       //Paused screen or last frame
       if(pausedImageAssetPath!=null) 
          screenMat.setTexture("ColorMap", app.getAssetManager().loadTexture(pausedImageAssetPath));
          
       
       if(audioAssetPath!=null)
           audioBG.pause();
       
      paused=true;          
       pauseTime=System.currentTimeMillis();
   }

    /**
     *Unpuses paused media
     */
    public void unpauseMedia()
   {
       if(!paused)
           return;
       paused=false;    
       pausePeriod=pausePeriod+System.currentTimeMillis()-pauseTime;
       //reestablish texture
        screenMat.setTexture("ColorMap",  getTexture());
        
      if(audioAssetPath!=null)
         audioBG.play();
   }

    /**
     *Main update method used to display images. Must be called manually from parent object.
     * @param tpf - delta
     */
    public void update(float tpf)
        {
            //////////////////LOADING///////////////
          if(loading)
             {
                 
                //check if the loading is complete
                 if(loadingResult.isDone())
                   {
                   
                     if(!loaded)
                        {
                        //clean
                         cleanLoading();
                        //Loaded
                         loaded=true;
                       //inform listener
                        if(videoScreenListener!=null)
                         videoScreenListener.onLoaded(screenName);
                       //Play if not waiting for play -  starts audio and waits for syncing  
                        if(playOnLoad)
                           startMedia();
                        }
                   }
             }
          /////////////////////PLAYBACK//////////////  
          //Wait for the audio to start
          if(syncing)
            {
              syncAudioAndVideo();
              return;
            }
         //Do not play if not ON   
         if(!playing)
            return;
          
         //PLAY - true if any frame was retrieved and played
         boolean isPlayed=  playFrame();
              
         //if end - it is already stop by setEnd. Here just smol last cleanup
        if(!isPlayed)    
            {
               
            }
        }
   
 //  
 private void cleanLoading()
 {
      //kill task 
    loadingResult.cancel(true);
    executor.shutdownNow(); 
    //not laoding
   loading=false;
 }
 
 //////////////////EFFECTS////////////

    /**
     * Enables or disables VHS effect - small line glitches
     * @param enabled
     */
 public void enableVHSEffect(boolean enabled)
 {
     if(enabled)
        screenMat.setBoolean("EnabledVHS", enabled);
      else
        screenMat.clearParam("EnabledVHS");
   }

   /**
     * Enables or disables Line effect - wide line moving vertically
     * @param enabled
     */
    public void enableLineEffect(boolean enabled)
 {
     if(enabled)
        screenMat.setBoolean("EnabledLine", enabled);
      else
        screenMat.clearParam("EnabledLine");
   }

     /**
     * Enables or disables Grain effect - white noise
     * @param enabled
     */
    public void enableGrainEffect(boolean enabled)
 {
     if(enabled)
        screenMat.setBoolean("EnabledGrain", enabled);
      else
        screenMat.clearParam("EnabledGrain");
   }

    /**
     * Enables or disables scanline effect - tv lines across the screen
     * @param enabled
     */
    public void enableScanlineEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledScanline", enabled);
      else
        screenMat.clearParam("EnabledScanline");
   }

   /**
     * Enables or disables vignette effect  
     * @param enabled
     */
    public void enableVignetteEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledVignette", enabled);
      else
        screenMat.clearParam("EnabledVignette");
   }
    /**
     * Enables or disables LCD effect  - weak pixelization
     * @param enabled
     */
    public void enableLCDEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledLCD", enabled);
      else
        screenMat.clearParam("EnabledLCD");
   }
 
    /**
     * Enables or disables CRT effect  - old monitor pixelization
     * @param enabled
     */
    public void enableCRTEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledCRT", enabled);
      else
        screenMat.clearParam("EnabledCRT");
   }

     /**
     * Enables or disables Glitch effect  -  strong disturbances
     * @param enabled
     */
    public void enableGlitchEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledGlitch", enabled);
      else
        screenMat.clearParam("EnabledGlitch");
   }
    
     /**
     * Enables or disables Black and White effect  
     * @param enabled
     */
    public void enableBlackAndWhiteEffect(boolean enabled)
   {
     if(enabled)
        screenMat.setBoolean("EnabledBAW", enabled);
      else
        screenMat.clearParam("EnabledBAW");
   }
 
 //////////////////////////LOADER/////////////

   
   private class LoadingTask implements Runnable {

       
        @Override
    public void run()   {
       
           
        //Open file
        videoStream=openAsset(videoAssetPath,app.getAssetManager());
        //MJPEg codec
        VideoMjpegCodec videoMjpegCodec=new VideoMjpegCodec();
        //read all frames at once
       videoMjpegCodec.read( videoStream,frames ); 
         
    }
    
    
        
}
   
 
 ///////////////////////RUNNER///////////////////
    
     private void setupRunner() {
        startTime=System.currentTimeMillis();
      //   //System.out.println("STARTa "+frames.size());
        running=true;
        }

    private void stopRunner() {
         running=false;
         paused=false;
         pausePeriod=0;
         if(playBackMode==PB_MODE_ONCE) 
           {
             frames.clear();
             loaded=false;
           }
        }

   
 
 private boolean calcFrame()
    {
                
     if(running && !paused)
        { 
             
            //
                  timeSinceStart= ((System.currentTimeMillis()-startTime)-pausePeriod);
                  //
		  //for(int a=0;a<frames.size();a++)
                  //    viewer.setImage(frames.get(a)); 
                      currentFrameIndex =(int)((timeSinceStart/fps)/1000);
                      
                      //
                      if(currentFrameIndex==prevFrameIndex)
                        return true;
                      
                     
                      //new frame
                      prevFrameIndex=currentFrameIndex;
                     // if(prevFrameIndex<=30)
                     //    frameCount++;
                     if(currentFrameIndex>=frames.size())
                        {
                              stopPlayBack();
                        } 
                      else
                        setImage(frames.get(currentFrameIndex)); 
                     
        }
      //1////System.out.println("rrunning="+running );
               
       return running;
     }
 
 //////////////////////RUNNER END///////////////
 
 ///////////////////////DECODEC////////////////////
 private class VideoMjpegCodec {
	// start of image
	public static final byte SOI = (byte)0xD8;
	// end of image
	public static final byte EOI = (byte)0xD9;

	public void  read( InputStream streamIn, List<byte[]> ret ) {
		// read the whole movie in at once to make it faster
                try {
			byte[] b = convertToByteArray(streamIn);
 

			DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));

			while( findMarker(in,SOI) && in.available() > 0 ) {
				byte data[] = readJpegData(in, EOI);
				ret.add(data);
			}
		} catch (IOException e) {
		}
		 
	}

	/**
	 * Read a single frame at a time
	 */
	public byte[] readFrame( DataInputStream in ) {
		try {
			if( findMarker(in,SOI) && in.available() > 0 ) {
				return readJpegData(in, EOI);
			}
		} catch (IOException e) {}
		return null;
	}


	public   byte[] convertToByteArray(InputStream streamIn) throws IOException {
		ByteArrayOutputStream temp = new ByteArrayOutputStream(1024);
		byte[] data = new byte[ 1024 ];
		int length;
		while( ( length = streamIn.read(data)) != -1 ) {
			temp.write(data,0,length);
		}
		return temp.toByteArray();
	}

	private boolean findMarker( DataInputStream in , byte marker ) throws IOException {
		boolean foundFF = false;

		while( in.available() > 0 )  {
			byte b = in.readByte();
			if( foundFF ) {
				if( b == marker ) {
					return true;
				} else if( b != (byte)0xFF )
					foundFF = false;
			} else if( b == (byte)0xFF ) {
				foundFF = true;
			}
		}
		return foundFF;
	}

	private byte[] readJpegData(DataInputStream in, byte marker) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

		// add the SOI marker back into it
		bout.write(0xFF);
		bout.write(SOI);

		boolean foundFF = false;

		while( in.available() > 0 ) {
			byte d = in.readByte();
			if( foundFF ) {
				if( d == marker )
					break;
				else {
					bout.write(0xFF);
					bout.write(d);
					foundFF = false;
				}
			} else if( d ==(byte)0xFF ) {
				foundFF = true;
			} else {
				bout.write(d);
			}
		}
		return bout.toByteArray();
	}
     }
  
 ////////////////////DECODEC END/////////////////////
 
 //////////////////ENCODER/////////////////////
 static class MJPEGGenerator
{
    /*
     * Info needed for MJPEG AVI
     * 
     * - size of file minus "RIFF & 4 byte file size"
     */

    int              width          = 0;
    int              height         = 0;
    double           framerate      = 0;
    int              numFrames      = 0;
    File             aviFile        = null;
    FileOutputStream aviOutput      = null;
    FileChannel      aviChannel     = null;

    long             riffOffset     = 0;
    long             aviMovieOffset = 0;

    AVIIndexList     indexlist      = null;

    /** Creates a new instance of MJPEGGenerator */
     private MJPEGGenerator(File aviFile, int width, int height, double framerate, int numFrames) throws Exception
    {
        this.aviFile = aviFile;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.numFrames = numFrames;
        aviOutput = new FileOutputStream(aviFile);
        aviChannel = aviOutput.getChannel();

        RIFFHeader rh = new RIFFHeader();
        aviOutput.write(rh.toBytes());
        aviOutput.write(new AVIMainHeader().toBytes());
        aviOutput.write(new AVIStreamList().toBytes());
        aviOutput.write(new AVIStreamHeader().toBytes());
        aviOutput.write(new AVIStreamFormat().toBytes());
        aviOutput.write(new AVIJunk().toBytes());
        aviMovieOffset = aviChannel.position();
        aviOutput.write(new AVIMovieList().toBytes());
        indexlist = new AVIIndexList();
    }

    public void addImage(java.awt.Image image) throws Exception
    {
        byte[] fcc = new byte[] { '0', '0', 'd', 'b' };
        byte[] imagedata = writeImageToBytes(image);
        int useLength = imagedata.length;
        long position = aviChannel.position();
        int extra = (useLength + (int) position) % 4;
        if(extra > 0)
            useLength = useLength + extra;

        indexlist.addAVIIndex((int) position, useLength);

        aviOutput.write(fcc);
        aviOutput.write(intBytes(swapInt(useLength)));
        aviOutput.write(imagedata);
        if(extra > 0)
        {
            for(int i = 0; i < extra; i++)
                aviOutput.write(0);
        }
        imagedata = null;
        
    }

    public void finishAVI() throws Exception
    {
        byte[] indexlistBytes = indexlist.toBytes();
        aviOutput.write(indexlistBytes);
        aviOutput.close();
        long size = aviFile.length();
        RandomAccessFile raf = new RandomAccessFile(aviFile, "rw");
        raf.seek(4);
        raf.write(intBytes(swapInt((int) size - 8)));
        raf.seek(aviMovieOffset + 4);
        raf.write(intBytes(swapInt((int) (size - 8 - aviMovieOffset - indexlistBytes.length))));
        raf.close();
    }

    // public void writeAVI(File file) throws Exception
    // {
    // OutputStream os = new FileOutputStream(file);
    //
    // // RIFFHeader
    // // AVIMainHeader
    // // AVIStreamList
    // // AVIStreamHeader
    // // AVIStreamFormat
    // // write 00db and image bytes...
    // }

    public   int swapInt(int v)
    {
        return (v >>> 24) | (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
    }

    public   short swapShort(short v)
    {
        return (short) ((v >>> 8) | (v << 8));
    }

    public   byte[] intBytes(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (i >>> 24);
        b[1] = (byte) ((i >>> 16) & 0x000000FF);
        b[2] = (byte) ((i >>> 8) & 0x000000FF);
        b[3] = (byte) (i & 0x000000FF);

        return b;
    }

    public   byte[] shortBytes(short i)
    {
        byte[] b = new byte[2];
        b[0] = (byte) (i >>> 8);
        b[1] = (byte) (i & 0x000000FF);

        return b;
    }

    private class RIFFHeader
    {
        public byte[] fcc      = new byte[] { 'R', 'I', 'F', 'F' };
        public int    fileSize = 0;
        public byte[] fcc2     = new byte[] { 'A', 'V', 'I', ' ' };
        public byte[] fcc3     = new byte[] { 'L', 'I', 'S', 'T' };
        public int    listSize = 200;
        public byte[] fcc4     = new byte[] { 'h', 'd', 'r', 'l' };

        public RIFFHeader()
        {

        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(fileSize)));
            baos.write(fcc2);
            baos.write(fcc3);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc4);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIMainHeader
    {
        /*
         * 
         * FOURCC fcc; DWORD cb; DWORD dwMicroSecPerFrame; DWORD
         * dwMaxBytesPerSec; DWORD dwPaddingGranularity; DWORD dwFlags; DWORD
         * dwTotalFrames; DWORD dwInitialFrames; DWORD dwStreams; DWORD
         * dwSuggestedBufferSize; DWORD dwWidth; DWORD dwHeight; DWORD
         * dwReserved[4];
         */

        public byte[] fcc                   = new byte[] { 'a', 'v', 'i', 'h' };
        public int    cb                    = 56;
        public int    dwMicroSecPerFrame    = 0;                                // (1
                                                                                 // /
                                                                                 // frames
                                                                                 // per
                                                                                 // sec)
                                                                                 // *
                                                                                 // 1,000,000
        public int    dwMaxBytesPerSec      = 10000000;
        public int    dwPaddingGranularity  = 0;
        public int    dwFlags               = 65552;
        public int    dwTotalFrames         = 0;                                // replace
                                                                                 // with
                                                                                 // correct
                                                                                 // value
        public int    dwInitialFrames       = 0;
        public int    dwStreams             = 1;
        public int    dwSuggestedBufferSize = 0;
        public int    dwWidth               = 0;                                // replace
                                                                                 // with
                                                                                 // correct
                                                                                 // value
        public int    dwHeight              = 0;                                // replace
                                                                                 // with
                                                                                 // correct
                                                                                 // value
        public int[]  dwReserved            = new int[4];

        public AVIMainHeader()
        {
            dwMicroSecPerFrame = (int) ((1.0 / framerate) * 1000000.0);
            dwWidth = width;
            dwHeight = height;
            dwTotalFrames = numFrames;
        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(dwMicroSecPerFrame)));
            baos.write(intBytes(swapInt(dwMaxBytesPerSec)));
            baos.write(intBytes(swapInt(dwPaddingGranularity)));
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwTotalFrames)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwStreams)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwWidth)));
            baos.write(intBytes(swapInt(dwHeight)));
            baos.write(intBytes(swapInt(dwReserved[0])));
            baos.write(intBytes(swapInt(dwReserved[1])));
            baos.write(intBytes(swapInt(dwReserved[2])));
            baos.write(intBytes(swapInt(dwReserved[3])));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamList
    {
        public byte[] fcc  = new byte[] { 'L', 'I', 'S', 'T' };
        public int    size = 124;
        public byte[] fcc2 = new byte[] { 's', 't', 'r', 'l' };

        public AVIStreamList()
        {

        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(fcc2);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamHeader
    {
        /*
         * FOURCC fcc; DWORD cb; FOURCC fccType; FOURCC fccHandler; DWORD
         * dwFlags; WORD wPriority; WORD wLanguage; DWORD dwInitialFrames; DWORD
         * dwScale; DWORD dwRate; DWORD dwStart; DWORD dwLength; DWORD
         * dwSuggestedBufferSize; DWORD dwQuality; DWORD dwSampleSize; struct {
         * short int left; short int top; short int right; short int bottom; }
         * rcFrame;
         */

        public byte[] fcc                   = new byte[] { 's', 't', 'r', 'h' };
        public int    cb                    = 64;
        public byte[] fccType               = new byte[] { 'v', 'i', 'd', 's' };
        public byte[] fccHandler            = new byte[] { 'M', 'J', 'P', 'G' };
        public int    dwFlags               = 0;
        public short  wPriority             = 0;
        public short  wLanguage             = 0;
        public int    dwInitialFrames       = 0;
        public int    dwScale               = 0;                                // microseconds
                                                                                 // per
                                                                                 // frame
        public int    dwRate                = 1000000;                          // dwRate
                                                                                 // /
                                                                                 // dwScale
                                                                                 // =
                                                                                 // frame
                                                                                 // rate
        public int    dwStart               = 0;
        public int    dwLength              = 0;                                // num
                                                                                 // frames
        public int    dwSuggestedBufferSize = 0;
        public int    dwQuality             = -1;
        public int    dwSampleSize          = 0;
        public int    left                  = 0;
        public int    top                   = 0;
        public int    right                 = 0;
        public int    bottom                = 0;

        public AVIStreamHeader()
        {
            dwScale = (int) ((1.0 / framerate) * 1000000.0);
            dwLength = numFrames;
        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(fccType);
            baos.write(fccHandler);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(shortBytes(swapShort(wPriority)));
            baos.write(shortBytes(swapShort(wLanguage)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwScale)));
            baos.write(intBytes(swapInt(dwRate)));
            baos.write(intBytes(swapInt(dwStart)));
            baos.write(intBytes(swapInt(dwLength)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwQuality)));
            baos.write(intBytes(swapInt(dwSampleSize)));
            baos.write(intBytes(swapInt(left)));
            baos.write(intBytes(swapInt(top)));
            baos.write(intBytes(swapInt(right)));
            baos.write(intBytes(swapInt(bottom)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIStreamFormat
    {
        /*
         * FOURCC fcc; DWORD cb; DWORD biSize; LONG biWidth; LONG biHeight; WORD
         * biPlanes; WORD biBitCount; DWORD biCompression; DWORD biSizeImage;
         * LONG biXPelsPerMeter; LONG biYPelsPerMeter; DWORD biClrUsed; DWORD
         * biClrImportant;
         */

        public byte[] fcc             = new byte[] { 's', 't', 'r', 'f' };
        public int    cb              = 40;
        public int    biSize          = 40;                               // same
                                                                           // as
                                                                           // cb
        public int    biWidth         = 0;
        public int    biHeight        = 0;
        public short  biPlanes        = 1;
        public short  biBitCount      = 24;
        public byte[] biCompression   = new byte[] { 'M', 'J', 'P', 'G' };
        public int    biSizeImage     = 0;                                // width
                                                                           // x
                                                                           // height
                                                                           // in
                                                                           // pixels
        public int    biXPelsPerMeter = 0;
        public int    biYPelsPerMeter = 0;
        public int    biClrUsed       = 0;
        public int    biClrImportant  = 0;

        public AVIStreamFormat()
        {
            biWidth = width;
            biHeight = height;
            biSizeImage = width * height;
        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(biSize)));
            baos.write(intBytes(swapInt(biWidth)));
            baos.write(intBytes(swapInt(biHeight)));
            baos.write(shortBytes(swapShort(biPlanes)));
            baos.write(shortBytes(swapShort(biBitCount)));
            baos.write(biCompression);
            baos.write(intBytes(swapInt(biSizeImage)));
            baos.write(intBytes(swapInt(biXPelsPerMeter)));
            baos.write(intBytes(swapInt(biYPelsPerMeter)));
            baos.write(intBytes(swapInt(biClrUsed)));
            baos.write(intBytes(swapInt(biClrImportant)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIMovieList
    {
        public byte[] fcc      = new byte[] { 'L', 'I', 'S', 'T' };
        public int    listSize = 0;
        public byte[] fcc2     = new byte[] { 'm', 'o', 'v', 'i' };

        // 00db size jpg image data ...

        public AVIMovieList()
        {

        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc2);
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIIndexList
    {
        public byte[]         fcc = new byte[] { 'i', 'd', 'x', '1' };
        public int            cb  = 0;
        public List<AVIIndex> ind = new ArrayList<AVIIndex>();

        public AVIIndexList()
        {

        }

        @SuppressWarnings("unused")
        public void addAVIIndex(AVIIndex ai)
        {
            ind.add(ai);
        }

        public void addAVIIndex(int dwOffset, int dwSize)
        {
            ind.add(new AVIIndex(dwOffset, dwSize));
        }

        public byte[] toBytes() throws Exception
        {
            cb = 16 * ind.size();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            for(int i = 0; i < ind.size(); i++)
            {
                AVIIndex in = (AVIIndex) ind.get(i);
                baos.write(in.toBytes());
            }

            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIIndex
    {
        public byte[] fcc      = new byte[] { '0', '0', 'd', 'b' };
        public int    dwFlags  = 16;
        public int    dwOffset = 0;
        public int    dwSize   = 0;

        public AVIIndex(int dwOffset, int dwSize)
        {
            this.dwOffset = dwOffset;
            this.dwSize = dwSize;
        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwOffset)));
            baos.write(intBytes(swapInt(dwSize)));
            baos.close();

            return baos.toByteArray();
        }
    }

    private class AVIJunk
    {
        public byte[] fcc  = new byte[] { 'J', 'U', 'N', 'K' };
        public int    size = 1808;
        public byte[] data = new byte[size];

        public AVIJunk()
        {
            Arrays.fill(data, (byte) 0);
        }

        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(data);
            baos.close();

            return baos.toByteArray();
        }
    }

    private byte[] writeImageToBytes(java.awt.Image image) throws Exception
    {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        ImageIO.write(bi, "jpg", baos);
        baos.close();
        bi = null;
        g = null;

        return baos.toByteArray();
    }

}
  

    /**
     *Encodes separate jpgs files into mjpg file. See console for debugging.
     * @param outputFile - Target file - full path with file e.g. C://Media //myFile.mjpg.
     * @param outputWidth - Output movie dimention. In general it doesnt need to match input images dimention, but it is better to resize outside of java and input files which are already resized.
     * @param outputHeght @see outputWidth  0
     * @param fps  - Target fps. In geneal should match the input fps. Not really required though. This info is encoded into mjpeg but the playback is dependant on the value provided later on.
     * @param startNumber -  rder numer of first and last frame. All inclusive. 0 and 273 mean there is file prefix000.extName to prefix273extName
     *Important. Convention assumes that the padding is consistents with the number of digits of endNumber.
     *It forces the file names to be as follows (with files from 0 to 273): prefix000.extName to prefix273extName.
     *If the frames were for example 0 to 12 the files would be:  prefix00.extName to prefix12extName 
     * @param endNumber @see startNumber
     * @param inputFolder - Folder containing frame jpg files
     * @param imagePrefix - Any prefix(before digits). Must be the same for every file
     * @param extName -  Extention name
     */
 public static void encodeImages(File outputFile,int outputWidth, int outputHeght, int fps, int startNumber,  int endNumber, String inputFolder, String imagePrefix, String extName)
 {
        System.out.println("Running "+inputFolder+imagePrefix+extName+" with "+outputWidth+":"+outputHeght+" at "+fps+" frames from "+startNumber+" to "+endNumber+" into "+outputFile);
            try {
                String filler="00000000";
                MJPEGGenerator mJPEGGenerator=  new MJPEGGenerator(outputFile, outputWidth,outputHeght, fps,endNumber-startNumber);
                for(int a=startNumber;a<=endNumber;a++)
                  {
                   String counter=filler.substring(0, String.valueOf(endNumber).length()-String.valueOf(a).length())+a;
                   String fileName=inputFolder+"//"+imagePrefix+counter+"."+extName;
                     System.out.println(a+"/"+endNumber+" with "+fileName);
                    mJPEGGenerator.addImage(ImageIO.read(new File(fileName)));
                  
      
                  }
                mJPEGGenerator.finishAVI();
               } catch (Exception ex) {
                 ex.printStackTrace();
               }
 }
 
 ///////////////////ENCODER END////////////////
 
 ////////////////LISTENER////////////////////

    /**
     *
     * @param videoScreenListener
     */
 public void setListener(VideoScreenListener videoScreenListener)
 {
     this.videoScreenListener=videoScreenListener;
 }
 public VideoScreenListener getListener( )
 {
     return this.videoScreenListener;
 }

    /**
     *Listener for media events. You can use only one setListener.
     */
    public void removeListener( )
 {
     this.videoScreenListener=null;
 }    
 
    /**
     *Listener for media events. You can use only one setListener.
     * Marks key moments for the playback
     */
    public interface  VideoScreenListener
 {

        /**
         *Triggered when loadAndPlayMedia or loadMedia is called
         * @param screenName to call it.
         */
        public void onPreLoad(String screenName);

        /**
         *Triggered when video is loaded before syncing with audio
         * @param screenName to call it.
         */
        public void onLoaded(String screenName);

        /**
        *Triggered right before playing media
         * @param screenName to call it.
         */
        public void onPrePlay(String screenName);

        /**
         *Triggered after playback loop is finished. Called only in LOOP mode
         * @param screenName to call it.
         */
        public void onLoopEnd(String screenName);

        /**
          *Triggered after playback is finished. Called in LOOP and ONCE mode at the very end. 
         * @param screenName to call it.
         */
        public void onEnd(String screenName);
    
 }
 //////////////////LISTENER END///////////

    /**
     *
     * @return
     */
  @Override
    public int hashCode() {
        return (screenName.hashCode()+movieWidth+movieHeight);
    }
    
}
