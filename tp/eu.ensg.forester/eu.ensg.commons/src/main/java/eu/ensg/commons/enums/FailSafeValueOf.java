package eu.ensg.commons.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastien Lorber at http://stackoverflow.com/questions/1167982/check-if-enum-exists-in-java
 */
public class FailSafeValueOf<T extends Enum<T>> {

    private final Map<String,T> nameToEnumMap;

    private FailSafeValueOf(Class<T> enumClass) {
        nameToEnumMap = new HashMap<>();
        for ( T value : EnumSet.allOf(enumClass)) {
            nameToEnumMap.put( value.name() , value);
        }
    }

    /**
     * Returns the value of the given enum element
     * If the
     * @param enumName
     * @return
     */
    public T valueOf(String enumName) {
        return nameToEnumMap.get(enumName);
    }

    public static <U extends Enum<U>> FailSafeValueOf<U> create(Class<U> enumClass) {
        return new FailSafeValueOf<U>(enumClass);
    }

}