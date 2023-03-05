package com.example.audiostore

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.R.attr.bitmap
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var rv: RecyclerView
    lateinit var adapter: AudioAdapter
    lateinit var button: Button
    lateinit var urlEt: EditText
    lateinit var button_start_recording: Button
    lateinit var button_stop_recording: Button
    lateinit var button_pause_recording: Button
    private var STORAGE: Int = 1000
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private val list: ArrayList<AudioModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv = findViewById(R.id.rv)
        button = findViewById(R.id.addButton)
        button_start_recording = findViewById(R.id.button_start_recording)
        button_stop_recording = findViewById(R.id.button_stop_recording)
        urlEt = findViewById(R.id.urlEt)
        val cw = ContextWrapper(applicationContext)
        val directory: File = cw.getDir("imageDir", MODE_PRIVATE)
        val file = File(directory, "UniqueFileName" + ".jpg")
        if (!file.exists()) {
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
//        val outputt = cw.getDir("image", MODE_PRIVATE)
        output = file.absolutePath.toString()
//        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
        rv.layoutManager = LinearLayoutManager(this)
        list.add(AudioModel("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"))
        list.add(AudioModel("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"))
        list.add(AudioModel(output!!))
//        list.add(AudioModel("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3"))
//        list.add(AudioModel("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3"))
//        list.add(AudioModel("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3"))
//        list.add(AudioModel(output.toString()))
        adapter = AudioAdapter(this, list)
        rv.adapter = adapter
        button.setOnClickListener {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED) {
                    requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE),
                    STORAGE)
                } else {
                    startDownloading()
                }
            }
            else {
                startDownloading()
            }
        }
        button_start_recording.setOnClickListener {
                startRecording()

        }

        button_stop_recording.setOnClickListener{
            stopRecording()
        }

    }
    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startDownloading() {
        val url = urlEt.text.toString()
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
        )
        request.setTitle("Скачено!")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}")
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        list.add(AudioModel(songUrl = "${urlEt.text.toString()}"))
        adapter = AudioAdapter(this, list)
        rv.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownloading()
                }
                else {
                    Toast.makeText(this,"Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
            0 -> {
                startRecording()
            }
        }
    }
}
