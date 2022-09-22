uniform mat4 im;
uniform vec3 sourcemax;
uniform int cropactive;
uniform vec3 cropmin;
uniform vec3 cropmax;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, vec3( 0, 0, 0 ), sourcemax, tnear, tfar );
}

uniform sampler3D volume;

vec4 sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	
	float cropf = 1.0;
	if(cropactive>0)
	{
		vec3 poscrop = pos - 0.5;
		vec3 s = step(cropmin, poscrop) - step(cropmax, poscrop);
		cropf= s.x * s.y * s.z;
	} 
	return cropf*texture( volume, pos / textureSize( volume, 0 ) );
}
