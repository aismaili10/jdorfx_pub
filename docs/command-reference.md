# JDORFX Command Reference

This is the canonical command reference for JDORFX. Command names and aliases
are defined by `EnumCommand` in `src/JavaFXDrawingHandler.java`; update this
document whenever that enum or command behavior changes.

Detailed image, clipboard, and printing semantics are documented in
[Image compatibility](image-compatibility.md). Animation concepts and runnable
examples are introduced in the [Animation guide](animation-guide.md).

## Command index

The handler currently recognizes the following canonical command names and
compatibility aliases. Recognition does not imply full JavaFX equivalence; see
the unsupported-command section and individual command notes below.

`animationFade`, `animationFill`, `animationParallel`, `animationPath`, `animationPause`, `animationPauseTransition`, `animationPlay`, `animationRotate`
`animationScale`, `animationSequential`, `animationStatus`, `animationStop`, `animationStroke`, `animationTranslate`, `areaAdd`, `areaExclusiveOr`
`areaIntersect`, `areaSubtract`, `areaTransform`, `AREAUNION`, `AREAXOR`, `assignRC`, `background`, `camera`
`cameraFarClip`, `cameraNearClip`, `CLEAR`, `clearRect`, `clip`, `clipboardGet`, `clipboardSet`, `clipboardSetWithoutAlpha`
`clipRemove`, `clipShape`, `color`, `COLOUR`, `composite`, `copyArea`, `cullFace`, `draw3DRect`
`draw3DShape`, `drawArc`, `drawImage`, `drawLine`, `drawOval`, `drawPolygon`, `drawPolyline`, `drawRect`
`drawRoundRect`, `drawShape`, `drawString`, `fill3DRect`, `fill3DShape`, `fillArc`, `fillOval`, `fillPolygon`
`fillRect`, `fillRoundRect`, `fillShape`, `font`, `fontSize`, `fontStyle`, `GC`, `GETCLIPBOARD`
`getState`, `GOTO`, `gradientPaint`, `image`, `imageCopy`, `imageSize`, `imageType`, `keyframe`
`keyValue`, `light`, `loadImage`, `LOCATION`, `map`, `material`, `materialColor`, `materialMap`
`moveTo`, `NEW`, `newImage`, `noOp`, `paint`, `pathAppend`, `pathClone`, `pathClose`
`pathCurrentPoint`, `pathCurveTo`, `pathIterator`, `pathLineTo`, `pathMoveTo`, `pathQuadTo`, `pathReset`, `pathTransform`
`pathWindingRule`, `popGC`, `popImage`, `POS`, `POSITION`, `preferredImageType`, `printImage`, `printPos`
`printScale`, `printScaleToPage`, `pushGC`, `pushImage`, `render`, `reset`, `rotate`, `rotate3DShape`
`rotateCamera`, `saveImage`, `scale`, `scale3DShape`, `setCamera`, `SETCLIPBOARD`, `SETCLIPBOARDWITHOUTALPHA`, `setInterpolator`
`setLight`, `setMaterial`, `setPaintMode`, `setXorMode`, `shape`, `shape3d`, `shapeBounds`, `SHAPECLIP`
`SHAPEDRAW`, `SHAPEFILL`, `shear`, `shear3DShape`, `sleep`, `stringBounds`, `stroke`, `timeline`
`transform`, `translate`, `translate3DShape`, `translateCamera`, `translatePointLight`, `winAlwaysOnTop`, `winAlwaysOnTopSupported`, `winClose`
`winFrame`, `winHide`, `winLocation`, `WINMOVETO`, `winResizable`, `winScreenSize`, `winShow`, `winSize`
`winTitle`, `winToBack`, `winToFront`, `winUpdate`, `winVisible`


PART I: GENERAL, 2D, AND 3D COMMANDS
========================================

