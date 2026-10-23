package gt.cocina.cocinagt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import gt.cocina.cocinagt.ui.AppViewModel
import gt.cocina.cocinagt.ui.screens.OnboardingScreen
import gt.cocina.cocinagt.ui.screens.PlanScreen
import gt.cocina.cocinagt.ui.screens.RecipeDetailScreen
import gt.cocina.cocinagt.ui.screens.RecipesScreen
import gt.cocina.cocinagt.ui.screens.SettingsScreen
import gt.cocina.cocinagt.ui.screens.ShoppingScreen
import gt.cocina.cocinagt.ui.theme.CocinaGTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CocinaGTTheme {
                Surface {
                    val vm: AppViewModel = viewModel()
                    if (vm.prefs.hasOnboarded) {
                        MainScaffold(vm)
                    } else {
                        OnboardingScreen(vm)
                    }
                }
            }
        }
    }
}

private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Plan : Tab("plan", "Mi plan", Icons.Outlined.CalendarMonth)
    data object Shopping : Tab("shopping", "Compras", Icons.Outlined.ShoppingCart)
    data object Recipes : Tab("recipes", "Recetas", Icons.Outlined.RestaurantMenu)
    data object Settings : Tab("settings", "Ajustes", Icons.Outlined.Settings)
}

@Composable
private fun MainScaffold(vm: AppViewModel) {
    val navController = rememberNavController()
    val tabs = listOf(Tab.Plan, Tab.Shopping, Tab.Recipes, Tab.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Plan.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Plan.route) {
                PlanScreen(vm) { recipeId -> navController.navigate("recipe/$recipeId") }
            }
            composable(Tab.Shopping.route) { ShoppingScreen(vm) }
            composable(Tab.Recipes.route) {
                RecipesScreen(vm) { recipeId -> navController.navigate("recipe/$recipeId") }
            }
            composable(Tab.Settings.route) { SettingsScreen(vm) }
            composable("recipe/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                RecipeDetailScreen(vm, id) { navController.popBackStack() }
            }
        }
    }
}
