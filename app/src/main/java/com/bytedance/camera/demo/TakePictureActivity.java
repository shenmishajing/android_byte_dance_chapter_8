package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 101;
    private File imageFile;

    private String[] permissions = new String[]{
            //todo 在这里申请相机、存储的权限
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (Utils.isPermissionsReady(TakePictureActivity.this, permissions)) {
                takePicture();
            } else {
                Utils.reuqestPermissions(TakePictureActivity.this, permissions, REQUEST_EXTERNAL_STORAGE);
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if (imageFile != null) {
            Uri uri = FileProvider.getUriForFile(this, "com.bytedance.camera.demo", imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //todo 处理返回数据

            setPic();
        }
    }

    private void setPic() {
        //todo 根据imageView裁剪

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        //todo 根据缩放比例读取文件，生成Bitmap
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        //todo 如果存在预览方向改变，进行图片旋转
        Bitmap bitmap = Utils.rotateImage(BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options), imageFile.getAbsolutePath());

        //todo 显示图片
        imageView.setImageBitmap(bitmap);
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile.getAbsolutePath()}, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                //todo 判断权限是否已经授予
                if (Utils.isPermissionsReady(TakePictureActivity.this, permissions)) {
                    takePicture();
                }
                break;
            }
        }
    }
}
