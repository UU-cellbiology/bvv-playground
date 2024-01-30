[![Build Status](https://github.com/UU-cellbiology/bvv-playground/actions/workflows/build.yml/badge.svg)](https://github.com/UU-cellbiology/bvv-playground/actions/workflows/build.yml)  
 
# BigVolumeViewer-playground

This is a fork of [BVV](https://github.com/bigdataviewer/bigvolumeviewer-core) to play around with some new features:
- using LUTs;
- using gamma correction;
- using different volume render methods (transparency/illumination);
- clipping of displayed sources in shaders without reloading them.

Currently synced to BVV version 0.3.3 (this [commit](https://github.com/bigdataviewer/bigvolumeviewer-core/commit/60fe3d0595f1a68dd45f85e216f09b369eaa165d)).

## How to install it for users

Download latest zip archive with jar files from <a href="https://github.com/ekatrukha/bvv-playground/releases">releases</a>  
(it is called *bvv-playground-X.X.X_jar_files.zip*),   
extract and put them to the _jar_ folder of the _latest_ FIJI installation.

The plugin should appear in the _Plugins>BigDataViewer>BVV-playground_

It works in the same way as BigVolumeViewer, but there are some new additional features (see below).

## Additional features

First of all, "Brightness and Color" dialog (shortcut <kbd>S</kbd>) is different:

![brighntess dialog](https://katpyxa.info/software/bvv_playground/bvvPG_brightness.png)  

*(works also with Cards panel, activated by shortcut <kbd>P</kbd>)

It uses range sliders (and one can pull the range by dragging the middle interval).

A standard "brightness" is now called "LUT range", it maps image intensity values to specific colors. This mapping can be done non-linearly by adjusting "LUT **γ**", [gamma](https://en.wikipedia.org/wiki/Gamma_correction) value (a power-law).  

In addition to color, one can also independently map intensity values to an opacity of voxel using "**α** range" (with a corresponding "**α γ**" gamma adjustment slider below).

A new additional checkbox on the left is used to synchronize top pair of sliders (LUT) with the bottom pair (**α**), but not the other way around. It also synchronizes ranges in the extended view (<kbd>>></kbd> button).

A new shortcut <kbd>O</kbd> (letter) is used to switch between "Maximum intensity projection" and "Volumetric" rendering. By "volumetric" I mean "transparency" or "alpha-blending" ray-casting. These two different render methods are illustrated below:  
_Maximum intensity_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_maximum_intensity_render.png)  
_Volumetric_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_volumetric_render.png)  

Displaying volumes with LUTs and clipped view at the current stage are available only from the code (see an [example](https://github.com/ekatrukha/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/BT_Example01.java)).

## For developers

A list of additional methods (adding LUTs and volume clipping) is illustrated by [this example](https://github.com/ekatrukha/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/BT_Example01.java).  
There is a possibility to specify "clipping transform", shown [here](https://github.com/ekatrukha/bvv-playground/blob/master/src/test/java/bvv/vistools/examples/BT_Example02.java).   
   
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
and add corresponding dependency:

```
<dependency>
  <groupId>nl.uu.science.cellbiology</groupId>
  <artifactId>bvv-playground</artifactId>
  <version>X.X.X</version>
</dependency>
```


----------
Developed in <a href='http://cellbiology.science.uu.nl/'>Cell Biology group</a> of Utrecht University.  
<a href="mailto:katpyxa@gmail.com">E-mail</a> for any questions.
