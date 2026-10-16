package com.jay.cartube

import android.app.Presentation
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer

/**
 * Android Auto gives navigation apps a raw Surface on the car screen. We point a private
 * VirtualDisplay at that surface and show a Presentation (a window on that display)
 * containing the WebView. Car touch events arrive here and are replayed into the WebView.
 */
class CarSurfaceRenderer(private val carContext: CarContext) : SurfaceCallback {

    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null

    override fun onSurfaceAvailable(container: SurfaceContainer) {
        val surface = container.surface ?: return
        release()

        val dm = carContext.getSystemService(DisplayManager::class.java)
        val vd = dm.createVirtualDisplay(
            "CarTube",
            container.width,
            container.height,
            container.dpi,
            surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        )
        virtualDisplay = vd

        val p = Presentation(carContext, vd.display)
        val root = FrameLayout(p.context)
        root.addView(
            CarBrowser.attach(p.context),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        CarBrowser.host = root
        p.setContentView(root)
        p.show()
        presentation = p
    }

    override fun onSurfaceDestroyed(container: SurfaceContainer) = release()

    /** Single tap on the car touchscreen (Car API level 5+). */
    override fun onClick(x: Float, y: Float) {
        val target = presentation?.window?.decorView ?: return
        val down = SystemClock.uptimeMillis()
        sendTouch(target, down, down, MotionEvent.ACTION_DOWN, x, y)
        sendTouch(target, down, down + 60, MotionEvent.ACTION_UP, x, y)
    }

    /** Drag on the screen (in pan mode). Same sign convention as GestureDetector. */
    override fun onScroll(distanceX: Float, distanceY: Float) {
        CarBrowser.scrollBy(distanceX, distanceY)
    }

    override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        // Some hosts send -1 for a double-tap zoom; treat that as a small zoom-in.
        CarBrowser.zoom(if (scaleFactor < 0f) 1.25f else scaleFactor)
    }

    private fun sendTouch(
        target: android.view.View, downTime: Long, eventTime: Long,
        action: Int, x: Float, y: Float
    ) {
        val ev = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
        ev.source = InputDevice.SOURCE_TOUCHSCREEN
        target.dispatchTouchEvent(ev)
        ev.recycle()
    }

    fun release() {
        CarBrowser.detach()
        presentation?.dismiss()
        presentation = null
        virtualDisplay?.release()
        virtualDisplay = null
    }
}
