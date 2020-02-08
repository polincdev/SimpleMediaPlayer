package main.java.org.smp.test;
 
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.awt.Rectangle;
import java.util.HashMap;
import main.java.org.smp.player.SimpleMediaPlayer;

/**
 * Test to show how to use MediaPlayer as intro or cutscene/outro. 
 */
public class MenuGeometryTest extends SimpleApplication {

    //Main player
    SimpleMediaPlayer mediaPlayer1;
    SimpleMediaPlayer mediaPlayer2;
    SimpleMediaPlayer mediaPlayer3;
    SimpleMediaPlayer mediaPlayer4;
    //Menu geometry
    Geometry menuGeometry1;
    Geometry menuGeometry2;
    Geometry menuGeometry3;
    Geometry menuGeometry4;
    //Map for picking
    HashMap<Rectangle, SimpleMediaPlayer> pickPlayer=new HashMap<Rectangle, SimpleMediaPlayer> ();
          
    public static void main(String[] args) {
        MenuGeometryTest app = new MenuGeometryTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
       
        setDisplayStatView(false);
        setDisplayFps(true);
        
         //Node to add the geometry to. It gets self attached and dettached on enable/disable
        Node guiNode=getGuiNode();
        //Background color
        viewPort.setBackgroundColor(ColorRGBA.White);
        //Disable flyby cam
        flyCam.setEnabled(false);
       //Init player
         mediaPlayer1=new SimpleMediaPlayer(this);
         mediaPlayer2=new SimpleMediaPlayer(this);
         mediaPlayer3=new SimpleMediaPlayer(this);
         mediaPlayer4=new SimpleMediaPlayer(this);
        //Config
        //Unique name
        String screenName="Menu1";
         //Image to display when player is idle. Null to use screenColor
         String idleImageAssetPath="Textures/idleImageAssetPath.jpg";
         //Image to display when player is loading. Null to use screenColor
         String loadingImageAssetPath="Textures/loadingImageAssetPath.jpg";
       //Image to display when player is paused. Null to use screenColor
         String pausedImageAssetPath="Textures/pausedImageAssetPath.jpg";
         //Color to use if any of above pictures is not provided.
         ColorRGBA screenColor=ColorRGBA.Black;
         //Video to play
         String videoAssetPath="Media/960_540.mjpg";
         //Audio to play
         String audioAssetPath="Media/audio.ogg";
         //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
         int framesPerSec=30;
         //Playback mode. Play once or loop
        int playBackMode=SimpleMediaPlayer.PB_MODE_LOOP;
        //Transparency of the screen. 1 for intro, material and menu geometries. Below 1f for HUD geometries
         float alpha=1f;
         //Relative size 16/9
         int wid=(int)(cam.getHeight()/2.3f);
         int hei=(int)((wid/16f)*9f);
         int margin=wid/3;
         
         ///////////////////////GEOMETRIES
         //1
         menuGeometry1=mediaPlayer1.genGeometry( screenName,wid, hei, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
         //Effect
          mediaPlayer1.enableVHSEffect(true);
        
          //Add to gui 
          guiNode.attachChild(menuGeometry1);
          //Position      
          menuGeometry1.setLocalTranslation(cam.getWidth()/4-wid/2, cam.getHeight()*0.75f-hei/2, 1.0f);
          //Add to map for picking
          pickPlayer.put(new Rectangle((int)menuGeometry1.getLocalTranslation().x, (int)menuGeometry1.getLocalTranslation().y ,mediaPlayer1.getWidth(),mediaPlayer1.getHeight()),mediaPlayer1);
          
            //2
          videoAssetPath="Media/960_540.mjpg";
          screenName="Menu2";
          screenColor=ColorRGBA.Red;
          idleImageAssetPath=null;
          playBackMode=SimpleMediaPlayer.PB_MODE_ONCE;
          menuGeometry2=mediaPlayer2.genGeometry( screenName,wid, hei, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
          //Effect
          mediaPlayer2.enableLineEffect(true);
          mediaPlayer2.enableGrainEffect(true);
          //Add to gui 
          guiNode.attachChild(menuGeometry2);
          //Position      
           menuGeometry2.setLocalTranslation(cam.getWidth()*0.75f-wid/2, (int)menuGeometry1.getLocalTranslation().y, 1.0f);
           //Add to map for picking
          pickPlayer.put(new Rectangle((int)menuGeometry2.getLocalTranslation().x, (int)menuGeometry2.getLocalTranslation().y ,mediaPlayer2.getWidth(),mediaPlayer2.getHeight()),mediaPlayer2);
          
           //3
          videoAssetPath="Media/800_480.mjpg";
          screenName="Menu3";
          screenColor=ColorRGBA.Green;
          idleImageAssetPath="Textures/idleImageAssetPath.jpg";
          loadingImageAssetPath=null;
          playBackMode=SimpleMediaPlayer.PB_MODE_ONCE;
          menuGeometry3=mediaPlayer3.genGeometry( screenName,wid, hei, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
          //Effect
          mediaPlayer3.enableScanlineEffect(true); 
          mediaPlayer3.enableBlackAndWhiteEffect(true); 
          //Add to gui 
          guiNode.attachChild(menuGeometry3);
          //Position      
          menuGeometry3.setLocalTranslation(menuGeometry1.getLocalTranslation().x,  cam.getHeight()/4-hei/2 , 1.0f);
          //Add to map for picking
          pickPlayer.put(new Rectangle((int)menuGeometry3.getLocalTranslation().x, (int)menuGeometry3.getLocalTranslation().y ,mediaPlayer3.getWidth(),mediaPlayer3.getHeight()),mediaPlayer3);
          
          //4
          videoAssetPath="Media/640_360.mjpg";
          screenName="Menu4";
          idleImageAssetPath="Textures/idleImageAssetPath.jpg";
          loadingImageAssetPath="Textures/loadingImageAssetPath.jpg";
          pausedImageAssetPath=null;
          audioAssetPath=null;
          menuGeometry4=mediaPlayer4.genGeometry( screenName,wid, hei, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
          //Effect
          mediaPlayer4.enableVignetteEffect(true);
          mediaPlayer4.enableLCDEffect(true);
          //Add to gui 
          guiNode.attachChild(menuGeometry4);
          //Position      
          menuGeometry4.setLocalTranslation(menuGeometry2.getLocalTranslation().x,  menuGeometry3.getLocalTranslation().y , 1.0f);
          //Add to map for picking
          pickPlayer.put(new Rectangle((int)menuGeometry4.getLocalTranslation().x, (int)menuGeometry4.getLocalTranslation().y ,mediaPlayer1.getWidth(),mediaPlayer1.getHeight()),mediaPlayer4);
          
        //Text
         BitmapFont font =  getAssetManager().loadFont("Interface/Fonts/Default.fnt");
         //Hint 1
         BitmapText hintText = new BitmapText(font);
         hintText.setSize(font.getCharSet().getRenderedSize()*1.0f);
         hintText.setColor(ColorRGBA.Black);
         hintText.setText("960/540 Full image decoration. Loop." );
         hintText.setLocalTranslation(menuGeometry1.getLocalTranslation().x, menuGeometry1.getLocalTranslation().y-margin/4, 1.0f);
         hintText.updateGeometricState();
         guiNode.attachChild(hintText);
         //Hint 2
         BitmapText hintText2 = hintText.clone();
         hintText2.setColor(ColorRGBA.Black);
         hintText2.setText("800/480 No idle image. Red. Run once." );
         hintText2.setLocalTranslation(menuGeometry2.getLocalTranslation().x, menuGeometry2.getLocalTranslation().y-margin/4, 1.0f);
         hintText2.updateGeometricState();
         guiNode.attachChild(hintText2);
          //Hint 3
         BitmapText hintText3 = hintText.clone();
         hintText3.setColor(ColorRGBA.Black);
         hintText3.setText("640/360 No loading image. Green. Run once." );
         hintText3.setLocalTranslation(menuGeometry3.getLocalTranslation().x, menuGeometry3.getLocalTranslation().y-margin/4, 1.0f);
         hintText3.updateGeometricState();
         guiNode.attachChild(hintText3);
          //Hint4
         BitmapText hintText4 = hintText.clone();
         hintText4.setColor(ColorRGBA.Black);
         hintText4.setText("320/180 No pause image. No audio. Run once." );
         hintText4.setLocalTranslation(menuGeometry4.getLocalTranslation().x, menuGeometry4.getLocalTranslation().y-margin/4, 1.0f);
         hintText4.updateGeometricState();
         guiNode.attachChild(hintText4);
          
          //
          RawInputListener inputListener=new RawInputListener() {
            @Override
            public void beginInput() {
            }

            @Override
            public void endInput() {
             }

            @Override
            public void onJoyAxisEvent(JoyAxisEvent evt) {
             }

            @Override
            public void onJoyButtonEvent(JoyButtonEvent evt) {
             }

            @Override
            public void onMouseMotionEvent(MouseMotionEvent evt) {
               
             }

            @Override
            public void onMouseButtonEvent(MouseButtonEvent evt) {
             
                if(evt.isPressed()) 
                   return;
                
                for(Rectangle key: pickPlayer.keySet())
                   {
                       //
                      if(key.contains(evt.getX() ,evt.getY()))
                        {
                            SimpleMediaPlayer mediaPlayer= pickPlayer.get(key);
                          
                            //Pause the rest if playing
                             for(Rectangle key2: pickPlayer.keySet())
                                {
                                 SimpleMediaPlayer mediaPlayer2= pickPlayer.get(key2);
                                //skip the chosen one
                                 if(mediaPlayer.getScreenName().equals(mediaPlayer2.getScreenName()))
                                     continue;
                                 //Pause the rest  
                                 if( mediaPlayer2.isPlaying())
                                     mediaPlayer2.pauseMedia();
                                }
                                 
                               
                               //enable    
                               if(mediaPlayer.isPaused())
                                   mediaPlayer.unpauseMedia();
                               else   if(!mediaPlayer.isLoaded())
                                  mediaPlayer.loadAndPlayMedia();
                                else   if( mediaPlayer.isPlaying())
                                  mediaPlayer.pauseMedia();
                                
                               
                            return;
                        }
                 // System.out.println("Click "+evt.getX()+" "+evt.getY()+" "+ key.contains( evt.getX() ,evt.getY() ));
                  }
            }

            @Override
            public void onKeyEvent(KeyInputEvent evt) {
             }

            @Override
            public void onTouchEvent(TouchEvent evt) {
             }
        } ;
          
          //
          inputManager.addRawInputListener(inputListener);
         
    }
  
    @Override
    public void simpleUpdate(float tpf) {
        
        //!!!!!!!!!!IMPORTANT
       mediaPlayer1.update(tpf);
       mediaPlayer2.update(tpf);
       mediaPlayer3.update(tpf);
       mediaPlayer4.update(tpf);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
   
    
}
