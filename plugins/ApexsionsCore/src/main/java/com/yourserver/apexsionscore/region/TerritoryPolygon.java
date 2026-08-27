package com.yourserver.apexsionscore.region;

import java.util.Collections;
import java.util.List;

/**
 * Represents a 2D/3D polygon bounding territory parsed from BlueMap shape definitions.
 */
public class TerritoryPolygon {

    public static class Point2D {
        public final double x;
        public final double z;

        public Point2D(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }

    private final List<Point2D> vertices;
    private final double minY;
    private final double maxY;
    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;

    public TerritoryPolygon(List<Point2D> vertices, double minY, double maxY) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.minY = minY;
        this.maxY = maxY;

        double tempMinX = Double.MAX_VALUE;
        double tempMaxX = -Double.MAX_VALUE;
        double tempMinZ = Double.MAX_VALUE;
        double tempMaxZ = -Double.MAX_VALUE;

        for (Point2D p : vertices) {
            if (p.x < tempMinX) tempMinX = p.x;
            if (p.x > tempMaxX) tempMaxX = p.x;
            if (p.z < tempMinZ) tempMinZ = p.z;
            if (p.z > tempMaxZ) tempMaxZ = p.z;
        }

        this.minX = tempMinX;
        this.maxX = tempMaxX;
        this.minZ = tempMinZ;
        this.maxZ = tempMaxZ;
    }

    /**
     * Determines whether the given coordinates fall within this 3D polygon.
     */
    public boolean contains(double x, double y, double z) {
        // Fast Y-bounds check
        if (y < minY || y > maxY) {
            return false;
        }

        // Fast bounding-box check
        if (x < minX || x > maxX || z < minZ || z > maxZ) {
            return false;
        }

        // Ray-casting algorithm for 2D polygon inclusion
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point2D pi = vertices.get(i);
            Point2D pj = vertices.get(j);

            if (((pi.z > z) != (pj.z > z)) && (x < (pj.x - pi.x) * (z - pi.z) / (pj.z - pi.z) + pi.x)) {
                inside = !inside;
            }
        }

        return inside;
    }

    public List<Point2D> getVertices() {
        return vertices;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }
}
