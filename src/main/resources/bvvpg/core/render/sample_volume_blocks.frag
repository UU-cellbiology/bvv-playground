#define NUM_BLOCK_SCALES 10

uniform mat4 im;
uniform vec3 sourcemin;
uniform vec3 sourcemax;
uniform int clipactive;
uniform int voxelInterpolation;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;	
//	if(clipactive>0)
//	{
//		vec3 rangemin = max(vec3((im*vec4(clipmin,1.0)).xyz), sourcemin - 0.5);
//		vec3 rangemax = min(vec3((im*vec4(clipmax,1.0)).xyz), sourcemax + 0.5);
//		intersectBox( mfront.xyz, (mback - mfront).xyz, rangemin, rangemax, tnear, tfar );
//	}
//	else
//	{
		intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin - 0.5, sourcemax + 0.5, tnear, tfar );
//	}
}


uniform usampler3D lutSampler;
uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;

float sampleVolume( vec4 wpos, sampler3D volumeCache, vec3 cacheSize, vec3 blockSize, vec3 paddedBlockSize, vec3 padOffset )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(clipactive>0)
	{
		//mat4 temp = cliptransform*im;
		//vec3 posclip = (cliptransform*vec4(pos-0.5,1.0)).xyz;
		//vec3 cmin = (temp*vec4(clipmin,1.0)).xyz;
		//vec3 cmax = (temp*vec4(clipmax,1.0)).xyz;
		//vec3 cmin = (cliptransform*vec4(clipmin,1.0)).xyz;
		//vec3 cmax = (cliptransform*vec4(clipmax,1.0)).xyz;
		//vec3 posclip = wpos.xyz;
		//vec3 s = step(cmin, posclip) - step(cmax, posclip);
		
		vec3 posclip = (cliptransform*wpos).xyz;
		//vec3 s = step(cmin, posclip) - step(cmax, posclip);
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
			
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;

	uvec4 lutv = texture( lutSampler, q / lutSize );
	vec3 B0 = lutv.xyz * paddedBlockSize + padOffset;
	vec3 sj = blockScales[ lutv.w ];
	pos = pos*sj;
	if(voxelInterpolation == 0)
	{
		pos = floor(pos);
	}
	vec3 c0 = B0 + mod( pos, blockSize ) + 0.5 * sj;
	                                       // + 0.5 ( sj - 1 )   + 0.5 for tex coord offset
	
	return texture( volumeCache, c0 / cacheSize ).r;
}
