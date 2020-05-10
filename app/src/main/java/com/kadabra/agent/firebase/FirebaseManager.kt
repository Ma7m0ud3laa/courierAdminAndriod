package com.kadabra.agent.firebase

import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kadabra.agent.model.Courier
import com.kadabra.agent.model.Task
import com.kadabra.agent.model.location
import com.kadabra.agent.utilities.AppConstants


object FirebaseManager {

    //region Members
    private var TAG = "FirebaseManager"
    private lateinit var dbCourier: DatabaseReference
    private lateinit var dbCourierTaskHistory: DatabaseReference

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var firebaseAuthListener: FirebaseAuth.AuthStateListener? = null
    private var fireBaseUser: FirebaseUser? = null
    private var dbNameCourier = "courier"
    private var dbNameTaskHistory = "task_history"
    private var tasksImagesFolderName = "task_images"
    private var tasksRecordsFolderName = "task_records"
    lateinit var mImageStorage: StorageReference
    lateinit var mAudioStorage: StorageReference

    private var courier = Courier()
    var exception = ""
    var token = ""


    //endregion

    //region Helper Functions
    interface IFbOperation {
        fun onSuccess(code: Int)
        fun onFailure(message: String)
    }

    fun setUpFirebase() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            token = instanceIdResult.token
            Log.d("Token", token)
        }
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbCourier = firebaseDatabase.getReference(dbNameCourier)
        dbCourierTaskHistory = firebaseDatabase.getReference(dbNameTaskHistory)
        mImageStorage = FirebaseStorage.getInstance().getReference(tasksImagesFolderName)
        mAudioStorage = FirebaseStorage.getInstance().getReference(tasksRecordsFolderName)

    }


    fun getAllCouriers(completion: (success: Boolean, data: ArrayList<Courier>?) -> Unit) {

        val query = dbCourier
        dbCourier.keepSynced(true)
        var courier = Courier()


        var valueListener = query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                completion(false, null)
            }


            override fun onDataChange(dataSnapshot: DataSnapshot) {
                AppConstants.ALL_COURIERS_FIREBASE.clear()
                for (currentCourier in dataSnapshot.children) {
                    var courier = currentCourier.getValue(Courier::class.java)!!
                    courier.CourierId = currentCourier.key!!.toInt()

//                    if (AppConstants.ALL_COURIERS_FIREBASE.find { it.CourierId == courier.CourierId } == null&&courier.isActive)
                    if (courier.isActive)
                        AppConstants.ALL_COURIERS_FIREBASE.add(courier)


                }
                if (AppConstants.ALL_COURIERS_FIREBASE.size > 0)
                    completion(true, AppConstants.ALL_COURIERS_FIREBASE)
                else
                    completion(false, null)

            }

        })

        dbCourier.addListenerForSingleValueEvent(valueListener)
    }

    fun getCurrentCourierLocation(
        courieId: String,
        completion: (success: Boolean, data: location?) -> Unit
    ) {

        val query = dbCourier.child(courieId).child("location")
        dbCourier.keepSynced(true)

        var valueListener = query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                completion(false, null)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (currentTask in dataSnapshot.children) {
                    try {
                        var location = dataSnapshot.getValue(location::class.java)!!
                        if (!location.lat.isNullOrEmpty() && !location.long.isNullOrEmpty()) {
                            AppConstants.CurrentCourierLocation = location!!
                            completion(true, location)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, ex.message)
                    }
                }
            }

        })

        dbCourier.addListenerForSingleValueEvent(valueListener)
    }

    fun getCurrentActiveTask(courieId: String, listener: IFbOperation) {
        var task = Task()
        val query = dbCourierTaskHistory
        dbCourierTaskHistory.keepSynced(true)
        var valueEventListener = query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                listener.onFailure(p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (currentTask in dataSnapshot.children) {
                    task = currentTask.getValue(Task::class.java)!!
                    if (task.isActive && task.CourierID == courieId.toInt() && !task.TaskId.isNullOrEmpty()) {
                        task.TaskId = currentTask.key!!
                        listener.onSuccess(1)
                    }
                }
            }

        })

        dbCourierTaskHistory.addListenerForSingleValueEvent(valueEventListener)

    }

    fun uploadRecord(uri: Uri, taskId: String, completion: (success: Boolean) -> Unit) {
        Log.d(TAG, "uploadRecord")
        mAudioStorage.child(taskId).putFile(uri).addOnSuccessListener { completion(true) }
            .addOnFailureListener { completion(false) }

    }

    fun getTaskRecord(taskId:String, completion: (success: Boolean,data: Uri?) -> Unit)
    {

        mAudioStorage.child(taskId).downloadUrl.addOnSuccessListener {
            completion(true,it)
        }.addOnFailureListener { completion(false,null) }
    }

    fun getTaskImage(taskId:String, completion: (success: Boolean,data: Uri?) -> Unit)
    {
        Log.d(TAG, "getTaskImage")
        mImageStorage.child("$taskId.jpeg").downloadUrl.addOnSuccessListener {
            Log.d(TAG, it.toString())
            completion(true,it)
        }.addOnFailureListener { completion(false,null) }
    }



    //endregion


}