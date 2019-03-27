package txs.cleaneat;

/**
 * Class holding information about entities that populate the spinners
 */
public class SpinnerRecord {
    private int id;
    private String stringId;
    private String name;
    private String regionName;

    public SpinnerRecord(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SpinnerRecord(int id, String name, String regionName) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        if (stringId == null) {
            return String.valueOf(id);
        } else {
            return stringId;
        }
    }
    public String getName() {
        return name;
    }

    public String getRegionName() {
        return regionName;
    }

    @Override
    public String toString() {
        return getName();
    }
}
