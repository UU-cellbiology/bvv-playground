uniform mat4 im;
uniform vec3 sourcemax;
uniform int cropactive;
uniform vec3 cropmin;
uniform vec3 cropmax;
uniform mat4 croptransform;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, vec3( 0, 0, 0 ), sourcemax, tnear, tfar );
}

uniform sampler3D volume;

vec4 sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz;
	
	if(cropactive>0)
	{
		//apply crop transform
		mat4 temp = croptransform*im;
		vec3 poscrop = (croptransform*vec4(pos,1.0)).xyz;
		vec3 cmin = (temp*vec4(cropmin,1.0)).xyz;
		vec3 cmax = (temp*vec4(cropmax,1.0)).xyz;
		vec3 s = step(cmin, poscrop) - step(cmax, poscrop);
		//vec3 poscrop = wpos.xyz;
		//vec3 s = step(cropmin, poscrop) - step(cropmax, poscrop);
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	return texture( volume, (pos+0.5) / textureSize( volume, 0 ) );
}
