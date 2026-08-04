# Oracle JavaFX 8 Animation Basics: JDORFX comparison samples

These pairs reproduce the examples in `3 Animation Basics (Release 8).pdf`. The Java files are clean, runnable JavaFX applications built closely around Oracle's snippets; the `.rxj` files use only existing JDORFX commands.

| Oracle example | JavaFX 8 source | JDORFX source | Fidelity |
| --- | --- | --- | --- |
| 3-1 Fade Transition | `samples/oracle_animation_basics/Example_01_FadeTransition.java` | `samples/oracle_animation_basics/Example_01_FadeTransition.rxj` | Equivalent |
| 3-2 Path Transition | `samples/oracle_animation_basics/Example_02_PathTransition.java` | `samples/oracle_animation_basics/Example_02_PathTransition.rxj` | Same path, duration, repetition, and reversal; orientation limitation below |
| 3-3 Parallel Transition | `samples/oracle_animation_basics/Example_03_ParallelTransition.java` | `samples/oracle_animation_basics/Example_03_ParallelTransition.rxj` | Visually equivalent |
| 3-4 Sequential Transition | `samples/oracle_animation_basics/Example_04_SequentialTransition.java` | `samples/oracle_animation_basics/Example_04_SequentialTransition.rxj` | Visually equivalent |
| 3-5 Basic Timeline | `samples/oracle_animation_basics/Example_05_BasicTimelineAnimation.java` | `samples/oracle_animation_basics/Example_05_BasicTimelineAnimation.rxj` | Visually equivalent; property difference below |
| 3-7 Built-in Interpolator | `samples/oracle_animation_basics/Example_06_BuiltInInterpolator.java` | `samples/oracle_animation_basics/Example_06_BuiltInInterpolator.rxj` | Visually equivalent; property difference below |

## Current limitations

- **Path orientation:** Oracle sets `ORTHOGONAL_TO_TANGENT`, which rotates the rectangle to follow the path tangent. `animationPath` does not expose path orientation, so the JDORFX rectangle follows the exact cubic path without tangent rotation.
- **Transition endpoints:** JDORFX translate and scale transitions expose relative `byX`/`byY` values, not JavaFX's `fromX`/`toX` and `fromX`/`toX` scale setters. The samples incorporate the initial translation in the shape coordinates and use the equivalent delta, producing the same visible motion and scale range.
- **Timeline position property:** JDORFX timelines expose `translateX`, but not `Rectangle.xProperty()`. Starting at geometric x=100 and animating `translateX` to 200 produces the same visible x=100 to x=300 movement.
- **Timeline Events (Example 3-6):** JDORFX keyframes currently contain property values only. They cannot attach an `onFinished` event handler, update text through an `AnimationTimer`, or choose a random translation at a keyframe, so this example cannot be reproduced faithfully without new handler functionality.
- **Custom Interpolator (Examples 3-8 and 3-9):** JDORFX accepts the built-in JavaFX interpolators and `SPLINE(x1,y1,x2,y2)`, but it cannot register an arbitrary Java `Interpolator` subclass. Oracle's `abs(0.5-t)*2` curve is non-monotonic and cannot be represented exactly by one cubic spline, so no misleading approximation is included.

## Running the pairs

Run a JDORFX example from the repository root, for example:

```text
cd samples
rexx oracle_animation_basics/Example_01_FadeTransition.rxj
```

The Java sources use the `oracle_animation_basics` package. Java class names cannot begin with a digit, so the common `Example_01` through `Example_06` prefixes preserve the comparison order while keeping each public class name identical to its filename.
