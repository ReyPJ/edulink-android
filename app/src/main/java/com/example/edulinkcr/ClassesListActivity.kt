package com.example.edulinkcr

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.edulinkcr.api.RetrofitClient
import com.example.edulinkcr.model.AddStudentsToClass
import com.example.edulinkcr.model.User
import com.example.edulinkcr.ui.theme.EduLinkCRTheme
import com.example.edulinkcr.model.ClaseItem
import com.example.edulinkcr.model.Clase
import com.example.edulinkcr.model.CreateClaseItem
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.exp

class ClassesListActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            EduLinkCRTheme {
                val context = LocalContext.current
                var classes by remember { mutableStateOf(Clase()) }
                val subjects = listOf("Todo", "Matematica", "Español", "Ciencias", "Estudios Sociales", "Ingles")
                var showCreateClassDialog by remember { mutableStateOf(false) }
                var role by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    role = sharedPreferences.getString("role", "") ?: ""
                    fetchClasses(context, null) { fetchedClasses ->
                        classes = fetchedClasses
                    }
                }
                Scaffold(modifier = Modifier.systemBarsPadding(),
                    floatingActionButton = {
                        if (role == "profesor") {
                            FloatingActionButton(onClick = { showCreateClassDialog = true} ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar Clase")
                            }
                        }
                    }
                ){
                    Column {
                        ClassesTabs(subjects) { subject ->
                            fetchClasses(context, if (subject == "Todo") null else subject){ fetchedClasses ->
                                classes = if(subject == "Todo") fetchedClasses else Clase().apply { addAll(fetchedClasses.filter { it.subject == subject }) }
                            }
                        }
                        ClassesList(classes)
                    }
                }

                if (showCreateClassDialog) {
                    CreateClassDialog(
                        onDismiss = { showCreateClassDialog = false },
                        onCreateClass = { name, subject, center, teacherId ->
                            createClass(context, name, subject, center, teacherId)
                            showCreateClassDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun fetchClasses(context: Context, subject: String?, onComplete: (Clase) -> Unit){
        val apiService = RetrofitClient.getApiService(context)
        val call = if(subject == null){
            apiService.getClasses()
        } else {
            apiService.getClassesBySubject(subject)
        }

        call.enqueue(object: Callback<Clase> {
            override fun onResponse(call: Call<Clase>, response: Response<Clase>) {
                if (response.isSuccessful){
                    onComplete(response.body() ?: Clase())
                } else {
                    Log.e("ClassesListActivity", "Error fetching classes: ${response.code()}")
                    onComplete(Clase())
                }
            }

            override fun onFailure(call: Call<Clase>, t: Throwable) {
                Log.e("ClassesListActivity", "Error Fetching Classes", t)
                onComplete(Clase())
            }
        })
    }

    private fun createClass(context: Context, name: String, subject: String, center: String, teacherId: Int){
        val apiService = RetrofitClient.getApiService(context)
        val requestBody = CreateClaseItem(
            name = name,
            subject =  subject,
            center = center,
            teacher = teacherId,
            students = emptyList()
        )

        apiService.createClass(requestBody).enqueue(object : Callback<CreateClaseItem> {
            override fun onResponse(
                call: Call<CreateClaseItem>,
                response: Response<CreateClaseItem>
            ) {
                if (response.isSuccessful) {
                    Log.d("ClassesListActivity", "Class created successfully")
                } else {
                    Log.e("ClassesListActivity", "Error creating class: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CreateClaseItem>, t: Throwable) {
                Log.e("ClassesListActivity", "Error creating class", t)
            }
        })
    }
}

@Composable
fun ClassesTabs(
    subjects: List<String>,
    onSubjectSelected: (String) -> Unit
){
    var selectedTabIndex by remember {  mutableIntStateOf(0) }

    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
        subjects.forEachIndexed {index, subject ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    onSubjectSelected(subject)
                },
                text = {
                    Text(
                        text = subject,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(100.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun ClassesList(classes: Clase) {
    val context = LocalContext.current
    var role by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        role = sharedPreferences.getString("role", "") ?: ""
    }

    LazyColumn {
        items(classes) { clase ->
            var expanded by remember { mutableStateOf(false) }
            var showAddStudentsDialog by remember { mutableStateOf(false) }
            var availableStudents by remember { mutableStateOf<List<User>>(emptyList()) }

            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = clase.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Profesor: ${clase.teacherName}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Cantidad de estudiantes: ${clase.students.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    // Cantidad de estudiantes
                    Box {
                        Column {
                            Text(
                                text = "Ver lista de estudiantes",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clickable { expanded = true }
                                    .padding(8.dp)
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                clase.studentsName.forEach { studentName ->
                                    DropdownMenuItem(
                                        text = { Text(studentName) },
                                        onClick = { expanded = false }
                                    )
                                }
                            }
                        }
                        if (role == "profesor" || role == "admin") {
                            Button(
                                onClick = {
                                    showAddStudentsDialog = true
                                    fetchAvailableStudents(context, clase.id) { students ->
                                        availableStudents = students
                                    }
                                },
                                modifier = Modifier.padding(top = 32.dp)
                            ) {
                                Text("Agregar estudiantes")
                            }
                        }
                    }
                }

                if (showAddStudentsDialog) {
                    AddStudentsDialog(
                        availableStudents = availableStudents,
                        currentStudents = clase.students,
                        onDismiss = { showAddStudentsDialog = false },
                        onAddStudents = { selectedStudents ->
                            addStudentsToClass(context, clase.id, selectedStudents)
                            showAddStudentsDialog = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentsDialog(
    availableStudents: List<User>,
    currentStudents: List<Int>,
    onDismiss: () -> Unit,
    onAddStudents: (List<User>) -> Unit
){
    var selectedStudents by remember { mutableStateOf<List<User>>(emptyList()) }
    val filteredStudents = availableStudents.filter { it.id !in currentStudents }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
            ) {
                Column {
                    Text("Agregar estudiantes")
                    Spacer(modifier = Modifier.height(8.dp))
                    filteredStudents.forEach { student ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStudents = if (selectedStudents.contains(student)) {
                                        selectedStudents - student
                                    } else {
                                        selectedStudents + student
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = selectedStudents.contains(student),
                                onCheckedChange = null
                            )
                            Text("${student.first_name} ${student.last_name}")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onAddStudents(selectedStudents) }) {
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onCreateClass: (String, String, String, Int) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var center by remember { mutableStateOf("") }
    var teacherId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        center = sharedPreferences.getString("center", "") ?: ""
        teacherId = sharedPreferences.getInt("userId", 0)
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
            ) {
                Column {
                    Text("Crear Clase")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la Clase") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Materia") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onCreateClass(name, subject, center, teacherId) }) {
                            Text("Crear")
                        }
                    }
                }
            }
        }
    )
}

private fun fetchAvailableStudents(context: Context, classId: Int, onComplete: (List<User>) -> Unit) {
    val apiService = RetrofitClient.getApiService(context)
    apiService.getUsers().enqueue(object : Callback<List<User>> {
        override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
            if (response.isSuccessful) {
                val students = response.body()?.filter { it.role == "student" } ?: emptyList()
                onComplete(students)
            } else {
                Log.e("ClassesListActivity", "Error fetching available students: ${response.code()}")
                onComplete(emptyList())
            }
        }

        override fun onFailure(call: Call<List<User>>, t: Throwable) {
            Log.e("ClassesListActivity", "Error fetching available students", t)
            onComplete(emptyList())
        }
    })
}

private fun addStudentsToClass(context: Context, classId: Int, students: List<User>) {
    val apiService = RetrofitClient.getApiService(context)
    val studentIds = students.map { it.id } // Map to actual student IDs
    val requestBody = AddStudentsToClass(students = studentIds)

    Log.d("Students to Add", "Estudiantes: $requestBody")
    Log.d("Class ID", "Clase: $classId")

    apiService.addStudentsToClass(classId, requestBody).enqueue(object : Callback<AddStudentsToClass> {
        override fun onResponse(call: Call<AddStudentsToClass>, response: Response<AddStudentsToClass>) {
            if (response.isSuccessful) {
                Log.d("ClassesListActivity", "Students added successfully")
            } else {
                Log.e("ClassesListActivity", "Error adding students: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<AddStudentsToClass>, t: Throwable) {
            Log.e("ClassesListActivity", "Error adding students", t)
        }
    })
}