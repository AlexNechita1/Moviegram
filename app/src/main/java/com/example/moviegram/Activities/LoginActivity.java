package com.example.moviegram.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.moviegram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButtonLogin;
    private RadioButton radioButtonSignup;
    private Button loginBtn,registerBtn;
    private ProgressBar progressBar,progressBar2;
    private FirebaseAuth mAuth;
    private ConstraintLayout loginConstraint,signupConstraint;
    private EditText mailEdt, passEdt, signupUsername,signupEmail,signupPass,signupRePass;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initLoginView();
        initSignupView();
    }

    private void initSignupView() {
        signupUsername = findViewById(R.id.input_username);
        signupEmail = findViewById(R.id.input_email_signup);
        signupPass = findViewById(R.id.input_pass_signup);
        signupRePass = findViewById(R.id.input_confirm_pass_signup);
        registerBtn = findViewById(R.id.button_signup);
        progressBar2 = findViewById(R.id.progressBar2);
        loginConstraint = findViewById(R.id.login_form);
        signupConstraint = findViewById(R.id.signup_form);
        mAuth=FirebaseAuth.getInstance();
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerBtn.setVisibility(View.GONE);
                progressBar2.setVisibility(View.VISIBLE);
                if(signupUsername.getText().toString().isEmpty() && signupEmail.getText().toString().isEmpty() && signupPass.getText().toString().isEmpty() && signupRePass.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Incomplete fields", Toast.LENGTH_SHORT).show();
                    registerBtn.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.GONE);
                } else {
                    if(signupPass.getText().toString().isEmpty() == signupRePass.getText().toString().isEmpty()){
                        mAuth.createUserWithEmailAndPassword(signupEmail.getText().toString(), signupPass.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            FirebaseUser user = mAuth.getCurrentUser();
                                            String uid = user.getUid();
                                            String username = signupUsername.getText().toString();

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("uid", uid);
                                            userData.put("username", username);

                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("Users")
                                                    .document(uid)
                                                    .set(userData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(LoginActivity.this, "User data added to Firestore", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(LoginActivity.this, "Failed to add user data to Firestore", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Signup failed",
                                                    Toast.LENGTH_SHORT).show();
                                            registerBtn.setVisibility(View.VISIBLE);
                                            progressBar2.setVisibility(View.GONE);

                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Passwords not matching", Toast.LENGTH_SHORT).show();
                        registerBtn.setVisibility(View.VISIBLE);
                        progressBar2.setVisibility(View.GONE);
                    }

                }
            }
        });
    }

    private void initLoginView() {
        mailEdt =findViewById(R.id.editTextEmail);
        passEdt=findViewById(R.id.editTextPass);
        loginBtn = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonLogin = findViewById(R.id.radioButtonLogin);
        radioButtonSignup = findViewById(R.id.radioButtonSignup);
        mAuth=FirebaseAuth.getInstance();
        underlineRadioButton(radioButtonLogin);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radioButtonLogin.isChecked()) {
                    removeUnderline(radioButtonSignup);
                    underlineRadioButton(radioButtonLogin);
                    transitionLoginForm();
                } else if (radioButtonSignup.isChecked()) {
                    removeUnderline(radioButtonLogin);
                    underlineRadioButton(radioButtonSignup);
                    transitionSignupForm();
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loginBtn.setVisibility(View.GONE);
                String email, password;
                email = mailEdt.getText().toString();
                password = passEdt.getText().toString();

                if(mailEdt.getText().toString().isEmpty() || passEdt.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Email or password empty.", Toast.LENGTH_SHORT).show();
                    loginBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }  else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Login failed",
                                                Toast.LENGTH_SHORT).show();
                                        loginBtn.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

    }
    private void transitionLoginForm(){
        ViewGroup parent = findViewById(R.id.main);
        Transition transition = new Slide(Gravity.START);
        transition.setDuration(450);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(parent, transition);

        signupConstraint.setVisibility(View.GONE);
        loginConstraint.setVisibility(View.VISIBLE);


    }
    private void transitionSignupForm(){
        ViewGroup parent = findViewById(R.id.main);
        Transition transition = new Slide(Gravity.END);
        transition.setDuration(450);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(parent, transition);

        loginConstraint.setVisibility(View.GONE);
        signupConstraint.setVisibility(View.VISIBLE);
    }

    private void underlineRadioButton(RadioButton radioButton) {
        CharSequence text = radioButton.getText();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue)), 0, spannableString.length(), 0);
        radioButton.setText(spannableString);
    }

    private void removeUnderline(RadioButton radioButton) {
        CharSequence text = radioButton.getText();
        SpannableString spannableString = new SpannableString(text);

        UnderlineSpan[] underlines = spannableString.getSpans(0, spannableString.length(), UnderlineSpan.class);
        for (UnderlineSpan underline : underlines) {
            spannableString.removeSpan(underline);
        }

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)), 0, spannableString.length(), 0);

        radioButton.setText(spannableString);
    }

}