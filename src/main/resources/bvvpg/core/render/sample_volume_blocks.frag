#define NUM_BLOCK_SCALES 10

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


uniform sampler3D volumeCache;

// -- comes from CacheSpec -----
uniform vec3 blockSize;
uniform vec3 paddedBlockSize;
uniform vec3 cachePadOffset;

// -- comes from TextureCache --
uniform vec3 cacheSize;// TODO: get from texture!?

uniform usampler3D lutSampler;
uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;

float sampleRaw (vec3 posin)
{	
	vec3 pos = vec3(posin);
	float zerofade = 1.0;
	vec3 B0 = vec3(0.0,0.0,0.0);
	vec3 sj = vec3(0.0,0.0,0.0);
	
	//no interpolation
	if(voxelInterpolation == 0)
	{	
		pos = pos + 0.5;
					
		vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;
		uvec4 lutv = texture( lutSampler, q / lutSize );
		B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
		sj = blockScales[ lutv.w ];
		pos = pos*sj;
		
		pos = floor(pos);

	}
	else
	{	
		//cannot read texture with negative coordinates,
		//so let's take the value at the border	
		vec3 over = pos * step(0.0, pos) - pos;
		pos = over + pos;
		over = clamp(over,0,1);
		
		//fake interpolation to zero		
		zerofade = (1.0-over.x)*(1.0-over.y)*(1.0-over.z);		

		vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;	
		uvec4 lutv = texture( lutSampler, q / lutSize );
		B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
		sj = blockScales[ lutv.w ];
		pos = pos*sj;		
	}
	
	vec3 c0 = B0 + mod( pos, blockSize ) + 0.5 * sj ;
	                                       // + 0.5 ( sj - 1 )   + 0.5 for tex coord offset
	
	return zerofade * texture( volumeCache, c0 / cacheSize ).r;		
}

float sampleVolume( vec4 wpos )
{
	//check if in the clipping area
	if(clipactive>0)
	{		
		vec3 posclip = (cliptransform*wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z == clipactive-1)
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
