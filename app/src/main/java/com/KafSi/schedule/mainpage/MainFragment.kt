package com.KafSi.schedule.mainpage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.KafSi.schedule.HostViewActivity
import com.KafSi.schedule.PublicData
import com.KafSi.schedule.R
import com.KafSi.schedule.SettingsActivity
import com.KafSi.schedule.teachers.FacultySelectActivity

class MainFragment : Fragment() {

    private lateinit var bakButton: Button
    private lateinit var magButton: Button
    private lateinit var extraButton1: Button
    private lateinit var extraButton2: Button
    private lateinit var settingsButton: Button
    private lateinit var teachButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bakButton = view.findViewById(R.id.bakButton)
        magButton = view.findViewById(R.id.magButton)
        extraButton1 = view.findViewById(R.id.extraButton1)
        extraButton2 = view.findViewById(R.id.extraButton2)
        settingsButton = view.findViewById(R.id.settingsButton)
        teachButton = view.findViewById(R.id.teachButton)

        val buttonMutableList = mutableListOf(
            bakButton, magButton,
            extraButton1, extraButton2
        )

        /**анимация прозрачности*/
        //val animation: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.alpha)

        /**кнопки главного меню*/
        for (i in buttonMutableList) {
            i.setOnClickListener {
                //i.startAnimation(animation)

                val intent = Intent(requireContext(), HostViewActivity::class.java)
                intent.putExtra("data", buttonMutableList.indexOf(i))
                startActivity(intent)
            }
        }

        /**кнопка настроек*/
        settingsButton.setOnClickListener {
            //settingsButton.startAnimation(animation)

            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        /**кнопка учителей*/
        teachButton.setOnClickListener {
            //teachButton.startAnimation(animation)

            PublicData.catalog = ""
            //val intent = Intent(requireContext(), TeachersActivity::class.java)
            val intent = Intent(requireContext(), FacultySelectActivity::class.java)
            intent.putExtra("data", 4)
            startActivity(intent)
        }
    }
}