The following commands were newly created for JDORFX:
```txt
Command
	Argument		-- Info
	[optional Argument]

shape3d
	String nickName		-- Queries 3D shape with nickName
				-- if no such shape exists, return error

shape3d
	String nickName		-- creates 3D shape with nickName
	"box"			-- creates a box
	Double x  		-- sets location coordinates to x
	Double y  		-- sets location coordinates to y
	Double z  		-- sets location coordinates to z
	Double width  		-- sets width of box
	Double height  		-- sets height of box
	Double depth		-- sets depth of box

shape3d
	String nickName		-- creates 3D shape with nickName
	"cylinder"		-- creates a cylinder
	Double x  		-- sets location coordinates to x
	Double y  		-- sets location coordinates to y
	Double z  		-- sets location coordinates to z
	Double radius  		-- sets radius of cylinder
	Double height  		-- sets height of cylinder

shape3d
	String nickName		-- creates 3D shape with nickName
	"sphere"		-- creates a sphere
	Double x  		-- sets location coordinates of x
	Double y  		-- sets location coordinates of y
	Double z  		-- sets location coordinates of z
	Double radius  		-- sets radius of sphere

draw3dShape
drawShape3d
	String nickName		-- draws 3D shape with nickName to scene as wire model
				-- if no such shape exists, return error

fill3dShape
fillShape3d
	String nickName		-- draws 3D shape with nickName to scene with filled veretices
				-- if no such shape exists, return error

camera
				-- no arguments supplied -> query last set camera

camera
	String nickName		-- Queries camera with nickName
				-- if no such camera exists, return error

camera
	String nickName		-- Creates new camera
	"parallel"		-- Creates new parallel Camera
	Double x  		-- sets location coordinates of x
	Double y  		-- sets location coordinates of y
	Double z  		-- sets location coordinates of z
				-- z value does not influence location of parallel camera
				-- A parallel camera looks at the xy plane
				-- and possess viewing volume for parallel projection

camera
	String nickName		-- Creates new camera
	"perspective"		-- Creates new perspective Camera
	Double x  		-- sets location coordinates of x
	Double y  		-- sets location coordinates of y
	Double z  		-- sets location coordinates of z
	[Double fieldOfView]	-- set field of view
				-- if omitted, default will be 30
				-- A perspective camera looks at the xy plane
				-- and defines the viewing volume for a
				-- perspective projection (fieldOfView)
	[boolean fixedEyeAtCameraZero] -- set fixedEyeAtCameraZero
				-- if omitted, default will be false
				-- if true, the camera will not move when the scene is transformed

setCamera
				-- if no arguments supplied ->
				-- set to default parallel camera

setCamera
	String nickName		-- sets named camera as new camera of scene
				-- if no such camera exists, return error

translateCamera
	String nickName		-- name of the camera to translate
	Double tx		-- distance to move along the x axis
	Double ty		-- distance to move along the y axis
	Double tz		-- distance to move along the z axis
				-- translation is cumulative

rotateCamera
	String nickName		-- name of the camera to rotate
	Double angle		-- rotation angle in degrees
	Double pivotX		-- x coordinate of the rotation pivot
	Double pivotY		-- y coordinate of the rotation pivot
	Double pivotZ		-- z coordinate of the rotation pivot
	Double axisX		-- x component of the rotation axis
	Double axisY		-- y component of the rotation axis
	Double axisZ		-- z component of the rotation axis
				-- rotation uses a cumulative Affine transform

cameraNearClip
	String nickName		-- name of the camera
	[Double distance]	-- optional positive near clipping distance
				-- without distance, returns the current near clip
				-- near clip must remain less than far clip

cameraFarClip
	String nickName		-- name of the camera
	[Double distance]	-- optional positive far clipping distance
				-- without distance, returns the current far clip
				-- far clip must remain greater than near clip

light
	String nickName		-- Queries light with nickName
				-- if no such light exists, return error

light
	String nickName		-- creates new light
	"ambient"		-- creates ambient light
	[String color]		-- sets the color of light
				-- if omitted, default color is white
				-- when first new light is created ->
				-- default ambient light will be turned off
				-- An ambient light is a light source
				-- that radiates from all directions

light
	String nickName		-- creates new light
	"point"			-- creates point light
	Double x  		-- sets location coordinates of x
	Double y  		-- sets location coordinates of y
	Double z  		-- sets location coordinates of z
	[String color]		-- sets the color of light
				-- if omitted, default color is white
				-- when first new light is created ->
				-- default ambient light will be turned off
				-- A point light projects light in all
				-- directions away from its position

setLight
	String nickName		-- sets light with nickName to scene
	["turnOn" / "turnOff"]	-- if omitted or "turnOn" -> turn on light with nickName
				-- if "turnOff" -> turn off light with nickName

translatePointLight
	String nickName		-- name of the PointLight to translate
	Double tx		-- distance to move along the x axis
	Double ty		-- distance to move along the y axis
	Double tz		-- distance to move along the z axis
				-- translation is cumulative; AmbientLight is not supported

cullFace
	String shape3DName	-- name of the Shape3D
	["NONE" / "BACK" / "FRONT"] -- optional face-culling mode
				-- without a mode, returns the current mode
				-- NONE draws all faces, BACK culls back-facing faces,
				-- and FRONT culls front-facing faces


rotate3dShape
rotateShape3d
	String nickName		-- rotates 3D shape with nickName
	Double angle		-- rotates shape with angle
	Double pivotX		-- set x coordinates of pivot point
	Double pivotY		-- set y coordinates of pivot point
	Double pivotZ		-- set z coordinates of pivot point
	Double axisX (0-1.0)	-- rotates shape with (percent of) angle around x axis
	Double axisY (0-1.0)	-- rotates shape with (percent of) angle around y axis
	Double axisZ (0-1.0)	-- rotates shape with (percent of) angle around z axis
				-- 0 0 1 rotates the shape on the z axis

scale3dshape
scaleShape3d
	String nickName		-- scales 3D shape with nickName
	Double x		-- scale x of shape (2.0 -> doubles size on x axis)
	Double y		-- scale y of shape
	Double z		-- scale z of shape

shear3dShape
shearShape3d
	String nickName 	-- shears 3D shape with nickName
	Double x		-- shears x of shape
	Double y		-- shears y of shape

translate3dShape
translateShape3d
	String nickName		-- moves 3D shape
	Double x		-- moves shape on x axis
	Double y		-- moves shape on y axis
	Double z		-- moves shape on z axis
				-- transformations can stack

map
	String nickName		-- stores image with nickName
	String imagePath	-- file path of image

map
	String nickName		-- stores image with nickName
	String imagePath	-- file path of image
	Integer addWidth	-- add width to image to change proportions
	Integer addHeight	-- add height to image to change proportions
	Double angle		-- rotate image with angle around centre
	[String color]		-- fill all transparent pixels of image with color
				-- if omitted -> pixels stay transparent
				-- if rotation values not 0, 90, 180, 270, 360
				-- -> may lead to loss of image quality

material
	String nickName		-- creates new material with nickName
	[String color]		-- sets the diffuse color of material
				-- if omitted -> default is white

materialColor
	String nickName		-- sets color to material with nickName
	String colorType*	-- sets type of color
	String color		-- sets color value
	[Double specularPower]	-- if color type "specular" / "specularColor"
				-- -> sets specular power
				-- if omitted -> default is 32
				-- *colorTypes:
				-- "diffuse" / "diffuseColor"
				-- -> base color of material
				-- "specular" / "specularColor"
				-- -> color of light that is reflecting from surface
				-- specularPower: smoothness of material
				-- smaller -> narrower reflection / smoother surface
				-- if no such material exists, return error

materialMap
	String nickName		-- adds image as map to material
	String mapType*		-- sets type of map
	String imagePath/name	-- file path of image or variable name of image
				-- *mapTypes:
				-- "bump" / "bumpMap"
				-- -> normal map as RGB image for  material
				-- -> It adds depth to the surface’s image.
				-- "diffuse" / "diffuseMap"
				-- -> image as surface of material
				-- "selfIllumination" / "selfIlluminationMap"
				-- -> selfillumination effect of material
				-- "specular" / "specularMap"
				-- -> reflection properties of material
				-- -> Brighter pixels -> brighter reflection
				-- if no such material exists, return error

setMaterial
	String sNickName	-- add material to shape with sNickName
	String mNickName	-- mNickName of material
				-- if no such shape or material, return error
```



