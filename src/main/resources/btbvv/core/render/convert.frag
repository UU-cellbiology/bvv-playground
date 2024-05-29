uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform int useLUT;
uniform sampler3D lut;

vec4 convert(vec4 acc, float v )
{
	vec4 finC = vec4(0);
	
	if(useLUT >0)
	{
		vec3 q = vec3(0);
		q.x = 1.0 - pow(clamp(offset.r + scale.r *v,0.0,1.0),gamma);
		finC =  texture( lut, q);
		
	}
	else
	{
		finC.r = 1.0 - pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
		finC.g = 1.0 - pow(clamp(offset.g + scale.g * v,0.0,1.0),gamma);
		finC.b = 1.0 - pow(clamp(offset.b + scale.b * v,0.0,1.0),gamma);
	}
	
	finC.a = pow(clamp(offset.a + scale.a * v,0.0,1.0),alphagamma);		
	
	if(renderType==0)
	{	
		//need to think about it, if it is true
		//return max(acc, finC);
		if(acc.a>finC.a)
		{	return acc;}
		else
			{return finC;}
//		return max(acc.a, finC.a);
		
	}
	else
	{
		finC = acc + (1.-acc.a) * vec4( finC.rgb, 1 ) *finC.a;		 
		return finC;

	}
	
}
