package com.singaporetech.eod.components.render;

import com.singaporetech.eod.SETTINGS;

/**
 * Created by mrchek on 6/2/17.
 */

public class SpriteSheetBug extends SpriteSheet {

    public SpriteSheetBug(String spritePath) {
        super("SpriteSheetBug", spritePath, SETTINGS.SPRITE_WIDTH, SETTINGS.SPRITE_HEIGHT);
    }

    /**
     * Graphics: 2D Graphics
     * 2. Setting keyframes. (see actual cockroach.png/.txt and TexturePacker app)
     * @param seq The keyframes.
     */
    @Override
    public void setSequence(Sequence seq) {
        this.sequence = seq;
        switch(seq) {
            case RUN:
                startFrame = 0;
                endFrame = 3;
                break;

            case MELEE:
                startFrame = 4;
                endFrame =8;
                break;

            case DESTRUCT:
                startFrame = 9;
                endFrame =10;
                break;
        }
        currSpriteIndex = startFrame;
    }
}
