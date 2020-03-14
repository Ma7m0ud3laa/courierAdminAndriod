package com.kadabra.agent.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
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

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbCourier = firebaseDatabase.getReference(dbNameCourier)
        dbCourierTaskHistory = firebaseDatabase.getReference(dbNameTaskHistory)

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

                    if (AppConstants.ALL_COURIERS_FIREBASE.find { it.CourierId == courier.CourierId } == null)
                        AppConstants.ALL_COURIERS_FIREBASE.add(courier)


                }
                if(AppConstants.ALL_COURIERS_FIREBASE.size>0)
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
                }
                catch(ex:Exception)
                {}
                }
            }

        })

        dbCourier.addListenerForSingleValueEvent(valueListener)
    }


    //endregion


}