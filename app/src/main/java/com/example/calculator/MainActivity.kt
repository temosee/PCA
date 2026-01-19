package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calculator.ui.screen.HistoryScreen
import com.example.calculator.ui.screen.HomeScreen
import com.example.calculator.ui.screen.InputScreen
import com.example.calculator.ui.screen.ResultScreen
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodel.SplitEvent
import com.example.calculator.viewmodel.SplitViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Input : Screen("input")
    object Result : Screen("result/{calcId}") {
        fun createRoute(calcId: String) = "result/$calcId"
    }
    object History : Screen("history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                SplitMateApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitMateApp() {
    val navController = rememberNavController()
    val viewModel: SplitViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SplitMate", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (currentRoute != Screen.Home.route && currentRoute != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screen.Input.route) {
                        IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Icon(Icons.Default.History, null)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onStart = { navController.navigate(Screen.Input.route) })
            }
            composable(Screen.Input.route) {
                val state = viewModel.state
                InputScreen(
                    state = state,
                    onBillChanged = { viewModel.onEvent(SplitEvent.BillChanged(it)) },
                    onPeopleChanged = { viewModel.onEvent(SplitEvent.PeopleChanged(it)) },
                    onCalculate = {
                        val id = viewModel.calculate()
                        navController.navigate(Screen.Result.createRoute(id))
                    }
                )
            }
            composable(
                route = Screen.Result.route,
                arguments = listOf(navArgument("calcId") { type = NavType.StringType })
            ) { backStackEntry ->
                val calcId = backStackEntry.arguments?.getString("calcId")
                val calculation = viewModel.getCalculation(calcId)
                ResultScreen(
                    calculation = calculation,
                    onBackToEdit = { navController.popBackStack() },
                    onNewCalculation = {
                        viewModel.onEvent(SplitEvent.Reset)
                        navController.navigate(Screen.Input.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    history = viewModel.history,
                    onItemClick = { id -> navController.navigate(Screen.Result.createRoute(id)) }
                )
            }
        }
    }
}