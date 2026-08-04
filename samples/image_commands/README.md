# JDOR/JDORFX image-command comparisons

Each numbered pair performs the same image-command operations, first with the
original Java2D JDOR handler and then with the JavaFX JDORFX handler. Handler
setup, window titles, and generated filenames differ so the results remain
identifiable.

Run a pair from its sample directory so the relative image paths resolve:

```sh
cd samples/image_commands
rexx 25a_jdor_load_image.rxj
rexx 25a_jdorfx_load_image.rxj
```

Replace `25a` with another pair from `25b` through `25h` as needed. Build and
install the current JDORFX development JAR before running the JDORFX variants.

Physical printing remains disabled in `25h`; `25g` only compares print settings.
