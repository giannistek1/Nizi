package nl.stekkinger.nizi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.auth0.android.Auth0
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var TAG = "Main"

    val EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activity_main_btn_logout.setOnClickListener { logout() }

        //Obtain the token from the Intent's extras
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
        activity_main_txt_credentials.text = accessToken
    }

    private fun logout() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.putExtra(EXTRA_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }
}
