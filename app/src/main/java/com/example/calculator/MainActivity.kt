package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.util.UUID

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            primaryContainer = Color(0xFFBB86FC).copy(alpha = 0.2f)
        ),
        content = content
    )
}

data class Calculation(
    val id: String = UUID.randomUUID().toString(),
    val totalBill: Double,
    val people: Int,
    val tipPercent: Double = 15.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val tipAmount: Double get() = totalBill * (tipPercent / 100)
    val totalWithTip: Double get() = totalBill + tipAmount
    val perPerson: Double get() = totalWithTip / people
}

class SplitViewModel : ViewModel() {
    var billAmount by mutableStateOf("")
    var numPeople by mutableStateOf("")
    
    private val _history = mutableStateListOf<Calculation>()
    val history: List<Calculation> get() = _history.takeLast(5).reversed()

    val isInputValid: Boolean
        get() = (billAmount.toDoubleOrNull() ?: 0.0) > 0 && (numPeople.toIntOrNull() ?: 0) > 0

    fun calculate(): String {
        val calc = Calculation(
            totalBill = billAmount.toDoubleOrNull() ?: 0.0,
            people = numPeople.toIntOrNull() ?: 1
        )
        _history.add(calc)
        return calc.id
    }

    fun getCalculation(id: String?): Calculation? {
        return _history.find { it.id == id }
    }

    fun resetInputs() {
        billAmount = ""
        numPeople = ""
    }
}

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
                    if (currentRoute != Screen.History.route) {
                        IconButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Icon(Icons.Default.History, contentDescription = "История")
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
                InputScreen(
                    viewModel = viewModel,
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
                        viewModel.resetInputs()
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

@Composable
fun HomeScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("SplitMate", fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text("Разделите счет легко и честно", fontSize = 16.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Начать расчет", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InputScreen(viewModel: SplitViewModel, onCalculate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Введите данные", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.billAmount,
                    onValueChange = { if (it.length <= 9) viewModel.billAmount = it },
                    label = { Text("Сумма чека") },
                    prefix = { Text("₽ ") },
                    leadingIcon = { Icon(Icons.Rounded.Payments, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = viewModel.numPeople,
                    onValueChange = { if (it.length <= 3) viewModel.numPeople = it },
                    label = { Text("Человек") },
                    leadingIcon = { Icon(Icons.Rounded.Groups, null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCalculate,
            enabled = viewModel.isInputValid,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Рассчитать итог", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResultScreen(
    calculation: Calculation?,
    onBackToEdit: () -> Unit,
    onNewCalculation: () -> Unit
) {
    if (calculation == null) return

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ваш результат", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("С КАЖДОГО ПО", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                Text("${"%.2f".format(calculation.perPerson)} ₽", fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Чек", "${calculation.totalBill} ₽", onDark = true)
                        DetailRow("Чаевые (15%)", "${"%.2f".format(calculation.tipAmount)} ₽", onDark = true)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        DetailRow("Итого", "${"%.2f".format(calculation.totalWithTip)} ₽", isBold = true, onDark = true)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBackToEdit,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Правка")
            }
            
            Button(
                onClick = onNewCalculation,
                modifier = Modifier.weight(1.1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Заново")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false, onDark: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (onDark) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = if (onDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun HistoryScreen(history: List<Calculation>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет истории", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onItemClick(item.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Итого: ${"%.2f".format(item.totalWithTip)} ₽", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${item.people} чел. • ${"%.2f".format(item.perPerson)} ₽ / чел") },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                        )
                    }
                }
            }
        }
    }
}
