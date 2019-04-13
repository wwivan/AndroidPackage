package me.factory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.yzxing.CameraMainActivity;
import java.util.HashSet;
import java.util.Set;

public class QrcodeActivity extends AppCompatActivity {

    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        text = (TextView) findViewById(R.id.text);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(QrcodeActivity.this, CameraMainActivity.class),1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            //显示扫描到的内容
            String qrcode = bundle.getString("result");
            text.setText(qrcode);
            Intent result = new Intent();
            result.putExtra("result", qrcode);
            setResult(803, result);
            QrcodeActivity.this.finish();
        }

    }
}
