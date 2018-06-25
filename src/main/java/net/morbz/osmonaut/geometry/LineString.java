package net.morbz.osmonaut.geometry;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Merten Peetz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import net.morbz.osmonaut.osm.LatLon;
import net.morbz.osmonaut.osm.Node;
import net.morbz.osmonaut.osm.Way;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a LineString (i.e. a non-closed way in geometry-speak)
 */
public class LineString implements IGeometry {

    protected List<LatLon> coords = new ArrayList<LatLon>();
    protected Bounds bounds = new Bounds();

    /**
     * Creates a LineString that contains all coordinates of the given list.
     *
     * @param coords
     *            The list of coordinates
     */
    public LineString(List<LatLon> coords) {
        // Check size
        if (coords.size() == 0) {
            return;
        }

        // Add coords
        for (LatLon latlon : coords) {
            add(latlon);
        }
    }

    /**
     * Creates a LineString that contains the coordinates of the nodes of the given
     * way.
     *
     * @param way
     *            The way
     */
    public LineString(Way way) {
        // Check size
        if (way.getNodes().size() == 0) {
            return;
        }

        // Add way nodes
        for (Node node : way.getNodes()) {
            add(node.getLatlon());
        }
    }

    /**
     * Adds a coordinate to this polygon and extend bounds.
     */
    protected void add(LatLon latlon) {
        this.coords.add(latlon);
        this.bounds.extend(latlon);
    }

    public boolean isClosed() {
        if (coords == null || coords.size() <= 2) {
            return false;
        }
        return coords.get(0).equals(coords.get(coords.size() - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLon getCenter() {
        // weight each segment by its length
        int n;
        if((n = coords.size()) >= 2) {
            int i, j;
            LatLon pi,pj;
            double li,xi,yi,xj,yj,dx,dy;
            double ltmp = 0, xtmp = 0, ytmp = 0;
            for (i = 0, j = 1; j < n; i = j, j++)
            {
                pi = coords.get(i);
                pj = coords.get(j);
                xi = pi.getLon(); yi = pi.getLat();
                xj = pj.getLon(); yj = pj.getLat();
                dx = xj-xi;
                dy = yj-yi;
                li = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
                ltmp += li;
                xtmp += (xi + dx/2.0) * li;
                ytmp += (yi + dy/2.0) * li;
            }
            if (ltmp != 0)
                return new LatLon(ytmp / ltmp, xtmp / ltmp);
        }

        // Otherwise we have to use the bounding box (e.g. when all coords are in one line)
        return bounds.getCenter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public Integer getDimension() {
        return !coords.isEmpty() ? 1 : null;
    }

    @Override
    public Integer getNumberOfPoints() {
        return coords.size();
    }
}
