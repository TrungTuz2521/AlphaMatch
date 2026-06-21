package com.example.twochar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class gameActivity extends AppCompatActivity {

    Button btnGameHome, btnGameRestart, btnGamePause;
    GridView grvGame;
    TextView tvMascotMsg;
    int level;
    int firstPosition = -1;
    boolean isBusy = false;
    boolean isPaused = false;

    ArrayList<String> letterList;
    GameAdapter adapter;
    MediaPlayer mediaPlayer;
    
    // Âm thanh hiệu ứng (SFX)
    private SoundPool soundPool;
    private int soundFlip, soundMatch, soundFail;
    private boolean isSfxEnabled = true;

    TextView tvPairCount, tvLevel, tvTimer;
    int matchedPairCount = 0;
    Handler timerHandler = new Handler(Looper.getMainLooper());
    int secondsElapsed = 0;
    Runnable timerRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initWidget();
        
        level = getIntent().getIntExtra("LEVEL", 1);
        isSfxEnabled = getIntent().getBooleanExtra("MUSIC_ON", true);
        
        // --- KHỞI TẠO ÂM THANH ---
        initSound();
        if (isSfxEnabled) {
            mediaPlayer = MediaPlayer.create(this, R.raw.backgroundmusic);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        initGame(level);

        btnGameHome.setOnClickListener(v -> finish());
        btnGameRestart.setOnClickListener(v -> initGame(level));
        btnGamePause.setOnClickListener(v -> showPauseDialog());
    }

    private void initSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Tải các file âm thanh (Cần thêm vào res/raw)
        soundFlip = soundPool.load(this, R.raw.sfx_flip, 1);
        soundMatch = soundPool.load(this, R.raw.sfx_match, 1);
        soundFail = soundPool.load(this, R.raw.sfx_fail, 1);
    }

    private void playSfx(int soundId) {
        if (isSfxEnabled && soundPool != null && !isPaused) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    private void initWidget() {
        btnGameHome    = findViewById(R.id.btnGameHome);
        btnGameRestart = findViewById(R.id.btnGameRestart);
        btnGamePause   = findViewById(R.id.btnGamePause);
        grvGame        = findViewById(R.id.grvGame);
        tvPairCount    = findViewById(R.id.tvPairCount);
        tvLevel        = findViewById(R.id.tvLevel);
        tvTimer        = findViewById(R.id.tvTimer);
        tvMascotMsg    = findViewById(R.id.tvMascotMsg);
    }

    private void initGame(int level) {
        firstPosition    = -1;
        isBusy           = false;
        isPaused         = false;
        matchedPairCount = 0;

        // Tối ưu hóa kích thước lưới cho di động
        int columns, rows;
        if (level == 2) {
            columns = 6;
            rows = 8;
        } else if (level == 3) {
            columns = 8;
            rows = 12;
        } else {
            columns = 4;
            rows = 6;
        }

        String[] labels = {"Dễ", "Vừa", "Khó"};
        tvLevel.setText(labels[level - 1]);

        int totalPairs = (columns * rows) / 2;
        tvPairCount.setText("0 / " + totalPairs);

        grvGame.setNumColumns(columns);
        initData(columns, rows);
        setupSquareItem(columns);
        startTimer();
        tvMascotMsg.setText("Bắt đầu lật thẻ và tìm cặp giống nhau nào! 🎯");
    }

    private void startTimer() {
        stopTimer();
        secondsElapsed = 0;
        resumeTimer();
    }

    private void resumeTimer() {
        stopTimer();
        if (timerRunnable == null) {
            timerRunnable = new Runnable() {
                @Override public void run() {
                    secondsElapsed++;
                    int m = secondsElapsed / 60;
                    int s = secondsElapsed % 60;
                    tvTimer.setText(String.format("%02d:%02d", m, s));
                    timerHandler.postDelayed(this, 1000);
                }
            };
        }
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
    }

    private void showPauseDialog() {
        isPaused = true;
        stopTimer();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();

        Dialog pauseDialog = new Dialog(this);
        pauseDialog.setContentView(R.layout.pause_dialog);
        pauseDialog.setCancelable(false);
        
        if (pauseDialog.getWindow() != null) {
            pauseDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85f);
            pauseDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        Button btnResume = pauseDialog.findViewById(R.id.btnResume);
        Button btnQuit = pauseDialog.findViewById(R.id.btnQuit);

        btnResume.setOnClickListener(v -> {
            isPaused = false;
            resumeTimer();
            if (mediaPlayer != null && isSfxEnabled) mediaPlayer.start();
            pauseDialog.dismiss();
        });

        btnQuit.setOnClickListener(v -> {
            pauseDialog.dismiss();
            finish();
        });

        pauseDialog.show();
    }

    private void setupSquareItem(int columns) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int gridMargin  = dpToPx(3) * 2;
        int totalSpace  = gridMargin;
        int itemSize    = (screenWidth - totalSpace) / columns;

        adapter = new GameAdapter(this, itemSize, letterList);
        grvGame.setAdapter(adapter);
        grvGame.setOnItemClickListener((parent, view, position, id) -> handleClick(position, view));
    }

    private void handleClick(int position, View view) {
        if (isBusy || isPaused || adapter.isRevealed.get(position) || adapter.isMatched.get(position)) return;

        playSfx(soundFlip);

        applyFlipAnimation(view, () -> {
            adapter.isRevealed.set(position, true);
            adapter.notifyDataSetChanged();

            if (firstPosition == -1) {
                firstPosition = position;
            } else {
                isBusy = true;
                final int prevPos = firstPosition;
                firstPosition = -1;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (letterList.get(prevPos).equals(letterList.get(position))) {
                        playSfx(soundMatch);
                        adapter.isMatched.set(prevPos, true);
                        adapter.isMatched.set(position, true);
                        matchedPairCount++;
                        
                        String[] cheers = {"Tuyệt vời! 🌟", "Bạn rất giỏi! 💪", "Wow, đỉnh thật! 🎉", "Trí nhớ siêu đỉnh! 🧠"};
                        tvMascotMsg.setText(cheers[matchedPairCount % cheers.length]);
                        
                        int totalPairs = letterList.size() / 2;
                        tvPairCount.setText(matchedPairCount + " / " + totalPairs);
                        checkWin();
                    } else {
                        playSfx(soundFail);
                        adapter.isRevealed.set(prevPos, false);
                        adapter.isRevealed.set(position, false);
                        tvMascotMsg.setText("Không khớp rồi, thử lại nhé! 🔍");
                    }
                    isBusy = false;
                    adapter.notifyDataSetChanged();
                }, 500);
            }
        });
    }

    private void checkWin() {
        for (boolean b : adapter.isMatched) {
            if (!b) return;
        }
        stopTimer();
        showWinDialog();
    }

    private void showWinDialog() {
        Dialog winDialog = new Dialog(this);
        winDialog.setContentView(R.layout.win_dialog);
        winDialog.setCancelable(false);

        if (winDialog.getWindow() != null) {
            winDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.88f);
            winDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvWinTime  = winDialog.findViewById(R.id.tvWinTime);
        TextView tvWinPairs = winDialog.findViewById(R.id.tvWinPairs);
        TextView tvWinLevel = winDialog.findViewById(R.id.tvWinLevel);
        TextView tvWinRate = winDialog.findViewById(R.id.tvWinRate);
        TextView tvWinTitle = winDialog.findViewById(R.id.tvWinTitle);
        Button btnReplay    = winDialog.findViewById(R.id.btnWinReplay);
        Button btnHome      = winDialog.findViewById(R.id.btnWinHome);

        tvWinTime.setText(tvTimer.getText());
        int totalPairs = letterList.size() / 2;
        tvWinPairs.setText(totalPairs + " / " + totalPairs);
        String[] labels = {"Dễ", "Vừa", "Khó"};
        tvWinLevel.setText(labels[level - 1]);
        
        int time = secondsElapsed;
        if(level == 1) {
            if(time < 120) { tvWinRate.setText("⭐⭐⭐"); tvWinTitle.setText("Tuyệt Vời!"); }
            else if(time < 180) { tvWinTitle.setText("Có cố gắng!"); tvWinRate.setText("⭐⭐"); }
            else { tvWinTitle.setText("Cần cố gắng hơn!"); tvWinRate.setText("⭐"); }
        } else if (level == 2) {
            if(time < 360) { tvWinTitle.setText("Tuyệt Vời!"); tvWinRate.setText("⭐⭐⭐"); }
            else if(time < 480) { tvWinTitle.setText("Có cố gắng!"); tvWinRate.setText("⭐⭐"); }
            else { tvWinTitle.setText("Cần cố gắng hơn!"); tvWinRate.setText("⭐"); }
        } else {
            if(time < 900) { tvWinTitle.setText("Tuyệt Vời!"); tvWinRate.setText("⭐⭐⭐"); }
            else if(time < 1020) { tvWinTitle.setText("Có cố gắng!"); tvWinRate.setText("⭐⭐"); }
            else { tvWinTitle.setText("Cần cố gắng hơn!"); tvWinRate.setText("⭐"); }
        }

        btnReplay.setOnClickListener(v -> { winDialog.dismiss(); initGame(level); });
        btnHome.setOnClickListener(v -> { winDialog.dismiss(); finish(); });

        winDialog.show();
    }

    private void applyFlipAnimation(View view, Runnable onMidPoint) {
        ScaleAnimation scaleOut = new ScaleAnimation(1f, 0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleOut.setDuration(150);
        scaleOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a)  {}
            @Override public void onAnimationRepeat(Animation a) {}
            @Override public void onAnimationEnd(Animation a) {
                onMidPoint.run();
                ScaleAnimation scaleIn = new ScaleAnimation(0f, 1f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleIn.setDuration(150);
                view.startAnimation(scaleIn);
            }
        });
        view.startAnimation(scaleOut);
    }

    public class GameAdapter extends BaseAdapter {
        private final Context context;
        private final int itemSize;
        private final ArrayList<String> data;
        public final ArrayList<Boolean> isRevealed;
        public final ArrayList<Boolean> isMatched;

        public GameAdapter(Context context, int itemSize, ArrayList<String> data) {
            this.context    = context;
            this.itemSize   = itemSize;
            this.data       = data;
            this.isRevealed = new ArrayList<>();
            this.isMatched  = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                isRevealed.add(false);
                isMatched.add(false);
            }
        }

        @Override public int getCount() { return data.size(); }
        @Override public Object getItem(int pos) { return data.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
                view.setLayoutParams(new GridView.LayoutParams(itemSize, itemSize));
                androidx.cardview.widget.CardView card = (androidx.cardview.widget.CardView) view;
                card.setRadius(Math.min(itemSize * 0.15f, dpToPx(8)));
            } else {
                view = convertView;
            }

            androidx.cardview.widget.CardView card = (androidx.cardview.widget.CardView) view;
            TextView txt = view.findViewById(R.id.txtLetter);

            if (isMatched.get(position)) {
                card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                txt.setText(data.get(position));
                txt.setTextColor(Color.parseColor("#1B5E20"));
            } else if (isRevealed.get(position)) {
                card.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
                txt.setText(data.get(position));
                txt.setTextColor(Color.parseColor("#ff0066"));
            } else {
                card.setCardBackgroundColor(Color.parseColor("#1565C0"));
                txt.setText("★");
                txt.setTextColor(Color.parseColor("#ffff00"));
            }
            return view;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
            stopTimer();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPaused) {
            resumeTimer();
            if (mediaPlayer != null && isSfxEnabled) mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; }
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    private ArrayList<String> createSourceChars() {
        ArrayList<String> source = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) source.add(String.valueOf(c));
        for (char c = 'a'; c <= 'z'; c++) source.add(String.valueOf(c));
        for (char c = '0'; c <= '9'; c++) source.add(String.valueOf(c));
        Collections.shuffle(source);
        return source;
    }

    private void initData(int columns, int rows) {
        int total = columns * rows;
        int pairCount = total / 2;
        ArrayList<String> source = createSourceChars();
        letterList = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            String value = source.get(i % source.size());
            letterList.add(value);
            letterList.add(value);
        }
        Collections.shuffle(letterList);
    }
}
