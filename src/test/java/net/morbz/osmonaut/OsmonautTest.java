package net.morbz.osmonaut;

import static net.morbz.osmonaut.osm.EntityType.NODE;
import static net.morbz.osmonaut.osm.EntityType.WAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import net.morbz.osmonaut.geometry.LineString;
import net.morbz.osmonaut.geometry.MultiPolygon;
import net.morbz.osmonaut.geometry.MultiPolygonMember;
import net.morbz.osmonaut.geometry.Polygon;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.EntityType;
import net.morbz.osmonaut.osm.LatLon;
import net.morbz.osmonaut.osm.Node;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;

public class OsmonautTest {
	@Test
	public void should_find_nodes() throws Exception {
		List<Node> nodes = scan(new EntityFilter(true, false, false), new Predicate<Tags>() {
			@Override
			public boolean test(Tags tags) {
				return tags.hasKeyValue("railway", "subway_entrance");
			}
		});

		assertThat(nodes.get(1)).isEqualToComparingFieldByFieldRecursively(
				new Node(1986875861, entranceTags(), new LatLon(48.867002500000005, 2.3217243)));
		assertThat(nodes).extracting("latlon").containsOnly(new LatLon(48.867002500000005, 2.3217243),
				new LatLon(48.8667336, 2.3225672), new LatLon(48.866254600000005, 2.32355),
				new LatLon(48.8653454, 2.3226649000000004), new LatLon(48.866504500000005, 2.3237284000000002),
				new LatLon(48.8662246, 2.3234453));
	}

	@Test
	public void should_find_ways() throws Exception {
		List<Way> ways = scan(new EntityFilter(false, true, false), new Predicate<Tags>() {
			@Override
			public boolean test(Tags tags) {
				return tags.hasKeyValue("bridge", "yes");
			}
		});

		assertThat(ways).hasSize(2);
		assertThat(ways.get(0)).isEqualToComparingFieldByFieldRecursively(new Way(28302023, bridgeTags(), nodes()));
	}

	@Test
	public void should_find_relations() throws Exception {
		List<Relation> relations = scan(new EntityFilter(false, false, true), new Predicate<Tags>() {
			@Override
			public boolean test(Tags tags) {
				return tags.hasKeyValue("public_transport", "stop_area") && tags.hasKeyValue("name", "Concorde");
			}
		});
		assertThat(relations).hasSize(1);

		Relation concorde = relations.get(0);
		assertThat(concorde.getId()).isEqualTo(379422);
		assertThat(concorde.getMembers()).filteredOn(only(NODE)).hasSize(13);
		assertThat(concorde.getMembers()).filteredOn(only(WAY)).hasSize(4);
	}

	private Predicate<RelationMember> only(final EntityType type) {
		return new Predicate<RelationMember>() {
			@Override
			public boolean test(RelationMember t) {
				return t.getEntity().getEntityType().equals(type);
			}
		};
	}

