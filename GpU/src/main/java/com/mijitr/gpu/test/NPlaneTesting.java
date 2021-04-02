package com.mijitr.gpu.test;

import com.mijitr.gpu.NPlane;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author rwhil
 */
public class NPlaneTesting {
    public static final void main(final String[] args) throws InterruptedException, InvocationTargetException {
        NPlane plane = new NPlane(NPlane.NUM_PLAYERS_DEF);
        //plane.fly();
        plane.flyFree();
    }
}
