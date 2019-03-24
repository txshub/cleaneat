package txs.cleaneat;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface EstablishmentDao {
    @Insert
    void insertEstablishment(Establishment establishment);

    @Query("SELECT * FROM establishment")
    LiveData<List<Establishment>> retrieveEstablishments();
}
