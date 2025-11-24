package curso.petenusso.clinicsapp.ui.paciente

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import curso.petenusso.clinicsapp.R
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.Navigator
import curso.petenusso.clinicsapp.data.firebase.ConsultasFirebaseRepository
import curso.petenusso.clinicsapp.databinding.ActivityPacienteBinding
import curso.petenusso.clinicsapp.model.session.SessionManager
import curso.petenusso.clinicsapp.ui.paciente.fragments.LobbyPacienteFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PacienteActivity : AppCompatActivity() {

    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
        private const val CHANNEL_ID_CONSULTAS = "consultas_channel"
    }

    private lateinit var binding: ActivityPacienteBinding
    private val consultasRepo = ConsultasFirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        criarCanalNotificacoes()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.pacienteContainer.id, LobbyPacienteFragment())
                .commit()
        }

        // 🔹 Android 13+ precisa pedir permissão, versões antigas podem notificar direto.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checarOuPedirPermissaoNotificacao()
        } else {
            checarConsultaHoje()
        }
    }

    private fun criarCanalNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de consultas"
            val descriptionText = "Notificações de consultas agendadas para hoje"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID_CONSULTAS, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checarOuPedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                // Já tem permissão → checa consultas
                checarConsultaHoje()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_POST_NOTIFICATIONS
                )
            }
        }
    }

    // Chamado depois do diálogo de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_POST_NOTIFICATIONS &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Agora que tem permissão → checa consultas e, se tiver, notifica
                checarConsultaHoje()
            } else {
                showSnack("⚠️ Permissão de notificações negada. Não será possível avisar sobre consultas.")
            }
        }
    }

    /**
     * Checa se o paciente logado (SessionManager.asPaciente) tem consulta HOJE.
     */
    private fun checarConsultaHoje() {
        val pacienteSession = SessionManager.asPaciente()
        val pacienteId = pacienteSession?.pacienteId

        if (pacienteId.isNullOrBlank()) {
            // Sem paciente em sessão → nada a fazer
            return
        }

        lifecycleScope.launch {
            when (val result = consultasRepo.listarDeHojePorPaciente(pacienteId)) {
                is AppResult.Success -> {
                    val consultasHoje = result.data

                    if (consultasHoje.isEmpty()) {
                        // Nenhuma consulta hoje, vida que segue.
                        return@launch
                    }

                    // 🔹 Mostra só um resumo na tela
                    showSnack("🩺 Você tem ${consultasHoje.size} consulta(s) hoje.")

                    // 🔹 Para CADA consulta do dia, gera uma notificação separada
                    consultasHoje.forEach { consulta ->
                        val horaFormatada = try {
                            val sdfEntrada = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ssXXX",
                                Locale.getDefault()
                            )
                            val date = sdfEntrada.parse(consulta.dataHoraConsulta)
                            val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                            if (date != null) sdfHora.format(date) else "horário indefinido"
                        } catch (e: Exception) {
                            consulta.dataHoraConsulta
                        }

                        // Usa o id da consulta pra ter um notificationId único
                        val notificationId = consulta.id.hashCode()

                        mostrarNotificacaoConsultaHoje(
                            horaFormatada = horaFormatada,
                            notificationId = notificationId
                        )
                    }
                }

                is AppResult.Error -> {
                    showSnack(
                        "Erro ao checar consultas de hoje: " +
                                (result.throwable.localizedMessage ?: "desconhecido")
                    )
                }
            }
        }
    }

    private fun mostrarNotificacaoConsultaHoje(
        horaFormatada: String,
        notificationId: Int
    ) {
        // Android 13+ → garante permissão antes de notificar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Intent para abrir a tela do paciente ao tocar na notificação
        val intent = Intent(this, PacienteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId, // também pode variar o requestCode
            intent,
            flags
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_CONSULTAS)
            .setSmallIcon(R.mipmap.ic_launcher_clinics)
            .setContentTitle("Você tem consulta hoje")
            .setContentText("Consulta marcada para às $horaFormatada.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun showSnack(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun logoutToLogin() {
        FirebaseAuth.getInstance().signOut()
        SessionManager.clear()
        Navigator.logoutToLogin(this)
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && !isChangingConfigurations) {
            SessionManager.clear()
        }
    }
}
