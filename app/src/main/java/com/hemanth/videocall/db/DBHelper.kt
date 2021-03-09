package com.hemanth.videocall.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hemanth.videocall.model.UserProfile
import java.lang.Exception
import java.sql.SQLException

class DBHelper(var context: Context):
    SQLiteOpenHelper(
        context,
        DB_NAME,
        null,
        VERSION
    ) {

    companion object {
        private const val DB_NAME = "videocall.db"
        private const val VERSION = 1
    }
    override fun onCreate(db: SQLiteDatabase?) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    fun createTables(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS ${TableNames.userProfile} (" + User.userName + "TEXT," +
                    User.profilePic + "TEXT," +
                    User.bio
        )
    }

    fun insertUserDetails(userProfile: UserProfile): Boolean {
        var response = -1L
        val selectQuery = "SELECT * FROM ${TableNames.userProfile}"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        try {
            val values = ContentValues()
            values.put(User.userName, userProfile.userName)
            values.put(User.profilePic, userProfile.profilePic)
            values.put(User.bio, userProfile.bio)

            if (cursor.moveToFirst()) {
                db.update(TableNames.userProfile, values, null, null)
            } else {
                response = db.insert(TableNames.userProfile, null, values)
            }
        } catch (se: SQLException) {
            se.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db?.close()
        }
        return (Integer.parseInt("$response") != -1)
    }

    fun getUserDetails(): UserProfile {
        var userProfile = UserProfile()
        val selectQuery = "SELECT * FROM ${TableNames.userProfile}"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        try {
            if (cursor.moveToFirst()) {
                userProfile.userName = cursor.getString(cursor.getColumnIndex(User.userName))
                userProfile.profilePic = cursor.getString(cursor.getColumnIndex(User.profilePic))
                userProfile.bio = cursor.getString(cursor.getColumnIndex(User.bio))
            }
        } catch (se: SQLException) {
            se.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db?.close()
        }
        return userProfile
    }


}