package btbvv.core.backend;

public interface Texture1D extends Texture
{
	@Override
	default int texDims()
	{
		return 1;
	}
	
	@Override
	default int texHeight()
	{
		return 1;
	}
	
	@Override
	default int texDepth()
	{
		return 1;
	}
}
