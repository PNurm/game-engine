package com.gengine.core.model.terrain;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;
import com.gengine.core.world.node.TerrainNode;

public class TerrainNodeAttribute extends Attribute {

    public static final String ATTRIBUTE_REGION_ALIAS = "region0";
    public static final long ATTRIBUTE_REGION = register(ATTRIBUTE_REGION_ALIAS);

    public TerrainNode region;

    protected static long Mask = ATTRIBUTE_REGION;

    /**
     * Method to check whether the specified type is a valid DoubleAttribute
     * type
     */
    public static Boolean is(final long type) {
        return (type & Mask) != 0;
    }

    public TerrainNodeAttribute(long type, TerrainNode region) {
        super(type);
        if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
        this.region = region;
    }

    public TerrainNodeAttribute(TerrainNodeAttribute other) {
        super(other.type);
        if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
        this.region = other.region;
    }

    @Override
    public Attribute copy() {
        return new TerrainNodeAttribute(this);
    }

    @Override
    public int hashCode() {
        final int prime = 7;
        final long v = NumberUtils.doubleToLongBits(region.hashCode());
        return prime * super.hashCode() + (int) (v ^ (v >>> 32));
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type) return type < o.type ? -1 : 1;
        TerrainNode otherValue = ((TerrainNodeAttribute) o).region;
        return region.equals(otherValue) ? 0 : -1;
    }

}
