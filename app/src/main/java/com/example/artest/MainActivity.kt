package com.example.artest

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.artest.databinding.ActivityMainBinding
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private lateinit var binding: ActivityMainBinding
    private  var session: com.google.ar.core.Session? = null
    private var shouldConfigureSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //request permission
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.P)
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission  Camera need to use Camera",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            })
            .check()

        binding.arView.scene.addOnUpdateListener(this)


    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.P)
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "permission  Camera need to use Camera",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            })
            .check()
    }

    override fun onPause() {
        super.onPause()
        if (session != null){
            session!!.pause()
            binding.arView.pause()
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupSession() {
        if (session == null) {
            try {
                session = com.google.ar.core.Session(this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                e.printStackTrace()
            } catch (e: UnavailableApkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableSdkTooOldException) {
                e.printStackTrace()
            } catch (e: UnavailableDeviceNotCompatibleException) {
                e.printStackTrace()
            }
            shouldConfigureSession = true
        }
        if (shouldConfigureSession) {
            configSession()
            shouldConfigureSession = false
            binding.arView.setupSession((session))
        }
        try {
            session!!.resume()
            binding.arView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            session = null
            return
        }
    }

    private fun configSession() {
        val config = Config(session)
        if (!buildDataBase(config))
            Toast.makeText(this@MainActivity, "Error Built-in database", Toast.LENGTH_SHORT).show()
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session!!.configure(config)
    }

    private fun buildDataBase(config: Config): Boolean {
        val augmentedImageDatabase: AugmentedImageDatabase
        val bitmap = loadBitmapFromRessourcces()
        if (bitmap == null)
            return false
        augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("lion", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadBitmapFromRessourcces(): Bitmap? {
val inputStream = assets.open("lion_qr.png")
        return BitmapFactory.decodeStream(inputStream)
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = binding.arView.arFrame
        val updateAugmentedImage = frame?.getUpdatedTrackables<AugmentedImage>(AugmentedImage::class.java)
        if (updateAugmentedImage != null) {
            for (augmentedImg in updateAugmentedImage){
                if (augmentedImg.trackingState == TrackingState.TRACKING){
                    if(augmentedImg.name.equals("lion")){
                        val node = MyNode(this,R.raw.lion)
                        node.image = augmentedImg
                        binding.arView.scene.addChild(node)
                    }

                }

            }
        }
    }

}