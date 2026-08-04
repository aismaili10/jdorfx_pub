# JDOR Image Compatibility Layer

## Purpose

The JDORFX image compatibility layer reproduces the original JDOR image commands as closely as reasonably possible while using JavaFX for rendering. Backward compatibility with JDOR command syntax and observable behavior takes priority over introducing a JavaFX-specific image API.

The implementation was derived in this order from:

1. `JDOR Synopsis.html`, the authoritative command specification.
2. `JavaDrawingHandler.java`, the original JDOR Java2D implementation.
3. `src/JavaFXDrawingHandler.java`, the JDORFX architecture and command-handler conventions.

The primary architectural difference is that JDOR draws directly into a `BufferedImage` through `Graphics2D`, whereas JDORFX draws into a JavaFX `Canvas` through `GraphicsContext`. JavaFX `Image` objects are used internally for loaded and registered images. Commands that expose images to Rexx return `BufferedImage` objects through `SwingFXUtils` when this is necessary to preserve the original JDOR interface.

## Implementation summary

| Command | Status | Primary implementation |
|---|---|---|
| `newImage` / `new` | Existing, with compatibility limitation | JavaFX `Canvas` |
| `loadImage` | Implemented | JavaFX `Image` and image registry |
| `drawImage` | Implemented | `GraphicsContext.drawImage` |
| `saveImage` | Implemented | Canvas snapshot, `SwingFXUtils`, `ImageIO` |
| `image` | Implemented | Canvas snapshot or registry lookup, returned as `BufferedImage` |
| `imageCopy` | Implemented | Pixel-level deep copy, returned as `BufferedImage` |
| `imageSize` | Implemented | Canvas or image dimensions |
| `clipboardSet` / `setClipboard` | Implemented | JavaFX system `Clipboard` |
| `clipboardGet` / `getClipboard` | Implemented | JavaFX system `Clipboard` and image registry |
| `clipboardSetWithoutAlpha` / `setClipboardWithoutAlpha` | Implemented | White compositing and `TYPE_INT_RGB` conversion |
| `pushImage` | Implemented | Canvas snapshot and LIFO image stack |
| `popImage` | Implemented | LIFO image stack and `GraphicsContext.drawImage` |
| `printImage` | Implemented | JavaFX `PrinterJob` and print-only `Canvas` |
| `printScale` | Implemented | Stored printing state |
| `printScaleToPage` | Implemented | Stored printing state and proportional page fit |
| `printPos` | Implemented | Stored printing state |
| `imageType` | Intentionally unsupported | No faithful JavaFX equivalent |
| `preferredImageType` | Intentionally unsupported | No faithful JavaFX equivalent |

## Shared image model

JDORFX maintains a case-insensitive image registry by uppercasing nickname keys. The registry stores JavaFX `Image` objects because these can be drawn directly by `GraphicsContext` and placed on the JavaFX clipboard.

When a command accepts an image nickname, lookup generally proceeds as follows:

1. Look for the uppercase nickname in the internal image registry.
2. Where JDOR permits it, look for a Rexx variable with that name.
3. Accept a JavaFX `Image` or a Java `BufferedImage` from the Rexx variable.
4. Convert a `BufferedImage` to JavaFX with `SwingFXUtils.toFXImage` when required.

Commands that return an image to Rexx return a `BufferedImage` where the original JDOR command returned one. This preserves existing Rexx programs that invoke methods such as `getWidth`, `getHeight`, `getRGB`, `getType`, or `getColorModel` on the returned object.

Canvas snapshots and system clipboard operations run on the JavaFX Application Thread. Synchronous `FutureTask` calls are used where the Rexx command thread must wait for a JavaFX result.

`reset` clears the image registry and image stack in addition to restoring the other handler state.

## Command details

### `newImage` / `new`

JDOR syntax:

```text
newImage
newImage width height [type]
```

Original JDOR creates a `BufferedImage`. Its default size is 500 by 500 pixels, and its default type comes from `preferredImageType`, initially `BufferedImage.TYPE_INT_ARGB` (`2`). The optional type accepts either a numeric `BufferedImage` constant or its symbolic name.

JDORFX creates a JavaFX `Canvas` and obtains its `GraphicsContext`. A canvas provides the correct drawing surface and integrates with the existing JavaFX scene architecture, but it has no selectable `BufferedImage` raster type.

Compatibility limitation: the existing JDORFX fourth command token currently represents the JavaFX depth-buffer boolean rather than JDOR's image type. Therefore, a JDOR command such as `newImage 500 500 1` can be interpreted as enabling a depth buffer rather than requesting `TYPE_INT_RGB`. Image-type compatibility cannot be claimed for this argument. A future API cleanup should separate JavaFX depth-buffer selection from the JDOR `newImage` syntax.

