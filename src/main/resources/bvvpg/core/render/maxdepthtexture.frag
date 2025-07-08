uniform sampler2D sceneDepth;
uniform float xf;

float tw( float zd )
{
	return ( xf * zd ) / ( 2 * xf * zd - xf - zd + 1 );
}

float getMaxDepth( vec2 uv )
{
	return tw( texture( sceneDepth, ( uv + 1 ) / 2 ).x );
}


float getMaxDepthNDC( vec2 uv )
{
	return  texture( sceneDepth, ( uv + 1 ) / 2 ).x;
}

float tzd( float w )
{
	return (w*xf-w)/(2.0*xf*w - w - xf);
}