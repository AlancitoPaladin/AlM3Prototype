package com.itsm.prototype.model

import android.content.Context
import android.util.Log
import android.view.Choreographer
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
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Manipulator
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
    private lateinit var cameraManipulator: Manipulator


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
        setupFilament()
    }

    private fun setupFilament() {
        choreographer = Choreographer.getInstance()

        // Inicializar Filament
        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()

        // Crear cámara correctamente
        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)

        // Configurar cámara
        view.camera = camera
        view.scene = scene

        // Configurar iluminación
        setupLighting()

        // Configurar UiHelper para el SurfaceView
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            renderCallback = SurfaceCallback()
            attachTo(surfaceView)
        }

        displayHelper = DisplayHelper(context)

        // Usar UbershaderProvider (correcto para Filament 1.67.0)
        val materialProvider = UbershaderProvider(engine)
        assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        isInitialized = true
    }

    private fun setupLighting() {
        // Luz direccional principal
        val sunEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.SUN)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(100_000.0f)
            .direction(0.0f, -1.0f, 0.0f)
            .castShadows(true)
            .build(engine, sunEntity)
        scene.addEntity(sunEntity)

        // Skybox simple
        val skybox = Skybox.Builder()
            .color(0.35f, 0.35f, 0.40f, 1.0f)
            .build(engine)
        scene.skybox = skybox

        val ibl = IndirectLight.Builder()
            .intensity(30_000.0f)
            .build(engine)
        scene.indirectLight = ibl
    }

    fun loadModelFromUrl(url: String, callback: (Boolean) -> Unit) {
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

                val bytes = response.body.bytes()

                withContext(Dispatchers.Main) {
                    loadModelFromBuffer(ByteBuffer.wrap(bytes))
                    callback(true)
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
        // Limpiar asset anterior
        currentAsset?.let { asset ->
            scene.removeEntities(asset.entities)
            assetLoader.destroyAsset(asset)
        }

        // Cargar nuevo modelo
        currentAsset = assetLoader.createAsset(buffer)
        currentAsset?.let { asset ->
            resourceLoader.loadResources(asset)

            // Agregar a la escena
            scene.addEntities(asset.entities)

            // Ajustar cámara al modelo
            adjustCameraToModel(asset)
        }
    }

    private fun adjustCameraToModel(asset: FilamentAsset) {
        val boundingBox = asset.boundingBox
        val center = boundingBox.center.let { c ->
            Float3(c[0], c[1], c[2])
        }
        val halfExtent = boundingBox.halfExtent.let { h ->
            Float3(h[0], h[1], h[2])
        }

        val maxExtent = maxOf(halfExtent.x, halfExtent.y, halfExtent.z)
        val distance = maxExtent * 3.0f

        camera.lookAt(
            center.x.toDouble(),
            (center.y + maxExtent * 0.5),
            (center.z + distance).toDouble(),
            center.x.toDouble(),
            center.y.toDouble(),
            center.z.toDouble(),
            0.0, 1.0, 0.0
        )

        val aspect = surfaceView.width.toDouble() / surfaceView.height.toDouble()
        camera.setProjection(
            45.0, aspect, 0.1, (distance * 10.0),
            Camera.Fov.VERTICAL
        )
    }

    fun resetCamera() {
        currentAsset?.let { adjustCameraToModel(it) }
    }

    fun setWireframeMode(enabled: Boolean) {
        view.isPostProcessingEnabled = !enabled
    }

    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer?.postFrameCallback(this)

            if (!isInitialized || uiHelper?.isReadyToRender != true) {
                return
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
                // ⭐ Crear y guardar swapChain
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
        choreographer?.removeFrameCallback(frameScheduler)

        currentAsset?.let { asset ->
            scene.removeEntities(asset.entities)
            assetLoader.destroyAsset(asset)
        }

        uiHelper?.detach()

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