### `loadImage`

Syntax:

```text
loadImage imageNickName fileName
```

The complete remainder of the command after `imageNickName` is treated as the filename, allowing paths that contain spaces. Loading is synchronous, matching the behavior of `ImageIO.read` in the original implementation.

JDORFX loads the file through `new Image(InputStream)`, checks `Image.isError`, stores the image under the uppercase nickname, and returns:

```text
width height
```

The canonical command preserves the supplied nickname and complete filename. JavaFX directly supports its standard raster formats, but it may support fewer custom formats than a Java installation extended with additional `ImageIO` plugins.

Required classes: `File`, `FileInputStream`, `javafx.scene.image.Image`.

### `drawImage`

Syntax:

```text
drawImage imageNickName [bkgColor]
drawImage imageNickName width height [bkgColor]
drawImage imageNickName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]
```

Images may come from the registry or a Rexx variable. The image is drawn at the current JDOR position, `currX currY`.

The three forms map to JavaFX as follows:

- Native dimensions: `GraphicsContext.drawImage(image, currX, currY)`.
- Scaled dimensions: the JavaFX destination-width and destination-height overload.
- Source rectangle: the JavaFX source/destination rectangle overload, converting JDOR's second source corner into source width and height.

Java2D provides `drawImage` overloads with a background color; JavaFX does not. JDORFX reproduces the visible result by filling the destination rectangle with the registered background color before compositing the image. The previous JavaFX fill is restored so that `drawImage` does not change the current drawing state.

Numeric values use the handler's normal integer conversion and canonicalization rules. Reversed or negative source/destination rectangles may differ from Java2D because JavaFX does not guarantee all Java2D image-flipping behavior for non-positive dimensions.

Required classes: `GraphicsContext`, `Image`, `BufferedImage`, `SwingFXUtils`, `Color`.

### `saveImage`

Syntax:

```text
saveImage fileName
```

The complete remainder of the command is used as the filename. The format is derived from the filename extension and defaults to PNG when there is no usable extension.

JavaFX has no general image encoder. JDORFX therefore:

1. Snapshots the current canvas on the JavaFX Application Thread.
2. Converts the `WritableImage` to `BufferedImage` with `SwingFXUtils`.
3. Writes the result through `ImageIO.write`.

The command returns `1` on success. A save failure raises the documented JDOR error with `RC=-16`. Canonical output contains the complete filename.

Required classes: `WritableImage`, `SwingFXUtils`, `BufferedImage`, `ImageIO`, `File`.

### `image`

Syntax:

```text
image
image imageNickName
```

With no nickname, JDORFX snapshots the current canvas. With a nickname, it retrieves the registered image. The result is converted to and returned as a `BufferedImage`, matching JDOR's public object type and allowing existing Rexx code to invoke `BufferedImage` methods.

The registry form intentionally follows the synopsis and original source: it does not fall back to a Rexx variable because the purpose of the argument is registry lookup.

Required classes: `WritableImage`, `BufferedImage`, `SwingFXUtils`.

### `imageCopy`

Syntax:

```text
imageCopy
imageCopy imageNickName [bkgColor]
```

With no arguments, the current canvas is copied. A named source may come from the registry or a Rexx variable. JDORFX performs a pixel-level deep copy into a new `WritableImage` and returns an independent `BufferedImage`.

The synopsis lists the optional `bkgColor`, although the original Java implementation accepts only the optional image nickname. JDORFX honors the authoritative synopsis: when a background is supplied, transparent pixels are composited over that registered color while copying. This is additive compatibility and does not change existing JDOR calls.

Required classes: `Image`, `WritableImage`, `PixelReader`, `PixelWriter`, `Color`, `BufferedImage`, `SwingFXUtils`.

### `imageSize`

Syntax:

```text
imageSize
imageSize imageNickName
```

With no argument, the command returns the current canvas dimensions. A named image may come from the registry or a Rexx variable and may be a JavaFX `Image` or `BufferedImage`.

The result is always:

```text
width height
```

An unresolved nickname produces JDOR's nickname error with `RC=-2`. Canonical output includes the nickname when supplied.

### `clipboardSet` / `setClipboard`

Syntax:

```text
clipboardSet
clipboardSet imageNickName
```

With no nickname, the current canvas snapshot is placed on the system clipboard. A named image may come from the registry or a Rexx variable. Clipboard access runs on the JavaFX Application Thread through `Clipboard.getSystemClipboard` and `ClipboardContent.putImage`.

