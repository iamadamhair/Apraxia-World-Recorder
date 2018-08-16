package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by adamhair on 8/16/2018.
 */

@Dao
public interface ProbeDao {

    @Query("SELECT DISTINCT probe_number FROM probe WHERE user_id LIKE :userId")
    LiveData<List<Integer>> getUniqueProbeNumbers(int userId);

    @Query("SELECT * FROM probe WHERE probe_number LIKE :probeNumbers")
    LiveData<List<Probe>> getProbesForNumber(int probeNumbers);

    @Query("SELECT * FROM probe WHERE user_id LIKE :userId GROUP BY probe_number")
    LiveData<List<Probe>> getUniqueProbesForUser(int userId);

    @Insert
    void insertAll(Probe... probes);

    @Delete
    void delete(Probe probes);

    @Update
    void update(Probe... probes);
}
