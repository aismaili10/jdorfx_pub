# JDORFX

JDORFX is a BSF4ooRexx command handler that brings JavaFX drawing, 2D and 3D
shapes, animation, cameras, lights, and image operations to ooRexx scripts.

## Getting started

Build the handler:

```sh
./build.sh
```

Run a sample:

```sh
./test.sh samples/01_jdorfx_drawing2d.rxj
```

`test.sh` may request elevated privileges because it installs the development
JAR into the local BSF4ooRexx library directory.

## Documentation

- [Documentation index](docs/README.md)
- [Setup and testing](docs/setup.md)
- [Command reference](docs/command-reference.md)
- [Animation guide](docs/animation-guide.md)
- [Image compatibility](docs/image-compatibility.md)
- [Architecture](docs/architecture.md)
- [Fixes and JDOR differences](docs/fixes-and-differences.md)
- [Development history](docs/development-history.md)

Runnable examples are under [`samples/`](samples/). Image-command comparisons
have their own [README](samples/image_commands/README.md), as do the paired
[Oracle JavaFX animation examples](samples/oracle_animation_basics/README.md).

## Source of truth

Command names and aliases are defined by `EnumCommand` in
`src/JavaFXDrawingHandler.java`. The documentation describes their public
syntax and behavior; when adding a command, update both places in the same
change.

## License

See [LICENSE](src/licence%20JDORFX.txt).
