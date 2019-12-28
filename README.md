# SimpleMediaPlayer
Simple Media player library for JMonkey Game Engine playing mjpeg(video) and ogg(audio). 

![smp_menu_geometries](../master/img/smp_menu_geometries.jpg)

![smp_model_material](../master/img/smp_model_material.jpg)


## Target usage
1. Full screen quads - intro, cutscenes.
2. Smaller quads - menu animations(tutorials etc), gui HUD.
3. Material - embedded materials  e.g. tv set, weapon visors.

## General workflow
1. Choose video you want to play in any format.
2. Resize video to your needs.
3. Extract video and audio.
4. Convert video to set of jpgs.
5. Convert audio to ogg.
6. Encode jpgs into mjpeg using build-in encoder.
7. Place media files to asset folder.
8. Declare SimpleMediaPlayer object.
9. Generate state, geometry or material using SimpleMediaPlayer.
10. Provide SimpleMediaPlayer object with update(time/delta) data.
11. Load media, play, stop.
 
## Preparing media
The library can play both video and audio. It can also play video alone, but doesn't support playing audio only.
SimpleMediaPlayer uses mjpeg format for playing video and ogg JME native audio format. As far as audio is concerned built-in JME library is used directly. 
As for video, the encoder and decoder are contained in the SimpleMediaPlayer library. Despite its description mjpeg is not a compression format, it is rather a container.
As a consequence, there is no specific MJPEG standard. Many apps use their own MJPEG standards which cannot be used in SimpleMediaPlayer. For example FFMEG lib generates mjpgs which cannot be used even by VLC.

That is why you need to encode mjpg files on your own using SimpleMediaPlayer's encoder.  
 
More at: 

https://en.wikipedia.org/wiki/Motion_JPEG

#### Tools
In order to reformat media files you will need couple of tools. As you will see they can be replaced with any video processing tools which can carry out basic operations. 
 
1. Resizing video:

http://avidemux.sourceforge.net

2. Extracting and converting  video/audio: 

https://www.videolan.org/vlc/download-windows.html

3. Converting audio 

https://www.audacityteam.org/download/

4. Extracting sequence of jpgs

http://www.virtualdub.org/download.html

#### Resizing
In general resizing is not required. The encoder can resize jpgs on the fly while encoding mjpg but I don't recommend it. Java native libs don't give the best results and as a result jpgs may contain ugly artfacts. Preparing the same file in different sizes by using external app provide better quality.  Use Avidemux for this. 

#### Extracting and converting video and audio
Once resized(or not) the file is ready to extract separate files - one for video and one for audio. We will use VLC for this. So open VLC then:
1. Choose File.
2. Press Convert/Save.
3. First tab, press Add and find the file.
4. Press Convert/Save.
5. Choose Profil: Video - H.264+Mp3
6. Choose output file. 
7. Press Start.

Now do the same with the same file but choose profil Audio:Mp3. We end up with two files: video and audio. Unfortunately they are middle formats and cannot be used directly. 
Video was encoded to  H.264 just because VirtualDub has problems with different formats. If it wasn't for this we could use base video file directly to extract jpgs.
As for audio, you may notice that VLC enables you to encode into ogg. You may be tempted to encode to ogg directly. DO NOT do it. Vlc's ogg format is corrupted i.e. It may result is freezing JME audio library. 
You may encode into ogg, but you must always run VLC's mp3/ogg through Audacity(open file, export to ogg). At this point audio file is ready. Video requires extracting jpgs. 
 
We will use VLC again for once important thing – checking video's resolution and frame rate(FPS). 
1. Play video in Vlc
2. Choose Tools from Menu
3. Choose File info
4. Choose third tab - Codec
5. Check resolution and frame rate. 
6. Write down this info.

Frame rate(FPS) is a piece of information that is vital for playing mjpeg properly. In 99% of cases FPS is either 25 or 30. 

 
#### Converting video to set of jpgs.
We will use VirtualDub for this. 

