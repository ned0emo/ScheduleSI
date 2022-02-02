package com.KafSi.schedule.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.FavoriteActivity
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.Serializable

class FavoriteListFragment : Fragment() {
    private val filesTextsList = mutableListOf<List<String>>()
    private var favIndex = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onResume()
    }

    override fun onResume() {
        super.onResume()

        filesTextsList.clear()
        var fileCount = requireContext().fileList().size

        for (i in requireContext().fileList()) {
            /**список с этой херней, с текстом из файлов по группам/преподам*/
            if (i != "fav" && i.length > 2 && i != "classesNotify") {
                if (requireContext().getFileStreamPath(i).readLines().size > 25)
                    filesTextsList.add(requireContext().getFileStreamPath(i).readLines())
                else{
                    fileCount--
                }
            } else {
                fileCount--
            }
        }

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = FavRecAdapter(fileCount, filesTextsList, requireContext(), requireActivity())
        recyclerView = requireActivity().findViewById<RecyclerView>(R.id.favRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val delFloatButton: FloatingActionButton =
            requireActivity().findViewById(R.id.deleteFloatButton)
        delFloatButton.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Очистить избранное?")
                .setPositiveButton("Да") { _, _ ->
                    for (i in requireContext().fileList()) {
                        /**список с этой херней, с текстом из файлов по группам/преподам*/
                        if (i != "classesNotify") {
                            requireContext().getFileStreamPath(i).delete()
                        }
                    }

                    filesTextsList.clear()

                    viewManager = LinearLayoutManager(requireContext())
                    viewAdapter =
                        FavRecAdapter(0, filesTextsList, requireContext(), requireActivity())
                    recyclerView =
                        requireActivity().findViewById<RecyclerView>(R.id.favRecyclerView).apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                }
                .setNegativeButton("Нет", null)
                .create()
                .show()
        }
    }
}

class FavRecAdapter(
    private val itemCount: Int,
    private val filesList: MutableList<List<String>>,
    private val context: Context,
    private val thisActivity: FragmentActivity
) :
    RecyclerView.Adapter<FavRecAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate((R.layout.fav_button_layout), parent, false)

        return MyViewHolder(item, context)
    }

    override fun getItemCount() = itemCount

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        object : Thread() {
            override fun run() {
                holder.bind(filesList[holder.absoluteAdapterPosition], thisActivity)
            }
        }.start()
    }

    /**Класс внутри класса кекс*/
    class MyViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val favButton = itemView.findViewById<Button>(R.id.favItemButton)
        private val listOfSchedule = mutableListOf<MutableList<String>>()

        /**Тут преобразуем данные из файла в лист*/
        fun bind(scheduleList: List<String>, thisActivity: FragmentActivity) {

            if (scheduleList.size < 50) {
                return
            }

            /**Для студентов очных*/
            if (scheduleList[0] == "bakalavriat" || scheduleList[0] == "spezialitet") {
                /**Строка в файле, с которой начинается добавление*/
                var k = 4

                val tmpList = mutableListOf<String>()

                for (i in 0..11) {
                    tmpList.clear()

                    for (j in k..k + 5) {
                        tmpList.add(scheduleList[j])
                    }

                    k += 7
                    listOfSchedule.add(tmpList.drop(0) as MutableList<String>)

                    if (k >= scheduleList.size) break
                }

                thisActivity.runOnUiThread {
                    run {
                        favButton.text = scheduleList[2]
                        /**кнопка для студентов*/
                        favButton.setOnClickListener {
                            val intent = Intent(context, FavoriteActivity::class.java)
                                .putExtra("type", scheduleList[0])
                                .putExtra("link", scheduleList[1])
                                .putExtra("name", scheduleList[2])
                                .putExtra("favSchedule", listOfSchedule as Serializable)

                            //PublicData.favSchedule = listOfSchedule

                            startActivity(context, intent, null)
                        }
                    }
                }

            } else if (scheduleList[0] == "zo1" || scheduleList[0] == "zo2") {
                /**заочники*/
                var k = 4 //строка в файле, с которой начинается добавление

                val tmpList = mutableListOf<String>()

                for (i in 0..27) {
                    if (scheduleList.size - k < 7) break

                    tmpList.clear()

                    for (j in k..k + 7) {
                        tmpList.add(scheduleList[j])
                    }

                    k += 9
                    listOfSchedule.add(tmpList.drop(0) as MutableList<String>)
                }

                thisActivity.runOnUiThread {
                    run {
                        favButton.text = scheduleList[2]
                        /**кнопка для студентов*/
                        favButton.setOnClickListener {
                            val intent = Intent(context, FavoriteActivity::class.java)
                                .putExtra("type", scheduleList[0])
                                .putExtra("link", scheduleList[1])
                                .putExtra("name", scheduleList[2])
                                .putExtra("favSchedule", listOfSchedule as Serializable)

                            //PublicData.favSchedule = listOfSchedule

                            startActivity(context, intent, null)
                        }
                    }
                }
            } else if (scheduleList[1] == ";;;") {
                /**для аудиторий*/
                var k = 2
                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..11) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 5) {
                            tmpList.add(scheduleList[j])
                        }
                    } catch (e: Exception) {
                    }

                    k += 7
                    listOfList.add(tmpList)
                }

                thisActivity.runOnUiThread {
                    run {
                        favButton.text = scheduleList[0]
                        /**кнопка для аудиторий*/
                        favButton.setOnClickListener {
                            val intent = Intent(context, FavoriteActivity::class.java)
                            intent.putExtra("name", scheduleList[0])
                                .putExtra("favSchedule", listOfList as Serializable)

                            //PublicData.favSchedule = listOfList
                            //PublicData.catalog = ""
                            startActivity(context, intent, null)
                        }
                    }
                }
            } else {
                /**Для преподов*/
                var k = 5
                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..11) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 7) {
                            tmpList.add(scheduleList[j])
                        }
                    } catch (e: Exception) {
                    }

                    k += 9
                    listOfList.add(tmpList)
                }

                thisActivity.runOnUiThread {
                    run {
                        favButton.text = scheduleList[3]
                        /**кнопка для преподов*/
                        favButton.setOnClickListener {
                            val intent = Intent(context, FavoriteActivity::class.java)
                            intent.putExtra("name", scheduleList[3])
                                .putExtra("link", scheduleList[1])
                                .putExtra("link2", scheduleList[2])
                                .putExtra("favSchedule", listOfList as Serializable)
                                .putExtra("position", scheduleList[0])

                            //PublicData.favSchedule = listOfList
                            //PublicData.catalog = ""
                            startActivity(context, intent, null)
                        }
                    }
                }
            }
        }
    }
}