The image used for the operation is returned as a `BufferedImage`, matching JDOR. `setClipboard` is retained as an alias, while canonical output uses `clipboardSet`.

System clipboard behavior remains platform-dependent. In particular, ownership and persistence after process termination are controlled by the desktop environment.

Required classes: `Clipboard`, `ClipboardContent`, `Image`, `BufferedImage`, `SwingFXUtils`.

### `clipboardGet` / `getClipboard`

Syntax:

```text
clipboardGet
clipboardGet imageNickName
```

The command reads an image from the system clipboard. With no nickname, it stores the image under `CLIPBOARD`; otherwise it uppercases and uses the supplied nickname. The returned value is a `BufferedImage`.

If the clipboard does not contain an image, the command raises an error rather than storing a null registry value. `getClipboard` is retained as an alias, while canonical output uses `clipboardGet`.

### `clipboardSetWithoutAlpha` / `setClipboardWithoutAlpha`

Syntax:

```text
clipboardSetWithoutAlpha
clipboardSetWithoutAlpha imageNickName
```

This command uses the same source-resolution rules as `clipboardSet`, but composites the image over white and creates a `BufferedImage.TYPE_INT_RGB` result before placing it on the clipboard.

Creating an opaque JavaFX image alone would not be sufficient: conversion could still return an alpha-capable `BufferedImage`. Explicit `TYPE_INT_RGB` construction preserves JDOR's observable no-alpha behavior, including `returnedImage.getColorModel().hasAlpha() == false`.

`setClipboardWithoutAlpha` is retained as an alias. Canonical output uses `clipboardSetWithoutAlpha`.

Required classes: `BufferedImage`, `Graphics2D`, `SwingFXUtils`, JavaFX clipboard classes.

### `pushImage`

Syntax:

```text
pushImage [imageNickName]
```

JDORFX snapshots the current canvas, converts the snapshot to a `BufferedImage`, and pushes it onto a LIFO `ArrayDeque`. The pushed copy is returned via `RC`.

If a nickname is supplied, the same image content is also converted for the JavaFX registry and stored under the uppercase nickname. Canonical output includes the supplied nickname.

The image stack and registry are exposed through `getState`, as documented by JDOR.

### `popImage`

Syntax:

```text
popImage
```

The top `BufferedImage` is removed from the image stack, converted to JavaFX, and drawn onto the current canvas at `(0,0)`. This mirrors the original source: the popped image is composited onto the current image; it does not replace or resize the canvas.

The popped `BufferedImage` is returned. An empty stack raises JDOR's `RC=-16` error. Canonical output contains no arguments.

Required classes: `ArrayDeque`, `BufferedImage`, `SwingFXUtils`, `GraphicsContext`.

### `printScale`

Syntax:

```text
printScale
printScale scaleX [scaleY]
```

The default value is `1.0 1.0`. Supplying only `scaleX` applies it to both axes. Querying returns the current value; setting returns the previous value, matching JDOR's query/set convention.

These values are used only when `printScaleToPage` is false. During printing, the print graphics context is scaled before the image and its print position are drawn, matching the order in the original `Printable` implementation.

### `printScaleToPage`

Syntax:

```text
printScaleToPage [booleanValue]
```

The default is false (`0`). Accepted boolean spellings follow the handler's BSF4ooRexx boolean parser: `0`, `1`, `false`, `true`, `.false`, and `.true`, without case sensitivity.

When enabled, JDORFX computes:

```text
scaleX = printableWidth / imageWidth
scaleY = printableHeight / imageHeight
scale  = min(scaleX, scaleY)
```

It then draws the image at the printable area's origin with this uniform scale. The entire image therefore fits proportionally on one page. In this mode, `printScale` and `printPos` are ignored.

Querying returns the current setting; setting returns the previous setting. Canonical output uses `.true` or `.false` when named canonical values are enabled.

### `printPos`

Syntax:

```text
printPos
printPos x [y]
```

The default is `0 0`. If `y` is omitted, `x` is used for both coordinates. Values use JDOR's integer conversion and rounding rules. Querying returns the current position; setting returns the previous position.

The original Java source accidentally accesses the first argument even during a no-argument query. JDORFX follows the authoritative synopsis and correctly supports querying with no arguments.

The position is used only when `printScaleToPage` is false.

### `printImage`

Syntax:

```text
printImage
printImage imageNickName
```

With no argument, the current canvas snapshot is printed. A named image may come from the registry or a Rexx variable.

JDORFX uses `PrinterJob.createPrinterJob` and the default printer without opening a print dialog, matching JDOR. A temporary canvas is sized to the default `PageLayout` printable area. The image is drawn according to `printScaleToPage`, `printScale`, and `printPos`, and a single page is submitted with `printPage` followed by `endJob`.

