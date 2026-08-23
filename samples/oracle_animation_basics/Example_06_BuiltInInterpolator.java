import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Oracle JavaFX 8 Animation Basics, Example 3-7. */
public class Example_06_BuiltInInterpolator extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle rect = new Rectangle(100, 50, 100, 50);
        rect.setFill(Color.BROWN);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        KeyValue kv = new KeyValue(rect.xProperty(), 300, Interpolator.EASE_BOTH);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), kv));
        timeline.play();

        stage.setTitle("Oracle JavaFX 8 - Timeline EASE_BOTH");
        stage.setScene(new Scene(new Group(rect), 520, 180, Color.WHITE));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
