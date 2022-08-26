if (vis)
{
	float x = sampleVolume(wpos, volumeCache, cacheSize, blockSize, paddedBlockSize, cachePadOffset);
	
	v = convert(v, x);
	//v = max(v, convert(x));
	/*
	newval =convert(sampleVolume(wpos));
	
	//front to back
	v += (1.-v.a) * vec4( newval.rgb, 1 ) *newval.a;
	
	//make sure we do not overdo
	if(v.a>0.99)
	{
		i=numSteps;
	}
	
	//back to front
	//v = mix( v, vec4( newval.rgb, 1), newval.a );
	
	//manual version
	//vold= v;
	//v.rgb=(1.0-newval.a)*vold.rgb+newval.rgb*newval.a;
	//v.a = (1.0-newval.a)*vold.a+newval.a;
	*/
}
