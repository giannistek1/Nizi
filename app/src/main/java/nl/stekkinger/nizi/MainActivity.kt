package nl.stekkinger.nizi

import android.accounts.Account
import android.accounts.AccountManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activity_main_btn_login.setOnClickListener {

            // Checks
            if (activity_main_et_username.text.toString() == "") {
                return@setOnClickListener
            }

            if (activity_main_et_password.text.toString() == "") {
                return@setOnClickListener
            }

            // Login using AuthRepository
            // ...
        }

    }
}
