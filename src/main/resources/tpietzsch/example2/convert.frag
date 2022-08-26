uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform int renderType;
uniform vec3 lut [256];

vec4 convert(vec4 acc, float v )
{
	vec4 finC = vec4(0);
	//finC.a = offset.a + scale.a * v;
	finC.a = pow(clamp(offset.a + scale.a * v,0.0,1.0),gamma);
		
	
	//finC.a = offset.a +  pow(scale.a*v,gamma);
	finC.rgb= lut[clamp(int(255*finC.a),0,255)];
	//finC.rgb= lut[clamp(int(255*(offset.a + scale.a*v)),0,255)];
	
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
	
	//return finC;

	//int ind = int(255*(offset.w + scale.w * v));
	//int ind = clamp(int(255*(offset.w + scale.w * v)),0,255);
	//ind = clamp(ind, 0,255);
	//finC.xyz= lut[ind];

	
	//return offset + scale * v;
}
