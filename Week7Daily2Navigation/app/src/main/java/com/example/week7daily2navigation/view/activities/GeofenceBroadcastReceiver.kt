package com.example.week7daily2navigation.view.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.week7daily2navigation.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE_EVENT", "Error code: " + geofencingEvent.errorCode)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Send notification and log the transition details.
            sendNotification(context!!, geofencingEvent.triggeringLocation.latitude,  geofencingEvent.triggeringLocation.longitude)
            Log.d("GEOFENCE_EVENT", "Transition Event Occurred")
        } else {
            // Log the error.
            Log.e("GEOFENCE_EVENT", "Wrong Geofence Transition")
        }
    }

    private fun sendNotification(context: Context, lat : Double, lng : Double){
        val intent = Intent(context, MapsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)
        val builder = NotificationCompat.Builder(context, "geofence_event")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        builder.setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("You have arrived at at $lat,$lng")
            .setContentText("Click here to see your surroundings")
            .setContentIntent(pendingIntent)

        manager.notify(0, builder.build())
    }
}