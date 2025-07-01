if (vis)
{
	float x = sampleVolume(wpos);

	vnew = convert(x);	
	
	//max projection
	if(renderType == 0)
	{	
		if(vnew.a>0.01)
		{
			//vec3 lightDir = normalize(vec3(0, -0.2, -1));
			//vec3 lightDir = normalize(wback.xyz);
            
            
            vec4 ld1 = vec4(-1.0,0.0, 0.0, 1);
            vec4 ld2 = ipv * ld1;
			ld2 *= 1 / ld2.w;
			vec3 lightPos = ld2.xyz;
			vec3 lightDir = normalize(lightPos - wpos.xyz);
			
			
            vec3 viewDir = -rayDir;
            float phase = phaseHG(lightDir, viewDir, 0.8);
            //vec3 phase = diffuse(viewDir,lightDir);
            
            float stepSize = 1.1;
   			float attenuation = 1.0;
    		float maxDist = length(lightPos - wpos.xyz);
    		vec3 pos = wpos.xyz;
    		//for (float dist = 0.0; dist < maxDist; dist += stepSize) 
    		for (float dist = 0.0; dist < maxDist; dist += step*3.0) 

    		{
        		pos += lightDir * stepSize;
        		//float density = sampleDensity(pos); // Your density/sigma function
        		float density = sampleVolume(vec4(pos,1.0));
        		attenuation *= exp(-density * stepSize * 5.0);
        		if (attenuation < 0.01) break;
    		}
            
            
            
            
            //float lightAtten = computeLightAttenuation(wpos.xyz, lightDir, wfront.xyz);
            vec3 lighting = vnew.rgb * lightColor * 1.0*attenuation ;//* phase;

            //vec3 lighting = vnew.rgb * lightColor * 1.0*lightAtten;// * phase;

            v = v + (1.-v.a) * vec4( lighting, 1 ) *vnew.a;		
		}
	}
	
	//volumetric
	if(renderType == 1)
	{
		v = v + (1.-v.a) * vec4( vnew.rgb, 1 ) *vnew.a;		 
	}
	
	//min projection
	if(renderType == 2)
	{
		v = min(v, vnew);
	}

}
