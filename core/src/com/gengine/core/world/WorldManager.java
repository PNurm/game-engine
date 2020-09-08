package com.gengine.core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gengine.core.model.terrain.TerrainTile;
import com.gengine.core.world.node.CellNode;
import com.gengine.core.world.node.TerrainNode;
import com.gengine.util.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

import static com.gengine.core.world.WorldCell.TILE_SIZE;


public class WorldManager {

    public HashMap<Integer, WorldCell> loadedCells = new HashMap<>();

    private WorldCell currentCell;

    final int regionSpan = 1;

    public void updateCellView(float x, float z) {

        float worldX = (x / TILE_SIZE);
        float worldZ = (z / TILE_SIZE);

        int newSectorX = (int) (worldX / WorldCell.SIZE);
        int newSectorY = (int) (worldZ / WorldCell.SIZE);

        //Avoid calculations if region hasn't changed.
        if(currentCell != null &&
                newSectorX == currentCell.getX() &&
                newSectorY == currentCell.getY()) {
            return;
        }

        currentCell = getCell(newSectorX, newSectorY);

        int minX = newSectorX - regionSpan;
        int minY = newSectorY - regionSpan;

        int maxX = newSectorX + regionSpan;
        int maxY = newSectorY + regionSpan;

        for(WorldCell cell : loadedCells.values()) {
            if(cell.getX() < minX || cell.getX() > maxX || cell.getY() < minY || cell.getY() > maxY) {
                disposeCell(cell);
            }
        }

        for(int cellY = minY; cellY < maxY; cellY++) {
            for(int cellX = minX; cellX < maxX; cellX++) {

                int cellHash = cellX * 31 + cellY;
                if (loadedCells.containsKey(cellHash)) {
                    continue;
                }

                WorldCell worldCell = getCell(cellX, cellY);
                loadedCells.put(cellHash, worldCell);
                System.out.println("Loaded " + worldCell);
            }
        }
    }

    private void disposeCell(WorldCell cell) {
        loadedCells.remove(cell.hashCode());
        cell.dispose();
        System.out.println("Disposing " + cell);
    }


    public Vector3 pickPoint(Vector3 out, Ray ray) {
        if (ray != null) {
            Vector3 v = getRayIntersection(out, ray);
            return v;
        }
        return out;
    }

    private final Vector3 startV = new Vector3();
    private final Vector3 endV = new Vector3();

    private boolean intersectionInRange(float start, float finish, Ray ray) {
        Vector3 startPoint = ray.getEndPoint(startV, start);//getPointOnRay(ray, start);
        Vector3 endPoint = ray.getEndPoint(endV, finish);//getPointOnRay(ray, finish);

        if (!isUnderground(startPoint) && isUnderground(endPoint)) {
            return true;
        } else {
            return false;
        }
    }

    private static final int MAX_RECURSION_COUNT = 100;
    private static final float RAY_RANGE = 600;

