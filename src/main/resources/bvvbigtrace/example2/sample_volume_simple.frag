uniform mat4 im;
uniform vec3 sourcemax;
uniform int cropactive;
uniform vec3 cropmin;
uniform vec3 cropmax;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	vec3 rangemin = vec3((im*vec4(cropmin,0.0)).xyz*cropactive+(1-cropactive)*vec3( 0, 0, 0 ));
	vec3 rangemax = vec3((im*vec4(cropmax,0.0)).xyz*cropactive+(1-cropactive)*sourcemax);
	intersectBox( mfront.xyz, (mback - mfront).xyz, rangemin, rangemax, tnear, tfar );
}

uniform sampler3D volume;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	if(cropactive>0)
	{
		vec3 poscrop = pos - 0.5;
		vec3 s = step((im*vec4(cropmin,0.0)).xyz, poscrop) - step((im*vec4(cropmax,0.0)).xyz, poscrop);
		if(s.x * s.y * s.z==0.0)
			return 0.0;
	} 
	return texture( volume, pos / textureSize( volume, 0 ) ).r;
}
