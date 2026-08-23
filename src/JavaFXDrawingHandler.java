/* Java classes that constitute the JDORFX ("JavaFXDrawing ooRexx") command handler.

   This enables any programmer to easily create and manipulate bitmaps on any
   operating system (Windows, MacOS, Linux) where Java, ooRexx and BSF4ooRexx is
   available.

   based on JDOR ("JavaDrawing ooRexx") by Rony G. Flatscher

   author:  Philipp Schaller
   modified & extended by: Alireza Ismaili 
   date:    20240320
   modiefied: 2026-05-19
   version: 000.20230920
   license: Apache license 2.0

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2023 Rony G. Flatscher
       Copyright (C) 2024 Philipp Schaller

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -----------------------------------------------------------------------------

*/

/*
-----------------------------------------------------------------------------------------
the following changes were implemented into JavaDrawingHandler (implements AWT classes) before the development of JavaFXDrawingHandler

changes:
        2022-10-05: - corrected Frame's currX and currY values
                    - corrected return value for IMAGE_SIZE as num+' '+num would add the
                      numbers with the value of the single blank value to num+" "+num to
                      return a string

        2022-10-20: - corrected drawString argument: make sure to preserve leading blanks
                      by just skipping a single blank after the command, taking the remainder
                      as the whole string

        2022-10-21: - corrected WIN_LOCATION: now honors no arguments
                    - new command "drawImageFromVar": allow an image referred to by a Rexx
                      variable to be drawn (to ease drawing images on different JDOR handler
                      instances)
        2022-10-22: - fold DRAW_IMAGE_FROM_VAR into DRAW_IMAGE implementation
                    - corrected LOAD_IMAGE to return dimension instead of image (cf. documentation)
                    - change logic for queryable and setable commands: if setting new
                      value, return previous value
        2022-10-23: - reworked some WIN_* commands to not automatically create and show JFrame
        2022-10-26: - allow float values to define colors in addition to int values
                    - add synonym POSITION for POS command
        2022-10-27: - added command "copyImage"
        2022-10-28: - translate, scale and shear now return values at time of invocation
                    - new command "transform" to allow querying, setting and resetting Graphics2D's
                      AffineTransform
        2022-10-29: - "transform": uses default AffineTransform() for reset, "reset" argument can
                                   be reduced to first letter ('r' or 'R'), setting argument values
                                   now allows to use the Rexx anonymous variable "." (the dot) which
                                   causes no change in the respective argument's value (its current
                                   value gets reused)
                    - fixed errors in areas tested for the first time (like testing caps or linend values)
                    - added string2int(strValue): allows for supplying float and double values but
                      return an int value rounded with Math.round() in places where floats may be
                      produced (e.g. calculating positions, lengths, widths) but Graphics[2D] methods
                      expect int values instead
        2022-10-30: - add public instance fields "bUseNames4canonical" (true -> replace ints with
                      constant names, else int value) and "bUseInt4numbers" (true -> use int value,
                      else number as supplied)
                    - "font": fixed bug if using one of the operating system independent (logical)
                              fonts named "Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced":
                              now honors also fontSize and fontStyle
        2022-10-31: - fix COMPOSITE (correct int value caused exception)
                    - removed command DRAW_IMAGE_FROM_VAR_NAME, instead changed DRAW_IMAGE to
                      try to fetch the image from a Rexx variable named as the nickName, if an
                      image cannot be found in the registry by that nickname
                    - PRINT_IMAGE now also can take an image (must be a BufferedImage) from a Rexx
                      variable named after the nickname (only if not found in the image registry)
        2022-11-01: - add fontStyles[Int2Name], replace fontStyle numeric value with name, if
                      "bUseName4canonical" is set
                    - new command "imageCopy": returns a copy of an image (maybe for buffering purposes)
                    - new command "stringBounds": returns the Rectangle2D floats for "x y width height"
                      for the supplied string (allows for adjusting the string)
        2022-11-02: - when loading a font try also Font.decode(...) if a replacement font was
                      created; now one can append the style and size information directly after the fontname
                    - turned antialiasing and text antialising on (renderings will look better on
                      high resoulution targets)
                    - added new 'render' command to query or change the settings; note: if animating
                      text (using drawString) it may be necessary to turn off text antialiasing to
                      inhibit a trail/shmear, cf. 3-110_JDOR_animate_composite.rxj as an example
        2022-11-03: - the following commands now also take a Rexx variable named by the supplied
                      nick name, if not found in the registries: BACKGROUND, COLOR, FONT, GRADIENT_PAINT,
                      IMAGE_SIZE, IMAGE_TYPE, PAINT, SET_XOR_MODE, STROKE
        2022-11-05: - changing logic of "font" command:
                      - if sought font is not found, but a replacement font gets supplied then
                        use that but create an error condition to communicate that fact
                      - if sought font could be found via Font.decode() (may include trailing font style
                        and font size information), then do not change currFontType and currFontSize which
                        serve as default values if a font gets created with a name without type and size
                        information
        2022-11-06: - cmd "reset" also resets GC's AffineTransform (translate to 0,0, scale to 1,1,
                      shear to 0,0)
                    - cmd "getState" adds an entry "gc.transform" referring to current AffienTransform object
        2022-12-03: - adding EnumShape enumeration
                    - changing Command enumeration to EnumCommand
                    - adding commands "shape", "fillShape", "drawShape", "clipShape"
                    - adding support for Shapes: Arc2D, CubicCurve2D, Ellipse2D, Line2D, Polygon,
                      QuadCurve2D, Rectangle2D, RoundRectangle2D
        2022-12-03: - drawShape, fillShape, clipShape: if name not in cache, check whether a Rexx variable
                      of that name refers to a Shape object, if so use it, otherwise raise an error condition
                    - add alias names for shapes without the trailing "2D"
        2022-12-07: - adding Path2D shape support, new commands: shapeBounds, pathAppend, pathClose,
                      pathCurrentPoint, pathCurveTo, pathLineTo, pathMoveTo, pathQuatTo, pathReset,
                      pathWindingRule
                    - pathAppend: accepts a Shape or a PathIterator object via Rexx variable
                    - add ability to have named TRANSFORM objects: allows creating affineTransforms
                      via commands to be used in Shape.getPathIterator(affineTransform)
        2023-01-04: - small edits and corrections to error messages
                    - shape abbreviations, at least two letters, added "polygon" as abbreviation
        2023-01-07: - simple text on new commands (shapes and path related)
        2023-01-08: - add "assignTo RexxVarName" command: allows self contained macros to refer to earlier return values
        2023-01-16: - drawString: if not at least a single blank, report error (ok, if "drawString  ")
                    - bUseNames4canonical: if true then also use .true/.false if checkBooleanValue() used
        2023-01-18: - rename command "new" to "newImage" to better self document, add "new" as an alias
                      to allow existing programs to continue to work
        2023-05-30: - GET_STATE: add Rexx variable name to canonical command, if supplied
        2023-05-31: - fixed canonical name for STROKE (cap, join values)
        2023-06-03: - adding floatArrayToRexxArrayExpression(float [] floatArray)
                    - Rexx int and float arrays will be turned into RexxArrayExpression strings for
                      the canonical command form
        2023-06-05: - remove unused variables
                    - always use [int|float]ArrayToRexxExpression() in canonical command (polygon, strokes etc.)
                    - changed resizable to false by default, adding new command "winResizable [.true|.false]"
                    - add Area support (including Area's CAG methods)
        2023-06-06: - allow Rexx variables as second argument to the areaXYZ commands
                    - removed shape abbreviations that were too short to convey any meaning or
                      (LI, PA, PO), changed shape abbreviations to allow inferring better (CU->CUBIC,
                      EL->ELLI, QU->QUAD, RE->RECT, RO->ROUNDRECT)
        2023-06-08: - fixed bugs in Shape area (tested with new sample "1-190_JDOR_shapes.rxj")
                    - fixed (introduced) bug in processCommand() that inhibited processing the
                      redirected commands via redirected input
                    - now handleCommand() will have the triggering command processed when
                      executing redirected input
                    - added synonyms "areaUnion" for "areaAdd" and "areaXor" for "areaExclusiveOr"
        2023-06-11: - added commands "clipboardGet" ("getClipboard"), "clipboardSet" ("setClipboard")
        2023-06-12: - added command "clipboardSetWithoutAlpha" ("setClipboardWithoutAlpha")
        2023-07-18: - fixed pushImage(): if argument given and output redirected now supplies imageNickName
                      if supplied
                    - add "COLOUR" as synonym for "COLOR"

------------------------------------------------------------------------------------------------------------------------------
based on JavaDrawingHandler, the JavaFXDrawingHandler was created (implementing JavaFX)
the following changes were implemented directly into JavaFXDrawingHandler
changes:

        2024-03-02:
                     -The following JDOR commands were directly adopted into JDORFX:
                        WIN_SCREEN_SIZE, SLEEP, ASSIGN_RC

                      -The following JDOR commands were translated into JDORFX implementing JavaFX:
                        WIN_SHOW, WIN_HIDE, WIN_CLOSE, WIN_LOCATION, WIN_ALWAYS_ON_TOP, WIN_TO_BACK, WIN_TO_FRONT,
                        WIN_ALWAYS_ON_TOP_SUPPORTED, WIN_TITLE, WIN_FRAME, WIN_RESIZABLE, WIN_VISIBLE, WIN_UPDATE,
                        WIN_SIZE, SCALE, SHEAR, NEW_IMAGE, GET_STATE, COLOR, BACKGROUND, DRAW_POLYLINE,
                        DRAW_POLYGON, FILL_POLYGON, STROKE, FONT_STYLE, FONT_SIZE, FONT, RESET, MOVE_TO, DRAW_LINE,
                        DRAW_STRING, STRING_BOUNDS, DRAW_OVAL, FILL_OVAL, DRAW_ROUND_RECT, FILL_ROUND_RECT, DRAW_RECT,
                        FILL_RECT, CLEAR_RECT, DRAW_ARC, FILL_ARC, GET_GC, ROTATE, TRANSLATE, TRANSFORM, SHAPE,
                        DRAW_SHAPE, FILL_SHAPE, CLIP_SHAPE, SHAPE_BOUNDS, AREA_ADD, AREA_EXCLUSIVE_OR, AREA_INTERSECT,
                        AREA_SUBTRACT, AREA_TRANSFORM, PATH_APPEND, PATH_CLOSE, PATH_RESET, PATH_CURRENT_POINT,
                        PATH_CLONE, PATH_LINE_TO, PATH_MOVE_TO, PATH_QUAD_TO, PATH_CURVE_TO, PATH_TRANSFORM,
                        PATH_WINDING_RULE

                      -The following JDOR commands were newly created for JDORFX:
                        SHAPE_3D, DRAW_3D_SHAPE, FILL_3D_SHAPE, ROTATE_3D_SHAPE, SCALE_3D_SHAPE, SHEAR_3D_SHAPE,
                        TRANSLATE_3D_SHAPE, CAMERA, SET_CAMERA, LIGHT, SET_LIGHT, MATERIAL, MATERIAL_COLOR,
                        MATERIAL_MAP, SET_MATERIAL

                      -The following JDOR commands were not implemented into JDORFX:
                        PRINT_SCALE_TO_PAGE, PRINT_SCALE, PRINT_POS, CLIPBOARD_GET, CLIPBOARD_SET, CLIPBOARD_SET_WITHOUT_ALPHA,
                        PRINT_IMAGE, PREFERRED_IMAGE_TYPE, IMAGE_TYPE, IMAGE_COPY, IMAGE_SIZE, GRADIENT_PAINT, SET_PAINT_MODE,
                        PAINT, SET_XOR_MODE, COMPOSITE, RENDER, CLIP_REMOVE, CLIP, COPY_AREA, DRAW_3D_RECT, FILL_3D_RECT,
                        LOAD_IMAGE, GET_IMAGE, SAVE_IMAGE, DRAW_IMAGE, PUSH_GC, POP_GC, PUSH_IMAGE, POP_IMAGE, SHAPE_GET_PATH_ITERATOR

------------------------------------------------------------------------------------------------------------------------------
with the goal to extend the functionality of JavaFXDrawingHandler to support animation,
the following changes were implemented directly into JavaFXDrawingHandler
changes:
        2026-05-19:
                     -The following JDORFX commands were newly created for animation support:
                        ANIMATION_PLAY, ANIMATION_PAUSE, ANIMATION_STOP, SET_INTERPOLATOR, TIMELINE, KEY_FRAME, KEY_VALUE,
                        ANIMATION_FADE, ANIMATION_ROTATE, ANIMATION_SCALE, ANIMATION_TRANSLATE, ANIMATION_PATH, ANIMATION_FILL,
                        ANIMATION_STROKE, ANIMATION_SEQUENTIAL, ANIMATION_PARALLEL

                     -To support animation, the following instance fields were added to JavaFXDrawingHandler:
                        HashMap<String, Animation> hmAnimations
                        HashMap<String, Timeline> hmTimelines
                        HashMap<String, KeyValue> hmKeyValues

                     -other changes to support animation:
                        - new parameter for JavaFXDrawingFrame constructor to allow enabling depth buffering for 3D shapes

*/



package org.oorexx.handlers.jdorfx;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.paint.PhongMaterial;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Animation imports
import javafx.animation.*;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.File;
import javax.imageio.ImageIO;   // for loading/saving

import org.apache.bsf.BSFException;
// 2023-06-11
import org.rexxla.bsf.engines.rexx.*;

import java.io.FileInputStream;
import java.util.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;


// ============================================================================

/** Constructor.
 */
class JavaFXDrawingFrame extends Scene
{
    /* static definitions           */
    static final private int prefWidth  = 500;
    static final private int prefHeight = 500;
    static final private boolean prefResizable=false;

    /* instance definitions         */
    boolean bDebug= true; // true
    Pane root = new Pane();
    Canvas canvas = new Canvas();
    Group shapeGroup = new Group();
    Group shape3DGroup = new Group();
    Group lightGroup = new Group();
    int currWidth  = prefWidth;
    int currHeight = prefHeight;
    boolean currFrameVisible = true;    // cf. command "winFrame [.true|.false]"
    boolean currFrameResizable = prefResizable;

    /** Constructor.
     * @param canvas to set size and display on scene
     * @param depthBuffer whether to enable depth buffering (for 3D shapes)
     */
    public JavaFXDrawingFrame(Canvas can, boolean depthBuffer)
    {
        super(new Group(), can.getWidth(), can.getHeight(), depthBuffer);
        this.setRoot(root);
        currWidth = (int) can.getWidth();
        currHeight = (int) can.getHeight();
        root.setPrefSize(currWidth,currHeight);
        canvas = can;
        root.getChildren().add(canvas);
        root.getChildren().add(shapeGroup);
        root.getChildren().add(shape3DGroup);
        root.getChildren().add(lightGroup);
    }
    /** Resizes frame.
     *  @param width  the width in pixel
     *  @param height the width in pixel
     */
    public void resizeSurface (int width, int height)
    {
        root.setPrefSize(width,height);
        canvas.setWidth(width);
        canvas.setHeight(height);
    }
}




// ============================================================================
public class JavaFXDrawingHandler extends Application implements RexxRedirectingCommandHandler
{
    /* static definitions           */
    // default image sizes
    static final public String version = "100.20230718";
    static final private int prefWidth  = 500;
    static final private int prefHeight = 500;

        // a Rexx variable symbol may start with one of the following characters
    static final String startRexxVariableChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_!?";

    static final HashMap<String,Color> predefinedFXColors     = new HashMap<>();

    // BasicStroke
    static final HashMap<String,Integer> endCaps            = new HashMap<>();
    static final HashMap<Integer,String> endCapsInt2Name    = new HashMap<>();
    static final HashMap<String,Integer> lineJoins          = new HashMap<>();
    static final HashMap<Integer,String> lineJoinsInt2Name  = new HashMap<>();

    // FontStyle
    static final HashMap<String,Integer> fontStyles         = new HashMap<>();
    static final HashMap<Integer,String> fontStylesInt2Name = new HashMap<>();

    static final HashMap<String,Integer> fxFontStyles         = new HashMap<>();
    static final HashMap<Integer,String> fxFontStylesInt2Name = new HashMap<>();


    // Arc2D Closures
    static final HashMap<String,Integer> arcClosures         = new HashMap<>();
    static final HashMap<Integer,String> arcClosuresInt2Name = new HashMap<>();

    static final HashMap<String, Integer> arcFXClosures = new HashMap<>();
    static final HashMap<Integer,ArcType> arcFXClosuresInt2Type = new HashMap<>();

    // Path2D winding rules
    static final HashMap<String,Integer> windingRules         = new HashMap<>();
    static final HashMap<Integer,String> windingRulesInt2Name = new HashMap<>();

    static {
        // ----------------------------------------------------------------------
        predefinedFXColors.put("BLACK"        ,  Color.BLACK         );
        predefinedFXColors.put("BLUE"         ,  Color.BLUE         );
        predefinedFXColors.put("CYAN"         ,  Color.CYAN         );
        predefinedFXColors.put("DARK_GRAY"    ,  Color.DARKGRAY    );
        predefinedFXColors.put("GRAY"         ,  Color.GRAY         );
        predefinedFXColors.put("GREEN"        ,  Color.GREEN        );
        predefinedFXColors.put("LIGHT_GRAY"   ,  Color.LIGHTGRAY   );
        predefinedFXColors.put("MAGENTA"      ,  Color.MAGENTA      );
        predefinedFXColors.put("ORANGE"       ,  Color.ORANGE       );
        predefinedFXColors.put("PINK"         ,  Color.PINK         );
        predefinedFXColors.put("RED"          ,  Color.RED          );
        predefinedFXColors.put("WHITE"        ,  Color.WHITE        );
        predefinedFXColors.put("YELLOW"       ,  Color.YELLOW       );

        // ----------------------------------------------------------------------
        // BasicStroke
        endCaps.put("CAP_BUTT"  , 0);
        endCaps.put("CAP_ROUND" , 1);
        endCaps.put("CAP_SQUARE", 2);
        // allow for looking up constant names (key) by value
        endCaps.forEach((K,V)->endCapsInt2Name.put(V,K));
        // ----------------------------------------------------------------------
        lineJoins.put("JOIN_MITER", 0);
        lineJoins.put("JOIN_ROUND", 1);
        lineJoins.put("JOIN_BEVEL", 2);
        // allow for looking up constant names (key) by value
        lineJoins.forEach((K,V)->lineJoinsInt2Name.put(V,K));
// ----------------------------------------------------------------------
        // FontStyle
        fontStyles.put("PLAIN"     , 0);
        fontStyles.put("BOLD"      , 1);
        fontStyles.put("ITALIC"    , 2);
        fontStyles.put("BOLDITALIC", 3);
        // allow for looking up constant names (key) by value
        fontStyles.forEach((K,V)->fontStylesInt2Name.put(V,K));
        // ----------------------------------------------------------------------
        fxFontStyles.put("NORMAL"   , 0);
        fxFontStyles.put("BOLD"      , 1);
        fxFontStyles.put("ITALIC"    , 2);
        fxFontStyles.put("BOLDITALIC", 3);
        // allow for looking up constant names (key) by value
        fxFontStyles.forEach((K,V)->fxFontStylesInt2Name.put(V,K));


        // ----------------------------------------------------------------------
        // Arc2D closures
        arcClosures.put("OPEN"  , 0);
        arcClosures.put("CHORD" , 1);
        arcClosures.put("PIE"   , 2);
        // allow for looking up constant names (key) by value
        arcClosures.forEach((K,V)->arcClosuresInt2Name.put(V,K));

        // ArcFX closures
        arcFXClosures.put("OPEN"  , 0);
        arcFXClosures.put("CHORD" , 1);
        arcFXClosures.put("PIE"   , 2);
        // allow for looking up constant names (key) by value
        arcFXClosuresInt2Type.put(0, ArcType.OPEN);
        arcFXClosuresInt2Type.put(1, ArcType.CHORD);
        arcFXClosuresInt2Type.put(2, ArcType.ROUND);

        // ----------------------------------------------------------------------
        // Path2D windingRules
        windingRules.put("WIND_EVEN_ODD" , 0);
        windingRules.put("WIND_NON_ZERO" , 1);
        // allow for looking up constant names (key) by value
        windingRules.forEach((K,V)->windingRulesInt2Name.put(V,K));
    }


    int nrCommand=0;    // in case many commands, indicates which command may have caused an error or failure condition

    public boolean bUseNames4canonical=true;    // if true: use constant names instead of int values in canonical output
    public boolean bUseInt4numbers    =true;    // if true: use int values instead of the supplied numbers in canonical output

    /* instance definitions         */
    JavaFXDrawingFrame fxframe = null;       // maintance a singleton or empty
    boolean bDebug=false; // true;

    // we draw here
    Canvas canvas = null;
    GraphicsContext canGC = null;

    Group root = new Group();

    // Scene scene = null; // not used
    Stage fxStage = null;

    // default camera with position 0 0 0
    Camera defCamera = new ParallelCamera();

    private boolean changeScene = false;
    public synchronized void setChangeSceneTrue () {
        changeScene = true;
    }
    public synchronized void setChangeSceneFalse () {
        changeScene = false;
    }
    private String frameTitle = "JavaFXDrawingFrame";

    public synchronized void renameFrame (String newTitle) {
        frameTitle = newTitle;

    }

    boolean changeFrame = false;

    public synchronized void setChangeFrameTrue () {
        changeFrame = true;
    }
    public synchronized void setChangeFrameFalse () {
        changeFrame = false;
    }

    boolean changeDecoration = false;
    boolean stageDecorated = true;
    public synchronized void setStageDecorated () {
        stageDecorated = true;
        changeDecoration = true;
    }
    public synchronized void setStageUndecorated () {
        stageDecorated = false;
        changeDecoration = true;
    }

    public synchronized void setchangeDecorationFalse () {
        changeDecoration = false;
    }

    boolean changeBackFront = false;

    public synchronized void setChangeBackFrontFalse () {
        changeBackFront = false;
    }

    boolean winToFront = true;

    public synchronized void setWinToFront () {
        winToFront = true;
        winAlwaysOnTop = false;
        changeBackFront = true;
    }

    public synchronized void setWinToBack () {
        winToFront = false;
        winAlwaysOnTop = false;
        changeBackFront = true;
    }


    boolean changeAlwaysOnTop = false;
    public synchronized void setChangeAlwaysOnTopFalse () {
        changeAlwaysOnTop = false;
    }
    boolean winAlwaysOnTop = false;

    public synchronized void setWinAlwaysOnTopTrue () {
        winAlwaysOnTop = true;
        changeAlwaysOnTop = true;
    }
    public synchronized void setWinAlwaysOnTopFalse () {
        winAlwaysOnTop = false;
        changeAlwaysOnTop = true;
    }



    boolean fxFrameResizable = false;
    public synchronized void setFxFrameResizable () {
        fxFrameResizable = true;
    }
    public synchronized void setFxFrameNonResizable () {
        fxFrameResizable = false;
    }

    int frameX = 0;
    int frameY = 0;
    boolean changeFrameLocation = false;


    public synchronized void moveFrame (int x, int y) {
        frameX = x;
        frameY = y;
        changeFrameLocation = true;
    }

    public synchronized void frameMoved () {
        changeFrameLocation = false;
    }


    private boolean fxVisible = false;

    public synchronized void showFrame () {
        fxVisible = true;
    }
    public synchronized void hideFrame () {
        fxVisible = false;
    }

    private Stage rebuildStage(Stage oldStage, Scene scene)
    {
        logSceneState("rebuildStage(before)", scene);
        refreshVisualState(scene);
        Stage newStage = new Stage();
        newStage.initStyle(stageDecorated ? StageStyle.DECORATED : StageStyle.UNDECORATED);
        newStage.setOnCloseRequest(event -> {
            fxVisible=false;
            activeHandlers.remove(this);
        });
        newStage.setScene(scene);
        newStage.setTitle(frameTitle);
        newStage.sizeToScene();
        oldStage.hide();
        logSceneState("rebuildStage(after)", scene);
        return newStage;
    }

    private void refreshVisualState(Scene scene)
    {
        if (!(scene instanceof JavaFXDrawingFrame))
        {
            return;
        }

        JavaFXDrawingFrame drawingFrame = (JavaFXDrawingFrame) scene;

        Camera currentCamera = scene.getCamera();
        if (currentCamera != null)
        {
            scene.setCamera(null);
            scene.setCamera(currentCamera);
        }

        java.util.List<Node> currentLights = new java.util.ArrayList<>(drawingFrame.lightGroup.getChildren());
        drawingFrame.lightGroup.getChildren().clear();
        for (Node lightNode : currentLights)
        {
            drawingFrame.lightGroup.getChildren().add(lightNode);
        }
    }

