out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D tex;

void main()
{
    fragColor = texture( tex, texCoord );
    fragColor.rgb = fragColor.rgb/max(fragColor.a,0.00001);
   
}
