package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight;

public enum FlightControlType {
    SUPER_BOOST_STYLE,
    SIMPLE_FLIGHT_SIM;

    public static FlightControlType fromName(String name) {
        if (name != null) {
            for (FlightControlType value : values()) {
                if (value.name().equalsIgnoreCase(name)) return value;
            }
        }
        return SUPER_BOOST_STYLE;
    }
}
