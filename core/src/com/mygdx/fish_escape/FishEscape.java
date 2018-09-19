package com.mygdx.fish_escape;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.Random;

/**
 * This program is a fish game. The goal is to get to the finish line with as few moves as possible
 * without getting eaten. There will be several enemies who will try to eat the player. There will
 * also be obstacles that the player will need to navigate in order to obtain the finish line. The
 * obstacles are random, providing the user with a unique experience each game. Their high score
 * also be saved.
 */
public class FishEscape extends ApplicationAdapter {
	private SpriteBatch batch;
    // Stage is used for actors, like buttons
    private Stage stage;
    private Stage restartStage;
	private Texture background;
	private Random randomGenerator;
	private ShapeRenderer shapeRenderer;
    // General game data
    private float cellHeight;
    private float cellWidth;
    private final int numberOfVerticalCells = 14;
    private final int numberOfHorizontalCells = 8;
    private final int goal = 40;
    private int movesLeft = goal;
    private BitmapFont gameFont;
    private Texture loseGame;
    private Texture winGame;
    private int highScore;
    private int moves = 0;
    private int points = 0;
    private int moveTime = 60;
    private int frames = 0;
    private int level = 1;
    private BitmapFont finishLineFont;
    private BitmapFont highScoreFont;
    // Main fish data
	private Texture upFish;
	private Texture leftFish;
	private Texture rightFish;
    private char fishDirection;
    private final int startYLocation = 5;
    private final int startXLocation = 3;
    private float fishX;
	private float fishY;
	private Rectangle fishCell;
	private boolean eaten = false;
	// Enemy fish data
    private final int numberOfEnemies = 4;
    private Texture enemyTextures[] = new Texture[numberOfEnemies];
    private Rectangle enemies[]     = new Rectangle[numberOfEnemies];
    private final int enemyStartX[] = new int[numberOfEnemies];
    private Texture enemyLeftTexture;
    private Texture enemyRightTexture;
    private Texture enemyUpTexture;
    private boolean jumped = false;
    // Obstacle data
    private final int numberOfObstacles = 24;
    private final int obstaclesPerRow   = 4;
    private final int obstacleYGap      = 3;
    private Rectangle obstacles[]  = new Rectangle[numberOfObstacles];
    private float obstacleX[]      = new float[numberOfObstacles];
    private float obstacleY[]      = new float[numberOfObstacles];
    private float obstacleWidth[]  = new float[numberOfObstacles];
    private float obstacleHeight[] = new float[numberOfObstacles];
    private int furthestObstacle = numberOfObstacles - 1;
    // Cracker data
    private final int numberOfCrackers = 18;
    private final int crackersPerRow   = 3;
    private final int crackerYGap      = 3;
    private Texture crackerTexture;
    private Rectangle crackers[]   = new Rectangle[numberOfCrackers];
    private float crackerX[]       = new float[numberOfCrackers];
    private float crackerY[]       = new float[numberOfCrackers];
    private boolean crackerTaken[] = new boolean[numberOfCrackers];

