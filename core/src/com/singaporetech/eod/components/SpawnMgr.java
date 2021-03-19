package com.singaporetech.eod.components;

import com.singaporetech.eod.GameObject;
import com.singaporetech.eod.GameState;
import com.singaporetech.eod.SETTINGS;
import com.singaporetech.eod.components.ai.FsmBug;
import com.singaporetech.eod.components.ai.SteeringPursue;
import com.singaporetech.eod.components.collision.Collider;
import com.singaporetech.eod.components.render.PrimitiveHealth;
import com.singaporetech.eod.components.render.SpriteBam;
import com.singaporetech.eod.components.render.SpritePlusOne;
import com.singaporetech.eod.components.render.SpriteSheetBug;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mrboliao on 6/2/17.
 * todo: different spawn types
 */

public class SpawnMgr extends Component {
    private static final String TAG = "SpawnMgr:C";

    private int spawnTimes = 0;
    private int numToSpawn = 1;

    private List<GameObject> gameObjects = new LinkedList<GameObject>();
    private GameObject player;
    private int spawnPosX = SETTINGS.BUG_POS_X;

    public SpawnMgr(GameObject player) {
        super("SpawnMgr");

        this.player = player;
    }

    @Override
    public void init(GameObject owner) {
        super.init(owner);

        spawn();
    }

    public void spawn() {
        GameObject bug;

        for (int i=0; i<numToSpawn; ++i) {
            bug = new GameObject("bug"+i);
            gameObjects.add(bug);
            bug.addComponent(new Transform(spawnPosX, SETTINGS.BUG_POS_Y, 50));
            bug.addComponent(new SpriteSheetBug("sprites/cockroach.txt"));
            bug.addComponent(new Movement(SETTINGS.SPEED_BUG));
            bug.addComponent(new Collider(false, false));
            bug.addComponent(new SteeringPursue(player));
            bug.addComponent(new Combat(player, SETTINGS.BUG_DMG));
            bug.addComponent(new Health());
            bug.addComponent(new SpritePlusOne("sprites/plus1.png")); //todo: decouple this from primitive health
            bug.addComponent(new PrimitiveHealth());
            bug.addComponent(new FsmBug());
            bug.addComponent(new SpriteBam("sprites/bam.png"));
            bug.init();

            // move spawn pos by a little each time
            spawnPosX += SETTINGS.BUG_POS_JITTER_X;
            if (spawnPosX > SETTINGS.VIEWPORT_WIDTH) {
                spawnPosX = 0;
            }
        }

        // increase the level by adding bugs
        // todo: this is totally temporary, so make a proper leveling system
        ++spawnTimes;
        if (spawnTimes == 2) {
            ++numToSpawn;
        }
        else if (spawnTimes == 6) {
            ++numToSpawn;
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // spawn when night arrives
        if (GameState.i().isCanSpawn()) {
            spawn();
        }

        // garbage collection
        // - delete one at a time
        for (GameObject go: gameObjects) {
            if (go.isDestroyed()) {
                go.finalize();
                gameObjects.remove(go);
                break;
            }
        }

        // process game object updates
        for (GameObject go: gameObjects) {
            go.update(dt);
        }
    }

    @Override
    public void finalize() {
        super.finalize();

        for (GameObject go: gameObjects) {
            go.finalize();
        }
        gameObjects.clear();
    }
}
