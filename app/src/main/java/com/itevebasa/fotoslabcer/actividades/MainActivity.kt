package com.itevebasa.fotoslabcer.actividades

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.auxiliar.VariablesGlobales
import com.itevebasa.fotoslabcer.conexion.RetrofitClient
import com.itevebasa.fotoslabcer.modelos.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val usuario: EditText = findViewById(R.id.usuario)
        val password: EditText = findViewById(R.id.password)
        val checkbox: CheckBox = findViewById(R.id.checkBox)
        val recordar = sharedPreferences.getBoolean("recordar", false)
        if (recordar) {
            VariablesGlobales.token = sharedPreferences.getString("token", "")!!
            VariablesGlobales.user_id = sharedPreferences.getInt("user_id", 0)
            checkbox.isChecked = true
            val intent = Intent(this, ExpedientesActivity::class.java)
            startActivity(intent)
            finish()
        }

        val loginBtn: Button = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener{
            val usuarioTexto = usuario.text.toString()
            val passwordTexto = password.text.toString()
            val apiService = RetrofitClient.getApiService()
            apiService.login(usuarioTexto, passwordTexto).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            VariablesGlobales.token = it.token
                            VariablesGlobales.user_id = it.user.id
                            if (checkbox.isChecked) {
                                sharedPreferences.edit()
                                    .putString("token", it.token)
                                    .putInt("user_id", it.user.id)
                                    .putBoolean("recordar", true)
                                    .apply()
                            } else {
                                sharedPreferences.edit().clear().apply()
                            }
                            val intent = Intent(this@MainActivity, ExpedientesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Log.d("API", "ERROR: " + response.code())
                        Toast.makeText(this@MainActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Log.d("API", "Fallo llamada a la API: " + t.message)
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }
}