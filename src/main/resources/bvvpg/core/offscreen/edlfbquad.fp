out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D colorTex;
uniform sampler2D depthTex;
uniform vec2 texel;
uniform float fnratio;
uniform float radius;
uniform float strength;

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}

void main()
{
	vec3 color = texture(colorTex, texCoord).rgb;

    vec2 offsets[8] = vec2[](
        vec2( 1.0,  0.0), vec2(-1.0,  0.0),
        vec2( 0.0,  1.0), vec2( 0.0, -1.0),
        vec2( 1.0,  1.0), vec2(-1.0,  1.0),
        vec2( 1.0, -1.0), vec2(-1.0, -1.0)
    );

    float centerDepth = linearizeDepth(texture(depthTex, texCoord).r);
    float diff = 0.0;
    for (int i = 0; i < 8; i++)
    {
        float d = linearizeDepth(
            texture(depthTex, texCoord + offsets[i] * texel * radius).r);
        //diff += max(0.0, centerDepth - d);
        diff += max(0.0, (centerDepth - d) / centerDepth);
       
    }

    float shade = exp(-1.0 * strength * diff);

    fragColor =  vec4(color * shade, 1.0);

    gl_FragDepth = texture( depthTex, texCoord ).r;
}