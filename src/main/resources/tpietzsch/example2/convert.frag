uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform int useLUT;
uniform vec3 lut [256];

vec4 convert(vec4 acc, float v )
{
	vec4 finC = vec4(0);
	
	finC.a = pow(clamp(offset.a + scale.a * v,0.0,1.0),alphagamma);			

	if(useLUT >0)
	{
		float lutN = pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
		finC.rgb= lut[clamp(int(255*lutN),0,255)];
	}
	else
	{
		finC.r= pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
		finC.g= pow(clamp(offset.g + scale.g * v,0.0,1.0),gamma);
		finC.b= pow(clamp(offset.b + scale.b * v,0.0,1.0),gamma);
	}
	
	if(renderType==0)
	{	
		//need to think about it, if it is true
		return max(acc, finC);
	}
	else
	{
		finC = acc + (1.-acc.a) * vec4( finC.rgb, 1 ) *finC.a;		 
		return finC;

	}
	
}
