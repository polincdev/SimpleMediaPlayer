package org.smp.test;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import org.smp.player.SimpleMediaPlayer;

/**
 * Test to show how to use MediaPlayer as intro or cutscene/outro. 
 */
public class IntroStateTest extends SimpleApplication {

    //Main player
    SimpleMediaPlayer mediaPlayer;
    //Initial intro state - generated by mediaplayer
    BaseAppState introState;
    //State shown after the intro
    MenuState menuState;
          
    public static void main(String[] args) {
        IntroStateTest app = new IntroStateTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
       
         setDisplayStatView(false);
        //setDisplayFps(false);
        
         //Node to add the geometry to. It gets self attached and dettached on enable/disable
        Node guiNode=getGuiNode();
        
       //Init player
         mediaPlayer=new SimpleMediaPlayer(this);
        //Config
        //Original movie dimentions - relevant only for keeping aspect ratio
        int movieWidth=960;
        int movieHeight=540;
        //True if aspect ratio should be kept. False is the movie should be stretched to the screen 
        boolean keepAspect=true;
        //Unique name
        String screenName="Intro";
         //Image to display when player is idle. Null to use screenColor
         String idleImageAssetPath="Textures/idleImageAssetPath.jpg";
         //Image to display when player is loading. Null to use screenColor
         String loadingImageAssetPath="Textures/loadingImageAssetPath.jpg";
         //Image to display when player is paused. Null to last frame
         String pausedImageAssetPath="Textures/pausedImageAssetPath.jpg";
         //Color to use if above pictures are not provided.
         ColorRGBA screenColor=ColorRGBA.Black;
         //Video to play. Must not be null
         String videoAssetPath="Media/960_540.mjpg";
         //Audio to play. Null if no audio
         String audioAssetPath="Media/audio.ogg";
         //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
         int framesPerSec=30;
         //Playback mode. Play once or loop
         int playBackMode=SimpleMediaPlayer.PB_MODE_ONCE;
         //Transparency of the screen. 1 for intro, material and menu geometries. Below 1 for HUD geometries
         float alpha=1f;
         //Generate state 
         introState=mediaPlayer.genState(guiNode, movieWidth, movieHeight, keepAspect,screenName, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
         //Add intro state. Auto load and play video on enabled
         stateManager.attach(introState);
         
          //Listener to chain next (menu)state. On end switches states
          mediaPlayer.setListener(new SimpleMediaPlayer.VideoScreenListener() {
              @Override
              public void onPreLoad(String screenName) {
                  System.out.println("Media event: onPreLoad by "+screenName);
                  }

              @Override
              public void onLoaded(String screenName) {
                   System.out.println("Media event: onLoaded by "+screenName);
               }

              @Override
              public void onPrePlay(String screenName) {
                    System.out.println("Media event: onPrePlay by "+screenName);
               }

              @Override
              public void onLoopEnd(String screenName) {
                   System.out.println("Media event: onLoopEnd by "+screenName);
               }

              @Override
              public void onEnd(String screenName) {
                     System.out.println("Media event: onEnd by "+screenName);
                     //
                     switchFromIntroToMenu();
                  
                }
          });
          
         //Key listener to stop the intro in the course of playback. Calls onEnd and switches to menu
         ActionListener breakListener=new ActionListener(){
              @Override
              public void onAction(String name, boolean isPressed, float tpf) {
                  mediaPlayer.stopMedia();
                 
               }
          };
         
        inputManager.addMapping("StopBySpace", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("StopByEnter", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(breakListener, new String[]{"StopBySpace"});
        inputManager.addListener(breakListener, new String[]{"StopByEnter"});
         
        
         //Menu state - switched after intro. Initially detached. Attached on movie end
         menuState = new MenuState(guiNode);
       
         
       
        
    }
    
/** 
 *Method to switch between player and other screen/state. If BaseAppState is not used attach and dettach instead of enable/disable
 */
  void switchFromIntroToMenu()
  {
         stateManager.detach(introState);
        stateManager.attach(menuState);
   }     
    @Override
    public void simpleUpdate(float tpf) {
        
        //!!!!!!!!!!IMPORTANT
       mediaPlayer.update(tpf);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
  //Fake state representing main menu  
 class MenuState extends BaseAppState 
           {
            Node guiNode;
            BitmapText hintText ;
            MenuState(  Node guiNode)
            {
             this.guiNode=guiNode;   
            BitmapFont font =  getAssetManager().loadFont("Interface/Fonts/Default.fnt");
             //Hint
            hintText = new BitmapText(font);
            hintText.setSize(font.getCharSet().getRenderedSize()*3.0f);
            hintText.setColor(ColorRGBA.Red);
            hintText.setText("MAIN MENU STATE" );
            hintText.setLocalTranslation(20, 400, 1.0f);
            hintText.updateGeometricState();
          
            }
 
        @Override
        protected void initialize(Application app) {
         }

        @Override
        protected void cleanup(Application app) {
         }

        @Override
        protected void onEnable() {
              guiNode.attachChild(hintText);
         }

        @Override
        protected void onDisable() {
              guiNode.detachChild(hintText);
         }
                
     };
    
}
