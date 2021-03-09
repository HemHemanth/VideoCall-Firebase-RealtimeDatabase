package com.hemanth.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.hemanth.videocall.fragments.AccountFragment
import com.hemanth.videocall.interfaces.IMyFriendsFragment
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), IMyFriendsFragment {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var navController = findNavController(R.id.fragNavHost)
        bottomNavView.setupWithNavController(navController)

    }

    override fun onFindProfilesTapped() {
        val view: View = bottomNavView.findViewById(R.id.search)
        view.performClick()
    }

}