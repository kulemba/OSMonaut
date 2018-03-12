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

import java.util.List;

import net.morbz.osmonaut.osm.LatLon;
import net.morbz.osmonaut.osm.Way;

/**
 * This class defines a Polygon of Latitude/Longitude coordinates. A polygon is
 * always closed.
 * 
 * @author MorbZ
 */
public class Polygon extends LineString implements IPolygon {

	/**
	 * Creates a polygon that contains all coordinates of the given list.
	 * 
	 * @param coords
	 *            The list of coordinates
	 */
	public Polygon(List<LatLon> coords) {
		super(coords);

		// Close polygon
		if (!isClosed()) {
			add(coords.get(0));
		}
	}

	/**
	 * Creates a polygon that contains the coordinates of the nodes of the given
	 * way.
	 * 
	 * @param way
	 *            The way
	 */
	public Polygon(Way way) {
		super(way);

		// Close polygon
		if (!way.isClosed()) {
			add(way.getNodes().get(0).getLatlon());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSignedArea() {
		int n;
		if((n = coords.size()-1) >= 3) { // coords is closed, discard the last point
			int i, j;
			LatLon lli,llj;
			double ai,xi,yi,xj,yj;
			double atmp = 0;
			for (i = n-1, j = 0; j < n; i = j, j++)
			{
				lli = coords.get(i);
				llj = coords.get(j);
				xi = lli.getLon();
				yi = lli.getLat();
				xj = llj.getLon();
				yj = llj.getLat();
				ai = xi * yj - xj * yi;
				atmp += ai;
			}
			return atmp / 2;
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LatLon getCenter() {
		// from http://www.faqs.org/faqs/graphics/algorithms-faq/ Subject 2.02
		int n;
		if((n = coords.size()-1) >= 3) { // coords is closed, discard the last point
			int i, j;
			LatLon pi,pj;
			double ai,xi,yi,xj,yj;
			double atmp = 0, xtmp = 0, ytmp = 0;
			for (i = n-1, j = 0; j < n; i = j, j++)
			{
				pi = coords.get(i);
				pj = coords.get(j);
				xi = pi.getLon(); yi = pi.getLat();
				xj = pj.getLon(); yj = pj.getLat();
				ai = xi * yj - xj * yi;
				atmp += ai;
				xtmp += (xj + xi) * ai;
				ytmp += (yj + yi) * ai;
			}
			if (atmp != 0)
				return new LatLon(ytmp / (3 * atmp), xtmp / (3 * atmp));
		}

		// Otherwise we have to use the bounding box (e.g. when all coords are in one line)
		return bounds.getCenter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LatLon> getCoords() {
		return coords;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(LatLon latlon) {
		// Check bounds
		if (!bounds.contains(latlon)) {
			return false;
		}

		// Iterate vertices
		boolean isIn = false;
		double lat = latlon.getLat();
		double lon = latlon.getLon();
		for (int i = 0, j = coords.size() - 1; i < coords.size(); j = i++) {
			double iLon = coords.get(i).getLon();
			double iLat = coords.get(i).getLat();
			double jLon = coords.get(j).getLon();
			double jLat = coords.get(j).getLat();
			if (((iLon > lon) != (jLon > lon)) && (lat < (jLat - iLat) * (lon - iLon) / (jLon - iLon) + iLat)) {
				isIn = !isIn;
			}
		}
		return isIn;
	}

	@Override
	public Integer getDimension() {
		return !coords.isEmpty() ? 2 : null;
	}
}
