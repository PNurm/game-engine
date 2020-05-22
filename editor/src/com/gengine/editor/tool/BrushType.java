package com.gengine.editor.tool;

public enum BrushType {
    SQUARE {
        @Override
        public float weight(float size, float x, float z) {
            return Math.abs(x) <= size && Math.abs(z) <= size ? 1 : 0;
        }
    },
    CIRCLE {
        @Override
        public float weight(float size, float x, float z) {
            return (x * x + z * z) < size * size ? 1f : 0f;
        }
    },
    SMOOTHC {
        @Override
        public float weight(float size, float x, float z) {
            return Math.max(0, 1 - (float) Math.pow((x * x + z * z) / (size * size), 4));
        }
    };
    public abstract float weight(float size, float x, float z);
}
