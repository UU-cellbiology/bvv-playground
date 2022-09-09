uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform vec3 lut [256];

vec4 convert(vec4 acc, float v )
{
	vec4 finC = vec4(0);
	
	finC.a = pow(clamp(offset.a + scale.a * v,0.0,1.0),alphagamma);
		
	float lutN = pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
	finC.rgb= lut[clamp(int(255*lutN),0,255)];

	
	if(renderType==0)
	{
		return max(acc, finC);
	}
	else
	{
		finC = acc + (1.-acc.a) * vec4( finC.rgb, 1 ) *finC.a;
		if(finC.a>0.999)
		{
			finC.a = 100;
		}
		return finC;

	}
	
}
