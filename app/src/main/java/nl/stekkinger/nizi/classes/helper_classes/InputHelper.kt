package nl.stekkinger.nizi.classes.helper_classes

import android.content.Context
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import nl.stekkinger.nizi.R
import java.util.regex.Pattern

object InputHelper {
    fun inputIsEmpty(context: Context, input: EditText, stringIndex: Int) : Boolean {
        // Check if input is blank (does NOT count whitespace, alternative: .trim().isEmpty())
        if (input.text.isBlank()) {
            input.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            input.requestFocus()
            Toast.makeText(context, stringIndex, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    fun isValidEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}