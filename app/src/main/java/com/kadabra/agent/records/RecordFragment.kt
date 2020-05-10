package com.kadabra.agent.records

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.kadabra.agent.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class RecordFragment : Fragment(),
    View.OnClickListener {
    //    private var navController: NavController? = null
    private var listBtn: ImageButton? = null
    private var recordBtn: ImageButton? = null
    private var filenameText: TextView? = null
    private var isRecording = false
    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_CODE = 21
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null
    private var timer: Chronometer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        //Intitialize Variables
//        navController = Navigation.findNavController(view)
        listBtn = view.findViewById(R.id.record_list_btn)
        recordBtn = view.findViewById(R.id.record_btn)
        timer = view.findViewById(R.id.record_timer)
        filenameText = view.findViewById(R.id.record_filename)
        /* Setting up on click listener
           - Class must implement 'View.OnClickListener' and override 'onClick' method
         */listBtn?.setOnClickListener(this)
        recordBtn?.setOnClickListener(this)
    }

    override fun onClick(v: View) { /*  Check, which button is pressed and do the task accordingly
        */
        when (v.id) {
            R.id.record_list_btn ->  /*
                Navigation Controller
                Part of Android Jetpack, used for navigation between both fragments
                 */if (isRecording) {
                val alertDialog =
                    AlertDialog.Builder(context)
                alertDialog.setPositiveButton(
                    "OKAY"
                ) { dialog, which ->
                    //                    navController.navigate(R.id.action_recordFragment_to_audioListFragment)
                    isRecording = false
                }
                alertDialog.setNegativeButton("CANCEL", null)
                alertDialog.setTitle("Audio Still recording")
                alertDialog.setMessage("Are you sure, you want to stop the recording?")
                alertDialog.create().show()
            } else {
//                navController.navigate(R.id.action_recordFragment_to_audioListFragment)
            }
            R.id.record_btn -> if (isRecording) { //Stop Recording
                stopRecording()
                // Change button image and set Recording state to false
                recordBtn!!.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.record_btn_stopped,
                        null
                    )
                )
                isRecording = false
            } else { //Check permission to record audio
                if (checkPermissions()) { //Start Recording
                    startRecording()
                    // Change button image and set Recording state to false
                    recordBtn!!.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.record_btn_recording,
                            null
                        )
                    )
                    isRecording = true
                }
            }
        }
    }

    private fun stopRecording() { //Stop Timer, very obvious
        timer!!.stop()
        //Change text on page to file saved
        filenameText!!.text = "Recording Stopped, File Saved : $recordFile"
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        mediaRecorder = null
    }

    private fun startRecording() { //Start timer from 0
        timer!!.base = SystemClock.elapsedRealtime()
        timer!!.start()
        //Get app external directory path
        val recordPath = activity!!.getExternalFilesDir("/")?.absolutePath
        //Get current date and time
        val formatter =
            SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA)
        val now = Date()
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".3gp"
        filenameText!!.text = "Recording, File Name : $recordFile"
        //Setup Media Recorder for recording
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile("$recordPath/$recordFile")
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Start Recording
        mediaRecorder!!.start()
    }

    private fun checkPermissions(): Boolean { //Check permission
        return if (ActivityCompat.checkSelfPermission(
                context!!,
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) { //Permission Granted
            true
        } else { //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(recordPermission),
                PERMISSION_CODE
            )
            false
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stopRecording()
        }
    }
}

