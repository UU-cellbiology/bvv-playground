out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D colorTex;
uniform sampler2D depthTex;

void main()
{
    fragColor = texture( colorTex, texCoord );
    gl_FragDepth = texture( depthTex, texCoord ).r;
}
