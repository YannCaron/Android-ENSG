package eu.ensg.forester;

import android.location.Location;

/**
 * Created by cyann on 26/12/15.
 */
public class WeatherObservation {

    private final Location location;
    private final Condition condition;
    private final int temperature;
    private final int windSpeed;

    public WeatherObservation(Location location, String condition, String cloud, int temperature, int windSpeed) {
        this.location = location;
        this.temperature = temperature;
        this.windSpeed = windSpeed;

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
        } else {
            this.condition = Condition.MIST;
        }
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
        SUN, CLOUD, MIST, RAIN, SNOW
    }
}
