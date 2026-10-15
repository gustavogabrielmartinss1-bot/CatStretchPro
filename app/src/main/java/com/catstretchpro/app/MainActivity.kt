package com.catstretchpro.app

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var widthInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 17, 28)
        window.navigationBarColor = Color.rgb(13, 17, 28)
        window.decorView.setBackgroundColor(Color.rgb(13, 17, 28))

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(26), dp(22), dp(24))
        }
        val scroll = ScrollView(this)
        scroll.addView(page)

        page.addView(text("CAT STRETCH PRO", 26f, Color.WHITE, true))
        page.addView(text("Tela esticada • Android • Sem root", 14f, Color.rgb(164, 178, 205), false).also { it.setPadding(0, dp(6), 0, dp(22)) })
        page.addView(text("Resolução personalizada", 18f, Color.WHITE, true))
        page.addView(text("Ajustes experimentais para testar proporções no Free Fire. Faça por sua conta e restaure se a interface ficar difícil de usar.", 14f, Color.rgb(196, 204, 220), false).also { it.setPadding(0, dp(8), 0, dp(18)) })

        widthInput = EditText(this).apply {
            hint = "Largura (ex.: 900)"
            setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true); setPadding(dp(14), dp(10), dp(14), dp(10))
            setText("900")
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(90, 130, 255))
        }
        heightInput = EditText(this).apply {
            hint = "Altura (ex.: 1600)"
            setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true); setPadding(dp(14), dp(10), dp(14), dp(10))
            setText("1600")
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(90, 130, 255))
        }
        page.addView(widthInput, params(bottom = 10))
        page.addView(heightInput, params(bottom = 16))
        page.addView(button("APLICAR RESOLUÇÃO") { applyResolution() }, params(bottom = 10))
        page.addView(button("MODO ESTICADO LEVE (900 × 1600)") {
            widthInput.setText("900"); heightInput.setText("1600"); applyResolution()
        }, params(bottom = 10, secondary = true))
        page.addView(button("RESTAURAR RESOLUÇÃO ORIGINAL") { restoreResolution() }, params(bottom = 18, secondary = true))

        status = text("Status: aguardando autorização ADB.", 14f, Color.rgb(255, 210, 120), false)
        page.addView(status)
        page.addView(text("IMPORTANTE\nPara permitir a alteração, conecte o celular a um computador com ADB e execute:\n\nadb shell pm grant com.catstretchpro.app android.permission.WRITE_SECURE_SETTINGS\n\nSem essa autorização, o Android bloqueia a mudança. O efeito pode variar conforme a versão do sistema e o Free Fire. A resolução sugerida é apenas um teste; não aumenta FPS nem garante vantagem no jogo.", 13f, Color.rgb(164, 178, 205), false).also { it.setPadding(0, dp(22), 0, 0) })
        setContentView(scroll)
        refreshStatus()
    }

    private fun applyResolution() {
        if (!hasPermission()) {
            status.text = "Falta autorização ADB. Veja as instruções abaixo."
            Toast.makeText(this, "Autorize WRITE_SECURE_SETTINGS via ADB primeiro", Toast.LENGTH_LONG).show()
            return
        }
        val w = widthInput.text.toString().toIntOrNull()
        val h = heightInput.text.toString().toIntOrNull()
        if (w == null || h == null || w !in 320..4000 || h !in 320..5000) {
            Toast.makeText(this, "Digite largura e altura válidas", Toast.LENGTH_SHORT).show(); return
        }
        try {
            Settings.Global.putString(contentResolver, "display_size_forced", "${w}x${h}")
            status.text = "Solicitação enviada: ${w} × ${h}. Se não mudar, o Android pode bloquear esse método."
            Toast.makeText(this, "Configuração enviada", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            status.text = "Não foi possível alterar: ${e.message ?: "permissão bloqueada"}"
        }
    }

    private fun restoreResolution() {
        if (!hasPermission()) {
            status.text = "Falta autorização ADB. Veja as instruções abaixo."
            Toast.makeText(this, "Autorize WRITE_SECURE_SETTINGS via ADB primeiro", Toast.LENGTH_LONG).show()
            return
        }
        try {
            Settings.Global.putString(contentResolver, "display_size_forced", null)
            status.text = "Solicitação de restauração enviada. Confira as configurações da tela."
            Toast.makeText(this, "Restauração solicitada", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            status.text = "Não foi possível restaurar: ${e.message ?: "erro"}"
        }
    }

    private fun hasPermission(): Boolean = checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun refreshStatus() {
        status.text = if (hasPermission()) "Status: autorização ADB detectada." else "Status: aguardando autorização ADB."
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun button(label: String, secondary: Boolean = false, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; textSize = 14f
        setTextColor(Color.WHITE)
        backgroundTintList = android.content.res.ColorStateList.valueOf(if (secondary) Color.rgb(42, 52, 76) else Color.rgb(56, 92, 220))
        setOnClickListener { action() }
    }

    private fun params(bottom: Int, secondary: Boolean = false) = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(bottom) }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
