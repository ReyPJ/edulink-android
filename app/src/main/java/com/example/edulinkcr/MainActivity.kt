package com.example.edulinkcr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edulinkcr.ui.theme.EduLinkCRTheme
import com.example.edulinkcr.api.RetrofitClient
import com.example.edulinkcr.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduLinkCRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserList(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UserList(modifier: Modifier = Modifier){
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        RetrofitClient.apiService.getUsers().enqueue(object : Callback<List<User>>{
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    users = response.body( ) ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("UserList", "Error: ${t.message}")
            }
        })
    }

    LazyColumn(modifier = modifier.padding(16.dp)){
        items(users) { user ->
            UserItem(user)
        }
    }
}

@Composable
fun UserItem(user:User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${user.first_name}   ${user.last_name}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Centro: ${user.center}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Rol: ${user.role}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Codigo Unicol: ${user.unique_code}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserListPreview() {
    EduLinkCRTheme {
        UserItem(User(
            1,
            first_name = "Juan",
            last_name = "Perez",
            center = "Centro Educativo",
            role = "Estudiante",
            unique_code = "123",
            username = "123",
            email = "example@gmail.com",
            phone = "0202020",
            date_joined = "2021-10-10"
        ))
    }
}

