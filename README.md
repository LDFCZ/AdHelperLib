# AdHelperLib

## Инструкция по добавлению данной библиотеки в свое Android приложение.
[Проект с примером](https://github.com/LDFCZ/PreviewApp)  

Для того, чтобы импортировать данную библиотеку к себе в проект нужно:
1. Добавть в ```settings.gradle```
``` java
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. Добавить в ```build.gradle```
``` java
dependencies {
    ...
    implementation 'com.github.LDFCZ:AdHelperLib:tag'
}
```
Где ```tag``` - номер релиза.

3. Создать ```Activity``` c WebView.     
Пример: 
``` xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".AdHelper">
    <WebView
            android:id="@+id/webView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
    />

</androidx.constraintlayout.widget.ConstraintLayout>
```
4. Добавить в ```AndroidManifest.xml``` следущие разрешения:
``` xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" tools:ignore="CoarseFineLocation"/>
    <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
```

5. В созданном ```Activity``` добавляем следущий код: 
``` java
public class AdHelper extends AppCompatActivity {

    private AdInitializer adInitializer;

    private String geolocation;

    private String adId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_helper);


        adInitializer = new AdInitializer(AdHelper.this, "your app token", R.id.webView);
        adInitializer.loadPreferences(getPreferences(MODE_PRIVATE));


        if (adId == null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                //Background work here
                adInitializer.getAdIdFromDevice();
                this.adId = adInitializer.getAdId();
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        adInitializer.showAd();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (geolocation == null) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        //Background work here
                        adInitializer.getGeolocationFromDevice();
                        this.geolocation = adInitializer.getGeolocation();
                    });
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adInitializer.savePreferences(getPreferences(MODE_PRIVATE));
    }
}
```
