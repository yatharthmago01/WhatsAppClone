package com.example.whatsappclone.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.isVisible
import com.example.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {

    private var phoneNumber: String? = null
    private var mCounterDown: CountDownTimer? = null
    var mVerificationId: String? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        initViews()
        startVerify()
    }

    private fun startVerify() {

        val options = PhoneAuthOptions.newBuilder().setPhoneNumber(phoneNumber.toString())
                .setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        progressDialog = createProgressDialog("Sending Verification Code", false)
        progressDialog.show()

        showTimer(60000)
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = false
        val mCounterDown = object :CountDownTimer(milliSecInFuture, 1000){

            override fun onFinish() {
                counterTv.isVisible = false
                resendBtn.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.seconds_remaining, millisUntilFinished/1000)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown != null)
            mCounterDown!!.cancel()
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()

        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                checkIfProgressDialogIsInit()

                val smsCode = credential.smsCode
                if(!smsCode.isNullOrBlank())
                    sentCodeEt.setText(smsCode)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                checkIfProgressDialogIsInit()

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                notifyUserAndRetry("Phone number might be wrong or connection error. Try again!")
            }

            override fun onCodeSent(verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken){

                checkIfProgressDialogIsInit()

                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                checkIfProgressDialogIsInit()
                startActivity(Intent(this, SignUpActivity::class.java).setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            } else{
                notifyUserAndRetry("Phone verification failed. Try again!")
            }
        }
    }

    private fun checkIfProgressDialogIsInit() {
        if( :: progressDialog.isInitialized)
            progressDialog.dismiss()
    }

    private fun notifyUserAndRetry(message: String) {

        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok"){ _,_ ->
                showLoginActivity()
            }
            setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }

            setCancelable(false); create(); show();
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_to_detect_sms, phoneNumber))

        val clickableSpan = object : ClickableSpan(){
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan, span.length-13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    override fun onClick(v: View?) {

        when(v){

            verificationBtn -> {
                val code = sentCodeEt.text.toString()
                if(code.isNotBlank() && !mVerificationId.isNullOrBlank()){
                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()

                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if(mResendToken != null){
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending verification code...", false)
                    progressDialog.show()

                    val options = PhoneAuthOptions.newBuilder().setPhoneNumber(phoneNumber.toString())
                            .setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).setForceResendingToken(mResendToken!!).build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }
        }
    }
}

// Extension function to create a dialog so that it can be used anywhere
fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(isCancelable)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}