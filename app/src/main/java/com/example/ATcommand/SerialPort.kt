package com.example.ATcommand

import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

class SerialPort {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    var strBuilder =StringBuilder()
    var readThread: Thread? = null
    public fun OpenStream(device: File){
        //Permission Check
        if (!device.canRead() || !device.canWrite()) {
            Log.d("ATcommand", "Can't readFile")
        }else{
            Log.d("ATcommand", "Can readFile")
            inputStream = device.inputStream()
            outputStream = device.outputStream()
        }
    }

    fun CloseStream(){
        if(inputStream != null
            ||outputStream != null){
                CoroutineScope(Main).launch {
                    val stopThread = async { readThread?.interrupt()}
                    stopThread.await()
                    inputStream?.close()
                    outputStream?.close()
                }
        }
    }
    fun StartRxThread(textView: TextView, scrollView: ScrollView ,ctread: CoroutineScope) {
        if(inputStream == null){
            Log.e("SerialExam","Can't open inputstream")
            return
        }
        readThread = thread(start = true) {
            Log.d("SerialExam","start thread")
            while(inputStream != null){
                try{
                    var buffer = ByteArray(1024)
                        val size = inputStream?.read(buffer)
                    ctread.launch {
                        textView.text = OnReceiveData(buffer, size ?: 0)
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }
                }catch (e: IOException){e.printStackTrace()}
            }
        }
    }
    //send data
    fun SendData(data: String){
        try {
            outputStream?.write(data.encodeToByteArray())
        }catch (e: IOException){e.printStackTrace()}
    }

    // make string
    fun OnReceiveData(buffer: ByteArray, size: Int): StringBuilder? {
        if(size<1) return null
        for (i in 0 until size){
            strBuilder.append(String.format("%c", buffer[i].toInt()))
        }
        strBuilder.append("\n")
        Log.d("ATcommand",strBuilder.toString())
        return strBuilder
    }
    fun clearString(): java.lang.StringBuilder{
        strBuilder.clear()
        return strBuilder
    }


}