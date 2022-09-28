# BigVolumeViewer-playground

This is a fork of [BVV](https://github.com/tpietzsch/jogl-minimal) to play around with some new features:
- using LUTs;
- using gamma correction;
- using different volume render methods (transparency/illumination);
- cropping displayed sources in shaders without reloading them.

## How to install it for users

Download latest jar files from <a href="https://github.com/ekatrukha/bvv-playground/releases">releases</a>  
(you will need bvv-playground-0.0.3.jar and jide-oss-3.7.12.jar)  
and put them to the _jar_ folder of the _latest_ FIJI installation.

The plugin should appear in the _Plugins>BigDataViewer>BVV-playground_

It works in the same way as BigVolumeViewer, but there are some new additional features (see below).

## Additional features

First of all, "Brightness and Color" dialog (shortcut <kbd>S</kbd>) is different:

![brighntess dialog](https://katpyxa.info/software/bvv_playground/bvvPG_brightness.png)

It uses range sliders (and one can pull the range by dragging the middle interval).

A standard "brightness" is now called "LUT range", it maps image intensity values to specific colors. This mapping can be done non-linearly by adjusting "LUT **γ**", [gamma](https://en.wikipedia.org/wiki/Gamma_correction) value (a power-law).  

In addition to color, one can also independently map intensity values to an opacity of voxel using "**α** range" (with a corresponding "**α γ**" gamma adjustment slider below).

A new additional checkbox on the left is used to synchronize top pair of sliders (LUT) with the bottom pair (**α**), but not the other way around. It also synchronizes ranges in the extended view (<kbd>>></kbd> button).

A new shortcut <kbd>P</kbd> is used to switch between "Maximum intensity projection" and "Volumetric" rendering. By "volumetric" I mean "transparency" or "alpha-blending" ray-casting. These two different render methods are illustrated below:  
_Maximum intensity_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_maximum_intensity_render.png)  
_Volumetric_  
![Maximum intensity render](https://katpyxa.info/software/bvv_playground/bvvPG_volumetric_render.png)  

Displaying volumes with LUTs and crop view at the current stage are available only from the code (see an [example](https://github.com/ekatrukha/bvv-playground/blob/master/src/test/java/bvv/examples/BT_Example01.java).

## For developers

Since it is not in maven repository, to use this package in your own project,  
you would have to manually install it to the local maven repository using
```
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=put_here_path_to_jar/bvv-playground-X.X.X.jar
```
This jar you can build yourself or take from the release files.  
You project will need an addition to pom.xml:
```
		<dependency>
			<groupId>sc.fiji</groupId>
			<artifactId>bvv-playground</artifactId>
			<version>0.0.2</version>
		</dependency>
 ```
 
A list of additional methods (adding LUTs and volume crop) is illustrated by [this example](https://github.com/ekatrukha/bvv-playground/blob/master/src/test/java/bvv/examples/BT_Example01.java).
The project is currently under development and has a lot of "experimental" code (i.e. not clean and in the state of "well, at least it works". So comments and optimization suggestions are welcome. 
 
Developed in <a href='http://cellbiology.science.uu.nl/'>Cell Biology group</a> of Utrecht University.  
<a href="mailto:katpyxa@gmail.com">E-mail</a> for any questions.
