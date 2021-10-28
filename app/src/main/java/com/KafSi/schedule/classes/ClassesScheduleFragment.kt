package com.KafSi.schedule.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.KafSi.schedule.R
import java.net.URL
import java.nio.charset.Charset

class ClassesScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}