	private <T> List<T> scan(EntityFilter filter, final Predicate<Tags> predicate) {
		String file = OsmonautTest.class.getResource("/concorde-paris.osm.pbf").getPath();
		final List<T> acc = new ArrayList<>();
		Osmonaut osmonaut = new Osmonaut(file, filter);
		osmonaut.scan(new IOsmonautReceiver() {
			@Override
			public boolean needsEntity(EntityType type, Tags tags) {
				return predicate.test(tags);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void foundEntity(Entity entity) {
				acc.add((T) entity);
			}
		});
		return acc;
	}

	private ArrayList<Node> nodes() {
		ArrayList<Node> list = new ArrayList<Node>();
		list.add(new Node(310795674, new Tags(), new LatLon(48.887131700000005, 2.252968)));
		list.add(new Node(417635644, new Tags(), new LatLon(48.8861514, 2.2561025000000003)));
		return list;
	}

	private Tags entranceTags() {
		Tags tags = new Tags();
		tags.set("name", "Concorde");
		tags.set("railway", "subway_entrance");
		tags.set("wheelchair", "no");
		return tags;
	}

	private Tags bridgeTags() {
		Tags tags = new Tags();
		tags.set("bridge", "yes");
		tags.set("name", "Métro 1");
		tags.set("railway", "subway");
		return tags;
	}

	@Test
	public void polygon_area_centroid() {
		// a square with side length 2, oriented CCW
		{
			Polygon squareCCW = new Polygon(Arrays.asList(
					new LatLon(0, 0),
					new LatLon(0, 2),
					new LatLon(2, 2),
					new LatLon(2, 0)
			));
			assertThat(squareCCW.getSignedArea()).isEqualTo(4);
			assertThat(squareCCW.getCenter()).isEqualTo(new LatLon(1, 1));
		}
		// a square with side length 2, oriented CW
		{
			Polygon squareCW = new Polygon(Arrays.asList(
					new LatLon(0, 0),
					new LatLon(2, 0),
					new LatLon(2, 2),
					new LatLon(0, 2)
			));
			assertThat(squareCW.getSignedArea()).isEqualTo(-4);
			assertThat(squareCW.getCenter()).isEqualTo(new LatLon(1, 1));
		}
		// a circle with center (42,16) and radius 1000, oriented CCW
		{
			LatLon center = new LatLon(42, 16);
			double radius = 1000;
			double PI2 = Math.PI * 2.0;
			double step = PI2 / 360.0;
			List<LatLon> circlePoints = new ArrayList<>(360);
			for (double t = 0; t < PI2; t += step)
				circlePoints.add(new LatLon(
						center.getLat() + radius * Math.sin(t),
						center.getLon() + radius * Math.cos(t)
				));
			Polygon circle = new Polygon(circlePoints);
			assertThat(circle.getSignedArea()).isCloseTo(Math.pow(radius, 2.0) * Math.PI, Percentage.withPercentage(0.1));
			LatLon calculatedCenter = circle.getCenter();
			assertThat(calculatedCenter.getLat()).isCloseTo(center.getLat(), Percentage.withPercentage(0.1));
			assertThat(calculatedCenter.getLon()).isCloseTo(center.getLon(), Percentage.withPercentage(0.1));
		}
		// a pentagram
		{
			double alpha = (2 * Math.PI) / 10;
			double radius = 8;
			double shortRadius = radius * (Math.sin(alpha/2.0)/Math.sin(Math.PI - 1.5*alpha));
			LatLon center = new LatLon(42,16);
			List<LatLon> starPoints = new ArrayList<>(10);
			for(int i = 10; i > 0; i--)
			{
				double r = i%2 == 1 ? radius : shortRadius;
				double omega = alpha * i;
				starPoints.add(new LatLon(
						(r * Math.cos(omega)) + center.getLat(),
						(r * Math.sin(omega)) + center.getLon()
				));
			}
			Polygon star = new Polygon(starPoints);
			assertThat(star.getSignedArea()).isCloseTo(radius * shortRadius * Math.sin(alpha) * 5.0, Percentage.withPercentage(0.1));
			LatLon calculatedCenter = star.getCenter();
			assertThat(calculatedCenter.getLat()).isCloseTo(center.getLat(), Percentage.withPercentage(0.1));
			assertThat(calculatedCenter.getLon()).isCloseTo(center.getLon(), Percentage.withPercentage(0.1));
		}
		{
			/*
			 * Just some weird shape, wound CCW
			 *  6 ┤     ┌─←─┐
			 *  5 ┤     ↓   ↑
			 *  4 ┤     │   └─←─┐
			 *  3 ┤     ↓ •     ↑   the dot denotes the centroid
			 *  2 ┤ ┌─←─┼─→───→─┘
			 *  1 ┤ ↓   ↑
			 *  0 ┤ └─→─┘
			 * -1 ┼─┬─┬─┬─┬─┬─┬─┬─
			 *   -1 0 1 2 3 4 5 6
			 */
			Polygon shape = new Polygon(Arrays.asList(
					new LatLon(0, 0),
					new LatLon(0, 2),
					new LatLon(2, 2),
					new LatLon(2, 6),
					new LatLon(4, 6),
					new LatLon(4, 4),
					new LatLon(6, 4),
					new LatLon(6, 2),
					new LatLon(2, 2),
					new LatLon(2, 0)
			));
			assertThat(shape.getSignedArea()).isEqualTo(16);
			assertThat(shape.getCenter()).isEqualTo(new LatLon(3, 3));
		}
	}

	@Test
	public void multipolygon_area_centroid() {
		/*
		 * two squares, one of them having a hole (marked with an x)
		 *  6 ┤     ┌───┬───┐
		 *  5 ┤     │   │ x │
		 *  4 ┤     │   └───┤
		 *  3 ┤     │ •     │   the dot denotes the centroid
		 *  2 ┤ ┌───┼───────┘
		 *  1 ┤ │   │
		 *  0 ┤ └───┘
		 * -1 ┼─┬─┬─┬─┬─┬─┬─┬─
		 *   -1 0 1 2 3 4 5 6
		 */
		{
			MultiPolygon twoSquares = new MultiPolygon(Arrays.asList(
					new MultiPolygonMember(MultiPolygonMember.Type.OUTER, new Polygon(Arrays.asList(
							new LatLon(0, 0),
							new LatLon(0, 2),
							new LatLon(2, 2),
							new LatLon(2, 0)
					))),
					new MultiPolygonMember(MultiPolygonMember.Type.OUTER, new Polygon(Arrays.asList(
							new LatLon(2, 2),
							new LatLon(2, 6),
							new LatLon(6, 6),
							new LatLon(6, 2)
					))),
					new MultiPolygonMember(MultiPolygonMember.Type.INNER, new Polygon(Arrays.asList(
							new LatLon(4, 4),
							new LatLon(4, 6),
							new LatLon(6, 6),
							new LatLon(6, 4)
					)))
			));
			assertThat(twoSquares.getSignedArea()).isEqualTo(16);
			assertThat(twoSquares.getCenter()).isEqualTo(new LatLon(3, 3));
		}
	}

	@Test
	public void linestring_centroid() {
		{
			/*
			 * Just some weird shape, wound CCW
			 *  2 ┤     ┌─→───→─╴
			 *  1 ┤     ↑ •        the dot (roughly) denotes the centroid
			 *  0 ┤ ╶─→─┘
			 * -1 ┼─┬─┬─┬─┬─┬─┬─┬─
			 *   -1 0 1 2 3 4 5 6
			 */
			LineString shape = new LineString(Arrays.asList(
					new LatLon(0, 0),
					new LatLon(0, 2),
					new LatLon(2, 2),
					new LatLon(2, 6)
			));
			assertThat(shape.getCenter()).isEqualTo(new LatLon(1.25, 2.75));
		}
	}
}
