package com.example.weatherjournal2.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.weatherjournal2.R;
import com.example.weatherjournal2.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    LinearLayout profileView, loginView, registerView;
    TextView registerNow, userName, userEmail;
    Button logout;
    TextInputEditText nameEditTextReg, emailEditText, passwordEditText, emailEditTextReg, passwordEditTextReg;
    ProgressBar progressBar, progressBarReg;
    FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FrameLayout frameLayout = root.findViewById(R.id.frameLayout);

        if (currentUser != null) {
            View profileView = inflater.inflate(R.layout.fragment_profile_logged, frameLayout, false);
            frameLayout.addView(profileView);

            userName = profileView.findViewById(R.id.userName);
            userEmail = profileView.findViewById(R.id.userEmail);

            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            userEmail.setText(email);
            userName.setText(name);

            // Here you can set the user's data in the profile layout
            // For example, you can set the user's name and email

            // Set an OnClickListener for the logout button
            Button logoutButton = profileView.findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    // You can navigate to the login page or perform other actions upon logout
                    View loginView = inflater.inflate(R.layout.fragment_login, container, false);
                    frameLayout.removeAllViews();
                    frameLayout.addView(loginView);
                }
            });
        } else {
            View loginView = inflater.inflate(R.layout.fragment_login, container, false);
            frameLayout.removeAllViews();
            frameLayout.addView(loginView);

            progressBar = loginView.findViewById(R.id.progressBar);
            emailEditText = loginView.findViewById(R.id.emailEditText);
            passwordEditText = loginView.findViewById(R.id.passwordEditText);
            registerNow = loginView.findViewById(R.id.registerNow);

            // Set an OnClickListener for the login button
            Button loginButton = loginView.findViewById(R.id.btn_login);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    String email, password;
                    email = String.valueOf(emailEditText.getText());
                    password = String.valueOf(passwordEditText.getText());

                    if (TextUtils.isEmpty(email)){
                        Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(password)){
                        Toast.makeText(getContext(), "Enter password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(getContext(), "Login successful",
                                                Toast.LENGTH_SHORT).show();
                                        View profileView = inflater.inflate(R.layout.fragment_profile, container, false);
                                        frameLayout.removeAllViews();
                                        frameLayout.addView(profileView);
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch(FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(getContext(), "Invalid credentials.",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.d("Firebase login error", e.getMessage());
                                            Toast.makeText(getContext(), "Authentication error. Make sure the credentials are right.",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                            });
                }
            });

            // Set an OnClickListener for the "Don't have an account?" text
            registerNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View registerView = inflater.inflate(R.layout.fragment_register, container, false);
                    frameLayout.removeAllViews();
                    frameLayout.addView(registerView);

                    nameEditTextReg = registerView.findViewById(R.id.nameEditTextReg);
                    emailEditTextReg = registerView.findViewById(R.id.emailEditTextReg);
                    passwordEditTextReg = registerView.findViewById(R.id.passwordEditTextReg);
                    progressBarReg = registerView.findViewById(R.id.progressBarReg);

                    Button registerButton = registerView.findViewById(R.id.btn_register);
                    registerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBarReg.setVisibility(View.VISIBLE);
                            String email, password, name;
                            name = String.valueOf(nameEditTextReg.getText());
                            email = String.valueOf(emailEditTextReg.getText());
                            password = String.valueOf(passwordEditTextReg.getText());

                            if (TextUtils.isEmpty(email)){
                                Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (TextUtils.isEmpty(password)){
                                Toast.makeText(getContext(), "Enter password", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .build();
                                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                }
                                                            }
                                                        });

                                                Toast.makeText(getContext(), "Account created.",
                                                        Toast.LENGTH_SHORT).show();
                                                View profileView = inflater.inflate(R.layout.fragment_profile, container, false);
                                                frameLayout.removeAllViews();
                                                frameLayout.addView(profileView);
                                            } else {
                                                try {
                                                    throw Objects.requireNonNull(task.getException());
                                                } catch (FirebaseAuthWeakPasswordException e) {
                                                    Toast.makeText(getContext(), "Weak password!",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                                    Toast.makeText(getContext(), "Invalid credentials.",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (FirebaseAuthException e) {
                                                    Toast.makeText(getContext(), "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (Exception e){
                                                    Log.d("Firebase exception", Objects.requireNonNull(e.getMessage()));
                                                }
                                            }
                                        }
                                    });
                        }
                    });
                }
            });
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}