package com.snail.collie.debug

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import kotlin.jvm.JvmOverloads

class FloatingFpsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mRecyclerView: RecyclerView? = null
    private val mStringList: MutableList<String?> = ArrayList()
    private var mAdapter: RecyclerView.Adapter<*> =
        object : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val textView = TextView(getContext())
                textView.textSize = 12f
                textView.setPadding(8, 16, 0, 0)
                return object : RecyclerView.ViewHolder(textView) {
                    override fun toString(): String {
                        return super.toString()
                    }
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).text = mStringList[position]
            }

            override fun getItemCount(): Int {
                return mStringList.size
            }
        }

    private fun init() {
        setBackgroundColor(0x33000000)
        mRecyclerView = RecyclerView(context)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.adapter = mAdapter
        val params = LayoutParams(250, 500)
        addView(mRecyclerView, params)
    }

    fun update(content: String?) {
        mStringList.add(content)
        mAdapter.notifyItemInserted(mStringList.size - 1)
        mRecyclerView!!.scrollToPosition(mStringList.size - 1)
    }

    init {
        init()
    }
}