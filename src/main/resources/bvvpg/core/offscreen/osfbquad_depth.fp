out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D tex;

void main()
{
    gl_FragDepth = texture( tex, texCoord ).r;
    //fragColor.r = texture( tex, texCoord ).r;
}
