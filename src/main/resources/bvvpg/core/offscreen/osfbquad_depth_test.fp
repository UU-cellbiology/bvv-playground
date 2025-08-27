out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D tex;

void main()
{
	float f = texture( tex, texCoord ).r;
    fragColor = vec4(f,f,f,1.0);
}
