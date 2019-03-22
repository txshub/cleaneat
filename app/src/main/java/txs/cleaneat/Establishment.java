package txs.cleaneat;

public class Establishment {
    private String name;
    private String rating;

    public Establishment(String name, String rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }
}
