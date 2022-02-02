package com.KafSi.schedule

import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.nio.charset.Charset

class AddToFavButtonHandler {

    companion object {
        fun addToFavButtonClick(
            context: Context,
            localData: MutableList<List<String>>,
            oldName: String, type: String,
            link1: String, link2: String = "_",
            position: Int = -1
        ): Int {
            val name = if (oldName[oldName.length - 1] == '.') {
                oldName.dropLast(1)
            } else {
                oldName
            }

            val fileOutput = try {
                context.openFileOutput(name, Context.MODE_APPEND)
            } catch (e: Exception) {
                return 26
            }
            val file = context.getFileStreamPath(name)

            if (file.readLines().toString().indexOf(name) < 0) {
                /**домашняя страница*/

                try {
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

                    if (position > -1) {
                        fileOutput.run {
                            write(("" + position + '\n').toByteArray())
                            write((link1 + '\n').toByteArray())
                            write((link2 + '\n').toByteArray())
                            write((name + '\n').toByteArray())
                        }
                    } else {
                        if (type == "") {
                            file.delete()
                            fileOutput.close()
                            return 76
                        }

                        fileOutput.run {
                            write((type + '\n').toByteArray())
                            write((link1 + '\n').toByteArray())
                            write((name + '\n').toByteArray())
                        }
                    }

                    for (i in localData) {
                        fileOutput.write(";;;\n".toByteArray())

                        for (j in i) {
                            fileOutput.write((j + '\n').toByteArray())
                        }
                    }

                    fileOutput.close()
                    return 0
                } catch (e: Exception) {
                    file.delete()
                }
            } else {
                try {
                    file.delete()
                } catch (e: Exception) {
                }

                fileOutput.close()
                return 1
            }

            fileOutput.close()
            return 110
        }

        fun addClassToFavButtonClick(
            context: Context, localData: MutableList<List<String>>,
            name: String
        ): Int {
            val fileOutput = try {
                context.openFileOutput(name, Context.MODE_APPEND)
            } catch (e: Exception) {
                return 120
            }

            val file = context.getFileStreamPath(name)

            if (file.readLines().toString().indexOf(name) < 0) {
                try {
                    fileOutput.write((name + '\n').toByteArray())

                    for (i in localData) {
                        fileOutput.write(";;;\n".toByteArray())

                        for (j in i) {
                            fileOutput.write((j + '\n').toByteArray())
                        }
                    }

                    fileOutput.close()

                    val classesNotifyFile = context.getFileStreamPath("classesNotify")

                    if (!classesNotifyFile.canRead() || classesNotifyFile.readText(Charset.defaultCharset()) != "true") {
                        val classesNotifyFileOutput =
                            context.openFileOutput("classesNotify", Context.MODE_PRIVATE)

                        AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setTitle("Внимание")
                            .setMessage("Расписание аудиторий не будет автоматически обновляться")
                            .setPositiveButton("Больше не показывать") { _, _ ->
                                classesNotifyFileOutput.write("true".toByteArray())
                                classesNotifyFileOutput.close()
                            }
                            .setNeutralButton("Ок") { _, _ ->
                                classesNotifyFileOutput.write("false".toByteArray())
                                classesNotifyFileOutput.close()
                            }
                            .create()
                            .show()
                    }

                    return 0
                } catch (e: Exception) {
                    file.delete()
                }
            } else {
                try {
                    file.delete()
                } catch (e: Exception) {
                }

                fileOutput.close()
                return 1
            }

            fileOutput.close()
            return 177
        }
    }
}
