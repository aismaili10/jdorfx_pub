package oracle_animation_basics;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Oracle JavaFX 8 Animation Basics, Example 3-3. */
public class Example_03_ParallelTransition extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle rectParallel = new Rectangle(10, 200, 50, 50);
        rectParallel.setArcHeight(15);
        rectParallel.setArcWidth(15);
        rectParallel.setFill(Color.DARKBLUE);
        rectParallel.setTranslateX(50);
        rectParallel.setTranslateY(75);

        FadeTransition fade = new FadeTransition(Duration.millis(3000), rectParallel);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);

        TranslateTransition translate = new TranslateTransition(Duration.millis(2000), rectParallel);
        translate.setFromX(50);
        translate.setToX(350);
        translate.setCycleCount(2);
        translate.setAutoReverse(true);

        RotateTransition rotate = new RotateTransition(Duration.millis(3000), rectParallel);
        rotate.setByAngle(180);
        rotate.setCycleCount(4);
        rotate.setAutoReverse(true);

        ScaleTransition scale = new ScaleTransition(Duration.millis(2000), rectParallel);
        scale.setToX(2);
        scale.setToY(2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);

        ParallelTransition parallel = new ParallelTransition(fade, translate, rotate, scale);
        parallel.setCycleCount(Timeline.INDEFINITE);
        parallel.play();

        stage.setTitle("Oracle JavaFX 8 - Parallel Transition");
        stage.setScene(new Scene(new Group(rectParallel), 520, 400, Color.WHITE));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
