package com.example.weatherjournal2.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.weatherjournal2.MainActivity;
import com.example.weatherjournal2.R;
import com.example.weatherjournal2.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    TextView registerNow;
    TextInputEditText nameEditTextReg, emailEditText, passwordEditText, emailEditTextReg, passwordEditTextReg;
    ProgressBar progressBar, progressBarReg;
    ImageView userImage;
    Button addProfileImageButton;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FrameLayout frameLayout = root.findViewById(R.id.frameLayout);
        View profileView = inflater.inflate(R.layout.fragment_profile_logged, container, false);
        View loginView = inflater.inflate(R.layout.fragment_login, container, false);
        View registerView = inflater.inflate(R.layout.fragment_register, container, false);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            frameLayout.addView(profileView);
            setProfileInfo(profileView);
            userImage = profileView.findViewById(R.id.userImage);

            addProfileImageButton = profileView.findViewById(R.id.idBAddProfileImage);

            Button logoutButton = profileView.findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    frameLayout.removeAllViews();
                    frameLayout.addView(loginView);
                    update();
                }
            });

            addProfileImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickImageLauncher.launch(intent);
                }
            });

            pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    uploadImageToStorage(selectedImageUri);
                }
            });

        } else {
            frameLayout.removeAllViews();
            frameLayout.addView(loginView);

            progressBar = loginView.findViewById(R.id.progressBar);
            emailEditText = loginView.findViewById(R.id.emailEditText);
            passwordEditText = loginView.findViewById(R.id.passwordEditText);
            registerNow = loginView.findViewById(R.id.registerNow);

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
                                        Toast.makeText(getContext(), "Login successful",
                                                Toast.LENGTH_SHORT).show();

                                        frameLayout.removeAllViews();
                                        frameLayout.addView(profileView);
                                        update();

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

            registerNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                                        "://" + getResources().getResourcePackageName(R.drawable.profile)
                                                        + '/' + getResources().getResourceTypeName(R.drawable.profile) + '/' + getResources().getResourceEntryName(R.drawable.profile));
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .setPhotoUri(imageUri)
                                                        .build();
                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getContext(), "Account created.",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    frameLayout.removeAllViews();
                                                                    frameLayout.addView(profileView);
                                                                    update();
                                                                } else {
                                                                    Toast.makeText(getContext(), "Failed to update user profile",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                try {
                                                    throw Objects.requireNonNull(task.getException());
                                                } catch (FirebaseAuthWeakPasswordException e) {
                                                    Toast.makeText(getContext(), "Weak password!",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                                    Toast.makeText(getContext(), "Invalid credentials.",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (FirebaseAuthException e) {
                                                    Toast.makeText(getContext(), "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
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

    private void update() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            String imageName = mAuth.getCurrentUser().getUid() + ".jpg";
            StorageReference imageRef = storageRef.child("user_images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Use Picasso to load and display the image
                            Picasso.get()
                                    .load(uri)
                                    .fit()
                                    .centerCrop()
                                    .transform(new CropCircleTransformation())
                                    .into(userImage);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setProfileInfo(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        ImageView userImage = view.findViewById(R.id.userImage);
        TextView userName = view.findViewById(R.id.userName);
        TextView userEmail = view.findViewById(R.id.userEmail);

        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        Uri imageUri = currentUser.getPhotoUrl();

        userName.setText(name);
        userEmail.setText(email);

        if (imageUri != null) {
            Picasso.get()
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .transform(new CropCircleTransformation())
                    .into(userImage);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}