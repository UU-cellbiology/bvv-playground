out vec4 fragColor;

in vec2 uv;

uniform sampler2D colorTex;   // original image
uniform sampler2D blurTex;    // blurred image
uniform sampler2D depthTex;   // depth buffer
uniform float xf;
uniform float focalDepth;
uniform float focalRange;
float tw( float zd )
{
	return ( xf * zd ) / ( 2 * xf * zd - xf - zd + 1 );
}

void main()
{

   
    
    float depth = texture(depthTex, uv).r; // assume linear depth
    float coc = clamp(abs(tw(depth) - focalDepth) / focalRange, 0.0, 1.0);

    vec3 sharp = texture(colorTex, uv).rgb;
    vec3 blurred = texture(blurTex, uv).rgb;

    vec3 finalColor = mix(sharp, blurred, coc);

    fragColor = vec4(finalColor, 1.0);
}
