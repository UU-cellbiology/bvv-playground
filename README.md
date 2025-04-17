[![Build Status](https://github.com/UU-cellbiology/bvv-playground/actions/workflows/build.yml/badge.svg)](https://github.com/UU-cellbiology/bvv-playground/actions/workflows/build.yml)  
 
# BigVolumeViewer-playground

This is a fork of [BVV](https://github.com/bigdataviewer/bigvolumeviewer-core) with some additional features:
- alpha opacity/transparency slider;
- gamma correction for brightness/opacity (and alpha);
- "volumetric" rendering method ("alpha-blending");
- nearest-neighbor and trilinear interpolation;
- lookup tables (LUTs, custom with alpha values and coming from ImageJ);
- clipping of displayed sources in shaders (optionally using custom transform);
- perspective and orthographic projections.

Currently synced to BVV version 0.3.4 (this [commit](https://github.com/bigdataviewer/bigvolumeviewer-core/tree/2b8367ef592ede840ecba932deb7ff19b1896d6a)).

## How to install it for users

Download latest zip archive with jar files from <a href="https://github.com/ekatrukha/bvv-playground/releases">releases</a>  
(it is called *bvv-playground-X.X.X_jar_files.zip*),   
extract and put them to the _jar_ folder of the _latest_ FIJI installation.

The plugin should appear in the _Plugins>BigDataViewer>BVV-playground_

## Additional features

### Volumetric rendering
A new shortcut <kbd>O</kbd> (letter) is used to switch between "Maximum intensity projection" and "Volumetric" rendering. By "volumetric" I mean "transparency" or "alpha-blending" ray-casting. These two different rendering methods are illustrated below:  
_Maximum intensity_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_maximum_intensity_render.png)  
_Volumetric_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_volumetric_render.png)  

### Gamma and opacity sliders
The standard "Brightness and Color" dialog (shortcut <kbd>S</kbd>) is now different:

![brighntess dialog collapsed](https://katpyxa.info/software/bvv_playground/bvvPG_brightness_0.2.0.png)  

*(works also with Cards panel, activated by shortcut <kbd>P</kbd>)

It uses range sliders (and one can pull the range by dragging the middle interval).

A standard "brightness" slider is now located next to the color/LUT icon/button, it maps image intensity values to specific colors.  
By clicking on the "three trianlges" button it can be expanded to show advanced settings.  
![brighntess dialog expanded](https://katpyxa.info/software/bvv_playground/bvvPG_brightness_expanded_0.2.0.png)  

There is a new slider marked "**γ**" that adjusts non-linear color/LUT mapping by introducing [gamma](https://en.wikipedia.org/wiki/Gamma_correction) correction (a power-law).  

In addition to color, one can also independently adjust the mapping of intensity values to the opacity using "**α**" range slider (with a corresponding "**γ α**" gamma adjustment slider below).

A new additional checkbox on the left is used to synchronize top pair of sliders (color/LUT + **γ**) with the bottom pair (**α** + **γ α**), but not the other way around. It also synchronizes slider ranges. It is useful to keep it selected in the beginning, to see the volume, and later fine-tune the alpha values independently (unselected) for a better result. It is especially helpful in the volumetric rendering mode.   

### Lookup tables (LUTs)

In a brightness dialog, right clicking on the color icon displays the list of ImageJ LUTs that can be selected and applied to a specific source.   

![LUT selection](https://katpyxa.info/software/bvv_playground/bvvPG_lut_selection_0.2.0.gif)   

The left mouse button click still activates a dialog for a monochromatic, "single-color" painting of voxels. LUTs can be specified in Cards dialog, also on the Source and Group tables by using the right mouse button click too.   

## Different interpolation schemes

For each source one can specify how voxels' intensity is interpolated: via nearest neighbor or trilinear. In the example below, a left part of the sphere (filled with random numbers) is interpolated trilinearly (deafult), on the right side the nearest neighbor interpolation is shown.  

![voxel interpolation example](https://katpyxa.info/software/bvv_playground/bvvPG_voxel_interpolation.png) 

The code for this example can be found [here](https://github.com/UU-cellbiology/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/PG_Example02.java).

## For developers

A list of additional methods (adjusting gamma and opacity, adding LUTs and volume clipping) is illustrated by [this example](https://github.com/UU-cellbiology/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/PG_Example01.java).  
There is a possibility to specify "clipping transform", shown [here](https://github.com/UU-cellbiology/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/PG_Example03.java).   

Newly added functions of BvvSource can be found [here](https://github.com/UU-cellbiology/bvv-playground/blob/c65494c3c2be4bcd30d1d000ad68ead9d8804a7f/src/main/java/bvvpg/vistools/BvvSource.java#L66). 
   
The project is currently under development and has a lot of "experimental" code (i.e. not clean and in the state of "well, at least it works"). So comments and optimization suggestions are welcome.

### Adding project in maven
This project lives in _scijava.public_ maven repository.
To use it in your own project, add to your _pom.xml_ :
```
<repositories>
....	
	<repository>
		<id>scijava.public</id>
		<url>https://maven.scijava.org/content/groups/public</url>
	</repository>
</repositories>
```
and add the corresponding dependency:

```
<dependency>
  <groupId>nl.uu.science.cellbiology</groupId>
  <artifactId>bvv-playground</artifactId>
  <version>X.X.X</version>
</dependency>
```
### Shipping of bvv-playground with FIJI

You need to download and ship the latest _bvv-playground-X.X.X.jar_ from [maven](https://maven.scijava.org/#nexus-search;quick~bvv-playground) or [release](https://github.com/UU-cellbiology/bvv-playground/releases). 

## Updates history

It is available through releases description or [as one file](https://github.com/UU-cellbiology/bvv-playground/blob/master/updates_history.md).

----------
Developed in <a href='http://cellbiology.science.uu.nl/'>Cell Biology group</a> of Utrecht University.  
<a href="mailto:katpyxa@gmail.com">E-mail</a> for any questions or tag <a href="https://forum.image.sc/u/ekatrukha/summary">@ekatrukha</a> at <a href="https://forum.image.sc/">image.sc</a> forum.

