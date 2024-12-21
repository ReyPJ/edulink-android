package com.example.edulinkcr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.edulinkcr.api.RetrofitClient
import com.example.edulinkcr.model.User
import com.example.edulinkcr.ui.theme.EduLinkCRTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.core.view.WindowCompat
import com.example.edulinkcr.model.CreateUserRequest
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.reflect.KFunction3

class FullUsersListActivity : ComponentActivity() {
    private fun validateInputs(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        uniqueCode: String,
        password: String
    ): Boolean {
        return username.isNotBlank() &&
                email.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                uniqueCode.isNotBlank() &&
                password.isNotBlank()
    }

    private fun createUser(context: Context, user: CreateUserRequest, onComplete: (Boolean) -> Unit) {
        val apiService = RetrofitClient.getApiService(context)
        Log.d("CreateUser", "Request Body: $user")

        apiService.createUser(user).enqueue(object : Callback<CreateUserRequest> {
            override fun onResponse(call: Call<CreateUserRequest>, response: Response<CreateUserRequest>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                } else {
                    Toast.makeText(
                        context,
                        "Error al crear usuario: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<CreateUserRequest>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                onComplete(false)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            EduLinkCRTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var showCreateDialog by remember { mutableStateOf(false) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent2(
                            modifier = Modifier
                                .width(250.dp)
                                .systemBarsPadding()
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            RoleTopBar2(onMenuClick = {
                                scope.launch { drawerState.open() }
                            })
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { showCreateDialog = true}
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Crear usuario")
                            }
                        },
                        modifier = Modifier.systemBarsPadding()
                    ) { innerPadding ->
                        Box{
                            UserList(modifier = Modifier.padding(innerPadding))

                            if (showCreateDialog) {
                                CreateUserDialog(
                                    onDismiss = { showCreateDialog = false },
                                    onUserCreated = { showCreateDialog = false },
                                    onValidatedInputs = ::validateInputs,
                                    onCreateUser = ::createUser
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onUserCreated: () -> Unit,
    onValidatedInputs: (String, String, String, String, String, String) -> Boolean,
    onCreateUser: KFunction3<Context, CreateUserRequest, (Boolean) -> Unit, Unit>
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var phone by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }
    var adminCenter by remember { mutableStateOf("") }
    var uniqueCode by remember { mutableStateOf("") }
    var showRoleMenu by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val roles = listOf("student", "professor", "father")

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        adminCenter = sharedPreferences.getString("center", "") ?: ""
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Crear nuevo usuario",
                    style =  MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it},
                        label = { Text("Nombre de Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it},
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it},
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it},
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it},
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = { },
                            label = { Text("Rol") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRoleMenu = true
                                },
                            singleLine = true
                        )
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false },
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = {Text(role)},
                                    onClick = {
                                        selectedRole = role
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = adminCenter,
                        onValueChange = { },
                        label = { Text("Centro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        enabled = false
                    )
                    OutlinedTextField(
                        value = uniqueCode,
                        onValueChange = { uniqueCode = it},
                        label = { Text("Código Único") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it},
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (onValidatedInputs(username, email, firstName, lastName, uniqueCode, password)) {
                                isLoaded = true
                                val newUser = CreateUserRequest(
                                    username = username,
                                    email = email,
                                    first_name = firstName,
                                    last_name = lastName,
                                    role = selectedRole,
                                    phone = phone,
                                    center = adminCenter,
                                    unique_code = uniqueCode,
                                    password = password
                                )
                                onCreateUser(context, newUser) { success ->
                                    if (success) {
                                        onUserCreated()
                                    }
                                }
                            }
                        },
                        enabled = !isLoaded
                    ) {
                        if (isLoaded) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Crear")
                        }
                    }
                }

            }
        }
    }

}

@Composable
fun DrawerContent2(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var role by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        role = sharedPreferences.getString("role", "") ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(text = "Menu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when (role) {
            "admin" -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Listas de usuarios",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, FullUsersListActivity::class.java)
                        context.startActivity(intent)
                    })
            } else -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mis clases",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable {
                        // TODO: Implementar la navegación a la pantalla de clases
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleTopBar2(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    var role by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("role", "") ?: ""
        role = when (userRole) {
            "admin" -> "Administrador"
            "profesor" -> "Profesor"
            "student" -> "Estudiante"
            "father" -> "Padre/Madre"
            else -> "Desconocido"
        }
    }

    TopAppBar(
        title = { Text(text = role) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            IconButton(onClick = {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()

                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión"
                )
            }
        }
    )
}

@Composable
fun UserList(modifier: Modifier = Modifier) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val apiService = RetrofitClient.getApiService(context)
        apiService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    users = response.body()?.filter { it.role != "admin" } ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("UserList", "Error: ${t.message}")
            }
        })
    }

    val groupedUsers = users.groupBy { it.role }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        groupedUsers.forEach { (role, users) ->
            val isExpanded = expandedStates[role] ?: false
            val roleName = when (role) {
                "student" -> "Estudiantes"
                "professor" -> "Profesores"
                "father" -> "Padres"
                else -> role.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
            item {
                Text(
                    text = "$roleName (${users.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .clickable {
                            expandedStates[role] = !isExpanded
                        }
                )
            }
            if (isExpanded) {
                items(users) { user ->
                    UserItem(user)
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${user.first_name} ${user.last_name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Centro: ${user.center}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Rol: ${user.role}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Codigo Unico: ${user.unique_code}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}