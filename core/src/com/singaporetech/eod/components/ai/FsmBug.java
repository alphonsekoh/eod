package com.singaporetech.eod.components.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.singaporetech.eod.CollisionEngine;
import com.singaporetech.eod.GameObject;
import com.singaporetech.eod.components.render.SpriteSheet;

/**
 * Created by mrboliao on 23/1/17.
 */

public class FsmBug extends Fsm {
    private static final String TAG = "FsmBug:C";

    public FsmBug() {
        super("FsmBug");
    }

    @Override
    public void init(GameObject owner) {
        super.init(owner);

        // setup additional links
        spriteSheet = (SpriteSheet) owner.getComponent("SpriteSheetBug");
        steering = (Steering) owner.getComponent("SteeringPursue");
    }

    /**
     * AI: decision making
     * 1. transition switch cases.
     * @param dt
     */
    @Override
    public void update(float dt) {
        super.update(dt);

        Vector2 avoidTarget;

        switch (currState) {
            case IDLE:
                if (combat.hasTarget()) {
                    transit(StateType.PURSUE);
                }
                break;

            case PURSUE:
                // tothink: this is an alarm condition, need HFSM to resolve...
                if (health.isEmpty()) {
                    transit(StateType.DESTRUCT);
                }
                else {
                    // todo: conditions to be a class for reuse
                    // this will check whether near obstacle using steering collision technique
                    avoidTarget = CollisionEngine.i().getCollisionAvoidTarget(collider);
                    if (avoidTarget != null) {
                        Gdx.app.log(TAG, "COLLISION DETECTED");
                        transit(StateType.COLLISION_RESPONSE);
                    } else if (steering.reachedDestPos()) {
                        Gdx.app.log(TAG, "REACHED TARGET");
                        transit(StateType.ATTACK);
                    }
                }
                break;

            case COLLISION_RESPONSE:
                if (health.isEmpty()) {
                    transit(StateType.DESTRUCT);
                }
                else {
                    avoidTarget = CollisionEngine.i().getCollisionAvoidTarget(collider);
                    if (avoidTarget == null) {
                        Gdx.app.log(TAG, "NO MORE COLLISIONS");
                        transit(StateType.PURSUE);
                    } else if (steering.reachedDestPos()) {
                        Gdx.app.log(TAG, "REACHED COLLISION AVOID TARGET");
                        transit(StateType.PURSUE);
                    }
                }
                break;

            case ATTACK:
                if (health.isEmpty()) {
                    transit(StateType.DESTRUCT);
                }
                else {
                    if (((SteeringPursue) steering).targetGotAway()) {
                        Gdx.app.log(TAG, "ATTACK TARGET GOT AWAY");
                        transit(StateType.PURSUE);
                    }
                }
                break;

            case BUILD:
                break;

            case DESTRUCT:
                //Gdx.app.log(TAG, "Destroying " + owner.getName());
                break;

            default:
                break;
        }
    }
}
