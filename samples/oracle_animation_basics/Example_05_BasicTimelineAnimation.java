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

/** Oracle JavaFX 8 Animation Basics, Example 3-5. */
public class Example_05_BasicTimelineAnimation extends Application {
    @Override
    public void start(Stage stage) {
        Rectangle rect = new Rectangle(100, 50, 100, 50);
        rect.setFill(Color.RED);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        KeyValue kv = new KeyValue(rect.xProperty(), 300);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), kv));
        timeline.play();

        stage.setTitle("Oracle JavaFX 8 - Basic Timeline");
        stage.setScene(new Scene(new Group(rect), 520, 180, Color.WHITE));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
