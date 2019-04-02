package me.factory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class InputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        final EditText et = findViewById(R.id.etEmail);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et.getText().toString();
                Intent intent = new Intent(InputActivity.this, MainActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }
}
