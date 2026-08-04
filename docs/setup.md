# JDORFX Setup and Test Instructions

This guide explains how to set up the environment, build the JDORFX Java command handler, install the required files, and run the supplied ooRexx test programs.

The project contains a Java command handler named `JavaFXDrawingHandler`, which is packaged as `org.oorexx.handlers.jdorfx.JavaFXDrawingHandler`. ooRexx programs load it through `jdorfx.cls`, then switch their command environment with `address jdorfx`.

---

## 1. Required software

Install the following in this order:

1. **Java JDK with JavaFX included**
   - Recommended for this project: **Liberica Full JDK 8, 64-bit**.
   - A “Full JDK” is important because it includes JavaFX modules.
   - Verify after installation:

     ```cmd
     java -version
     javac -version
     jar --version
     ```

2. **Open Object Rexx**
   - Required: **ooRexx 5.0.0 or newer**, 64-bit.
   - Verify after installation:

     ```cmd
     rexx -version
     ```

3. **BSF4ooRexx850**
   - This provides the bridge between ooRexx and Java.
   - Install it after Java and ooRexx.
   - On Windows, the default installation path is usually:

     ```text
     C:\Program Files\BSF4ooRexx850
     ```

   - On Linux, it's:
     ```
     opt/BSF4ooRexx850
     ```

4. **A terminal or command prompt**
   - On Windows, use `cmd` or PowerShell.
   - On Linux/macOS, use a normal shell terminal.

---

## 2. Check the BSF4ooRexx installation

After installing BSF4ooRexx850, run the included test utility:

```text
opt/BSF4ooRexx850/utilities/ooRexxTry.rxj
```

If the GUI opens and accepts ooRexx commands, the Java/ooRexx bridge is working.

---

## 3. Project files expected

The main project layout is:

```text
src/                 Java command-handler source
samples/             executable ooRexx examples and sample assets
docs/                maintained project documentation
javafx_examples/     small JavaFX reference implementations
build.sh              compile the handler
test.sh               install the development JAR and run a sample
```

The project folder should contain:

```text

src/JavaFXDrawingHandler.java

samples/jdorfx.cls
samples/01_jdorfx_drawing2d.rxj
samples/*.rxj

```

Some examples also require image texture files. Keep those image files in the same relative paths expected by the `.rxj` scripts, or edit the paths inside the scripts.

---

## 4. `jdorfx.cls`

A file named `jdorfx.cls` is provided and used in samples:

```rexx
::routine addJdorFXHandler public
   use strict arg environmentName="JDORFX"

   call BsfCommandHandler "add", -
   environmentName, -
   .bsf~new("org.oorexx.handlers.jdorfx.JavaFXDrawingHandler")

::requires "BSF.CLS"
```

Goal: It registers the Java command handler under the default environment name `JDORFX`.

---

## 5. Build the JDORFX Java package

Open a terminal in the folder containing `JavaFXDrawingHandler.java`.

### 5.1 Compile the Java source file

```cmd
javac -d . JavaFXDrawingHandler.java
```

This should create the package directory:

```text
org\oorexx\handlers\jdorfx
```

Inside it, you should see compiled `.class` files.

### 5.2 Create the JAR file

```cmd
jar -cvf JDORFX.jar org
```

This creates:

```text
JDORFX.jar
```

You may also use a dated name, for example:

```cmd
jar -cvf JDORFX_20260520.jar org
```

Make sure you copy that exact JAR into the BSF4ooRexx library folder.

---

## 6. Install the JDORFX files

### Option A: Install globally for BSF4ooRexx

Copy the files as follows:

Linux:

```text
JDORFX.jar  ->  opt/BSF4ooRexx850/lib
jdorfx.cls  ->  opt/BSF4ooRexx850 - make sure to add this path to your PATH env var
```

Depending on the installation, the BSF profile folder may also be under the user profile.

The important part is that:

- `JDORFX.jar` is on the Java classpath used by BSF4ooRexx.
- `jdorfx.cls` can be found by the `.rxj` scripts.

### Option B: Keep files local to the test folder

You can also keep `jdorfx.cls` in the same folder as the `.rxj` test files.

For the JAR, either place it in the BSF4ooRexx `lib` folder or manually extend the classpath before running the examples.

---

## 7. Structure of a JDORFX ooRexx test program

Each test script should contain this setup pattern:

```rexx
-- create JDORFX handler
call addJdorFXHandler

-- set default environment to JDORFX
address jdorfx

-- JDORFX drawing commands go here
newimage 500 500
winshow
sleep 5

-- get ooRexx-Java bridge and JDORFX handler registration
::requires "jdorfx.CLS"
```

The `call addJdorFXHandler` line registers the Java handler. The `address jdorfx` line tells ooRexx to send later drawing commands to JDORFX instead of the operating system.

---

## 8. Run the supplied test programs

Open a terminal in the folder containing the `.rxj` files and run:

```cmd
rexx 12a_fade_circle.rxj
```

Expected result: each script opens a JavaFX window, draws the scene, sleeps for a few seconds, and then ends.

---

## 9. Common problems and fixes

### `java.lang.ClassNotFoundException: org.oorexx.handlers.jdorfx.JavaFXDrawingHandler`

The JAR is not on the BSF4ooRexx Java classpath.

Fix:

- Rebuild the JAR.
- Copy it into the BSF4ooRexx `lib` folder.
- Restart the terminal before running the `.rxj` script again.

---

### `Could not find file "jdorfx.cls" for ::REQUIRES.` or `requires` error

ooRexx cannot find `jdorfx.cls`.

Fix:

- Put `jdorfx.cls` in the same folder as the `.rxj` script, or
- Put it into the BSF4ooRexx profile folder.

---

### JavaFX classes are missing

If compilation fails because JavaFX packages cannot be found, your JDK does not include JavaFX.

Fix:

- Use a JDK distribution that includes JavaFX, such as Liberica **Full** JDK 8.
- Make sure `java -version` and `javac -version` point to that JDK.

---

### The script runs but no window appears

Possible causes:

- The script did not call `winshow`.
- The JavaFX application thread did not start correctly.
- The program ended too quickly.

Fix:

- Ensure the script contains:

  ```rexx
  winshow
  sleep 5 -- exit after 5 seconds
  ```

  or

  ```rexx
  winshow
  pull . -- exit after pressing enter
  exit 0
  ```

---

### Texture/map examples fail

The map examples require image files.

Fix:

- Check the file paths inside the `.rxj` script.
- Put the image files at the expected locations, or edit the script to use the correct local paths.

---

## 10. Clean rebuild

On Linux/macOS:

```sh
rm -rf org JDORFX.jar
javac -d . JavaFXDrawingHandler.java
jar -cvf JDORFX.jar org
```

---

## 11. Notes for development

- `JavaFXDrawingHandler.java` must keep the package declaration:

  ```java
  package org.oorexx.handlers.jdorfx;
  ```

- The Java class loaded by `jdorfx.cls` must match the package and class name exactly:

  ```text
  org.oorexx.handlers.jdorfx.JavaFXDrawingHandler
  ```

- If you rename the package, class, or JAR, update the build and setup instructions accordingly.

---

## 12. Quick command checklist

```cmd
java -version
javac -version
rexx -version
cd path\to\jdorfx-project
javac -d . JavaFXDrawingHandler.java
jar -cvf JDORFX.jar org
copy JDORFX.jar %WINPROFILE%\BSF4ooRexx850\lib\
copy jdorfx.cls %WINPROFILE%\BSF4ooRexx850\
rexx 01_jdorfx_drawing2d.rxj
```