=================================================================================================
The following JDOR commands were directly adopted into JDORFX:
winScreenSize
sleep
assignRc


=================================================================================================
The following JDOR commands were translated into JDORFX implementing JavaFX:
winShow
winHide
winClose
winLocation
winAlwaysOnTop
winToBack
winToFront
winAlwaysOnTopSupported
winTitle
winFrame
winResizable
winVisible
winUpdate
winSize
scale
shear
newImage
getState
color
background
drawPolyline
drawPolygon
fillPolygon
stroke
fontStyle
fontSize
font
reset
moveTo
drawLine
drawString
stringBounds
drawOval
fillOval
drawRoundRect
fillRoundRect
drawRect
fillRect
clearRect
drawArc
fillArc
gc
rotate
translate
transform
shape
drawShape
fillShape
clipShape
shapeBounds
areaAdd
areaExclusiveOr
areaIntersect
areaSubtract
areaTransform
pathAppend
pathClose
pathReset
pathCurrentPoint
pathClone
pathLineTo
pathMoveTo
pathQuadTo
pathCurveTo
pathTransform
pathWindingRulle

26-05-2024
Philipp Schaller

=================================================================================================
The following Animation commands were newly created for JDORFX:
animationFade
animationRotate
animationScale
animationTranslate
animationPath
animationPlay
animationPause
animationStop
timeline
keyframe
keyValue

