uniform mat4 im;
uniform vec3 sourcemax;
uniform int voxelInterpolation;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar)
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;	

	intersectBox( mfront.xyz, (mback - mfront).xyz, vec3( -0.5, -0.5, -0.5 ), sourcemax + 0.5, tnear, tfar );
}

uniform sampler3D volume;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(clipactive>0)
	{
		vec3 posclip = (cliptransform*wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
			
		if(s.x * s.y * s.z == clipactive-1)
			return 0.0;
	} 
	
	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	return texture( volume, pos / textureSize( volume, 0 ) ).r;
}

vec3 gradientVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	vec3 ox = vec3(1.0,0,0);
	vec3 oy = vec3(0,1.0,0);
	vec3 oz = vec3(0,0,1.0);
	float fx1 = texture( volume, (pos + ox) / textureSize( volume, 0 ) ).r;
	float fx0 = texture( volume, (pos - ox) / textureSize( volume, 0 ) ).r;
	float fy1 = texture( volume, (pos + oy) / textureSize( volume, 0 ) ).r;
	float fy0 = texture( volume, (pos - oy) / textureSize( volume, 0 ) ).r;
	float fz1 = texture( volume, (pos + oz) / textureSize( volume, 0 ) ).r;
	float fz0 = texture( volume, (pos - oz) / textureSize( volume, 0 ) ).r;
		
	// divide by 2*voxelSize to approximate derivative in physical units
	float dx = (fx1 - fx0) * 0.5;
	float dy = (fy1 - fy0) * 0.5;
	float dz = (fz1 - fz0) * 0.5;

	return normalize(vec3(dx, dy, dz));
	
	
}