1. Drop converted video file onto VirtualDub. 
2. Choose File.
3. Choose Export
4. Choose Image sequence.
5. Input any Filename/Prefix name.
6. Input extension name: .jpg
7. Input number of digits - it's quite vital form encoder’s point of view. It basically means padding with 0s. The number should be consistent with the number of digits in the last frame.
For example if you have 200 frames the number should be 3. If the last frame is 50 it should be 2. 
8. Choose output folder.
9. Select JPG format.

After this operation you will end up with set of files/jpgs in a specific folder.

#### Encoding jpgs into mjpeg using build-in encoder.

Once we have a set jpgs we will use build-in encoder to encode them into mjpeg file. Use it in any kind or java app. It is a static method. 

Let's make some assumptions. We have got 274 files containing frames, named from 640_360_0.jpg to  640_360_273.jpg extracted from a video, running at 30FPS, placed in D:/tmp and we want to generate mjpeg named 640_360.mjpg in D:/.
The files are of resolution 640/360. The output file should be of the same size. 

```
//Config
File outputFile=new File("D://640_360.mjpg");
int outputWidth=640;
int outputHeght=360;
int fps=30;
int startNumber=0;
int endNumber=273;
String inputFolder="D://tmp";
String imagePrefix="640_360_";
String extName="jpg";
//GENERATE - watch console!
SimpleMediaPlayer.encodeImages(  outputFile,  outputWidth,   outputHeght,   fps, startNumber,   endNumber,   inputFolder,   imagePrefix,   extName);
```

That's it. You should end up with D://640_360.mjpg. You can test with VLC. 
Just play it. Properly encoded file should play nicely in VLC. It may be too fast or too slow, but it doesn't matter. It should NOT ask for reindexing or freeze at any frame. 

Now place both mjpg and ogg file in the asset folder. New Media folder may is ok. 
    
TIP: The github code includes MJPEGAssemblyTest class to teach you how to encode properly (after reconfiguration). Also it contains set of jpgs in aseet Test folder. 

 ## Installation
 The library consists only of for 4 files: java file, material def file, vert and frag shaders.  Place java file under org.smp.player or whatever you want. Put j3md and shaders in assets in MatDef/SimpleMediaPlayer folder. Et voila. We are ready. 
 
 ## Usage
 The use of the player required three steps.
 1. Declare  SimpleMediaPlayer.
 2. Generate target object (state, geometry, material).
 3. Call SimpleMediaPlayer's update in any of update methods. 
 
Now just load and play media.

At first glance SimpleMediaPlayer may seem to follow Factory design pattern and you may think that SimpleMediaPlayer can be reused for generating many different target objects. Like this

```
 mediaPlayer=new SimpleMediaPlayer(this);
 BaseAppState introState=mediaPlayer.genState(…);
Geometry menuGeometry=mediaPlayer1.genGeometry(…);
```
The second call will return null. One SimpleMediaPlayer can only be used for one target. 
You cannot use the same SimpleMediaPlayer to generate another target object of the same type.

```
mediaPlayer=new SimpleMediaPlayer(this);
BaseAppState introState=mediaPlayer.genState(…);
BaseAppState introState2=mediaPlayer.genState(…);
 
```
The second call will return the same object.


 ### Usage - configuration
 The use of the lib requires some configuration. In most cases it is the same regardless of target display. 
 
 ```
//State only. Node to add the geometry to. It gets self attached and dettached on enable/disable
Node guiNode=getGuiNode();
//Original movie dimentions - relevant only for keeping aspect ratio
int movieWidth=960;
int movieHeight=540;
//State only. True if aspect ratio should be kept. False is the movie should be stretched to the screen 
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
 ```

### Usage - state
SimpleMediaPlayer may we applied as a JME state. It useful especially if you want to play media in an intro or a cutscene. It gets self attached and dettached to a provided node. 

```
//Declare
mediaPlayer=new SimpleMediaPlayer(this);
//Generate   
BaseAppState introState=mediaPlayer.genState(guiNode, movieWidth, movieHeight, keepAspect,screenName, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
//Update in simpleUpdate
 mediaPlayer.update(tpf);     
...

 
```
You may also want to switch to a different state after the video finishes or stop the playback on a key event. Check out IntroStateTest for this. 

### Usage - geometry
SimpleMediaPlayer may we applied as a JME plain geometry. Useful for menu animations or gameplay HUD.
 
