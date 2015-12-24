package eu.ensg.spatialite.geom;

import junit.framework.TestCase;
/**
 * Created by cyann on 18/12/15.
 */
public class PointTest extends TestCase {

    public void testMarshall() throws Exception {

        Point point = new Point(new XY(10, 20));

        System.out.println(point);
        assertEquals("POINT (10 20)", point.toString());
    }

    public void testUnMarshall() throws Exception {

        String pointString = "POINT (10 20)";

        Point point = Point.unMarshall(new StringBuilder(pointString));
        System.out.println(point);

        assertEquals(pointString, point.toString());
    }

}