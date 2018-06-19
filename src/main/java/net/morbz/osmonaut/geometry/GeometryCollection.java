package net.morbz.osmonaut.geometry;

import net.morbz.osmonaut.osm.LatLon;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;

import java.util.ArrayList;
import java.util.Collection;

public class GeometryCollection implements IGeometry {
    private ArrayList<IGeometry> members = new ArrayList<IGeometry>();
    private Bounds bounds = new Bounds();

    public GeometryCollection(Relation relation) {
        for(RelationMember member : relation.getMembers())
            add(IGeometry.from(member.getEntity()));
    }

    public GeometryCollection(Collection<IGeometry> members) {
        for(IGeometry member : members)
            add(member);
    }

    public GeometryCollection() {
        //Used when using the add method
    }

    public void add(IGeometry member) {
        this.members.add(member);
        this.bounds.extend(member.getBounds());
    }

    @Override
    public LatLon getCenter() {
        // centroid of the centroid of the members of highest dimension
        Integer maxDimension = getDimension();
        if(maxDimension != null) {
            int n = 0;
            double xtmp = 0, ytmp = 0;
            for (IGeometry member : members) {
                if(maxDimension.equals(member.getDimension())) {
                    LatLon center = member.getCenter();
                    if(center != null) {
                        xtmp += center.getLon();
                        ytmp += center.getLat();
                        ++n;
                    }
                }
            }
            if(n > 0)
                return new LatLon(ytmp/((double)n), xtmp/((double)n));
        }
        return null;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public Integer getDimension() {
        Integer maxDimension = null;
        for(IGeometry member : members) {
            Integer dim = member.getDimension();
            if((maxDimension == null && dim != null) || (dim != null && dim > maxDimension))
                maxDimension = dim;
        }
        return maxDimension;
    }
}
