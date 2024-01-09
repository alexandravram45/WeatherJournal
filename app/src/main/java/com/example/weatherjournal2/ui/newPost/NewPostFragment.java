package com.example.weatherjournal2.ui.newPost;

import static android.app.Activity.RESULT_OK;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherjournal2.MainActivity;
import com.example.weatherjournal2.databinding.FragmentNewPostBinding;
import com.example.weatherjournal2.ui.Models.Post;
import com.example.weatherjournal2.ui.Models.Weather;
import com.example.weatherjournal2.ui.Models.WeatherManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

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
    private ProgressBar progressBar;


    private final ActivityResultCallback<ActivityResult> pickImageCallback = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (result.getData().getData() != null) {
                    pickedImageUri = result.getData().getData();
                } else if (result.getData().getExtras() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        backgroundImage.setImageBitmap(imageBitmap);

                        saveImageToGallery(imageBitmap);
                    }
                }
                    if (pickedImageUri != null) {
                        frameLayoutImage.setVisibility(View.VISIBLE);
                        Picasso.get()
                                .load(pickedImageUri)
                                .fit()
                                .into(backgroundImage);
                        postButton.setEnabled(true);
                        Log.d("image", pickedImageUri.toString());
                    } else {
                        Log.d("image", "Image capture failed");
                    }

            } else {
                Log.d("image", "Result code: " + result.getResultCode());
            }
        }
    };

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Objects.requireNonNull(out).close();
            Toast.makeText(getContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show();
            frameLayoutImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(uri)
                    .fit()
                    .into(backgroundImage);
            pickedImageUri = uri;
            postButton.setEnabled(true);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCameraAvailable() {
        return requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

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
        progressBar = binding.progressBar;

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
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(getContext(), "You must be logged in to take a photo", Toast.LENGTH_SHORT).show();
                } else {
                    if (isCameraAvailable()) {
                        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, 1);
                        } else {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                                pickImageLauncher.launch(takePictureIntent);
                            } else {
                                Toast.makeText(getContext(), "No app can handle the camera intent", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        Toast.makeText(getContext(), "No camera app found. Please install a camera app.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
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
                                        GeoPoint location = new GeoPoint(currentWeather.getLatitude(), currentWeather.getLongitude());
                                        Post post = new Post(
                                                editPostDescription.getText().toString(),
                                                imageDownloadLink,
                                                mAuth.getCurrentUser().getPhotoUrl().toString(),
                                                mAuth.getCurrentUser().getDisplayName(),
                                                Timestamp.now(),
                                                location);
                                        addPostToDb(post);
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("failure post: ", e.getMessage());
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            ;
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Please choose an image!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
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
