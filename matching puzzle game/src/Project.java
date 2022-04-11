//ICS201 - Project

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Project extends Application {

    //Fruit Paths
    Image apple = new Image("apple.png");
    Image banana = new Image("banana.png");
    Image cherry = new Image("cherry.png");
    Image kiwi = new Image("kiwi.png");
    Image mango = new Image("mango.png");
    Image orange = new Image("orange.png");
    Image pineapple = new Image("pineapple.png");
    Image strawberry = new Image("strawberry.png");
    Image questionMark = new Image("questionMark.png");
    private final Image[] fruitList = {apple, banana, cherry, kiwi, mango, orange, pineapple, strawberry};
    private ImageView[][] myFruits = initializeList(4, fruitList);

    private GridPane gridPane;
    private GridPane scorePane;

    Media sound = new Media(new File("sound.mp3").toURI().toString());
    Media win = new Media(new File("win.mp3").toURI().toString());
    Media wrong = new Media(new File("wrong.mp3").toURI().toString());

    private int openedCards = 0;
    private Button flipped1, flipped2;
    private int matches = 0;

    private RunTimer timer;
    private Button reset;

    @Override
    public void start(Stage primaryStage) {

        //Program Start Initialization
        gridPane = new GridPane();
        (new reset()).insert();

        //TimerBox
        Timer myTimer = new Timer();
        timer = new RunTimer();
        myTimer.scheduleAtFixedRate(timer, 0, 1000);
        Text timeElapsed = new Text("Time Elapsed: ");
        timeElapsed.setFont(new Font("Arial", 26));
        HBox timeBox = new HBox(timeElapsed, timer.getTimeText());
        timeBox.setSpacing(4);
        timeBox.setAlignment(Pos.CENTER);

        //BottomBox
        reset = new Button("Reset");
        reset.setPrefSize(100, 40);
        reset.setOnAction(new reset());
        HBox botBox = new HBox(reset);
        botBox.setSpacing(4);
        botBox.setAlignment(Pos.CENTER);

        //HighScores
        scorePane = new GridPane();
        updateScorePane();

        //BorderPane
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        borderPane.setTop(timeBox);
        timeBox.setAlignment(Pos.BOTTOM_CENTER);
        borderPane.setBottom(botBox);

        //Scene
        StackPane gamePane = new StackPane(scorePane, borderPane);
        Scene scene = new Scene(gamePane, 1500, 1000);
        primaryStage.setTitle("Memory Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //flipCard Class
    class flipCard implements EventHandler<ActionEvent> {
        private final int row;
        private final int col;

        public flipCard(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void handle(ActionEvent actionEvent) {
            if (openedCards == 2) {
                openedCards = 0;
                if (!((ImageView) flipped1.getGraphic()).getImage().equals(((ImageView) flipped2.getGraphic()).getImage())) {
                    flipped1.setGraphic(new ImageView(questionMark));
                    flipped2.setGraphic(new ImageView(questionMark));
                    //Wrong Sound
                }
                if (((ImageView) (((Button) actionEvent.getSource()).getGraphic())).getImage().equals(questionMark)) {
                    flipped1 = (Button) actionEvent.getSource();
                    flipped1.setGraphic(myFruits[row][col]);
                    openedCards++;
                }
            } else if (((ImageView) (((Button) actionEvent.getSource()).getGraphic())).getImage().equals(questionMark)) {
                if (openedCards == 0) {
                    flipped1 = (Button) actionEvent.getSource();
                    flipped1.setGraphic(myFruits[row][col]);
                } else {
                    flipped2 = (Button) actionEvent.getSource();
                    flipped2.setGraphic(myFruits[row][col]);
                }
                openedCards++;
            }
            if (openedCards == 2) {
                if (((ImageView) flipped1.getGraphic()).getImage().equals(((ImageView) flipped2.getGraphic()).getImage())) {
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();
                    matches++;
                }
                else {
                    MediaPlayer mediaPlayer = new MediaPlayer(wrong);
                    mediaPlayer.play();
                }
            }
            if (matches == 8) {
                MediaPlayer winPlayer = new MediaPlayer(win);
                winPlayer.play();
                String timeElapsed = timer.getTimeText().getText();
                timer.stop();
                try {
                    FileWriter fileWriter = new FileWriter("HighScores.txt", true);
                    fileWriter.write("Score: " + timeElapsed + "\n");
                    fileWriter.close();
                } catch (IOException e) {
                    System.out.println("File wasn't found.");
                }
                reset.setText("Start Again?");
                updateScorePane();
                Text winText = new Text("Congratulations!");
                winText.setFont(new Font("Arial", 46));
                gridPane.getChildren().clear();
                gridPane.add(winText, 0, 0);
                matches = 0;
            }
        }
    }

    //reset Class
    class reset implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            gridPane.getChildren().clear();
            Image[] newFruitList = fruitList.clone();
            Collections.shuffle(Arrays.asList(newFruitList));
            myFruits = initializeList(4, newFruitList);
            reset.setText("Reset");
            flipped1 = null;
            flipped2 = null;
            openedCards = 0;
            matches = 0;
            insert();
            timer.reset();
        }

        public void insert() {
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++) {
                    Button b = new Button();
                    b.setGraphic(new ImageView(questionMark));
                    b.setOnAction(new flipCard(i, j));
                    gridPane.add(b, i, j);
                }
            gridPane.setAlignment(Pos.CENTER);
        }
    }

    //Timer Class
    static class RunTimer extends TimerTask {
        private final int[] time = {0, 0};
        private final Text timeText = new Text();
        private boolean stop = false;

        public RunTimer() {
            timeText.setText("00:00");
            timeText.setFont(new Font("Arial", 26));
        }

        public void run() {
            if (!stop) {
                if (time[1] == 59) {
                    time[0]++;
                    time[1] = 0;
                } else {
                    time[1]++;
                }
                if (time[0] < 10)
                    if (time[1] < 10)
                        Platform.runLater(() -> timeText.setText("0" + time[0] + ":0" + time[1]));
                    else
                        Platform.runLater(() -> timeText.setText("0" + time[0] + ":" + time[1]));

                else if (time[1] < 10)
                    Platform.runLater(() -> timeText.setText(time[0] + ":0" + time[1]));
                else
                    Platform.runLater(() -> timeText.setText(time[0] + ":" + time[1]));
            }
        }

        public void stop() {
            stop = true;
        }

        public Text getTimeText() {
            return timeText;
        }

        public void reset() {
            time[0] = 0;
            time[1] = 0;
            timeText.setText("00:00");
            stop = false;
        }
    }

    //Initialize Method
    public ImageView[][] initializeList(int squareSide, Image[] fruitList) {
        ImageView[][] myFruits = new ImageView[squareSide][squareSide];
        for (int i = 0, c = 0; i < squareSide; i++) {
            for (int j = 0; j < squareSide; j++, c++) {
                if (!(c < fruitList.length))
                    c = 0;
                myFruits[i][j] = new ImageView(fruitList[c]);
            }
            Collections.shuffle(Arrays.asList(myFruits[i]));
        }
        return myFruits;
    }

    //Top5 Method
    public String[] getTop5() {
        String[] top5 = new String[5];
        try {
            FileInputStream inputStream = new FileInputStream("HighScores.txt");
            Scanner scan = new Scanner(inputStream);
            ArrayList<String> allScores = new ArrayList<>();
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                allScores.add(line.substring(line.indexOf(":") + 1));
            }
            Collections.sort(allScores);
            for (int i = 0; i < 5; i++) {
                if (i < allScores.size())
                    top5[i] = allScores.get(i);
            }
            inputStream.close();
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File wasn't found.");
        } catch (IOException e) {
            System.out.println("IOException exception.");
        }
        return top5;
    }

    //UpdateScorePane Method
    public void updateScorePane() {
        scorePane.getChildren().clear();
        Text highScoreText = new Text("High Scores: ");
        highScoreText.setFont(new Font("Arial", 26));
        scorePane.add(highScoreText, 0, 0);
        String[] top5 = getTop5();
        for (int i = 0; i < top5.length; i++) {
            Text score = new Text(top5[i]);
            score.setFont(new Font("Arial", 26));
            scorePane.add(score, 0, i + 1);
        }
        scorePane.setAlignment(Pos.CENTER_LEFT);
    }

    public static void main(String[] args) {
        launch(args);
    }
}