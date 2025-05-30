package com.itevebasa.fotoslabcer.actividades

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itevebasa.fotoslabcer.R

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
            usuario.setText(sharedPreferences.getString("usuario", ""))
            password.setText(sharedPreferences.getString("password", ""))
            checkbox.isChecked = true

            // Si quieres que entre automáticamente sin tocar el botón:
            val intent = Intent(this, ExpedientesActivity::class.java).apply {
                putExtra("usuario", sharedPreferences.getString("usuario", ""))
            }
            startActivity(intent)
            finish()
        }

        val loginBtn: Button = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener{
            val usuarioTexto = usuario.text.toString()
            val passwordTexto = password.text.toString()

            // Guardar solo si la casilla está marcada
            if (checkbox.isChecked) {
                sharedPreferences.edit()
                    .putString("usuario", usuarioTexto)
                    .putBoolean("recordar", true)
                    .apply()
            } else {
                sharedPreferences.edit().clear().apply()
            }
            val intent = Intent(this, ExpedientesActivity::class.java).apply {
                putExtra("usuario", usuario.text.toString())
            }
            startActivity(intent)
        }
    }
}