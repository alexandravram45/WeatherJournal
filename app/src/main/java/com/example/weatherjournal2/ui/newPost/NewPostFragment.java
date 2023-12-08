package com.example.weatherjournal2.ui.newPost;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherjournal2.MainActivity;
import com.example.weatherjournal2.databinding.FragmentNewPostBinding;
import com.example.weatherjournal2.ui.Models.Post;
import com.example.weatherjournal2.ui.Models.Weather;
import com.example.weatherjournal2.ui.Models.WeatherManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class NewPostFragment extends Fragment {

    private FragmentNewPostBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private FirebaseAuth mAuth;
    private Uri pickedImageUri = null;
    private ImageView backgroundImage, conditionIcon;
    private FrameLayout frameLayoutImage;
    private EditText editPostDescription;
    private Button chooseFromGalleryButton, takeAPhotoButton, postButton;
    private TextView postLocation, postTemperature;

    private final ActivityResultCallback<ActivityResult> pickImageCallback = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                pickedImageUri = result.getData().getData();
                frameLayoutImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(pickedImageUri)
                        .fit()
                        .into(backgroundImage);
                postButton.setEnabled(true);
                Log.d("image", pickedImageUri.toString());
            } else {
                Log.d("image", "" + result.getResultCode());

            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), pickImageCallback);

        NewPostViewModel newPostViewModel = new ViewModelProvider(this).get(NewPostViewModel.class);

        editPostDescription = binding.editPostDescription;
        chooseFromGalleryButton = binding.chooseFromGallery;
        takeAPhotoButton = binding.takeAPhoto;
        backgroundImage = binding.backgroundImage;
        frameLayoutImage = binding.frameLayoutImage;
        postLocation = binding.postLocation;
        postTemperature = binding.postTemperature;
        postButton = binding.postButton;
        conditionIcon = binding.conditionIcon;

        newPostViewModel.setEditPostDescription(editPostDescription);
        newPostViewModel.setChooseFromGalleryButton(chooseFromGalleryButton);
        newPostViewModel.setTakeAPhotoButton(takeAPhotoButton);
        newPostViewModel.setBackgroundImage(backgroundImage);
        newPostViewModel.setFrameLayoutImage(frameLayoutImage);
        newPostViewModel.setPostLocation(postLocation);
        newPostViewModel.setPostTemperature(postTemperature);
        newPostViewModel.setPostButton(postButton);
        newPostViewModel.setConditionIcon(conditionIcon);

        Weather currentWeather = WeatherManager.getInstance().getCurrentWeather();

        postLocation.setText(currentWeather.getCityName());
        postTemperature.setText(currentWeather.getTemperature() + " °C");
        Picasso.get().load("http:".concat(currentWeather.getConditionIconString())).into(conditionIcon);

        chooseFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(getContext(), "You must be logged to upload a photo", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    pickImageLauncher.launch(intent);
                }
            }
        });
        takeAPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //........
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickedImageUri != null) {
                    StorageReference storage = FirebaseStorage.getInstance().getReference().child("posts_images");
                    final StorageReference imageFilePath = storage.child(pickedImageUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    if (mAuth.getCurrentUser() != null) {
                                        Post post = new Post(
                                                editPostDescription.getText().toString(),
                                                imageDownloadLink,
                                                mAuth.getCurrentUser().getPhotoUrl().toString());
                                        addPostToDb(post);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("failure post: ", e.getMessage());
                                }
                            })
                            ;
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Please choose an image!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        return root;
    }

    public void addPostToDb(Post post){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();
                returnToMain();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Cannot succeed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
