package com.example.weatherjournal2.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.weatherjournal2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {

    private TextInputEditText nameEditTextReg;
    private TextInputEditText emailEditTextReg;
    private TextInputEditText passwordEditTextReg;
    private ProgressBar progressBarReg;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        progressBarReg = root.findViewById(R.id.progressBarReg);
        nameEditTextReg = root.findViewById(R.id.nameEditTextReg);
        emailEditTextReg = root.findViewById(R.id.emailEditTextReg);
        passwordEditTextReg = root.findViewById(R.id.passwordEditTextReg);

        // Set an OnClickListener for the register button
        Button registerButton = root.findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarReg.setVisibility(View.VISIBLE);
                String email, password, name;
                name = String.valueOf(nameEditTextReg.getText());
                email = String.valueOf(emailEditTextReg.getText());
                password = String.valueOf(passwordEditTextReg.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBarReg.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign up success, update UI with the newly registered user's information
                                    Toast.makeText(getContext(), "Account created.", Toast.LENGTH_SHORT).show();

                                    // Perform navigation or UI updates upon successful registration
                                } else {
                                    // If sign-up fails, display a message to the user.
                                    Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        return root;
    }
}
