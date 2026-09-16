uniform vec4 offset;
uniform vec4 scale;
uniform float gamma;
uniform float alphagamma;
uniform int renderType;
uniform float lightType;
uniform int sizeColorLut;
uniform float lLayer;

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
		
		//2D texture with fixed width of 256
		//Clamp normalized scalar input to [0.0, 1.0]
		float normVal = clamp(offset.r + scale.r * v, 0.0, 1.0);
		
		// Apply gamma scaling and scale to discrete LUT range
    	float val = (float(sizeColorLut) - 1.0) * pow(normVal, gamma);
    	
    	// Calculate 2D grid coordinates inside the slice (256 columns per row)
    	float row = floor(val / 256.0);
    	float col = val - (row * 256.0);
    	
    	float totalRows = ceil(float(sizeColorLut) / 256.0);
    	
    	// Map to normalized texture coordinates with half-texel centers (+0.5)
	    vec3 q = vec3(0);
	    q.x = (col + 0.5) / 256.0;
	    q.y = (row + 0.5) / totalRows;
	    q.z = lLayer;
		
		finC =  texture( globalColorLutArray, q);
		//finC = vec4(1,0,0,1);
		//lut->red
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
		
