if (vis)
{

		v = convert(v, sampleVolume(wpos));
		
		//make sure we do not overdo
		//if(v.a>0.99)
		//{
		//	i=numSteps;
		//}
		//v = max(v, convert(sampleVolume(wpos)));

		/*
		newval =convert(sampleVolume(wpos));
		
		//front to back
		v += (1.-v.a) * vec4( newval.rgb, 1 ) *newval.a;
		

		//back to front
		//v = mix( v, vec4( newval.rgb, 1), newval.a );
		
		//manual version
		//vold= v;
		//v.rgb=(1.0-newval.a)*vold.rgb+newval.rgb*newval.a;
		//v.a = (1.0-newval.a)*vold.a+newval.a;
		*/
	
}
