package com.jc897407.shubhamcurrencyconverterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String[] lan;
    String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lan = getResources().getStringArray(R.array.lan);
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        currentLanguage = preferences.getString("my_lang", "EN");
        setLocale(currentLanguage);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.app_name);

        // Display current Time
        displayTime();

        // Click button to Convert Currency
        Button convertCurrency = (Button) findViewById(R.id.convertCurrency);
        convertCurrency.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    currencyConverter();
                Toast.makeText(MainActivity.this, "Currency Converted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.language).setTitle(currentLanguage);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.language){
            changeLanguage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Displaying alert dialog box to choose language from
    private void changeLanguage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language..");
        builder.setSingleChoiceItems(lan, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setLocale(lan[i]);
                recreate();
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        sharedPreferences.edit().putString("my_lang", lang).apply();
    }


    private void displayTime() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String currentTime = Helper.getCurrentTime();
                TextView timeView = findViewById(R.id.timeView);
                timeView.setText(currentTime);
                handler.postDelayed(this, 1000); //1000 ms = 1 s
            }
        });
    }

    private void currencyConverter(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Select the country currency
                Spinner convertFrom = findViewById(R.id.selectCurrency);
                String currencyFrom = convertFrom.getSelectedItem().toString();

                //Choose the country currency you want to convert
                Spinner convertTo = findViewById(R.id.selectCurrency2);
                String currencyTo = convertTo.getSelectedItem().toString();

                // Enter the amount you want to convert
                EditText enterAmount = findViewById(R.id.enterAmount);
                double amount = Double.parseDouble(enterAmount.getText().toString());
                
                String rateInfoStr = Helper.getInfo(Helper.RATE_API_CORE + currencyFrom);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rateInfoStr != null) {
                            Log.i("rateInfoStr", rateInfoStr);
                            try {
                                JSONObject jsonObject = new JSONObject(rateInfoStr);
                                JSONObject rateObject = jsonObject.getJSONObject("rates");
                                double convertedAmount = amount * rateObject.getDouble(currencyTo);
                                Log.i("convertedAmount",
                                        String.format("%.2f", convertedAmount));

                                TextView result = findViewById(R.id.boldResult);
                                TextView conRes = findViewById(R.id.result);
                                conRes.setText(String.format("%.2f", convertedAmount));
                                result.setText(String.format("%.2f %s", convertedAmount, currencyTo));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Log.i("rateInfoStr", "No Data");
                        }
                    }
                });
            }
        }).start();
    }
}
