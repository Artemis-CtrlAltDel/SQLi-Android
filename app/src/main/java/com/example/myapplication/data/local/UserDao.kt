package com.example.myapplication.data.local

import androidx.paging.rxjava3.RxPagingSource
import androidx.room.*
import com.example.myapplication.data.local.pojo.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUser(id: Long): User

    @Query("SELECT * FROM user ORDER BY joinedTimestamp DESC")
    fun getUserList(): RxPagingSource<Int, User>

    @Delete
    fun deleteUser(vararg user: User)

    @Query("DELETE FROM user")
    fun truncate()
}