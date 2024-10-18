out vec4 fragColor;
#define KSIZE 25
in vec2 texCoord;

uniform vec2 offsets[KSIZE];
uniform int edge_kernel[KSIZE];
uniform sampler2D tex;

void main()
{
	fragColor = vec4(0.0f);

    vec3 samplex[KSIZE];
    
    // sample from texture offsets if using convolution matrix
    for(int i = 0; i < KSIZE; i++)
    {
         samplex[i] = vec3(texture(tex, texCoord.xy + offsets[i]));
    }
    
    for(int i = 0; i < KSIZE; i++)
    {   	
           fragColor += vec4(samplex[i] * edge_kernel[i], 0.0f);
           //fragColor += vec4(samplex[i] , 0.0f);

    }
    
    
    fragColor.a = 1.0f;
    //fragColor = texture( tex, texCoord );
}
