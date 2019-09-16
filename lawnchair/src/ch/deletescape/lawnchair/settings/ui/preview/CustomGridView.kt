/*
 *     Copyright (C) 2019 Lawnchair Team.
 *
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.deletescape.lawnchair.settings.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import ch.deletescape.lawnchair.runOnMainThread
import ch.deletescape.lawnchair.runOnUiWorkerThread
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.R
import kotlinx.android.synthetic.lawnchair.custom_grid_view.view.*

class CustomGridView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
                                                               SeekBar.OnSeekBarChangeListener {

    lateinit var currentSize: Point

    lateinit var gridCustomizer: InvariantDeviceProfile.GridCustomizer
    private var previewLoader: PreviewLoader? = null
        set(value) {
            field?.onFinishListener = null
            field = value
            field?.onFinishListener = ::onPreviewLoaded
            field?.loadPreview()
        }

    init {
        View.inflate(context, R.layout.custom_grid_view, this)
    }

    fun setInitialSize(size: Point) {
        currentSize = size
        heightSeekbar.progress = currentSize.y
        widthSeekbar.progress = currentSize.x
        heightSeekbar.let {
            it.min = 3
            it.max = 20
            it.setOnSeekBarChangeListener(this)
        }
        widthSeekbar.let {
            it.min = 3
            it.max = 20
            it.setOnSeekBarChangeListener(this)
        }
        updateText(currentSize)
        updatePreview()
    }

    private fun updateText(size: Point) {
        heightValue.text = "${size.y}"
        widthValue.text = "${size.x}"
    }

    private fun updatePreview() {
        previewLoader = PreviewLoader(context, gridCustomizer)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        updateText(Point(widthSeekbar.progress, heightSeekbar.progress))
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        setSize(Point(widthSeekbar.progress, heightSeekbar.progress))
    }

    fun setSize(newSize: Point) {
        if (currentSize != newSize) {
            currentSize = newSize
            updatePreview()
            heightSeekbar.progress = currentSize.y
            widthSeekbar.progress = currentSize.x
        }
    }

    private fun onPreviewLoaded(preview: Bitmap) {
        gridPreview.setImageDrawable(BitmapDrawable(resources, preview))
    }

    private class PreviewLoader(
            private val context: Context,
            private val gridCustomizer: InvariantDeviceProfile.GridCustomizer) {

        var onFinishListener: ((Bitmap) -> Unit)? = null

        fun loadPreview() {
            runOnUiWorkerThread {
                val preview = CustomGridProvider.getInstance(context).renderPreview(gridCustomizer).get()
                runOnMainThread {
                    onFinishListener?.invoke(preview)
                }
            }
        }
    }
}
