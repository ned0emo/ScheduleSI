package com.KafSi.schedule.fragments

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KafSi.schedule.FavoriteActivity
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R
import kotlinx.coroutines.Runnable
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class MyTabFragment : Fragment() {
    private var weekPos = 0
    private lateinit var localData: MutableList<MutableList<String>>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        weekPos = requireArguments().getInt("pos")

        localData = try{
            (parentFragment as MainScheduleFragment).localData
        }catch (e: Exception){
            (requireActivity() as FavoriteActivity).localData
        }

        return inflater.inflate(R.layout.fragment_schedule_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewManager = LinearLayoutManager(requireContext())
        //адаптер для ресайклера--------------------------------------------------------------------
        viewAdapter = MyAdapter(
            localData,
            weekPos,
            view.findViewById(R.id.hideWeeksLayout),
            requireActivity()
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}

class MyAdapter(//для ресайклера!!!-----------------------------------------------------------------
    private val scheduleList: MutableList<MutableList<String>>,//расписание по дням недели----------
    private var weekPos: Int,
    private var hideWeeksLayout: ConstraintLayout,
    private val thisActivity: FragmentActivity
) : RecyclerView.Adapter<MyAdapter.MyTabFragmentHolder>() {

    private var holderList = mutableListOf<MyTabFragmentHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTabFragmentHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_schedule_recycler_item, parent, false)

        return MyTabFragmentHolder(
            item,
            scheduleList,
            weekPos,
            itemCount,
            hideWeeksLayout,
            thisActivity
        )
    }

    override fun onBindViewHolder(holder: MyTabFragmentHolder, position: Int) {
        holderList.add(holder)
        object : Thread() {
            override fun start() {
                holder.bind(position, holderList)
            }
        }.start()
        //holder.bind(position, holderList)
    }

    override fun getItemCount() = if (scheduleList.size == 12) 6 else 7

    class MyTabFragmentHolder(
        itemView: View,
        private val scheduleList: MutableList<MutableList<String>>,
        private val weekPos: Int,
        private val itemCount: Int,
        private var hideWeeksLayout: ConstraintLayout,
        private val thisActivity: FragmentActivity
    ) : RecyclerView.ViewHolder(itemView) {

        private val dayOfWeekTextView =
            itemView.findViewById<TextView>(R.id.dayOfWeekTextView)
        private val dayOfWeekCardView =
            itemView.findViewById<CardView>(R.id.dayOfWeekCardVeiw)
        private val lessonsList =
            mutableListOf<String>()
        private val lessonsRecycler = itemView.findViewById<RecyclerView>(R.id.lessonsRecycler)
        private var isOpen = false
        private var originalHeight = 0
        private var expandedHeight = 0
        private var index = weekPos * 6

        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>
        private lateinit var viewManager: RecyclerView.LayoutManager

        fun bind(pos: Int, holderList: MutableList<MyTabFragmentHolder>) {
            index += pos

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))
            }

            val dayOfWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().dayOfWeek.value
            } else {
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))
                when (Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK)) {
                    1 -> 7
                    else -> Calendar.getInstance(TimeZone.getDefault())
                        .get(Calendar.DAY_OF_WEEK) - 1
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"))
            }

            var currentLesson = 0
            val currentLessonTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LocalTime.now().minute + LocalTime.now().hour * 60
            else
                Calendar.getInstance(TimeZone.getDefault())
                    .get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance(TimeZone.getDefault())
                    .get(Calendar.MINUTE)

            /**зазеленяем текущую пару*/
            if (PublicData.catalog != "zo1"
                && PublicData.catalog != "zo2"
                && pos + 1 == dayOfWeek
            ) {
                currentLesson = when (currentLessonTime) {
                    in 540..635 -> {
                        1
                    }
                    in 645..740 -> {
                        2
                    }
                    in 780..875 -> {
                        3
                    }
                    in 885..980 -> {
                        4
                    }
                    in 985..1080 -> {
                        5
                    }
                    in 1085..1180 -> {
                        6
                    }
                    else -> 0
                }
            }

            thisActivity.runOnUiThread {
                run {
                    //назначение заголовку дня недели-------------------------------------------------------
                    if (PublicData.catalog != "zo1" &&
                        PublicData.catalog != "zo2"
                    ) {
                        dayOfWeekTextView.text = when (pos) {
                            0 -> "Понедельник"
                            1 -> "Вторник"
                            2 -> "Среда"
                            3 -> "Четверг"
                            4 -> "Пятница"
                            5 -> "Суббота"
                            else -> ""
                        }
                    } else {
                        //для заочников отдельный алгоритм, у них дни недели привязаны к датам--------------
                        try {
                            dayOfWeekTextView.text = scheduleList[pos + weekPos * 7][0]
                            index = pos + weekPos * 7
                        } catch (e: Exception) {

                        }
                    }

                    for (i in 1..7) {
                        try{
                            lessonsList.add(scheduleList[index][i - PublicData.isTeacher])
                        }
                        catch(e: Exception){
                            break
                        }
                    }

                    /**вычисление высот карточки
                     * открытие нужной карточки
                     * исчезновение скрывающего слоя */
                    val displayMetrics = itemView.context.resources.displayMetrics
                    originalHeight = ((50 * displayMetrics.density) + 0.5).toInt()
                    lessonsRecycler.visibility = View.VISIBLE

                    if (expandedHeight == 0) {
                        itemView.post(Runnable {
                            expandedHeight = dayOfWeekCardView.height + 5

                            dayOfWeekCardView.layoutParams.height = originalHeight
                            lessonsRecycler.visibility = View.GONE

                            if (scheduleList.size == 12 && dayOfWeek == pos + 1) {
                                isOpen = true
                            }

                            if (pos == 5) {
                                for (i in holderList) {
                                    if (i.isOpen) {
                                        i.lessonsRecycler.visibility = View.VISIBLE
                                        i.lessonsRecycler.requestLayout()
                                        expandItem(i, true)
                                        break
                                    }
                                }
                                hideWeeksLayout.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .setStartDelay(500)
                                    .start()
                                //hideWeeksLayout.visibility = View.GONE
                            }
                        })

                        viewManager = LinearLayoutManager(itemView.context)
                        viewAdapter = ScheduleRecyclerAdapter(lessonsList, itemCount,
                            currentLesson, thisActivity)
                        recyclerView = lessonsRecycler.apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                    }

                    /**нажатие на день недели*/
                    dayOfWeekCardView.setOnClickListener {
                        if (!isOpen) {
                            for (i in holderList) {
                                if (i.isOpen) {
                                    lessonsRecycler.visibility = View.GONE
                                    lessonsRecycler.requestLayout()
                                    i.isOpen = false
                                    expandItem(i, false)
                                }

                                lessonsRecycler.visibility = View.VISIBLE
                                lessonsRecycler.requestLayout()
                                isOpen = true
                                expandItem(this@MyTabFragmentHolder, true)
                            }
                        }
                    }
                }
            }
        }

        private fun expandItem(holder: MyTabFragmentHolder, expand: Boolean) {
            val animator =
                getValueAnimator(expand, 300, AccelerateDecelerateInterpolator()) { progress ->
                    holder.dayOfWeekCardView.layoutParams.height =
                        (holder.originalHeight + (holder.expandedHeight - holder.originalHeight) * progress).toInt()

                    holder.dayOfWeekCardView.requestLayout()
                }
            animator.start()
        }

        private inline fun getValueAnimator(
            forward: Boolean = true, duration: Long, interpolator: TimeInterpolator,
            crossinline updateListener: (progress: Float) -> Unit
        ): ValueAnimator {
            val a =
                if (forward) ValueAnimator.ofFloat(0f, 1f)
                else ValueAnimator.ofFloat(1f, 0f)
            a.addUpdateListener { updateListener(it.animatedValue as Float) }
            a.duration = duration
            a.interpolator = interpolator
            return a
        }
    }
}

