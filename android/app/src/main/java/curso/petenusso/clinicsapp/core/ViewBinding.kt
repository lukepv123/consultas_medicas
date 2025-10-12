package curso.petenusso.clinicsapp.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Pequenos helpers opcionais para ViewBinding. Use se quiser.
 */
inline fun <T : ViewBinding> inflate(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    crossinline binder: (LayoutInflater, ViewGroup?, Boolean) -> T
): T = binder(inflater, parent, false)
