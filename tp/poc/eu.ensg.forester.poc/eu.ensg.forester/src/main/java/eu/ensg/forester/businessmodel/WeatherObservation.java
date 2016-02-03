package eu.ensg.forester.businessmodel;

import com.google.android.gms.maps.model.LatLng;

import eu.ensg.commons.enums.FailSafeValueOf;

/**
 * Created by cyann on 26/12/15.
 */
public class WeatherObservation {

    private final LatLng location;
    private final Condition condition;
    private final int temperature;
    private final int windSpeed;

    public WeatherObservation(LatLng location, String condition, String cloud, int temperature, int windSpeed) {
        this.location = location;
        this.temperature = temperature;
        this.windSpeed = windSpeed;

        // find codes here: http://forum.geonames.org/gforum/posts/list/28.page
        if (condition.equalsIgnoreCase("n/a")) {
            if (cloud.equalsIgnoreCase("FEW") || cloud.equalsIgnoreCase("SCT") || cloud.equalsIgnoreCase("BKN") || cloud.equalsIgnoreCase("OVC")) {
                this.condition = Condition.CLOUD;
            } else {
                this.condition = Condition.SUN;
            }
        } else if (condition.equalsIgnoreCase("DZ") || condition.equalsIgnoreCase("RA")) {
            this.condition = Condition.RAIN;
        } else if (condition.equalsIgnoreCase("SN") || condition.equalsIgnoreCase("SG") || condition.equalsIgnoreCase("GR")) {
            this.condition = Condition.SNOW;
        } else if (condition.equalsIgnoreCase("SS") || condition.equalsIgnoreCase("DS") || condition.equalsIgnoreCase("TS")) {
            this.condition = Condition.STORM;
        } else {
            this.condition = Condition.MIST;
        }
    }

    public LatLng getLocation() {
        return location;
    }

    public Condition getCondition() {
        return condition;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    @Override
    public String toString() {
        return "WeatherObservation{" +
                "location=" + location +
                ", condition=" + condition +
                ", temperature=" + temperature +
                ", windSpeed=" + windSpeed +
                '}';
    }

    public enum Condition {
        SUN, CLOUD, MIST, RAIN, SNOW, STORM;

        private static final FailSafeValueOf<Condition> FAIL_SAFE = FailSafeValueOf.create(Condition.class);
        public static Condition failSafeValueOf(String enumName) {
            return FAIL_SAFE.valueOf(enumName);
        }
    }
}
