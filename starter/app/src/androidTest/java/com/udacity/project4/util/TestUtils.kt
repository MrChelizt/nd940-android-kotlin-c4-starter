package com.udacity.project4.util

/**
 * Created by dannyroa on 5/9/15.
 */
object TestUtils {

    fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
        return RecyclerViewMatcher(recyclerViewId)
    }

}