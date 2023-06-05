package com.example.ti3;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class ShootingMovingExample extends Application {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private static final double CELL_SIZE = 50.0;
    private static final double BULLET_SPEED = 5.0;
    private static final double PLAYER_SPEED = 3.0;
    private int currentLevel = 1;
    private boolean gold = false;
    private boolean useYellowBullet = true;
    private int bulletsShot = 0;
    private static final int MAX_BULLETS = 6;



    private static class Bullet {
        Circle shape;
        double dx, dy;
        Color color;

        Bullet(double startX, double startY, double endX, double endY, Color color) {
            double angle = Math.atan2(endY - startY, endX - startX);
            double spawnRadius = 20;
            boolean useYellowBullet = true;
            this.color=color;
            double spawnX = startX + Math.cos(angle) * spawnRadius;
            double spawnY = startY + Math.sin(angle) * spawnRadius;

            if (useYellowBullet) {
                this.shape = new Circle(spawnX, spawnY, 5, YELLOW);
            } else {
                this.shape = new Circle(spawnX, spawnY, 5, RED);
            }


            this.dx = Math.cos(angle) * BULLET_SPEED;
            this.dy = Math.sin(angle) * BULLET_SPEED;
        }

        void update() {
            shape.setCenterX(shape.getCenterX() + dx);
            shape.setCenterY(shape.getCenterY() + dy);
            shape.setFill(color); // Actualiza el color de la forma Circle
        }

    }
    private static final Color YELLOW = Color.YELLOW;
    private static final Color RED = Color.RED;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        // Get screen width and height
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        // Set stage width and height to 50% of screen size
        double stageWidth = screenWidth / 2;
        double stageHeight = screenHeight / 2;
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        // Calculate rows and columns based on full screen size
        int rows = (int) (screenHeight / CELL_SIZE);
        int cols = (int) (screenWidth / CELL_SIZE);


        // Create the map
        List<Rectangle> map = new ArrayList<>();
        Random rand = new Random();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if(rand.nextFloat() < 0.2) {  // 20% chance of a wall
                    Rectangle cell = new Rectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    cell.setFill(Color.GREEN);
                    map.add(cell);
                    root.getChildren().add(cell);
                }
            }
        }

        // Create the player
        Rectangle player = new Rectangle(CELL_SIZE*0.5, CELL_SIZE*0.5, Color.BLUE);
        root.getChildren().add(player);
        // Create a list to hold the bullets
        List<Bullet> bullets = new ArrayList<>();

        //create golden rectangle
        // Choose a location for the golden rectangle
        double goldenRectangleX = 350;
        double goldenRectangleY = 265;

        Rectangle goldenRectangle = new Rectangle(goldenRectangleX, goldenRectangleY, CELL_SIZE*1.3, CELL_SIZE*1.3);
        goldenRectangle.setFill(Color.GOLD);

        // Create a set to hold the currently pressed keys
        Set<KeyCode> keys = new HashSet<>();
        System.out.println("Current level"+currentLevel);
        // Create a timer to update the game
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Try to update the player
                double newPlayerX = player.getX();
                double newPlayerY = player.getY();
                if (keys.contains(KeyCode.W)) newPlayerY -= PLAYER_SPEED;
                if (keys.contains(KeyCode.S)) newPlayerY += PLAYER_SPEED;
                if (keys.contains(KeyCode.A)) newPlayerX -= PLAYER_SPEED;
                if (keys.contains(KeyCode.D)) newPlayerX += PLAYER_SPEED;

                // Check for collisions with the map
                boolean collision = false;
                for (Rectangle cell : map) {
                    if (cell.getBoundsInParent().intersects(newPlayerX, newPlayerY, player.getWidth(), player.getHeight())) {
                        collision = true;
                        break;
                    }
                }

                // If no collision, move the player
                if (!collision) {
                    player.setX(newPlayerX);
                    player.setY(newPlayerY);
                }

                // If the player is at the edge of the screen, increase the stage size
                if (player.getX() + player.getWidth() > stage.getWidth() && stage.getX() == 0 && currentLevel == 1) {
                    stage.setWidth(screenWidth);  // expand to full width
                    currentLevel++;
                    System.out.println("Current level"+currentLevel);
                }
                else if (player.getY() + player.getHeight() > stage.getHeight() && stage.getY() == 0 && currentLevel == 2) {

                    // instead of expanding the window, move all game objects up by the height of the window
                    double offsetY = stage.getHeight();
                    player.setY(player.getY() - offsetY);
                    for (Rectangle cell : map) {
                        cell.setY(cell.getY() - offsetY);
                    }
                    for (Bullet bullet : bullets) {
                        bullet.shape.setCenterY(bullet.shape.getCenterY() - offsetY);
                    }
                    currentLevel++;
                    System.out.println("Current level"+currentLevel);
                }
                else if (currentLevel == 3 && !gold) {
                    gold = true;
                    root.getChildren().add(goldenRectangle);
                    System.out.println("Gold: "+gold);
                }
                // If the player is at the top of the screen, decrease the stage size and move all game objects down
                else if (player.getY() < 0 && currentLevel == 3) {
                    double offsetY = stage.getHeight();
                    player.setY(player.getY() + offsetY);
                    for (Rectangle cell : map) {
                        cell.setY(cell.getY() + offsetY);
                    }
                    for (Bullet bullet : bullets) {
                        bullet.shape.setCenterY(bullet.shape.getCenterY() + offsetY);
                    }
                    currentLevel--;
                    System.out.println("Current level"+currentLevel);

                    gold = false;
                    root.getChildren().remove(goldenRectangle);
                    System.out.println("Gold: "+gold);
                }


                // Update the bullets
                for (Bullet bullet : bullets) {
                    bullet.update();
                }

                // Check for collisions
                for (Bullet bullet : new ArrayList<>(bullets)) {
                    // Check for collisions with the player
                    if (bullet.shape.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        // Handle player being hit by a bullet
                        System.out.println("Player hit!");
                        bullets.remove(bullet);
                        root.getChildren().remove(bullet.shape);
                        continue;
                    }

                    // Check for collisions with the walls
                    for (Rectangle cell : new ArrayList<>(map)) {
                        if (bullet.shape.getBoundsInParent().intersects(cell.getBoundsInParent())) {
                            // Remove the bullet and the cell
                            bullets.remove(bullet);
                            map.remove(cell);
                            root.getChildren().removeAll(bullet.shape, cell);
                            break;
                        }
                    }

                    // Check for collisions with the golden rectangle
                    if (gold && player.getBoundsInParent().intersects(goldenRectangle.getBoundsInParent())) {
                        // Handle player touching the golden rectangle
                        Pane victoryPane = new Pane();
                        victoryPane.setStyle("-fx-background-color: #000000;");

                        Label victoryLabel = new Label("Victory!");
                        victoryLabel.setFont(new Font("Arial", 50));
                        victoryLabel.setTextFill(Color.GOLD);
                        victoryLabel.setAlignment(Pos.CENTER);
                        victoryPane.getChildren().add(victoryLabel);
                        victoryLabel.setLayoutX(stageWidth/2 - victoryLabel.getPrefWidth()/2);
                        victoryLabel.setLayoutY(stageHeight/2 - victoryLabel.getPrefHeight()/2);

                        // Adding a simple scale transition to the victory message
                        ScaleTransition st = new ScaleTransition(Duration.millis(2000), victoryLabel);
                        st.setByX(1.5f);
                        st.setByY(1.5f);
                        st.setCycleCount(2);
                        st.setAutoReverse(true);
                        st.play();

                        Scene victoryScene = new Scene(victoryPane, stageWidth, stageHeight);
                        stage.setScene(victoryScene);
                    }

                }

            }
        };
        timer.start();

        Scene scene = new Scene(root, COLS * CELL_SIZE, ROWS * CELL_SIZE);


        scene.setOnKeyPressed((KeyEvent event) -> {
            keys.add(event.getCode());

            if (event.getCode() == KeyCode.X) {
                System.out.println("Player coordinates: X = " + player.getX() + ", Y = " + player.getY());
            } else if (event.getCode() == KeyCode.C) {
                useYellowBullet = !useYellowBullet;
                System.out.println("Weapon switched: " + (useYellowBullet ? "Yellow" : "Red"));
            }
        });

        scene.setOnKeyReleased((KeyEvent event) -> {
            keys.remove(event.getCode());
        });


        scene.setOnKeyReleased((KeyEvent event) -> {
            keys.remove(event.getCode());
        });

        scene.setOnMouseClicked((MouseEvent event) -> {
            // Shoot a bullet
            if (bulletsShot < MAX_BULLETS) {
                Color bulletColor;
                if (useYellowBullet) {
                    bulletColor = Color.YELLOW;
                } else {
                    bulletColor = Color.RED;
                }

                double bulletStartX = player.getX() + CELL_SIZE * 0.25;
                double bulletStartY = player.getY() + CELL_SIZE * 0.25;
                double bulletEndX = event.getX();
                double bulletEndY = event.getY();

                Bullet bullet = new Bullet(bulletStartX, bulletStartY, bulletEndX, bulletEndY, bulletColor);
                bullets.add(bullet);
                root.getChildren().add(bullet.shape);

                bulletsShot++;
            } else {
                System.out.println("Reloading...");
                // Lógica para recargar el arma
                bulletsShot = 0; // Reinicia el contador de balas disparadas
            }
        });





        stage.setX(0);
        stage.setY(0);
        stage.setScene(scene);
        //stage.setFullScreen(true);  // Set full screen
        stage.show();

        // Request focus on the root node
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
