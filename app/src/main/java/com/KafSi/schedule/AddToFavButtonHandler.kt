package com.KafSi.schedule

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.nio.charset.Charset

class AddToFavButtonHandler {
    companion object {
        fun addToFavButtonClick(
            button: FloatingActionButton, context: Context,
            localData: MutableList<List<String>>,
            pos: Int, name: String,
            link1: String, link2: String = "_"
        ) {
            val fileOutput = context.openFileOutput(name, Context.MODE_APPEND)
            val file = context.getFileStreamPath(name)

            if (file.readLines().toString().indexOf(name) < 0) {
                /**домашняя страница*/
                AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("Сделать домашней страницей?")
                    .setNegativeButton("Нет") { dialog, id ->
                        val favFile = context.getFileStreamPath("fav")

                        try {
                            if (favFile != null && favFile.readText(Charset.defaultCharset()) ==
                                name
                            ) {
                                favFile.delete()
                            }
                        } catch (e: Exception) {
                        }
                    }
                    .setPositiveButton("Да") { dialog, id ->
                        var favFileOutput = context.openFileOutput("fav", Context.MODE_APPEND)
                        val favFile = context.getFileStreamPath("fav")

                        if (favFile.readText(Charset.defaultCharset()) == "") {
                            favFileOutput.write(name.toByteArray())
                        } else {
                            favFileOutput.close()
                            favFile.delete()
                            favFileOutput = context.openFileOutput("fav", Context.MODE_APPEND)
                            favFileOutput.write(name.toByteArray())
                        }
                    }
                    .create()
                    .show()

                Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                button.setImageResource(R.drawable.star_on)

                if (link2 != "_") {
                    fileOutput.run {
                        write((link1 + '\n').toByteArray())
                        write((link2 + '\n').toByteArray())
                        write("${pos}\n".toByteArray())
                        write((name + '\n').toByteArray())
                    }
                } else {
                    fileOutput.run {
                        write((link1 +'\n').toByteArray())
                        write((name + '\n').toByteArray())
                    }
                }

                var count = 1
                for (i in localData) {
                    fileOutput.write("$count;;\n".toByteArray())
                    count++

                    for (j in i) {
                        fileOutput.write((j + '\n').toByteArray())
                    }
                }
            } else {
                Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                button.setImageResource(R.drawable.star_off)

                try {
                    file.delete()
                } catch (e: Exception) {
                }
            }
            fileOutput.close()
        }
    }
}
