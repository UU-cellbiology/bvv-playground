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

vec3 getPosCache(vec3 pos)
{
		vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;
		uvec4 lutv = texture( lutSampler, q / lutSize );
		B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
		sj = blockScales[ lutv.w ];
		return pos*sj;
}

float sampleVolume( vec4 wpos )
{

	//check if in the clipping area
	if(clipactive>0)
	{		
		vec3 posclip = (cliptransform*wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
			
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	}
	
	
	float zerofade = 1.0;
	vec3 pos = (im * wpos).xyz;
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
	
	return zerofade*texture( volumeCache, c0 / cacheSize ).r;
	
	
}
