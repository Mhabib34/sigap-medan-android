package com.example.smart_city.fragment

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smart_city.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Skeleton
        val skeleton = view.findViewById<View>(R.id.skeletonDashboard)
        val content = view.findViewById<View>(R.id.contentDashboard)
        skeleton.visibility = View.VISIBLE
        content.visibility = View.GONE


        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            skeleton.visibility = View.GONE
            content.visibility = View.VISIBLE
        }, 400)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            skeleton.visibility = View.VISIBLE
            content.visibility = View.GONE
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                skeleton.visibility = View.GONE
                content.visibility = View.VISIBLE

                swipeRefresh.isRefreshing = false
            }, 400)
        }
    }

}