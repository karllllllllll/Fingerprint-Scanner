package com.karl.fingerprintmodule.Fragments


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.karl.fingerprintmodule.R

class SliderFragment : Fragment() {

    private var NUM_PAGES = 2
    private var mPager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null

    private var device_name = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(
            R.layout.fragment_screen_slide_page, container, false
        ) as ViewGroup

        if (arguments != null) {
            device_name = arguments!!.getString("device_name")
        }

        mPager = v.findViewById(R.id.pager)
        pagerAdapter = ScreenSlidePagerAdapter(activity!!.supportFragmentManager)
        mPager!!.adapter = pagerAdapter
        mPager!!.setPageTransformer(true, ZoomOutPageTransformer())

        //val tabLayout = v.findViewById<TabLayout>(R.id.tabDots)
        val tabLayout = activity!!.findViewById<TabLayout>(R.id.tl_dashboard)
        tabLayout.setupWithViewPager(mPager, true)

        return v
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        private val tabTitles = arrayOf("Register", "Kiosk")

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }


        override fun getItem(position: Int): Fragment? {

            var f: Fragment? = null



            when (position) {

                0 -> {
//                    val fragmentTag = makeFragmentName(mPager!!.id, 2)
//                    val fragment = activity!!.supportFragmentManager.findFragmentByTag(fragmentTag)

                    f = RegisterFromListFragment()
                    val args = Bundle()
                    args.putString("device_name", device_name)
                    f.arguments = args
                }

                1 -> {

                    f = TimeInOutFragment()
                    val args = Bundle()
                    args.putString("device_name", device_name)
                    f.arguments = args

                }
            }

            return f
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    private fun makeFragmentName(viewId: Int, id: Long): String {
        return "android:switcher:$viewId:$id"
    }

    class ZoomOutPageTransformer : ViewPager.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

}