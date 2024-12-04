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
		v = v + (1.-v.a) * vec4( vnew.rgb, 1 ) *vnew.a;		 
	}
	
	//min projection
	if(renderType == 2)
	{
		v = min(v, vnew);
	}

}
