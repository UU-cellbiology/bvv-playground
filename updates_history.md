# Updates history

## 0.5.2

- added 32-bit images support;

## 0.5.1

- added isosurface rendering of volumes;
- updated to scijava POM 42.0.0;

## 0.5.0

- added loading of 8-bit images;
- added separate rendering of opaque and transparent objects of the scene;
- added depth writing for "volumetric" rendering, allowing occlusion of transparent objects;
- fixed "fake" interpolation;
- added tooltips to converter setup panels;

## 0.4.0

- added orthographic projection rendering (>viewer->setProjectionType);
- remade range and value (regular) sliders, no dependence on jide library;
- sliders color can be changed, double click expands range slider;
- clip interval is now in BVV world coordinates;
- clip transform is now "inverted" in shaders, i.e. can be treated as normal;
- volume rendering now happens from -0.5 pixel width till (N-1)+0.5, where N is pixel number;
- added "fake" linear interpolation for -0.5 till 0.0 rendering range in trilinear interpolation mode;
- added exposed source selection listeners in the source table;
- added function to select sources in source table from outside programmatically;

## 0.3.4

- updated to fiji POM 40.0.0;

## 0.3.3

- fixed thrown exception during BVV restart;

----------
Developed in <a href='http://cellbiology.science.uu.nl/'>Cell Biology group</a> of Utrecht University.  
<a href="mailto:katpyxa@gmail.com">E-mail</a> for any questions or tag <a href="https://forum.image.sc/u/ekatrukha/summary">@ekatrukha</a> at <a href="https://forum.image.sc/">image.sc</a> forum.

