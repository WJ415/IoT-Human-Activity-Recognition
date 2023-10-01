package com.specknet.pdiotapp

import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpGet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.runBlocking

class MonthlyFragment : Fragment() {
    lateinit var barChart: BarChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
        // Inflate the layout for this fragment
        val bind=  inflater.inflate(R.layout.fragment_monthly, container, false)
        barChart = bind.findViewById(R.id.monthly_barChart)


        val barstats = categoriseThree(getStatsMonth())

        val list_static: ArrayList<BarEntry> = ArrayList()



        list_static.add(BarEntry(1f, barstats[0].toFloat()*100))

        val list_light: ArrayList<BarEntry> = ArrayList()
        list_light.add(BarEntry(3f, barstats[1].toFloat()*100))

        val list_intense: ArrayList<BarEntry> = ArrayList()
        list_intense.add(BarEntry(5f, barstats[2].toFloat()*100))




        val barDataSet = BarDataSet(list_static,"%static")
        val barDataSet1 = BarDataSet(list_light,"%light activity")
        val barDataSet2 = BarDataSet(list_intense,"%intense activity")

        barDataSet.color = resources.getColor((R.color.blue))
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 15F

        barDataSet1.color = resources.getColor((R.color.barchart))
        barDataSet1.valueTextColor = Color.BLACK
        barDataSet1.valueTextSize = 15F

        barDataSet2.color = resources.getColor((R.color.barchart1))
        barDataSet2.valueTextColor = Color.BLACK
        barDataSet2.valueTextSize = 15F



        val barData = BarData(barDataSet,barDataSet1,barDataSet2)


//        barChart.setFitBars(true)
        barChart.data = barData
        barChart.description.text = "Bar chart"
//        barChart.animateY(3000)

        barChart.setBackgroundColor(resources.getColor(R.color.bg))
        return barChart

    }

    fun getStats(timestamp: Long): List<Int> {
        val url =(LoginActivity.baseUrl + "/getStats?"+ "ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC+"&timestamp="+timestamp.toString())
        val x = doRequest(url)
        var stat= x.split(",").map { it.toInt() }
        return stat
    }


    fun getStatsMonth(): List<Float> {
        val MonthInMs: Long = 24L * 60L * 60L * 1000L * 30L
        val currentTimestamp = System.currentTimeMillis()
        println("currentTimestamp: "+currentTimestamp)
        val ago = currentTimestamp - MonthInMs
        println("ago month: "+ago.toString())
        val stat = getStats(ago)
        val sum = stat.sum()
        Log.d("#####SUM ", sum.toString())
        var result = stat.map { it.toFloat() }.toMutableList()
        for (i in 0 until 14){
            result[i]= (stat.get(i).toFloat())/sum
            Log.d("#####Each ratio ", stat.get(i).toString())
        }
        return result
    }

    fun categoriseThree(result: List<Float>):List<Float>{
        val list_to_return = mutableListOf<Float>()
        list_to_return.add(0.0f)
        list_to_return.add(0.0f)
        list_to_return.add(0.0f)
        list_to_return[0]+= result[0]
        list_to_return[0]+= result[1]
        list_to_return[0]+= result[2]
        list_to_return[0]+= result[3]
        list_to_return[0]+= result[4]
        list_to_return[0]+= result[5]
        list_to_return[0]+= result[6]
        list_to_return[0]+= result[7]
        list_to_return[1]+= result[8]
        list_to_return[1]+= result[12]
        list_to_return[2]+= result[9]
        list_to_return[2]+= result[10]
        list_to_return[2]+= result[11]
        list_to_return[2]+= result[13]
        return  list_to_return
    }

    fun doRequest(url:String) = runBlocking {
        val (_, _, result) = Fuel.get(url).awaitStringResponse()
        result
    }
}