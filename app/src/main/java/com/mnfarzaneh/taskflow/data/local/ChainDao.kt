package com.mnfarzaneh.taskflow.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChainDao {

    @Query("SELECT * FROM chains ORDER BY createdAt DESC")
    fun getAllChains(): Flow<List<ChainEntity>>

    @Query("SELECT * FROM chains WHERE id = :chainId")
    suspend fun getChainById(chainId: Long): ChainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChain(chain: ChainEntity): Long

    @Update
    suspend fun updateChain(chain: ChainEntity)

    @Delete
    suspend fun deleteChain(chain: ChainEntity)
}