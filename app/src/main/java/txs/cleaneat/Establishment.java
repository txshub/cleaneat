package txs.cleaneat;

/**
 * Class representing an establishment
 */
public class Establishment {

    private Integer id;
    private String name;
    private String rating;
    private boolean favourite;

    public Establishment(int id, String name, String rating, boolean favourite) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.favourite = favourite;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public boolean isFavourite() {
        return favourite;
    }
    public void toggleFavourite() {
        favourite = !favourite;
    }
    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
