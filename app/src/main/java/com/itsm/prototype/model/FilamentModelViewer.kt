package com.itsm.prototype.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.ByteBuffer


class FilamentModelViewer(
    private val context: Context,
    private val surfaceView: SurfaceView
) : DefaultLifecycleObserver {

    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private lateinit var assetLoader: AssetLoader
    private lateinit var resourceLoader: ResourceLoader
    private var cameraManipulator: Manipulator? = null
    private lateinit var gestureDetector: GestureDetector

    private var uiHelper: UiHelper? = null
    private var displayHelper: DisplayHelper? = null
    private var choreographer: Choreographer? = null
    private var currentAsset: FilamentAsset? = null
    private var swapChain: com.google.android.filament.SwapChain? = null

    private val frameScheduler = FrameCallback()
    private var isInitialized = false
    private var isDestroyed = false

    private val client = OkHttpClient()

    init {
        Utils.init()
        setupFilament()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFilament() {
        choreographer = Choreographer.getInstance()

        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()

        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)

        view.camera = camera
        view.scene = scene

        setupLighting()

        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    cameraManipulator?.grabBegin(e.x.toInt(), e.y.toInt(), e.pointerCount > 1)
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    cameraManipulator?.grabUpdate(e2.x.toInt(), e2.y.toInt())
                    return true
                }


                override fun onDoubleTap(e: MotionEvent): Boolean {
                    resetCamera()
                    return true
                }
            })

        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            renderCallback = SurfaceCallback()
            attachTo(surfaceView)
        }
        surfaceView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                cameraManipulator?.grabEnd()
            }
            true
        }

        displayHelper = DisplayHelper(context)

        val materialProvider = UbershaderProvider(engine)
        assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        isInitialized = true
    }

    private fun setupLighting() {
        val sunEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(150_000.0f) // más intensidad
            .direction(0.0f, -1.0f, -1.0f)
            .castShadows(true)
            .build(engine, sunEntity)
        scene.addEntity(sunEntity)

        val skybox = Skybox.Builder()
            .color(0.35f, 0.35f, 0.40f, 1.0f)
            .build(engine)
        scene.skybox = skybox

        val ibl = IndirectLight.Builder()
            .intensity(50_000.0f) // más intensidad
            .build(engine)
        scene.indirectLight = ibl
    }

    fun loadModelFromApi(url: String, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                    return@launch
                }

                val bytes = response.body?.bytes()
                if (bytes != null) {
                    withContext(Dispatchers.Main) {
                        loadModelFromBuffer(ByteBuffer.wrap(bytes))
                        callback(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("FilamentViewer", "Error loading model", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }


    private fun loadModelFromBuffer(buffer: ByteBuffer) {
        currentAsset?.let { asset ->
            scene.removeEntities(asset.entities)
            assetLoader.destroyAsset(asset)
        }

        currentAsset = assetLoader.createAsset(buffer)
        currentAsset?.let { asset ->
            resourceLoader.loadResources(asset)
            scene.addEntities(asset.entities)
            adjustCameraToModel(asset)
        }
    }

    private fun adjustCameraToModel(asset: FilamentAsset) {
        val box = asset.boundingBox

        val cx = box.center[0]
        val cy = box.center[1]
        val cz = box.center[2]

        val hx = box.halfExtent[0]
        val hy = box.halfExtent[1]
        val hz = box.halfExtent[2]

        val radius = kotlin.math.sqrt(hx*hx + hy*hy + hz*hz)
        val distance = radius * 3.0f

        val aspect = surfaceView.width.toDouble() / surfaceView.height.toDouble()
        camera.setProjection(
            45.0,
            aspect,
            radius * 0.05,
            distance * 10.0,
            Camera.Fov.VERTICAL
        )

        val eyeX = cx
        val eyeY = cy + radius * 0.3f
        val eyeZ = cz + distance


        cameraManipulator = Manipulator.Builder()
            .targetPosition(cx, cy, cz)
            .orbitHomePosition(eyeX, eyeY, eyeZ)
            .viewport(surfaceView.width, surfaceView.height)
            .build(Manipulator.Mode.ORBIT)
    }




    fun resetCamera() {
        currentAsset?.let { adjustCameraToModel(it) }
    }

    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer?.postFrameCallback(this)

            if (!isInitialized || uiHelper?.isReadyToRender != true) return

            cameraManipulator?.let { manipulator ->
                manipulator.update(frameTimeNanos / 1_000_000_000.0f)

                val eye = FloatArray(3)
                val target = FloatArray(3)
                val up = FloatArray(3)
                manipulator.getLookAt(eye, target, up)

                camera.lookAt(
                    eye[0].toDouble(), eye[1].toDouble(), eye[2].toDouble(),
                    target[0].toDouble(), target[1].toDouble(), target[2].toDouble(),
                    up[0].toDouble(), up[1].toDouble(), up[2].toDouble()
                )
            }


            swapChain?.let { chain ->
                if (renderer.beginFrame(chain, frameTimeNanos)) {
                    renderer.render(view)
                    renderer.endFrame()
                }
            }
        }
    }

    private inner class SurfaceCallback : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            val display =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    context.display
                } else {
                    @Suppress("DEPRECATION")
                    val windowManager =
                        context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
                    windowManager.defaultDisplay
                }

            display?.let {
                swapChain = engine.createSwapChain(surface)
                displayHelper?.attach(renderer, it)
            } ?: run {
                Log.w("FilamentViewer", "Display not available")
            }
        }

        override fun onDetachedFromSurface() {
            displayHelper?.detach()
            swapChain?.let {
                engine.destroySwapChain(it)
                swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            view.viewport = Viewport(0, 0, width, height)
            cameraManipulator?.setViewport(width, height)

            val aspect = width.toDouble() / height.toDouble()
            camera.setProjection(
                45.0, aspect, 0.1, 1000.0,
                Camera.Fov.VERTICAL
            )
        }

    }

    override fun onResume(owner: LifecycleOwner) {
        choreographer?.postFrameCallback(frameScheduler)
    }

    override fun onPause(owner: LifecycleOwner) {
        choreographer?.removeFrameCallback(frameScheduler)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
    }


    fun destroy() {
        if (isDestroyed) return
        isDestroyed = true

        choreographer?.removeFrameCallback(frameScheduler)
        uiHelper?.detach()

        engine.flushAndWait()

        currentAsset?.let { asset ->
            scene.removeEntities(asset.entities)
            assetLoader.destroyAsset(asset)
            currentAsset = null
        }

        swapChain?.let {
            engine.destroySwapChain(it)
            swapChain = null
        }

        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)

        EntityManager.get().destroy(camera.entity)

        assetLoader.destroy()
        resourceLoader.destroy()

        engine.destroy()
    }
}
