#define NUM_BLOCK_SCALES 10

uniform mat4 im;
uniform vec3 sourcemin;
uniform vec3 sourcemax;
uniform int cropactive;
uniform vec3 cropmin;
uniform vec3 cropmax;
uniform mat4 croptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin, sourcemax, tnear, tfar );
}


uniform usampler3D lutSampler;
uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;

float sampleVolume( vec4 wpos, sampler3D volumeCache, vec3 cacheSize, vec3 blockSize, vec3 paddedBlockSize, vec3 padOffset )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(cropactive>0)
	{
		mat4 temp = croptransform*im;
		vec3 poscrop = (croptransform*vec4(pos-0.5,1.0)).xyz;
		vec3 cmin = (temp*vec4(cropmin,1.0)).xyz;
		vec3 cmax = (temp*vec4(cropmax,1.0)).xyz;
		vec3 s = step(cmin, poscrop) - step(cmax, poscrop);
		//vec3 poscrop = pos - 0.5;
		//vec3 s = step((im*vec4(cropmin,0.0)).xyz, poscrop) - step((im*vec4(cropmax,0.0)).xyz, poscrop);	
		//vec3 poscrop = wpos.xyz;
		//vec3 s = step(cropmin, poscrop) - step(cropmax, poscrop);			
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	vec3 q = floor( pos / blockSize ) - lutOffset + 0.5;

	uvec4 lutv = texture( lutSampler, q / lutSize );
	vec3 B0 = lutv.xyz * paddedBlockSize + padOffset;
	vec3 sj = blockScales[ lutv.w ];

	vec3 c0 = B0 + mod( pos * sj, blockSize ) + 0.5 * sj;
	                                       // + 0.5 ( sj - 1 )   + 0.5 for tex coord offset
	
	return texture( volumeCache, c0 / cacheSize ).r;
}
