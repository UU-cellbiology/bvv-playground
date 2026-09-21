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

	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin - 0.5, sourcemax + 0.5, tnear, tfar );
}

uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;
uniform int cacheType;
uniform int cacheLutZOffset;

float sampleRaw( vec3 posin )
{	
	vec3 pos = posin;

	// Clamp tile lookup to minimum tile 0 so we don't hit the zero-LUT pad
	vec3 qPos = max( vec3( 0.0 ), pos );
	
	vec3 tileIndex = floor( qPos / cacheBlockSize );
	
	ivec3 localQ = ivec3( tileIndex - lutOffset );
	
	if ( any( lessThan( localQ, ivec3( 0 ) ) ) || any( greaterThanEqual( localQ, ivec3( lutSize ) ) ) )
		return 0.0;

	// normalized sampling coordinate [0.0, 1.0] for lutSampler
	vec3 q = (localQ + 0.5);
	q.z += cacheLutZOffset;
	q /= globalCacheLutSize;
	
	uvec4 lutv = texture( globalCacheLut, q );
	
	vec3 B0 = vec3(lutv.xyz) * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	
	vec3 tileIndexCoarse = floor( qPos / ( cacheBlockSize * sj ) );
	vec3 relativePos = (pos - tileIndexCoarse * (cacheBlockSize * sj)) * sj;
	//nearest neighbor
	if(voxelInterpolation == 0)
		relativePos = floor(relativePos + 0.5);
	vec3 c0 = B0 + relativePos + 0.5;

	return texture( u_Caches[cacheType], c0/ cacheSize[cacheType] ).r;
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

	vec3 pos = (im * wpos).xyz;

	return sampleRaw(pos);	
}

vec3 gradientVolume( vec4 wpos, float fStep )
{
	vec3 pos = (im * wpos).xyz + 0.5;

	if(voxelInterpolation == 0)
	{
		pos = floor(pos) + 0.5;
	}
	vec3 ox = vec3(fStep, 0, 0);
	vec3 oy = vec3(0, fStep, 0);
	vec3 oz = vec3(0, 0, fStep);
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