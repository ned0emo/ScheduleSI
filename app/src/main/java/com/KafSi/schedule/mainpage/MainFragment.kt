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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val settingsButton: Button = view.findViewById(R.id.settingsButton)
        val teachButton: Button = view.findViewById(R.id.teachButton)

        val buttonMutableList = mutableListOf<Button>(
            view.findViewById(R.id.bakButton), view.findViewById(R.id.magButton),
            view.findViewById(R.id.extraButton1), view.findViewById(R.id.extraButton2),
            view.findViewById(R.id.classesButton)
        )

        /**анимация прозрачности*/
        //val animation: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.alpha)

        /**кнопки главного меню*/
        for (i in buttonMutableList) {
            i.setOnClickListener {
                val intent = Intent(requireContext(), HostViewActivity::class.java)
                intent.putExtra("data", buttonMutableList.indexOf(i))
                startActivity(intent)
            }
        }

        /**кнопка настроек*/
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        /**кнопка учителей*/
        teachButton.setOnClickListener {
            //PublicData.catalog = ""
            //val intent = Intent(requireContext(), TeachersActivity::class.java)
            val intent = Intent(requireContext(), FacultySelectActivity::class.java)
            intent.putExtra("data", 4)
            startActivity(intent)
        }
    }
}