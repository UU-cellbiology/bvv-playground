package bvvpg.source.converters;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;

public class ConverterSetupSyncState
{
	private final Map< ConverterSetup, Boolean > setupToSyncState = new HashMap<>();

	ConverterSetupSyncState()
	{
		
	}
	public boolean getSyncState( final ConverterSetup setup )
	{
		Boolean out = setupToSyncState.get( setup );
		if (out == null)
		{
			setupToSyncState.put( setup, new Boolean(true) );
			return true;
		}
		return out;
	}
	
	public void setSyncState (final ConverterSetup setup, boolean bState)
	{
		setupToSyncState.put( setup, new Boolean(bState) );
	}
}
