package jasdip.a4443project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public static final String PARTICIPANT_NAME = "PARTICIPANT_NAME";
    public static final String CONTROL_MODE = "CONTROL_MODE";
    public static final String TARGET_SIZE = "TARGET_SIZE";
    public static final String PRACTICE_MODE = "PRACTICE_MODE";

    Button buttonMode;
    Button buttonSize;
    ToggleButton buttonPractice;
    int controlMode = 0;
    int targetSize = 30;
    boolean practiceMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonMode = findViewById(R.id.buttonMode);
        buttonSize = findViewById(R.id.buttonSize);
        buttonPractice = findViewById(R.id.practice);
        setPracticeListener();
    }

    public void toggleMode(View button) {
        if (controlMode == 0) {
            buttonMode.setText("Swipe");
            controlMode = 1;
        }
        else {
            buttonMode.setText("Joystick");
            controlMode = 0;
        }
    }

    public void toggleSize(View button) {
        if (targetSize == 30) {
            buttonSize.setText("Large");
            targetSize = 50;
        }
        else {
            buttonSize.setText("Small");
            targetSize = 30;
        }
    }

    public void setPracticeListener(){
        buttonPractice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                practiceMode = isChecked;
            }
        });
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        EditText editText = findViewById(R.id.participant_name);
        String name = editText.getText().toString();

        intent.putExtra(PARTICIPANT_NAME, name);
        intent.putExtra(CONTROL_MODE, controlMode);
        intent.putExtra(TARGET_SIZE, targetSize);
        intent.putExtra(PRACTICE_MODE, practiceMode);
        startActivity(intent);
    }
}