    private Vector3 binarySearch(Vector3 out, int count, float start, float finish, Ray ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= MAX_RECURSION_COUNT) {
            Vector3 endPoint = ray.getEndPoint(out, half);
            //check if end point hits a terrain at all
            return endPoint;
        }
        if (intersectionInRange(start, half, ray)) {
            return binarySearch(out, count + 1, start, half, ray);
        } else {
            return binarySearch(out, count + 1, half, finish, ray);
        }
    }

    public Vector3 getRayIntersection(Vector3 out, Ray ray) {

        if (intersectionInRange(0, RAY_RANGE, ray)) {
            out = binarySearch(out, 0, 0, RAY_RANGE, ray);
        }

        return out;
    }

    public boolean isUnderground(Vector3 point) {
        float terrainHeight = WorldManager.worldHeight(point.x, point.z);
        return point.y < terrainHeight;
    }

    public static float lerp2_smart(float h00, float h01, float h10, float h11, float tx, float ty) {
        // Is only one NaN?
        if (!Float.isFinite(h11) && Float.isFinite(h00) && Float.isFinite(h01) && Float.isFinite(h10))
            h11 = h10 + h01 + h00;
        else if (!Float.isFinite(h10) && Float.isFinite(h00) && Float.isFinite(h01) && Float.isFinite(h11))
            h10 = h00 + h11 - h01;
        else if (!Float.isFinite(h01) && Float.isFinite(h00) && Float.isFinite(h10) && Float.isFinite(h11))
            h01 = h00 + h11 - h10;
        else if (!Float.isFinite(h00) && Float.isFinite(h10) && Float.isFinite(h01) && Float.isFinite(h11))
            h00 = h10 + h01 - h11;
        return lerp2(h00, h01, h10, h11, tx, ty);
    }

    public static float lerp2(float h00, float h01, float h10, float h11, float tx, float ty) {
        // Is only one NaN?
        if (!Float.isFinite(h00)) {
            if (Float.isFinite(h10))
                h00 = h10;
            else if (Float.isFinite(h01))
                h00 = h10 = h01;
            else if (Float.isFinite(h11))
                h00 = h10 = h01 = h11;
            else
                return Float.NaN; // Bad	!
        }
        if (!Float.isFinite(h10))
            h10 = h00;
        if (!Float.isFinite(h01))
            h01 = h00;
        if (!Float.isFinite(h11))
            h11 = h00;

        float ha = h00 + (h10 - h00) * tx;
        float hb = h01 + (h11 - h01) * tx;
        return ha + (hb - ha) * ty;
    }

    public static float worldHeight(float wx, float wy) {
        TerrainTile t00 = worldTile(wx, wy);
        float h00 = t00 != null ? t00.getHeight() : Float.NaN;
        TerrainTile t01 = worldTile(wx + TILE_SIZE, wy);
        float h01 = t01 != null ? t01.getHeight() : Float.NaN;
        TerrainTile t10 = worldTile(wx, wy + TILE_SIZE);
        float h10 = t10 != null ? t10.getHeight() : Float.NaN;
        TerrainTile t11 = worldTile(wx + TILE_SIZE, wy + TILE_SIZE);
        float h11 = t11 != null ? t11.getHeight() : Float.NaN;

        wx /= TILE_SIZE;
        wy /= TILE_SIZE;

        float tx = (float) (wx - Math.floor(wx));
        float ty = (float) (wy - Math.floor(wy));

        float h = lerp2_smart(h00, h01, h10, h11, tx, ty);
        if (!Float.isFinite(h))
            h = 0;
        return h;
    }

    public static TerrainTile worldTile(float worldX, float worldY) {
        //System.out.println(worldX + ", " + worldY);
        worldX /= TILE_SIZE;
        worldY /= TILE_SIZE;

        int cellX = (int) Math.floor(worldX / WorldCell.SIZE);
        int cellY = (int) Math.floor(worldY / WorldCell.SIZE);

        int tX = (int) Math.floor(worldX - (cellX * WorldCell.SIZE));
        int tY = (int) Math.floor(worldY - (cellY * WorldCell.SIZE));

        WorldCell cell = getCell(cellX, cellY);
        if (cell == null) {
            return null;
        }
        TerrainNode node = cell.getTerrain();
        if (node == null) {
            return null;
        }

        return node.getTile(tX, tY);
    }

    private static HashMap<Long, WorldCell> cellCache = new HashMap<>();

    public static WorldCell getCell(int cellX, int cellY) {
        long hash = cellX * 31 + cellY;
        if (cellCache.containsKey(hash)) {
           // System.out.println("Return " + cellX + " " + cellY);
            return cellCache.get(hash);
        }
        WorldCell worldCell = new WorldCell(cellX,cellY);
        cellCache.put(hash, worldCell);
        System.out.println("Created new " + worldCell);

        TerrainNode terrainNode = new TerrainNode(worldCell);
        worldCell.root.addChild(terrainNode);
        System.out.println("Created terrain node for " + worldCell);

        return worldCell;
    }



    public static WorldCell load(int cellX, int cellY) {
        String filename = String.format("x%+04dy%+04d", cellX, cellY);
        FileHandle handle = Gdx.files.local(filename);

        WorldCell region = new WorldCell(cellX, cellY);
        if (handle.file().exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(handle.file());
                ByteBuffer data = ByteBuffer.wrap(IOUtils.readStream(is));
                region.unpack(data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return region;
    }

    public WorldCell getCurrentCell() {
        return currentCell;
    }

    public WorldCell getCellFromWorld(float x, float z) {
        x /= TILE_SIZE;
        z /= TILE_SIZE;
        int cellX = (int) Math.floor(x / WorldCell.SIZE);
        int cellY = (int) Math.floor(z / WorldCell.SIZE);

        WorldCell cell = getCell(cellX, cellY);
        if (cell == null) {
            return null;
        }
        return cell;
    }
}
