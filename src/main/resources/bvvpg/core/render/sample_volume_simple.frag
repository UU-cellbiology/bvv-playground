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
			
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	
	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	return texture( volume, pos / textureSize( volume, 0 ) ).r;
}