    /*
        This function initializes all of the elements used in several functions in the game.
     */
	@Override
	public void create () {
		batch             = new SpriteBatch();
		shapeRenderer     = new ShapeRenderer();
		background        = new Texture("ocean02.png");
		upFish            = new Texture("clownfish_up.png");
		leftFish          = new Texture("clownfish_left.png");
		rightFish         = new Texture("clownfish_right.png");
		loseGame          = new Texture("lose_game.png");
		winGame           = new Texture("win_game.png");
		enemyUpTexture    = new Texture("shark_up.png");
		enemyLeftTexture  = new Texture("shark_left.png");
		enemyRightTexture = new Texture("shark_right.png");
		crackerTexture    = new Texture("coin.png");
        randomGenerator   = new Random();
        // Create cell dimensions
        cellWidth  = Gdx.graphics.getWidth()  / numberOfHorizontalCells;
        cellHeight = Gdx.graphics.getHeight() / numberOfVerticalCells;
        // Initialize all of the game text
        gameFont       = new BitmapFont();
        highScoreFont  = new BitmapFont();
        finishLineFont = new BitmapFont();
        int gameFontSize       = 2;
        int highScoreFontSize  = 1;
        int finishLineFontSize = 3;
        gameFont.setColor(Color.BLACK);
        highScoreFont.setColor(Color.BLACK);
        finishLineFont.setColor(Color.WHITE);
        gameFont.getData().scale(gameFontSize);
        highScoreFont.getData().scale(highScoreFontSize);
        finishLineFont.getData().scale(finishLineFontSize);
        // Initialize the beginning highscore and fish position
        highScore = 0;
        fishX = cellWidth  * startXLocation;
        fishY = cellHeight * startYLocation;
        // Create all of the buttons and add them to the stage
        stage = new Stage();
        stage.addActor(createArrowButton("left_arrow.png"));
        stage.addActor(createArrowButton("right_arrow.png"));
        Gdx.input.setInputProcessor(stage);
        // Create all of the objects in the game
        createFish();
        createEnemies();
        createCrackers();
        createObstacles();
	}

	/*
	    This function draws all of the elements in the game, mainly via other functions.
	 */
	@Override
	public void render () {
		drawBackground();
        drawCoins();
        drawFish();
        drawEnemies();
        drawObstacles();
        drawFinishLine();
        drawScore();
        // Draw the end screen
        if (eaten || movesLeft <= 0) {
            restartStage.draw();
            drawEndScreen();
        }
        stage.draw();
        if (frames % moveTime == 0) {
            moveUp();
        }
        frames++;

    }

