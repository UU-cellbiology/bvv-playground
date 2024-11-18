uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform int sizeLUT;
uniform sampler3D lut;

vec4 convert(vec4 acc, float v )
{
	vec4 finC = vec4(0);
	
	if(sizeLUT >0)
	{
		vec3 q = vec3(0);

		//2D texture with fixed width of 256
		
		float val = (sizeLUT-1)*pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);

		//q.x = (val/256.0)-floor(val/256.0);
		//q.y = (floor(val/256.0)+0.5)/ceil(sizeLUT/256.0);
		//or
		q.y = floor(val/256.0);
		q.x = (val/256.0)- q.y;
		q.y = (q.y+0.5)/ceil(sizeLUT/256.0);
	
		//linear 1D texture (obsolete)
		//q.x = pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
				
		finC =  texture( lut, q);
	}
	else
	{
		finC.r = pow(clamp(offset.r + scale.r * v,0.0,1.0),gamma);
		finC.g = pow(clamp(offset.g + scale.g * v,0.0,1.0),gamma);
		finC.b = pow(clamp(offset.b + scale.b * v,0.0,1.0),gamma);
	}
	
	finC.a = pow(clamp(offset.a + scale.a * v,0.0,1.0),alphagamma);		
	
	//max projection
	if(renderType==0)
	{	
		return max(acc, finC);
	}
	
	//volumetric
	if(renderType==1)
	{
		finC = acc + (1.-acc.a) * vec4( finC.rgb, 1 ) *finC.a;		 
		return finC;

	}
	//min projection
	else
	{
		return min(acc, finC);
	}
	
}
