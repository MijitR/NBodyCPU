package com.mijitr.gpu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
/**
 *
 * @author rwhil
 */
public class NBody implements Comparable {
    
    private static final ThreadLocalRandom RAND;
    
    private static final float MIN_MASS, MAX_MASS, G, MIN_DIST,
            MAX_FORCE, MAX_VEL, MASS_SCALAR, DAMPENING_FACTOR, DIST_SCLR;
    
    static {
        RAND = ThreadLocalRandom.current();
        
        MASS_SCALAR = 3F;
        
        MIN_MASS = 1F;
        MAX_MASS = 10F;
        
        MIN_DIST = 3F;
        
        G = .02f;

        DAMPENING_FACTOR = 0.1f;

        DIST_SCLR = 1000f;
        
        MAX_FORCE = G * MAX_MASS*MAX_MASS/(MIN_DIST*MIN_DIST);
        
        MAX_VEL = 5F;
    }
    
    private final List<NBody> neighbors;
    
    private final float MASS;
    
    private final double[] velocity = new double[2],
            acceleration = new double[2];
    
    private final int index;
    
    private transient float col, row;
    
    private int inputSize;
    
    public NBody(final Dimension dims, final int index) {
        this(RAND.nextFloat()*dims.height,RAND.nextFloat()*dims.width,NPlane.NUM_PLAYERS_DEF,index);
    }
    
    private NBody(final float row, final float col, final int inputSize, final int index) {
        this.col=col;this.row=row;
        this.inputSize=inputSize;
        float rand;
        neighbors = new ArrayList<>(inputSize);
        MASS = (rand=RAND.nextFloat())*(MAX_MASS-MIN_MASS) + MIN_MASS;
        velocity[0] = 2f*((rand = RAND.nextFloat())-0.5F)*(MAX_VEL/10f);
        velocity[1] = 2f*((rand = RAND.nextFloat())-0.5F)*(MAX_VEL/10f);
        
        this.index = index;
    }
    
    public final NBody subsume(final Collection<NBody> struct) {
        neighbors.clear();
        if(struct.contains(this)) {
            System.out.println("Added self as neighbor!");
        }
        neighbors.addAll(struct);
        return this;
    }
    
    public final NBody operate() {
        neighbors.stream().map(
            n->new Force(n)
        ).reduce(
            (f1,f2)->f1.mask(f2)
        ).ifPresentOrElse(
            f -> this.update(f), 
            ()->System.out.println("We fucked up")
        );
        return this;
    }
    
   
    
    private final void update(final Force f) {
        acceleration[0] = f.width  / MASS;
        acceleration[1] = f.height / MASS;
        
        velocity[0] += acceleration[0];
        velocity[1] += acceleration[1];
        
        velocity[0] = limit(-MAX_VEL,(float)velocity[0],MAX_VEL);
        velocity[1] = limit(-MAX_VEL,(float)velocity[1],MAX_VEL);
        
        col += velocity[0];
        row += velocity[1];
        
        col = limit(0,col,NPlane.WIDTH);
        row = limit(0,row,NPlane.HEIGHT);
        
        if(col==0||col==NPlane.WIDTH) {
            velocity[0] = -velocity[0] * DAMPENING_FACTOR;
        }if(row==0||row==NPlane.HEIGHT) {
            velocity[1] = -velocity[1] * DAMPENING_FACTOR;
        }
        
        //System.out.println(index + " " + Arrays.toString(velocity));
    }
    
    private final double distance(final NBody n) {
        return Math.sqrt((n.row-row)*(n.row-row)+(n.col-col)*(n.col-col));
    }
    
    private final float limit(final float low, final float val, final float high) {
        return Math.max(low,Math.min(val,high));
    }
    
    private final double quickDist(final NBody n) {
        return (n.row-row)*(n.row-row)+(n.col-col)*(n.col-col);
    }
    
    public final Shape getCircle(final Graphics2D g2) {
        g2.setColor(new Color(RAND.nextInt(255),RAND.nextInt(255),RAND.nextInt(255)));
        return new Ellipse2D.Float(col-.5f*MASS_SCALAR*MASS, row-.5F*MASS_SCALAR*MASS, MASS_SCALAR*MASS,(MASS_SCALAR*MASS));
    }
    // v = at + vo
    // x = 1/2at^2 + vot + x0
    // Fg = G (M1*M2) / r ^ 2;
    // F = m * a;
    // a = Fg/m

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof NBody)) {
            return -1;
        } return Integer.compare(((NBody)o).index,index);
    }
    
    @Override
    public final boolean equals(final Object o) {
        if(!(o instanceof NBody)) {
            return false;
        } return ((NBody)o).index == index;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.index;
        return hash;
    }
 
    public class Force {
        public final double magnitude, theta, width, height;
        
        private Force(final double magnitude, final double theta) {
            this.magnitude = magnitude;
            this.theta = theta;
            
            width = this.magnitude*Math.cos(theta);
            height = this.magnitude*Math.sin(theta);
        }
        
        Force(final NBody neighbor) {
            this.magnitude = getForceMagnitude(neighbor);
            this.theta = getForceTheta(neighbor);
            
            width = this.magnitude*Math.cos(theta);
            height = this.magnitude*Math.sin(theta);
        }
        
        public final Force mask(final Force tm) {
            /*return new Force(
                    this.width*tm.width+this.height*tm.height,
                    
                    Math.acos(
                            (this.width*tm.width+this.height*tm.height)
                        / (this.magnitude * tm.magnitude)
                    )
            );*/
            final double newW = this.width+tm.width;
            
            final double newH = this.height+tm.height;
            
            return new Force(Math.sqrt(newW*newW+newH*newH), Math.atan2(newH,newW));
        }
        
        private double getForceMagnitude(final NBody n) {
            return G * NBody.this.MASS*n.MASS / (NBody.this.quickDist(n)/(DIST_SCLR/inputSize) + MIN_DIST)
                   / (1+Math.exp(-NBody.this.distance(n) + MIN_DIST));
        }
        
        //To keep things straight (+ right and + down)
        private double getForceTheta(final NBody n) {
            return Math.atan2((n.row-row),(n.col-col));
        }
    }
    
}