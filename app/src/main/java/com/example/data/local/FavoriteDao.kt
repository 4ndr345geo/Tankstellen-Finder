package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_stations ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT id FROM favorite_stations")
    fun getFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStationEntity)

    @Query("UPDATE favorite_stations SET lastDiesel = :diesel, lastE5 = :e5, lastE10 = :e10, isOpen = :isOpen WHERE id = :stationId")
    suspend fun updatePrices(stationId: String, diesel: Double?, e5: Double?, e10: Double?, isOpen: Boolean)

    @Query("DELETE FROM favorite_stations WHERE id = :stationId")
    suspend fun removeFavorite(stationId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE id = :stationId)")
    suspend fun isFavorite(stationId: String): Boolean
}
