package btbvv.core.backend;

public interface Texture1D extends Texture
{
	@Override
	default int texDims()
	{
		return 1;
	}
}
