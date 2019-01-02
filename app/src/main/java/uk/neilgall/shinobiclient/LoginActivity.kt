package uk.neilgall.shinobiclient

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var authTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password_text.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in.setOnClickListener { attemptLogin() }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (authTask != null) {
            return
        }

        // Reset errors.
        server_text.error = null
        email_text.error = null
        password_text.error = null

        // Store values at the time of the login attempt.
        val serverStr = server_text.text.toString()
        val emailStr = email_text.text.toString()
        val passwordStr = password_text.text.toString()

        if (!isServerValid(serverStr)) {
            server_text.error = getString(R.string.error_invalid_server)
            server_text.requestFocus()
            return
        } else if (!isEmailValid(emailStr)) {
            email_text.error = getString(R.string.error_invalid_email)
            email_text.requestFocus()
            return
        } else if (!isPasswordValid(passwordStr)) {
            password_text.error = getString(R.string.error_incorrect_password)
            password_text.requestFocus()
            return
        }

        showProgress(true)
        authTask = UserLoginTask(serverStr, use_tls.isChecked, emailStr, passwordStr)
        authTask!!.execute(null as Void?)
    }

    private fun isServerValid(server: String): Boolean = !server.isEmpty()

    private fun isEmailValid(email: String): Boolean = email.contains("@")

    private fun isPasswordValid(password: String): Boolean = !password.isEmpty()

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val server: String, private val tls: Boolean, private val email: String, private val password: String) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            val service = createShinobiService(server, tls)
            val login = service.login(LoginRequest(email, password)).execute()
            if (!login.isSuccessful)
                return false

            val user = login.body()?.user
            println("$user")

            return user != null
        }

        override fun onPostExecute(success: Boolean?) {
            authTask = null
            showProgress(false)

            if (success!!) {
                finish()
            } else {
                password_text.error = getString(R.string.error_incorrect_password)
                password_text.requestFocus()
            }
        }

        override fun onCancelled() {
            authTask = null
            showProgress(false)
        }
    }
}
