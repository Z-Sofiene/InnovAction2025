package com.example.iot;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private TextView  batteryText, angleText, distanceText, humidityText;

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
        
        batteryText = findViewById(R.id.text_battery);
        angleText = findViewById(R.id.text_angle);
        distanceText = findViewById(R.id.text_distance);
        humidityText = findViewById(R.id.text_humidity);
        fetchData();
    }

    private void fetchData() {
        String url = "http://YourBackendIpAndPort/api/data/stream";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API Failure", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        final String batteryLevel = jsonObject.getString("battery_lvl");
                        final String panelAngle = jsonObject.getString("angel_panneau");
                        final String distance = jsonObject.getString("distance");
                        final String soilHumidity = jsonObject.getString("humidite_sol");
                        
                        runOnUiThread(() -> {
                            batteryText.setText("Battery: " + batteryLevel + " %");
                            angleText.setText("Panel Angle: " + panelAngle + " °");
                            distanceText.setText("" + distance + " cm");
                            humidityText.setText("" + soilHumidity + " %");
                        });

                    } catch (JSONException e) {
                        Log.e("JSON Error", "Failed to parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("API Error", "Response Code: " + response.code());
                }
            }
        });
    }
}

