out vec4 fragColor;

in vec2 pos;

uniform sampler2D tex;
uniform float radius;

void main()
{

    //blurring radius of the kernel
    float r = radius;
    //taken from https://stackoverflow.com/questions/64837705/opengl-blurring
    //texture resolution
    float xs = 800;
    float ys = 600;
    
    float x, y, xx, yy, rr = r*r, dx, dy, w, w0;
    w0 = 0.3780/pow(radius,1.975);
    vec2 p;
    vec4 col = vec4(0.0,0.0,0.0,0.0);
    for (dx=1.0/xs, x=-r, p.x=pos.x+(x*dx);  x<=r;  x++, p.x+=dx)
    { 
    	xx = x*x;
     	for (dy=1.0/ys, y=-r, p.y=pos.y+(y*dy);  y<=r;  y++, p.y+=dy)
     	{ 
     		yy = y*y;
      		if (xx+yy <= rr)
       		{
        		w = w0*exp((-xx-yy)/(2.0*rr));
    			col += texture(tex,p)*w;
			}
		}
    }
	fragColor = col;
}
