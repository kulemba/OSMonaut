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

public class Point implements IGeometry {
    private final LatLon point;
    private final Bounds bounds;

    public Point(Node node) {
        this.point = node.getLatlon();
        this.bounds = node.getBounds();
    }

    public Point(LatLon point) {
        this.point = new LatLon(point.getLat(), point.getLon());
        this.bounds = new Bounds();
        this.bounds.extend(point);
    }

    @Override
    public LatLon getCenter() {
        return point;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public Integer getDimension() {
        return point != null ? 0 : null;
    }

    @Override
    public Integer getNumberOfPoints() {
        return 1;
    }
}