    private void logSceneState(String context, Scene scene)
    {
        if (!bDebug)
        {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("[JavaFXDrawingHandler] ").append(context);
        message.append(" handler=@").append(Integer.toHexString(System.identityHashCode(this)));
        message.append(" thread=").append(Thread.currentThread().getName());
        message.append(" | scene=");
        if (scene == null)
        {
            message.append("null");
        }
        else
        {
            message.append(scene.getClass().getSimpleName())
                   .append("@").append(Integer.toHexString(System.identityHashCode(scene)));
            message.append(" camera=");
            Camera camera = scene.getCamera();
            if (camera == null)
            {
                message.append("null");
            }
            else
            {
                message.append(camera.getClass().getSimpleName())
                       .append("@").append(Integer.toHexString(System.identityHashCode(camera)));
            }

            if (scene instanceof JavaFXDrawingFrame)
            {
                JavaFXDrawingFrame drawingFrame = (JavaFXDrawingFrame) scene;
                message.append(" lightGroupChildren=").append(drawingFrame.lightGroup.getChildren().size());
                message.append(" rootChildren=").append(drawingFrame.root.getChildren().size());
            }
        }

        message.append(" | hmCamera=").append(hmCamera.size());
        message.append(" hmLightBase=").append(hmLightBase.size());
        message.append(" fxframe=");
        if (fxframe == null)
        {
            message.append("null");
        }
        else
        {
            message.append(fxframe.getClass().getSimpleName())
                   .append("@").append(Integer.toHexString(System.identityHashCode(fxframe)));
        }
        message.append(" fxStage=");
        if (fxStage == null)
        {
            message.append("null");
        }
        else
        {
            message.append(fxStage.getClass().getSimpleName())
                   .append("@").append(Integer.toHexString(System.identityHashCode(fxStage)))
                   .append(" showing=").append(fxStage.isShowing());
        }

        System.err.println(message.toString());
    }

    private void logVisualDetails(Scene scene)
    {
        if (!bDebug || scene==null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("[JavaFXDrawingHandler] visualDetails | scene=");
        sb.append(scene.getClass().getSimpleName()).append("@").append(Integer.toHexString(System.identityHashCode(scene)));
        sb.append(" width=").append(scene.getWidth()).append(" height=").append(scene.getHeight());
        Camera cam = scene.getCamera();
        if (cam!=null) {
            sb.append(" camera=").append(cam.getClass().getSimpleName())
              .append("@").append(Integer.toHexString(System.identityHashCode(cam)));
            sb.append(" translate=(").append(cam.getTranslateX()).append(",").append(cam.getTranslateY()).append(",").append(cam.getTranslateZ()).append(")");
            sb.append(" nearClip=").append(cam.getNearClip()).append(" farClip=").append(cam.getFarClip());
        } else {
            sb.append(" camera=null");
        }
        if (scene instanceof JavaFXDrawingFrame) {
            JavaFXDrawingFrame df = (JavaFXDrawingFrame) scene;
            sb.append(" lightCount=").append(df.lightGroup.getChildren().size());
            for (int i=0;i<df.lightGroup.getChildren().size();i++) {
                Node n = df.lightGroup.getChildren().get(i);
                sb.append(" [light#").append(i).append(":class=").append(n.getClass().getSimpleName())
                  .append(" trans=(").append(n.getTranslateX()).append(",").append(n.getTranslateY()).append(",").append(n.getTranslateZ()).append(")]");
            }
        }
        System.err.println(sb.toString());
    }

    private void runOnFxThreadAndWait(Runnable action)
    {
        if (Platform.isFxApplicationThread())
        {
            action.run();
            return;
        }

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try
            {
                action.run();
            }
            finally
            {
                latch.countDown();
            }
        });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX update", e);
        }
    }

    private WritableImage snapshotCanvas() throws Exception
    {
        if (Platform.isFxApplicationThread())
        {
            return canvas.snapshot(null, null);
        }

        java.util.concurrent.FutureTask<WritableImage> snapshotTask =
                new java.util.concurrent.FutureTask<>(() -> canvas.snapshot(null, null));
        Platform.runLater(snapshotTask);
        return snapshotTask.get();
    }

    private Image resolveImage(Object slot, String name)
    {
        Image image=hmImages.get(name.toUpperCase());
        if (image!=null) return image;

        try
        {
            Object rexxImage=getContextVariable(slot,name);
            if (rexxImage instanceof Image) return (Image) rexxImage;
            if (rexxImage instanceof BufferedImage)
            {
                return SwingFXUtils.toFXImage((BufferedImage) rexxImage,null);
            }
        }
        catch (Throwable t) {}
        return null;
    }

    private static WritableImage copyImage(Image source, Color background)
    {
        int width=(int) source.getWidth();
        int height=(int) source.getHeight();
        WritableImage copy=new WritableImage(width,height);
        PixelReader reader=source.getPixelReader();
        PixelWriter writer=copy.getPixelWriter();

        for (int y=0;y<height;y++)
        {
            for (int x=0;x<width;x++)
            {
                Color sourceColor=reader.getColor(x,y);
                if (background==null || sourceColor.getOpacity()>=1.0)
                {
                    writer.setColor(x,y,sourceColor);
                    continue;
                }

                double sourceAlpha=sourceColor.getOpacity();
                double backgroundAlpha=background.getOpacity();
                double outputAlpha=sourceAlpha+backgroundAlpha*(1.0-sourceAlpha);
                if (outputAlpha==0.0)
                {
                    writer.setColor(x,y,Color.TRANSPARENT);
                }
                else
                {
                    double red=(sourceColor.getRed()*sourceAlpha+background.getRed()*backgroundAlpha*(1.0-sourceAlpha))/outputAlpha;
                    double green=(sourceColor.getGreen()*sourceAlpha+background.getGreen()*backgroundAlpha*(1.0-sourceAlpha))/outputAlpha;
                    double blue=(sourceColor.getBlue()*sourceAlpha+background.getBlue()*backgroundAlpha*(1.0-sourceAlpha))/outputAlpha;
                    writer.setColor(x,y,new Color(red,green,blue,outputAlpha));
                }
            }
        }
        return copy;
    }

    private Image getClipboardImage() throws Exception
    {
        if (Platform.isFxApplicationThread())
        {
            return Clipboard.getSystemClipboard().getImage();
        }
        java.util.concurrent.FutureTask<Image> clipboardTask =
                new java.util.concurrent.FutureTask<>(() -> Clipboard.getSystemClipboard().getImage());
        Platform.runLater(clipboardTask);
        return clipboardTask.get();
    }

    private boolean setClipboardImage(Image image) throws Exception
    {
        java.util.concurrent.Callable<Boolean> action = () -> {
            ClipboardContent content=new ClipboardContent();
            content.putImage(image);
            return Clipboard.getSystemClipboard().setContent(content);
        };
        if (Platform.isFxApplicationThread()) return action.call();

        java.util.concurrent.FutureTask<Boolean> clipboardTask=new java.util.concurrent.FutureTask<>(action);
        Platform.runLater(clipboardTask);
        return clipboardTask.get();
    }

    private static BufferedImage imageWithoutAlpha(Image source)
    {
        BufferedImage sourceImage=SwingFXUtils.fromFXImage(source,null);
        BufferedImage opaqueImage=new BufferedImage(sourceImage.getWidth(),sourceImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics=opaqueImage.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0,0,opaqueImage.getWidth(),opaqueImage.getHeight());
        graphics.drawImage(sourceImage,0,0,null);
        graphics.dispose();
        return opaqueImage;
    }

    private boolean printImage(Image image) throws Exception
    {
        java.util.concurrent.Callable<Boolean> action = () -> {
            PrinterJob printJob=PrinterJob.createPrinterJob();
            if (printJob==null)
            {
                throw new IllegalStateException("no default printer is available");
            }

            PageLayout pageLayout=printJob.getJobSettings().getPageLayout();
            Canvas printCanvas=new Canvas(pageLayout.getPrintableWidth(),pageLayout.getPrintableHeight());
            GraphicsContext printGC=printCanvas.getGraphicsContext2D();
            if (printScaleToPage)
            {
                double scale=Math.min(pageLayout.getPrintableWidth()/image.getWidth(),
                                      pageLayout.getPrintableHeight()/image.getHeight());
                printGC.scale(scale,scale);
                printGC.drawImage(image,0,0);
            }
            else
            {
                printGC.scale(printScaleX,printScaleY);
                printGC.drawImage(image,printPosX,printPosY);
            }

            boolean printed=printJob.printPage(pageLayout,printCanvas);
            if (printed) return printJob.endJob();
            printJob.cancelJob();
            return false;
        };
        if (Platform.isFxApplicationThread()) return action.call();

        java.util.concurrent.FutureTask<Boolean> printTask=new java.util.concurrent.FutureTask<>(action);
        Platform.runLater(printTask);
        return printTask.get();
    }


    /* current values      */
    int currX = 0;
    int currY = 0;


    int currFontStyle = 0;   // PLAIN by default
    FontWeight currFXFontWeight = FontWeight.NORMAL;   // PLAIN by default---------------------------------------------------------------------------------------
    FontPosture currFXFontPosture = FontPosture.REGULAR;   // PLAIN by default---------------------------------------------------------------------------------------
    int currFontSize  = 12;  // by default
    double currFXFontSize  = 12;  // by default

    boolean currVisible = false;

    boolean currWinUpdate = true;       // update Frame whenever we draw: allows turning updating on/off
    boolean currResizable = false;      // cf. command "winResizable [.true|.false]"

    // JDORFX print settings
    boolean printScaleToPage=false;
    int printPosX=0;
    int printPosY=0;
    double printScaleX=1.0;
    double printScaleY=1.0;
    String currCompositeRule="SRC_OVER";
    double currCompositeAlpha=1.0;


    boolean fxWinUpdate = true;

    public synchronized void setFXWinUpdateTrue () {
        fxWinUpdate = true;
    }
    public synchronized void setFXWinUpdateFalse () {
        fxWinUpdate = false;
    }

    /** Contains always the predefined colors (cf. Javadoc of java.awt.Color).  */
    HashMap<String, Color>         hmFXColors = new HashMap<>(predefinedFXColors );
    HashMap<String, javafx.scene.text.Font>          hmFXFonts  = new HashMap<>();

    HashMap<String,ArrayList<Object>>  hmFXStroke         = new HashMap<>();

    double currStrokeWidth = 1.0;

    StrokeLineCap currStrokeCap = StrokeLineCap.SQUARE;

    StrokeLineJoin currStrokeJoin = StrokeLineJoin.MITER;

    double currMiterLimit = 10.0;

    double [] currStrokeDashArray = null;
    double currStrokeDashOffset = 0.0;


    // 2022-12-03
    HashMap<String, Shape>         hmFXShapes = new HashMap<>();

    HashMap<String, Shape3D>    hm3DShapes = new HashMap<>();

    // Transformations
    HashMap<String, Affine>   hmFXTransforms = new HashMap<>();
    HashMap<String, Affine> hmPathTransforms = new HashMap<>();

    HashMap<String, Camera>   hmCamera = new HashMap<>();
    HashMap<String, LightBase> hmLightBase = new HashMap<>();
    HashMap<String, PhongMaterial> hmMaterial = new HashMap<>();
    // Maps / Textures
    HashMap<String, Image> hmMaps         = new HashMap<>();

    // Images loaded by JDORFX image-management commands
    HashMap<String, Image> hmImages       = new HashMap<>();
    ArrayDeque<BufferedImage> imageStack  = new ArrayDeque<>();
    
    // Animation storage
    HashMap<String, Animation> hmAnimations = new HashMap<>();
    HashMap<String, Timeline> hmTimelines = new HashMap<>();
    HashMap<String, KeyValue> hmKeyValues = new HashMap<>();

    void reset ()   // reset all current variables and caches
    {
        currX            =  0;
        currY            =  0;

        currFontStyle = 0;   // PLAIN by default
        currFXFontWeight = FontWeight.NORMAL;   // PLAIN by default
        currFXFontPosture = FontPosture.REGULAR;   // PLAIN by default
        currFXFontSize  = 12;  // by default

        printScaleToPage=false;
        printPosX=0;
        printPosY=0;
        printScaleX=1.0;
        printScaleY=1.0;
        currCompositeRule="SRC_OVER";
        currCompositeAlpha=1.0;

        if (canGC!=null)
        {
            canGC.setTransform(new Affine());   // reset transform
        }

        canGC = null;

        hmFXColors=new HashMap<>(predefinedFXColors);

        hmFXFonts.clear();

        hmFXStroke.clear();

        hmFXShapes.clear();

        hmFXTransforms.clear();
        hmPathTransforms.clear();

        hm3DShapes.clear();
        logSceneState("reset(before-clear)", fxframe);
        hmCamera.clear();
        hmLightBase.clear();
        hmMaterial.clear();
        hmMaps.clear();
        hmImages.clear();
        imageStack.clear();

        hmAnimations.clear();
        hmTimelines.clear();
        hmKeyValues.clear();
        logSceneState("reset(after-clear)", fxframe);

        if (fxframe!=null && canvas!=null)
        {
            if (bDebug) System.err.println(" /// ---> resizeSurface(): w="+canvas.getWidth()+", h="+canvas.getHeight());
            fxframe.resizeSurface((int) canvas.getWidth(), (int) canvas.getHeight());
        }
    }


    private static Thread initFxThread;
    private static volatile Throwable fxStartupFailure;
    private static final CountDownLatch fxReady = new CountDownLatch(1);
    private static final CopyOnWriteArrayList<JavaFXDrawingHandler> activeHandlers = new CopyOnWriteArrayList<>();
    private static final ConcurrentLinkedDeque<Scene> deque = new ConcurrentLinkedDeque<>();

    private static synchronized void ensureFxApplicationStarted()
    {
        if (initFxThread==null)
        {
            initFxThread=new Thread(() -> {
                try
                {
                    Application.launch(JavaFXDrawingHandler.class);
                }
                catch (Throwable t)
                {
                    fxStartupFailure=t;
                    fxReady.countDown();
                }
            },"JDORFX-Launcher");
            initFxThread.setDaemon(true);
            initFxThread.start();
        }

        try
        {
            fxReady.await();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalThreadStateException("JavaFX Application Thread could not be initialized");
        }
        if (fxStartupFailure!=null)
        {
            throw new IllegalThreadStateException("JavaFX Application Thread could not be initialized: "+fxStartupFailure);
        }
    }

    /** Callback method of this command handler.
     *
     * @param slot opaque argument (needs to be used if invoking the Handler default methods to interact with the Rexx context)
     * @param address the environment name this handler works for
     * @param command the command to process
     */
    public Object handleCommand(Object slot, String address, String command)
    {

        ensureFxApplicationStarted();
        activeHandlers.addIfAbsent(this);

        nrCommand++;        // increase counter

        //hier auf true umstellen-----------------------------------------------------------------------------------------
if (bDebug)    System.err.println("[JavaFXDrawingHandler].handleCommand(slot, address=["+address+"]"
                                   + "command # ["+nrCommand+"]"
                                   + "command=["+command+"]"

                                   );
        Object res=null;
        if (isInputRedirected(slot))    // take commands via redirected input
        {
            res=processCommand(slot, address, command, nrCommand, null);    // process kick-off command first

            String currCommand=null;
            while ( (currCommand=readInput(slot)) != null)   // feed commands from redirected input
            {
                res=processCommand(slot, address, command, nrCommand, currCommand);
                if (checkCondition(slot))   // a Rexx condition got set: stop processing, return immediately
                {
                    return null;
                }
            }
        }
        else    // single command, indicated by null in fifth argument
        {
            res=processCommand(slot, address, command, nrCommand, null);

        }

        // signal FX GUI to update
        setChangeSceneTrue();


        return res;
    }



    /** Process single command
                processCommand(slot, address, command, nrCommand, currCommand);
     * @param slot opaque argument (needs to be used if invoking the Handler default methods to interact with the Rexx context)
     * @param address the environment name this handler works for
     * @param command the command to process
     * @param nrCommand counter to number this command
     * @param currCommand command that was received in redirected modus
     */
    Object processCommand(Object slot, String address, String command, int nrCommand, String currCommand)
    {
        if (currCommand!=null)      // a command from redirected input, then it prevails
        {
            command=currCommand;    // direct command that triggered handler gets ignored in this use case
        }
        String [] arrCommand = null;

        boolean isOR = isOutputRedirected(slot);    // is output redirected ?

        // get the boundaries of the first 15 words
        ArrayList<int[]> alWordBoundaries = getWordBoundaries (command, 15);
        arrCommand = getNonBlankWords(command, alWordBoundaries);

        EnumCommand cmd = null;
        if (arrCommand.length>0)        // empty string
        {
            cmd=EnumCommand.getCommand(arrCommand[0]);    // can be null, if not available
        }

        if (cmd==null)  // unknown command raise error condition
        {
            String commentChars = "-#;/\"'.:!?_";      // regarded as a comment if any used as first non-blank character
            if (arrCommand.length==0 || arrCommand[0].length()==0 || (commentChars).indexOf(arrCommand[0].charAt(0))>=0)   // treat as comment)
            {
                if (isOR)
                {
                   writeOutput(slot, command);  // write unchanged
                }
                return null;
            }
            String errMsg = "unknown command";
            if (isOR)
            {
                writeOutput(slot, "-- FAILURE (unknown command): ["+command+"]");
            }
            return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-1", errMsg );
        }

        // no image as of yet, not the NEW command, hence create a default image
        if (canvas==null && !(cmd==EnumCommand.NEW_IMAGE))
        {
            canvas = new Canvas(prefWidth, prefHeight);
            canGC = (GraphicsContext) canvas.getGraphicsContext2D();
            canGC.setFill(Color.BLACK);
            javafx.scene.text.Font currFXFont = canGC.getFont();
            currFXFontSize = currFXFont.getSize();

        }

        String  canonical = cmd.mixedCase;;
        Object  resultValue = null;

        // process commands; jd may be null !
        try
        {
            switch (cmd)        // process commands
            {
                // ---------------------- display, control related --- Start
                case WIN_SHOW:      // show frame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        if (fxVisible)    // already visible, nothing to do
                        {
                            return null;
                        }

                        if (fxframe==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                        }
                        showFrame();
                        setChangeFrameTrue();
                        break;
                    }

                case WIN_HIDE:      // hide frame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        if (!fxVisible || fxframe==null)   // not visible already, nothing to do
                        {
                            return null;
                        }
                        hideFrame();
                        setChangeFrameTrue();
                        break;
                    }

                case WIN_CLOSE:     // close (exit) JFrame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        if (fxframe==null)       // nothing to do
                        {
                            return null;
                        }

                        fxframe=null;
                        hideFrame();
                        setChangeFrameTrue();
                    }
                    break;

                case WIN_LOCATION:  // synonym WINMOVETO   "winLocation [x y]" get or set JFrame location on screen
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        if (fxframe==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            showFrame();    // make sure frame is visible
                        }

                        int newX = frameX;
                        int newY = frameY;

                        resultValue = newX+" "+newY;  // query current value (to be returned if change occurs)

                        if (arrCommand.length==3)
                        {
                            newX = string2int(arrCommand[1]);
                            newY = string2int(arrCommand[2]);
                            moveFrame(newX,newY);
                            setChangeFrameTrue();
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newX+" "+newY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                                }
                            }
                        }

                        if (isOR)
                        {
                           writeOutput(slot, canonical);  // write canonical form
                        }
                        if (arrCommand.length==1)
                        {
                            // return newX+" "+newY;
                            return resultValue;
                        }
                        break;

                    }

                    // new 2022-09-27
                case WIN_ALWAYS_ON_TOP:             // "winAlwaysOnTop [.true | .false]" get or set of this window state
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        if (fxframe!=null)
                        {
                            resultValue = winAlwaysOnTop;   // query current value (to be returned if change occurs)
                        }
                        else
                        {
                            resultValue = "0";      // false, does not exist
                        }
                    }
                case WIN_TO_BACK:                   // "winToBack" put window into the back
                case WIN_TO_FRONT:                  // "winToFront" put window into the front
                case WIN_ALWAYS_ON_TOP_SUPPORTED:   // "winAlwaysOnTopSupported" get value
                    {
                        if (cmd!=EnumCommand.WIN_ALWAYS_ON_TOP && arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command expects no arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        if (fxframe==null)   // implies creation of JavaFXDrawingFrame
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            if (cmd!=EnumCommand.WIN_ALWAYS_ON_TOP_SUPPORTED)
                            {
                                currVisible=true;
                                showFrame();
                            }
                            else
                            {
                                currVisible=false;
                                hideFrame();
                            }
                        }

                        switch (cmd)
                        {
                            case WIN_ALWAYS_ON_TOP:
                                {
                                    if (arrCommand.length!=1)
                                    {
                                        String newValue = arrCommand[1];
                                        boolean retBoolValue;

                                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                                        {
                                            retBoolValue=getBooleanValue(newValue);  // get value
                                        }
                                        else
                                        {
                                            throw new IllegalArgumentException("the supplied \"winAlwaysOnTo\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                                        }
                                        if (isOR)
                                        {
                                            if (bUseNames4canonical)
                                            {
                                                canonical=canonical+" "+ (retBoolValue ? ".true" : ".false");
                                            }
                                            else    // reuse argument verbatimely
                                            {
                                                canonical=canonical+" "+newValue;
                                            }
                                        }
                                        if (retBoolValue) {
                                            setWinAlwaysOnTopTrue();
                                        }
                                        else {
                                            setWinAlwaysOnTopFalse();
                                        }

                                    }
                                    break;
                                }

                            case WIN_TO_BACK:                   // "winToBack" put window into the back
                                setWinToBack();
                                break;

                            case WIN_TO_FRONT:                  // "winToFront" put window into the front
                                setWinToFront();
                                break;

                            case WIN_ALWAYS_ON_TOP_SUPPORTED:   // "winAlwaysOnTopSupported" get value
                                // retBoolValue=jd.isAlwaysOnTopSupported();
                                resultValue = winAlwaysOnTop;   // query current value (to be returned if change occurs)
                                break;
                        }

                        if (isOR)
                        {
                           writeOutput(slot, canonical);  // write canonical form
                        }

                        setChangeFrameTrue();
                        // return retBoolValue ? "1" : "0";
                        return resultValue;
                    }

                case WIN_TITLE:     // winTitle [newTitle]: query or set title
                    {

                        if (fxframe==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            showFrame();    // make sure frame is visible
                        }
                        resultValue = frameTitle;    // query current value (to be returned if change occurs)
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        int [] pos = (int []) alWordBoundaries.get(0);
                        String newTitle = command.substring(pos[1]+1); // extract String: skip over first trailing blank

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+newTitle);
                        }
                            // no exception, so args o.k.
                        frameTitle = newTitle; // will use invokeLater()
                        break;
                    }

                case WIN_FRAME:     // winFrame [.true|.false]: query or set whether frame is decorated or not
                    {
                        if (fxframe==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            hideFrame();    // frame not visible
                        }

                        resultValue = stageDecorated ? "1" : "0";  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newFrameVisible=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newFrameVisible=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"winFrame\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        // no exception, so args o.k.
                        if (isOR)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newFrameVisible ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot, canonical);
                        }

                        if (newFrameVisible) {
                            setStageDecorated();
                        }
                        else {
                            setStageUndecorated();
                        }

                        setChangeFrameTrue();
                        break;
                    }

                case WIN_RESIZABLE:   // winResizable [.true|.false]: query or set whether frame is resizable
                    {
                        resultValue = fxFrameResizable ? "1" : "0";  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newBooleanValue=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newBooleanValue=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"winResizable\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (fxFrameResizable==newBooleanValue)   // already at the same state, ignore command, but return "old" value
                        {
                            return resultValue;
                        }

                        // no exception, so args o.k.
                        if (isOR && arrCommand.length==2)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }

                            writeOutput(slot,canonical);
                        }

                        if (fxframe==null)
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            if (newBooleanValue)
                            {
                                setFxFrameResizable();    // make frame resizable
                            }
                        }
                        else if (newBooleanValue)
                        {
                            setFxFrameResizable();
                        }
                        else if (!newBooleanValue)
                        {
                            setFxFrameNonResizable();
                        }
                        setChangeFrameTrue();
                        break;
                    }

                case WIN_VISIBLE:     // winVisible [.true|.false]: query or set whether frame is visible
                    {
                        resultValue = fxVisible ? "1" : "0";  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newBooleanValue=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newBooleanValue=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"winVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (fxVisible==newBooleanValue)   // already at the same state, ignore command, but return "old" value
                        {
                            return resultValue;
                        }

                        // no exception, so args o.k.
                        if (isOR && arrCommand.length==2)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }

                            writeOutput(slot,canonical);
                        }

                        if (fxframe==null)
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            if (newBooleanValue)
                            {
                                showFrame();    // make frame is visible
                            }
                        }
                        else if (newBooleanValue)
                        {
                            showFrame();
                        }
                        else if (!newBooleanValue)
                        {
                            hideFrame();
                        }
                        setChangeFrameTrue();

                        break;
                    }

                case WIN_UPDATE:     // "winUpdate [.true|.false]" get or set whether Frame should be updated upon draws/changes
                    {
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return (currWinUpdate ? "1" : "0");
                        }

                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command accepts 1 argument at the most, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newBooleanValue=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newBooleanValue=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"winUpdate\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (newBooleanValue) {
                            setFXWinUpdateTrue();
                        }
                        else {
                            setFXWinUpdateFalse();
                        }

                        if (isOR)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot,canonical);
                        }
                        // return res;
                        break;
                    }

                case WIN_SIZE:      // query or set size
                    {

                        if (fxframe==null)   // implies creation of JavaFXDrawingFrame and make it visible
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                            showFrame();
                        }

                        resultValue = canvas.getWidth()+" "+canvas.getHeight();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        int newWidth  = string2int(arrCommand[1]);
                        int newHeight = string2int(arrCommand[2]);

                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+newWidth+" "+newHeight;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }
                            writeOutput(slot, canonical);
                        }
                            // no exception, so args o.k.
                        fxframe.resizeSurface(newWidth,newHeight);
                        break;
                    }

                case WIN_SCREEN_SIZE:   // query screen size
                    {
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
                            return screenSize.width+" "+screenSize.height;
                        }
                        throw new IllegalArgumentException("this command does not expect any arguments, received "+(arrCommand.length-1)+" instead");
                    }

                case SLEEP:     // sleep for msecs (allows for simple animations)
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        double secs = Double.parseDouble(arrCommand[1]);

                        if (isOR)
                        {
                           writeOutput(slot, canonical+" "+secs);  // write canonical form
                        }
                        try { Thread.sleep((long)(secs*1000)); } catch (Throwable t) {}

                        return null;
                    }

                    // ---------------------- display, control related --- end
                case SCALE:         // "scale x [y]" set scale for x, y; if y omitted, uses x
                case SHEAR:         // "shear x [y]" set scale for x, y; if y omitted, uses x
                    {
                        int argNum=arrCommand.length;
                        if (argNum>3)
                        {
                            throw new IllegalArgumentException("this command needs either no, "
                                    + "one or 2 arguments, received "+(arrCommand.length-1)
                                    +" instead");
                        }

                        // get current settings
                        Affine fxAffine = canGC.getTransform();
                        String strResult=null;
                        double newX=0, newY=0;

                        if (cmd==EnumCommand.SCALE)
                        {
                            strResult=fxAffine.getMxx()+" "+fxAffine.getMyy();
                        }
                        else // SHEAR
                        {
                            strResult=fxAffine.getMxy()+" "+fxAffine.getMyx();
                        }

                        if (argNum>1)   // set value
                        {
                            newX = Double.parseDouble(arrCommand[1]);
                            newY = newX;     // default to X value in case Y value is omitted
                            if (argNum==3)          // Y value supplied, use it
                            {
                                newY=Double.parseDouble(arrCommand[2]);
                            }

                            if (cmd==EnumCommand.SCALE)
                            {
                                // set new Scale values
                                fxAffine.appendScale(newX,newY);
                                // apply affine transform to canvas
                                canGC.setTransform(fxAffine);
                            }
                            else
                            {
                                // set new Shear values
                                fxAffine.appendShear(newX,newY);
                                // apply affine transform to canvas
                                canGC.setTransform(fxAffine);
                            }
                        }
                        if (isOR)
                        {
                           if (argNum>1)
                           {
                               canonical=canonical+" "+newX+" "+newY;
                           }
                           writeOutput(slot, canonical);  // write canonical form
                        }
                        return strResult;       // current/old settings
                    }

                case NEW_IMAGE:       // "new[Image] [width height [depthBuffer]]" creates a new Canvas
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=3 && arrCommand.length!=4)
                        {
                            throw new IllegalArgumentException("this command needs no or 2 (width height) arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width      = prefWidth;
                        int height     = prefHeight;

                        if (arrCommand.length>2)
                        {
                            width  = string2int(arrCommand[1]);
                            height = string2int(arrCommand[2]);
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+width+" "+height;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1] + " " + arrCommand[2];
                                }
                            }
                        }
                            // no exception, so args o.k.
                        if (canvas!=null)     // make sure all old GCs get disposed
                        {
                            canvas=null;
                            canGC = null;
                        }

                        canvas = new Canvas(width, height);     // create new canvas
                        canGC = (GraphicsContext) canvas.getGraphicsContext2D();    // get Graphics context
                        if (arrCommand.length == 4)
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, getBooleanValue(arrCommand[3]));
                            if (isOR)
                            {
                                canonical = canonical + " " + arrCommand[3];
                            }
                        }
                        else
                        {
                            fxframe = new JavaFXDrawingFrame(canvas, false);
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                    }
                    break;

                case LOAD_IMAGE:      // "loadImage imageNickName fileName" loads an image and returns its dimensions
                    {
                        int size = alWordBoundaries.size();
                        if (size<3)
                        {
                            throw new IllegalArgumentException("this command needs 2 arguments (imageNickName filename), received "+(size-1)+" instead");
                        }

                        String nickName = arrCommand[1];
                        int [] pos = alWordBoundaries.get(2);
                        String fileName = command.substring(pos[0]); // filenames may contain blanks

                        Image img;
                        try (FileInputStream input = new FileInputStream(new File(fileName)))
                        {
                            img = new Image(input);
                        }
                        if (img.isError())
                        {
                            throw new IllegalArgumentException("could not load image from file \""+fileName+"\"", img.getException());
                        }

                        hmImages.put(nickName.toUpperCase(),img);
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName+" "+fileName);
                        }
                        return ((int) img.getWidth())+" "+((int) img.getHeight());
                    }

                case DRAW_IMAGE:      // "drawImage imageNickName [width height [srcX1 srcY1 srcX2 srcY2]] [bkgColor]"
                    {
                        int argNum = arrCommand.length;
                        if (argNum!=2 && argNum!=3 && argNum!=4 && argNum!=5 && argNum!=8 && argNum!=9)
                        {
                            throw new IllegalArgumentException("this command needs 1, 2, 3, 4, 7 or 8 arguments, received "+(argNum-1)+" instead");
                        }

                        String name = arrCommand[1];
                        canonical += " "+name;

                        Image img = resolveImage(slot,name);
                        if (img==null)
                        {
                            throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign an image to a Rexx variable named \""+name+"\"");
                        }

                        String colorNickName = null;
                        if (argNum==3) colorNickName=arrCommand[2];
                        else if (argNum==5) colorNickName=arrCommand[4];
                        else if (argNum==9) colorNickName=arrCommand[8];

                        Color bkgColor = null;
                        if (colorNickName!=null)
                        {
                            bkgColor=hmFXColors.get(colorNickName.toUpperCase());
                            if (bkgColor==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered";
                                if (isOR) writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                return createCondition(slot, nrCommand, command, ConditionType.ERROR, "-14", errMsg);
                            }
                        }

                        if (argNum==2 || argNum==3)
                        {
                            double width=img.getWidth();
                            double height=img.getHeight();
                            if (bkgColor!=null)
                            {
                                javafx.scene.paint.Paint oldFill=canGC.getFill();
                                canGC.setFill(bkgColor);
                                canGC.fillRect(currX,currY,width,height);
                                canGC.setFill(oldFill);
                            }
                            canGC.drawImage(img,currX,currY);
                            if (colorNickName!=null) canonical += " "+colorNickName;
                        }
                        else
                        {
                            int width=string2int(arrCommand[2]);
                            int height=string2int(arrCommand[3]);
                            canonical += " "+(bUseInt4numbers ? width : arrCommand[2])+
                                         " "+(bUseInt4numbers ? height : arrCommand[3]);

                            if (bkgColor!=null)
                            {
                                javafx.scene.paint.Paint oldFill=canGC.getFill();
                                canGC.setFill(bkgColor);
                                canGC.fillRect(currX,currY,width,height);
                                canGC.setFill(oldFill);
                            }

                            if (argNum==4 || argNum==5)
                            {
                                canGC.drawImage(img,currX,currY,width,height);
                            }
                            else
                            {
                                int srcX1=string2int(arrCommand[4]);
                                int srcY1=string2int(arrCommand[5]);
                                int srcX2=string2int(arrCommand[6]);
                                int srcY2=string2int(arrCommand[7]);
                                canonical += " "+(bUseInt4numbers ? srcX1 : arrCommand[4])+
                                             " "+(bUseInt4numbers ? srcY1 : arrCommand[5])+
                                             " "+(bUseInt4numbers ? srcX2 : arrCommand[6])+
                                             " "+(bUseInt4numbers ? srcY2 : arrCommand[7]);
                                canGC.drawImage(img,srcX1,srcY1,srcX2-srcX1,srcY2-srcY1,
                                               currX,currY,width,height);
                            }
                            if (colorNickName!=null) canonical += " "+colorNickName;
                        }

                        if (isOR) writeOutput(slot,canonical);
                        break;
                    }

                case SAVE_IMAGE:      // "saveImage fileName" saves the current canvas
                    {
                        int size=alWordBoundaries.size();
                        if (size<2)
                        {
                            throw new IllegalArgumentException("this command needs 1 argument (filename)");
                        }

                        int [] pos=alWordBoundaries.get(1);
                        String fileName=command.substring(pos[0]); // filenames may contain blanks
                        String extension="png";
                        int lastPos=fileName.lastIndexOf('.');
                        if (lastPos>1 && lastPos<fileName.length()-1)
                        {
                            extension=fileName.substring(lastPos+1);
                        }

                        if (isOR) writeOutput(slot,canonical+" "+fileName);
                        try
                        {
                            WritableImage snapshot=snapshotCanvas();
                            boolean writeSuccess=ImageIO.write(SwingFXUtils.fromFXImage(snapshot,null),extension,new File(fileName));
                            if (!writeSuccess)
                            {
                                throw new RuntimeException("fileName=["+fileName+"] hence extension=["+extension+"] failed, try extension \"png\" instead");
                            }
                            return "1";
                        }
                        catch (Throwable t)
                        {
                            return createCondition(slot, nrCommand, command, ConditionType.ERROR, "-16", t.toString());
                        }
                    }

                case GET_IMAGE:       // "image [imageNickName]" returns the current or registered image
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }
                        if (arrCommand.length==1)
                        {
                            if (isOR) writeOutput(slot,canonical);
                            return SwingFXUtils.fromFXImage(snapshotCanvas(),null);
                        }

                        String name=arrCommand[1];
                        Image img=hmImages.get(name.toUpperCase());
                        if (img==null)
                        {
                            throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\"");
                        }
                        if (isOR) writeOutput(slot,canonical+" "+name);
                        return SwingFXUtils.fromFXImage(img,null);
                    }

                case IMAGE_COPY:      // "imageCopy [imageNickName [bkgColor]]"
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments (image nickname and optional background color), received "+(arrCommand.length-1)+" instead");
                        }

                        Image source;
                        if (arrCommand.length==1)
                        {
                            source=snapshotCanvas();
                        }
                        else
                        {
                            String name=arrCommand[1];
                            source=resolveImage(slot,name);
                            if (source==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign an image to a Rexx variable named \""+name+"\"");
                            }
                            canonical += " "+name;
                        }

                        Color background=null;
                        if (arrCommand.length==3)
                        {
                            String colorName=arrCommand[2];
                            background=hmFXColors.get(colorName.toUpperCase());
                            if (background==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorName+"\" is not registered";
                                if (isOR) writeOutput(slot,"-- ERROR (nickname argument): ["+command+"]");
                                return createCondition(slot,nrCommand,command,ConditionType.ERROR,"-14",errMsg);
                            }
                            canonical += " "+colorName;
                        }

                        WritableImage copy=copyImage(source,background);
                        if (isOR) writeOutput(slot,canonical);
                        return SwingFXUtils.fromFXImage(copy,null);
                    }

                case IMAGE_SIZE:      // "imageSize [imageNickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }

                        if (arrCommand.length==1)
                        {
                            if (isOR) writeOutput(slot,canonical);
                            return ((int) canvas.getWidth())+" "+((int) canvas.getHeight());
                        }

                        String name=arrCommand[1];
                        Image img=resolveImage(slot,name);
                        if (img==null)
                        {
                            String errMsg="image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or supply a Rexx variable referring to an image";
                            if (isOR) writeOutput(slot,"-- ERROR (nickname argument): ["+command+"]");
                            return createCondition(slot,nrCommand,command,ConditionType.ERROR,"-2",errMsg);
                        }
                        if (isOR) writeOutput(slot,canonical+" "+name);
                        return ((int) img.getWidth())+" "+((int) img.getHeight());
                    }

                case CLIPBOARD_GET:   // "clipboardGet [imageNickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for storage), received "+(arrCommand.length-1)+" instead");
                        }

                        String imageName=arrCommand.length==2 ? arrCommand[1] : "CLIPBOARD";
                        Image clipboardImage=getClipboardImage();
                        if (clipboardImage==null)
                        {
                            throw new IllegalArgumentException("could not fetch image from system clipboard");
                        }
                        hmImages.put(imageName.toUpperCase(),clipboardImage);
                        if (arrCommand.length==2) canonical += " "+imageName;
                        if (isOR) writeOutput(slot,canonical);
                        return SwingFXUtils.fromFXImage(clipboardImage,null);
                    }

                case CLIPBOARD_SET:
                case CLIPBOARD_SET_WITHOUT_ALPHA:
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }

                        Image source;
                        if (arrCommand.length==1)
                        {
                            source=snapshotCanvas();
                        }
                        else
                        {
                            String imageName=arrCommand[1];
                            source=resolveImage(slot,imageName);
                            if (source==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+imageName+"\" not found, you must first use \"loadImage "+imageName+" filename\" or \"pushImage "+imageName+"\" or assign an image to a Rexx variable named \""+imageName+"\"");
                            }
                            canonical += " "+imageName;
                        }

                        BufferedImage returnedImage;
                        Image clipboardImage;
                        if (cmd==EnumCommand.CLIPBOARD_SET_WITHOUT_ALPHA)
                        {
                            returnedImage=imageWithoutAlpha(source);
                            clipboardImage=SwingFXUtils.toFXImage(returnedImage,null);
                        }
                        else
                        {
                            returnedImage=SwingFXUtils.fromFXImage(source,null);
                            clipboardImage=source;
                        }
                        if (!setClipboardImage(clipboardImage))
                        {
                            throw new IllegalStateException("could not set image on system clipboard");
                        }
                        if (isOR) writeOutput(slot,canonical);
                        return returnedImage;
                    }

                case PUSH_IMAGE:      // "pushImage [imageNickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command expects 1 argument at the most (nickName), received "+(arrCommand.length-1)+" argument(s)");
                        }

                        BufferedImage pushedImage=SwingFXUtils.fromFXImage(snapshotCanvas(),null);
                        imageStack.push(pushedImage);
                        if (arrCommand.length==2)
                        {
                            String imageName=arrCommand[1];
                            hmImages.put(imageName.toUpperCase(),SwingFXUtils.toFXImage(pushedImage,null));
                            canonical += " "+imageName;
                        }
                        if (isOR) writeOutput(slot,canonical);
                        return pushedImage;
                    }

                case POP_IMAGE:       // "popImage"
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not expect arguments, received "+(arrCommand.length-1)+" argument(s)");
                        }
                        if (imageStack.isEmpty())
                        {
                            String errMsg="did not return an image (empty stack)";
                            if (isOR) writeOutput(slot,"-- ERROR (empty stack): ["+command+"]");
                            return createCondition(slot,nrCommand,command,ConditionType.ERROR,"-16",errMsg);
                        }

                        BufferedImage poppedImage=imageStack.pop();
                        canGC.drawImage(SwingFXUtils.toFXImage(poppedImage,null),0,0);
                        if (isOR) writeOutput(slot,canonical);
                        return poppedImage;
                    }

                case PRINT_SCALE_TO_PAGE: // "printScaleToPage [booleanValue]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String oldValue=printScaleToPage ? "1" : "0";
                        if (arrCommand.length==2)
                        {
                            String newValue=arrCommand[1];
                            if (!checkBooleanValue(newValue))
                            {
                                throw new IllegalArgumentException("the supplied \"printScaleToPage\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: \"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                            }
                            printScaleToPage=getBooleanValue(newValue);
                            canonical += " "+(bUseNames4canonical ? (printScaleToPage ? ".true" : ".false") : newValue);
                        }
                        if (isOR) writeOutput(slot,canonical);
                        return oldValue;
                    }

                case PRINT_SCALE:     // "printScale [scaleX [scaleY]]"
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no or 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        String oldValue=printScaleX+" "+printScaleY;
                        if (arrCommand.length>1)
                        {
                            double newScaleX=Double.parseDouble(arrCommand[1]);
                            double newScaleY=arrCommand.length==3 ? Double.parseDouble(arrCommand[2]) : newScaleX;
                            printScaleX=newScaleX;
                            printScaleY=newScaleY;
                            canonical += " "+arrCommand[1];
                            if (arrCommand.length==3) canonical += " "+arrCommand[2];
                        }
                        if (isOR) writeOutput(slot,canonical);
                        return oldValue;
                    }

                case PRINT_POS:       // "printPos [x [y]]"
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command expects no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        String oldValue=printPosX+" "+printPosY;
                        if (arrCommand.length>1)
                        {
                            int newX=string2int(arrCommand[1]);
                            int newY=arrCommand.length==3 ? string2int(arrCommand[2]) : newX;
                            printPosX=newX;
                            printPosY=newY;
                            if (bUseInt4numbers) canonical += " "+newX+" "+newY;
                            else canonical += " "+arrCommand[1]+" "+(arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                        }
                        if (isOR) writeOutput(slot,canonical);
                        return oldValue;
                    }

                case PRINT_IMAGE:     // "printImage [imageNickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }

                        Image imageToPrint;
                        if (arrCommand.length==1)
                        {
                            imageToPrint=snapshotCanvas();
                        }
                        else
                        {
                            String imageName=arrCommand[1];
                            imageToPrint=resolveImage(slot,imageName);
                            if (imageToPrint==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+imageName+"\" not found, you must first use \"loadImage "+imageName+" filename\" or \"pushImage "+imageName+"\" or assign an image to a Rexx variable named \""+imageName+"\"");
                            }
                            canonical += " "+imageName;
                        }
                        if (isOR) writeOutput(slot,canonical);
                        if (!printImage(imageToPrint))
                        {
                            throw new IllegalStateException("the default printer did not complete the print job");
                        }
                        return null;
                    }

                    // create a RexxStringTable, fill it with current settings, stacks, maps and return it;
                    // if optional nameCtxtVariable supplied, store it as a context variable in addition
                case GET_STATE:     // "getstate [ctxtVariableName]" returns a StringTable with current variables, stacks and HashMaps;
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (nameOfContextVariable), received "+(arrCommand.length-1)+" instead");
                        }

                        if (isOR)
                        {
                            if (arrCommand.length==2)   // supply context Rexx variable name to set
                            {
                                canonical = canonical + " " + arrCommand[1];
                            }
                            writeOutput(slot, canonical);
                        }

                        RexxProxy rop = (RexxProxy) newStringTable(slot);   // get a StringTable
                        rop.sendMessage2("SETENTRY", "currX"      , currX);
                        rop.sendMessage2("SETENTRY", "currY"      , currY);

                        rop.sendMessage2("SETENTRY", "canvas"           , canvas );
                        rop.sendMessage2("SETENTRY", "GC"              , canGC    );

                        rop.sendMessage2("SETENTRY", "imageWidth"  , canvas.getWidth());
                        rop.sendMessage2("SETENTRY", "imageHeight" , canvas.getHeight());

                        rop.sendMessage2("SETENTRY", "currVisible"     , fxVisible);
                        rop.sendMessage2("SETENTRY", "currWinUpdate"   , fxWinUpdate);    // if false inhibits updates of Frame


                        rop.sendMessage2("SETENTRY", "background" , fxframe.getFill()       );
                        // current GraphicConfiguration
                        rop.sendMessage2("SETENTRY", "gc.color"      , canGC.getFill()      );
                        rop.sendMessage2("SETENTRY", "gc.font"       , canGC.getFont()      );
                        rop.sendMessage2("SETENTRY", "gc.StrokeWidth", canGC.getLineWidth() );
                        rop.sendMessage2("SETENTRY", "gc.StrokeCap"  , canGC.getLineCap()   );
                        rop.sendMessage2("SETENTRY", "gc.StrokeJoin" , canGC.getLineJoin()  );
                        rop.sendMessage2("SETENTRY", "gc.StrokeDashArray", canGC.getLineDashes()  );
                        rop.sendMessage2("SETENTRY", "gc.StrokeDashOffset", canGC.getLineDashOffset() );
                        rop.sendMessage2("SETENTRY", "gc.transform"  , canGC.getTransform() );

                        // stack and HashMaps
                        rop.sendMessage2("SETENTRY", "colors"     , hmFXColors           );
                        rop.sendMessage2("SETENTRY", "fonts"      , hmFXFonts            );
                        rop.sendMessage2("SETENTRY", "shapes"     , hmFXShapes           );
                        rop.sendMessage2("SETENTRY", "strokes"    , hmFXStroke           );
                        rop.sendMessage2("SETENTRY", "transforms" , hmFXTransforms       );
                        rop.sendMessage2("SETENTRY", "shapes3d"   , hm3DShapes           );
                        rop.sendMessage2("SETENTRY", "cameras"    , hmCamera             );
                        rop.sendMessage2("SETENTRY", "lights"     , hmLightBase          );
                        rop.sendMessage2("SETENTRY", "materials " , hmMaterial           );
                        rop.sendMessage2("SETENTRY", "maps "      , hmMaps               );
                        rop.sendMessage2("SETENTRY", "images"      , hmImages             );
                        rop.sendMessage2("SETENTRY", "imageStack"  , imageStack           );
                        rop.sendMessage2("SETENTRY", "printScaleToPage", (printScaleToPage ? "1" : "0"));
                        rop.sendMessage2("SETENTRY", "printPosX", printPosX);
                        rop.sendMessage2("SETENTRY", "printPosY", printPosY);
                        rop.sendMessage2("SETENTRY", "printScaleX", printScaleX);
                        rop.sendMessage2("SETENTRY", "printScaleY", printScaleY);

                        if (arrCommand.length==2)   // assign to Rexx variable in RexxContext
                        {
                            String contextVariableName = arrCommand[1];
                            setContextVariable(slot, contextVariableName, rop); // set context variable
                        }

                        return rop;
                    }

                case COMPOSITE:     // "composite [SRC_OVER [alpha]]"
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, one, or two arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue=currCompositeRule+" "+currCompositeAlpha;
                        if (arrCommand.length==1)
                        {
                            if (isOR) writeOutput(slot,canonical);
                            return resultValue;
                        }

                        String rule=arrCommand[1].toUpperCase();
                        if (!"SRC_OVER".equals(rule))
                        {
                            throw new IllegalArgumentException("JDORFX currently supports only the SRC_OVER composite rule; received \""+arrCommand[1]+"\"");
                        }

                        double alpha=arrCommand.length==3 ? Double.parseDouble(arrCommand[2]) : 1.0;
                        if (alpha<0.0 || alpha>1.0)
                        {
                            throw new IllegalArgumentException("composite alpha must be between 0.0 and 1.0; received "+alpha);
                        }

                        currCompositeRule=rule;
                        currCompositeAlpha=alpha;
                        canGC.setGlobalAlpha(alpha);
                        if (isOR) writeOutput(slot,canonical+" "+rule+(arrCommand.length==3 ? " "+alpha : ""));
                        return resultValue;
                    }

                case COLOR:         // "color  [colorNickName [r g b [a]]" query current color or set + define new color
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=2 && arrCommand.length!=5
                                                 && arrCommand.length!=6 )
                        {
                            throw new IllegalArgumentException("this command needs either no, 1, 4 or exactly 5 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = canGC.getFill();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        Color fxColor = null;

                        String colorNickName = arrCommand[1];   // get color name
                        if (isOR)
                        {
                            canonical=canonical+" "+colorNickName;
                        }

                        if (arrCommand.length==2)
                        {
                            try {
                                fxColor = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            canGC.setFill(fxColor);
                            canGC.setStroke(fxColor);

                            return resultValue;     // return previous color
                        }

                        int red=0, green=0, blue=0, alpha=0;
                        float fRed=0, fGreen=0, fBlue=0, fAlpha=0;
                        String strArguments=command.substring(alWordBoundaries.get(2)[0]);   // get all arguments
                        boolean bFloat=(strArguments.indexOf('.')>=0);  // if dot in any of the values then use float version

                        if (bFloat)
                        {
                            fRed   = Float.parseFloat(arrCommand[2]);
                            fGreen = Float.parseFloat(arrCommand[3]);
                            fBlue  = Float.parseFloat(arrCommand[4]);
                        }
                        else
                        {
                            red   = Integer.parseInt(arrCommand[2]);
                            green = Integer.parseInt(arrCommand[3]);
                            blue  = Integer.parseInt(arrCommand[4]);
                        }

                        if (arrCommand.length==6)   // alpha supplied, parse and use it
                        {
                            if (bFloat)
                            {
                                fAlpha = Float.parseFloat(arrCommand[5]);
                                fxColor = new Color(fRed, fGreen, fBlue, fAlpha);
                            }
                            else
                            {
                                alpha = Integer.parseInt(arrCommand[5]);
                                fxColor = new Color((double) red/255, (double) green/255, (double) blue/255, (double) alpha/255);

                            }

                            if (isOR)
                            {
                                if (bFloat)
                                {
                                    canonical = canonical + " " + fRed + " " + fGreen + " " + fBlue + " " + fAlpha;
                                }
                                else
                                {
                                    canonical = canonical + " " + red + " " + green + " " + blue + " " + alpha;
                                }
                            }
                        }
                        else
                        {
                            if (bFloat)
                            {
                                fxColor = new Color(fRed, fGreen, fBlue, 1);
                            }
                            else
                            {
                                fxColor = new Color(red, green, blue, 1);
                            }

                            if (isOR)
                            {
                                if (bFloat)
                                {
                                    canonical = canonical + " " + fRed + " " + fGreen + " " + fBlue;
                                }
                                else
                                {
                                    canonical = canonical + " " + red + " " + green + " " + blue;
                                }
                            }
                        }
                        hmFXColors.put(colorNickName.toUpperCase(),fxColor);
                        canGC.setFill(fxColor);
                        canGC.setStroke(fxColor);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return resultValue;     // return previous color
                    }

                case BACKGROUND:     // "background  [colorNickName]" query current background color or set
                    {
                        resultValue = fxframe.getFill();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        if (arrCommand.length==2)
                        {
                            String colorNickName = arrCommand[1];   // get color name
                            if (isOR)
                            {
                                canonical=canonical+" "+colorNickName;
                            }
                            Color fxColor;
                            try {
                                fxColor = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            fxframe.setFill(fxColor);
                            // return color;
                            return resultValue;
                        }
                        throw new IllegalArgumentException("this command needs either no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                    }

                case DRAW_POLYLINE: // "drawPolyline []xPoints []yPoints nPoints
                case DRAW_POLYGON:  // "drawPolygon  []xPoints []yPoints nPoints
                case FILL_POLYGON:  //  "fillPolygon  []xPoints []yPoints nPoints
                    {
                        if (arrCommand.length!=4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 3 (xPointsArray yPointsArray nPoints) arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        int [] xPoints=null, yPoints=null;
                        double [] xFXPoints=null, yFXPoints=null;
                        int    nPoints=0;

                        String strXPoints=arrCommand[1];
                        if (startRexxVariableChar.indexOf(strXPoints.charAt(0))>=0) // a Rexx variable?
                        {
                            xPoints=(int []) getContextVariable(slot, strXPoints);
                        }
                        else    // a Rexx array expression: comma separated list of ints in parentheses (no blanks!)
                        {
                            xPoints=RexxArrayExpressionToIntArray(strXPoints,0);
                            xFXPoints=RexxArrayExpressionToDoubleArray(strXPoints,0);
                        }

                        String strYPoints=arrCommand[2];
                        if (startRexxVariableChar.indexOf(strYPoints.charAt(0))>=0) // a Rexx variable?
                        {
                            yPoints=(int []) getContextVariable(slot, strYPoints);
                        }
                        else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                        {
                            yPoints=RexxArrayExpressionToIntArray(strYPoints,0);
                            yFXPoints=RexxArrayExpressionToDoubleArray(strYPoints,0);
                        }

                        String strNPoints=arrCommand[3];
                        nPoints = Integer.parseInt(strNPoints);

                        //------------------------------------------------------------------------------------------------
                        if (isOR)
                        {
                            // canonical=canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints;
                            canonical=canonical+
                                            " "+intArrayToRexxArrayExpression(xPoints)+
                                            " "+intArrayToRexxArrayExpression(yPoints)+
                                            " "+strNPoints;
                            writeOutput(slot, canonical);
                        }


                        switch (cmd)
                        {
                            case DRAW_POLYLINE:
                                canGC.strokePolyline(xFXPoints,yFXPoints,nPoints);
                                break;
                            case DRAW_POLYGON:
                                canGC.strokePolygon(xFXPoints,yFXPoints,nPoints);
                                break;
                            case FILL_POLYGON:
                                canGC.fillPolygon(xFXPoints,yFXPoints,nPoints);
                                break;
                        }
                        break;
                    }

                case STROKE:        // "stroke [strokeNickName [width [cap join [miterlimit [floatDashArray floatDash] ]]]" query or set stroke
                    {
                    int argNum=arrCommand.length;
                    if (argNum==4 || argNum==6 || argNum>8) // command length of 6 not allowed
                    {
                        throw new IllegalArgumentException("this command needs either no, 2 (strokeNickName width), 4 (strokeNickName width cap join), or 6 arguments (strokeNickName width cap join miterlimit arrDashes dashPhase), received "+(argNum-1)+" instead");
                    }

                    resultValue = currStrokeWidth + " " + currStrokeCap + " " + currStrokeJoin + " " + currMiterLimit + " " + currStrokeDashArray + " " + currStrokeDashOffset;  // query current stroke properties (to be returned if change occurs)

                    if (argNum==1)
                    {
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                    String strokeNickName = arrCommand[1];   // get name
                    if (isOR)
                    {
                        canonical=canonical+" "+strokeNickName;
                    }

                    ArrayList<Object> strokeProperties = new ArrayList<Object>();


                    if (argNum==2)      // fetch stored stroke
                    {
                        strokeProperties = hmFXStroke.get(strokeNickName.toUpperCase());

                        if (strokeProperties==null)
                        {
                            try // try to get from a Rexx variable
                            {
                                //strokeProperties = (ArrayList<Object>) getContextVariable(slot, strokeNickName);
                            }
                            catch (Throwable t) {}

                            if (strokeProperties==null)
                            {
                                String errMsg="stroke with the supplied nickname \""+strokeNickName+"\" is not registered nor is it a Rexx variable referring to a stroke";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-10", errMsg );
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // set stroke properties of supplied nickname to current stroke

                        currStrokeWidth = (double) strokeProperties.get(0);
                        currStrokeCap = (StrokeLineCap) strokeProperties.get(1);
                        currStrokeJoin = (StrokeLineJoin) strokeProperties.get(2);
                        currMiterLimit = (double) strokeProperties.get(3);
                        currStrokeDashArray = (double[]) strokeProperties.get(4);
                        currStrokeDashOffset = (double) strokeProperties.get(5);

                        // set stroke properties to canvas
                        canGC.setLineWidth(currStrokeWidth);
                        canGC.setLineCap(currStrokeCap);
                        canGC.setLineJoin(currStrokeJoin);
                        canGC.setMiterLimit(currMiterLimit);
                        canGC.setLineDashes(currStrokeDashArray);
                        canGC.setLineDashOffset(currStrokeDashOffset);
                        // return stroke;
                        return resultValue;
                    }

                    int width, cap, join; // , miterlimit;
                    float miterlimit;
                    width = string2int(arrCommand[2]);
                    if (isOR)
                    {
                        if (bUseInt4numbers)
                        {
                            canonical = canonical+" "+width;
                        }
                        else
                        {
                            canonical = canonical+" "+arrCommand[2];
                        }
                    }
                    if (argNum==3)
                    {
                        // default values
                        currStrokeCap = StrokeLineCap.SQUARE;
                        currStrokeJoin = StrokeLineJoin.MITER;
                        currMiterLimit = 10.0;
                        currStrokeDashArray = null;
                        currStrokeDashOffset = 0.0;
                    }
                    else
                    {
                        // CAP
                        String tmpCap = arrCommand[3].toUpperCase();
                        if (startRexxVariableChar.indexOf(tmpCap.charAt(0))>=0)    // a symbolic name?
                        {
                            if (!endCaps.containsKey(tmpCap))
                            {
                                throw new IllegalArgumentException("unknown value for \"cap\" argument supplied: ["+tmpCap+"]");
                            }
                            cap=endCaps.get(tmpCap);
                        }
                        else // verbatim int type
                        {
                            cap=Integer.parseInt(tmpCap);
                            if (!endCaps.containsValue(cap))
                            {
                                throw new IllegalArgumentException("unknown value for \"cap\" argument supplied: ["+tmpCap+"]");
                            }
                        }

                        switch (cap) {
                            case 0:
                                currStrokeCap = StrokeLineCap.BUTT;
                                break;
                            case 1:
                                currStrokeCap = StrokeLineCap.ROUND;
                                break;
                            case 2:
                                currStrokeCap = StrokeLineCap.SQUARE;
                                break;
                        }

                        // JOIN
                        String tmpJoin = arrCommand[4].toUpperCase();
                        if (startRexxVariableChar.indexOf(tmpJoin.charAt(0))>=0)    // a symbolic name?
                        {
                            if (!lineJoins.containsKey(tmpJoin))
                            {
                                throw new IllegalArgumentException("unknown value for \"join\" argument supplied: ["+tmpJoin+"]");
                            }
                            join=lineJoins.get(tmpJoin);
                        }
                        else // verbatim int type
                        {
                            join=Integer.parseInt(tmpJoin);
                            if (! lineJoins.containsValue(join))
                            {
                                throw new IllegalArgumentException("unknown value for \"join\" argument supplied: ["+tmpJoin+"]");
                            }
                        }
                        switch (join) {
                            case 0:
                                currStrokeJoin = StrokeLineJoin.MITER;
                                break;
                            case 1:
                                currStrokeJoin = StrokeLineJoin.ROUND;
                                break;
                            case 2:
                                currStrokeJoin = StrokeLineJoin.BEVEL;
                                break;
                        }

                        if (isOR)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+
                                        endCapsInt2Name.get(cap)+" "+
                                        lineJoinsInt2Name.get(join);
                            }
                            else
                            {
                                canonical=canonical+" "+cap+" "+join;
                            }
                        }
                        if (argNum==5)
                        {
                        }
                        else
                        {
                            miterlimit = Float.parseFloat(arrCommand[5]);
                            if (isOR)
                            {
                                canonical=canonical+" "+miterlimit;
                            }
                            float [] dash=null;

                            double [] dashArray= null;
                            float dashPhase;

                            if (argNum>6) // dash float array and dashPhase
                            {
                                String strDash=arrCommand[6];
                                if (startRexxVariableChar.indexOf(strDash.charAt(0))>=0) // a Rexx variable?
                                {
                                    dash=(float []) getContextVariable(slot, strDash);
                                    dashArray=(double []) getContextVariable(slot, strDash);
                                }
                                else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                                {
                                    dash=RexxArrayExpressionToFloatArray(strDash,0);
                                    dashArray=RexxArrayExpressionToDoubleArray(strDash,0);
                                }

                                String strDashPhase=arrCommand[7];
                                dashPhase=Float.parseFloat(strDashPhase);

                                if (isOR)
                                {
                                    canonical=canonical+" "+strDash+" "+strDashPhase;
                                }

                                currMiterLimit = miterlimit;
                                currStrokeDashArray = dashArray;
                                currStrokeDashOffset = dashPhase;
                            }

                        }
                    }

                    currStrokeWidth = width;

                    // save stroke properties for stroke Nickname
                    strokeProperties.add(currStrokeWidth);
                    strokeProperties.add(currStrokeCap);
                    strokeProperties.add(currStrokeJoin);
                    strokeProperties.add(currMiterLimit);
                    strokeProperties.add(currStrokeDashArray);
                    strokeProperties.add(currStrokeDashOffset);

                    hmFXStroke.put(strokeNickName.toUpperCase(),strokeProperties);

                    // set stroke properties to canvas
                    canGC.setLineWidth(currStrokeWidth);
                    canGC.setLineCap(currStrokeCap);
                    canGC.setLineJoin(currStrokeJoin);
                    canGC.setMiterLimit(currMiterLimit);
                    canGC.setLineDashes(currStrokeDashArray);
                    canGC.setLineDashOffset(currStrokeDashOffset);


                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }
                    // return stroke;
                    return resultValue;
                }

                case FONT_STYLE:    // "fontStyle [0=PLAIN | 1=BOLD | 2=ITALIC | 3=BOLD+ITALIC]
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (fontStyle), received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = "" + currFXFontWeight +""+ currFXFontPosture;   // query current value (to be returned if change occurs)

                        if (arrCommand.length==2)
                        {
                            int    fontStyle;
                            String newFontStyle = arrCommand[1].toUpperCase();
                            if (startRexxVariableChar.indexOf(newFontStyle.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!fontStyles.containsKey(newFontStyle))
                                {
                                    throw new IllegalArgumentException("unknown value for \"fontStyle\" argument: \""+arrCommand[1]+"\"");
                                }
                                fontStyle=fontStyles.get(newFontStyle);
                            }
                            else // verbatim int type
                            {
                                fontStyle=Integer.parseInt(newFontStyle);
                                if (!fontStyles.containsValue(fontStyle))
                                {
                                    throw new IllegalArgumentException("unknown value for \"fontStyle\" argument: \""+arrCommand[1]+"\"");
                                }
                            }

                            if (isOR)
                            {
                                canonical = canonical + " " +
                                    (bUseNames4canonical ? fontStylesInt2Name.get(fontStyle) : arrCommand[1]) ;
                            }

                            currFontStyle=fontStyle;


                            switch (fontStyle) {
                                case 0:     // PLAIN
                                    currFXFontWeight = FontWeight.NORMAL;
                                    currFXFontPosture = FontPosture.REGULAR;
                                    break;
                                case 1:     // BOLD
                                    currFXFontWeight = FontWeight.BOLD;
                                    currFXFontPosture = FontPosture.REGULAR;
                                    break;
                                case 2:     // ITALIC
                                    currFXFontWeight = FontWeight.NORMAL;
                                    currFXFontPosture = FontPosture.ITALIC;
                                    break;
                                case 3:     // BOLDITALIC
                                    currFXFontWeight = FontWeight.BOLD;
                                    currFXFontPosture = FontPosture.ITALIC;
                                    break;

                            }

                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case FONT_SIZE:     // "fontSize [size]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (fontSize), received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = "" + currFontSize;   // query current value (to be returned if change occurs)

                        if (arrCommand.length==2)
                        {
                            int newFontSize = string2int(arrCommand[1]);
                                // no exception, so args o.k.
                            if (newFontSize<=0)
                            {
                                throw new IllegalArgumentException("illegal fontSize ["+newFontSize+"]: must not be 0 or less.");
                            }
                            currFontSize = newFontSize;
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newFontSize;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1];
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return resultValue;
                    }

                case FONT:          // "font   [fontNickName [name]" query or set font;  uses currFontStyle, currFontSize; "Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced"
                    {
                        resultValue = canGC.getFont();   // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        javafx.scene.text.Font fxFont = null;
                        String fontNickName = arrCommand[1];   // get font name
                        if (isOR)
                        {
                            canonical = canonical + " " + fontNickName;
                        }

                        // Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced"
                        if (arrCommand.length==2 )
                        {
                            fxFont = hmFXFonts.get(fontNickName.toUpperCase());

                            if (fxFont==null)
                            {
                                try // try to get from a Rexx variable
                                {
                                    fxFont = (javafx.scene.text.Font) getContextVariable(slot, fontNickName);
                                }
                                catch (Throwable t) {}
                                if (fxFont==null)
                                {
                                    String errMsg="font with the supplied nickname \""+fontNickName+"\" is not registered nor is it a Rexx variable referring to a font";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-11", errMsg );
                                }
                            }
                            canGC.setFont(fxFont);
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        // o.k. Font name may contain blanks so use start of third word and all what remains
                        String fontName = command.substring(alWordBoundaries.get(2)[0]);

                        //Font font(String family, FontWeight weight, FontPosture posture, double size)
                        //fxFont = new javafx.scene.text.Font();
                        fxFont = javafx.scene.text.Font.font(fontName, currFXFontWeight, currFXFontPosture, currFontSize);

                        if (isOR)
                        {
                            canonical = canonical + " " + fontName;
                        }

                        hmFXFonts.put(fontNickName.toUpperCase(),fxFont);
                        canGC.setFont(fxFont);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case RESET:     // "reset" synonym: "clear", clears everything, resets
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not take any arguments");
                        }
                        reset();        // reset all current variables and caches
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return null;
                    }

                    // ----------------------

                case MOVE_TO:   // query current coordinates or goto x, y co-ordinate
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        resultValue = currX+" "+currY;   // query current value (to be returned if change occurs)
                        if (arrCommand.length>1)
                        {
                            int newX = string2int(arrCommand[1]);
                            int newY = newX;    // default to newX in case Y is not supplied
                            if (arrCommand.length==3)
                            {
                                newY = string2int(arrCommand[2]);
                            }
                                // no exception, so args o.k.
                            currX = newX;
                            currY = newY;
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+currX+" "+currY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+
                                        (arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        // return currX+" "+currY;
                        return resultValue;
                    }

                case DRAW_LINE:     // "drawLine toX toY"
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int toX = string2int(arrCommand[1]);
                        int toY = string2int(arrCommand[2]);

                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+toX+" "+toY;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }

                            writeOutput(slot,canonical);
                        }
                            // no exception, so args o.k.
                        canGC.strokeLine(currX,currY,toX,toY);

                    }
                    break;

                case DRAW_STRING:   // "drawString text"
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        int [] pos = (int []) al.get(0);    // get boundaries of first non-blank word
                        if (al.size()<2)    // maybe blanks ?
                        {
                            String str = command.substring(pos[1]); // extract String: skip over first trailing blank

                            if (command.length()<= (pos[1]+1))    // not even a blank supplied
                            {
                                throw new IllegalArgumentException("this command needs 1 argument (the string to be drawn)");
                            }

                        }
                            // no exception, so args o.k.
                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        String str = command.substring(pos[1]+1); // extract String: skip over first trailing blank

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+str);
                        }
                        canGC.fillText(str,currX,currY);
                    }
                    break;

                    // 2022-11-01: return bounds of string with current font and gc
                case STRING_BOUNDS:     // ( "stringBounds"             ) ,    //   "stringBounds string": returns width and height)
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        if (al.size()<2)
                        {
                            throw new IllegalArgumentException("this command needs 1 argument (the string to be measured with current font and graphic context)");
                        }
                            // no exception, so args o.k.
                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        int [] pos = (int []) al.get(0);
                        String str = command.substring(pos[1]+1);   // extract String: skip over first trailing blank

                        String strResult = null;

                        // create new text object
                        Text text = new Text(str);
                        // set current font to text
                        text.setFont(canGC.getFont());
                        // set current location to font
                        text.setX(currX);
                        text.setY(currY);

                        // return location and size of text
                        strResult = text.getX()+" "+text.getY()+" "+text.getLayoutBounds().getWidth()+" "+text.getLayoutBounds().getHeight();

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+str);
                        }
                        return strResult;
                    }

                case DRAW_OVAL:     // "drawOval width height"
                case FILL_OVAL:
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
                            // no exception, so args o.k.
                        if (cmd==EnumCommand.DRAW_OVAL)
                        {
                            canGC.strokeOval(currX,currY,width,height);

                        }
                        else
                        {
                            canGC.fillOval(currX,currY,width,height);

                        }
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }

                            writeOutput(slot, canonical);
                        }
                    }
                    break;

                case DRAW_ROUND_RECT:    // "drawRoundRect width height arcWidth arcHeight"
                case FILL_ROUND_RECT:    // "fillRoundRect width height arcWidth arcHeight"
                    if (arrCommand.length!=5)
                    {
                        throw new IllegalArgumentException("this command needs exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                    }
                case DRAW_RECT:          // "drawRect width height"
                case FILL_RECT:          // "fillRect width height"
                case CLEAR_RECT:
                    {
                        boolean isRoundRect = (cmd==EnumCommand.DRAW_ROUND_RECT || cmd==EnumCommand.FILL_ROUND_RECT );
                        if (!isRoundRect && arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }
                        }

                        int arcWidth=-1, arcHeight=-1;
                        if (isRoundRect)
                        {
                            arcWidth  = string2int(arrCommand[3]);
                            arcHeight = string2int(arrCommand[4]);
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+arcWidth+" "+arcHeight;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[3]+" "+arrCommand[4];
                                }
                            }
                        }
                            // no exception, so args o.k.
                        switch (cmd)
                        {
                            case DRAW_RECT:
                                canGC.strokeRect(currX,currY,width,height);

                                break;
                            case FILL_RECT:
                                canGC.fillRect(currX,currY,width,height);

                                break;
                            case CLEAR_RECT:
                                canGC.clearRect(currX,currY,width,height);
                                break;
                            case DRAW_ROUND_RECT:    // "drawRoundRect width height arcWidth arcHeight"
                                canGC.strokeRoundRect(currX,currY,width,height,arcWidth,arcHeight);
                                break;
                            case FILL_ROUND_RECT:    // "fillRoundRect width height arcWidth arcHeight"
                                canGC.fillRoundRect(currX,currY,width,height,arcWidth,arcHeight);
                                break;
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                    }
                    break;

                case DRAW_ARC:  //   "drawArc  width height startAngle arcAngle"
                case FILL_ARC:  //   "fillArc  width height startAngle arcAngle"
                    {
                        if (arrCommand.length!=5)
                        {
                            throw new IllegalArgumentException("this command needs exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width      = string2int(arrCommand[1]);
                        int height     = string2int(arrCommand[2]);
                        int startAngle = string2int(arrCommand[3]);
                        int arcAngle   = string2int(arrCommand[4]);
                            // no exception, so args o.k.
                        if (cmd==EnumCommand.DRAW_ARC)
                        {
                            canGC.strokeArc(currX,currY,width,height,startAngle,arcAngle, ArcType.OPEN);
                        }
                        else
                        {
                            canGC.fillArc(currX,currY,width,height,startAngle,arcAngle,ArcType.ROUND);

                        }
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height+" "+startAngle+" "+arcAngle;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2]+
                                                      " "+arrCommand[3]+" "+arrCommand[4];
                            }
                            writeOutput(slot, canonical);
                        }
                    }
                    break;


                case GET_GC:    // "getGC"    returns current GC (GraphicsContext)
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command must not have an argument, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return canGC;
                    }

                case ROTATE:       // "rotate theta [x y]" query or set rotation
                    {
                        ArrayList<int[]> al = alWordBoundaries;
                        int argNum = al.size();

                        if (argNum!=2 && argNum!=4)
                        {
                            throw new IllegalArgumentException("this command needs exactly one or three arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        double theta = 0;

                        Affine fxAffine = canGC.getTransform();

                        if (argNum>1) // at least theta given
                        {
                            theta = Double.parseDouble(arrCommand[1]);
                            if (isOR)
                            {
                                canonical=canonical+" "+theta;
                            }
                            if (theta!=0)
                            {
                                if (argNum==2)    // just rotate by theta
                                {
                                    // set rotation values
                                    fxAffine.appendRotation(theta);
                                    // apply affine transform to canvas
                                    canGC.setTransform(fxAffine);
                                }
                                else    // if 3 arguments, fetch arg 2 (x) and 3 (y)
                                {
                                    int x = string2int(arrCommand[2]);
                                    int y = string2int(arrCommand[3]);

                                    if (isOR)
                                    {
                                        if (bUseInt4numbers)
                                        {
                                            canonical = canonical+" "+x+" "+y;
                                        }
                                        else
                                        {
                                            canonical = canonical+" "+arrCommand[2]+" "+arrCommand[3];
                                        }
                                    }

                                    // set rotation and pivot values
                                    fxAffine.appendRotation(theta,x,y);
                                    // apply affine transform to canvas
                                    canGC.setTransform(fxAffine);
                                }
                            }

                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return resultValue;
                    }

                case TRANSLATE: // "translate x [y]" query or move origin; if y omitted, uses x
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        // get current affine of canvas
                        Affine fxAffine = canGC.getTransform();

                        String strResult=null;
                        Integer newX=null, newY=null;

                        strResult=canGC.getTransform().getTx()+" "+canGC.getTransform().getTy();
                        if (arrCommand.length>1)
                        {
                            newX = string2int(arrCommand[1]);
                            newY = newX;    // default to newX
                            if (arrCommand.length==3)
                            {
                                newY = string2int(arrCommand[2]);
                            }
                            // set translate values
                            fxAffine.appendTranslation(newX,newY);
                            // apply affine transform to canvas
                            canGC.setTransform(fxAffine);

                        }

                        if (isOR)
                        {
                            if (arrCommand.length>1)
                            {
                                // canonical = canonical+" "+newX+" "+newY;
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newX+" "+newY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+
                                        (arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                                }
                            }
                            writeOutput(slot, canonical);
                        }
                        return strResult;   // return previous setting
                    }

                    // 2022-10-28: "transform { | RESET | translateX translateY scaleX scaleY shearX shearY}" query, change reset Graphics2D's AffineTransform
                    // 2022-12-07: additional option: "transform name translateX translateY scaleX scaleY shearX shearY}", returns AffineTransform
                case TRANSFORM:
                    {
                        int argNum = arrCommand.length;
                        if ((argNum>2 && argNum<7) || argNum>8)
                        {
                            throw new IllegalArgumentException("this command needs exactly no, 1, 6, or 7 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String strReset="RESET";
                        boolean isReset=false;
                        String nickName="";
                        String ucNickName="";

                        Affine fxAffine = canGC.getTransform();

                        double  translateX=fxAffine.getTx(),
                                translateY=fxAffine.getTy(),
                                scaleX    =fxAffine.getMxx(),
                                scaleY    =fxAffine.getMyy(),
                                shearX    =fxAffine.getMxy(),
                                shearY    =fxAffine.getMyx();


                        resultValue=translateX+" "+translateY+" "+
                                scaleX+" "+scaleY+" "+
                                shearX+" "+shearY;

                        if (argNum>1)
                        {
                            Affine newFXAffine = null;

                            nickName=arrCommand[1];

                            if (argNum==2)  // reset or query stored AffineTransform ?
                            {

                                ucNickName=nickName.toUpperCase();

                                if (ucNickName.equals(strReset))
                                {

                                    newFXAffine = new Affine();

                                    isReset=true;

                                    if (isOR)
                                    {
                                        canonical=canonical+" "+strReset;
                                    }

                                }

                                else    // try to get it from cache
                                {

                                    if (isOR)
                                    {
                                        canonical=canonical+" "+nickName;
                                    }

                                    fxAffine = hmFXTransforms.get(ucNickName);

                                    if (fxAffine==null)
                                    {
                                        if (startRexxVariableChar.indexOf(ucNickName.charAt(0))>=0) // a Rexx variable?
                                        {
                                            try
                                            {
                                                fxAffine=(Affine) getContextVariable(slot, ucNickName);
                                            }
                                            catch (Throwable t) {}

                                            if (fxAffine==null)
                                            {
                                                throw new IllegalArgumentException("no transform with name \""+nickName+"\" stored, nor a Rexx variable that refers to an AffineTransform object");
                                            }
                                        }
                                    }

                                    return fxAffine;  // return AffineTransform object
                                }
                            }
                            else if (argNum>6)
                            {
// System.err.println("*** argNum>6: argNum=["+argNum+"] | command # "+nrCommand+": ["+command+"]");
                                if (argNum==7)
                                {
                                    // a dot keeps the current value
                                    translateX = arrCommand[1].equals(".") ? translateX : Double.parseDouble(arrCommand[1]);
                                    translateY = arrCommand[2].equals(".") ? translateY : Double.parseDouble(arrCommand[2]);
                                    scaleX     = arrCommand[3].equals(".") ? scaleX     : Double.parseDouble(arrCommand[3]);
                                    scaleY     = arrCommand[4].equals(".") ? scaleY     : Double.parseDouble(arrCommand[4]);
                                    shearX     = arrCommand[5].equals(".") ? shearX     : Double.parseDouble(arrCommand[5]);
                                    shearY     = arrCommand[6].equals(".") ? shearY     : Double.parseDouble(arrCommand[6]);

                                }
                                else // if (argNum==8)   // we need to store it!
                                {
                                    ucNickName=nickName.toUpperCase();
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+nickName;  // show name to use
                                    }
                                    translateX = arrCommand[2].equals(".") ? translateX : Double.parseDouble(arrCommand[2]);
                                    translateY = arrCommand[3].equals(".") ? translateY : Double.parseDouble(arrCommand[3]);
                                    scaleX     = arrCommand[4].equals(".") ? scaleX     : Double.parseDouble(arrCommand[4]);
                                    scaleY     = arrCommand[5].equals(".") ? scaleY     : Double.parseDouble(arrCommand[5]);
                                    shearX     = arrCommand[6].equals(".") ? shearX     : Double.parseDouble(arrCommand[6]);
                                    shearY     = arrCommand[7].equals(".") ? shearY     : Double.parseDouble(arrCommand[7]);
                                }


                                // Affine(double mxx,double mxy,double mxz,double tx,double myx,double myy,double myz,double ty,double mzx,double mzy,double mzz,double tz)
                               /*
                                mxx - the X coordinate scaling element
                                mxy - the XY coordinate element
                                mxz - the XZ coordinate element
                                tx - the X coordinate translation element
                                myx - the YX coordinate element
                                myy - the Y coordinate scaling element
                                myz - the YZ coordinate element
                                ty - the Y coordinate translation element
                                mzx - the ZX coordinate element
                                mzy - the ZY coordinate element
                                mzz - the Z coordinate scaling element
                                tz - the Z coordinate translation element

                                */

                                newFXAffine = new Affine();

                                newFXAffine.appendTranslation(translateX, translateY);
                                newFXAffine.appendScale(scaleX, scaleY);
                                newFXAffine.appendShear(shearX,shearY);



                            }

                            if (isReset || argNum==7)
                            {
                                // apply affine transform to canvas
                                canGC.setTransform(newFXAffine);
                            }
                            else    // return AffineTransform
                            {
                                hmFXTransforms.put(ucNickName,newFXAffine);
                                resultValue=newFXAffine;
                            }
                        }

                        if (isOR)
                        {
                            if (arrCommand.length>=7)
                            {
                                canonical = canonical+" "+translateX+" "+translateY+" "+
                                                          scaleX+" "+scaleY+" "+
                                                          shearX+" "+shearY;
                            }
                            writeOutput(slot, canonical);
                        }


                        return resultValue;
                    }

                case ASSIGN_RC:     //   "assignRC RexxVariable": assigns current value of RC to "RexxVariable": allows self contained macros
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 (Rexx variable name) argument (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String rxVarName=arrCommand[1];
                        if (startRexxVariableChar.indexOf(rxVarName.charAt(0))<0) // a Rexx variable?
                        {
                            throw new IllegalArgumentException("argument ["+rxVarName+"] is not a valid Rexx symbol for a Rexx variable name");
                        }

                        resultValue = getContextVariable(slot, "RC");   // get the current value of variable RC
                        setContextVariable(slot, rxVarName, resultValue);
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+rxVarName);
                        }
                        break;
                    }

                    // "shape name [type args...]"
                case SHAPE:
                    {
                        int argNum=arrCommand.length;
                        if (arrCommand.length<2)
                        {
                            throw new IllegalArgumentException("this command needs more than "+arrCommand.length+" arguments");
                        }
                        String nickName=arrCommand[1];
                        String ucNickName=nickName.toUpperCase();
                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }
                        Shape fxShape = null;
                        if (argNum==2)      // query Shape from registry
                        {
                            fxShape=hmFXShapes.get(ucNickName);

                            if (fxShape==null)
                            {
                                String errMsg="Shape with the supplied nickname \""+nickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-17", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return fxShape;     // return Shape
                        }

                        // create and store a Shape
                        String strShapeType=arrCommand[2];
                        EnumShape eShape=EnumShape.getShape(strShapeType);
                        if (eShape==null)
                        {
                            String errMsg="Shape type \""+strShapeType+"\" is not supported";
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-18", errMsg );
                        }
                        String canonicalShapeType=eShape.getMixedCase();
                        canonical=canonical+" "+canonicalShapeType; // get canoncial shape type
                        switch (eShape)
                        {

                            case SHAPE_AREA:    //  "SHAPE areaNickName AREA shapeNickName" (can be an Area)
                                {
                                    if (argNum!=4)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly two arguments (\"areaNickName shapeNickName\"), however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-19", errMsg );
                                    }
                                    // check whether shapeNickName exists
                                    String strShapeArg2=arrCommand[3];
                                    Shape arg2FXShape=(Shape) hmFXShapes.get(strShapeArg2.toUpperCase());

                                    if (arg2FXShape==null)
                                    {
                                        String errMsg="area argument named \""+strShapeArg2+"\" is not registered";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (area shape argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-20", errMsg );
                                    }
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+strShapeArg2;
                                    }
                                    // is not a copy but points to same Shape, copy is made at area operations
                                    fxShape = arg2FXShape;

                                    break;
                                }
                                                //  Double(double�x, double�y, double�w, double�h, double�start, double�extent, int�type)
                            case SHAPE_ARC2D:   //  Double(Rectangle2D�ellipseBounds             , double�start, double�extent, int�type)
                                {
                                    if (argNum!=7 && argNum!=10)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 4 (rectangle2d start extent type) or 7 arguments (x y w h start extent type), however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                                    }

                                    Rectangle recFX = null;

                                    double x=-1.0;
                                    double y=-1.0;
                                    double h=-1.0;
                                    double w=-1.0;
                                    double start =-1.0;
                                    double extent=-1.0;
                                    int    type=-1;
                                    String strType=null;
                                    ArcType arcType = null;

                                    if (argNum==7)
                                    {
                                        String strArg3=arrCommand[3];
                                        recFX=(Rectangle) hmFXShapes.get(strArg3.toUpperCase());

                                        if (recFX==null)  // not registered, try Rexx variable instead
                                        {
                                            try
                                            {
                                                recFX=(Rectangle) getContextVariable(slot,strArg3);
                                            }
                                            catch (Throwable t) {}
                                        }
                                        if (recFX==null)
                                        {
                                            String errMsg="rectangle2d argument named \""+strArg3+"\" is not registered nor is it a Rexx variable referring to a Rectangle2D shape";
                                            if (isOR)
                                            {
                                                writeOutput(slot, "-- ERROR (rectangle2d argument): ["+command+"]");
                                            }
                                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-22", errMsg );
                                        }

                                        if (isOR)
                                        {
                                            canonical=canonical+" "+strArg3;
                                        }
                                        start =Double.parseDouble(arrCommand[4]);
                                        extent=Double.parseDouble(arrCommand[5]);
                                        strType=arrCommand[6];

                                    }
                                    else
                                    {
                                        x=Double.parseDouble(arrCommand[3]);
                                        y=Double.parseDouble(arrCommand[4]);
                                        h=Double.parseDouble(arrCommand[5]);
                                        w=Double.parseDouble(arrCommand[6]);
                                        if (isOR)
                                        {
                                            canonical=canonical+" "+x+" "+y+" "+h+" "+w;
                                        }
                                        start =Double.parseDouble(arrCommand[7]);
                                        extent=Double.parseDouble(arrCommand[8]);
                                        strType=arrCommand[9];

                                    }

                                    // check validy of type argument!
                                    String ucStringType = strType.toUpperCase();
                                    if (startRexxVariableChar.indexOf(ucStringType.charAt(0))>=0)    // a symbolic name?
                                    {
                                        if (!arcClosures.containsKey(ucStringType))
                                        {
                                            throw new IllegalArgumentException("unknown value for \"type\" argument supplied: ["+strType+"]");
                                        }
                                        type=arcClosures.get(ucStringType);

                                    }
                                    else // verbatim int type
                                    {
                                        type=string2int(strType);
                                        if (!arcClosures.containsValue(type))
                                        {
                                            throw new IllegalArgumentException("unknown value for \"type\" argument supplied: ["+strType+"]");
                                        }
                                    }

                                    arcType = arcFXClosuresInt2Type.get(type);

                                    if (isOR)
                                    {
                                        canonical=canonical+" "+start+" "+extent+" ";
                                        if (bUseNames4canonical)
                                        {
                                            canonical=canonical+arcClosuresInt2Name.get(type);
                                        }
                                        else
                                        {
                                            canonical=canonical+type;
                                        }
                                    }

                                    // declaration of type parameter does not work for fxShape, hence create newFXShape
                                    Arc newFXShape = new Arc();
                                    if (argNum==7)
                                    {
                                        // turning parameters to match javafx arc
                                        //public Arc(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length)
                                        double centerX = recFX.getX() + recFX.getWidth()/2;
                                        double centerY = recFX.getY() + recFX.getHeight()/2;
                                        double radiusX = recFX.getWidth() / 2;
                                        double radiusY = recFX.getHeight() / 2;

                                        newFXShape=new Arc(centerX,centerY,radiusX,radiusY,start,extent);
                                    }
                                    else
                                    {
                                        // turning parameters to match javafx arc
                                        //public Arc(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length)
                                        newFXShape= new Arc(x + w/2,y + h/2,w/2,h/2,start,extent);

                                    }

                                    newFXShape.setType(arcType);

                                    fxShape = newFXShape;

                                    break;
                                }

                            case SHAPE_CUBIC_CURVE2D:   // Double(double�x1, double�y1, double�ctrlx1, double�ctrly1, double�ctrlx2, double�ctrly2, double�x2, double�y2)
                                {
                                    if (argNum!=11)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 8 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-23", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);
                                    double arg5=Double.parseDouble(arrCommand[7]);
                                    double arg6=Double.parseDouble(arrCommand[8]);
                                    double arg7=Double.parseDouble(arrCommand[9]);
                                    double arg8=Double.parseDouble(arrCommand[10]);
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4+" "+arg5+" "+arg6+" "+arg7+" "+arg8;
                                    }
                                    fxShape = new CubicCurve(arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8);

                                    break;
                                }

                            case SHAPE_ELLIPSE2D:   // Double(double�x, double�y, double�w, double�h)
                            case SHAPE_LINE2D:      // Double(double�x1, double�y1, double�x2, double�y2)
                            case SHAPE_RECTANGLE2D: // Double(double�x, double�y, double�w, double�h)
                                {
                                    if (argNum!=7)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 4 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-24", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);

                                    if (eShape==EnumShape.SHAPE_ELLIPSE2D)
                                    {
                                        //javafx Ellipse(double centerX, double centerY, double radiusX, double radiusY)
                                        // converting input to match javafx ellipse arguments
                                        double radiusX = arg3 / 2;
                                        double radiusY = arg4 / 2;
                                        double centerX = arg1 + radiusX;
                                        double centerY = arg2 + radiusY;

                                        fxShape = new javafx.scene.shape.Ellipse(centerX,centerY,radiusX,radiusY);

                                    }
                                    else if (eShape==EnumShape.SHAPE_LINE2D)
                                    {
                                        fxShape = new javafx.scene.shape.Line(arg1,arg2,arg3,arg4);
                                    }
                                    else
                                    {
                                        fxShape = new Rectangle(arg1,arg2,arg3,arg4);

                                    }
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4;
                                    }
                                    break;
                                }

                            case SHAPE_POLYGON:     // Polygon(int[]�xpoints, int[]�ypoints, int�npoints)
                                {
                                    if (arrCommand.length!=6)
                                    {
                                        throw new IllegalArgumentException("this command needs exactly 3 (xPointsArray yPointsArray nPoints) arguments, received "+(arrCommand.length-3)+" instead");
                                    }

                                    int [] xPoints=null, yPoints=null;
                                    int    nPoints=0;

                                    String strXPoints=arrCommand[3];
                                    if (startRexxVariableChar.indexOf(strXPoints.charAt(0))>=0) // a Rexx variable?
                                    {
                                        xPoints=(int []) getContextVariable(slot, strXPoints);
                                    }
                                    else    // a Rexx array expression: comma separated list of ints in parentheses (no blanks!)
                                    {
                                        xPoints=RexxArrayExpressionToIntArray(strXPoints,0);
                                    }

                                    String strYPoints=arrCommand[4];
                                    if (startRexxVariableChar.indexOf(strYPoints.charAt(0))>=0) // a Rexx variable?
                                    {
                                        yPoints=(int []) getContextVariable(slot, strYPoints);
                                    }
                                    else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                                    {
                                        yPoints=RexxArrayExpressionToIntArray(strYPoints,0);
                                    }

                                    String strNPoints=arrCommand[5];
                                    nPoints = Integer.parseInt(strNPoints);
                                    if (isOR)
                                    {
                                        // canonical=canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints;
                                        canonical=canonical+
                                                        " "+intArrayToRexxArrayExpression(xPoints)+
                                                        " "+intArrayToRexxArrayExpression(yPoints)+
                                                        " "+strNPoints;
                                    }

                                    fxShape = new Polygon();
                                    for (int i = 0; i < xPoints.length; i++) {
                                        ((Polygon) fxShape).getPoints().add((double) xPoints[i]);
                                        ((Polygon) fxShape).getPoints().add((double) yPoints[i]);

                                    }
                                    break;
                                }

                            case SHAPE_QUAD_CURVE2D:        //  Double(double�x1, double�y1, double�ctrlx, double�ctrly, double�x2, double�y2)
                            case SHAPE_ROUND_RECTANGLE2D:   // Double(double�x, double�y, double�w, double�h, double�arcWidth, double�arcHeight)
                                {
                                    if (argNum!=9)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 6 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-25", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);
                                    double arg5=Double.parseDouble(arrCommand[7]);
                                    double arg6=Double.parseDouble(arrCommand[8]);
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4+" "+arg5+" "+arg6;
                                    }

                                    if (eShape==EnumShape.SHAPE_QUAD_CURVE2D)
                                    {
                                        fxShape=new QuadCurve(arg1,arg2,arg3,arg4,arg5,arg6);
                                    }
                                    else
                                    {
                                        fxShape= new Rectangle(arg1,arg2,arg3,arg4);
                                        ((Rectangle) fxShape).setArcWidth(arg5);
                                        ((Rectangle) fxShape).setArcHeight(arg6);
                                    }
                                    break;
                                }

                            case SHAPE_PATH2D:   // shape shapeName pa[th[2d]] [WIND_NON_ZERO=1]
                                {
                                    if (argNum<3 || argNum>4)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs no or exactly 1 argument, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                                    }

                                    int windingType=1;  // default to WIND_NON_ZERO (1)

                                    if (argNum==4)
                                    {
                                        String strType=arrCommand[3];
                                        // check validy of type argument!
                                        String ucStringType = strType.toUpperCase();
                                        if (startRexxVariableChar.indexOf(ucStringType.charAt(0))>=0)    // a symbolic name?
                                        {
                                            if (!windingRules.containsKey(ucStringType))
                                            {
                                                throw new IllegalArgumentException("unknown value for \"windingType\" argument supplied: ["+strType+"]");
                                            }
                                            windingType=windingRules.get(ucStringType);
                                        }
                                        else // verbatim int type
                                        {
                                            windingType=string2int(strType);
                                            if (!windingRules.containsValue(windingType))
                                            {
                                                throw new IllegalArgumentException("unknown value for \"windingType\" argument supplied: ["+strType+"]");
                                            }
                                        }
                                    }

                                    if (isOR)
                                    {
                                        if (bUseNames4canonical)
                                        {
                                            canonical=canonical+" "+windingRulesInt2Name.get(windingType);
                                        }
                                        else
                                        {
                                            canonical=canonical+" "+windingType;
                                        }
                                    }

                                    // in javafx fillrule is called even_odd and non_zero
                                    FillRule fillRule = null;
                                    if (windingType==1) {
                                        fillRule = FillRule.NON_ZERO;
                                    } else {
                                        fillRule = FillRule.EVEN_ODD;
                                    }

                                    fxShape = new Path();
                                    ((Path) fxShape).setFillRule(fillRule);
                                    break;
                                }

                            default:
                                {
                                    String errMsg="shape type \""+canonicalShapeType+"\" not known/implemented";
                                    return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-27", errMsg );
                                }
                        }
                        hmFXShapes.put(ucNickName,fxShape);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return fxShape;      // return the shape
                    }

                // "shape3D name [type args...]"
                case SHAPE_3D:
                    {
                    int argNum=arrCommand.length;
                    if (arrCommand.length<2)
                    {
                        throw new IllegalArgumentException("this command needs more than "+arrCommand.length+" arguments");
                    }
                    String nickName=arrCommand[1];
                    String ucNickName=nickName.toUpperCase();
                    if (isOR)
                    {
                        canonical=canonical+" "+nickName;
                    }

                    Shape3D shape3D = null;

                    if (argNum==2)      // query Shape from registry
                    {
                        shape3D=hm3DShapes.get(ucNickName);

                        if (shape3D==null)
                        {
                            String errMsg="3D Shape with the supplied nickname \""+nickName+"\" is not registered";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-17", errMsg );
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        // query dimensions of shape3d
                        resultValue = "[x="+ shape3D.getTranslateX() + " y=" + shape3D.getTranslateY() + " z=" + shape3D.getTranslateZ();
                        if (shape3D instanceof Box) {
                            Box box = (Box) shape3D;
                            resultValue = "Box" + resultValue + " width=" + box.getWidth() + " height=" + box.getHeight() + " depth=" + box.getDepth() + "]";
                        } else if (shape3D instanceof Cylinder) {
                            Cylinder cylinder = (Cylinder) shape3D;
                            resultValue = "Cylinder" + resultValue + " radius=" + cylinder.getRadius() + " height=" + cylinder.getHeight() + "]";
                        } else {
                            Sphere sphere = (Sphere) shape3D;
                            resultValue = "Sphere" + resultValue + " radius=" + sphere.getRadius() + "]";
                        }
                        return resultValue;

                    }

                    //------------------------------------------------------------------------
                    // create and store a Shape
                    String strShapeType=arrCommand[2];
                    EnumShape eShape=EnumShape.getShape(strShapeType);

                    //----------------------------------------------------------------------------------------
                    if (eShape==null)
                    {
                        String errMsg="Shape3D type \""+strShapeType+"\" is not supported";
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-18", errMsg );
                    }
                    String canonicalShapeType=eShape.getMixedCase();
                    canonical=canonical+" "+canonicalShapeType; // get canoncial shape type

                    // position arguments
                    double x=Double.parseDouble(arrCommand[3]);
                    double y=Double.parseDouble(arrCommand[4]);
                    double z=Double.parseDouble(arrCommand[5]);

                    if (isOR)
                    {
                        canonical=canonical+" "+x+" "+y+" "+z;
                    }

                    switch (eShape)
                    {


                        //  Box(double width, double height, double depth)
                        case SHAPE_BOX:
                        {

                            if (argNum!=9)
                            {
                                String errMsg="Shape3D \""+canonicalShapeType+"\" needs exactly 6 (x y z width height depth), however there are "+(argNum-3)+" supplied";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape3D): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                            }


                            double width=Double.parseDouble(arrCommand[6]);
                            double height=Double.parseDouble(arrCommand[7]);
                            double depth=Double.parseDouble(arrCommand[8]);

                            shape3D = new Box(width,height,depth);

                            if (isOR)
                            {
                                canonical=canonical+" "+width+" "+height+" "+depth;
                            }

                            shape3D.setTranslateX(x);
                            shape3D.setTranslateY(y);
                            shape3D.setTranslateZ(z);

                            break;
                        }

                        //cylinder(double radius, double height)
                        case SHAPE_CYLINDER:
                        {
                            if (argNum!=8)
                            {
                                String errMsg="Shape3D \""+canonicalShapeType+"\" needs exactly 5 (x y z radius height), however there are "+(argNum-3)+" supplied";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape3D): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                            }

                            double radius=Double.parseDouble(arrCommand[6]);
                            double height=Double.parseDouble(arrCommand[7]);

                            shape3D = new Cylinder(radius, height);

                            if (isOR)
                            {
                                canonical=canonical+" "+radius+" "+height;
                            }

                            shape3D.setTranslateX(x);
                            shape3D.setTranslateY(y);
                            shape3D.setTranslateZ(z);

                            break;
                        }

                        //Sphere(double radius)
                        case SHAPE_SPHERE:
                        {
                            if (argNum!=7)
                            {
                                String errMsg="Shape3D \""+canonicalShapeType+"\" needs exactly 4 (x y z radius), however there are "+(argNum-3)+" supplied";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape3D): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                            }

                            double radius=Double.parseDouble(arrCommand[6]);

                            shape3D = new Sphere(radius);

                            if (isOR)
                            {
                                canonical=canonical+" "+radius;
                            }

                            shape3D.setTranslateX(x);
                            shape3D.setTranslateY(y);
                            shape3D.setTranslateZ(z);

                            break;
                        }

                        default:
                        {
                            String errMsg="shape3D type \""+canonicalShapeType+"\" not known/implemented";
                            return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-27", errMsg );
                        }
                    }

                    hm3DShapes.put(ucNickName,shape3D);
                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }

                    return shape3D;      // return the 3D shape
                }

                // "drawShape name
                case DRAW_SHAPE:
                    // "fillShape name
                case FILL_SHAPE:
                    // "clipShape name
                case CLIP_SHAPE:
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String nickName=arrCommand[1];

                        Shape fxShape=hmFXShapes.get(nickName.toUpperCase());


                        if (fxShape==null)
                        {
                            if (startRexxVariableChar.indexOf(nickName.charAt(0))>=0) // a Rexx variable?
                            {
                                fxShape=(Shape) getContextVariable(slot, nickName);
                                if (fxShape==null)
                                {
                                    throw new IllegalArgumentException("no shape with name \""+nickName+"\" stored, nor a Rexx variable that refers to a Shape object");
                                }
                            }
                        }


                        Affine newAffine = new Affine(canGC.getTransform());

                        Affine pathAffine = new Affine();

                        String pathAffineName = nickName+"pathTransform";

                        pathAffine = hmPathTransforms.get(pathAffineName.toUpperCase());

                        if (pathAffine==null)
                        {
                            try
                            {
                                pathAffine = (Affine) getContextVariable(slot, pathAffineName); // try to get from a Rexx variable
                            }
                            catch (Throwable t) {}

                        }

                        if(pathAffine != null) {
                            newAffine.append(pathAffine);
                        }

                        fxShape.getTransforms().add(newAffine);


                        if (EnumCommand.DRAW_SHAPE==cmd)
                        {
                            // set no fill color
                            fxShape.setFill(null);

                            //set stroke color
                            fxShape.setStroke(canGC.getStroke());

                            // set stroke properties
                            fxShape.setStrokeWidth(canGC.getLineWidth());
                            fxShape.setStrokeLineCap(canGC.getLineCap());
                            fxShape.setStrokeLineJoin(canGC.getLineJoin());
                            fxShape.setStrokeMiterLimit(canGC.getMiterLimit());
                            fxShape.setStrokeDashOffset(canGC.getLineDashOffset());
                            currStrokeDashArray = canGC.getLineDashes();

                            if (currStrokeDashArray != null) {
                                for (int i = 0; i < currStrokeDashArray.length; i++) {
                                    fxShape.getStrokeDashArray().add(currStrokeDashArray[i]);
                                }
                            }

                            // add shape to shapeGroup and show in scene
                            fxframe.shapeGroup.getChildren().add(fxShape);
                        }
                        else
                        if (EnumCommand.FILL_SHAPE==cmd)
                        {
                            // set to current fill color
                            fxShape.setFill(canGC.getFill());

                            // set no stroke
                            fxShape.setStroke(null);

                            // add shape to shapeGroup and show in scene
                            fxframe.root.getChildren().add(fxShape);

                        }
                        else    // CLIP_SHAPE
                        {
                            // clip root node with fxShape
                            fxframe.root.setClip(fxShape);
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        break;
                    }

                // "drawShape name
                case DRAW_3D_SHAPE:
                    // "fillShape name
                case FILL_3D_SHAPE:
                    {
                        
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String nickName=arrCommand[1];
                        
                        Shape3D shape3D = hm3DShapes.get(nickName.toUpperCase());

                        if (shape3D==null)
                        {
                            if (startRexxVariableChar.indexOf(nickName.charAt(0))>=0) // a Rexx variable?
                            {
                                shape3D=(Shape3D) getContextVariable(slot, nickName);
                                if (shape3D==null)
                                {
                                    throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored, nor a Rexx variable that refers to a 3D Shape object");
                                }
                            }
                        }

                        if (EnumCommand.DRAW_3D_SHAPE==cmd)
                        {
                            // draw 3D shape as lines / wire frame model
                            shape3D.setDrawMode(DrawMode.LINE);
                            // add shape to 3D shape group and show in scene
                            fxframe.shape3DGroup.getChildren().add(shape3D);
                        }

                        else
                        if (EnumCommand.FILL_3D_SHAPE==cmd)
                        {
                            // draw filled 3D shape
                            shape3D.setDrawMode(DrawMode.FILL);
                            // add shape to 3D shape group and show in scene
                            fxframe.shape3DGroup.getChildren().add(shape3D);
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        break;

                }

                case ROTATE_3D_SHAPE:    //   "rotate3dShape, shapeName, double angle, double pivotX, double pivotY, double pivotZ, double axisX, double axisY, double axisZ"
                    {
                    if (arrCommand.length!=9)
                    {
                        throw new IllegalArgumentException("this command needs exactly 8 arguments (shapeNickName angle pivotX pivotY pivotZ axisX axisY axisZ), received "+(arrCommand.length-1)+" instead");
                    }

                    String nickName=arrCommand[1];

                    Shape3D shape3D = (Shape3D) hm3DShapes.get(nickName.toUpperCase());

                    if (shape3D==null)
                    {
                        throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored");
                    }

                    double angle = Double.parseDouble(arrCommand[2]);
                    double pivotX = Double.parseDouble(arrCommand[3]);
                    double pivotY = Double.parseDouble(arrCommand[4]);
                    double pivotZ = Double.parseDouble(arrCommand[5]);
                    double axisX = Double.parseDouble(arrCommand[6]);
                    double axisY = Double.parseDouble(arrCommand[7]);
                    double axisZ = Double.parseDouble(arrCommand[8]);

                    Affine fxAffine = new Affine();
                    fxAffine.appendRotation(angle,pivotX,pivotY,pivotZ,axisX,axisY,axisZ);

                    shape3D.getTransforms().add(fxAffine);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+" "+angle+" "+pivotX+" "+pivotY+" "+pivotZ+" "+axisX+" "+axisY+" "+axisZ);
                    }
                    return resultValue;
                }

                case SCALE_3D_SHAPE:    //   "scale3dShape, shapeName, double sx, double sy, double sz, double pivotX, double pivotY, double pivotZ,"
                    {
                    if (arrCommand.length!=8)
                    {
                        throw new IllegalArgumentException("this command needs exactly 7 arguments (shapeNickName sx sy sz pivotX pivotY pivotZ), received "+(arrCommand.length-1)+" instead");
                    }

                    String nickName=arrCommand[1];

                    Shape3D shape3D = (Shape3D) hm3DShapes.get(nickName.toUpperCase());

                    if (shape3D==null)
                    {
                        throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored");
                    }


                    double sx = Double.parseDouble(arrCommand[2]);
                    double sy = Double.parseDouble(arrCommand[3]);
                    double sz = Double.parseDouble(arrCommand[4]);
                    double pivotX = Double.parseDouble(arrCommand[5]);
                    double pivotY = Double.parseDouble(arrCommand[6]);
                    double pivotZ = Double.parseDouble(arrCommand[7]);

                    Affine fxAffine = new Affine();
                    fxAffine.appendScale(sx,sy,sz,pivotX,pivotY,pivotZ);

                    shape3D.getTransforms().add(fxAffine);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+" "+sx+" "+sy+" "+sz+" "+pivotX+" "+pivotY+" "+pivotZ);
                    }
                    return resultValue;
                }
                case SHEAR_3D_SHAPE:    //   "shear3dShape, shapeName, double shx, double shy, double pivotX, double pivotY"
                    {
                    if (arrCommand.length!=6)
                    {
                        throw new IllegalArgumentException("this command needs exactly 5 arguments (shapeNickName shx shy pivotX pivotY), received "+(arrCommand.length-1)+" instead");
                    }

                    String nickName=arrCommand[1];

                    Shape3D shape3D = (Shape3D) hm3DShapes.get(nickName.toUpperCase());

                    if (shape3D==null)
                    {
                        throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored");
                    }

                    double shx = Double.parseDouble(arrCommand[2]);
                    double shy = Double.parseDouble(arrCommand[3]);
                    double pivotX = Double.parseDouble(arrCommand[4]);
                    double pivotY = Double.parseDouble(arrCommand[5]);

                    Affine fxAffine = new Affine();
                    fxAffine.appendShear(shx,shy,pivotX,pivotY);

                    shape3D.getTransforms().add(fxAffine);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+" "+shx+" "+shy+" "+pivotX+" "+pivotY);
                    }
                    return resultValue;
                }
                case TRANSLATE_3D_SHAPE:    //   "translate3dShape, shapeName, double tx, double ty, double tz"
                    {
                    if (arrCommand.length!=5)
                    {
                        throw new IllegalArgumentException("this command needs exactly 4 arguments (shapeNickName tx ty tz), received "+(arrCommand.length-1)+" instead");
                    }

                    String nickName=arrCommand[1];

                    Shape3D shape3D = (Shape3D) hm3DShapes.get(nickName.toUpperCase());

                    if (shape3D==null)
                    {
                        throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored");
                    }

                    double tx = Double.parseDouble(arrCommand[2]);
                    double ty = Double.parseDouble(arrCommand[3]);
                    double tz = Double.parseDouble(arrCommand[4]);

                    Affine fxAffine = new Affine();
                    fxAffine.appendTranslation(tx,ty,tz);

                    shape3D.getTransforms().add(fxAffine);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+" "+tx+" "+ty+" "+tz);
                    }
                    return resultValue;
                }

                case CULL_FACE:             // "cullFace shapeName [NONE|BACK|FRONT]"
                    {
                    if (arrCommand.length!=2 && arrCommand.length!=3)
                    {
                        throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (shapeNickName [NONE|BACK|FRONT]), received "+(arrCommand.length-1)+" instead");
                    }
                    String nickName=arrCommand[1];
                    Shape3D shape3D=hm3DShapes.get(nickName.toUpperCase());
                    if (shape3D==null)
                    {
                        throw new IllegalArgumentException("no 3D shape with name \""+nickName+"\" stored");
                    }
                    CullFace previous=shape3D.getCullFace();
                    if (arrCommand.length==3)
                    {
                        try
                        {
                            shape3D.setCullFace(CullFace.valueOf(arrCommand[2].toUpperCase()));
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new IllegalArgumentException("invalid cull face ["+arrCommand[2]+"], expected NONE, BACK or FRONT");
                        }
                    }
                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+(arrCommand.length==3 ? " "+shape3D.getCullFace().name() : ""));
                    }
                    return previous.name();
                    }

                case ROTATE_CAMERA:
                    {
                    if (arrCommand.length!=9)
                    {
                        throw new IllegalArgumentException("this command needs exactly 8 arguments (name angle pivotX pivotY pivotZ axisX axisY axisZ), received "+(arrCommand.length-1)+" instead");
                    }
                    String nickName=arrCommand[1];
                    Node node = hmCamera.get(nickName.toUpperCase());
                    if (node==null)
                    {
                        throw new IllegalArgumentException("no "+"camera" +" with name \""+nickName+"\" stored");
                    }
                    double angle=Double.parseDouble(arrCommand[2]);
                    double pivotX=Double.parseDouble(arrCommand[3]);
                    double pivotY=Double.parseDouble(arrCommand[4]);
                    double pivotZ=Double.parseDouble(arrCommand[5]);
                    double axisX=Double.parseDouble(arrCommand[6]);
                    double axisY=Double.parseDouble(arrCommand[7]);
                    double axisZ=Double.parseDouble(arrCommand[8]);
                    Affine affine=new Affine();
                    affine.appendRotation(angle,pivotX,pivotY,pivotZ,axisX,axisY,axisZ);
                    node.getTransforms().add(affine);
                    if (isOR) writeOutput(slot, canonical+" "+nickName+" "+angle+" "+pivotX+" "+pivotY+" "+pivotZ+" "+axisX+" "+axisY+" "+axisZ);
                    return resultValue;
                    }

                case TRANSLATE_CAMERA:
                case TRANSLATE_POINT_LIGHT:
                    {
                    if (arrCommand.length!=5)
                    {
                        throw new IllegalArgumentException("this command needs exactly 4 arguments (name tx ty tz), received "+(arrCommand.length-1)+" instead");
                    }
                    String nickName=arrCommand[1];
                    Node node=cmd==EnumCommand.TRANSLATE_CAMERA ? hmCamera.get(nickName.toUpperCase()) : hmLightBase.get(nickName.toUpperCase());
                    if (node==null || (cmd==EnumCommand.TRANSLATE_POINT_LIGHT && !(node instanceof PointLight)))
                    {
                        throw new IllegalArgumentException("no "+(cmd==EnumCommand.TRANSLATE_CAMERA ? "camera" : "PointLight")+" with name \""+nickName+"\" stored");
                    }
                    double tx=Double.parseDouble(arrCommand[2]);
                    double ty=Double.parseDouble(arrCommand[3]);
                    double tz=Double.parseDouble(arrCommand[4]);
                    Affine affine=new Affine();
                    affine.appendTranslation(tx,ty,tz);
                    node.getTransforms().add(affine);
                    if (isOR) writeOutput(slot, canonical+" "+nickName+" "+tx+" "+ty+" "+tz);
                    return resultValue;
                    }

                case CAMERA_NEAR_CLIP:
                case CAMERA_FAR_CLIP:
                    {
                    if (arrCommand.length!=2 && arrCommand.length!=3)
                    {
                        throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (cameraName [distance]), received "+(arrCommand.length-1)+" instead");
                    }
                    String nickName=arrCommand[1];
                    Camera camera=hmCamera.get(nickName.toUpperCase());
                    if (camera==null)
                    {
                        throw new IllegalArgumentException("no camera with name \""+nickName+"\" stored");
                    }
                    boolean near=cmd==EnumCommand.CAMERA_NEAR_CLIP;
                    double previous=near ? camera.getNearClip() : camera.getFarClip();
                    if (arrCommand.length==3)
                    {
                        double distance=Double.parseDouble(arrCommand[2]);
                        if (!Double.isFinite(distance) || distance<=0)
                            throw new IllegalArgumentException("clip distance must be a finite number greater than zero");
                        if (near && distance>=camera.getFarClip()) {
                            throw new IllegalArgumentException("nearClip must be less than farClip ("+camera.getFarClip()+")");
                        }
                        if (!near && distance<=camera.getNearClip())
                            throw new IllegalArgumentException("farClip must be greater than nearClip ("+camera.getNearClip()+")");
                        if (near) {
                            camera.setNearClip(distance);
                        }
                        else {
                            camera.setFarClip(distance);
                        }
                    }
                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName+(arrCommand.length==3 ? " "+(near ? camera.getNearClip() : camera.getFarClip()) : ""));
                    }
                    setChangeSceneTrue();
                    return previous;
                    }

                // "camera name [type args...]"
                case CAMERA:
                    {
                    int argNum=arrCommand.length;
                    
                    if (argNum!=1 && argNum!=2 && argNum!=6 && argNum!=7 && argNum!=8)
                    {
                        throw new IllegalArgumentException("this command needs exactly 0, 1, 5 or 6 arguments, received "+(arrCommand.length-1)+" instead");
                    }

                    Camera camera = null;

                    if (argNum<=2)
                    {
                        camera = fxframe.getCamera();

                        if (argNum==2)      // query Shape from registry
                        {
                            String nickName=arrCommand[1];
                            camera=hmCamera.get(nickName.toUpperCase());

                            if (camera==null)
                            {
                                String errMsg="Camera with the supplied nickname \""+nickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-17", errMsg );
                            }
                            if (isOR)
                            {
                                canonical=canonical+" "+nickName;
                            }

                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        
                        // query current camera properties
                        resultValue = "x=" + camera.getTranslateX() + " y=" + camera.getTranslateY() + " z=" + camera.getTranslateZ();
                        if (camera instanceof PerspectiveCamera) {
                            PerspectiveCamera perspectiveCamera = (PerspectiveCamera) camera;
                            resultValue = "PerspectiveCamera[" + resultValue + " fieldOfView=" + perspectiveCamera.getFieldOfView() + "]";
                        } else {
                            resultValue = "ParallelCamera[" + resultValue + "]";
                        }
                        return resultValue;
                    }

                    String nickName=arrCommand[1];
                    String ucNickName=nickName.toUpperCase();

                    String cameraType=arrCommand[2];
                    String ucCameraType=cameraType.toUpperCase();

                    // position arguments
                    double x=Double.parseDouble(arrCommand[3]);
                    double y=Double.parseDouble(arrCommand[4]);
                    double z=Double.parseDouble(arrCommand[5]);

                    if (isOR)
                    {
                        canonical=canonical+" "+nickName+" "+cameraType+" "+x+" "+y+" "+z;
                    }

                    // only relevant for perspective camera
                    double view= 0;

                    if(ucCameraType.equals("PARALLEL")) {

                        if (argNum!=6)
                        {
                            String errMsg="Parallel Camera needs exactly 3 (x y z), however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (wrong number of arguments for Parallel Camera shape3D): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                        }

                        camera = new ParallelCamera();

                    }
                    else if (ucCameraType.equals("PERSPECTIVE"))
                    {

                        if (argNum<6 || argNum>8)
                        {
                            String errMsg="Perspective Camera needs exactly 3 (x y z), 4 arguments (x y z FieldOfView) or 5 arguments (x y z FieldOfView fixedEyeAtCameraZero), however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (wrong number of arguments for Perspective Camera): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                        }

                        boolean fixedEyeAtCameraZero = (argNum==8) ? getBooleanValue(arrCommand[7]) : false;
                        PerspectiveCamera perCamera = new PerspectiveCamera(fixedEyeAtCameraZero);

                        // if argument for field of view is supplied, otherwise default field of view is 30
                        if (argNum>=7) {
                            view=Double.parseDouble(arrCommand[6]);
                            perCamera.setFieldOfView(view);
                            if (isOR)
                            {
                                canonical=canonical+" "+view+" "+fixedEyeAtCameraZero;
                            }
                        }

                        camera = perCamera;

                    }
                    else    // argument is neither parallel nor perspective
                    {
                        String errMsg="Camera type \""+cameraType+"\" is not supported";
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-18", errMsg );
                    }

                    camera.setTranslateX(x);
                    camera.setTranslateY(y);
                    camera.setTranslateZ(z);

                    hmCamera.put(ucNickName,camera);
                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }

                    return camera;      // return the camera
                }

                // "camera name
                case SET_CAMERA:
                    {

                    if (arrCommand.length!=1 && arrCommand.length!=2)
                    {
                        throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                    }

                    // set back to default parallel camera
                    if (arrCommand.length==1)
                    {
                        logSceneState("setCamera(default,before)", fxframe);
                        final Camera _defCam = defCamera;
                        runOnFxThreadAndWait(() -> fxframe.setCamera(_defCam));
                        logSceneState("setCamera(default,after)", fxframe);
                        break;
                    }

                    String nickName=arrCommand[1];

                    Camera camera = hmCamera.get(nickName.toUpperCase());

                    if (camera==null)
                    {
                        if (startRexxVariableChar.indexOf(nickName.charAt(0))>=0) // a Rexx variable?
                        {
                            camera=(Camera) getContextVariable(slot, nickName);
                            if (camera==null)
                            {
                                throw new IllegalArgumentException("no Camera with name \""+nickName+"\" stored, nor a Rexx variable that refers to a Light object");
                            }
                        }
                    }

                    logSceneState("setCamera(named,before)", fxframe);
                    final Camera _cameraToSet = camera;
                    runOnFxThreadAndWait(() -> fxframe.setCamera(_cameraToSet));
                    logSceneState("setCamera(named,after)", fxframe);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+nickName);
                    }
                    break;

                }

                // light name [type args...]
                case LIGHT:
                    {
                    int argNum=arrCommand.length;

                    if (argNum < 2 || argNum > 7 || argNum==5)
                    {
                        throw new IllegalArgumentException("this command needs exactly 1 argument (lightName) or 2, 3, 5 or 6 arguments (lightName type [type specific args]), received "+(arrCommand.length-1)+" instead");
                    }

                    String lightName=arrCommand[1];
                    String ucLightName=lightName.toUpperCase();

                    LightBase lightBase = null;

                    if (argNum==2)      // query Shape from registry
                    {
                        lightBase=hmLightBase.get(ucLightName);

                        if (lightBase==null)
                        {
                            String errMsg="Light with the supplied nickname \""+ucLightName+"\" is not registered";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-17", errMsg );
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+lightName);
                        }

                        // query properties of lightBase
                        resultValue = "color="+ lightBase.getColor();
                        if (lightBase instanceof PointLight) {
                            PointLight pointLight = (PointLight) lightBase;
                            resultValue = "PointLight[" + resultValue + " x=" + pointLight.getTranslateX() + " y=" + pointLight.getTranslateY() + " z=" + pointLight.getTranslateZ() + "]";
                        } else {
                            resultValue = "AmbienLight[" + resultValue + "]";
                        }
                        return resultValue;
                    }

                    String lightBaseType=arrCommand[2];
                    String ucLightType=lightBaseType.toUpperCase();

                    // position arguments for pointLight
                    double x=0;
                    double y=0;
                    double z=0;

                    if (isOR)
                    {
                        canonical=canonical+" "+lightName+" "+lightBaseType;
                    }

                    Color fxColor = null;

                        // [Color color]
                    if(ucLightType.equals("AMBIENT")) {

                        if (argNum!=3 && argNum!=4)
                        {
                            String errMsg="Ambient Light needs exactly no or 1 (color) arguments, however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (wrong number of arguments for Ambient Light): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                        }

                        AmbientLight amLight = new AmbientLight();

                        // if color argument is supplied, otherwise default color is white
                        if (argNum==4)
                        {
                            String colorNickName=arrCommand[3];
                            fxColor= hmFXColors.get(colorNickName.toUpperCase());
                            amLight.setColor(fxColor);
                            if (isOR)
                            {
                                canonical=canonical+" "+colorNickName;
                            }
                        }

                        lightBase = amLight;

                    }
                    // double x, double y, double z, [Color color]
                    else if (ucLightType.equals("POINT"))
                    {

                        if (argNum!=6 && argNum!=7)
                        {
                            String errMsg="Point Lightbase needs exactly 3 (x y z) or 4 arguments (x y z color), however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (wrong number of arguments for Point Lightbase): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                        }

                        x=Double.parseDouble(arrCommand[3]);
                        y=Double.parseDouble(arrCommand[4]);
                        z=Double.parseDouble(arrCommand[5]);

                        if (isOR)
                        {
                            canonical=canonical+" "+x+" "+y+" "+z;
                        }

                        PointLight pLight = new PointLight();

                        pLight.setTranslateX(x);
                        pLight.setTranslateY(y);
                        pLight.setTranslateZ(z);

                        // if color argument is supplied, otherwise default color is white
                        if (argNum==7)
                        {
                            String colorNickName=arrCommand[6];
                            fxColor= hmFXColors.get(colorNickName.toUpperCase());
                            pLight.setColor(fxColor);
                            if (isOR)
                            {
                                canonical=canonical+" "+colorNickName;
                            }
                        }

                        lightBase = pLight;

                    }
                    else    // argument is neither ambient nor point
                    {
                        String errMsg="LightBase type \""+lightBaseType+"\" is not supported";
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-18", errMsg );
                    }

                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }

                    // turn light off and add to scene
                    lightBase.setLightOn(false);
                    hmLightBase.put(ucLightName,lightBase);
                    logSceneState("light(add,before)", fxframe);
                    final LightBase _lightToAdd = lightBase;
                    runOnFxThreadAndWait(() -> fxframe.lightGroup.getChildren().add(_lightToAdd));
                    logSceneState("light(add,after)", fxframe);

                    return lightBase;      // return the lightbase
                }
                // setLight name [on / off]
                case SET_LIGHT:
                    {

                    if (arrCommand.length!=2 && arrCommand.length!=3)
                    {
                        throw new IllegalArgumentException("this command needs exactly 1 argument (lightName) or 2 arguments(lightName on/off), received "+(arrCommand.length-1)+" instead");
                    }

                    String lightName=arrCommand[1];

                    LightBase lightBase = hmLightBase.get(lightName.toUpperCase());

                    String turnedOn = "TURNON";     //default

                    if (lightBase==null)
                    {


                        if (startRexxVariableChar.indexOf(lightName.charAt(0))>=0) // a Rexx variable?
                        {
                            lightBase=(LightBase) getContextVariable(slot, lightName);
                            if (lightBase==null)
                            {
                                throw new IllegalArgumentException("no Light with name \""+lightName+"\" stored, nor a Rexx variable that refers to a Light object");
                            }
                        }
                    }



                    if (arrCommand.length==3)
                    {
                        turnedOn=arrCommand[2].toUpperCase();
                    }

                    final LightBase _targetLight = lightBase;
                    if (turnedOn.equals("TURNOFF")) {
                        runOnFxThreadAndWait(() -> _targetLight.setLightOn(false));
                    }
                    else if (turnedOn.equals("TURNON"))
                    {
                        runOnFxThreadAndWait(() -> _targetLight.setLightOn(true));
                    }
                    else
                    {
                        throw new IllegalArgumentException("the third command can only be TURNON or TURNOFF, received "+turnedOn+" instead");
                    }

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+lightName+(arrCommand.length==3 ? " "+turnedOn : ""));
                    }


                    break;

                }

                // material name [color]
                case MATERIAL:
                    {
                    int argNum=arrCommand.length;

                    String materialName=arrCommand[1];
                    String ucMaterialName=materialName.toUpperCase();

                    if (isOR)
                    {
                        canonical=canonical+" "+materialName;
                    }

                    PhongMaterial material = new PhongMaterial();

                    if (argNum!=2 && argNum!=3)
                    {
                        String errMsg="Material needs exactly 1 (materialName) or 2 argument (materialName color), however there are "+(argNum-1)+" supplied";
                        if (isOR)
                        {
                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                        }
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                    }

                    Color fxColor = null;

                        // color argument supplied
                    if (argNum==3) {

                        String colorNickName = arrCommand[2];
                        try {
                            fxColor = resolveColorFromToken(slot, colorNickName);
                        } catch (IllegalArgumentException e) {
                            String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                        }

                        if (isOR)
                        {
                            canonical=canonical+" "+colorNickName;
                        }

                        material.setDiffuseColor(fxColor);
                    }

                    hmMaterial.put(ucMaterialName, material);

                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }

                    return material;      // return the material
                }

                // materialColor name type color [specularPower]
                case MATERIAL_COLOR:
                {
                    int argNum=arrCommand.length;

                    String materialName=arrCommand[1];
                    String ucMaterialName=materialName.toUpperCase();

                    PhongMaterial material = hmMaterial.get(materialName.toUpperCase());

                    if (material==null)
                    {

                        if (startRexxVariableChar.indexOf(materialName.charAt(0))>=0) // a Rexx variable?
                        {
                            material=(PhongMaterial) getContextVariable(slot, materialName);
                            if (material==null)
                            {
                                throw new IllegalArgumentException("no material with name \""+materialName+"\" stored, nor a Rexx variable that refers to a material");
                            }
                        }
                    }

                    String colorNickName = arrCommand[3];

                    Color fxColor;
                    try {
                        fxColor = resolveColorFromToken(slot, colorNickName);
                    } catch (IllegalArgumentException e) {
                        String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                        if (isOR)
                        {
                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                        }
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                    }

                    String coloringType=arrCommand[2];
                    String ucColoringType=coloringType.toUpperCase();

                    if (isOR)
                    {
                        canonical=canonical+" "+materialName+" "+coloringType+" "+colorNickName;
                    }

                    if(ucColoringType.equals("DIFFUSE") || ucColoringType.equals("DIFFUSECOLOR")) {

                        if (argNum!=4)
                        {
                            String errMsg="DiffuseColor needs exactly 1 (color) argument, however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                        }

                        material.setDiffuseColor(fxColor);

                    }
                    else if(ucColoringType.equals("SPECULAR") || ucColoringType.equals("SPECULARCOLOR")) {

                        if (argNum!=4 && argNum!=5)
                        {
                            String errMsg="SpecularColor needs exactly 1 (color) or 2 (color specularPower) arguments, however there are "+(argNum-3)+" supplied";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                        }

                        double specPower = 32;

                        material.setSpecularColor(fxColor);

                        // if specularPower argument is supplied, otherwise default value is 32
                        if (argNum==5)
                        {
                            specPower= Double.parseDouble(arrCommand[4]);
                            if (isOR)
                            {
                                canonical=canonical+" "+arrCommand[4];
                            }
                        }

                        material.setSpecularPower(specPower);

                    }
                    else
                    {
                        throw new IllegalArgumentException("the type argument can only be DIFFUSE, DIFFUSECOLOR, SPECULAR or SPECULARCOLOR, received "+coloringType+" instead");
                    }

                    if (isOR)
                    {
                        writeOutput(slot, canonical);
                    }

                    break;

                }

                // map mapName mapPath [addWidth addHeight rotation [color]]
                case MAP:
                {
                    int argNum=arrCommand.length;

                    if (argNum!=3 && argNum!=6 && argNum!=7)
                    {
                        String errMsg="Map needs exactly 2 (mapName mapPath), 5 arguments (mapName mapPath addWidth addHeight rotation) or 6 arguments (mapName mapPath width height rotation color), however there are "+(argNum-1)+" supplied";
                        if (isOR)
                        {
                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                        }
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                    }

                    String mapName=arrCommand[1];
                    String ucMapName=mapName.toUpperCase();

                    String mapPath=fromRexxStringLiteral(arrCommand[2]);

                    Image map = new javafx.scene.image.Image(new FileInputStream(mapPath));

                    if (map==null)
                    {
                        throw new IllegalArgumentException("no image with name \""+mapPath+"\" found");
                    }

                    int addWidth = 0;
                    int addHeight = 0;
                    double rotation = 0;
                    String colorName = null;

                    // addWidth addHeight rotation [and color] supplied
                    // creating a new image with new size, rotation [and color]
                    if (argNum>=6) {

                        addWidth = Integer.parseInt(arrCommand[3]);
                        addHeight = Integer.parseInt(arrCommand[4]);
                        rotation = Double.parseDouble(arrCommand[5]);

                        double radian = Math.toRadians(rotation);

                        int wImageWidth = (int)map.getWidth() + addWidth;
                        int wImageHeight = (int)map.getHeight() + addHeight;

                        //Creating a writable image
                        WritableImage wImage = new WritableImage(wImageWidth, wImageHeight);

                        //getting the pixel writer
                        PixelWriter writer = wImage.getPixelWriter();

                        //Reading color from the loaded image
                        PixelReader pixelReader = map.getPixelReader();

                        // point of rotation is center of wImage
                        double rotationCenterX = wImage.getWidth()/2;
                        double rotationCenterY = wImage.getHeight()/2;

                        double mapCoordX = 0;
                        double mapCoordY = 0;
                        double imgCoordX = 0;
                        double imgCoordY = 0;

                        // if supplied, fill wImage with color
                        if (argNum==7) {

                            colorName = arrCommand[6];
                            Color fxColor;
                            try {
                                fxColor = resolveColorFromToken(slot, colorName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                            }

                            // iterate through all pixels of wImage and set color
                            for (int y = 0; y < wImageHeight; y++) {
                                for (int x = 0; x < wImageWidth; x++) {
                                    writer.setColor(x, y, fxColor);
                                }
                            }
                        }

                        // if supplied map has fully transparent pixels, the PixelReader will read them as "0x00000000"
                        Color c = Color.web("0x00000000", 1.0);

                        try {

                            //Read color of every pixel of the map and write them onto wImage
                            //map will be centered in wImage
                            for (int y = 0; y < (int) map.getHeight(); y++) {
                                for (int x = 0; x < (int) map.getWidth(); x++) {
                                    //Retrieving the color of the pixel of map
                                    Color color = pixelReader.getColor(x, y);

                                    // x and y coordinates as if map was centered in wImage
                                    mapCoordX = rotationCenterX - map.getWidth() / 2 + x;
                                    mapCoordY = rotationCenterY - map.getHeight() / 2 + y;

                                    // calculate new x and y coordinates after rotation
                                    // if rotation is 0, 90, 180, 270 or 360, the conversion will be flawless
                                    // otherwise, some pixels will be lost in the process
                                    switch((int)rotation) {
                                        case 0:
                                        case 360:
                                            imgCoordX = mapCoordX;
                                            imgCoordY = mapCoordY;
                                            break;

                                        case 90:
                                            imgCoordX = rotationCenterX + map.getHeight() / 2 - y;
                                            imgCoordY = rotationCenterY - map.getWidth() / 2 + x;
                                            break;

                                        case 180:
                                            imgCoordX = rotationCenterX + map.getHeight() / 2 - x;
                                            imgCoordY = rotationCenterY + map.getWidth() / 2 - y;
                                            break;

                                        case 270:
                                            imgCoordX = rotationCenterX - map.getHeight() / 2 + y;
                                            imgCoordY = rotationCenterY + map.getWidth() / 2 - x;
                                            break;

                                        default:
                                            imgCoordX = rotationCenterX + Math.cos(radian) * (mapCoordX - rotationCenterX) - Math.sin(radian) * (mapCoordY - rotationCenterY);
                                            imgCoordY = rotationCenterY + Math.sin(radian) * (mapCoordX - rotationCenterX) + Math.cos(radian) * (mapCoordY - rotationCenterY);

                                    }

                                    //if new coordinates are outside wImage bounds, skip this iteration
                                    if(imgCoordX<0 || imgCoordY<0 || imgCoordX>wImageWidth-1 || imgCoordY>wImageHeight-1) {
                                        continue;
                                    }

                                    // only fill color of wImage if current pixel color of map is not "0x00000000"
                                    // this ignores all pixels of the map that are fully transparent
                                    if (!color.equals(c)) {
                                        writer.setColor((int) imgCoordX, (int) imgCoordY, color);
                                    }

                                }
                            }
                        }
                        catch (Throwable t) {
                            // failsafe if coordinates of rotatedX or rotatedY are out of bounds of wImage
                            throw new IndexOutOfBoundsException("Added width or height too small: added width: " + addWidth + " added height: " + addHeight);
                        }

                        map = (Image) wImage;

                    }

                    hmMaps.put(ucMapName, map);

                    if (isOR)
                    {
                        canonical=canonical+" "+mapName+" "+toRexxStringLiteral(mapPath);
                        if (argNum>=6)
                        {
                            canonical=canonical+" "+addWidth+" "+addHeight+" "+rotation;
                        }
                        if (argNum==7)
                        {
                            canonical=canonical+" "+colorName;
                        }
                        writeOutput(slot, canonical);
                    }

                    return map.getWidth() + " " + map.getHeight();

                }
                // materialMap materialName type imagePath
                case MATERIAL_MAP:
                {
                    int argNum=arrCommand.length;

                    if (argNum!=4)
                    {
                        String errMsg="MaterialMap needs no or exactly 2 arguments (type map), however there are "+(argNum-1)+" supplied";
                        if (isOR)
                        {
                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                        }
                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                    }

                    String materialName=arrCommand[1];
                    String ucMaterialName=materialName.toUpperCase();

                    PhongMaterial material = hmMaterial.get(materialName.toUpperCase());

                    if (material==null)
                    {
                        if (startRexxVariableChar.indexOf(materialName.charAt(0))>=0) // a Rexx variable?
                        {
                            try // try to get from a Rexx variable
                            {
                                material=(PhongMaterial) getContextVariable(slot, materialName);
                            }
                            catch (Throwable t) {}
                            if (material==null)
                            {
                                throw new IllegalArgumentException("no material with name \""+materialName+"\" stored, nor a Rexx variable that refers to a material");
                            }
                        }
                    }

                    String mapName = arrCommand[3];

                    Image map = hmMaps.get(mapName.toUpperCase());

                    if (map==null)
                    {
                        map = new javafx.scene.image.Image(new FileInputStream(mapName));
                    }

                    if (map==null)
                    {
                        if (startRexxVariableChar.indexOf(mapName.charAt(0))>=0) // a Rexx variable?
                        {
                            try // try to get from a Rexx variable
                            {
                                map=(Image) getContextVariable(slot, mapName);
                            }
                            catch (Throwable t) {}
                            if (map==null)
                            {
                                throw new IllegalArgumentException("no map with name \""+mapName+"\" found");
                            }
                        }
                    }

                    String mapType = arrCommand[2];
                    String ucMapType=mapType.toUpperCase();

                    switch (ucMapType) {
                        case "BUMP":
                        case "BUMPMAP":
                            material.setBumpMap(map);
                            break;
                        case "DIFFUSE":
                        case "DIFFUSEMAP":
                            material.setDiffuseMap(map);
                            break;
                        case "SELFILLUMINATION":
                        case "SELFILLUMINATIONMAP":
                            material.setSelfIlluminationMap(map);
                            break;
                        case "SPECULAR":
                        case "SPECULARMAP":
                            material.setSpecularMap(map);
                            break;
                        default:
                            throw new IllegalArgumentException("the type argument can only be BUMP, BUMPMAP, DIFFUSE, DIFFUSEMAP, SELFILLUMINATION, SELFILLUMINATIONMAP, SPECULAR or SPECULARMAP, received "+mapType+" instead");
                    }

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+materialName+" "+mapType+" "+mapName);
                    }

                    break;

                }

                // setMaterial shapeName materialName
                case SET_MATERIAL:
                    {

                    if (arrCommand.length!=3)
                    {
                        throw new IllegalArgumentException("this command needs exactly 2 argument (shapeName materialName), received "+(arrCommand.length-1)+" instead");
                    }

                    String shapeName=arrCommand[1];

                    Shape3D shape3D = hm3DShapes.get(shapeName.toUpperCase());

                    if (shape3D==null)
                    {

                        if (startRexxVariableChar.indexOf(shapeName.charAt(0))>=0) // a Rexx variable?
                        {
                            shape3D=(Shape3D) getContextVariable(slot, shapeName);
                            if (shape3D==null)
                            {
                                throw new IllegalArgumentException("no 3D Shape with name \""+shapeName+"\" stored, nor a Rexx variable that refers to a 3D Shape object");
                            }
                        }
                    }

                    String materialName=arrCommand[2];

                    PhongMaterial material = hmMaterial.get(materialName.toUpperCase());

                    if (material==null)
                    {

                        if (startRexxVariableChar.indexOf(materialName.charAt(0))>=0) // a Rexx variable?
                        {
                            material=(PhongMaterial) getContextVariable(slot, materialName);
                            if (material==null)
                            {
                                throw new IllegalArgumentException("no material with name \""+materialName+"\" stored, nor a Rexx variable that refers to a material");
                            }
                        }
                    }

                    shape3D.setMaterial(material);

                    if (isOR)
                    {
                        writeOutput(slot, canonical+" "+shapeName +" "+materialName);
                    }

                    break;

                }

                // 2022-12-07: return bounds for any supported shape
                case SHAPE_BOUNDS:      // "shapeBounds shapeName" ... getBounds2D()
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (the name of the shape), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Shape fxShape = hmFXShapes.get(nickName.toUpperCase());

                        String strResult = fxShape.getTranslateX()+" "+fxShape.getTranslateY()+" "+fxShape.getBoundsInLocal().getWidth()+" "+fxShape.getBoundsInLocal().getHeight();

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        return strResult;
                    }

                        // AREA-command areaNickname shapeNickname (can be an area)
                case AREA_ADD:
                case AREA_EXCLUSIVE_OR:
                case AREA_INTERSECT:
                case AREA_SUBTRACT:
                case AREA_TRANSFORM:        // areaTransform areaNickname transformNickname
                    {
                        if (arrCommand.length!=3)
                        {
                            if (cmd!=EnumCommand.AREA_TRANSFORM)
                            {
                                throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the area and area/shape to be used as argument for the operation), received "+(arrCommand.length-1)+" instead");
                            }
                            throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the area and the nickname of a transform to be applied to it), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName1=arrCommand[1];     // areaNickname

                        Shape tmpFXShape = (Shape) hmFXShapes.get(nickName1.toUpperCase());


                        if (tmpFXShape==null)
                        {
                            throw new IllegalArgumentException("no Area shape with name \""+nickName1+"\" stored");
                        }

                        String nickName2=arrCommand[2];     // {transform|shape}NickName

                        if (isOR)
                        {
                            canonical = canonical+" "+nickName1+" "+nickName2;
                            writeOutput(slot, canonical);
                        }

                        if (cmd==EnumCommand.AREA_TRANSFORM)
                        {
                            Affine fxAffine=hmFXTransforms.get(nickName2.toUpperCase());

                            if (fxAffine==null)
                            {
                                if (startRexxVariableChar.indexOf(nickName2.charAt(0))>=0) // a Rexx variable?
                                {
                                    fxAffine=(Affine) getContextVariable(slot, nickName2);
                                }
                                if (fxAffine==null)
                                {
                                    throw new IllegalArgumentException("no transform with name \""+nickName2+"\" stored, nor a Rexx variable that refers to a transform object");
                                }
                            }
                            tmpFXShape.getTransforms().add(fxAffine);
                        }
                        else
                        {

                            Shape tmpFXShape1 = hmFXShapes.get(nickName2.toUpperCase());

                            if (tmpFXShape1==null)
                            {
                                if (startRexxVariableChar.indexOf(nickName2.charAt(0))>=0) // a Rexx variable?
                                {
                                    tmpFXShape1=(Shape) getContextVariable(slot, nickName2);
                                }
                                if (tmpFXShape1==null)
                                {
                                    throw new IllegalArgumentException("no Area or Shape with name \""+nickName2+"\" stored, nor a Rexx variable that refers to a shape object");
                                }
                            }

                            if (cmd==EnumCommand.AREA_ADD)
                            {
                                // perform operation and create a new shape
                                tmpFXShape = Shape.union(tmpFXShape,tmpFXShape1);
                            }
                            else if (cmd==EnumCommand.AREA_EXCLUSIVE_OR)
                            {
                                // javafx shapes do not have Xor operations, hence subtract both shapes off of each other and join them
                                tmpFXShape = Shape.union(Shape.subtract(tmpFXShape,tmpFXShape1),Shape.subtract(tmpFXShape1,tmpFXShape));
                            }
                            else if (cmd==EnumCommand.AREA_INTERSECT)
                            {
                                // perform operation and create a new shape
                                tmpFXShape = Shape.intersect(tmpFXShape,tmpFXShape1);
                            }
                            else if (cmd==EnumCommand.AREA_SUBTRACT)
                            {
                                // perform operation and create a new shape
                                tmpFXShape = Shape.subtract(tmpFXShape,tmpFXShape1);
                            }
                            // put new shape in hmFXShapes
                            hmFXShapes.put(nickName1.toUpperCase(),tmpFXShape);
                        }
                        resultValue=tmpFXShape;
                        break;
                    }


                case PATH_APPEND:       //   "pathAppend pathName shapeName [connect=.true]"
                    {
                        if (arrCommand.length<3 || arrCommand.length>4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 or 3 arguments (the name of the shape and optionally the boolean connect argument), received "+(arrCommand.length-1)+" instead");
                        }

                        // the path that a new shape will be appended to
                        String nickName=arrCommand[1];
                        Path fxPath = (Path) hmFXShapes.get(nickName.toUpperCase());
                        PathElement element = null;

                        if (fxPath==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        // the shape that will be appended to the path
                        String nickName2=arrCommand[2];
                        Shape fxShape= (Shape) hmFXShapes.get(nickName2.toUpperCase());

                        boolean bConnect=true;      // default: connect new shape (lineTo instead of moveTo)
                        String  newValue="1";
                        if (arrCommand.length==4)   // connect argument supplied!
                        {
                            newValue=arrCommand[3];
                            if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                            {
                                bConnect=getBooleanValue(newValue);  // get value
                            }
                            else
                            {
                                throw new IllegalArgumentException("the supplied \"connect\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                      + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                            }
                            // bConnect = getBooleanValue(arrCommand[3]);
                        }

                        // get parameters of fxShape, redraw as "PathElement" and append to fxPath
                        boolean shapeClosed = false;
                        if (fxShape instanceof Shape)
                        {

                            if (fxShape instanceof Arc) {

                                // Reproduce JDOR-like append behavior using pure JavaFX.
                                // Connect at the arc's end point and trace the arc towards its start point.
                                Arc arc = (Arc) fxShape;

                                double centerX = arc.getCenterX();
                                double centerY = arc.getCenterY();
                                double radiusX = arc.getRadiusX();
                                double radiusY = arc.getRadiusY();

                                // Angles in degrees as supplied by Arc; convert to radians for trig.
                                double startAngleDeg = arc.getStartAngle();
                                double lengthDeg = arc.getLength();
                                double endAngleDeg = startAngleDeg + lengthDeg;

                                double startAngleRad = Math.toRadians(startAngleDeg);
                                double endAngleRad = Math.toRadians(endAngleDeg);

                                // JavaFX Y axis points downwards; invert sin() to map math coords to screen coords.
                                double startX = centerX + radiusX * Math.cos(startAngleRad);
                                double startY = centerY - radiusY * Math.sin(startAngleRad);
                                double endX = centerX + radiusX * Math.cos(endAngleRad);
                                double endY = centerY - radiusY * Math.sin(endAngleRad);

                                boolean largeArcFlag = Math.abs(lengthDeg) > 180.0;
                                boolean sweepFlag = lengthDeg < 0.0;

                                if (bConnect) {
                                    fxPath.getElements().add(new LineTo(startX, startY));
                                } else {
                                    fxPath.getElements().add(new MoveTo(startX, startY));
                                }

                                fxPath.getElements().add(
                                    new ArcTo(radiusX, radiusY, 0.0, endX, endY, largeArcFlag, sweepFlag)
                                );

                                if (arc.getType() == ArcType.CHORD) {
                                    fxPath.getElements().add(new ClosePath());
                                } else if (arc.getType() == ArcType.ROUND) {
                                    fxPath.getElements().add(new LineTo(centerX, centerY));
                                    fxPath.getElements().add(new ClosePath());
                                }

                            }
                            else if (fxShape instanceof CubicCurve) {

                                // create new Shape type to get parameters of fxShape
                                CubicCurve cubic = (CubicCurve) fxShape;

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo(cubic.getStartX(), cubic.getStartY()));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo(cubic.getStartX(), cubic.getStartY()));
                                }

                                //javafx CubicCurveTo(double controlX1, double controlY1, double controlX2, double controlY2, double x, double y)
                                // get parameters from current fxShape and set parameters of element
                                element = new CubicCurveTo(cubic.getControlX1(), cubic.getControlY1(),
                                        cubic.getControlX2(), cubic.getControlY2(),
                                        cubic.getEndX(), cubic.getEndY());

                                // add new element to path
                                fxPath.getElements().add(element);

                            }
                            else if (fxShape instanceof Ellipse) {

                                // create new Shape type to get parameters of fxShape
                                Ellipse ellipse = (Ellipse) fxShape;

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo(ellipse.getCenterX() + ellipse.getRadiusX(), ellipse.getCenterY()));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo(ellipse.getCenterX() + ellipse.getRadiusX(), ellipse.getCenterY()));
                                }

                                //javafx ArcTo(double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag)
                                // get parameters from current fxShape and set parameters of element
                                double radiusX = ellipse.getRadiusX();
                                double radiusY = ellipse.getRadiusY();
                                double x = ellipse.getCenterX() - ellipse.getRadiusX();
                                double y = ellipse.getCenterY();

                                // two elements ArcTo needed to create an ellipse
                                // large arg flag: determines which arc to use (large/small)
                                // sweep flag: determines which arc to use (direction)
                                element = new ArcTo(radiusX,radiusY,0,x,y,false,true);
                                ArcTo element2 = new ArcTo(radiusX,radiusY,0,ellipse.getCenterX() + ellipse.getRadiusX(),y,false,true);

                                // add new elements to path
                                fxPath.getElements().add(element);
                                fxPath.getElements().add(element2);
                                shapeClosed = true;

                            }
                            else if (fxShape instanceof Line) {

                                // create new Shape type to get parameters of fxShape
                                Line line = (Line) fxShape;

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo(line.getStartX(), line.getStartY()));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo(line.getStartX(), line.getStartY()));
                                }

                                //javafx LineTo(double x, double y)
                                // get parameters from fxShape and set parameters of element
                                element = new LineTo(line.getEndX(), line.getEndY());


                                // add new element to path
                                fxPath.getElements().add(element);

                            }
                            else if (fxShape instanceof Polygon) {

                                // create new Shape type to get parameters of fxShape
                                Polygon poly = (Polygon) fxShape;

                                // get list of points of fxShape
                                List<Double> list = poly.getPoints();

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo (list.get(0), list.get(1) ));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo (list.get(0), list.get(1) ));
                                }

                                // draw lines through each point of fxShape if it has more than one point
                                if (list.size() > 2) {
                                    for (int i = 2; i < list.size(); i = i + 2) {
                                        fxPath.getElements().add(new LineTo(list.get(i), list.get(i+1) ));
                                    }
                                }
                                if (list.size() > 2) {
                                    shapeClosed = true;
                                }

                            }
                            else if (fxShape instanceof QuadCurve) {

                                // create new Shape type to get parameters of fxShape
                                QuadCurve quad = (QuadCurve) fxShape;

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo(quad.getStartX(), quad.getStartY()));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo(quad.getStartX(), quad.getStartY()));
                                }

                                //javafx QuadCurveTo(double controlX, double controlY, double x, double y)
                                // get parameters from fxShape and set parameters of element
                                element = new QuadCurveTo(quad.getControlX(), quad.getControlY(), quad.getEndX(), quad.getEndY());

                                // add new element to path
                                fxPath.getElements().add(element);

                            }
                            else if (fxShape instanceof Rectangle) {

                                // create new Shape type to get parameters of fxShape
                                Rectangle rect = (Rectangle) fxShape;

                                if (bConnect) {
                                    // connect path to starting point of fxShape
                                    fxPath.getElements().add(new LineTo(rect.getX(), rect.getY() + rect.getArcHeight()/2));
                                } else {
                                    // move to starting point of fxShape
                                    fxPath.getElements().add(new MoveTo(rect.getX(), rect.getY() + rect.getArcHeight()/2));
                                }

                                fxPath.getElements().addAll(
                                            new ArcTo(rect.getArcWidth()/2, rect.getArcHeight()/2, 0, rect.getX() + rect.getArcWidth()/2, rect.getY(), false, true),
                                            new LineTo(rect.getX() + rect.getWidth() - rect.getArcWidth()/2, rect.getY()),
                                            new ArcTo(rect.getArcWidth()/2, rect.getArcHeight()/2, 0, rect.getX() + rect.getWidth(), rect.getY() + rect.getArcHeight()/2, false, true),
                                            new LineTo(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight() - rect.getArcHeight()/2),
                                            new ArcTo(rect.getArcWidth()/2, rect.getArcHeight()/2, 0, rect.getX() + rect.getWidth() - rect.getArcWidth()/2, rect.getY() + rect.getHeight(), false, true),
                                            new LineTo(rect.getX() + rect.getArcWidth()/2, rect.getY() + rect.getHeight()),
                                            new ArcTo(rect.getArcWidth()/2, rect.getArcHeight()/2, 0, rect.getX(), rect.getY() + rect.getHeight() - rect.getArcHeight()/2, false, true),
                                            new LineTo(rect.getX(), rect.getY() + rect.getArcHeight()/2)
                                    );
                                        shapeClosed = true;

                            }

                            // in jdor, if bConnect is true, some paths return to starting point of path, others don't
                            // move path to starting point of path
                            // keep on????-----------------------------------------------------------------------------------------------------------------------------------
                            if (bConnect && shapeClosed) {
                                // connect path to starting point of fxShape
                                fxPath.getElements().add(new ClosePath());
                            }

                        }

                        if (isOR)
                        {
                            // writeOutput(slot, canonical+" "+nickName+" "+nickName2+" "+(bConnect ? "1" : "0"));
                            canonical = canonical+" "+nickName+" "+nickName2;
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(bConnect ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case PATH_CLOSE:        //   "pathClose shapeName" ... closePath()
                case PATH_RESET:        //   "pathReset shapeName"
                case PATH_CURRENT_POINT://   "pathCurrentPoint shapeName" | ... getCurrentPoint()
                case PATH_CLONE:        //   "pathClone shapeName [newShapeName]"
                    {
                        if (cmd==EnumCommand.PATH_CLONE)
                        {
                            if (arrCommand.length<2 && arrCommand.length>3)
                            {
                                throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (the name of the Path2D shape), received "+(arrCommand.length-1)+" instead");
                            }
                        }
                        else if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (the name of the Path2D shape), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path fxPath = (Path) hmFXShapes.get(nickName.toUpperCase());

                        if (fxPath==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }

                        if (cmd==EnumCommand.PATH_CLOSE)
                        {
                            ClosePath close = new ClosePath();
                            fxPath.getElements().add(close);

                        }
                        else if (cmd==EnumCommand.PATH_RESET)
                        {
                            // remove all path elements
                            fxPath.getElements().clear();
                        }
                        else if (cmd==EnumCommand.PATH_CURRENT_POINT)
                        {
                            // get a list of elements of fxPath
                            List<PathElement> pathElements = new ArrayList<>(fxPath.getElements());

                            PathElement last = pathElements.get(pathElements.size()-1);

                            double endX = 0;
                            double endY = 0;

                            // if path is closed, then the current point of the path is the end point of the last moveto element
                            if (last instanceof ClosePath) {

                                // remove elements that are not of moveTo type
                                for (int i = 0; i < pathElements.size(); i++) {
                                    if (!(pathElements.get(i) instanceof MoveTo)) {
                                        pathElements.remove(i);
                                    }
                                }

                                // last element closePath cannot be removed, hence take second to last element
                                MoveTo lastMove = (MoveTo) pathElements.get(pathElements.size()-2);

                                endX = lastMove.getX();
                                endY = lastMove.getY();

                            }
                            // otherwise get the end point of the last element
                            else if (last instanceof ArcTo) {
                                ArcTo elementType = (ArcTo) last;
                                endX = elementType.getX();
                                endY = elementType.getY();
                            } else if (last instanceof CubicCurveTo) {
                                CubicCurveTo elementType = (CubicCurveTo) last;
                                endX = elementType.getX();
                                endY = elementType.getY();
                            } else if (last instanceof LineTo) {
                                LineTo elementType = (LineTo) last;
                                endX = elementType.getX();
                                endY = elementType.getY();
                            } else {
                                // QuadCurveTo
                                QuadCurveTo elementType = (QuadCurveTo) last;
                                endX = elementType.getX();
                                endY = elementType.getY();

                            }

                            javafx.geometry.Point2D fxPoint = new javafx.geometry.Point2D(endX,endY);

                            if (fxPoint == null)    // no current point?
                            {
                                resultValue=getNil(slot);   // get and assign .nil
                            }
                            else
                            {
                                resultValue=endX+" "+endY;
                            }

                        }
                        else    // PATH_CLONE: may have a second argument
                        {

                            // create a new path and add all elements of the first one
                            Path clonedPath = new Path(fxPath.getElements());

                            resultValue=clonedPath;

                            if (arrCommand.length==3)   // save the clone?
                            {
                                String nickName2=arrCommand[2];
                                String ucNickName2=nickName2.toUpperCase();
                                hmFXShapes.put(ucNickName2, clonedPath);

                                if (isOR)
                                {
                                    canonical=canonical+" "+nickName2;
                                }
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case PATH_LINE_TO:      //   "pathLineTo shapeName x y"
                case PATH_MOVE_TO:      //   "pathMoveTo shapeName x y"
                case PATH_QUAD_TO:      //   "pathQuadTo shapeName x1 y1 x2 y2"
                case PATH_CURVE_TO:     //   "pathCurveTo shapeName x1 y1 x2 y2 x3 y3"
                    {
                        int argNum=-1;
                        if (cmd==EnumCommand.PATH_LINE_TO || cmd==EnumCommand.PATH_MOVE_TO)
                        {
                            argNum=3;
                        }
                        else if (cmd==EnumCommand.PATH_QUAD_TO)
                        {
                            argNum=5;
                        }
                        else    // PATH_CURVE_TO
                        {
                            argNum=7;
                        }

                        if (arrCommand.length!=(argNum+1))
                        {
                            throw new IllegalArgumentException("this command needs exactly "+argNum+" arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path fxPath = (Path) hmFXShapes.get(nickName.toUpperCase());

                        if (fxPath==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        double arg1 = Double.parseDouble(arrCommand[2]);
                        double arg2 = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical=canonical+" "+nickName+" "+arg1+" "+arg2;
                        }

                        // create new elements lineto, moveto or quadto and add to fxPath
                        if (cmd==EnumCommand.PATH_LINE_TO)
                        {
                            LineTo lineTo = new LineTo();
                            lineTo.setX(arg1);
                            lineTo.setY(arg2);
                            fxPath.getElements().add(lineTo);
                        }
                        else if (cmd==EnumCommand.PATH_MOVE_TO)
                        {
                            MoveTo moveTo = new MoveTo();
                            moveTo.setX(arg1);
                            moveTo.setY(arg2);
                            fxPath.getElements().add(moveTo);
                        }
                        else
                        {
                            double arg3 = Double.parseDouble(arrCommand[4]);
                            double arg4 = Double.parseDouble(arrCommand[5]);
                            if (isOR)
                            {
                                canonical=canonical+" "+arg3+" "+arg4;
                            }
                            if (cmd==EnumCommand.PATH_QUAD_TO)
                            {
                                QuadCurveTo quadCurveTo = new QuadCurveTo();
                                quadCurveTo.setControlX(arg1);
                                quadCurveTo.setControlY(arg2);
                                quadCurveTo.setX(arg3);
                                quadCurveTo.setY(arg4);
                                fxPath.getElements().add(quadCurveTo);
                            }
                            else
                            {
                                double arg5 = Double.parseDouble(arrCommand[6]);
                                double arg6 = Double.parseDouble(arrCommand[7]);
                                if (isOR)
                                {
                                    canonical=canonical+" "+arg5+" "+arg6;
                                }
                                CubicCurveTo curve = new CubicCurveTo();
                                curve.setControlX1(arg1);
                                curve.setControlY1(arg2);
                                curve.setControlX2(arg3);
                                curve.setControlY2(arg4);
                                curve.setX(arg5);
                                curve.setY(arg6);
                                fxPath.getElements().add(curve);
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return resultValue;
                    }

                case PATH_TRANSFORM:    //   "pathTransform shapeName atName"
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String pNickName=arrCommand[1];
                        Path fxPath = (Path) hmFXShapes.get(pNickName.toUpperCase());


                        if (fxPath==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+pNickName+"\" stored");
                        }

                        String atNickName=arrCommand[2];
                        Affine fxAffine = hmFXTransforms.get(atNickName.toUpperCase());

                        if (fxAffine==null)
                        {
                            try
                            {
                                fxAffine = (Affine) getContextVariable(slot, atNickName); // try to get from a Rexx variable
                            }
                            catch (Throwable t) {}
                            if (fxAffine==null)
                            {
                                throw new IllegalArgumentException("no transform with name \""+atNickName+"\" stored, nor a Rexx variable pointing to an AffineTransform");
                            }
                        }

                        String pathAffineName = pNickName+"pathTransform";

                        Affine pathAffine = new Affine();

                        Affine testAffine = new Affine(fxAffine);

                        pathAffine = hmPathTransforms.get(pathAffineName.toUpperCase());

                        if (pathAffine==null)
                        {
                            try
                            {
                                pathAffine = (Affine) getContextVariable(slot, atNickName); // try to get from a Rexx variable
                            }
                            catch (Throwable t) {}

                        }
                        if (pathAffine != null) {
                            pathAffine.append(testAffine);
                        }
                        else {
                            hmPathTransforms.put(pathAffineName.toUpperCase(),fxAffine);

                        }


                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+pNickName+" "+atNickName);
                        }
                        return resultValue;
                    }

                case PATH_WINDING_RULE: //   "pathWindingRule shapeName [WIND_EVEN_ODD=0 | WIND_NON_ZERO=1]"
                    {
                        if (arrCommand.length!=2 && arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];

                        Path fxPath = (Path) hmFXShapes.get(nickName.toUpperCase());


                        if (fxPath==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }

                        int wRule=0;

                        // in javafx fillrule is called even_odd and non_zero
                        FillRule fillRule = fxPath.getFillRule();

                        if (arrCommand.length==2)
                        {
                            resultValue=""+fillRule.toString();
                        }
                        else    // get and set the new windingRule
                        {
                            // WindingRule
                            String ucWR = arrCommand[2].toUpperCase();
                            if (startRexxVariableChar.indexOf(ucWR.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!windingRules.containsKey(ucWR))
                                {
                                    throw new IllegalArgumentException("unknown value for \"windingRule\" argument supplied: ["+ucWR+"]");
                                }
                                wRule=windingRules.get(ucWR);
                            }
                            else // verbatim int type
                            {
                                wRule=Integer.parseInt(ucWR);
                                if (! windingRules.containsValue(wRule))
                                {
                                    throw new IllegalArgumentException("unknown value for \"windingRule\" argument supplied: ["+ucWR+"]");
                                }
                            }

                            if (wRule==1) {
                                fillRule = FillRule.NON_ZERO;
                            } else {
                                fillRule = FillRule.EVEN_ODD;
                            }

                            if (isOR)
                            {
                                if (bUseNames4canonical)
                                {
                                    canonical=canonical+" "+windingRulesInt2Name.get(wRule);
                                }
                                else
                                {
                                    canonical=canonical+" "+wRule;
                                }
                            }

                            fxPath.setFillRule(fillRule);
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                // ---------------------- Animation commands --- Start
                case ANIMATION_FADE:  // "animationFade animName nodeName duration [fromValue] [toValue] [cycleCount] [autoReverse]"
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, nodeName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+nodeName+" "+duration;
                        }
                        
                        // Get the node (shape or shape3D)
                        Node node = resolveNamedAnimationNode(nodeName);
                        if (node == null) {
                            throw new IllegalArgumentException("no node with name \""+nodeName+"\" found");
                        }
                        
                        FadeTransition fade = new FadeTransition(Duration.millis(duration), node);
                        
                        // Optional parameters
                        if (arrCommand.length > 4) {
                            fade.setFromValue(Double.parseDouble(arrCommand[4]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[4];
                            }
                        }
                        if (arrCommand.length > 5) {
                            fade.setToValue(Double.parseDouble(arrCommand[5]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[5];
                            }
                        }
                        if (arrCommand.length > 6) {
                            int cycleCount = Integer.parseInt(arrCommand[6]);
                            fade.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        if (arrCommand.length > 7) {
                            fade.setAutoReverse(getBooleanValue(arrCommand[7]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[7];
                            }
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), fade);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }

                // Tranisition Family: FillTransition, ParallelTransition, SequentialTransition, StrokeTranition
                case ANIMATION_ROTATE:
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, nodeName, duration), received "+(arrCommand.length-1)+" instead");
                        }

                        String animName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);

                        // Get the node (shape or shape3D)
                        Node node = resolveNamedAnimationNode(nodeName);
                        if (node == null) {
                            throw new IllegalArgumentException("no node with name \""+nodeName+"\" found");
                        }

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+nodeName+" "+duration;
                        }

                        RotateTransition rotate = new RotateTransition(Duration.millis(duration), node);

                        // Optional parameters
                        if (arrCommand.length > 4) {
                            rotate.setByAngle(Double.parseDouble(arrCommand[4]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[4];
                            }
                        }
                        if (arrCommand.length > 5) {
                            int cycleCount = Integer.parseInt(arrCommand[5]);
                            rotate.setCycleCount(cycleCount == -1 ? RotateTransition.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[5];
                            }
                        }
                        if (arrCommand.length > 6) {
                            rotate.setAutoReverse(getBooleanValue(arrCommand[6]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        
                        // Handle axis parameters (axisX, axisY, axisZ)
                        if (arrCommand.length > 7 || arrCommand.length > 8 || arrCommand.length > 9) {
                            double axisX = (arrCommand.length > 7) ? Double.parseDouble(arrCommand[7]) : 0.0;
                            double axisY = (arrCommand.length > 8) ? Double.parseDouble(arrCommand[8]) : 0.0;
                            double axisZ = (arrCommand.length > 9) ? Double.parseDouble(arrCommand[9]) : 1.0;
                            rotate.setAxis(new Point3D(axisX, axisY, axisZ));
                            if (isOR)
                            {
                                canonical = canonical+" "+axisX+" "+axisY+" "+axisZ;
                            }
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), rotate);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_FILL:
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, nodeName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String shapeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);
                        
                        // Get the node (shape or shape3D)
                        Shape shape = hmFXShapes.get(shapeName.toUpperCase());
                        if (shape == null) {
                            throw new IllegalArgumentException("no shape with name \""+shapeName+"\" found");
                        }
                        
                        FillTransition fill = new FillTransition(Duration.millis(duration), shape);
                        
                        // Optional parameters
                        if (arrCommand.length > 4) {
                            String colorNickName = arrCommand[4];
                            if (isOR)
                            {
                                canonical = canonical+" "+animName+" "+shapeName+" "+duration+" "+colorNickName;
                            }
                            Color fxColor;
                            try {
                                fxColor = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                            }
                            fill.setFromValue(fxColor);
                        }
                        if (arrCommand.length > 5) {
                            String colorNickName = arrCommand[5];
                            if (isOR)
                            {
                                canonical = canonical+" "+colorNickName;
                            }
                            Color fxColor2;
                            try {
                                fxColor2 = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                            }
                            fill.setToValue(fxColor2);
                        }
                        if (arrCommand.length > 6) {
                            int cycleCount = Integer.parseInt(arrCommand[6]);
                            fill.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        if (arrCommand.length > 7) {
                            fill.setAutoReverse(getBooleanValue(arrCommand[7]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[7];
                            }
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), fill);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_STROKE:
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, shapeName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String shapeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+shapeName+" "+duration;
                        }
                        
                        // Get the shape
                        Shape shape = hmFXShapes.get(shapeName.toUpperCase());
                        if (shape == null) {
                            throw new IllegalArgumentException("no shape with name \""+shapeName+"\" found");
                        }
                        
                        StrokeTransition stroke = new StrokeTransition(Duration.millis(duration), shape);
                        
                        // Optional parameters
                        if (arrCommand.length > 4) {
                            String colorNickName = arrCommand[4];
                            if (isOR)
                            {
                                canonical = canonical+" "+colorNickName;
                            }
                            Color fxColor;
                            try {
                                fxColor = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                            }
                            stroke.setFromValue(fxColor);
                        }
                        if (arrCommand.length > 5) {
                            String colorNickName = arrCommand[5];
                            if (isOR)
                            {
                                canonical = canonical+" "+colorNickName;
                            }
                            Color fxColor2;
                            try {
                                fxColor2 = resolveColorFromToken(slot, colorNickName);
                            } catch (IllegalArgumentException e) {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                            }
                            stroke.setToValue(fxColor2);
                        }
                        if (arrCommand.length > 6) {
                            int cycleCount = Integer.parseInt(arrCommand[6]);
                            stroke.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        if (arrCommand.length > 7 && checkBooleanValue(arrCommand[7])) {
                            stroke.setAutoReverse(getBooleanValue(arrCommand[7]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[7];
                            }
                        }
                        else if (arrCommand.length > 7) {
                            throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[7]+"], expected true/false");
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), stroke);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_SCALE:
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, nodeName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+nodeName+" "+duration;
                        }
                        
                        // Get the node (shape or shape3D)
                        Node node = resolveNamedAnimationNode(nodeName);
                        if (node == null) {
                            throw new IllegalArgumentException("no node with name \""+nodeName+"\" found");
                        }
                        
                        ScaleTransition scale = new ScaleTransition(Duration.millis(duration), node);
                        
                        // Optional parameters
                        if (arrCommand.length > 4) {
                            scale.setByX(Double.parseDouble(arrCommand[4]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[4];
                            }
                        }
                        if (arrCommand.length > 5) {
                            scale.setByY(Double.parseDouble(arrCommand[5]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[5];
                            }
                        }
                        if (arrCommand.length > 6) {
                            scale.setByZ(Double.parseDouble(arrCommand[6]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        if (arrCommand.length > 7) {
                            int cycleCount = Integer.parseInt(arrCommand[7]);
                            scale.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[7];
                            }
                        }
                        if (arrCommand.length > 8) {
                            scale.setAutoReverse(getBooleanValue(arrCommand[8]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[8];
                            }
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), scale);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_TRANSLATE:
                    {
                        if (arrCommand.length < 4)
                        {
                            throw new IllegalArgumentException("this command needs at least 3 arguments (animName, nodeName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+nodeName+" "+duration;
                        }
                        
                        // Get the node (shape or shape3D)
                        Node node = resolveNamedAnimationNode(nodeName);
                        if (node == null) {
                            throw new IllegalArgumentException("no node with name \""+nodeName+"\" found");
                        }
                        
                        TranslateTransition translate = new TranslateTransition(Duration.millis(duration), node);
                        
                        // Optional parameters
                        if (arrCommand.length > 4) {
                            translate.setByX(Double.parseDouble(arrCommand[4]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[4];
                            }
                        }
                        if (arrCommand.length > 5) {
                            translate.setByY(Double.parseDouble(arrCommand[5]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[5];
                            }
                        }
                        if (arrCommand.length > 6) {
                            translate.setByZ(Double.parseDouble(arrCommand[6]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        if (arrCommand.length > 7) {
                            int cycleCount = Integer.parseInt(arrCommand[7]);
                            translate.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[7];
                            }
                        }
                        if (arrCommand.length > 8 && checkBooleanValue(arrCommand[8])) {
                            translate.setAutoReverse(getBooleanValue(arrCommand[8]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[8];
                            }
                        }
                        else if (arrCommand.length > 8) {
                            throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[8]+"], expected true/false");
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), translate);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_PATH:
                    {
                        if (arrCommand.length < 5)
                        {
                            throw new IllegalArgumentException("this command needs at least 4 arguments (animName, nodeName, duration, pathName), received "+(arrCommand.length-1)+" instead");
                        }

                        String animName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        double duration = Double.parseDouble(arrCommand[3]);
                        String pathName = arrCommand[4];

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+nodeName+" "+duration+" "+pathName;
                        }
                        
                        // Get the node (shape or shape3D)
                        Node node = resolveNamedAnimationNode(nodeName);
                        if (node == null) {
                            throw new IllegalArgumentException("no node with name \""+nodeName+"\" found");
                        }

                        Shape pathShape = hmFXShapes.get(pathName.toUpperCase());
                        if (pathShape == null) {
                            throw new IllegalArgumentException("no Path shape with name \""+pathName+"\" found");
                        }
                        
                        PathTransition pathT = new PathTransition(Duration.millis(duration), pathShape, node);
                        
                        // Optional parameters
                        if (arrCommand.length > 5) {
                            int cycleCount = Integer.parseInt(arrCommand[5]);
                            pathT.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[5];
                            }
                        }
                        if (arrCommand.length > 6 && checkBooleanValue(arrCommand[6])) {
                            pathT.setAutoReverse(getBooleanValue(arrCommand[6]));
                            if (isOR)
                            {
                                canonical = canonical+" "+arrCommand[6];
                            }
                        }
                        else if (arrCommand.length > 6) {
                            throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[6]+"], expected true/false");
                        }
                        if (arrCommand.length > 7) {
                            String orientation = arrCommand[7].toUpperCase().replace("-", "_");
                            if ("ORTHOGONAL_TO_TANGENT".equals(orientation) || "ORTHOGONAL".equals(orientation)) {
                                pathT.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
                            }
                            else if ("NONE".equals(orientation)) {
                                pathT.setOrientation(PathTransition.OrientationType.NONE);
                            }
                            else {
                                throw new IllegalArgumentException("invalid path orientation: ["+arrCommand[7]+"], expected NONE or ORTHOGONAL_TO_TANGENT");
                            }
                            if (isOR)
                            {
                                canonical = canonical+" "+orientation;
                            }
                        }

                        hmAnimations.put(animName.toUpperCase(), pathT);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;

                    }
                case ANIMATION_SEQUENTIAL:
                    {
                        if (arrCommand.length < 3)
                        {
                            throw new IllegalArgumentException("this command needs at least 2 arguments (animName, nodeName|firstAnim, anim1, anim2, ...), received "+(arrCommand.length-1)+" instead");
                        }

                        String animName = arrCommand[1];
                        String nodeOrAnim = arrCommand[2];

                        Node node = resolveNamedAnimationNode(nodeOrAnim);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName;
                            if (node != null)
                            {
                                canonical = canonical+" "+nodeOrAnim;
                            }
                        }

                        int endIndex = 0;
                        // If token refers to a node, the child animations start at index 3
                        // Otherwise token is the first animation and children start at index 2
                        int startIndex = (node != null) ? 3 : 2;
                        SequentialTransition seqT = (node != null) ? new SequentialTransition(node) : new SequentialTransition();

                        for (int i = startIndex; i < arrCommand.length; i++) {
                            endIndex = i;
                            // check for optional cycleCount and autoReverse parameters at the end of the command
                            if (i == arrCommand.length - 2) {
                                try {
                                    int cycleCount = Integer.parseInt(arrCommand[i]);
                                    seqT.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                                    if (isOR) {
                                        canonical = canonical+" "+arrCommand[i];
                                    }
                                    continue; // skip to next iteration
                                } catch (NumberFormatException e) {
                                    // not an integer, treat as animation name
                                }
                            } else if (i == arrCommand.length - 1) {
                                try {
                                    if (!checkBooleanValue(arrCommand[i]))
                                    {
                                        throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[i]+"], expected true/false");
                                    }
                                    boolean autoReverse = getBooleanValue(arrCommand[i]);
                                    seqT.setAutoReverse(autoReverse);
                                    if (isOR) {
                                        canonical = canonical+" "+arrCommand[i];
                                    }
                                    continue; // skip to next iteration
                                } catch (IllegalArgumentException e) {
                                    // not a boolean, treat as animation name
                                }
                            }
                            String subAnimName = arrCommand[i];
                            Animation subAnim = hmAnimations.get(subAnimName.toUpperCase());
                            if (subAnim == null) {
                                throw new IllegalArgumentException("no animation with name \""+subAnimName+"\" found");
                            }
                            try {
                                seqT.getChildren().add(subAnim);
                            } catch (IllegalArgumentException e) {
                                throw e;
                            }
                            if (isOR) {
                                canonical = canonical+" "+subAnimName;
                            }
                        }

                        hmAnimations.put(animName.toUpperCase(), seqT);

                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_PARALLEL:
                    {
                        if (arrCommand.length < 3)
                        {
                            throw new IllegalArgumentException("this command needs at least 2 arguments (animName, nodeName|firstAnim, anim1, anim2, ...), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        String nodeOrAnim = arrCommand[2];

                        Node node = resolveNamedAnimationNode(nodeOrAnim);

                        if (isOR)
                        {
                            canonical = canonical+" "+animName;
                            if (node != null)
                            {
                                canonical = canonical+" "+nodeOrAnim;
                            }
                        }

                        // If token refers to a node, the child animations start at index 3
                        // Otherwise token is the first animation and children start at index 2
                        int startIndex = (node != null) ? 3 : 2;
                        ParallelTransition parT = (node != null) ? new ParallelTransition(node) : new ParallelTransition();

                        for (int i = startIndex; i < arrCommand.length; i++) {
                            // check for optional cycleCount and autoReverse parameters at the end of the command
                            if (i == arrCommand.length - 2) {
                                try {
                                    int cycleCount = Integer.parseInt(arrCommand[i]);
                                    parT.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                                    if (isOR) {
                                        canonical = canonical+" "+arrCommand[i];
                                    }
                                    continue; // skip to next iteration
                                } catch (NumberFormatException e) {
                                    // not an integer, treat as animation name
                                }
                            }
                            else if (i == arrCommand.length - 1) {
                                try {
                                    if (!checkBooleanValue(arrCommand[i]))
                                    {
                                        throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[i]+"], expected true/false");
                                    }
                                    boolean autoReverse = getBooleanValue(arrCommand[i]);
                                    parT.setAutoReverse(autoReverse);
                                    if (isOR) {
                                        canonical = canonical+" "+arrCommand[i];
                                    }
                                    continue; // skip to next iteration
                                } catch (IllegalArgumentException e) {
                                    // not a boolean, treat as animation name
                                }
                            }
                            String subAnimName = arrCommand[i];
                            Animation subAnim = hmAnimations.get(subAnimName.toUpperCase());
                            if (subAnim == null) {
                                throw new IllegalArgumentException("no animation with name \""+subAnimName+"\" found");
                            }
                            try {
                                parT.getChildren().add(subAnim);
                            } catch (IllegalArgumentException e) {
                                throw e;
                            }
                            if (isOR) {
                                canonical = canonical+" "+subAnimName;
                            }
                        }
                        
                        hmAnimations.put(animName.toUpperCase(), parT);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_PAUSE_TRANSITION:  // "animationPauseTransition pauseName duration [cycleCount] [autoReverse]"
                    {
                        if (arrCommand.length < 3)
                        {
                            throw new IllegalArgumentException("this command needs at least 2 arguments (pauseName, duration), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String pauseName = arrCommand[1];
                        double duration = Double.parseDouble(arrCommand[2]);

                        if (isOR)
                        {
                            canonical = canonical+" "+pauseName+" "+duration;
                        }
                        
                        PauseTransition pause = new PauseTransition(Duration.millis(duration));
                        
                        // Optional parameters
                        if (arrCommand.length > 3) {
                            int cycleCount = Integer.parseInt(arrCommand[3]);
                            pause.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR) {
                                canonical = canonical+" "+arrCommand[3];
                            }
                        }
                        if (arrCommand.length > 4) {
                            pause.setAutoReverse(getBooleanValue(arrCommand[4]));
                            if (isOR) {
                                canonical = canonical+" "+arrCommand[4];
                            }
                        }
                        
                        hmAnimations.put(pauseName.toUpperCase(), pause);
                        
                        if (isOR) {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case TIMELINE:  // "timeline timelineName [cycleCount] [autoReverse]"
                    {
                        if (arrCommand.length < 2 || arrCommand.length > 4)
                        {
                            throw new IllegalArgumentException("this command needs 1 to 3 arguments (timelineName [cycleCount] [autoReverse]), received "+(arrCommand.length-1)+" instead");
                        }

                        String timelineName = arrCommand[1];
                        String ucTimelineName = timelineName.toUpperCase();

                        if (isOR)
                        {
                            canonical = canonical+" "+timelineName;
                        }

                        Timeline timeline = hmTimelines.get(ucTimelineName);
                        if (timeline == null)
                        {
                            timeline = new Timeline();
                            hmTimelines.put(ucTimelineName, timeline);
                        }

                        if (arrCommand.length > 2)
                        {
                            int cycleCount = Integer.parseInt(arrCommand[2]);
                            timeline.setCycleCount(cycleCount == -1 ? Timeline.INDEFINITE : cycleCount);
                            if (isOR) {
                                canonical = canonical+" "+arrCommand[2];
                            }
                        }

                        if (arrCommand.length > 3)
                        {
                            if (!checkBooleanValue(arrCommand[3]))
                            {
                                throw new IllegalArgumentException("invalid value for autoReverse argument: ["+arrCommand[3]+"], expected true/false");
                            }
                            timeline.setAutoReverse(getBooleanValue(arrCommand[3]));
                            if (isOR) {
                                canonical = canonical+" "+arrCommand[3];
                            }
                        }

                        hmAnimations.put(ucTimelineName, timeline);
                        resultValue = timeline;

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case KEYVALUE:  // "keyValue keyValueName nodeName propertyName targetValue [interpolator]"
                    {
                        if (arrCommand.length < 5 || arrCommand.length > 6)
                        {
                            throw new IllegalArgumentException("this command needs 4 or 5 arguments (keyValueName nodeName propertyName targetValue [interpolator]), received "+(arrCommand.length-1)+" instead");
                        }

                        String keyValueName = arrCommand[1];
                        String nodeName = arrCommand[2];
                        String propertyName = arrCommand[3];
                        String targetToken = arrCommand[4];
                        String interpolatorToken = arrCommand.length == 6 ? arrCommand[5] : null;

                        KeyValue keyValue = createKeyValueFromTokens(slot, nodeName, propertyName, targetToken, interpolatorToken);
                        hmKeyValues.put(keyValueName.toUpperCase(), keyValue);
                        resultValue = keyValue;

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+keyValueName+" "+nodeName+" "+propertyName+" "+targetToken + (interpolatorToken!=null ? " "+interpolatorToken : ""));
                        }
                        break;
                    }
                case KEYFRAME:  // "keyframe timelineName time keyValueName" OR "keyframe timelineName time keyValueArray"
                    {
                        if (arrCommand.length != 4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 3 arguments (timelineName time keyValueNameOrArray), received "+(arrCommand.length-1)+" instead");
                        }

                        String timelineName = arrCommand[1];
                        Timeline timeline = hmTimelines.get(timelineName.toUpperCase());
                        if (timeline == null)
                        {
                            throw new IllegalArgumentException("no timeline with name \""+timelineName+"\" found; create one using command \"timeline\" first");
                        }

                        double timeMs = Double.parseDouble(arrCommand[2]);
                        KeyFrame keyFrame;

                        String keyValueName = arrCommand[3];
                        KeyValue keyValue = hmKeyValues.get(keyValueName.toUpperCase());
                        if (keyValue != null)
                        {
                            keyFrame = new KeyFrame(Duration.millis(timeMs), keyValue);
                        }
                        else
                        {
                            Object contextVariable = getContextVariable(slot, keyValueName);
                            if (!(contextVariable instanceof KeyValue[]))
                            {
                                throw new IllegalArgumentException("no keyValue or keyValue[] with name \""+keyValueName+"\" found");
                            }
                            KeyValue[] keyValueArray = (KeyValue[]) contextVariable;
                            if (keyValueArray.length == 0)
                            {
                                throw new IllegalArgumentException("keyValue[] with name \""+keyValueName+"\" is empty");
                            }
                            for (int i = 0; i < keyValueArray.length; i++)
                            {
                                if (keyValueArray[i] == null)
                                {
                                    throw new IllegalArgumentException("keyValue[] with name \""+keyValueName+"\" contains null element at index "+i);
                                }
                            }
                            keyFrame = new KeyFrame(Duration.millis(timeMs), keyValueArray);
                        }
                        if (isOR)
                        {
                            canonical = canonical+" "+timelineName+" "+timeMs+" "+keyValueName;
                        }
                        timeline.getKeyFrames().add(keyFrame);
                        hmAnimations.put(timelineName.toUpperCase(), timeline);
                        resultValue = keyFrame;

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }
                case ANIMATION_SET_INTERPOLATOR:  // "setInterpolator animName interpolator"
                    {
                        if (arrCommand.length != 3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments (animName interpolator), received "+(arrCommand.length-1)+" instead");
                        }

                        String animName = arrCommand[1];
                        String interpolatorToken = arrCommand[2];
                        String ucAnimName = animName.toUpperCase();

                        Animation anim = hmAnimations.get(ucAnimName);
                        if (anim == null)
                        {
                            throw new IllegalArgumentException("no animation with name \""+animName+"\" found");
                        }

                        if (isOR)
                        {
                            canonical = canonical+" "+animName+" "+interpolatorToken;
                        }

                        Interpolator interpolator = resolveInterpolator(interpolatorToken);

                        if (anim instanceof FadeTransition)
                        {
                            ((FadeTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof RotateTransition)
                        {
                            ((RotateTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof ScaleTransition)
                        {
                            ((ScaleTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof TranslateTransition)
                        {
                            ((TranslateTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof FillTransition)
                        {
                            ((FillTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof StrokeTransition)
                        {
                            ((StrokeTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof PathTransition)
                        {
                            ((PathTransition) anim).setInterpolator(interpolator);
                        }
                        else if (anim instanceof Timeline)
                        {
                            throw new IllegalArgumentException("timeline \""+animName+"\" does not support setInterpolator; interpolation belongs to each KeyValue");
                        }
                        else if (anim instanceof ParallelTransition)
                        {
                            throw new IllegalArgumentException("parallel transition \""+animName+"\" does not support a global interpolator; set interpolators on child animations instead");
                        }
                        else if (anim instanceof SequentialTransition)
                        {
                            throw new IllegalArgumentException("sequential transition \""+animName+"\" does not support a global interpolator; set interpolators on child animations instead");
                        }
                        else
                        {
                            throw new IllegalArgumentException("animation \""+animName+"\" does not support setInterpolator (type: "+anim.getClass().getSimpleName()+")");
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }

                case ANIMATION_PLAY:  // "animationPlay animName"
                    {
                        if (arrCommand.length != 2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (animName), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        Animation anim = hmAnimations.get(animName.toUpperCase());
                        
                        if (anim == null) {
                            throw new IllegalArgumentException("no animation with name \""+animName+"\" found");
                        }
                        
                        anim.play();
                        
                        if (isOR) {
                            writeOutput(slot, canonical+" "+animName);
                        }
                        break;
                    }
                case ANIMATION_PAUSE:  // "animationPause animName"
                    {
                        if (arrCommand.length != 2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (animName), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        Animation anim = hmAnimations.get(animName.toUpperCase());
                        
                        if (anim == null) {
                            throw new IllegalArgumentException("no animation with name \""+animName+"\" found");
                        }
                        
                        anim.pause();
                        
                        if (isOR) {
                            writeOutput(slot, canonical+" "+animName);
                        }
                        break;
                    }

                case ANIMATION_STOP:  // "animationStop animName"
                    {
                        if (arrCommand.length != 2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (animName), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        Animation anim = hmAnimations.get(animName.toUpperCase());
                        
                        if (anim == null) {
                            throw new IllegalArgumentException("no animation with name \""+animName+"\" found");
                        }
                        
                        anim.stop();
                        
                        if (isOR) {
                            writeOutput(slot, canonical+" "+animName);
                        }
                        break;
                    }
                case ANIMATION_STATUS:  // "animationStatus animName" returns status
                    {
                        if (arrCommand.length != 2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (animName), received "+(arrCommand.length-1)+" instead");
                        }
                        
                        String animName = arrCommand[1];
                        Animation anim = hmAnimations.get(animName.toUpperCase());
                        
                        if (anim == null) {
                            throw new IllegalArgumentException("no animation with name \""+animName+"\" found");
                        }
                        
                        String status = anim.getStatus().toString();
                        
                        if (isOR) {
                            writeOutput(slot, canonical+" "+animName);
                        }
                        // System.out.println("[JavaFXDrawingHandler].handleCommand, Animation Status: " + status);
                        return status;
                    }
                // ---------------------- Animation commands --- End


                default:        // unknown/unhandled command, raise failure condition (should never arrive here)
                    if (isOR)
                    {
                        writeOutput(slot, "-- FAILURE (unimplemented command): ["+command+"]");
                    }
                    return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-101", "unimplemented command");
            }
        }
        catch (Throwable t)     // raise error condition if args etc. were causing exceptions
        {
            if (isOR)
            {
                writeOutput(slot, "-- ERROR (argument): ["+command+"]");
            }
            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-102", t.toString() );
        }


        // return null;
        return resultValue;
    }



    private void updateStageOnFxThread()
    {
        if (!changeScene || !fxWinUpdate || fxframe==null) return;

        Scene scene=fxframe;
        Stage stage=fxStage;
        if (stage==null)
        {
            stage=new Stage();
            stage.initStyle(stageDecorated ? StageStyle.DECORATED : StageStyle.UNDECORATED);
            stage.setOnCloseRequest(event -> {
                fxVisible=false;
                activeHandlers.remove(this);
            });
            fxStage=stage;
            setchangeDecorationFalse();
        }
        else if (changeFrame && changeDecoration && stage.isShowing())
        {
            stage=rebuildStage(stage,scene);
            fxStage=stage;
            setchangeDecorationFalse();
        }

        if (stage.getScene()!=scene) stage.setScene(scene);
        stage.setTitle(frameTitle);
        stage.sizeToScene();

        if (changeFrame)
        {
            if (changeDecoration && !stage.isShowing())
            {
                stage.initStyle(stageDecorated ? StageStyle.DECORATED : StageStyle.UNDECORATED);
                setchangeDecorationFalse();
            }
            if (changeBackFront)
            {
                if (winToFront) stage.toFront(); else stage.toBack();
                setChangeBackFrontFalse();
            }
            if (changeAlwaysOnTop)
            {
                stage.setAlwaysOnTop(winAlwaysOnTop);
                setChangeAlwaysOnTopFalse();
            }
            if (changeFrameLocation)
            {
                stage.setX(frameX);
                stage.setY(frameY);
                frameMoved();
            }
            stage.setResizable(fxFrameResizable);
            if (fxVisible) stage.show(); else stage.hide();
            setChangeFrameFalse();
        }
        setChangeSceneFalse();
    }

    @Override
    public void start(Stage stage) throws Exception {

        fxStage = stage;

        // prevents closing of Thread when stage is hidden
        Platform.setImplicitExit(false);
        stage.hide();
        fxReady.countDown();

        // close application thread when window is closed
        stage.setOnCloseRequest(event -> {
            Platform.exit();
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable updater = new Runnable() {
                    @Override
                    public void run() {

                        for (JavaFXDrawingHandler handler : activeHandlers)
                        {
                            handler.updateStageOnFxThread();
                        }

                        Stage stage = fxStage;
                        if (stage == null)
                        {
                            return;
                        }

                        // check if a new scene is available and an update should be executed
                        if (!deque.isEmpty() && changeScene && fxWinUpdate) {

                            // set first element of deque to stage
                            Scene scene = deque.getFirst();
                            logSceneState("updater(before-stage-swap)", scene);

                            if (changeFrame && changeDecoration && stage.isShowing()) {
                                stage = rebuildStage(stage, scene);
                                fxStage = stage;
                                setchangeDecorationFalse();
                            }

                            if (stage.getScene() != scene) {
                                logSceneState("updater(setScene,before)", scene);
                                stage.setScene(scene);
                                logSceneState("updater(setScene,after)", scene);
                            }
                            stage.setTitle(frameTitle);
                            stage.sizeToScene();            // in case of resize

                            // checks if changes to the stage have been signaled
                            if (changeFrame) {
                                try {
                                    // change stage decoration
                                    if (changeDecoration) {
                                        if (stageDecorated) {
                                            if (!stage.isShowing()) {
                                                stage.initStyle(StageStyle.DECORATED);
                                            }
                                        } else {
                                            if (!stage.isShowing()) {
                                                stage.initStyle(StageStyle.UNDECORATED);
                                            }
                                        }
                                        setchangeDecorationFalse();
                                    }
                                    // signal that frame has been changed
                                    setChangeFrameFalse();

                                } catch (Exception e) {
                                    // reset change signals to prevent repeated exceptions
                                    setChangeFrameFalse();
                                    setChangeSceneFalse();
                                    setchangeDecorationFalse();
                                    throw new IllegalArgumentException("WinFrame "
                                            + "cannot be changed once the window "
                                            + " has been set to visible");
                                }

                                // check if stage should be set to front or back
                                if (changeBackFront) {
                                    if (winToFront) {
                                        stage.toFront();
                                    }
                                    else {
                                        stage.toBack();
                                    }
                                }
                                // check if stage should be always on top
                                if (changeAlwaysOnTop) {
                                    if (winAlwaysOnTop) {
                                        stage.setAlwaysOnTop(true);
                                    }
                                    else {
                                        stage.setAlwaysOnTop(false);
                                    }
                                }

                                // check if the location of the frame should be changed
                                if (changeFrameLocation) {
                                    stage.setX(frameX);
                                    stage.setY(frameY);
                                    frameMoved();
                                }

                                // check if frame should be resizable
                                if (fxFrameResizable) {
                                    stage.setResizable(true);
                                } else {
                                    stage.setResizable(false);
                                }

                                // check if frame should be visible
                                if (fxVisible) {
                                    stage.show();
                                    logSceneState("after-show", stage.getScene());
                                    logVisualDetails(stage.getScene());
                                } else {
                                    stage.hide();
                                    logSceneState("after-hide", stage.getScene());
                                    logVisualDetails(stage.getScene());
                                }

                                // signal that changes to frame have been made
                                setChangeFrameFalse();
                            }

                            // signal that scene has been updated
                            setChangeSceneFalse();
                        }
                    }
                };
                // run updater every 10 milliseconds
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
            }
        });
        // don't let thread prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();

    }

    /** Resolves every named JavaFX Node registry accepted by node-based animations. */
    private Node resolveNamedAnimationNode(String nodeName)
    {
        String key=nodeName.toUpperCase();
        Node node=hmFXShapes.get(key);
        if (node==null) node=hm3DShapes.get(key);
        if (node==null) node=hmCamera.get(key);
        if (node==null) node=hmLightBase.get(key);
        return node;
    }

    private String normalizePropertyName(String propertyName)
    {
        return propertyName.trim().toUpperCase().replace("_", "").replace("-", "");
    }

    private Color resolveColorFromToken(Object slot, String token)
    {
        Color fxColor = hmFXColors.get(token.toUpperCase());
        if (fxColor == null)
        {
            try
            {
                fxColor = (Color) getContextVariable(slot, token);
            }
            catch (Throwable t)
            {
            }
        }
        if (fxColor == null)
        {
            try
            {
                fxColor = Color.web(token);
            }
            catch (Throwable t)
            {
            }
        }
        if (fxColor == null)
        {
            throw new IllegalArgumentException("unknown color value: ["+token+"]");
        }
        return fxColor;
    }

    private Interpolator resolveInterpolator(String token)
    {
        if (token == null)
        {
            return null;
        }

        String ucToken = token.toUpperCase();
        if ("DISCRETE".equals(ucToken))
        {
            return Interpolator.DISCRETE;
        }
        if ("LINEAR".equals(ucToken))
        {
            return Interpolator.LINEAR;
        }
        if ("EASE_BOTH".equals(ucToken) || "EASEBOTH".equals(ucToken))
        {
            return Interpolator.EASE_BOTH;
        }
        if ("EASE_IN".equals(ucToken) || "EASEIN".equals(ucToken))
        {
            return Interpolator.EASE_IN;
        }
        if ("EASE_OUT".equals(ucToken) || "EASEOUT".equals(ucToken))
        {
            return Interpolator.EASE_OUT;
        }

        if (ucToken.startsWith("SPLINE(") && ucToken.endsWith(")"))
        {
            String values = ucToken.substring(7, ucToken.length()-1);
            String [] parts = values.split(",");
            if (parts.length != 4)
            {
                throw new IllegalArgumentException("invalid spline interpolator: ["+token+"], expected SPLINE(x1,y1,x2,y2)");
            }
            return Interpolator.SPLINE(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
        }

        throw new IllegalArgumentException("unknown interpolator: ["+token+"]");
    }

    private KeyValue createKeyValueFromTokens(Object slot, String nodeName, String propertyName, String targetToken, String interpolatorToken)
    {
        Node node = resolveNamedAnimationNode(nodeName);
        if (node == null)
        {
            throw new IllegalArgumentException("could not resolve target node ["+nodeName+"]");
        }

        String normalized = normalizePropertyName(propertyName);
        WritableValue<?> writableValue = null;
        Object targetValue = null;

        switch (normalized)
        {
            case "OPACITY":
                writableValue = node.opacityProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "ROTATE":
                writableValue = node.rotateProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "X":
                writableValue = node instanceof Rectangle ? ((Rectangle) node).xProperty() : node.translateXProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "Y":
                writableValue = node instanceof Rectangle ? ((Rectangle) node).yProperty() : node.translateYProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "TRANSLATEX":
                writableValue = node.translateXProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "TRANSLATEY":
                writableValue = node.translateYProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "TRANSLATEZ":
            case "Z":
                writableValue = node.translateZProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "SCALEX":
                writableValue = node.scaleXProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "SCALEY":
                writableValue = node.scaleYProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "SCALEZ":
                writableValue = node.scaleZProperty();
                targetValue = Double.parseDouble(targetToken);
                break;
            case "FILL":
            case "FILLCOLOR":
                if (!(node instanceof Shape))
                {
                    throw new IllegalArgumentException("property ["+propertyName+"] requires a 2D Shape node");
                }
                writableValue = ((Shape) node).fillProperty();
                targetValue = resolveColorFromToken(slot, targetToken);
                break;
            case "STROKE":
            case "STROKECOLOR":
                if (!(node instanceof Shape))
                {
                    throw new IllegalArgumentException("property ["+propertyName+"] requires a 2D Shape node");
                }
                writableValue = ((Shape) node).strokeProperty();
                targetValue = resolveColorFromToken(slot, targetToken);
                break;
            default:
                throw new IllegalArgumentException("unknown keyframe property: ["+propertyName+"]");
        }

        Interpolator interpolator = resolveInterpolator(interpolatorToken);
        @SuppressWarnings("unchecked")
        WritableValue<Object> writableObject = (WritableValue<Object>) writableValue;
        if (interpolator != null)
        {
            return new KeyValue(writableObject, targetValue, interpolator);
        }
        return new KeyValue(writableObject, targetValue);
    }




    private enum ConditionType
    {
        ERROR,
        FAILURE
    };

    /** Create and raise condition in a uniform way. Includes the line number
     *  the name of the executable and the type of the executable.
     *
     *  @param scope
     *  @param nrCommand number of command
     *  @param command in error or failure
     *  @param errCategory 0 raises an error condition, failure condition else
     *  @param rc return code to return
     *  @param errMsg error message to be supplied
     *
     */
    String createCondition (Object slot, int nrCommand, String command, ConditionType errCategory, String rc, String errMsg)
    {
        try
        {
            RexxProxy context = (RexxProxy) getCallerContext(slot);         // get .context
            String   strLineNr  = (String) context.sendMessage0("LINE");    // get line nr

            RexxProxy executable = (RexxProxy) context.sendMessage0("executable");  // get object
            String strExecutable = (String) ((RexxProxy) executable.sendMessage0("CLASS")) .sendMessage0("ID"); // get class name

            String strName       = (String) context.sendMessage0("NAME");   // get class name

            String strErrCategory = "ERROR";        // default to "ERROR"
            if (errCategory==ConditionType.FAILURE) // failure!
            {
                strErrCategory   = "FAILURE";
            }
            String workErrMsg = String.format("%-7s %3s command # %3d [%s] line # %s in %s [%s] cause: [%s]",
                                               strErrCategory,
                                               rc,
                                               nrCommand,
                                               command,
                                               strLineNr,
                                               strExecutable.toLowerCase(),
                                               getFileName(strName),
                                               errMsg
                                             );
            if (isErrorRedirected(slot))
            {
                writeError(slot, workErrMsg);
            }
            Object [] additional = new Object[]{workErrMsg,command,rc,context};
            raiseCondition(slot, strErrCategory, command, additional, rc);
        }
        catch (BSFException bse)    // fallback in case this happens
        {
            String workErrMsg = String.format("%s-8s %4s: unexpected exception [%s] thrown in Java handler's createCondition(): %s rc=[%s] for command # [%3n] command=[%s] errMsg=[%s]",
                                              "FAILURE",
                                              "-99",
                                              bse.toString(),
                                              errCategory==ConditionType.ERROR ? "ERROR" : "FAILURE",
                                              rc,
                                              nrCommand,
                                              command,
                                              errMsg
                                            );
            if (isErrorRedirected(slot))
            {
                writeError(slot, errMsg);
            }
            raiseCondition(slot, "FAILURE", command, new Object[]{workErrMsg}, rc);
        }
        return "-99";
    }

    /** Extract filename and return it according to the operating system. Condition object will
     *  have the fully qualified file name in the additional array.
     *
     * @param name file name, possibly with full path
     * @return filename after last file separator character
     */
    static String getFileName(String name)
    {
        try // safety belt in case weird names get used
        {
            int lastPos=name.lastIndexOf(System.getProperty("file.separator")); // use system's file separator
            if (lastPos>-1)
            {
                return name.substring(lastPos+1);
            }
        }
        catch (Throwable t) {};
        return name;
    }




    /** This will create a word array from the supplied ArrayList returned by getWordBoundaries.
     *
     * @param s String
     * @param al ArrayList returned by getWordBoundaries
     * @return array of words
     */
    public static String[] getNonBlankWords(String s, ArrayList<int[]> al)
    {
    //    ArrayList<int[]> al = getWordBoundaries(s);
        if (al.size()>0)
        {
            String [] arrString = new String[al.size()];
            for (int i=0;i<al.size();i++)
            {
                int[] pos=(int[]) al.get(i);
                arrString[i] = s.substring(pos[0], pos[1]);
            }
            return arrString;
        }
        return new String[0];

    }


    /** Parses the string and returns an ArrayList of int[] denoting the start and the end
     *  of each word in the string delimited by ' ' or '\t' the Rexx whitespace characters.
     *
     * @param s the string to parse
     * @param maxNumberOfWords if -1 processes always entire string, otherwise stops after having
     *                         created maxNumberOfWords items
     * @return an ArrayList of int[] arrays
     */
    public static ArrayList<int[]> getWordBoundaries (String s, int maxNumberOfWords)
    {
        ArrayList <int[]> al = new ArrayList<>();
        if (s==null)
        {
            return al;
        }

        int len=s.length();
        int p=0, start=-1;                   // start and end positions in string
        for (p=0; p<len && maxNumberOfWords!=0; p++)
        {
            char c = s.charAt(p);
            if ( c==' ' || c=='\t' || c=='\n')  // whitespace?
            {
                if (start>=0)   // over a word
                {
                    al.add(new int[]{start,p});
                    start=-1;
                    maxNumberOfWords--;     // one word completed
                }
            }
            else    // not a whitespace
            {
                if (start==-1)  // a new word?
                {
                    start=p;
                }
            }
        }

        if (start>=0)       // pending word
        {
            al.add(new int[]{start,len});
        }
        return al;
    }

    /** Allow "1", ".true", "true" in any case to resolve to a boolean true;
     *  allow "0", ".false", "false" in any case to resolve to a boolean false;
     *
     * @param value the string that represents a BSF4ooRexx850 boolean value
     * @return true if string represents true, false else
     */
    public static boolean checkBooleanValue(String value)
    {
        switch (value.toUpperCase())
        {
            case "0":
            case "1":
            case ".FALSE":
            case ".TRUE":
            case "FALSE":
            case "TRUE":
                return true;
            default:
                return false;
        }
    }

    /** Determines the boolean value of a BSF4ooRexx850 boolean string and returns it.
     *
     *
     * @param value the string containing a BSF4ooRexx850 boolean rendering
     * @return true if "1", ".TRUE", "TRUE" and false else
     */
    public static boolean getBooleanValue(String value)
    {
        switch (value.toUpperCase())
        {
            case "1":
            case ".TRUE":
            case "TRUE":
                return true;
            default:
                return false;
        }
    }


// ----------------------------------------------------------

   final static boolean bStaticDebug=false; // true;
    /** ooRexx array expression open delimiter. */
    static char arrayOpenDeli ='('; // Rexx-like
    /** ooRexx array expression close delimiter. */
    static char arrayCloseDeli=')'; // Rexx-like

    // a string e.g. of " {a,b , c } "
    /** Turns an ooRexx array expression (a string) starting at the supplied position
     *  into a Java <code>int</code> array and returns it.
     *
     * @param value the string containing an ooRexx array expression
     * @param pos the starting position in the string to look for the ooRexx array expression
     * @return float array or <code>null</code> if not a valid ooRexx array expression
     */
    static float[] RexxArrayExpressionToFloatArray(final String value, final int pos)
    {
        int start=value.indexOf(arrayOpenDeli,pos);     // find starting delimiter
        if (start==-1)
        {
            return null;
        }
        int end=value.indexOf(arrayCloseDeli,start)+1;  // find closing delimiter
        if (end==0)
        {
            return null;
        }
        String [] parts=value.substring(start+1,end-1).split(",");
        float [] res=new float[parts.length];       // create array of needed size
        for (int i=0;i<parts.length;i++)        // fill array
        {
            res[i]=Float.parseFloat(strip(parts[i]));
        }
        return res;
    }



    // a string e.g. of " {a,b , c } "
    /** Turns an ooRexx array expression (a string) starting at the supplied position
     *  into a Java <code>int</code> array and returns it.
     *
     * @param value the string containing an ooRexx array expression
     * @param pos the starting position in the string to look for the ooRexx array expression
     * @return int array or <code>null</code> if not a valid ooRexx array expression
     */
    static int[] RexxArrayExpressionToIntArray(final String value, final int pos)
    {
        int start=value.indexOf(arrayOpenDeli,pos);     // find starting delimiter
        if (start==-1)
        {
            return null;
        }
        int end=value.indexOf(arrayCloseDeli,start)+1;  // find closing delimiter
        if (end==0)
        {
            return null;
        }
        String [] parts=value.substring(start+1,end-1).split(",");
        int [] res=new int[parts.length];       // create array of needed size
        for (int i=0;i<parts.length;i++)        // fill array
        {
            res[i]=Integer.parseInt(strip(parts[i]));
        }
        return res;
    }

    static double[] RexxArrayExpressionToDoubleArray(final String value, final int pos)
    {
        int start=value.indexOf(arrayOpenDeli,pos);     // find starting delimiter
        if (start==-1)
        {
            return null;
        }
        int end=value.indexOf(arrayCloseDeli,start)+1;  // find closing delimiter
        if (end==0)
        {
            return null;
        }
        String [] parts=value.substring(start+1,end-1).split(",");
        double [] res=new double[parts.length];       // create array of needed size
        for (int i=0;i<parts.length;i++)        // fill array
        {
            res[i]=Integer.parseInt(strip(parts[i]));
        }
        return res;
    }

    /** Removes leading and trailing blanks from integer value.
     *
     * @param value string to work on
     * @return string without leading and trailing blanks
     */
    // assume integer value may be enclosed in space character
    static String strip(String value)
    {
        int start = 0;
        int len=value.length();
        for ( ; start<len; start++)     // find first non-blank
        {
            if (value.charAt(start)!=' ') break;    // arrived at non-blank
        }
        if (start>=len)     // o.k. empty string or blanks only
        {
            return value;   // return unchanged parseInt() will throw an exception
        }

        int end   = start;              // proceed from current position
        for ( ; end<len;end++)          // find next blank, if any
        {
            if (value.charAt(end)==' ') break;    // arrived at a trailing blank
        }
        return value.substring(start,end);  // return digits only so Integer.parseInt() does not barf
    }

    /** Returns a Rexx string literal for canonical command output. */
    static String toRexxStringLiteral(String value)
    {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /** Removes surrounding Rexx string-literal quotes from a token, if present. */
    static String fromRexxStringLiteral(String value)
    {
        if (value!=null && value.length()>=2)
        {
            char quote=value.charAt(0);
            if ((quote=='"' || quote=='\'') && value.charAt(value.length()-1)==quote)
            {
                String doubled = "" + quote + quote;
                return value.substring(1, value.length()-1).replace(doubled, "" + quote);
            }
        }
        return value;
    }

        // create an ooRexx like array expression
    /** Returns a Rexx array expression from received <code>float</code> array.
     * @param floatArray int array to work on
     * @return rendered Rexx array expression
     */
    static String floatArrayToRexxArrayExpression(float [] floatArray)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(arrayOpenDeli);
        if (floatArray != null)
        {
            int last=floatArray.length-1;
            for (int i=0;i<floatArray.length;i++)
            {
                sb.append(""+floatArray[i]);
                if (i<last) sb.append(',');
            }
        }
        sb.append(arrayCloseDeli);
        return sb.toString();
    }

        // create an ooRexx like array expression
    /** Returns a Rexx array expression from received <code>int</code> array.
     * @param intArray int array to work on
     * @return rendered Rexx array expression
     */
    static String intArrayToRexxArrayExpression(int [] intArray)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(arrayOpenDeli);
        if (intArray != null)
        {
            int last=intArray.length-1;
            for (int i=0;i<intArray.length;i++)
            {
                sb.append(""+intArray[i]);
                if (i<last) sb.append(',');
            }
        }
        sb.append(arrayCloseDeli);
        return sb.toString();
    }

    /** Parse string value to an int value, even if the string value represents a float or double value.
     *  Implemenation is geared toward speed, only use Double.parseDouble() if a dot contained in the
     *  the string value.
     *
     * @param strValue
     * @return int value representint strValue
     * @throws NumberFormatException
     */
    static int string2int(String strValue) throws NumberFormatException
    {
        // if a dot in string assume Double value, else an int value
        boolean assumeInt=true;
        for (int i=0; i<strValue.length(); i++)
        {
            if (strValue.charAt(i)=='.')
            {
                assumeInt=false;
                break;
            }
        }
        if (assumeInt)
        {
            return Integer.parseInt(strValue);
        }
        return (int) Math.round(Double.parseDouble(strValue));
    }


}



// ============================================================================
/** Define the commands we process, support the string renderings.
 */
enum EnumCommand {
    // define the enum values for the commands available to Rexx programs,
    // supplying the strings associated with them                   // ? synonyms too

    // 20220929: do not support Shapes, maybe in a future update; one can always
    //           get access using "gc" command and interact with it from ooRexx directly

    BACKGROUND                  ( "background"               ) ,    //   "background  [colorNickName]" query current background color or set
    CLEAR_RECT                  ( "clearRect"                ) ,    //   "clear width height": uses current color to clear background
    CLIP                        ( "clip"                     ) ,    //   "clip [x y w h]" -> get (getClipBounds()) or clipRect(x y w h)
    CLIP_REMOVE                 ( "clipRemove"               ) ,    //   "clipRemove" -> setClip(null)
    COLOR                       ( "color"                    ) ,    //   "color  [colorNickName [r g b [a]]" query current color or set + define new color
    COMPOSITE                   ( "composite"                ) ,    //   "composite [rule [alpha]]": get current or get and set new;
    COPY_AREA                   ( "copyArea"                 ) ,    //   "copyArea width height distX distY"
    DRAW_3D_RECT                ( "draw3DRect"               ) ,    //   "draw3DRect width height raised" - 20220906
    DRAW_ARC                    ( "drawArc"                  ) ,    //   "drawArc  width height startAngle arcAngle"
    DRAW_IMAGE                  ( "drawImage"                ) ,    //   "drawImage nickName [bkgColor] | nickName width height [bkgColor] | nickName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"
    DRAW_LINE                   ( "drawLine"                 ) ,    //   "drawLine toX toY"
    DRAW_OVAL                   ( "drawOval"                 ) ,    //   "drawOval width height"
    DRAW_POLYGON                ( "drawPolygon"              ) ,    //   "drawPolygon  []xPoints []yPoints nPoints
    DRAW_POLYLINE               ( "drawPolyline"             ) ,    //   "drawPolyline []xPoints []yPoints nPoints
    DRAW_RECT                   ( "drawRect"                 ) ,    //   "drawRect width height"
    DRAW_ROUND_RECT             ( "drawRoundRect"            ) ,    //   "drawRoundRect width height arcWidth arcHeight"
    DRAW_STRING                 ( "drawString"               ) ,    //   "drawString text"
    FILL_3D_RECT                ( "fill3DRect"               ) ,    //   "fill3DRect width height raised" - 20220906
    FILL_ARC                    ( "fillArc"                  ) ,    //   "fillArc  width height startAngle arcAngle"
    FILL_OVAL                   ( "fillOval"                 ) ,    //   "fillOval width height"
    FILL_POLYGON                ( "fillPolygon"              ) ,    //   "fillPolygon  []xPoints []yPoints nPoints
    FILL_RECT                   ( "fillRect"                 ) ,    //   "fillRect width height"
    FILL_ROUND_RECT             ( "fillRoundRect"            ) ,    //   "fillRoundRect width height arcWidth arcHeight"
    FONT                        ( "font"                     ) ,    //   "font   [fontNickName [name]]" query or set font using curr type and size
    FONT_SIZE                   ( "fontSize"                 ) ,    //   "fontSize size
    FONT_STYLE                  ( "fontStyle"                ) ,    //   "fontStyle 0=PLAIN | 1=BOLD | 2=ITALIC | 3=BOLD+ITALIC
    GET_GC                      ( "GC"                       ) ,    //   "GC"    returns current GC (GraphicsContext)
    GET_IMAGE                   ( "image"                    ) ,    //   "image [nickName]" returns image or nickName-stored image
    GET_STATE                   ( "getState"                 ) ,    //   "getState [ctxtVariableName]" returns a StringTable with current variables, stacks and HashMaps;
    GRADIENT_PAINT              ( "gradientPaint"            ) ,    //   "gradientPaint paintNickName [x1 y1 colorName1 x2 y2 colorName2 [cyclic]]": set or define and set gradientPaint
    IMAGE_COPY                  ( "imageCopy"                ) ,    //   "imageCopy [nickName]": get a copy of the current image, if nickName supplied use from registry and if not found from Rexx variable
    IMAGE_SIZE                  ( "imageSize"                ) ,    //   "imageSize [imageNickName]": returns width and height
    IMAGE_TYPE                  ( "imageType"                ) ,    //   "imageType [nickName]": get current image's type, if nickName supplied return its image type
    LOAD_IMAGE                  ( "loadImage"                ) ,    //   "loadImage imageNickName filename", loads an image -> returns its dimension
    MOVE_TO                     ( "moveTo"                   ) ,    //   "pos [x y]" query or set position, synonyms: "goto"
    NEW_IMAGE                   ( "newImage"                 ) ,    //   "new[Image] [width height [type]]" creates a new BufferedImage
    PAINT                       ( "paint"                    ) ,    //   "paint [gradiantPaintNickName|colorNickName]" query or set paint (for background) // 2022-09-22, paint, as of 20220922 cf. <http://underpop.online.fr/j/java/help/colors-graphics-programming.html.gz>
    POP_GC                      ( "popGC"                    ) ,    //   "popGC"  pop and set previous gc
    POP_IMAGE                   ( "popImage"                 ) ,    //   "popImage"  pop and display previous image
    PREFERRED_IMAGE_TYPE        ( "preferredImageType"       ) ,    //   "preferredImageType [type]": get or set preferred image type, type can be integer or name of BufferedImage TYPE constant
    PRINT_IMAGE                 ( "printImage"               ) ,    //   "printImage [nickName]": print image, either current or the one by nickname
    PRINT_POS                   ( "printPos"                 ) ,    //   "printPos [X Y]: get or set position of left upperhand corner; honored if printScaleToPage is false
    PRINT_SCALE                 ( "printScale"               ) ,    //   "printScale [scaleX [scaleY]": get or set scaleX (if no scaleY use scaleX); honored if printScaleToPage is false
    PRINT_SCALE_TO_PAGE         ( "printScaleToPage"         ) ,    //   "printScaleToPage [.true|.false]": get or set
    PUSH_GC                     ( "pushGC"                   ) ,    //   "pushGC" push current gc
    PUSH_IMAGE                  ( "pushImage"                ) ,    //   "pushImage [nickName]" push current image displayed
    RENDER                      ( "render"                   ) ,    //   "render [opt1 [opt2]]": returns or sets the current antialiasing settings for rendering
    RESET                       ( "reset"                    ) ,    //   "reset" synonym: "clear", clears everything, resets
    ROTATE                      ( "rotate"                   ) ,    //   "rotate [theta [x y]]" query or set rotation
    SAVE_IMAGE                  ( "saveImage"                ) ,    //   "saveImage filename", saves current image to file
    SCALE                       ( "scale"                    ) ,    //   "scale [x [y]]" query or set scale for x, y; if y omitted, uses x
    SET_PAINT_MODE              ( "setPaintMode"             ) ,    //   "setPaintMode": cf. Grahpics#setPaintMode()
    SET_XOR_MODE                ( "setXorMode"               ) ,    //   "xorMode colorNickName": sets xorMode to supplied color
    SHEAR                       ( "shear"                    ) ,    //   "shear [x [y]]" query or set shear for x, y; if y omitted, uses x

    // 2022-12-03
    SHAPE                       ( "shape"                    ) ,    //   "shape name [type args...]" query or create a shape
    DRAW_SHAPE                  ( "drawShape"                ) ,    //   "drawShape name" draws the named shape
    FILL_SHAPE                  ( "fillShape"                ) ,    //   "fillShape name" fills the named shape
    CLIP_SHAPE                  ( "clipShape"                ) ,    //   "clipShape name" defines intersection with named shape as the new clip

    SLEEP                       ( "sleep"                    ) ,    //   "sleep msecs" (uses Thread.sleep())
    STRING_BOUNDS               ( "stringBounds"             ) ,    //   "stringBounds string": returns the Rectangle2D x y w h (in floats?)
    STROKE                      ( "stroke"                   ) ,    //   "stroke [strokeNickName [width [cap join [miterlimit [[]floatDash floatDashPhase]]]]" query or set stroke
    TRANSFORM                   ( "transform"                ) ,    //   "transform { | RESET | translateX translateY scaleX scaleY shearX shearY} " query, change reset Graphics2D's AfineTransform
    TRANSLATE                   ( "translate"                ) ,    //   "translate [x [y]]" query origin or move origin; if y omitted, uses x
    WIN_ALWAYS_ON_TOP           ( "winAlwaysOnTop"           ) ,    //   "winAlwaysOnTop [.true | .false]" get or set of this window state
    WIN_ALWAYS_ON_TOP_SUPPORTED ( "winAlwaysOnTopSupported"  ) ,    //   "winAlwaysOnTopSupported" get value
    WIN_CLOSE                   ( "winClose"                 ) ,    // S "winClose" synonym: "exit" closes JavaDrawingFrame instance
    WIN_FRAME                   ( "winFrame"                 ) ,    //   "winFrame [.true|.false]": query or set JFrame's frame according to argument
    WIN_HIDE                    ( "winHide"                  ) ,    //   "winHide" hide JavaDrawingFrame
    WIN_LOCATION                ( "winLocation"              ) ,    //   (synonym: WINMOVETO)"winLocation [x y]" get or set location on screen
    WIN_RESIZABLE               ( "winResizable"             ) ,    //   "winResizable [.true|.false]": query or set JFrame's frame according to argument
    WIN_SCREEN_SIZE             ( "winScreenSize"            ) ,    //   "winScreenSize" query screen's width and size
    WIN_SHOW                    ( "winShow"                  ) ,    //   "winShow" show JavaDrawingFrame
    WIN_SIZE                    ( "winSize"                  ) ,    //   "winSize [width height]" query or set position
    WIN_TITLE                   ( "winTitle"                 ) ,    //   "winTitle [newtitle]": get or set JFrame's title
    WIN_TO_BACK                 ( "winToBack"                ) ,    //   "winToBack" put window into the back
    WIN_TO_FRONT                ( "winToFront"               ) ,    //   "winToFront" put window into the front
    WIN_UPDATE                  ( "winUpdate"                ) ,    //   "winUpdate [.true|.false]" get or set whether Frame should be updated upon draws/changes
    WIN_VISIBLE                 ( "winVisible"               ) ,    //   "winVisible [.true|.false]" get or set visibility

    // 2022-12-07, Path2D specific support
    SHAPE_BOUNDS                ( "shapeBounds"              ) ,    //   "shapeBounds shapeName" ... getBounds2D()
    SHAPE_GET_PATH_ITERATOR     ( "pathIterator"             ) ,    //  "pathIterator shapeName [affinityTransform=.nil [flatness]]" -> returns getPathIterator(AffinityTransform[,flatness])

    // ad append: if shapeName not found check for Rexx variable name (must be either a Shape or a PathIterator!)
    PATH_APPEND                 ( "pathAppend"               ) ,    //   "pathAppend shapeName | "rexxVarName (Shape|PathIterator)" [connect=.true]"
    PATH_CLOSE                  ( "pathClose"                ) ,    //   "pathClose shapeName" ... closePath()
    PATH_CURRENT_POINT          ( "pathCurrentPoint"         ) ,    //   "pathCurrentPoint shapeName" ... getCurrentPoint
    PATH_CURVE_TO               ( "pathCurveTo"              ) ,    //   "pathCurveTo shapeName x1 y1 x2 y2 x3 y3"
    PATH_LINE_TO                ( "pathLineTo"               ) ,    //   "pathLineTo shapeName x y"
    PATH_MOVE_TO                ( "pathMoveTo"               ) ,    //   "pathMoveTo shapeName x y"
    PATH_QUAD_TO                ( "pathQuadTo"               ) ,    //   "pathQuadTo shapeName x1 y1 x2 y2"
    PATH_RESET                  ( "pathReset"                ) ,    //   "pathReset shapeName"
    PATH_WINDING_RULE           ( "pathWindingRule"          ) ,    //   "pathWindingRule shapeName [WIND_EVEN_ODD=0 | WIND_NON_ZERO=1]" ... getWindingRule(), setWindingRule(newValue)

    // 2022-12-08
    PATH_CLONE                  ( "pathClone"                ) ,    //   "pathClone shapeName [newShapeName]"
    PATH_TRANSFORM              ( "pathTransform"            ) ,    //   "pathTransform shapeName atName"
    CLIPBOARD_GET               ( "clipboardGet"             ) ,    //   "clipboardGet [imgName]" ... save in hmImages, if no imgName then under "CLIPBOARD"
    CLIPBOARD_SET               ( "clipboardSet"             ) ,    //   "clipboardSet [imgName]" ... if imgName: get from hmImages, else from currImage
    CLIPBOARD_SET_WITHOUT_ALPHA ( "clipboardSetWithoutAlpha" ) ,    //   "clipboardSetWithoutAlpha [imgName]" ... if imgName: get from hmImages, else from currImage

    // 2023-01-08
    ASSIGN_RC                   ( "assignRC"                 ) ,    //   "assignRC RexxVariable"

    // 2023-06-05
    AREA_ADD                    ( "areaAdd"                  ) ,    //   "areaAdd         areaName shapeName..."
    AREA_EXCLUSIVE_OR           ( "areaExclusiveOr"          ) ,    //   "areaExclusiveOr areaName shapeName..."
    AREA_INTERSECT              ( "areaIntersect"            ) ,    //   "areaIntersect   areaName shapeName..."
    AREA_SUBTRACT               ( "areaSubtract"             ) ,    //   "areaSubtract    areaName shapeName..."
    AREA_TRANSFORM              ( "areaTransform"            ) ,    //   "areaTransform   areaName tNickname"

    // 2024-02-26
    SHAPE_3D                    ("shape3d"                   ),     //    "shape3d name [type args...]" query or create a shape
    DRAW_3D_SHAPE               ( "draw3DShape"              ) ,    //   "draw3DShape name" draws the named shape
    FILL_3D_SHAPE               ( "fill3DShape"              ) ,    //   "fill3DShape name" draws the named shape
    ROTATE_3D_SHAPE             ( "rotate3DShape"            ) ,    //   "rotate3DShape name [type args...]" rotates the named 3D shape
    SCALE_3D_SHAPE              ( "scale3DShape"             ) ,    //   "scale3DShape name [type args...]" scales the named 3D shape
    SHEAR_3D_SHAPE              ( "shear3DShape"             ) ,    //   "shear3DShape name [type args...]" shears the named 3D shape
    TRANSLATE_3D_SHAPE          ( "translate3DShape"         ) ,    //   "translate3DShape name [type args...]" translates (moves) the named 3D shape
    CAMERA                      ( "camera"                   ) ,    //   "camera name [type args...]" query or create a camera
    SET_CAMERA                  ( "setCamera"                ) ,    //   "setCamera name" set camera
    LIGHT                       ( "light"                    ) ,    //   "light name [type args...]" query or create a light
    SET_LIGHT                   ( "setLight"                 ) ,    //   "setLight name" add light
    MATERIAL                    ( "material"                 ) ,    //   "material name [color]" create a material
    MATERIAL_COLOR              ( "materialColor"            ) ,    //   "material name [type args...]" set color of named material
    MAP                         ( "map"                      ) ,    //   "map mapPath [args...]" create map
    MATERIAL_MAP                ( "materialMap"              ) ,    //   "material materialName type mapPath/mapName" set map of named material
    SET_MATERIAL                ( "setMaterial"              ) ,    //   "setMaterial shape3DName materialName" set material to 3D shape
    
    // 2026-08-04
    CULL_FACE                   ( "cullFace"                 ) ,    //   "cullFace shapeName [NONE|BACK|FRONT]" query or set face culling
    ROTATE_CAMERA               ( "rotateCamera"             ) ,    //   same arguments as rotate3DShape
    TRANSLATE_CAMERA            ( "translateCamera"          ) ,    //   same arguments as translate3DShape
    CAMERA_NEAR_CLIP            ( "cameraNearClip"           ) ,    //   "cameraNearClip name [distance]"
    CAMERA_FAR_CLIP             ( "cameraFarClip"            ) ,    //   "cameraFarClip name [distance]"
    TRANSLATE_POINT_LIGHT       ( "translatePointLight"      ) ,    //   same arguments as translate3DShape

    // Animation commands
    ANIMATION_FADE              ( "animationFade"            ) ,    //   "animationFade animName nodeName duration [fromValue] [toValue] [cycleCount] [autoReverse]"
    ANIMATION_ROTATE            ( "animationRotate"          ) ,    //   "animationRotate animName nodeName duration [byAngle|toAngle] [cycleCount] [autoReverse]"
    ANIMATION_FILL              ("animationFill"             ) ,    //   "animationFill animName nodeName duration [fromValue] [toValue] [cycleCount] [autoReverse]"
    ANIMATION_STROKE            ("animationStroke"           ) ,    //   "animationStroke animName shapeName duration [fromColor] [toColor] [cycleCount] [autoReverse]"
    ANIMATION_SCALE             ( "animationScale"           ) ,    //   "animationScale animName nodeName duration [byX byY|toX toY] [cycleCount] [autoReverse]"
    ANIMATION_TRANSLATE         ( "animationTranslate"       ) ,    //   "animationTranslate animName nodeName duration [byX byY|toX toY] [cycleCount] [autoReverse]"
    ANIMATION_PATH              ( "animationPath"            ) ,    //   "animationPath animName nodeName duration pathName [cycleCount] [autoReverse] [orientation]"
    ANIMATION_SEQUENTIAL        ( "animationSequential"      ) ,    //   "animationSequential animName animName1 animName2 ..." defines a sequential animation
    ANIMATION_PARALLEL          ( "animationParallel"        ) ,    //   "animationParallel animName animName1 animName2 ..." defines a parallel animation
    ANIMATION_PAUSE_TRANSITION  ( "animationPauseTransition" ) ,    //   "animationPauseTransition animName" pauses the transition of the animation, i.e. freezes it at current state; if already paused, resumes it
    ANIMATION_SET_INTERPOLATOR  ( "setInterpolator"          ) ,    //   "setInterpolator animName interpolator" sets interpolator on supported transitions
    ANIMATION_PLAY              ( "animationPlay"            ) ,    //   "animationPlay animName"
    ANIMATION_PAUSE             ( "animationPause"           ) ,    //   "animationPause animName"
    ANIMATION_STOP              ( "animationStop"            ) ,    //   "animationStop animName"
    ANIMATION_STATUS            ( "animationStatus"          ) ,    //   "animationStatus animName" returns status (RUNNING, PAUSED, STOPPED)
    TIMELINE                    ( "timeline"                 ) ,    //   "timeline timelineName [cycleCount] [autoReverse]"
    KEYFRAME                    ( "keyframe"                 ) ,    //   "keyframe timelineName time keyValueNameOrArray"
    KEYVALUE                    ( "keyValue"                 ) ,    //   "keyValue keyValueName nodeName propertyName targetValue [interpolator]"

    NO_OP                       ( "noOp"                     )      // last element, doing nothing
                               ;


    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link #getCommand(String mixedCase)}.
     */
    final static HashMap<String,EnumCommand> upperCase2Command=new HashMap<>(64);

    static   // use static block to add synonym strings to upperCase2Command Map
    {
        // add all types to HashMap (in declaration order)
         for (EnumCommand it : EnumCommand.values())
         {
             upperCase2Command.put(it.upperCase, it);
         }

         // add synonyms         new name        enum type
         upperCase2Command.put( "AREAUNION"    , AREA_ADD  ) ;    // 2023-06-08
         upperCase2Command.put( "AREAXOR"      , AREA_EXCLUSIVE_OR  ) ;   // 2023-06-08
         upperCase2Command.put( "CLEAR"        , RESET     ) ;
         upperCase2Command.put( "COLOUR"       , COLOR     ) ;
         upperCase2Command.put( "GETCLIPBOARD" , CLIPBOARD_GET) ; // 2023-06-11
         upperCase2Command.put( "GOTO"         , MOVE_TO   ) ;
         upperCase2Command.put( "LOCATION"     , MOVE_TO   ) ;    // 2022-10-23: new alias
         upperCase2Command.put( "NEW"          , NEW_IMAGE ) ;    // 2023-01-18
         upperCase2Command.put( "POS"          , MOVE_TO   ) ;    // 2022-10-29: new alias
         upperCase2Command.put( "POSITION"     , MOVE_TO   ) ;    // 2022-10-26: new alias
         upperCase2Command.put( "SETCLIPBOARD" , CLIPBOARD_SET) ; // 2023-06-11
         upperCase2Command.put( "SETCLIPBOARDWITHOUTALPHA" , CLIPBOARD_SET_WITHOUT_ALPHA) ; // 2023-06-13

         upperCase2Command.put( "SHAPECLIP"    , CLIP_SHAPE   ) ; // 2023-06-06
         upperCase2Command.put( "SHAPEDRAW"    , DRAW_SHAPE   ) ; // 2023-06-06
         upperCase2Command.put( "SHAPEFILL"    , FILL_SHAPE   ) ; // 2023-06-06
         upperCase2Command.put( "WINMOVETO"    , WIN_LOCATION ) ;

         // 2024-02-26
         upperCase2Command.put( "DRAWSHAPE3D"  , DRAW_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "DRAW3DSHAPE"  , DRAW_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "FILLSHAPE3D"  , FILL_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "FILL3DSHAPE"  , FILL_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "ROTATESHAPE3D", ROTATE_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "ROTATE3DSHAPE", ROTATE_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "SCALESHAPE3D"  , SCALE_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "SCALE3DSHAPE"  , SCALE_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "SHEARSHAPE3D"  , SHEAR_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "SHEAR3DSHAPE"  , SHEAR_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "TRANSLATESHAPE3D", TRANSLATE_3D_SHAPE) ; // 2024-02-26
         upperCase2Command.put( "TRANSLATE3DSHAPE", TRANSLATE_3D_SHAPE) ; // 2024-02-26

         // Animation synonyms
         upperCase2Command.put( "FADE"              , ANIMATION_FADE      ) ;
         upperCase2Command.put( "FADETRANSITION"    , ANIMATION_FADE      ) ;
         upperCase2Command.put( "ROTATETRANSITION"  , ANIMATION_ROTATE    ) ;
         upperCase2Command.put( "FILLTRANSITION"    , ANIMATION_FILL      ) ;
         upperCase2Command.put( "STROKETRANSITION"  , ANIMATION_STROKE    ) ;
         upperCase2Command.put( "SCALETRANSITION"   , ANIMATION_SCALE     ) ;
         upperCase2Command.put( "TRANSLATETRANSITION", ANIMATION_TRANSLATE ) ;
         upperCase2Command.put( "PATHTRANSITION"    , ANIMATION_PATH      ) ;
         upperCase2Command.put( "SEQUENTIALTRANSITION", ANIMATION_SEQUENTIAL) ;
         upperCase2Command.put( "PARALLELTRANSITION", ANIMATION_PARALLEL  ) ;
         upperCase2Command.put( "PLAY"              , ANIMATION_PLAY      ) ;
         upperCase2Command.put( "PAUSE"             , ANIMATION_PAUSE     ) ;
         upperCase2Command.put( "STOP"              , ANIMATION_STOP      ) ;

    }


    final String mixedCase;
    /** Returns the String value in mixed case.
     *
     * @return  the String value in mixed case.
     */
    public String getMixedCase()
    {
        return mixedCase;
    }

    final String upperCase;
    /** Returns the String value in upper case.
     *
     * @return  the String value in upper case.
     */
    public String getUpperCase()
    {
        return upperCase;
    }

     // note: no access to static fields allowed in constructor (Enum values get created before static block
    EnumCommand(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the EnumCommand corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the command from Rexx
     *
     * @return the enum EnumCommand corresponding to the supplied String, <code>null</code> if not defined
     */
    static public EnumCommand getCommand(String mixedCase)
    {
        if (mixedCase==null)
        {
            return null;
        }
        return upperCase2Command.get(mixedCase.toUpperCase());
    }


    public static void main(String[] args) {
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE="+DRAW_LINE);
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE.toString()="+DRAW_LINE.toString());
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE.getMixedCase()="+DRAW_LINE.getMixedCase());
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"NOT_AVAIL\")="+getCommand("NOT_AVAIL"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"cLeAr\")  ="+getCommand("cLeAr"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"drawOval\")="+getCommand("drawOval"));

        for (int i=0; i<args.length; i++)
        {
            System.err.println("... ... ... .. main, args["+i+"/"+args.length+"]: getCommand("+args[i]+")=["+getCommand(args[i])+"]");
        }
    }
}

// ============================================================================
/** Define the Shapes we know of, add aliases for the supported ones (first significant letter(s)),
    names without trailing 2D),
    support the string renderings.
 */

enum EnumShape {
    // define the Shape enum values for the commands available to Rexx programs,
    // supplying the strings associated with them

    SHAPE_AREA                  ( "Area"                    ),
    SHAPE_ARC2D                 ( "Arc2D"                   ),
    SHAPE_CUBIC_CURVE2D         ( "CubicCurve2D"            ),
    SHAPE_ELLIPSE2D             ( "Ellipse2D"               ),
    SHAPE_LINE2D                ( "Line2D"                  ),
    SHAPE_PATH2D                ( "Path2D"                  ),
    SHAPE_POLYGON               ( "Polygon"                 ),  // no trailing "2D"
    SHAPE_QUAD_CURVE2D          ( "QuadCurve2D"             ),
    SHAPE_RECTANGLE2D           ( "Rectangle2D"             ),
    SHAPE_ROUND_RECTANGLE2D     ( "RoundRectangle2D"        ),
    
    // 3D Shapes
    // 2024-02-26
    SHAPE_BOX                   ("Box"                       ),
    SHAPE_CYLINDER              ("Cylinder"                  ),
    SHAPE_SPHERE                ("Sphere"                    ),


    NO_OP                       ( "noOp"                    )      // last element, doing nothing
    ;

    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link #getShape(String mixedCase)}.
     */
    final static HashMap<String,EnumShape> upperCase2Shape=new HashMap<>(64);

    static   // use static block to add synonym strings to upperCase2Shape Map
    {
        // add all types to HashMap (in declaration order)
         for (EnumShape it : EnumShape.values())
         {
             upperCase2Shape.put(it.upperCase, it);
         }

         // we allow shortening the shape type to the significant letters (at least 2 letters)
         // add synonyms      new name      enum type
         upperCase2Shape.put( "ARC"           , SHAPE_ARC2D             ) ;
         upperCase2Shape.put( "CUBIC"         , SHAPE_CUBIC_CURVE2D     ) ;
         upperCase2Shape.put( "CUBICCURVE"    , SHAPE_CUBIC_CURVE2D     ) ;
         upperCase2Shape.put( "ELLI"          , SHAPE_ELLIPSE2D         ) ;
         upperCase2Shape.put( "ELLIPSE"       , SHAPE_ELLIPSE2D         ) ;
         upperCase2Shape.put( "LINE"          , SHAPE_LINE2D            ) ;
         upperCase2Shape.put( "PATH"          , SHAPE_PATH2D            ) ;
         upperCase2Shape.put( "QUAD"          , SHAPE_QUAD_CURVE2D      ) ;
         upperCase2Shape.put( "QUADCURVE"     , SHAPE_QUAD_CURVE2D      ) ;
         upperCase2Shape.put( "RECT"          , SHAPE_RECTANGLE2D       ) ;
         upperCase2Shape.put( "RECTANGLE"     , SHAPE_RECTANGLE2D       ) ;
         upperCase2Shape.put( "ROUNDRECT"     , SHAPE_ROUND_RECTANGLE2D ) ;
         upperCase2Shape.put( "ROUNDRECTANGLE", SHAPE_ROUND_RECTANGLE2D ) ;

         // 3D Shapes
         // 2024-02-26
         upperCase2Shape.put( "BOX"           , SHAPE_BOX               ) ;
         upperCase2Shape.put( "CYLINDER"      , SHAPE_CYLINDER          ) ;
         upperCase2Shape.put( "SPHERE"        , SHAPE_SPHERE            ) ;

    }


    final String mixedCase;
    /** Returns the String value in mixed case.
     *
     * @return  the String value in mixed case.
     */
    public String getMixedCase()
    {
        return mixedCase;
    }

    final String upperCase;
    /** Returns the String value in upper case.
     *
     * @return  the String value in upper case.
     */
    public String getUpperCase()
    {
        return upperCase;
    }

     // note: no access to static fields allowed in constructor (Enum values get created before static block
    EnumShape(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the EnumShape corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the command from Rexx
     *
     * @return the enum EnumShape corresponding to the supplied String, <code>null</code> if not defined
     */
    static public EnumShape getShape(String mixedCase)
    {
        if (mixedCase==null)
        {
            return null;
        }
        return upperCase2Shape.get(mixedCase.toUpperCase());
    }


    public static void main(String[] args) {
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D="+SHAPE_ARC2D);
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D.toString()="+SHAPE_ARC2D.toString());
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D.getMixedCase()="+SHAPE_ARC2D.getMixedCase());
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"NOT_AVAIL\")="+getShape("NOT_AVAIL"));
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"A\")        ="+getShape("A"));
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"q\")        ="+getShape("q"));

        for (int i=0; i<args.length; i++)
        {
            System.err.println("... ... ... .. main, args["+i+"/"+args.length+"]: getShape("+args[i]+")=["+getShape(args[i])+"]");
        }
    }
}
