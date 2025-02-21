package com.singaporetech.eod

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

/**
 * The main underlying service that controls the state of the game.
 * - collect sensor data and send these updates to GameState (Model) in game core component
 * - constantly determine time to spawn bugs
 * - both a Started (collect sensor data) and Bound Service (update UI continuously)
 * - this background Service will try to persist until the app is explicitly closed
 * - QNS: when will it be killed?
 * - QNS: what happens when it is killed?
 *
 * TODO GameStateServiceModel layer below to focus on the logic.
 */
class GameStateService: Service(), SensorEventListener, CoroutineScope by MainScope() {
    companion object {
        private val TAG = GameStateService::class.simpleName

        // SERVICES 6.1: create vars to manage notifications
        private const val NOTIFICATION_CHANNEL_ID = "EOD CHANNEL"
        private const val NOTIFY_ID = 888
        private const val PENDINGINTENT_ID = 1

        // broadcast uris
        const val BROADCAST_ACTION = "com.singaporetech.eod.STEP_COUNT"
        const val STEP_KEY = "com.singaporetech.eod.STEP_KEY"
    }

    // - add var for NotificationManager
    private lateinit var notificationManager: NotificationManager

    // a raw thread for bg work
    private lateinit var bgThread: Thread

    // SENSORS 0: create vars to interface with hardware sensors
    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null

    /**
     * GameStateBinder class to "contain" this service
     * This is part of the boilerplate for Bound Service. Client can use this object to communicate
     * with the service. This approach uses the simple Binder class since clients are also in this
     * app/process. For this service to be used by other apps, use Messenger or AIDL for IPC.
     * - extends Binder()
     * - init an IBinder interface to offer a handle to this class
     * - return this service for clients to access public service methods
     */
    inner class GameStateBinder : Binder() {
        fun getService(): GameStateService = this@GameStateService
    }
    private val binder = GameStateBinder()

    /**
     * onBind to return the binder interface
     * Part of the boilerplate for Bound Service
     *
     * @param intent to hold any info from caller
     * @return IBinder to obtain a handle to the service class
     */
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * onCreate Service lifecycle to initialize various things
     * - get handle to SensorManager from a System Service
     * - get list of available sensors from the sensorManager
     * - get handle to step detector from sensorManager
     * - init NotificationManager and NotificationChannel
     */
    override fun onCreate() {
        super.onCreate()

        // get handle to sensor device and list all sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // get list of all available sensors, along with some capability data
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        var sensorsStr = "available sensors:"
        for (sensor in sensors) {
            sensorsStr += "\n$sensor.name madeBy=$sensor.vendor v=$sensor.version " +
                    "minDelay=$sensor.minDelay maxRange=$sensor.maximumRange power=$sensor.power"
        }
        Log.i(TAG, sensorsStr)

        // get handles only for required sensors
        // - if you want to show app only if user has the sensor, then do <uses-feature> in manifest
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepDetector == null) Log.e(TAG, "No step sensors on device!")

