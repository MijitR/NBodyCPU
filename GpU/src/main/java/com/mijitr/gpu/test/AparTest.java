package com.mijitr.gpu.test;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;
import java.util.Arrays;

/**
 *
 * @author rwhil
 */
public class AparTest {
    
    public static final void main(final String[] args) {
        
        final int LENGTH = 1024*28;
        
        final float[] a = new float[LENGTH],
                b = new float[LENGTH],
                c = new float[LENGTH];
        
        //final int[] done = new int[]{0};
        
        Arrays.fill(a, 1);
        for(int i = 0; i < b.length; i ++) {
            b[i] = 2;
        }
        
        final Kernel kernel = new Kernel() {
            @Override
            public final void run() {
                
                final int id = this.getGlobalId();
                c[id] = a[id] + b[id] + c[id];
        //        done[0] = c[id] >= 9000 ? 1 : 0;
                
            }
        };
        
        kernel.setExplicit(true);
        
        //kernel.put(done);
        kernel.put(a);
        kernel.put(b);
        kernel.put(c);
        
        kernel.execute(Range.create(1));
        
        c[0] = 0;
        
        System.out.println("Executing on: " );
        System.out.println(kernel.getTargetDevice());
        
        final long startTime = System.currentTimeMillis();
        final Range range = Range.create(LENGTH);
        kernel.getKernelState().setRange(range);
        
        
        //while(done[0] == 0) {
            kernel.execute(range);
          //  kernel.get(done);
        //}
        kernel.get(c);
        
        System.out.println((System.currentTimeMillis()-startTime) + " millis");
        
        //for(final float ci : c) {
        //    System.out.print(String.format("%.1f, ",ci));
        //}
        
        System.out.println();
        Arrays.fill(c, 0f);
        final long startTime2 = System.currentTimeMillis();
        int i = 0;
        do {
            for(int id = 0; id < LENGTH; id ++) {
                c[id] = a[id] + b[id] + c[id];
          //      done[0] = c[id] >= 1024 ? 1 : 0;
            }
            i++;
        } while(i < 1);
        System.out.println((System.currentTimeMillis()-startTime2) + " millis cpu");
        
        
        
    }
    
}
