package eu.ensg.spatialite.geom;

import junit.framework.TestCase;

/**
 * Created by cyann on 18/12/15.
 */
public class PolygonTest extends TestCase {

    public void testMarshall() throws Exception {

        Polygon polygon = new Polygon();
        polygon.addCoordinate(new XY(10, 10));
        polygon.addCoordinate(new XY(20, 10));
        polygon.addCoordinate(new XY(20, 20));
        polygon.addCoordinate(new XY(10, 20));

        XYList interior = new XYList(true);
        interior.add(new XY(14, 14));
        interior.add(new XY(17, 14));
        interior.add(new XY(17, 17));
        interior.add(new XY(14, 17));

        polygon.addInterior(interior);

        System.out.println(polygon);
        assertEquals("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10), (14 14, 17 14, 17 17, 14 17, 14 14))", polygon.toString());
    }

    public void testUnMarshall() throws Exception {

        String polyString = "POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10), (14 14, 17 14, 17 17, 14 17, 14 14))";

        Polygon polygon = Polygon.unMarshall(new StringBuilder(polyString));
        System.out.println(polygon);

        assertEquals(polyString, polygon.toString());
    }

}