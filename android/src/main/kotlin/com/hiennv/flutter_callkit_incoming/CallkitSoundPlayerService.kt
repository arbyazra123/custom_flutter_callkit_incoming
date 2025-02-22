package com.hiennv.flutter_callkit_incoming

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log

class CallkitSoundPlayerService : Service() {

    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null

    private var mediaPlayer: MediaPlayer? = null
    private var data: Bundle? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.prepare()
        this.playSound(intent)
        this.playVibrator()
        return START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        forceStopVibration()
        mediaPlayer = null
        vibrator = null
    }

    private fun prepare() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        forceStopVibration()
    }

    private fun playVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        when (audioManager?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0L, 1000L, 1000L),
                            0
                        )
                    )
                } else {
                    vibrator?.vibrate(longArrayOf(0L, 1000L, 1000L), 0)
                }
            }
        }
    }

    private fun playSound(intent: Intent?) {
        this.data = intent?.extras
        val sound = this.data?.getString(
            CallkitConstants.EXTRA_CALLKIT_RINGTONE_PATH,
            ""
        )
        var uri = sound?.let { getRingtoneUri(it) }
        if (uri == null) {
            uri = RingtoneManager.getActualDefaultRingtoneUri(
                this@CallkitSoundPlayerService,
                RingtoneManager.TYPE_RINGTONE
            )
        }
        Log.e("CallkitSoundPlayer", "Playing sound: ${uri.toString()}")
        try {
            mediaPlayer(uri!!)
        } catch (e: Exception) {
            try {
                uri = getRingtoneUri("ringtone_default")
                mediaPlayer(uri!!)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun mediaPlayer(uri: Uri) {
        mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attribution = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setLegacyStreamType(AudioManager.STREAM_RING)
                .build()
            mediaPlayer?.setAudioAttributes(attribution)
        } else {
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_RING)
        }
        setDataSource(uri)
        mediaPlayer?.prepare()
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun setDataSource(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val assetFileDescriptor =
                applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")
            if (assetFileDescriptor != null) {
                mediaPlayer?.setDataSource(assetFileDescriptor)
            }
            return
        }
        mediaPlayer?.setDataSource(applicationContext, uri)
    }

    private fun getRingtoneUri(fileName: String) = try {
        if (TextUtils.isEmpty(fileName)) {
            RingtoneManager.getActualDefaultRingtoneUri(
                this@CallkitSoundPlayerService,
                RingtoneManager.TYPE_RINGTONE
            )
        }
        val resId = resources.getIdentifier(fileName, "raw", packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${packageName}/$resId")
        } else {
            if (fileName.equals("system_ringtone_default", true)) {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            } else {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            }
        }
    } catch (e: Exception) {
        try {
            if (fileName.equals("system_ringtone_default", true)) {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            } else {
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@CallkitSoundPlayerService,
                    RingtoneManager.TYPE_RINGTONE
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun forceStopVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Newer APIs (API 26+): Use VibrationEffect to stop vibration
                    vibrator?.cancel() // Explicitly stop ongoing vibration
                    Log.d("Vibration", "Vibration canceled using newer API")
                } else {
                    // Older APIs (<API 26): Use a zero-duration vibration pattern
                    vibrator?.vibrate(longArrayOf(0), -1)
                    Log.d("Vibration", "Vibration force-stopped using legacy workaround")
                }
            } else {
                Log.e("Vibration", "No vibrator available on this device")
            }
        } catch (e: Exception) {
            Log.e("Vibration", "Error while force-stopping vibration", e)
        }
    }
}