    /*
        This function creates the arrow buttons. It takes in an image and sets that image as the
        button. It utilizes the first letter in the image name to determine the position and
        implementation of the button.
     */
    private ImageButton createArrowButton (final String image) {
        // Make the Texture whatever image has been passed
        final Texture texture                             = new Texture(image);
        final TextureRegion textureRegion                 = new TextureRegion(texture);
        final TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(textureRegion);
        final ImageButton button                          = new ImageButton(textureRegionDrawable);
        final float buttonImageSize = (float) 1.5;
        // Set the button size
        button.setWidth(Gdx.graphics.getWidth() / 4);
        button.setHeight(Gdx.graphics.getHeight() / 4);
        // Set the image size
        button.getImageCell().expandX().width(cellWidth * buttonImageSize);
        button.getImageCell().expandY().height(cellHeight * buttonImageSize);
        // Set the button's position depending on the button's image
        switch (image.charAt(0)) {
            case 'r':
                button.setPosition(Gdx.graphics.getWidth() - button.getWidth(), 0);
                break;
            case 'l':
                button.setPosition(0, 0);
                break;
        }
        // Make the button react when it's selected
        button.addListener(new ClickListener() {
            // When the button is touched, fire the correct function
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Move the character depending on the button, utilizing the image's name
                switch (image.charAt(0)) {
                    case 'r':
                        moveRight();
                        break;
                    case 'l':
                        moveLeft();
                        break;
                }
                return true;
            }
        });
        // Return the button's information to add it to the stage's actors
        return button;
    }

    /*
        This function creates the fish object used to collision detection.
     */
    private void createFish() { fishCell = new Rectangle(fishX, fishY, cellWidth, cellHeight); }

    /*
        This function creates the enemy objects used to collision detection. It also assigns
        textures to all of the enemies.
     */
    private void createEnemies() {
        for (int i = 0; i < numberOfEnemies; i++) {
            enemyStartX[i] = i * 2;
            enemyTextures[i] = enemyUpTexture;
        }
        enemies[0] = new Rectangle(enemyStartX[0], -cellHeight, cellWidth, cellHeight);
        enemies[1] = new Rectangle(cellWidth * enemyStartX[1], -cellHeight, cellWidth, cellHeight);
        enemies[2] = new Rectangle(cellWidth * enemyStartX[2], -cellHeight, cellWidth, cellHeight);
        enemies[3] = new Rectangle(cellWidth * enemyStartX[3], -cellHeight, cellWidth, cellHeight);
    }

    /*
        This function creates all of the obstacles. It assigns a random height, width, and gap
        between the previous obstacle for every obstacle. It then uses the created values to create
        an obstacle. It also separates the obstacles into rows with 3 cells between each row.
     */
    private void createObstacles() {
        float xGap;
        int currentRow = 0;

        // Create all the obstacles
        for (int i = 0; i < numberOfObstacles; i++) {
            // Make the gaps between the obstacles random between 0 and 4 cell-widths
            xGap = cellWidth * randomGenerator.nextInt(4);
            // Add the x gap to the end of the previous obstacle, if it's not the furthest left
            if (i % obstaclesPerRow > 0) {
                obstacleX[i] = obstacleX[i - 1] + obstacleWidth[i - 1] + xGap;
            } else {
                // Or else, add the between the border and the obstacle
                obstacleX[i] = xGap;
            }
            // If it's the first row, create the obstacles at the top of the screen
            if (currentRow == 0) {
                obstacleY[i] = cellHeight * (numberOfVerticalCells - 1);
            } else {
                // Or else, add the next row 3 cells up
                obstacleY[i] = cellHeight * (numberOfVerticalCells - 1) + currentRow * (cellHeight * obstacleYGap);
            }
            // Give the obstacles random widths and height between 1 and 2 cell widths or heights
            obstacleWidth[i] = cellWidth * (randomGenerator.nextInt(2) + 1);
            obstacleHeight[i] = cellHeight * (randomGenerator.nextInt(2) + 1);
            // Use the created values to create the obstacle
            obstacles[i] = new Rectangle(
                    obstacleX[i],
                    obstacleY[i],
                    obstacleWidth[i],
                    obstacleHeight[i]);
            // Make every 4 obstacles its own row
            if (i % obstaclesPerRow == obstaclesPerRow - 1) {
                currentRow++;
            }
        }
    }

    private void createCrackers() {
        float xGap;

        int currentRow = 0;
        // Create all the crackers
        for (int i = 0; i < numberOfCrackers; i++) {
            crackerTaken[i] = false;
            // Make the gaps between the crackers random between 1 and 4 cell-widths
            xGap = cellWidth * (randomGenerator.nextInt(4) + 1);
            // Add the x gap to the end of the previous cracker, if it's not the furthest left
            if (i % crackersPerRow > 0) {
                crackerX[i] = crackerX[i - 1] + xGap;
            } else {
                // Or else, add the between the border and the cracker
                crackerX[i] = xGap;
            }
            // If it's the first row, create the crackers at the top of the screen
            if (currentRow == 0) {
                crackerY[i] = cellHeight * (numberOfVerticalCells - 1);
            } else {
                // Or else, add the next row 3 cells up
                crackerY[i] = cellHeight * (numberOfVerticalCells - 1) + currentRow * (cellHeight * crackerYGap);
            }
            // Use the created values to create the cracker
            crackers[i] = new Rectangle(
                    crackerX[i],
                    crackerY[i],
                    cellWidth,
                    cellHeight);
            // Make every 2 crackers its own row
            if (i % crackersPerRow == crackersPerRow - 1) {
                currentRow++;
            }
        }
    }

    /*
        This function creates the restart button as an image of a fish.
     */
    private void createRestartButton() {
        restartStage             = new Stage();
        final float buttonHeight = (float) .75;
        Texture restartTexture;
        if (eaten) {
            restartTexture = loseGame;
        } else {
            restartTexture = winGame;
        }
        final TextureRegion restartTextureRegion = new TextureRegion(restartTexture);
        final TextureRegionDrawable restartTextureRegionDrawable =
                new TextureRegionDrawable(restartTextureRegion);
        final ImageButton restartButton = new ImageButton(restartTextureRegionDrawable);
        restartButton.setWidth(Gdx.graphics.getWidth());
        restartButton.setHeight(Gdx.graphics.getHeight() * buttonHeight);
        restartButton.getImageCell().expandX().fillX();
        restartButton.getImageCell().expandY().fillY();
        restartButton.setPosition(0, Gdx.graphics.getHeight() / 4);
        restartButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                restart();
                return true;
            }
        });
        restartStage.addActor(restartButton);
        Gdx.input.setInputProcessor(restartStage);
    }

    private void drawBackground() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

    private void drawScore() {
        batch.begin();
        gameFont.draw(
                batch,
                "Crackers: " + points,
                Gdx.graphics.getWidth() / 2 - cellWidth,
                Gdx.graphics.getHeight());

        highScoreFont.draw(
                batch,
                " Best Score: " + highScore,
                cellWidth * 3,
                cellHeight / 2);
        batch.end();
    }

    private void drawFinishLine() {
        final float finishLineHeight = cellHeight * 2;
        final float finishLineTextX  = cellWidth * 3;
        final float finishLineY      = cellHeight * (movesLeft + 6);
        final float finishTextY      = finishLineY + finishLineHeight / 2;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
                0,
                finishLineY,
                Gdx.graphics.getWidth(),
                finishLineHeight);
        shapeRenderer.end();
        batch.begin();
        String levelString = "Level ";
        finishLineFont.draw(
                batch,
                levelString + level,
                finishLineTextX,
                finishTextY);
        batch.end();
    }

    private void drawFish() {
        batch.begin();
        switch (fishDirection) {
            case 'l':
                batch.draw(leftFish, fishCell.x, fishCell.y, fishCell.width, fishCell.height);
                break;
            case 'r':
                batch.draw(rightFish, fishCell.x, fishCell.y, fishCell.width, fishCell.height);
                break;
            default:
                batch.draw(upFish, fishCell.x, fishCell.y, fishCell.width, fishCell.height);
                break;
        }
        batch.end();
    }

    /*
        This function draws the enemies. I also makes the background become more and more red as
        the enemies become closer to jumping. I had to cast it to a float so that it wouldn't do
        Integer math.
     */
    private void drawEnemies() {
        batch.begin();
        for (int i = 0; i < numberOfEnemies; i++) {
            batch.draw(enemyTextures[i],
                    enemies[i].x,
                    enemies[i].y,
                    enemies[i].width,
                    enemies[i].height);
        }
        batch.end();
    }

    private void drawObstacles() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        for (int i = 0; i < numberOfObstacles; i++) {
            shapeRenderer.rect(
                    obstacles[i].x,
                    obstacles[i].y,
                    obstacles[i].width,
                    obstacles[i].height);
        }
        shapeRenderer.end();
    }

    private void drawCoins() {
        batch.begin();
        for (int i = 0; i < numberOfCrackers; i++) {
            if (!crackerTaken[i]) {
                batch.draw(crackerTexture,
                        crackers[i].x,
                        crackers[i].y,
                        crackers[i].width,
                        crackers[i].height);
            }
        }
        batch.end();
    }

    private void drawEndScreen() {
        batch.begin();
        gameFont.draw(
                batch,
                "Tap the shark to restart",
                (float) (cellWidth * 1.5),
                Gdx.graphics.getHeight() / 4);
        batch.end();
    }

    /*
        This function moves the fish up. If the player tried to move off-screen or into
        an obstacle, the fish won't move. However, the enemies will still move (to add more
        difficulty). It also checks if the finish line was hit, if so, it creates the restart
        button.
     */
    private void moveUp() {
        // Check if the game hasn't already ended
        if (movesLeft > 0 && !eaten) {
            boolean hitObstacle = false;
            // Move the fish forward
            fishCell.setPosition(fishCell.x, fishCell.y + cellHeight);
            // Check if it hits any obstacles
            for (int i = 0; i < numberOfObstacles; i++) {
                // Test if the fish moved forward hits an obstacle
                if (Intersector.overlaps(fishCell, obstacles[i])) {
                    hitObstacle = true;
                }
            }
            // If the fish didn't hit anything, move the map
            if (!hitObstacle) {
                // Move the enemies with the map
                for (int i = 0; i < numberOfEnemies; i++) {
                    enemies[i].setPosition(enemies[i].x, enemies[i].y - cellHeight);
                }
                // Move the obstacles
                moveObstacles();
                for (int i = 0; i < numberOfCrackers; i++) {
                    if (Intersector.overlaps(crackers[i], fishCell) && !crackerTaken[i]) {
                        crackerTaken[i] = true;
                        points++;
                    }
                }
                // Move the crackers
                moveCrackers();
                fishDirection = 'u';
                // Change the game elements
                moves++;
                movesLeft--;
                // Check if the finish line was hit
                if (movesLeft <= 0) {
                    createRestartButton();
                }
            }
            // Move the fish back
            fishCell.setPosition(fishCell.x, fishCell.y - cellHeight);
            moveEnemies();
        }
    }

    /*
        This function moves the fish to the left. If the player tried to move off-screen or into
        an obstacle, the fish won't move. However, the enemies will still move (to add more
        difficulty).
     */
    private void moveLeft() {
        if (fishCell.x - cellWidth >= 0 && movesLeft > 0 && !eaten) {
            boolean hitObstacle = false;
            // Move the fish to the left
            fishCell.setPosition(fishCell.x - cellWidth, fishCell.y);
            for (int i = 0; i < numberOfObstacles; i++) {
                if (Intersector.overlaps(fishCell, obstacles[i])) {
                    hitObstacle = true;
                    Gdx.app.log("Left", "Y " + fishCell.y);
                    Gdx.app.log("Obstacle", "Y " + obstacles[i].y);
                    Gdx.app.log("Obstacle", "with height " + (obstacles[i].y + obstacles[i].height));
                }
            }
            if (hitObstacle) {
                fishCell.setPosition(fishCell.x + cellWidth, fishCell.y);
            } else {
                for (int i = 0; i < numberOfCrackers; i++) {
                    if (Intersector.overlaps(crackers[i], fishCell) && !crackerTaken[i]) {
                        crackerTaken[i] = true;
                        points++;
                    }
                }
                fishDirection = 'l';
            }
        } else if (fishCell.x - cellWidth < 0 && movesLeft > 0 && !eaten) {
            boolean hitObstacle = false;
            // Move the fish to the left
            fishCell.setPosition(cellWidth * (numberOfHorizontalCells - 1), fishCell.y);
            for (int i = 0; i < numberOfObstacles; i++) {
                if (Intersector.overlaps(fishCell, obstacles[i])) {
                    hitObstacle = true;
                    Gdx.app.log("Left", "Y " + fishCell.y);
                    Gdx.app.log("Obstacle", "Y " + obstacles[i].y);
                    Gdx.app.log("Obstacle", "with height " + (obstacles[i].y + obstacles[i].height));
                }
            }
            if (hitObstacle) {
                fishCell.setPosition(0, fishCell.y);
            } else {
                for (int i = 0; i < numberOfCrackers; i++) {
                    if (Intersector.overlaps(crackers[i], fishCell) && !crackerTaken[i]) {
                        crackerTaken[i] = true;
                        points++;
                    }
                }
                fishDirection = 'l';
            }
        }
    }

    /*
        This function moves the fish to the right. If the player tried to move off-screen or into
        an obstacle, the fish won't move. However, the enemies will still move (to add more
        difficulty).
     */
    private void moveRight() {
        if (fishCell.x + cellWidth < Gdx.graphics.getWidth() && movesLeft > 0 && !eaten) {
            boolean hitObstacle = false;
            // Move the fish to the right
            fishCell.setPosition(fishCell.x + cellWidth, fishCell.y);
            for (int i = 0; i < numberOfObstacles; i++) {
                if (Intersector.overlaps(fishCell, obstacles[i])) {
                    hitObstacle = true;
                }
            }
            if (hitObstacle) {
                fishCell.setPosition(fishCell.x - cellWidth, fishCell.y);
            } else {
                for (int i = 0; i < numberOfCrackers; i++) {
                    if (Intersector.overlaps(crackers[i], fishCell) && !crackerTaken[i]) {
                        crackerTaken[i] = true;
                        points++;
                    }
                }
                fishDirection = 'r';
            }
        } else if (fishCell.x + cellWidth >= Gdx.graphics.getWidth() && movesLeft > 0 && !eaten) {
            boolean hitObstacle = false;
            // Move the fish to the left
            fishCell.setPosition(0, fishCell.y);
            for (int i = 0; i < numberOfObstacles; i++) {
                if (Intersector.overlaps(fishCell, obstacles[i])) {
                    hitObstacle = true;
                }
            }
            if (hitObstacle) {
                fishCell.setPosition(cellWidth * (numberOfHorizontalCells - 1), fishCell.y);
            } else {
                for (int i = 0; i < numberOfCrackers; i++) {
                    if (Intersector.overlaps(crackers[i], fishCell) && !crackerTaken[i]) {
                        crackerTaken[i] = true;
                        points++;
                    }
                }
                fishDirection = 'r';
            }
        }
    }

    /*
        This function moves all of the obstacles down, giving the illusion that the player is
        moving. If a row of obstacles is more than 3 cells below the bottom of the screen, this
        function sets them back to the top, right above the highest row.
     */
    private void moveObstacles() {
        // Move all of the obstacles down 1 cell
        for (int i = 0; i < numberOfObstacles; i++) {
            obstacleY[i] -= cellHeight;
            obstacles[i].setPosition(obstacleX[i], obstacleY[i]);
            // Check if an obstacle is a row below the screen
            if (obstacleY[i] < 0 - cellWidth * obstacleYGap) {
                // Send it behind the row furthest to the back
                obstacleY[i] = obstacleY[furthestObstacle] + cellHeight * obstacleYGap;
                recreateObstacle(i);
                // Update the furthest obstacle row variable
                if (i % obstaclesPerRow == obstaclesPerRow - 1) {
                    furthestObstacle = i;
                }
            }
        }
    }

    private void moveCrackers() {
        // Move all of the crackers down 1 cell
        for (int i = 0; i < numberOfCrackers; i++) {
            crackerY[i] -= cellHeight;
            crackers[i].setPosition(crackerX[i], crackerY[i]);
            // Check if a cracker is a row below the screen
            if (crackerY[i] < 0 - cellHeight * crackerYGap) {
                // Send it behind the row furthest to the back
                crackerY[i] = obstacleY[furthestObstacle] + cellHeight * crackerYGap;
                recreateCracker(i);
            }
        }
    }

    /*
        This function loops through all of the enemies and executes the best movement for their
        position. It also lets them "jump", or move again so that it's more difficult for the
        player. It also checks if the enemy has eaten the player. Lastly, if an enemy has fallen
        too far behind, this function resets that enemy.
     */
    private void moveEnemies() {
        final float halfScreenWidth = Gdx.graphics.getWidth() / 2;
        // Move the enemies
        for (int i = 0; i < numberOfEnemies; i++) {
            if (enemies[i].y < fishCell.y) {
                enemyForward(i);
            } else if ((enemies[i].x > fishCell.x && enemies[i].x - fishCell.x <= halfScreenWidth) ||
                    fishCell.x - enemies[i].x > halfScreenWidth) {
                enemyLeft(i);
            } else if ((enemies[i].x < fishCell.x && fishCell.x - enemies[i].x <= halfScreenWidth) ||
                    enemies[i].x - fishCell.x > halfScreenWidth) {
                enemyRight(i);
            }
            // If the enemy has eaten the player, create the restart button
            if (Intersector.overlaps(enemies[i], fishCell)) {
                eaten = true;
                createRestartButton();
            }
            // If the enemy has fallen too far behind, recreate it
            if (enemies[i].y < -cellHeight) {
                resetEnemy(i);
            }
        }
        // Make the enemies move faster
        final int jumpTime = 5;
        if (moves % jumpTime == 0 && !jumped) {
            jumped = true;
            moveEnemies();
        }
        // Reset the jumped variable so they can jump more than once
        if (moves % jumpTime != 0 && jumped) {
            jumped = false;
        }
    }

    /*
        This function moves a specific enemy up. If it hit an obstacle, it moves back and tries
        another direction.
     */
    private void enemyForward(final int i) {
        enemyTextures[i] = enemyUpTexture;
        boolean hitObstacle = false;
        // Move it forward
        enemies[i].setPosition(enemies[i].x, enemies[i].y + cellHeight);
        // Check if it hit any obstacle
        for (int j = 0; j < numberOfObstacles; j++) {
            if (Intersector.overlaps(enemies[i], obstacles[j])) {
                hitObstacle = true;
            }
        }
        // If it did, move it back and try a different direction
        if (hitObstacle) {
            enemies[i].setPosition(enemies[i].x, enemies[i].y - cellHeight);
            if (enemies[i].x > fishCell.x) {
                enemyLeft(i);
            } else if (enemies[i].x < fishCell.x) {
                enemyRight(i);
            }
        }
    }

    /*
        This function moves a specific enemy to the left, so long as there's nothing in the way.
     */
    private void enemyLeft(final int i) {
        enemyTextures[i] = enemyLeftTexture;
        boolean hitObstacle = false;
        boolean wrapped = false;
        // Move it left
        if (enemies[i].x - cellWidth < 0) {
            enemies[i].setPosition(enemies[i].x + cellWidth * (numberOfHorizontalCells - 1), enemies[i].y);
            wrapped = true;
        } else {
            enemies[i].setPosition(enemies[i].x - cellWidth, enemies[i].y);
        }
        // Check if it hit an obstacle
        for (int j = 0; j < numberOfObstacles; j++) {
            if (Intersector.overlaps(enemies[i], obstacles[j])) {
                hitObstacle = true;
            }
        }
        // If it did, move it back
        if (hitObstacle) {
            if (wrapped) {
                enemies[i].setPosition(0, enemies[i].y);
            } else {
                enemies[i].setPosition(enemies[i].x + cellWidth, enemies[i].y);
            }
        }
    }

    /*
        This function moves a specific enemy to the right, so long as there's nothing in the way.
     */
    private void enemyRight(final int i) {
        enemyTextures[i] = enemyRightTexture;
        boolean hitObstacle = false;
        boolean wrapped = false;
        // Move the enemy right
        if (enemies[i].x + cellWidth > Gdx.graphics.getWidth()) {
            enemies[i].setPosition(0, enemies[i].y);
            wrapped = true;
        } else {
            enemies[i].setPosition(enemies[i].x + cellWidth, enemies[i].y);
        }
        // Check if it hit any obstacles
        for (int j = 0; j < numberOfObstacles; j++) {
            if (Intersector.overlaps(enemies[i], obstacles[j])) {
                hitObstacle = true;
            }
        }
        // If it did, move it back
        if (hitObstacle) {
            if (wrapped) {
                enemies[i].setPosition(cellWidth * (numberOfHorizontalCells - 1), enemies[i].y);
            } else {
                enemies[i].setPosition(enemies[i].x - cellWidth, enemies[i].y);
            }
        }
    }

    /*
        This function recreates an obstacle 3 cells above the topmost row. It reassigns the obstacle
        with a random width, height, and x gap between the previous obstacle.
     */
    private void recreateObstacle(final int i) {
        // Make a random x gap between 0 and 4 cell-widths
        float xGap = cellWidth * randomGenerator.nextInt(4);
        // Add the x gap to the end of the previous obstacle, if it's not the furthest left
        if (i % obstaclesPerRow > 0) {
            obstacleX[i] = obstacleX[i - 1] + obstacleWidth[i - 1] + xGap;
        } else {
            // Or else, add the between the border and the obstacle
            obstacleX[i] = xGap;
        }
        // Create a random height and width between 1 and 2 cell-widths and cell-heights
        obstacleWidth[i] = cellWidth * (randomGenerator.nextInt(2) + 1);
        obstacleHeight[i] = cellHeight * (randomGenerator.nextInt(2) + 1);
        // Create the obstacle with created values
        obstacles[i]= new Rectangle(
                obstacleX[i],
                obstacles[i].y,
                obstacleWidth[i],
                obstacleHeight[i]);
    }

    private void recreateCracker(final int i) {
        crackerTaken[i] = false;
        // Make a random x gap between 1 and 4 cell-widths
        float xGap = cellWidth * (randomGenerator.nextInt(4) + 1);
        // Add the x gap to the end of the previous obstacle, if it's not the furthest left
        if (i % crackersPerRow > 0) {
            crackerX[i] = crackerX[i - 1] + cellWidth + xGap;
        } else {
            // Or else, add the between the border and the obstacle
            crackerX[i] = xGap;
        }
        // Use the created values to create the cracker
        crackers[i] = new Rectangle(
                crackerX[i],
                crackerY[i],
                cellWidth,
                cellHeight);
    }

    /*
        This function resets the position of an enemy.
     */
    private void resetEnemy(final int i) {
        switch (i) {
            case 0:
                enemies[0].setPosition(enemyStartX[0], -cellHeight);
                break;
            case 1:
                enemies[1].setPosition(cellWidth * enemyStartX[1], -cellHeight);
                break;
            case 2:
                enemies[2].setPosition(cellWidth * enemyStartX[2], -cellHeight);
                break;
            case 3:
                enemies[3].setPosition(cellWidth * enemyStartX[3], -cellHeight);
                break;
        }
    }

    /*
        This function resets any variable that's changed and essential to gameplay.
     */
    private void restart() {
        if (eaten) {
            if (points > highScore) {
                highScore = points;
            }
            points   = 0;
            level    = 0;
            moveTime = 70;
        }
        // Reset the game data
        level++;
        movesLeft = goal;
        moves = 0;
        eaten = false;
        fishX = cellWidth * startXLocation;
        fishY = cellHeight * startYLocation;
        furthestObstacle = numberOfObstacles - 1;
        moveTime -= 10;
        if (moveTime <= 20) {
            moveTime += 5;
            if (moveTime <= 0) {
                moveTime = 5;
            }
        }
        // Create the objects in the game
        createFish();
        createEnemies();
        createObstacles();
        createCrackers();
        for (int i = 0; i < numberOfCrackers; i++) {
            crackerTaken[i] = false;
        }
        // Recreate the arrow buttons
        stage = new Stage();
        stage.addActor(createArrowButton("left_arrow.png"));
        stage.addActor(createArrowButton("right_arrow.png"));
        Gdx.input.setInputProcessor(stage);
    }

    /*
        This function cleans up the variables.
     */
	@Override
	public void dispose () {
	    // Dispose of all the Textures
        background.dispose();
        upFish.dispose();
		leftFish.dispose();
		rightFish.dispose();
        crackerTexture.dispose();
        loseGame.dispose();
        winGame.dispose();
		for (int i = 0; i < numberOfEnemies; i++) {
		    enemyTextures[i].dispose();
        }
        enemyLeftTexture.dispose();
		enemyRightTexture.dispose();
		enemyUpTexture.dispose();
		// Dispose of all the fonts
		finishLineFont.dispose();
		highScoreFont.dispose();
		gameFont.dispose();
		// Dispose of the rest
        batch.dispose();
        stage.dispose();
        shapeRenderer.dispose();
	}
}
