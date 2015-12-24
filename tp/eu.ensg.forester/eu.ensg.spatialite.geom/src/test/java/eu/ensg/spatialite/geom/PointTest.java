package eu.ensg.spatialite.geom;

import static org.junit.Assert.assertEquals;

/**
 * Created by cyann on 18/12/15.
 */
public class PointTest {

    @org.junit.Test
    public void testMarshall() throws Exception {

        Point point = new Point(new XY(10, 20));

        System.out.println(point);
        assertEquals("POINT (10 20)", point.toString());
    }

    @org.junit.Test
    public void testUnMarshall() throws Exception {

        String pointString = "POINT (10 20)";

        Point point = Point.unMarshall(new StringBuilder(pointString));
        System.out.println(point);

        assertEquals(pointString, point.toString());
    }

}