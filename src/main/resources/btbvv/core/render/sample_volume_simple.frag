uniform mat4 im;
uniform vec3 sourcemax;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar)
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;	
	if(clipactive>0)
	{
		vec3 rangemin = vec3((im*vec4(clipmin,1.0)).xyz);
		vec3 rangemax = vec3((im*vec4(clipmax,1.0)).xyz);
		intersectBox( mfront.xyz, (mback - mfront).xyz, rangemin, rangemax, tnear, tfar );
	}
	else
	{
		intersectBox( mfront.xyz, (mback - mfront).xyz, vec3( 0, 0, 0 ), sourcemax, tnear, tfar );
	}
}

uniform sampler3D volume;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz;
	
	if(clipactive>0)
	{
		//apply clip transform
		mat4 temp = cliptransform*im;
		vec3 posclip = (cliptransform*vec4(pos,1.0)).xyz;
		vec3 cmin = (temp*vec4(clipmin,1.0)).xyz;
		vec3 cmax = (temp*vec4(clipmax,1.0)).xyz;
		vec3 s = step(cmin, posclip) - step(cmax, posclip);
		//vec3 posclip = wpos.xyz;
		//vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	return texture( volume, (pos+0.5) / textureSize( volume, 0 ) ).r;
}
