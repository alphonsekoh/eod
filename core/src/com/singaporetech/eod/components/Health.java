package com.singaporetech.eod.components;

import com.singaporetech.eod.GameObject;
import com.singaporetech.eod.SETTINGS;
import com.singaporetech.eod.components.render.PrimitiveHealth;
import com.singaporetech.eod.components.render.SpritePlusOne;

/**
 * Created by mrboliao on 2/2/17.
 * comments
 */

public class Health extends Component {
    private static final String TAG = "Health:C";

    protected com.singaporetech.eod.components.Transform transform;
    protected com.singaporetech.eod.components.render.PrimitiveHealth primitiveHealth;
    protected com.singaporetech.eod.components.render.SpritePlusOne spritePlusOne;

    protected float maxHp = SETTINGS.PLAYER_HP;
    protected float hp = maxHp;
    protected float gcTime = SETTINGS.GC_DURATION; // when this expires, garbage collected

    public Health(float startHpScale) {
        super("Health");

        this.hp = startHpScale * maxHp;
    }

    public Health() {
        this(1);
    }

    @Override
    public void init(GameObject owner) {
        super.init(owner);

        transform = (Transform) owner.getComponent("Transform");
        primitiveHealth = (PrimitiveHealth) owner.getComponent("PrimitiveHealth");
        spritePlusOne = (SpritePlusOne) owner.getComponent("SpritePlusOne");

        // init the width of the visuals
        primitiveHealth.scaleWidth(hp/maxHp);
        spritePlusOne.setAlpha(0);
    }

    public void hit(float dmg) {
        hp -= dmg;
        if (hp < 0) {
            hp = 0;
        }
        primitiveHealth.scaleWidth(hp/maxHp);
    }

    public void heal(float amt) {
        hp += amt;
        if (hp > maxHp) {
            hp = maxHp;
        }
        primitiveHealth.scaleWidth(hp/maxHp);

        spritePlusOne.reset();
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // flag to be destroyed after timer expires
        if (isEmpty()) {
            gcTime -= dt;
            if (gcTime <= 0) {
                owner.setDestroyed();
            }
        }

        // do fade out animation for sprite
        if (spritePlusOne.getAlpha() > 0) {
            spritePlusOne.shrinkAndFade(SETTINGS.PLUSONE_FADEOUT_DECREMENT, dt);
            spritePlusOne.setPos(primitiveHealth.getRightEdgePos());
        }
    }

    public boolean isEmpty() {
        return (hp == 0);
    }
}
