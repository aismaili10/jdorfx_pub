import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Oracle JavaFX 8 Animation Basics, Example 3-4. */
public class Example_04_SequentialTransition extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle rectSeq = new Rectangle(25, 25, 50, 50);
        rectSeq.setArcHeight(15);
        rectSeq.setArcWidth(15);
        rectSeq.setFill(Color.CRIMSON);
        rectSeq.setTranslateX(50);
        rectSeq.setTranslateY(50);

        FadeTransition fade = new FadeTransition(Duration.millis(1000), rectSeq);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(1);
        fade.setAutoReverse(true);

        TranslateTransition translate = new TranslateTransition(Duration.millis(2000), rectSeq);
        translate.setFromX(50);
        translate.setToX(375);
        translate.setCycleCount(1);
        translate.setAutoReverse(true);

        RotateTransition rotate = new RotateTransition(Duration.millis(2000), rectSeq);
        rotate.setByAngle(180);
        rotate.setCycleCount(4);
        rotate.setAutoReverse(true);

        ScaleTransition scale = new ScaleTransition(Duration.millis(2000), rectSeq);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(2);
        scale.setToY(2);
        scale.setCycleCount(1);
        scale.setAutoReverse(true);

        SequentialTransition sequential = new SequentialTransition(fade, translate, rotate, scale);
        sequential.setCycleCount(Timeline.INDEFINITE);
        sequential.setAutoReverse(true);
        sequential.play();

        stage.setTitle("Oracle JavaFX 8 - Sequential Transition");
        stage.setScene(new Scene(new Group(rectSeq), 520, 300, Color.WHITE));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
