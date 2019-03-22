package txs.cleaneat;

public class SpinnerRecord {
    private int id;
    private String stringId;
    private String name;

    public SpinnerRecord(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SpinnerRecord(String stringId, String name) {
        this.stringId = stringId;
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

    @Override
    public String toString() {
        return getName();
    }
}
