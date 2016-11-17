package com.ffinder.android.extensions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.IProfileImagePickerListener;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.helpers.AndroidUtils;
import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Created by sionglengho on 22/10/16.
 */
public class ProfileImageView extends RelativeLayout {

    private Context context;
    private int backgroundOnTapColor, srcNormalColor, srcOnTapColor;
    private RelativeLayout layoutImageViewProfile;
    private TextView txtShortFormName;
    private RoundedImageView profileImage;
    private IProfileImagePickerListener profileImagePickerListener;

    public ProfileImageView(Context context) {
        super(context);
        init(context);
    }

    public ProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProfileImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context){
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_view_profile, this, true);

        layoutImageViewProfile = (RelativeLayout) this.findViewById(R.id.layoutImageViewProfile);
        txtShortFormName = (TextView) this.findViewById(R.id.txtShortFormName);
        profileImage = (RoundedImageView) this.findViewById(R.id.profileImage);

        backgroundOnTapColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        srcNormalColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        srcOnTapColor = ContextCompat.getColor(context, R.color.colorContrast);

        this.setClickable(true);
        this.setFocusable(true);
        setListener();
    }

    public void setShortFormName(String name){
        this.txtShortFormName.setText(name);
    }

    public void setProfileImageIfAvailable(FriendModel friendModel){

        if(friendModel.getHasProfileImage() == null){
            Bitmap bitmap = AndroidUtils.loadImageFromStorage(context, friendModel.getUserId());
            if (bitmap != null){
                friendModel.setHasProfileImage(true);
                friendModel.setFriendProfileImage(bitmap);
            }
            else{
                friendModel.setHasProfileImage(false);

            }
        }

        if(friendModel.getHasProfileImage()){
            profileImage.setVisibility(VISIBLE);
            profileImage.setImageBitmap(friendModel.getFriendProfileImage());
        }
        else{
            profileImage.setVisibility(GONE);
        }


    }

    public void setListener(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                    AnimateBuilder.animateBackgroundTintColor(Color.argb(0, 255, 255, 255),
                            backgroundOnTapColor, layoutImageViewProfile);
                    AnimateBuilder.animateTextColor(srcNormalColor, srcOnTapColor,
                            txtShortFormName);

                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    AnimateBuilder.animateBackgroundTintColor(backgroundOnTapColor,
                            Color.argb(0, 255, 255, 255), layoutImageViewProfile);

                    AnimateBuilder.animateTextColor(srcOnTapColor, srcNormalColor,
                            txtShortFormName);
                }
                return false;
            }
        });

    }

    public void setProfileImagePickerListener(IProfileImagePickerListener profileImagePickerListener) {
        this.profileImagePickerListener = profileImagePickerListener;
    }
}
