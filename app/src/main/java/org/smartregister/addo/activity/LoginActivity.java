package org.smartregister.addo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.addo.BuildConfig;
import org.smartregister.addo.R;
import org.smartregister.addo.presenter.LoginPresenter;
import org.smartregister.addo.util.Constants;
import org.smartregister.addo.view.activity.AddoSettingsActivity;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Author : Isaya Mollel on 2019-10-18.
 */
public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {

    public static final String TAG = BaseLoginActivity.class.getCanonicalName();
    private TextView tvNameEnv;
    private Button btnLogin;

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvNameEnv = findViewById(R.id.at_name);
        btnLogin = findViewById(R.id.login_login_btn);
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void goToHome(boolean b) {
        //Take user to a home page
        if (b) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        goToMainActivity(b);
        finish();

    }

    private void goToMainActivity(boolean b) {
        Intent intent = new Intent(this, AddoHomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
            startActivity(new Intent(this, AddoSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setServerURL() {
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();

        try {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Addo", "env_switch.json");

            if (file.exists()) {
                // if the file is there, then  switching has taken place and the data was cleared from the device
                // Get the environment configurations from the file and set the url based on that
/*                        Yaml yaml = new Yaml();
                        Map<String, Object> envConfig = (Map<String, Object>) yaml.load(new FileInputStream(file));*/
                JSONObject envConfig = getSwitchConfigurationsFromFile(file);

                if (envConfig != null) {

                    if (envConfig.get("env").toString().equalsIgnoreCase("test")) {

                        preferences.savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url_staging);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.ENVIRONMENT_CONFIG.PREFERENCE_PRODUCTION_ENVIRONMENT_SWITCH, false).commit();
                        preferences.savePreference(Constants.ENVIRONMENT_CONFIG.OPENSRP_ADDO_ENVIRONMENT, "test");
                        setAppNameProductionEnvironment("test");
                    } else {

                        preferences.savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url_production);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.ENVIRONMENT_CONFIG.PREFERENCE_PRODUCTION_ENVIRONMENT_SWITCH, true).commit();
                        preferences.savePreference(Constants.ENVIRONMENT_CONFIG.OPENSRP_ADDO_ENVIRONMENT, "production");
                        setAppNameProductionEnvironment("production");
                    }
                }

            } else {
                // The file does not exist, no switching environment has taken place. This is the first time the user logged into the device
                preferences.savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url_production);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.ENVIRONMENT_CONFIG.PREFERENCE_PRODUCTION_ENVIRONMENT_SWITCH, true).commit();
                preferences.savePreference(Constants.ENVIRONMENT_CONFIG.OPENSRP_ADDO_ENVIRONMENT, "production");
                setAppNameProductionEnvironment("production");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private JSONObject getSwitchConfigurationsFromFile(File file) {
        JSONObject jsonObject = new JSONObject();
        try {
            FileInputStream stream = new FileInputStream(file);
            String jString = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                /* Instead of using default, pass in a decoder. */
                jString = Charset.defaultCharset().decode(bb).toString();
            } finally {
                stream.close();
            }
            jsonObject = new JSONObject(jString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private void setAppNameProductionEnvironment(String nameAndEnvironment) {

        if ("test".equalsIgnoreCase(nameAndEnvironment)) {

            SpannableString spannableString = new SpannableString("Afya-Tek \u2022");
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getActivityContext().getResources().getColor(R.color.test_env_color));
            spannableString.setSpan(new RelativeSizeSpan(1.5f), 9, spannableString.length(), 0);
            //BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.MAGENTA);
            spannableString.setSpan(foregroundColorSpan, 9, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannableString.setSpan(backgroundColorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvNameEnv.setText(spannableString);
            btnLogin.setBackgroundColor(getResources().getColor(R.color.test_env_color));

        } else {

            SpannableString spannableString = new SpannableString("Afya-Tek \u2022");
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getActivityContext().getResources().getColor(R.color.login_background_color));
            spannableString.setSpan(new RelativeSizeSpan(1.5f), 9, spannableString.length(), 0);
            //BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.GREEN);
            spannableString.setSpan(foregroundColorSpan, 9, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannableString.setSpan(backgroundColorSpan, 9, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvNameEnv.setText(spannableString);
            btnLogin.setBackgroundColor(getResources().getColor(R.color.login_background_color));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Arrays.asList(permissions).contains(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setServerURL();
            }

        } else {
            Utils.getAllSharedPreferences().savePreference(AllConstants.DRISHTI_BASE_URL, BuildConfig.opensrp_url_production);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.ENVIRONMENT_CONFIG.PREFERENCE_PRODUCTION_ENVIRONMENT_SWITCH, true).commit();
            Utils.getAllSharedPreferences().savePreference(Constants.ENVIRONMENT_CONFIG.OPENSRP_ADDO_ENVIRONMENT, "production");
            setAppNameProductionEnvironment("production");
        }
    }
}
