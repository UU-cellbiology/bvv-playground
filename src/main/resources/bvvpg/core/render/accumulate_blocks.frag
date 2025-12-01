if (vis)
{
	float x = sampleVolume(wpos);

	vnew = convert(x);	
	
	//max projection
	if(renderType == 0)
	{	
		v =  max(vnew, v);
	}
	//volumetric or surface
	else
	{
		if(lightType * vnew.a > 0 )
		{
			//get gradient
			vec3 n = gradientVolume(wpos, gradientHalfStep);

			//apply lighting
			if(length(n) > 0.0000001)
			{
				n = normalize(n);

				//specular component
				vec3 spec = (lightType - 1.0) * specular( n, viewDir, lightDir, vec3(1.0,1.0,1.0), 16.0, 1.0 );

				// diffuse component
				// not the max(dot(n, lightDir), 0.0) for now, since for single segmetations
				// some edges are not visible 
				float diff = 1.2 * abs(dot(n, lightDir));

				//apply
				vnew = vec4( vnew.rgb * (diff + vec3(0.1, 0.1, 0.1)) + spec, vnew.a);
			}
		}
		v = v + (1.-v.a) * vec4( vnew.rgb, 1. ) * vnew.a;	
	}	

}
