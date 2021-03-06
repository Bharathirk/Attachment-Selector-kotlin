package com.example.admin.mvpinitialprojectsetupkotlin.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

class HomePagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    private var fragmentList = ArrayList<Fragment>()

    fun setFragmentList(fragmentList: ArrayList<Fragment>) {
        this.fragmentList = fragmentList
        notifyDataSetChanged()

    }

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "Images"
            1 -> return "Videos"
            2 -> return "Audio"
            3 -> return "Pdf"
            4 -> return "doc/docx"
        }
        return ""
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}