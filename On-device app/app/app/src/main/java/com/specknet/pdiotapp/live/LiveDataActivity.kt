package com.specknet.pdiotapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.android.volley.toolbox.HttpResponse
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpGet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.specknet.pdiotapp.LoginActivity

import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.TFLite
import com.specknet.pdiotapp.utils.ThingyLiveData
import kotlinx.android.synthetic.main.activity_connecting.view.*
import kotlinx.android.synthetic.main.activity_live_data.*
import kotlinx.android.synthetic.main.activity_live_data.view.*
import kotlinx.coroutines.runBlocking


class LiveDataActivity : AppCompatActivity() {

    private var itemList = arrayOf(
        "Walking",
        "Standing",
        "Climbing stairs",
        "Descending stairs",
        "Desk work",
        "Lying down left",
        "Lying down right",
        "Lying down back",
        "Lying down stomach",
        "Movement",
        "Running",
        "Sitting bent forward",
        "Sitting bent bakward",
        "Sitting"
    )
    private var listView: ListView? = null
    private var arrayAdapter: ArrayAdapter<String>? = null

    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData

    lateinit var allThingyData: LineData

    lateinit var respeckChart: LineChart
    lateinit var thingyChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    lateinit var looperThingy: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var respeckModel: TFLite
    lateinit var thingyModel: TFLite


            //override fun onCreate(savedInstanceState: Bundle?) {
            //super.onCreate(savedInstanceState)
            //  setContentView(R.layout.activity_live_data)

//        listView = findViewById(R.id.context_live)
//        arrayAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1,itemList)
//        listView?.adapter = arrayAdapter
//        registerForContextMenu(listView)

            //}



