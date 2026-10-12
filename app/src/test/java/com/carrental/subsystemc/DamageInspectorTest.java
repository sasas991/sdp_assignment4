package com.carrental.subsystemc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DamageInspectorTest {

    private final DamageInspector inspector = new DamageInspector();

    @Test
    void allowsHandoverWhenThereIsNoDamage() {
        assertTrue(inspector.canHandOver(DamageInspector.DamageLevel.NONE));
    }

    @Test
    void allowsHandoverWhenDamageIsMinor() {
        assertTrue(inspector.canHandOver(DamageInspector.DamageLevel.MINOR));
    }

    @Test
    void rejectsHandoverWhenDamageIsSerious() {
        assertFalse(inspector.canHandOver(DamageInspector.DamageLevel.SERIOUS));
    }
}