The command has no defined successful return object, matching the original implementation's null return. If no default printer exists or the job cannot complete, the normal handler error condition is raised. Physical output depends on the operating system's JavaFX printer integration.

Required classes: `PrinterJob`, `PageLayout`, `Canvas`, `GraphicsContext`, `Image`.

### `imageType` — intentionally unsupported

JDOR syntax:

```text
imageType [imageNickName]
```

JDOR returns `BufferedImage.getType()`. This number identifies the actual raster and sample model, not merely descriptive metadata.

JavaFX `Canvas` and `Image` do not expose a corresponding image type. A `SwingFXUtils` conversion creates a new representation chosen at the conversion boundary and therefore cannot reveal the original JavaFX storage format. A sidecar integer would be misleading because it would not reproduce the claimed type's alpha, channel order, premultiplication, grayscale, palette, binary quantization, copying, or encoding behavior.

The command could be answered for a Rexx variable that already contains a `BufferedImage`, but implementing only that case would make the command inconsistent for the current canvas and registered images. It therefore remains unsupported rather than returning invented semantics.

### `preferredImageType` — intentionally unsupported

JDOR syntax:

```text
preferredImageType [type]
```

JDOR accepts the `BufferedImage` type constants from `TYPE_CUSTOM` (`0`) through `TYPE_BYTE_INDEXED` (`13`) and uses the selected value when constructing later images.

JavaFX canvases cannot be constructed with selectable RGB, BGR, ARGB, grayscale, indexed, binary, ushort, or custom raster models. Remembering the preference without applying its pixel behavior would violate the command's semantics. Consequently, `preferredImageType` remains unsupported.

## Canonical output and replay

All implemented commands use the primary mixed-case command name from `EnumCommand`. Aliases such as `getClipboard` and `setClipboard` are emitted canonically as `clipboardGet` and `clipboardSet`.

Nicknames are preserved in the canonical command as supplied even though registry keys are uppercased internally. Filenames that may contain spaces are preserved as the complete remainder of the command. Integer-valued coordinates and dimensions follow `bUseInt4numbers`; boolean values use named canonical values when `bUseNames4canonical` is enabled.

This keeps redirected command output suitable for macro replay.

## Verification samples

The following samples serve as executable documentation:

Each JDORFX sample has a matching JDOR sample in the same
`samples/image_commands` directory. Their command bodies are intentionally
identical apart from handler setup, labels, and output filenames. Use
`samples/image_commands/run_pairs.sh 25a` for one pair or pass `all` to run
all eight pairs in order.

| Sample | Coverage |
|---|---|
| `samples/image_commands/25a_jdorfx_load_image.rxj` | Image loading, registry storage, returned dimensions |
| `samples/image_commands/25b_jdorfx_draw_image.rxj` | Native, scaled, source-rectangle, and background drawing |
| `samples/image_commands/25c_jdorfx_save_image.rxj` | Canvas snapshot and PNG encoding |
| `samples/image_commands/25d_jdorfx_image_queries.rxj` | `image`, `imageCopy`, `imageSize`, Rexx image variables |
| `samples/image_commands/25e_jdorfx_clipboard.rxj` | Clipboard aliases, registry storage, no-alpha behavior |
| `samples/image_commands/25f_jdorfx_image_stack.rxj` | Push/pop dimensions, registry entry, pixel restoration |
| `samples/image_commands/25g_jdorfx_print_settings.rxj` | Printing defaults, query/set returns, canonical values |
| `samples/image_commands/25h_jdorfx_image_commands_demo.rxj` | End-to-end integration with an existing 2D drawing workflow; all implemented image commands, with clipboard and physical printing opt-in |

The image and clipboard samples were exercised against the bundled `oorexx_256.png`. Saving produced a valid 64 by 48 RGBA PNG. Stack restoration produced matching saved and current pixel values. Clipboard no-alpha conversion returned a color model with `hasAlpha()` false.

Printing state and the missing-printer error path were tested. Physical printer output was not tested because the development host had no default printer and its print scheduler was not running.

## Remaining compatibility boundaries

- JavaFX image decoding may not include custom `ImageIO` reader plugins available to JDOR.
- Reversed image rectangles may not reproduce all Java2D flipping behavior.
- JavaFX clipboard persistence and supported native clipboard image formats vary by platform.
- JavaFX printing depends on the platform printer implementation.
- JavaFX canvas storage cannot reproduce `BufferedImage` raster-type semantics.
- The existing `newImage` depth-buffer argument conflicts with JDOR's optional image-type argument and should not be treated as type-compatible.
