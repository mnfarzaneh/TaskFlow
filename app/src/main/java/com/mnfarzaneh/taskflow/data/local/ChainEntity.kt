package com.mnfarzaneh.taskflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mnfarzaneh.taskflow.domain.model.Chain

@Entity(tableName = "chains")
data class ChainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Chain(
        id          = id,
        title       = title,
        description = description,
        createdAt   = createdAt
    )

    companion object {
        fun fromDomain(chain: Chain) = ChainEntity(
            id          = chain.id,
            title       = chain.title,
            description = chain.description,
            createdAt   = chain.createdAt
        )
    }
}