        // obtain and init notification manager with a channel
        // - notification channels introduced in Android Oreo
        // - need to initialize a channel before creating actual notifications
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(
                    NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            getString(R.string.channel_name),
                            NotificationManager.IMPORTANCE_HIGH
                    )
            )
    }

    /**
     * onStartCommand which defines what the service will actually do
     * - register this class as a SensorEventListener (extend this Service) using sensorManager
     * - add a thread to manage spawning of bugs based on a countdown
     * - spawn bug when GameState.i().isCanNotify() && !GameState.i().isAppActive()
     * - create pending intent to launch AndroidLauncher
     * - use NotificationCompat.Builder to make notification
     *
     * @param intent to hold any info from caller
     * @param flags to show more data about how this was started (e.g., REDELIVERY)
     * @param startId id of this started instance
     * @return an int that controls what happens when this service is auto killed
     *         , e.g., sticky or not (see https://goo.gl/shXLoy)
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Registering listener to listen for sensor events.
        // - note that the DELAY is the max, and system normally lower
        // - don't just use SENSOR_DELAY_FASTEST (0us) as it uses max power
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_GAME)


        // Legacy code: see the use of traw threads to control the spawn timer
        // O.M.G. a raw java thread
        /*
        bgThread = Thread( Runnable {
            gameloop()
        })
        bgThread.start()
        */

        launch {
            gameloop()
        }

        // return appropriate flag to indicate what happens when killed
        // QNS: what are the other flags?
        return START_STICKY
    }

    /**
     * The gameloop which will update the game infinitely.
     */
    suspend fun gameloop() = withContext(Dispatchers.Default) {
        while (true) {
            // TODO RECEIVERS 1: to communicate data to other apps
            // - broadcasting steps to the (Android device) world
            // - note that emulator can't view real steps
            GameState.i().incSteps(1)
            sendBroadcast(GameState.i().steps)

            Log.i(TAG, "in gameloop ${GameState.i().timer}")

            // fix fps updates to 1 sec
            // Thread.sleep(1000)
            delay(1000)

            // decrement countdown each loop
            GameState.i().decTimer()

            // notify user when bug is spawning
            // NOTE that we only send the notification when app is in active (foreground)
            if (GameState.i().isCanNotify && !GameState.i().isAppActive) {
                Log.i(TAG, "The NIGHT has come: a bug will spawn...")

                // create pending intent to open app from notification
                val intentToLaunchGame = Intent(this@GameStateService, AndroidLauncher::class.java)
                val pi = PendingIntent.getActivity(
                        this@GameStateService,
                        PENDINGINTENT_ID,
                        intentToLaunchGame,
                        PendingIntent.FLAG_IMMUTABLE)

                // build the notification
                val noti = Notification.Builder(this@GameStateService, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Exercise Or Die")
                        .setColor(Color.RED)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setContentText("OMG NIGHT TIME lai liao, BUGs will spawn")
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .build()

                // activate the notification
                notificationManager.notify(NOTIFY_ID, noti)
            }
        }
    }

    /**
     * Destroy any background activity if desired
     * - also destroy any manual threads
     */
    override fun onDestroy() {
        super.onDestroy()

        // Remove all notifications
        notificationManager.cancelAll()

        // unregister listeners from the sensorManager as appropriate
        sensorManager.unregisterListener(this, stepDetector)

        // TODO THREADING 1: look at the iffiness of using java threads
        // - no easy way to stop raw threads
        // - Oracle deprecated the most intuitive .stop() as it is dangerous
        // - the wall-of-text on how to do this supposedly simple task is:
        //   https://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
//        bgThread.stop()

        // cancel all coroutines, just in case
        cancel()
    }

    /**
     * Implement onSensorChanged callback
     * - system will call this back when sensor has new vals
     * - simply call GameState.i().incSteps(event.values[0])
     * if event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR
     * - log value for debugging
     * - do as minimal as possible (this is called VERY frequently)
     *
     * @param event received when values changed
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.values.isNotEmpty()) {
            val `val` = event.values[0].toInt()
            // update game state based on sensor vals
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                Log.d(TAG, "Step detector:$`val`")
                GameState.i().incSteps(`val`)

                sendBroadcast(GameState.i().steps)
            }
        }
    }

    /**
     * Implement onAccuracyChanged callback
     * - system will call this back when sensor accuracy changed
     * - just show a log msg here but may want to only track steps on HIGH  ACCURACY
     *
     * @param sensor in question affected by the change
     * @param accuracy value of the sensor during the change
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG, "Sensor accuracy changed to $accuracy")
    }

    /**
     * Send broadcast to apps that wish to get step count
     * - create a method that configures an intent with the BROADCAST_ACTION
     * - and the steps which the func receives as input
     * - good to Log this to the console
     * - use sendBroadcast function from the context to broadcast the intent
     * - call this method in onSensorChanged above
     *
     *  (Receive broadcast in another separate app)
     *
     * @param steps that this service is tracking from the pedometer
     */
    private fun sendBroadcast(steps: Int) {
        val intent = Intent(BROADCAST_ACTION)
        intent.putExtra(STEP_KEY, steps)
        Log.i(TAG, "Sending broadcast steps=$steps")
        sendBroadcast(intent)
    }
}
