out vec4 fragColor;

in vec2 uv;

uniform sampler2D colorTex;   // original image
uniform sampler2D blurTex;    // blurred image
uniform sampler2D depthTex;   // depth buffer

void main()
{
	float focalDepth = 500;
    float focalRange = 1000;
   
    
    float depth = texture(depthTex, uv).r; // assume linear depth
    float coc = clamp(abs(depth - focalDepth) / focalRange, 0.0, 1.0);

    vec3 sharp = texture(colorTex, uv).rgb;
    vec3 blurred = texture(blurTex, uv).rgb;

    vec3 finalColor = mix(sharp, blurred, coc);

    fragColor = vec4(finalColor, 1.0);
}
