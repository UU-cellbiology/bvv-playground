uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform int sizeLUT;
uniform sampler3D lut;


vec4 convert(vec4 v )
{
	vec4 finC = vec4(0);
	
	float al = (v.r+v.g+v.b)/3.0;
	finC.a = pow(clamp(offset.a + scale.a * al,0.0,1.0),alphagamma);			
	finC.r = pow(clamp(offset.r + scale.r * v.r,0.0,1.0),gamma);
	finC.g = pow(clamp(offset.g + scale.g * v.g,0.0,1.0),gamma);
	finC.b = pow(clamp(offset.b + scale.b * v.b,0.0,1.0),gamma);
	
	return finC;
		
}