    override fun onCreate(savedInstanceState: Bundle?) {

//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)

        respeckModel = TFLite(assets, "Model for Respeck.tflite", "labelsR.txt", 14, 50)
        thingyModel = TFLite(assets, "Model for Thingy.tflite", "labelsT.txt", 3, 50)

        //predict_real_move.setText("")


        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            var counter = 490
            var counter2 = 49
            var stepcount_interval = 0
            var stepcount = 0
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action


                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    val accelX = x
                    val accelY = y
                    val accelZ = z
                    val gyroX = liveData.gyro.x
                    val gyroY = liveData.gyro.y
                    val gyroZ = liveData.gyro.z

                    respeckModel.addToBuffer(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
                    val predicted = respeckModel.classify()

                    val label = respeckModel.getFstLabel(predicted).first

                    if (stepcount_interval == 15){
                        stepcount+=1
                        stepcount_interval = 0
                    }else{
                        if(label == "Climbing stairs" || label == "Descending stairs"|| label == "Walking at normal speed"){
                            stepcount_interval += 1
                        }
                        if(label == "Running"){
                            stepcount_interval += 3
                        }
                    }

                    if (label == "Sitting" || label == "Desk work" || label == "Standing") {
                        // modify the respeck predictions by the predictions of Thingy
                        val thingyPredicted = thingyModel.classify()
                        predicted[0].set(0,thingyPredicted[0].get(0))
                        predicted[0].set(3,thingyPredicted[0].get(1))
                        predicted[0].set(12,thingyPredicted[0].get(2))
                        updateLabelUI(predicted,stepcount)
                    } else {
                        updateLabelUI(predicted,stepcount)
                    }


                    if (counter == 750){//per 30 seconds

                        val stats = getStats24h()
                        updateStatUI(stats)
                        counter = 1
                    }else{
                        counter += 1
                    }
                    if (counter2 == 125){//per 5 seconds
                        val fstLabel = respeckModel.getFstLabel(predicted).first
                        upload_history(fstLabel)
                        counter2 = 1
                    }else{
                        counter2 += 1
                    }

                    //Log.i("thread","#####################"+label+"#####################")
                    time += 1

                }
            }
        }

        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

        // set up the broadcast receiver
        thingyLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_THINGY_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.THINGY_LIVE_DATA) as ThingyLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    val accelX = x
                    val accelY = y
                    val accelZ = z
                    val gyroX = liveData.gyro.x
                    val gyroY = liveData.gyro.y
                    val gyroZ = liveData.gyro.z

                    thingyModel.addToBuffer(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)

                    time += 1

                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        this.registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

    }

    fun cloud_push_Thingy(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ) {
        val url =(LoginActivity.baseUrl + "/push_Thingy?"+"ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC+"&accelX=" + accelX.toString() + "&accelY=" + accelY.toString() + "&accelZ=" + accelZ.toString() + "&gyroX=" + gyroX.toString() + "&gyroY=" + gyroY.toString() + "&gyroZ=" + gyroZ.toString())
        val x = doRequest(url)
    }
    fun cloud_push_Respeck(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ) {
        val url =(LoginActivity.baseUrl + "/push_Respeck?" + "ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC+"&accelX=" + accelX.toString() + "&accelY=" + accelY.toString() + "&accelZ=" + accelZ.toString() + "&gyroX=" + gyroX.toString() + "&gyroY=" + gyroY.toString() + "&gyroZ=" + gyroZ.toString())
        val x = doRequest(url)
    }

    fun cloud_classify(): String {
        val url =(LoginActivity.baseUrl + "/getClassification?"+ "ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC)
        val x = doRequest(url)
        return x
    }

    fun upload_history(label_to_upload:String) {
        val currentTimestamp = System.currentTimeMillis()
        val url =(LoginActivity.baseUrl + "/updateHistory?"+ "ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC+"&activity="+label_to_upload+"&timestamp="+currentTimestamp.toString())
        val x = doRequest(url)
    }

    fun to_percent(num: Float):String{
        val rounded = Math.round(num * 1000.0) / 10.0
        return rounded.toString()+"%"
    }

    fun to_percent(num: Int):String{
        val rounded = Math.round(num * 1000.0) / 10.0
        return num.toString()+"%"
    }

    fun getStats(timestamp: Long): List<Int> {
        val url =(LoginActivity.baseUrl + "/getStats?"+ "ID="+LoginActivity.IDC+"&password="+LoginActivity.passwordC+"&timestamp="+timestamp.toString())
        val x = doRequest(url)
        var stat= x.split(",").map { it.toInt() }
        return stat
    }

    fun getStats24h(): List<Float> {
        val twentyFourHrInMs: Long = 24L * 60L * 60L * 1000L
        val currentTimestamp = System.currentTimeMillis()
        println("currentTimestamp: "+currentTimestamp)
        val ago24h = currentTimestamp - twentyFourHrInMs
        println("ago24h: "+ago24h.toString())
        val stat = getStats(ago24h)
        val sum = stat.sum()
        Log.d("#####SUM ", sum.toString())
        var result = stat.map { it.toFloat() }.toMutableList()
        for (i in 0 until 14){
            result[i]= (stat.get(i).toFloat())/sum
            Log.d("#####Each ratio ", stat.get(i).toString())
        }
        return result
    }


    fun updateLabelUI(result: Array<FloatArray>, step: Int){//confidence: Int?
        val Fst = respeckModel.getFstLabel(result)

        val Snd = respeckModel.getSndLabel(Fst.second,result)

        val Trd = respeckModel.getTrdLabel(Snd.second,result)

        this@LiveDataActivity.runOnUiThread(java.lang.Runnable {
            predict_real_move.setText(""+Fst.first)
            predict_real_percentage.setText(""+to_percent(Fst.second))
            predict_real_main.setText(""+step.toString()+"\n"+"Steps")

            predict_real_sub1.setText(""+to_percent(Snd.second)+"\n"+Snd.first)

            predict_real_sub2.setText(""+to_percent(Trd.second)+"\n"+Trd.first)

//            data_walking.setText(""+to_percent(result[0].get(8)))
//            data_standing.setText(""+to_percent(result[0].get(3)))
//            data_climbing.setText(""+to_percent(result[0].get(10)))
//            data_descending.setText(""+to_percent(result[0].get(11)))
//            data_desk.setText(""+to_percent(result[0].get(12)))
//            data_left.setText(""+to_percent(result[0].get(4)))
//            data_right.setText(""+to_percent(result[0].get(5)))
//            data_lying_back.setText(""+to_percent(result[0].get(7)))
//            data_lying_stomach.setText(""+to_percent(result[0].get(6)))
//            data_movement.setText(""+to_percent(result[0].get(13)))
//            data_running.setText(""+to_percent(result[0].get(9)))
//            data_sitting_forward.setText(""+to_percent(result[0].get(1)))
//            data_sitting_backward.setText(""+to_percent(result[0].get(2)))
//            data_sitting.setText(""+to_percent(result[0].get(0)))

        })
    }

    fun updateStatUI(result: List<Float>){//confidence: Int?
        this@LiveDataActivity.runOnUiThread(java.lang.Runnable {
            data_walking.setText(""+to_percent(result.get(8)))
            data_standing.setText(""+to_percent(result.get(3)))
            data_climbing.setText(""+to_percent(result.get(10)))
            data_descending.setText(""+to_percent(result.get(11)))
            data_desk.setText(""+to_percent(result.get(12)))
            data_left.setText(""+to_percent(result.get(4)))
            data_right.setText(""+to_percent(result.get(5)))
            data_lying_back.setText(""+to_percent(result.get(7)))
            data_lying_stomach.setText(""+to_percent(result.get(6)))
            data_movement.setText(""+to_percent(result.get(13)))
            data_running.setText(""+to_percent(result.get(9)))
            data_sitting_forward.setText(""+to_percent(result.get(1)))
            data_sitting_backward.setText(""+to_percent(result.get(2)))
            data_sitting.setText(""+to_percent(result.get(0)))

        })
    }

    fun doRequest(url:String) = runBlocking {
        val (_, _, result) = Fuel.get(url).awaitStringResponse()
        result
    }

}

























