package com.example.twochar;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnStart , btnExit , btnSetting , btnInfor ,btnLevel;

    MediaPlayer musicBack;
    boolean isMusicOn = true;

    int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initWidget();
        PlayMusic();
        btnExit.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Xác nhận")
            .setMessage("Bạn có muốn thoát ứng dụng không?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Có", (dialog, which) -> {
                finishAffinity(); // thoát app
            })
                    .setNegativeButton("Không", (dialog, which) -> {
                dialog.dismiss(); // đóng hộp thoại
            })
                    .show();
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingDialog();
            }
        });
        btnLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelDialog();
            }
        });
        btnInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inforDialog();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,gameActivity.class);
                intent.putExtra("LEVEL", level);
                intent.putExtra("MUSIC_ON", isMusicOn); // Truyền biến âm thanh (true là bật, false là tắt)
                startActivity(intent);
            }
        });
    }

    // các hàm thao tac với nhạc nền
    @Override
    protected void onPause() {
        super.onPause();
        if (musicBack != null && musicBack.isPlaying()) {
            musicBack.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(isMusicOn
                && musicBack != null
                && !musicBack.isPlaying()) {

            musicBack.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicBack != null) {
            musicBack.release();
            musicBack = null;
        }
    }

    //Hàm Khai báo các nút
    private void initWidget(){
        btnStart = findViewById(R.id.btnStart);
        btnLevel = findViewById(R.id.btnLevel);
        btnInfor = findViewById(R.id.btnIfor);
        btnSetting = findViewById(R.id.btnSetting);
        btnExit = findViewById(R.id.btnExit);
    }

    // mở của sổ setting
    private void settingDialog(){
        Dialog settingDialog = new Dialog(this);
        settingDialog.setContentView(R.layout.setting_layout);

        if (settingDialog.getWindow() != null) {
            settingDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        RadioButton rbtnMusicOn = settingDialog.findViewById(R.id.rbtnMusicOn);
        RadioButton rbtnMusicOff = settingDialog.findViewById(R.id.rbtnMusicOff);

        Button btnCanelSetting = settingDialog.findViewById(R.id.btnCancelSetting);
        Button btnokSetting = settingDialog.findViewById(R.id.btnOkSetting);
        rbtnMusicOn.setChecked(isMusicOn);
        rbtnMusicOff.setChecked(!isMusicOn);



        btnCanelSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingDialog.dismiss();
            }
        });
        btnokSetting.setOnClickListener(v -> {
            // ==== MUSIC ====
            if (rbtnMusicOn.isChecked()) {
                isMusicOn = true;
                if (musicBack == null || !musicBack.isPlaying()) {
                    PlayMusic();
                }
            } else {
                isMusicOn = false;
                if (musicBack != null && musicBack.isPlaying()) {
                    musicBack.pause();
                }
            }

            settingDialog.dismiss();
        });

        settingDialog.show();

        // Đặt width 90% màn hình, không bị quá hẹp hay quá rộng
        if (settingDialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
            settingDialog.getWindow().setLayout(width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    //Hàm phát nhạc
    private void PlayMusic() {
        if (musicBack == null) {
            musicBack = MediaPlayer.create(
                    this,
                    R.raw.backgroundmusic);

            musicBack.setLooping(true);
        }

        if (!musicBack.isPlaying()) {
            musicBack.start();
        }
    }

    //Mở cửa sổ level
    private void levelDialog(){
        Dialog levelDialog = new Dialog(this);
        levelDialog.setContentView(R.layout.level_layout);
        CardView btnLevel1 = levelDialog.findViewById(R.id.btnLevel1);
        CardView btnLevel2 = levelDialog.findViewById(R.id.btnLevel2);
        CardView btnLevel3 = levelDialog.findViewById(R.id.btnLevel3);
        Button btnLevelCancel = levelDialog.findViewById(R.id.btnLevelCancel);

        btnLevelCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelDialog.dismiss();
            }
        });
        btnLevel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = 1;
                btnLevel.setText("Level 1");
                levelDialog.dismiss();
            }
        });
        btnLevel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = 2;
                btnLevel.setText("Level 2");
                levelDialog.dismiss();
            }
        });
        btnLevel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = 3;
                btnLevel.setText("Level 3");
                levelDialog.dismiss();
            }
        });
        levelDialog.show();
    }

    // hiển thị cửa sổ information
    private void inforDialog() {
        Dialog inforDialog = new Dialog(this);
        inforDialog.setContentView(R.layout.infor_layout);

        // Quan trọng: trong suốt nền Dialog để bo góc của bg_dialog_outer hiện ra đúng
        if (inforDialog.getWindow() != null) {
            inforDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.88f);
            inforDialog.getWindow().setLayout(width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        Button btnInforExit = inforDialog.findViewById(R.id.btnInforExit);
        btnInforExit.setOnClickListener(v -> inforDialog.dismiss());

        inforDialog.show();
    }


}