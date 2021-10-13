package com.KafSi.schedule.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.FavoriteActivity
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R

class FavoriteListFragment : Fragment() {
    private val filesList = mutableListOf<List<String>>()
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

        filesList.clear()
        var fileCount = requireContext().fileList().size

        for (i in requireContext().fileList()) {
            /**список с этой херней, с текстом из файлов по группам/преподам*/
            if (i != "fav" && i.length > 2) {
                filesList.add(requireContext().getFileStreamPath(i).readLines())
            } else {
                fileCount--
            }
        }

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = FavRecAdapter(fileCount, filesList, requireContext(), requireActivity())
        recyclerView = requireActivity().findViewById<RecyclerView>(R.id.favRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
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
                holder.bind(filesList[holder.bindingAdapterPosition], thisActivity)
            }
        }.start()
    }

    /**Класс внутри класса кекс*/
    class MyViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val favButton = itemView.findViewById<Button>(R.id.favItemButton)

        /**Тут преобразуем данные из файла в лист*/
        fun bind(scheduleList: List<String>, thisActivity: FragmentActivity) {
            /**Для студентов*/
            if (scheduleList[1].indexOf("http") < 0) {
                /**Строка, с которой начинается добавление*/
                var k = 3

                val listOfList = mutableListOf<MutableList<String>>()

                for (i in 0..27) {
                    val tmpList = mutableListOf<String>()

                    try {
                        for (j in k..k + 7) {
                            tmpList.add(scheduleList[j])
                        }
                    } catch (e: Exception) {
                        break
                    }

                    k += 9
                    listOfList.add(tmpList)

                    if (k >= scheduleList.size) break
                }

                thisActivity.runOnUiThread {
                    run {
                        favButton.text = scheduleList[1]
                        /**кнопка для студентов*/
                        favButton.setOnClickListener {
                            favButton.startAnimation(
                                AnimationUtils.loadAnimation(
                                    context,
                                    R.anim.alpha
                                )
                            )

                            val intent = Intent(context, FavoriteActivity::class.java)
                            intent.putExtra("name", scheduleList[1])
                                .putExtra("link", scheduleList[0])
                                .putExtra("favSchedule", listOfList.toTypedArray())

                            PublicData.favSchedule = listOfList
                            PublicData.isTeacher = 0
                            PublicData.catalog =
                                if (scheduleList[0].indexOf("zo1") > -1
                                    || scheduleList[0].indexOf("zo2") > -1
                                ) {
                                    "zo1"
                                } else {
                                    ""
                                }
                            startActivity(context, intent, null)
                        }
                    }
                }
                /**Для преподов*/
            } else {
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
                                .putExtra("link", scheduleList[0])
                                .putExtra("link2", scheduleList[1])
                                .putExtra("position", scheduleList[2])
                                .putExtra("favSchedule", listOfList as Array<MutableList<String>>)

                            PublicData.favSchedule = listOfList
                            PublicData.isTeacher = 1
                            PublicData.catalog = ""
                            startActivity(context, intent, null)
                        }
                    }
                }
            }
        }
    }
}