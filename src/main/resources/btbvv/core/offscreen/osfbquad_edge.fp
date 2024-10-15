out vec4 fragColor;

in vec2 texCoord;

uniform vec2 offsets[25];
uniform int edge_kernel[25];
uniform float blurkernel[9];
uniform sampler2D tex;

void main()
{
	fragColor = vec4(0.0f);
    vec3 samplex[25];
    
    // sample from texture offsets if using convolution matrix
    for(int i = 0; i < 25; i++)
    {
         samplex[i] = vec3(texture(tex, texCoord.xy + offsets[i]));
    }
    for(int i = 0; i < 25; i++)
    {   	
           fragColor += vec4(samplex[i] * edge_kernel[i], 0.0f);
           //fragColor += vec4(samplex[i] , 0.0f);
           //fragColor += vec4(samplex[i] * blurkernel[i], 0.0f);
           //fragColor += vec4(samplex[i] , 0.0f);
    }
    
    
    fragColor.a = 1.0f;
    //fragColor = texture( tex, texCoord );
}
