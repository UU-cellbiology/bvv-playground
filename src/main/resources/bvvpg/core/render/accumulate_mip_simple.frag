if (vis)
{
	vnew = convert(sampleVolume(wpos));	
	
	//max projection
	if(renderType == 0)
	{	
		v =  max(vnew, v);
	}
	
	//volumetric
	if(renderType == 1)
	{
		v = v + (1.-v.a) * vec4( vnew.rgb, 1 ) * vnew.a;		 
	}
	
	//min projection
	if(renderType == 2)
	{
		v = vnew;
		if(v.a >0.99)
		{
			vec3 n =  gradientVolume(  wpos );
			vec3 L = normalize(vec3(1.0, 1.0, 1.0));
			float diff = max(dot(n, L), 0.0);
			v = vec4(v.xyz * diff, 1.0);
		}
		
	}

}
