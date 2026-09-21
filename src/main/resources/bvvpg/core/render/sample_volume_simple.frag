uniform mat4 im;
uniform mat3 itvm;
uniform vec3 sourcemin;
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

	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin, sourcemax, tnear, tfar );
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
vec3 gradientVolume( vec4 wpos, float h )
{
	vec3 pos = (im * wpos).xyz + 0.5;

	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	vec3 d0 = clamp(pos + vec3(+h, +h, +h), sourcemin, sourcemax);
	vec3 d1 = clamp(pos + vec3(+h, -h, -h), sourcemin, sourcemax);
	vec3 d2 = clamp(pos + vec3(-h, +h, -h), sourcemin, sourcemax);
	vec3 d3 = clamp(pos + vec3(-h, -h, +h), sourcemin, sourcemax);
	
	float v0 = sampleRaw(d0);
	float v1 = sampleRaw(d1);
	float v2 = sampleRaw(d2);
	float v3 = sampleRaw(d3);
	
	vec3 grad = vec3(
	    v0 + v1 - v2 - v3,
	    v0 - v1 + v2 - v3,
	    v0 - v1 - v2 + v3
	) / (4.0 * h);	
	return -itvm*grad;	
}