class ScheduleRecyclerAdapter(
    private val lessonsList: MutableList<String>,
    private val itemCount: Int,
    private val currentLesson: Int,
    private val thisActivity: FragmentActivity
) :
    RecyclerView.Adapter<ScheduleRecyclerAdapter.ScheduleFragmentHolder>() {

    class ScheduleFragmentHolder(
        itemView: View,
        private val currentLesson: Int
    ) : RecyclerView.ViewHolder(itemView) {

        private val lessonNumber = itemView.findViewById<TextView>(R.id.lessonNumber)
        private val lessonTime = itemView.findViewById<TextView>(R.id.lessonTime)
        private val lessonName = itemView.findViewById<TextView>(R.id.lessonName)

        fun bind(position: Int, lesson: String) {
            lessonNumber.text = (position + 1).toString()
            lessonName.text = lesson

            lessonTime.text = when (position) {
                0 -> "9:00\n10:35"
                1 -> "10:45\n12:20"
                2 -> "13:00\n14:35"
                3 -> "14:45\n16:20"
                4 -> "16:25\n18:00"
                5 -> "18:05\n19:40"
                6 -> "19:40\n21:20"
                else -> ""
            }

            if (currentLesson == position + 1) {
                lessonNumber.setBackgroundResource(R.drawable.circle2)
                lessonNumber.setTextColor(Color.WHITE)
            } else {
                lessonNumber.setBackgroundResource(R.drawable.circle)
                lessonNumber.setTextColor(lessonTime.textColors)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleFragmentHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.lessons_rec_item, parent, false)

        return ScheduleFragmentHolder(item, currentLesson)
    }

    override fun onBindViewHolder(holder: ScheduleFragmentHolder, position: Int) {
        try{
            holder.bind(position, lessonsList[position])
        }
        catch (e: Exception){
            thisActivity.finish()
        }
    }

    override fun getItemCount() = itemCount
}