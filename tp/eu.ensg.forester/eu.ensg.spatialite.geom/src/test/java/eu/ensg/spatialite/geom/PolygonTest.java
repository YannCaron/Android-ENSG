package eu.ensg.spatialite.geom;

import static org.junit.Assert.*;

/**
 * Created by cyann on 18/12/15.
 */
public class PolygonTest {

    @org.junit.Test
    public void testMarshall() throws Exception {

        Polygon polygon = new Polygon();
        polygon.getExterior().add(new XY(10, 10));
        polygon.getExterior().add(new XY(20, 10));
        polygon.getExterior().add(new XY(20, 20));
        polygon.getExterior().add(new XY(10, 20));
        polygon.getExterior().add(new XY(10, 10));

        XYList interior = new XYList();
        interior.add(new XY(14, 14));
        interior.add(new XY(17, 14));
        interior.add(new XY(17, 17));
        interior.add(new XY(14, 17));
        interior.add(new XY(14, 14));

        polygon.addInterior(interior);

        System.out.println(polygon);
        assertEquals("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10), (14 14, 17 14, 17 17, 14 17, 14 14))", polygon.toString());
    }
}