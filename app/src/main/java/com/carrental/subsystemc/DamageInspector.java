package com.carrental.subsystemc;

public class DamageInspector {

    public enum DamageLevel {
        NONE,
        MINOR,
        SERIOUS
    }

    public boolean canHandOver(DamageLevel damageLevel) {
        if (damageLevel == null) {
            throw new IllegalArgumentException(
                    "damage level is required"
            );
        }

        return damageLevel != DamageLevel.SERIOUS;
    }
}
