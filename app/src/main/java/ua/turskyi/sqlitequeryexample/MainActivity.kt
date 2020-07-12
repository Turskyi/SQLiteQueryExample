package ua.turskyi.sqlitequeryexample

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main), View.OnClickListener {
    companion object {
        const val LOG_TAG = "myLogs"
    }

    var name = arrayOf("Китай", "США", "Бразилия", "Россия", "Япония",
            "Германия", "Египет", "Италия", "Франция", "Канада")
    var people = intArrayOf(1400, 311, 195, 142, 128, 82, 80, 60, 66, 35)
    var region = arrayOf("Азия", "Америка", "Америка", "Европа", "Азия",
            "Европа", "Африка", "Европа", "Европа", "Америка")

    private var dbHelper: DBHelper? = null
    var db: SQLiteDatabase? = null

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnAll.setOnClickListener(this)
        btnFunc.setOnClickListener(this)
        btnPeople.setOnClickListener(this)
        btnSort.setOnClickListener(this)
        btnGroup.setOnClickListener(this)
        btnHaving.setOnClickListener(this)
        dbHelper = DBHelper(this)
        // подключаемся к базе
        db = dbHelper?.writableDatabase

        // проверка существования записей
        val c: Cursor? = db?.query("mytable", null, null, null,
                null, null, null)
        if (c?.count == 0) {
            val cv = ContentValues()
            // заполним таблицу
            for (i in 0..9) {
                cv.put("name", name[i])
                cv.put("people", people[i])
                cv.put("region", region[i])
                Log.d(LOG_TAG, "id = " + db?.insert("mytable", null, cv))
            }
        }
        c?.close()
        dbHelper!!.close()
        // эмулируем нажатие кнопки btnAll
        onClick(btnAll)
    }

    override fun onClick(v: View?) {

        // подключаемся к базе
        db = dbHelper?.writableDatabase

        // данные с экрана
        val sFunc = etFunc.text.toString()
        val sPeople = etPeople.text.toString()
        val sRegionPeople = etRegionPeople.text.toString()

        // переменные для query
        val columns: Array<String>?
        val selection: String?
        val selectionArgs: Array<String>?
        val groupBy: String?
        val having: String?
        var orderBy: String? = null

        // курсор
        var c: Cursor? = null
        when (v?.id) {
            R.id.btnAll -> {
                Log.d(LOG_TAG, "--- Все записи ---")
                c = db?.query("mytable", null, null, null,
                        null, null, null)
            }
            R.id.btnFunc -> {
                Log.d(LOG_TAG, "--- Функция $sFunc ---")
                columns = arrayOf(sFunc)
                c = db?.query("mytable", columns, null, null,
                        null, null, null)
            }
            R.id.btnPeople -> {
                Log.d(LOG_TAG, "--- Население больше $sPeople ---")
                selection = "people > ?"
                selectionArgs = arrayOf(sPeople)
                c = db?.query("mytable", null, selection, selectionArgs,
                        null, null,
                        null)
            }
            R.id.btnGroup -> {
                Log.d(LOG_TAG, "--- Население по региону ---")
                columns = arrayOf("region", "sum(people) as people")
                groupBy = "region"
                c = db?.query("mytable", columns, null, null, groupBy,
                        null, null)
            }
            R.id.btnHaving -> {
                Log.d(LOG_TAG, "--- Регионы с населением больше " + sRegionPeople
                        + " ---")
                columns = arrayOf("region", "sum(people) as people")
                groupBy = "region"
                having = "sum(people) > $sRegionPeople"
                c = db?.query("mytable", columns, null, null, groupBy,
                        having, null)
            }
            R.id.btnSort -> {
                when (rgSort!!.checkedRadioButtonId) {
                    R.id.rName -> {
                        Log.d(LOG_TAG, "--- Сортировка по наименованию ---")
                        orderBy = "name"
                    }
                    R.id.rPeople -> {
                        Log.d(LOG_TAG, "--- Сортировка по населению ---")
                        orderBy = "people"
                    }
                    R.id.rRegion -> {
                        Log.d(LOG_TAG, "--- Сортировка по региону ---")
                        orderBy = "region"
                    }
                }
                c = db?.query("mytable", null, null, null,
                        null, null, orderBy)
            }
        }
        if (c != null) {
            if (c.moveToFirst()) {
                var str: String
                do {
                    str = ""
                    for (cn in c.columnNames) {
                        str += (cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ")
                    }
                    Log.d(LOG_TAG, str)
                } while (c.moveToNext())
            }
            c.close()
        } else Log.d(LOG_TAG, "Cursor is null")
        dbHelper?.close()
    }

    inner class DBHelper(context: Context?) : SQLiteOpenHelper(context, "myDB", null,
            1) {
        override fun onCreate(db: SQLiteDatabase) {
            Log.d(LOG_TAG, "--- onCreate database ---")
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement," + "name text,"
                    + "people integer," + "region text" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}