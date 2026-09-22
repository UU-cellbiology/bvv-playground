uniform mat4 im;
uniform mat3 itvm;
uniform vec3 sourcemin;
uniform vec3 sourcemax;
uniform int voxelInterpolation;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;	

	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin, sourcemax, tnear, tfar );
}

uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;
uniform int cacheType;
uniform int cacheLutZOffset;

float sampleRaw( vec3 pos )
{	
					
	vec3 q = floor( pos / cacheBlockSize ) - lutOffset + 0.5;
	q.z += cacheLutZOffset; 
	uvec4 lutv = texture( globalCacheLut, q / globalCacheLutSize );
	vec3 B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	pos = mod(pos * sj, cacheBlockSize);
		
	//nearest neighbor
	if(voxelInterpolation == 0)
	{	
		pos = floor(pos) + 0.5;
	}
	vec3 c0 = B0 + pos;
	
	return texture( u_Caches[cacheType], c0/ cacheSize[cacheType]  ).r;		

}

float sampleVolume( vec4 wpos )
{
	//check if in the clipping area
	if(clipactive > 0)
	{		
		vec3 posclip = (cliptransform * wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z == clipactive - 1)
			return 0.0;
	}

	vec3 pos = (im * wpos).xyz + 0.5; 

	return sampleRaw(pos);	
}


vec3 gradientVolume( vec4 wpos, float h )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	vec3 q = floor( pos / cacheBlockSize ) - lutOffset + 0.5;
	q.z += cacheLutZOffset; 
	uvec4 lutv = texture( globalCacheLut, q / globalCacheLutSize );
	vec3 sj = 1./blockScales[ lutv.w ];
	vec3 d0 = clamp(pos + vec3(+h, +h, +h) * sj, sourcemin, sourcemax);
	vec3 d1 = clamp(pos + vec3(+h, -h, -h) * sj, sourcemin, sourcemax);
	vec3 d2 = clamp(pos + vec3(-h, +h, -h) * sj, sourcemin, sourcemax);
	vec3 d3 = clamp(pos + vec3(-h, -h, +h) * sj, sourcemin, sourcemax);
	
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