/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.home.room.detail

import android.view.View
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

private var childAttachStateChangeListener: WeakReference<RecyclerView.OnChildAttachStateChangeListener>? = null

fun LinearLayoutManager.scrollToPosition(recyclerView: RecyclerView, position: Int, moveABit: Boolean) {
    clearAttachStateChangeListener(recyclerView)

    if (moveABit) {
        val childView = recyclerView.children.firstOrNull { recyclerView.getChildAdapterPosition(it) == position }
        if (null == childView || childView.measuredHeight == 0) {
            childAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    if (recyclerView.getChildAdapterPosition(view) == position) {
                        clearAttachStateChangeListener(recyclerView)
                        view.doOnPreDraw {
                            view.post { scrollToPositionWithOffset(position, view, recyclerView) }
                        }
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                }
            }.let {
                recyclerView.addOnChildAttachStateChangeListener(it)
                WeakReference(it)
            }
            scrollToPosition(position)
            recyclerView.doOnNextLayout {
                clearAttachStateChangeListener(recyclerView)
            }
        } else {
            scrollToPositionWithOffset(position, childView, recyclerView)
        }
    } else {
        scrollToPosition(position)
    }
}

fun clearAttachStateChangeListener(recyclerView: RecyclerView) {
    childAttachStateChangeListener?.get()?.let { recyclerView.removeOnChildAttachStateChangeListener(it) }
    childAttachStateChangeListener = null
}

private fun LinearLayoutManager.scrollToPositionWithOffset(position: Int, view: View, recyclerView: RecyclerView) {
    when {
        view.measuredHeight == 0 || recyclerView.measuredHeight == 0 -> {
            scrollToPosition(position)
        }
        else                                                         -> {
            scrollToPositionWithOffset(position, -view.measuredHeight + recyclerView.measuredHeight / 2)
        }
    }
}
