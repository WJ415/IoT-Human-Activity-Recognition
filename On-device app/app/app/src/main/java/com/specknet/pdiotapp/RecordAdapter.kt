package com.specknet.pdiotapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class RecordAdapter(fragmentActivity: FragmentActivity):FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount()=2

    override fun createFragment(position: Int): Fragment {
       return when(position){
           0 -> {WeeklyFragment()}
           1 -> {MonthlyFragment()}
           else -> {WeeklyFragment()}
       }
    }


}