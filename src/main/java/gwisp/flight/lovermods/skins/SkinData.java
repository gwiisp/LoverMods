package gwisp.flight.lovermods.skins;

public class SkinData {
    private final String name;
    private final String value;
    private final String demand;
    private final String season;
    private final String set;

    public SkinData(String name, String value, String demand, String season, String set) {
        this.name = name;
        this.value = value;
        this.demand = demand;
        this.season = season;
        this.set = set;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDemand() {
        return demand;
    }

    public String getSeason() {
        return season;
    }

    public String getSet() {
        return set;
    }

    public String getFormattedValue() {
        String displayValue = (value == null || value.isEmpty()) ? "UNKNOWN" : value;
        return "§e[LM] Skin Value: §f" + displayValue;
    }

    public String getFormattedDemand() {
        String displayDemand = (demand == null || demand.isEmpty()) ? "UNKNOWN" : demand;
        String color = switch (displayDemand.toUpperCase()) {
            case "HIGH" -> "§c§l";
            case "MEDIUM" -> "§e§l";
            case "LOW" -> "§a§l";
            default -> "§7§l";
        };
        return "§e[LM] Skin Demand: " + color + displayDemand.toUpperCase();
    }

    public String getFormattedSeason() {
        String displaySeason = (season == null || season.isEmpty()) ? "UNKNOWN" : season;
        return "§e[LM] Skin Season: §f" + displaySeason;
    }

    public String getFormattedSet() {
        String displaySet = (set == null || set.isEmpty()) ? "UNKNOWN" : set;
        return "§e[LM] Skin Set: §f" + displaySet;
    }
}