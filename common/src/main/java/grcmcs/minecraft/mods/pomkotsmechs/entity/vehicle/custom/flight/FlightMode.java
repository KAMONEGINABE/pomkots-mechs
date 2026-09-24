package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight;

public enum FlightMode {
    NORMAL,
    GERWALK,
    FLIGHT;

    public static FlightMode fromId(int id) {
        FlightMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : NORMAL;
    }
}