```
//Declare
mediaPlayer=new SimpleMediaPlayer(this);
//Generate
 menuGeometry1=mediaPlayer1.genGeometry( screenName,wid, hei, idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
//Add to gui 
guiNode.attachChild(menuGeometry1);
//Position      
menuGeometry1.setLocalTranslation(…);
//Play
mediaPlayer.loadAndPlayMedia();           
```

Check out MenuGeometryTest for more info. 
	
### Usage - material
SimpleMediaPlayer may also used as a plain Material. It is helpful when you want place animation in an existing Model. 

```
//Declare
mediaPlayer=new SimpleMediaPlayer(this);
//Generate
Material  modelMat=mediaPlayer.genMaterial(   idleImageAssetPath, loadingImageAssetPath, pausedImageAssetPath,screenColor,videoAssetPath,audioAssetPath, framesPerSec, playBackMode,alpha );
//Get submesh from a model. It was separated from different color during the import. Use SDK(or print children) to learn the name.
((Node)sceneAsNode.getChild("TV")).getChild("mat2").setMaterial(modelMat);
//Play
mediaPlayer.loadAndPlayMedia();      
```

### Usage - playback
SimpleMediaPlayer's playback API is very simple:
1. Load
2. Play
3. Pause/Unpause
4. Stop

First two operations in most cases happen at the same time hence there is a special method for that. 

``` 
//Load and play asap
mediaPlayer.loadAndPlayMedia();     
//Pause media
mediaPlayer.pauseMedia();
//Unpause - calling play again doesnt work
mediaPlayer.pauseMedia();
//Stop - release all memory
mediaPlayer.stopMedia();
```

For convenience load and play may be separated.
```
//Load 
mediaPlayer.loadMedia();     
...
//Play
mediaPlayer.playMedia();     	
```
As mentioned above one SimpleMediaPlayer can only be used for one target. So if you want to have four menu geometries playing different media you should declare four  SimpleMediaPlayer objects. However if you want to play the same video on different models/quads you may use one material set it into many objects. Also you way reuse one geometry for different media files. SimpleMediaPlayer is not bound to one video nor audio file. For example:
```
mediaPlayer.loadAndPlayMedia();     
mediaPlayer.stopMedia();
mediaPlayer.setMedia(“NewVideoAssetPath”, “NewAudioAssetPath”);
mediaPlayer.loadAndPlayMedia();     
```

### Usage - listener

SimpleMediaPlayer comes with a listener for basic events. 

```
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
                   
                }
          });
```
	
### Usage - decoration
SimpleMediaPlayer may be decorated with 3 static images for the following states:
1. Idle state - when SimpleMediaPlayer geometry is visible, but no media is loaded.
2. Loading state - when SimpleMediaPlayer is loading mjpeg.
3. Paused state - when  SimpleMediaPlayer is paused. 

You provide the above mentioned images while generating state/geometry/material. 
You may provide null values, in which case screenColor(Idle/Loading) or last frame will be used(Paused).
	
	
### Effects
SimpleMediaPlayer comes with a set of effects/shaders that may be applied to the video, even runtime.

```
mediaPlayer.enableVHSEffect(true);
mediaPlayer.enableLineEffect(true);
mediaPlayer.enableGrainEffect(true);        
mediaPlayer.enableScanlineEffect(true); 
mediaPlayer.enableBlackAndWhiteEffect(true); 		
mediaPlayer.enableVignetteEffect(true);
mediaPlayer.enableLCDEffect(true);
mediaPlayer.enableCRTEffect(true);
mediaPlayer.enableGlitchEffect(true);	
```
## Tests

Four tests along with video and audio data are provided:
1. IntroStateTest  - test state as an intro.
2. MenuGeometryTest - test geometries as menu animations.
3. ModelMaterialTest - test embedding the player into a model.
4. MJPEGAssemblyTest - test generating mjpg files. Warning - requires configuration. 

## Credits
 
 https://github.com/monceaux/java-mjpeg
 
 https://github.com/intrack/BoofCV-master
 
 https://github.com/plantuml/plantuml-mit
 
 https://github.com/mattdesl/slim

