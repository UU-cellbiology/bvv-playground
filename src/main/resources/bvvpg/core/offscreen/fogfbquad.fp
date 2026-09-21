out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D colorTex;
uniform sampler2D depthTex;
uniform vec2 texel;
uniform float fnratio;
uniform vec3 fogColor;
uniform float fogDensity;

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}

void main()
{
	vec4 sceneColor = texture(colorTex, texCoord);
	float depthSample = texture(depthTex, texCoord).r;
	if (depthSample >= 1.0) {
        fragColor = sceneColor;
        //fragColor = vec4(fogColor, 1.0);
        //gl_FragDepth = 1.0;
        return;
    }
    float viewDepth = linearizeDepth(depthSample);
	
	float fogFactor = exp( -pow( viewDepth*fogDensity,2));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    // Blend scene color into fog
    vec3 foggedRgb = mix(fogColor, sceneColor.rgb, fogFactor);

    fragColor = vec4(foggedRgb, sceneColor.a);

}