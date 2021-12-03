package com.example.ATcommand


import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    val scope = CoroutineScope(Main)
    val openStream = SerialPort()
    var device = File("/dev/smd11")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val respose = findViewById<TextView>(R.id.response_tv)
        val scrollview = findViewById<ScrollView>(R.id.response)
        val connectport = findViewById<TextView>(R.id.connect_tv)

        //setenforce 0
//        try {
//            val su = Runtime.getRuntime().exec("su")
//            val outputStream = DataOutputStream(su.outputStream)
//            outputStream.writeBytes("setenforce 0\n")
//            outputStream.flush()
//            outputStream.writeBytes("exit\n")
//            outputStream.flush()
//            try {
//                su.waitFor()
//                if (0 == su.exitValue()) {
//                    Toast.makeText(this, "슈퍼유저입니다.", Toast.LENGTH_SHORT).show()
//                }else{
//                    Toast.makeText(this, "슈퍼유저 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: IOException){
//                Toast.makeText(this,"슈퍼유저 권한이 필요합니다.",Toast.LENGTH_SHORT).show()
//                Log.d("SerialExam", "ex: $e")
//            }
//
//        } catch (e: IOException) {
//            Toast.makeText(this,"슈퍼유저 권한이 필요합니다.",Toast.LENGTH_SHORT).show()
//            Log.d("SerialExam", "ex: $e")
//        }

        // connect port
        findViewById<Button>(R.id.connect_btn).setOnClickListener {
            var path = findViewById<EditText>(R.id.port).text.toString()
            Log.d("ATcommand", path)
            device = File(path)
            openStream.OpenStream(device)
            if(openStream.inputStream !=null
                || openStream.inputStream != null){
                openStream.StartRxThread(respose, scrollview ,scope)
                connectport.text = path
            }
        }
        //diconnect port
        findViewById<Button>(R.id.disconnect_btn).setOnClickListener {
            openStream.CloseStream()
            connectport.text = "No Connect"

        }

        // send 버튼 눌렸을때
        findViewById<Button>(R.id.send_btn).setOnClickListener {
            val text = findViewById<EditText>(R.id.atcommand).text.toString()
            if (openStream.outputStream != null) {
                openStream.SendData(text + '\r')
                Log.d("ATcommand", "Send" + text)
            }
        }

        //clear 버튼 눌렀을때
        findViewById<Button>(R.id.clear_btn).setOnClickListener {
            respose.text = openStream.clearString()
        }

        //save button
        findViewById<Button>(R.id.save_btn).setOnClickListener {
            val filename = LocalDateTime.now().toString() + ".txt"
            val dir2 = File(this.getExternalFilesDir(null)?.path + "/LOG")
            if(!dir2.exists()) dir2.mkdirs()
            val fullpath = dir2.path + "/" + filename
            Log.d("SerialExam", fullpath)
            val writer = FileWriter(fullpath)
            val buffer = BufferedWriter(writer)
            buffer.write(openStream.strBuilder.toString())
            buffer.close()
            writer.close()
            Toast.makeText(this,fullpath, Toast.LENGTH_SHORT).show()
        }

        // 키보드 엔터입력시 동작
        findViewById<EditText>(R.id.atcommand).setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if(actionId == EditorInfo.IME_ACTION_GO){
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                handled = true
            }
            val text = findViewById<EditText>(R.id.atcommand).text.toString()
            openStream.SendData(text+'\r')
            Log.d("ATcommand","Send"+text)
            handled
        }
    }
}