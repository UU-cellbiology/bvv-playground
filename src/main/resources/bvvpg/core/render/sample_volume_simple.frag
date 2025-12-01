uniform mat4 im;
uniform mat3 itvm;
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

float sampleRaw (vec3 pos)
{
	return texture( volume, pos / textureSize( volume, 0 ) ).r;
}

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(clipactive>0)
	{
		vec3 posclip = (cliptransform * wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
			
		if(s.x * s.y * s.z == clipactive - 1)
			return 0.0;
	} 
	
	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	return sampleRaw(pos);
}
vec3 gradientVolume( vec4 wpos, float fStep )
{
	vec3 pos = (im * wpos).xyz + 0.5;

	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	vec3 ox = vec3(fStep,0,0);
	vec3 oy = vec3(0,fStep,0);
	vec3 oz = vec3(0,0,fStep);
	float fx1 = sampleRaw(pos + ox);
	float fx0 = sampleRaw(pos - ox);
	float fy1 = sampleRaw(pos + oy);
	float fy0 = sampleRaw(pos - oy);
	float fz1 = sampleRaw(pos + oz);
	float fz0 = sampleRaw(pos - oz);

	// divide by 2*voxelSize to approximate derivative in physical units
	float dx = (fx1 - fx0) * 0.5 / fStep;
	float dy = (fy1 - fy0) * 0.5 / fStep;
	float dz = (fz1 - fz0) * 0.5 / fStep;

	return -itvm*vec3(dx, dy, dz);	
}
