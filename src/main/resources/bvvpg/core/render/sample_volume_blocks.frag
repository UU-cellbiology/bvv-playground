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


uniform usampler3D lutSampler;
uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;

float sampleVolume( vec4 wpos, sampler3D volumeCache, vec3 cacheSize, vec3 blockSize, vec3 paddedBlockSize, vec3 padOffset )
{

	//check if in the clipping area
	if(clipactive>0)
	{		
		vec3 posclip = (cliptransform*wpos).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
			
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	}
	float corr = 1.0;
	vec3 pos = (im * wpos).xyz;
	vec3 B0 = vec3(0.0,0.0,0.0);
	vec3 sj = vec3(0.0,0.0,0.0);
	if(voxelInterpolation == 0)
	{	
		pos = pos + 0.5;			
		vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;
	
		uvec4 lutv = texture( lutSampler, q / lutSize );
		B0 = lutv.xyz * paddedBlockSize + padOffset;
		sj = blockScales[ lutv.w ];
		pos = pos*sj;
		pos = floor(pos);

	}
	else
	{		
		vec3 over = pos * step(0.0,pos) - pos;
		corr = 1.0-2.0*max(over.x,max(over.y,over.z));	 
		pos = over + pos;		
		vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;	
		uvec4 lutv = texture( lutSampler, q / lutSize );
		B0 = lutv.xyz * paddedBlockSize + padOffset;
		sj = blockScales[ lutv.w ];
		pos = pos*sj;		
	}
	
	vec3 c0 = B0 + mod( pos, blockSize ) + 0.5 * sj ;
	                                       // + 0.5 ( sj - 1 )   + 0.5 for tex coord offset
	
	return corr*texture( volumeCache, c0 / cacheSize ).r;
	
	
}
