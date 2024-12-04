if (vis)
{
	float currVal =  sampleVolume(wpos);
	vnew = convert(currVal);	
	
	//max projection
	if(renderType == 0)
	{	
		v =  max(vnew, v);
	}
	
	//volumetric
	if(renderType == 1)
	{
		v = v + (1.-v.a) * vec4( vnew.rgb, 1 ) *vnew.a;		 
	}
	
	//MIDA projection
	if(renderType == 2)
	{
		beta = 1.0;
		float currNorm = pow(clamp(offset.r + scale.r * currVal,0.0,1.0),gamma);
		if(currNorm>valMax)
		{
			beta = 1.0 - (currNorm - valMax);
			valMax = currNorm;
		}
		v = v*beta + (1.0-v.a*beta) * vec4( vnew.rgb, 1 ) *vnew.a;		 
	}

}
