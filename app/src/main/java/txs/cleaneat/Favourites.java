package txs.cleaneat;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Establishment.class}, version = 1)
public abstract class Favourites extends RoomDatabase {
    public abstract EstablishmentDao establishmentDao();
}
