 
package org.smp.test;
 
 
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import org.smp.player.SimpleMediaPlayer;
 
 
 

/**
 *
 * @author xxx
 */
public class ModelMaterialTest extends SimpleApplication  implements ActionListener {

 
  SimpleMediaPlayer  mediaPlayer;
  BitmapText hintText;  
  PointLight lamp_light;
  boolean enabled=true;
  
    /**Creates Test for SimpleMEdiaPlayer with materials
     *
     */
    public   ModelMaterialTest()
    {
        
    }
    
    @Override
    public void simpleInitApp() {
        
        //No stats
        setDisplayStatView(false);
        setDisplayFps(true);
        //Background color
        viewPort.setBackgroundColor(ColorRGBA.Gray);
        //faster cam
        cam.setLocation(cam.getLocation().addLocal(0, 2f, 0));
        flyCam.setMoveSpeed(2.0f);
          
        
         //Scene
         Spatial scene= assetManager.loadModel("Models/testScene.j3o");
         Node sceneAsNode=((Node)((Node)scene).getChild("Scene"));
         rootNode.attachChild(sceneAsNode);
       
         
         //Light
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(1.5f));
        sun.setDirection(new Vector3f( -.5f, -.5f, -.5f).normalizeLocal());
        sceneAsNode.addLight(sun);
        
        lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White.mult(2.5f));
        lamp_light.setRadius(10f);
        lamp_light.setPosition(new Vector3f( 0,2,0));
        sceneAsNode.addLight(lamp_light);
        
       // AmbientLight al = new AmbientLight();
       // al.setColor(ColorRGBA.White.mult(0.1f));
       // sceneAsNode.addLight(al);
         
        //Shdows
        ((Node)sceneAsNode.getChild("Room")).setShadowMode(ShadowMode.Receive); // The wall can cast shadows and also receive them.
        ((Node)sceneAsNode.getChild("TV")).setShadowMode(ShadowMode.CastAndReceive);       // Any shadows cast by the floor would be hidden by it.
        ((Node)sceneAsNode.getChild("Table")).setShadowMode(ShadowMode.CastAndReceive);      
       
        final int SHADOWMAP_SIZE=1024;
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
        dlsf.setLight(sun);
        dlsf.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
        
        
        //Keys
        inputManager.addMapping("StrInc", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("StrDec", new KeyTrigger(KeyInput.KEY_BACK));
        inputManager.addListener(this, new String[]{"StrInc"});
        inputManager.addListener(this, new String[]{"StrDec"});
          
        //Text
        BitmapFont font =  getAssetManager().loadFont("Interface/Fonts/Default.fnt");
	//Hint
	hintText = new BitmapText(font);
	hintText.setSize(font.getCharSet().getRenderedSize()*1.5f);
	hintText.setColor(ColorRGBA.Red);
	hintText.setText("Play/Stop:SPACE Pause/UnPause:Backspace");
	hintText.setLocalTranslation(0, this.getCamera().getHeight()-10, 1.0f);
	hintText.updateGeometricState();
        guiNode.attachChild(hintText);
      
        ///////////PLAYER///////////////////
        mediaPlayer =new SimpleMediaPlayer(this);
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
         String videoAssetPath="Media/320_180.mjpg";
         //Audio to play
         String audioAssetPath="Media/audio.ogg";
         //Source FPS. Should be consistent with original FPS. In most cases 25 or 30
         int framesPerSec=30;
         //Playback mode. Play once or loop
         int playBackMode=SimpleMediaPlayer.PB_MODE_LOOP;
         //Transparency of the screen. 1 for intro, material and menu geometries. Below 1f for HUD geometries
         float alpha=1f;
         //Gen material
          Material  modelMat=mediaPlayer.genMaterial(   idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
          //Effects
          //mediaPlayer.enableScanlineEffect(true);
          //mediaPlayer.enableVignetteEffect(true);
          //mediaPlayer.enableLCDEffect(true);
           mediaPlayer.enableCRTEffect(true);
           mediaPlayer.enableGlitchEffect(true);
          //Get submesh from a model. It was separated from different color during the import
          ((Node)sceneAsNode.getChild("TV")).getChild("mat2").setMaterial(modelMat);
         
        
      }
    
  
  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
       
        ModelMaterialTest app = new ModelMaterialTest();
         app.start();
     
  }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
       
        
        if(!isPressed)
            return;
          
        if(name.equals("StrDec"))
            {
           if( mediaPlayer.isLoaded()  && mediaPlayer.isPlaying())
                {
                  //enable    
                 if(mediaPlayer.isPaused())
                     mediaPlayer.unpauseMedia();
                 else
                 mediaPlayer.pauseMedia();
                }
            }
        else if(name.equals("StrInc"))
            {
             if( mediaPlayer.isLoaded() && mediaPlayer.isPlaying())
                {
                      mediaPlayer.stopMedia();
                }
             else
                {
                  mediaPlayer.loadAndPlayMedia();
                }
             }
    }
 
       @Override
    public void simpleUpdate(float tpf) {
        
        //!!!!!!!!!!IMPORTANT
       mediaPlayer.update(tpf);
   
       //Silly blinking screen effect
       if(mediaPlayer.isPlaying() && !mediaPlayer.isPaused())
         {
          float radius=(float)oscillate(lamp_light.getRadius(),10f,12f, tpf);
          lamp_light.setRadius(radius);
         }
    }
    
    
    public   float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
}
     float osciTime=0;
     public   float oscillate(float input, float min, float max, float delta)
	    {
	       float coof=0.1f ;
	    	if(delta%2==0)
	    	   osciTime=osciTime+delta;
	    	 else
	    	  osciTime=osciTime-delta;
	    	 
	    	float newValue=(float)clamp((float)(input + Math.sin(osciTime )* coof ), min, max);
	    	
	    	if(newValue==max || newValue==min)
	    	     newValue=(min+max)/2;
	    	 
	    	return newValue;
	            
	         
	    }

}
