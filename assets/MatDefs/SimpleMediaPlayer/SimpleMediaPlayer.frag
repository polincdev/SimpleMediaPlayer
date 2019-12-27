#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef DISCARD_ALPHA
    uniform float m_AlphaDiscardThreshold;
#endif
uniform float m_Alpha;
uniform vec4 m_Color;
uniform sampler2D m_ColorMap;
varying vec2 texCoord;
varying vec4 vertColor;
 
uniform float g_Time;
  uniform vec2 g_Resolution;
  
 uniform  bool m_EnabledVHS;
 uniform  bool m_EnabledLine;
 uniform  bool m_EnabledGrain;
 uniform  bool m_EnabledScanline;
 uniform  bool m_EnabledVignette;
 
 float lineHeight = 5.;
 float lineSpeed = 5.0;
 float lineOverflow = 1.4;
 float noise = .70;
 float pixelDensity = 450.;
    
 float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}


float rand2(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453) * 2.0 - 1.0;
}

float offset(float blocks, vec2 uv) {
	return rand2(vec2(g_Time, floor(uv.y * blocks)));
}
 vec3 lum = vec3(0.299, 0.587, 0.114);
void main(){
    vec4 color = vec4(1.0);
    vec2 uv = texCoord;
    #ifdef HAS_COLORMAP
        color = texture2D(m_ColorMap, texCoord);     
    #endif
  
    #ifdef HAS_COLOR
        color = m_Color;
    #endif
    
    
    //VHS dirt
     #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_VHS)  
           vec2 pos=vec2(0.5+0.5*sin(g_Time),uv.y);
           vec3 col2=vec3(texture2D(m_ColorMap,pos))*0.2;
           color+=col2;
     #endif
     
     
      // Moving strip effect
     #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_LINE)  
           float blurLine = clamp(sin(uv.y * lineHeight + g_Time * lineSpeed) + 1.22, 0., 1.);
            float line = clamp(floor(sin(uv.y * lineHeight + g_Time * lineSpeed) + 1.90), 0., lineOverflow);
            color = mix(color - noise * vec3(.08), color, line);
            color = mix(color - noise * vec3(.25), color, blurLine);
     #endif
     
       //Grain 
       #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_GRAIN)  
            color *= vec3(clamp(rand(vec2(floor(uv.x * pixelDensity ), floor(uv.y * pixelDensity)) *g_Time / 1000.) + 1. - noise, 0., 1.));
       #endif
      
    //Scanlines
    #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_SCANLINE)  
       float d = length(uv - vec2(0.5,0.5));
       float scanline = sin(uv.y* g_Resolution.y )*0.04;
       color  -= scanline;
     #endif
     
     // Vignette
     #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_VIGNETTE)  
         color *= vec3(1.0 - pow(distance(uv, vec2(0.5, 0.5)), 3.0) * 3.0);
     #endif
     
     
      // LCD
     #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_LCD)  
      float scanline2 	= clamp( 0.95 + 0.05 * cos( 3.14 * ( uv.y + 0.008 * g_Time ) * 240.0 * 1.0 ), 0.0, 1.0 );
     float lins 	= 0.85 + 0.15 * clamp( 1.5 * cos( 3.14 * uv.x * 640.0 * 1.0 ), 0.0, 1.0 );    
     color *= scanline2 * lins * 1.2;
     gl_FragColor = color*gl_FragColor ; 
      #endif
    
   // CRT
        #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_CRT)  
        if (mod(gl_FragCoord.x, 3.0 ) <1.0) {
            color += vec3(color.r, 0, 0);
        } else if (mod(gl_FragCoord.x, 3.0 ) <2.0) {
            color += vec3(0, color.g, 0);
        } else if (mod(gl_FragCoord.x, 3.0 ) <3.0) {
            color += vec3(0, 0, color.b);
        } else {
             color +=  vec3(.1);
        }
        // 
       if (  mod(gl_FragCoord.y, 3.0 ) < 1.0) {
           color +=  vec3(.1);
          }
       #endif 
     
     #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_GLITCH)  
    color.r *= texture2D(m_ColorMap, uv + vec2(offset(16.0, uv)*0.1  , 0.0)).r;	
    color.g *= texture2D(m_ColorMap, uv + vec2(offset(8.0, uv)*0.1 * 0.16666666, 0.0)).g;
    color.b *= texture2D(m_ColorMap, uv + vec2(offset(8.0, uv)*0.1 , 0.0)).b;
    #endif 
    
   #if defined(HAS_COLORMAP) && defined(HAS_EFFECT_BAW)  
      color =   vec4(vec3( color.r * lum.r+color.g * lum.g+color.b * lum.b),1.0);
    #endif 
      
   
   
    
    #ifdef DISCARD_ALPHA
        if(color.a < m_AlphaDiscardThreshold){
           discard;
        }
    #endif
    
    #ifdef HAS_ALPHA
        color.a = m_Alpha;
     #endif
  
    gl_FragColor = color;
}