package com.KafSi.schedule.mainpage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.KafSi.schedule.fragments.FavoriteListFragment

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0)
            return MainFragment()

        return FavoriteListFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {

        if (position == 0)
            return "Главная"
        return "Избранное"
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}