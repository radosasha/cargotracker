package com.tracker

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

object MainViewController {
    fun create(): UIViewController = ComposeUIViewController { App() }
}

@Suppress("unused")
fun MainViewController(): UIViewController = MainViewController.create()
