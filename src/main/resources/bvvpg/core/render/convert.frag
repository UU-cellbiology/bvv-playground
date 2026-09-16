uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform float lightType;
uniform int sizeColorLut;
uniform float colorLutLayer;

vec4 convert(float v)
{	
	vec4 finC = vec4(0);	
	float alphaFin = pow(clamp(offset.a + scale.a * v, 0.0, 1.0), alphagamma);

	if(renderType == 2)
	{
		if(alphaFin < 0.99999)
		{
			alphaFin = 0;
		}
	}
	
	if(sizeColorLut > 0)
	{
		float normVal = clamp(offset.r + scale.r * v, 0.0, 1.0);
		float val = pow(normVal, gamma) * (float(sizeColorLut) - 1.0);
		
		float col = mod(floor(val), 256.0);
		float row = floor(floor(val) / 256.0);
		
		vec3 q;
		q.x = (col + 0.5) / 256.0;
		q.y = (row + 0.5) / 256.0;
		q.z = colorLutLayer;
		
		finC =  texture( globalColorLutArray, q);
		finC.a *= alphaFin;	
	}
	else
	{
		finC.r = pow(clamp(offset.r + scale.r * v, 0.0, 1.0), gamma);
		finC.g = pow(clamp(offset.g + scale.g * v, 0.0, 1.0), gamma);
		finC.b = pow(clamp(offset.b + scale.b * v, 0.0, 1.0), gamma);
		finC.a = alphaFin;				
	}
	
	return finC;

}
		
