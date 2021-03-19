package com.singaporetech.eod;

import com.badlogic.gdx.Gdx;

import java.util.List;

/**
 * NOTE THAT THIS IS LEGACY CODE THAT HAS NO PROPER COMMENTS
 */
public class Game extends com.badlogic.gdx.Game {
    private static final String TAG = "Game";

    private static Game instance = new Game();
    private Game() {}
    public static Game i(){
        return instance;
    }

    private com.singaporetech.eod.PlayScreen playScreen;
    private GameState gameState = GameState.i();

	@Override
	public void create () {
        // init engines
        RenderEngine.i().init();
        com.singaporetech.eod.CollisionEngine.i().init();

        playScreen = new PlayScreen();
        setScreen(playScreen);

        restart();
    }

    public List<GameObject> getGameObjects() {
        return playScreen.getGameObjects();
    }

    public void restart() {
        RenderEngine.i().shutdownDebugRenderer();
        RenderEngine.i().clearRenderables();
        com.singaporetech.eod.CollisionEngine.i().clearCollidables();

        //TODO shift this elsewhere to allow reset when gameplay resets
        //GameState.i().reset();

        Game.i().resume();
        playScreen.restart();
        RenderEngine.i().initDebugRenderer();
        RenderEngine.i().hideEndGameMenu();
    }

    @Override
    public void render() {
        super.render();
    }

	@Override
	public void dispose () {
        Gdx.app.log(TAG, "in Game.dispose()");
        playScreen.dispose();

        CollisionEngine.i().finalize();
        RenderEngine.i().finalize();
	}

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }
}
