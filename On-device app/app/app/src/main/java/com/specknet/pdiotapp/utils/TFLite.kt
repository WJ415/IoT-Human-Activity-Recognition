package com.specknet.pdiotapp.utils

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.ArrayList

class TFLite(assetManager: AssetManager, modelPath: String, labelPath: String, private val outputClasses: Int, inputTensorLength: Int) {

    private var interpreter: Interpreter
    private var labelList: List<String>
    var length = 50
    var width = 6
    private val data: Array<Array<FloatArray>> = arrayOf(Array(length){ FloatArray(width)})

    init {
        interpreter = Interpreter(this.loadModelFile(assetManager, modelPath), Interpreter.Options())
        labelList = this.loadLabelList(assetManager, labelPath)
    }

    @Throws (IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws (IOException::class)
    private fun loadLabelList(assetManager: AssetManager, labelPath: String) : List<String> {
        val labelList: MutableList<String> = ArrayList()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        while (true){
            val line = reader.readLine()
            if (line != null){
                labelList.add(line)
            } else {
                break
            }
        }
        reader.close()
        return labelList
    }

    fun classify(): Array<FloatArray>{
        val output = arrayOf(FloatArray(outputClasses))
        interpreter.run(data, output)
        return output
    }

    fun getTrdLabel(snd: Float,predictions: Array<FloatArray>): Pair<String,Float>{
        var max = Float.MIN_VALUE
        var maxIdx = 0
        for (i in labelList.indices){
            if(predictions[0][i] > max && predictions[0][i] < snd) {
                max = predictions[0][i]
                maxIdx = i
            }
        }
        return labelList[maxIdx] to max
    }

    fun getSndLabel(fst: Float,predictions: Array<FloatArray>): Pair<String,Float>{
        var max = Float.MIN_VALUE
        var maxIdx = 0
        for (i in labelList.indices){
            if(predictions[0][i] > max && predictions[0][i] < fst) {
                max = predictions[0][i]
                maxIdx = i
            }
        }
        return labelList[maxIdx] to max
    }

    fun getFstLabel(predictions: Array<FloatArray>): Pair<String,Float>{
        var max = Float.MIN_VALUE
        var maxIdx = 0
        for (i in labelList.indices){
            if(predictions[0][i] > max) {
                max = predictions[0][i]
                maxIdx = i
            }
        }
        return labelList[maxIdx] to max
    }

    fun addToBuffer(accelX: Float,accelY: Float,accelZ: Float,gyroX: Float,gyroY: Float,gyroZ: Float){
        // move every row down one
        for (i in 0 until length - 1) {
            for (j in 0 until width) {
                data[0][i][j] = data[0][i+1][j]
            }
        }
        // add the new data to the first row
        data[0][length-1][0] = accelX
        data[0][length-1][1] = accelY
        data[0][length-1][2] = accelZ
        data[0][length-1][3] = gyroX
        data[0][length-1][4] = gyroY
        data[0][length-1][5] = gyroZ
    }

}