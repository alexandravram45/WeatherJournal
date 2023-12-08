package com.example.weatherjournal2.ui.newPost;

import androidx.lifecycle.ViewModel;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class NewPostViewModel extends ViewModel {
    private EditText editPostDescription;
    private Button chooseFromGalleryButton;
    private Button takeAPhotoButton;
    private FrameLayout frameLayoutImage;
    private ImageView backgroundImage;
    private TextView postLocation;
    private TextView postTemperature;
    private Button postButton;
    private ImageView conditionIcon;

    public ImageView getConditionIcon() {
        return conditionIcon;
    }

    public void setConditionIcon(ImageView conditionIcon) {
        this.conditionIcon = conditionIcon;
    }

    public EditText getEditPostDescription() {
        return editPostDescription;
    }

    public void setEditPostDescription(EditText editPostDescription) {
        this.editPostDescription = editPostDescription;
    }

    public Button getChooseFromGalleryButton() {
        return chooseFromGalleryButton;
    }

    public void setChooseFromGalleryButton(Button chooseFromGalleryButton) {
        this.chooseFromGalleryButton = chooseFromGalleryButton;
    }

    public Button getTakeAPhotoButton() {
        return takeAPhotoButton;
    }

    public void setTakeAPhotoButton(Button takeAPhotoButton) {
        this.takeAPhotoButton = takeAPhotoButton;
    }

    public FrameLayout getFrameLayoutImage() {
        return frameLayoutImage;
    }

    public void setFrameLayoutImage(FrameLayout frameLayoutImage) {
        this.frameLayoutImage = frameLayoutImage;
    }

    public ImageView getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(ImageView backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public TextView getPostLocation() {
        return postLocation;
    }

    public void setPostLocation(TextView postLocation) {
        this.postLocation = postLocation;
    }

    public TextView getPostTemperature() {
        return postTemperature;
    }

    public void setPostTemperature(TextView postTemperature) {
        this.postTemperature = postTemperature;
    }

    public Button getPostButton() {
        return postButton;
    }

    public void setPostButton(Button postButton) {
        this.postButton = postButton;
    }
}
