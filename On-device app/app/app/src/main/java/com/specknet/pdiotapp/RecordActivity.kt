package com.specknet.pdiotapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.android.synthetic.main.activity_record.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI
import java.util.jar.Manifest

class RecordActivity : AppCompatActivity() {

    private lateinit var tabLayout:TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var sharebtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "RecordActivity"



       tabLayout = findViewById(R.id.tabLayout)
       viewPager =  findViewById(R.id.real_graph)
       viewPager.adapter = RecordAdapter(this)

       TabLayoutMediator(tabLayout,viewPager){
           tab, index ->
           tab.text = when(index){
               0 -> {"Weekly"}
               1 -> {"Monthly"}
               else -> {"Monthly"}
           }
       }.attach()


        sharebtn = findViewById(R.id.record_share)

        val url= "http://youtu.be/5GMwP9ppjdk"

        sharebtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra("share this",url)
            val chooser = Intent.createChooser(intent,"Share using...")
            startActivity(chooser)
        }


        val savebtn = findViewById<Button>(R.id.record_save)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        val view = findViewById<ViewPager2>(R.id.real_graph)
        savebtn.setOnClickListener{
            val bitmap = getImageOfView(view)
            if(bitmap!=null){
                saveToStorage(bitmap)
            }
        }

    }

    private fun saveToStorage(bitmap: Bitmap) {
        val imageName = "noob_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            this.contentResolver?.also {resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME,imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
                }
                val imageUri : Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        }
        else{
            val imageDirectory = (Environment.DIRECTORY_PICTURES)
            val image = File(imageDirectory,imageName)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,it)
            Toast.makeText(this,"Successfully Save!",Toast.LENGTH_LONG).show()
        }

    }

    private fun getImageOfView(view: ViewPager2): Bitmap? {
        var image : Bitmap? = null
        try {
            image = Bitmap.createBitmap(view.measuredWidth,view.measuredHeight,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            view.draw(canvas)
        } catch (e:Exception){
            Log.e("No graph","cannot capture")
        }
        return image

    }


}