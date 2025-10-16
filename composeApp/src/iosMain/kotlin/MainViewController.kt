package com.tracker

import androidx.compose.ui.window.ComposeUIViewController
import com.tracker.di.IOSKoinApp
import platform.UIKit.UIViewController

object MainViewController {
    fun create(): UIViewController {
        try {
            println("MainViewController: Starting initialization...")

            // Инициализируем Application scope (если еще не инициализирован)
            println("MainViewController: Initializing IOSApplication...")
            IOSApplication.init()
            println("MainViewController: IOSApplication initialized successfully")

            // Инициализируем ViewController scope
            println("MainViewController: Initializing ViewController scope...")
            IOSKoinApp.initViewControllerScope()
            println("MainViewController: ViewController scope initialized successfully")

            println("MainViewController: Creating ComposeUIViewController...")
            val viewController = ComposeUIViewController { App() }
            println("MainViewController: ComposeUIViewController created successfully")

            return viewController
        } catch (e: Exception) {
            println("MainViewController: Error during initialization: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

@Suppress("unused")
fun MainViewController(): UIViewController = MainViewController.create()
