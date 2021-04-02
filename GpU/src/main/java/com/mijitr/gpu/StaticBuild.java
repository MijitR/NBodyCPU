package com.mijitr.gpu;

/**
 *
 * @author rwhil
 */
public enum StaticBuild {
    RAND_ONE_LAYER (1, new int[]{25}), RAND_TWO_LAYER(2, new int[]{25,25});
    private final int numLayers;
    private final int[] layerSizes;
    private StaticBuild(final int layerCount, final int[] sizes) {
        this.numLayers = layerCount;
        layerSizes = new int[this.numLayers];
        System.arraycopy(sizes,0,layerSizes,0,sizes.length);
    }
    
    public int numLayers() {
        return this.numLayers;
    }
    
    public int queryLSize(final int layer) {
        return layerSizes[layer];
    }
    
    public enum Activation {
        SIGMOID (true, 2.5d), TANH (true, 1d), RELU(false, 1d/7d),
        SOFTPLUS(false, 1.618d), ELU (true, 1d);//, IDENTITY(false, 1d/2d);
        
        private final boolean isResultBased;
        private final double etaMult;
        
        Activation(final boolean isResultBased, final double etaMult) {
            this.isResultBased = isResultBased;
            this.etaMult = etaMult;
        }
        
        public boolean isResultBased() {
            return isResultBased;
        }
        public double etaMult() {
            return etaMult;
        }
    }
}


