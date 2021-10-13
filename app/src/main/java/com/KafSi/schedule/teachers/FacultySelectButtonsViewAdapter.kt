package com.KafSi.schedule.teachers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.HostViewActivity
import com.KafSi.schedule.R

class FacultySelectButtonsViewAdapter(
    private val facDepList: MutableList<Pair<List<String>, List<String>>>,
    private val context: Context
) : RecyclerView.Adapter<FacultySelectButtonsViewAdapter.FacultyButtonHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyButtonHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_button_layout, parent, false)

        return FacultyButtonHolder(item)
    }

    override fun onBindViewHolder(holder: FacultyButtonHolder, position: Int) {
        holder.bind(facDepList[position], position, context)
    }

    override fun getItemCount() = facDepList.size

    class FacultyButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val favButton = itemView.findViewById<Button>(R.id.favItemButton)
        private val cafLinkPairList = mutableListOf<Pair<String, String>>()

        fun bind(
            currentFacDepPair: Pair<List<String>, List<String>>,
            position: Int,
            context: Context
        ) {
            if (currentFacDepPair.first[0].indexOf("META") >= 0 ||
                currentFacDepPair.second[0].indexOf("META") >= 0
            ) {
                favButton.text = "Прочее"
            } else {
                val facName = currentFacDepPair.first[0]

                favButton.text = try {
                    facName.substring(
                        facName.indexOf("\">") + 2,
                        facName.indexOf("</h2>")
                    )
                } catch (e: Exception) {
                    return
                }
            }

            for (i in 1 until currentFacDepPair.first.size) {
                var currentCaf = currentFacDepPair.first[i]

                cafLinkPairList.add(
                    Pair(
                        currentCaf.substring(
                            currentCaf.indexOf("Caf"),
                            currentCaf.indexOf(".htm")
                        ),
                        currentCaf.substring(
                            currentCaf.indexOf("\">") + 2,
                            currentCaf.indexOf("</a>")
                        )
                    )
                )

                try{
                    currentCaf = currentFacDepPair.second[i]

                    cafLinkPairList.add(
                        Pair(
                            currentCaf.substring(
                                currentCaf.indexOf("Caf"),
                                currentCaf.indexOf(".htm")
                            ),
                            currentCaf.substring(
                                currentCaf.indexOf("\">") + 2,
                                currentCaf.indexOf("</a>")
                            )
                        )
                    )
                }
                catch (e:Exception){
                    cafLinkPairList.add(Pair("", ""))
                }
            }

            val intent = Intent(context, HostViewActivity::class.java)
            intent.putExtra("isTeachers", true)
            intent.putExtra("cafLinkPairArray", cafLinkPairList.toList().toTypedArray())

            favButton.setOnClickListener {
                context.startActivity(intent)
            }
        }
    }
}