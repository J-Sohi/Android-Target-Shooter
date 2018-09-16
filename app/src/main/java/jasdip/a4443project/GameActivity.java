package jasdip.a4443project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class GameActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGES = 0;
    private static final String TAG = "myDebug";
    String name;
    private int controlMode;
    private int targetSize;
    private boolean practiceMode;
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        checkPermission();

        // Get the Intent that started this activity and extract the extras
        Intent intent = getIntent();
        name = intent.getStringExtra(MainActivity.PARTICIPANT_NAME);
        controlMode = intent.getIntExtra(MainActivity.CONTROL_MODE, 0);
        targetSize = intent.getIntExtra(MainActivity.TARGET_SIZE, 0);
        practiceMode = intent.getBooleanExtra(MainActivity.PRACTICE_MODE, false);
        Log.d(TAG, "practice initial: " + practiceMode);

        //get gameView and set parameters
        gameView = findViewById(R.id.gameView);
        gameView.setOptions(controlMode, targetSize);

        //get joystick and set listener
        JoystickView joystick = findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                gameView.joystickMove(angle, strength);
            }
        }, 10);

        //remove joystick if swipe mode
        if(controlMode == 1)
            ((ViewGroup)joystick.getParent()).removeView(joystick);

        //debug
        /*ConstraintLayout lView = (ConstraintLayout) findViewById(R.id.gameLayout);
        TextView myText = new TextView(this);
        myText.setText(name + " " + controlMode + " " + targetSize);
        myText.setTextColor(0xff000000);
        lView.addView(myText);*/
    }

    public void endGame() throws IOException {
        //get results, set resultView, get textview ids
        String results[] = gameView.getResults();
        setContentView(R.layout.results_layout);
        TextView cMode = findViewById(R.id.cMode);
        TextView tSize = findViewById(R.id.tSize);
        TextView time = findViewById(R.id.time);
        TextView accuracy = findViewById(R.id.accuracy);

        //set textviews with results
        time.setText("Time: " + results[0] +"s");
        accuracy.setText("Accuracy: " + results[1] + "%");
        if(controlMode == 0)
            cMode.setText("Control Mode: Joystick" );
        else
            cMode.setText("Control Mode: Swipe" );
        if(targetSize == 30)
            tSize.setText("Target Size: Small" );
        else
            tSize.setText("Target Size: Large" );


        //If practice mode is on, don't write results.
        Log.d(TAG, "practice: " + practiceMode);
        if(!practiceMode)
            writeResults(results[0], results[1]);
    }

    /**
    * Creates results file in public podcasts folder. Must be deleted by user manually!
    **/
    private void writeResults(String time, String accuracy) throws IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS), "4443Project.txt");
        FileWriter writer = new FileWriter(file, true);
        writer.append(name + "," + controlMode + "," + targetSize + "," + time + "," + accuracy + "\r\n");
        writer.flush();
        writer.close();
    }

    public void quit(View view){
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        final View decorView = this.getWindow().getDecorView();
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGES);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        } else {
            // Permission has already been granted
        }
    }
}