01-05-2026
Animation commands - Alireza Ismaili
19-07-2026
rotateCamera, translateCamera, cameraNearClip, cameraFarClip, translatePointLight, cullFace - Alireza Ismaili


PART II: ANIMATION COMMANDS
===========================

Animation Commands for JDORFX

The following animation commands were added to JDORFX to enable JavaFX-based animations:

```txt
Command
	Argument		-- Info
	[optional Argument]

Note: When using minus sign in arg, embed it in quote signs or parentheses to avoid subtraction with previous arg

================================================================================
TRANSITION-BASED ANIMATIONS
================================================================================

animationFade
fade
fadeTransition
	String animName		-- Creates fade transition animation with animName
	String nodeName		-- Name of shape or 3D shape to animate
	Double duration		-- Duration in milliseconds
	[Double fromValue]	-- Starting opacity (0.0 to 1.0)
	[Double toValue]	-- Ending opacity (0.0 to 1.0)
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

animationRotate
rotateTransition
	String animName		-- Creates rotate transition animation with animName
	String nodeName		-- Name of shape or 3D shape to animate
	Double duration		-- Duration in milliseconds
	[Double byAngle]	-- Degrees to rotate by
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)
	[Double axisX]		-- X component of rotation axis (optional, default: 0.0)
	[Double axisY]		-- Y component of rotation axis (optional, default: 0.0)
	[Double axisZ]		-- Z component of rotation axis (optional, default: 1.0)

animationScale
scaleTransition
	String animName		-- Creates scale transition animation with animName
	String nodeName		-- Name of shape or 3D shape to animate
	Double duration		-- Duration in milliseconds
	[Double byX]		-- Scale factor for X axis
	[Double byY]		-- Scale factor for Y axis
	[Double byZ]		-- Scale factor for Z axis (3D only)
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

animationPauseTransition
pauseTransition
	String animName		-- Creates pause transition animation with animName
	Double duration		-- Duration in milliseconds (the pause length)
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

	NOTE: PauseTransition has no target node. Use it to insert delays into
	sequential or parallel transitions. Example:
	  animationPauseTransition delay1 500 1 .false
	  animationSequential mySeq anim1 delay1 anim2

animationTranslate
translateTransition
	String animName		-- Creates translate transition animation with animName
	String nodeName		-- Name of shape or 3D shape to animate
	Double duration		-- Duration in milliseconds
	[Double byX]		-- Distance to move on X axis (pixels)
	[Double byY]		-- Distance to move on Y axis (pixels)
	[Double byZ]		-- Distance to move on Z axis (pixels, 3D only)
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

animationPath
pathTransition
	String animName		-- Creates path transition animation with animName
	String nodeName		-- Name of shape or 3D shape to animate
	Double duration		-- Duration in milliseconds
	String pathName		-- Name of path shape to follow
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)
	[String orientation]	-- NONE or ORTHOGONAL_TO_TANGENT (default=NONE)

animationFill
fillTransition
	String animName		-- Creates fill transition animation with animName
	String shapeName		-- Name of 2D Shape to animate (must be a `Shape`)
	Double duration		-- Duration in milliseconds
	[String fromValue]	-- Starting color
	[String toValue]	-- Ending color
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

animationStroke
strokeTransition
	String animName		-- Creates stroke transition animation with animName
	String shapeName	-- Name of 2D Shape to animate (must be a `Shape`)
	Double duration		-- Duration in milliseconds
	[String fromColor]	-- Starting stroke color
	[String toColor]	-- Ending stroke color
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

animationSequential
sequentialTransition
	String animName		-- Creates sequential transition animation with animName
	[String nodeName]	-- OPTIONAL: Name of shape/3D shape to provide default target node.
				   If omitted or resolved to an animation name, that animation becomes
				   the first child to play.
	String anim1  		-- Name of first animation to play (if nodeName was provided)
	String anim2  		-- Name of second animation to play after first finishes
	[String anim3]		-- Optional name of third animation to play after second finishes
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

	Usage forms:
	  animationSequential animName nodeName anim1 anim2 ... [cycleCount] [autoReverse]
	  animationSequential animName anim1 anim2 ... [cycleCount] [autoReverse]


animationParallel
parallelTransition
	String animName		-- Creates parallel transition animation with animName
	[String nodeName]	-- OPTIONAL: Name of shape/3D shape to provide default target node.
				   If omitted or resolved to an animation name, that animation becomes
				   the first child to play.
	String anim1  		-- Name of first animation to play (if nodeName was provided)
	String anim2  		-- Name of second animation to play simultaneously with first
	[String anim3]		-- Optional name of third animation to play simultaneously
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

	Usage forms:
	  animationParallel animName nodeName anim1 anim2 ... [cycleCount] [autoReverse]
	  animationParallel animName anim1 anim2 ... [cycleCount] [autoReverse]

================================================================================
ANIMATION CONTROL
================================================================================

animationPlay
play
	String animName		-- Starts or resumes the animation with animName
				-- Works with both transition and timeline animations

animationPause
pause
	String animName		-- Pauses the running animation with animName
				-- Animation can be resumed with animationPlay

animationStop
stop
	String animName		-- Stops the animation with animName
				-- Resets animation to beginning

animationStatus
	String animName		-- Returns current status of animation
				-- Returns: RUNNING, PAUSED, or STOPPED

setInterpolator
	String animName		-- Sets interpolator on supported transition animations
	String interpolator	-- LINEAR|DISCRETE|EASE_IN|EASE_OUT|EASE_BOTH|SPLINE(x1,y1,x2,y2)
			-- Supported: Fade/Rotate/Scale/Translate/Fill/Stroke/Path transitions
			-- Not supported: Timeline, Sequential/Parallel (global interpolator not allowed on groups)

================================================================================
TIMELINE-BASED ANIMATIONS
================================================================================

timeline
	String timelineName	-- Creates a timeline animation with timelineName
	[Integer cycleCount]	-- Number of cycles (-1 for infinite, default=1)
	[Boolean autoReverse]	-- .true or .false (default=.false)

keyframe
	String timelineName	-- Adds a keyframe to specified timeline
	Double time		-- Time in milliseconds for this keyframe
	String keyValueNameOrArray -- Registered KeyValue name or Rexx variable containing a Java KeyValue[]

keyValue
	String keyValueName	-- Creates reusable keyValue object
	String nodeName		-- Named node to animate
	String propertyName	-- Property to animate
	String targetValue	-- Target value
	[String interpolator]	-- LINEAR|DISCRETE|EASE_IN|EASE_OUT|EASE_BOTH|SPLINE(x1,y1,x2,y2)
```

