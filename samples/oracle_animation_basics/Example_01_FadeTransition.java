package oracle_animation_basics;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Oracle JavaFX 8 Animation Basics, Example 3-1. */
public class Example_01_FadeTransition extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle rect1 = new Rectangle(10, 10, 100, 100);
        rect1.setArcHeight(20);
        rect1.setArcWidth(20);
        rect1.setFill(Color.RED);

        FadeTransition ft = new FadeTransition(Duration.millis(3000), rect1);
        ft.setFromValue(1.0);
        ft.setToValue(0.1);
        ft.setCycleCount(Timeline.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();

        stage.setTitle("Oracle JavaFX 8 - Fade Transition");
        stage.setScene(new Scene(new Group(rect1), 420, 280, Color.WHITE));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
