package eu.ensg.spatialite.geom;

import android.util.Log;

import junit.framework.TestCase;

/**
 * Created by cyann on 18/12/15.
 */
public class GeometryCollectionTest extends TestCase {

    public void testMarshall() throws Exception {

        GeometryCollection geometryCollection= new GeometryCollection();

        GeometryCollection geometryCollection2= new GeometryCollection();
        geometryCollection.addGeometry(geometryCollection2);

        Polygon polygon = new Polygon();
        polygon.addCoordinate(new XY(10, 10));
        polygon.addCoordinate(new XY(20, 10));
        polygon.addCoordinate(new XY(20, 20));
        polygon.addCoordinate(new XY(10, 20));

        Point point = new Point(15, 15);

        geometryCollection2.addGeometry(polygon);
        geometryCollection2.addGeometry(point);

        Log.i(this.getClass().getName(), geometryCollection.toString());
        assertEquals("GEOMETRYCOLLECTION (GEOMETRYCOLLECTION (POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10)), POINT (15 15)))", geometryCollection.toString());
    }

    public void testUnMarshall() throws Exception {

        String geometryCollectionString = "GEOMETRYCOLLECTION (GEOMETRYCOLLECTION (POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10)), POINT (15 15)))";

        GeometryCollection geometryCollection= GeometryCollection.unMarshall(new StringBuilder(geometryCollectionString));
        System.out.println(geometryCollection);

        assertEquals(geometryCollectionString, geometryCollection.toString());
    }

}