```rexx
keyValue moveKV myShape translateX 300 LINEAR
moveKVObject = rc
keyValue fadeKV myShape opacity 0.25 EASE_BOTH
fadeKVObject = rc
valuesAt1000 = bsf.createJavaArrayOf("javafx.animation.KeyValue", moveKVObject, fadeKVObject)
keyframe myTimeline 1000 "valuesAt1000"
```

The array must be a non-empty Java `javafx.animation.KeyValue[]` and must not
contain null elements.
Quote the array variable name in the `keyframe` command so Rexx passes its name
to the handler instead of substituting the array object's string representation.

## Image, clipboard, and printing commands

The current handler implements these JDOR-compatible commands:

- `loadImage`, `drawImage`, `saveImage`, and `image`
- `imageCopy` and `imageSize`
- `clipboardGet`, `clipboardSet`, and `clipboardSetWithoutAlpha`
- `pushImage` and `popImage`
- `printScale`, `printScaleToPage`, `printPos`, and `printImage`

It also accepts the compatibility aliases `getClipboard`, `setClipboard`, and
`setClipboardWithoutAlpha`. See [Image compatibility](image-compatibility.md)
for signatures, return values, JavaFX/JDOR differences, and tested examples.

## Intentionally unsupported JDOR commands

`imageType` and `preferredImageType` are intentionally unsupported. JavaFX
`Canvas` and `Image` expose neither the selectable nor queryable raster/sample
models provided by Java2D `BufferedImage`. Reporting a remembered integer would
not reproduce the corresponding pixel behavior. See
[Image compatibility](image-compatibility.md#unsupported-commands) for the
design rationale.
