package txs.cleaneat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Establishment {
    @PrimaryKey
    @NonNull
    private Integer id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "rating")
    private String rating;

    public Establishment(int id, String name, String rating) {
        this.id = id;
        this.name = name;
        this.rating = rating;
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
}
