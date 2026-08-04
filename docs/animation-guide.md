# JDORFX Animation Guide

## Overview

Animation functionality has been added to JDORFX by integrating JavaFX's animation framework. The extension provides a set of scriptable animation primitives for both 2D and 3D objects, plus higher-level timeline/keyframe support and composition primitives.

## Changes Made

### 1. Source Code Modifications

**File**: `src/JavaFXDrawingHandler.java`

#### Imports and Utilities
- JavaFX animation classes (e.g. `javafx.animation.*`) and `javafx.util.Duration` were added where needed.

#### Data Structures Added
```java
// Animation storage
HashMap<String, Animation> hmAnimations = new HashMap<>();
HashMap<String, Timeline> hmTimelines = new HashMap<>();
HashMap<String, KeyValue> hmKeyValues = new HashMap<>();
```

#### Commands Added to `EnumCommand`

The animation-related enum entries in `src/JavaFXDrawingHandler.java` are:

**Transition-Based Animations:**
- `ANIMATION_FADE` — Fade transitions (opacity).
- `ANIMATION_ROTATE` — Rotate transitions.
- `ANIMATION_FILL` — Animate fill color on supported 2D shapes.
- `ANIMATION_STROKE` — Animate stroke color on supported 2D shapes.
- `ANIMATION_SCALE` — Scale transitions.
- `ANIMATION_TRANSLATE` — Translate transitions.
- `ANIMATION_PATH` — Path transitions (movement along shapes).

**Animation Composition & Control:**
- `ANIMATION_SEQUENTIAL` — Create a sequential animation group.
- `ANIMATION_PARALLEL` — Create a parallel animation group.
- `ANIMATION_PAUSE_TRANSITION` — Pause transition support.
- `ANIMATION_SET_INTERPOLATOR` — Assign an interpolator to a supported transition animation.
- `ANIMATION_PLAY`, `ANIMATION_PAUSE`, `ANIMATION_STOP`, `ANIMATION_STATUS` — Playback control and status.

**Timeline Animations:**
- `TIMELINE`, `KEYFRAME`, `KEYVALUE` — Create timelines and keyframes for multi-property animations.


#### Synonyms and Convenience Names
Common aliases are implemented in the handler so scripts can use shorter or more natural command names where supported, while the canonical enum values remain the ones listed above.

### 2. Implementation Details

Key aspects of the implementation:
- All animation commands are implemented inside `processCommand()` with strict parameter validation and descriptive error messages.
- Animations support both 2D shapes and 3D nodes where the underlying JavaFX animation class allows it.
- Animations are stored by name in maps so scripts can reference and control them by id.
- Composition of animations (sequential/parallel) is supported and exposes the same playback controls.
- Interpolators include linear and common easing functions; `SPLINE(x1,y1,x2,y2)` is supported for custom easing.

### 3. Documentation and Samples

The syntax reference is maintained in
[`docs/command-reference.md`](command-reference.md). Runnable examples are under
`samples/12f_jdorfx_broken_traffic_lights.rxj` through
`samples/24b_jdorfx_parallel_camera_clipping.rxj`. Paired JavaFX/JDORFX
examples and execution instructions are under
[`samples/oracle_animation_basics/`](../samples/oracle_animation_basics/README.md).

## Features

### Supported Animation Types
1. Fade transitions (opacity).
2. Rotate transitions (2D and 3D rotation).
3. Fill and Stroke transitions (only for 2D shapes)
4. Scale transitions.
5. Translate transitions.
6. Path transitions.
7. Timeline/keyframe animations for multi-property control.

### Control & Composition
- Play/Pause/Stop/Status for any named animation or group.
- Cycle count with `-1` for infinite repetition and `autoReverse` support.
- `ANIMATION_SEQUENTIAL` and `ANIMATION_PARALLEL` to orchestrate multiple animations.
- `ANIMATION_SET_INTERPOLATOR` to apply custom easing to supported transition animations.

### Advanced Usage
- Timelines and `KEYFRAME`s provide fine-grained control over property interpolation.
- Path iterators allow scripts to inspect and react to path geometry when constructing `ANIMATION_PATH` animations.
- `ANIMATION_FILL` and `ANIMATION_STROKE` are limited to 2D `Shape` instances.

## Compatibility

- Works with existing 2D shapes and supported 3D shapes (Box, Cylinder, Sphere).
- Existing commands remain unchanged; animation commands are additive.

## Testing Recommendations

1. Run a focused example, such as `samples/13c_jdorfx_rotate_fade_animation.rxj`.
2. Try different parameter values to see their effects
3. Experiment with combining multiple animations
4. Test with both 2D and 3D shapes
5. Try infinite loops and auto-reverse options

## Notes

- All animation durations are in milliseconds
- Animation names are case-insensitive
- Multiple animations can run simultaneously on the same object
- Use cycleCount=-1 for infinite loops
- Use quotes or parentheses for numeric arguments in commands (e.g. `animationPlay "myAnim" "node" "2000"` or `animationPlay (myAnim) (node) (2000)`) to avoid parsing ambiguities.
- Fill and stroke animations (`ANIMATION_FILL`, `ANIMATION_STROKE`) only support 2D `Shape` instances; they do not apply to 3D nodes.
- `ANIMATION_PAUSE_TRANSITION` exists as a dedicated command entry in the enum.
- `ANIMATION_SET_INTERPOLATOR` applies to `FadeTransition`, `RotateTransition`, `ScaleTransition`, `TranslateTransition`, `FillTransition`, `StrokeTransition`, and `PathTransition`; it is rejected for `Timeline`, `ParallelTransition`, `SequentialTransition`, and other unsupported animation types.
