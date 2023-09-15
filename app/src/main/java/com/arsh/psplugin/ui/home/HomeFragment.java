package com.arsh.psplugin.ui.home;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.arsh.psplugin.R;
import com.arsh.psplugin.databinding.FragmentHomeBinding;

import java.io.File;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private InterstitialAd mInterstitialAd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button button=root.findViewById(R.id.button);
        root.findViewById(R.id.rate_us).setOnClickListener(v->{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.arsh.psplugin")));
        });
        root.findViewById(R.id.exit_button).setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage("Are you sure you want to exit ?");
            builder.setTitle("Exit !");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                System.exit(1);
            });
            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        MobileAds.initialize(requireActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdView mAdView = root.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                loadAd();

            }
        });
        button.setOnClickListener(v->{
            if(isAppInstalled(requireActivity(),"com.kunpo88.baba2.say")) {
                if(mInterstitialAd==null){
                    launchApp();
                }else{
                    setCallBacks();
                    launchApp();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage("Arm plugin not installed do you want to install ?");
                builder.setTitle("Install Plugin !");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.kunpo88.baba2.say")));
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        return root;
    }

    void loadAd(){
        AdRequest adRequestI = new AdRequest.Builder().build();
        InterstitialAd.load(requireActivity(),"ca-app-pub-3940256099942544/1033173712", adRequestI,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("TAG", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void launchApp(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/x-iso9660-image"); // Filter to show only ISO files
        startActivityForResult(intent,100);
    }
    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }
    private void setCallBacks(){
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.");
                mInterstitialAd = null;
                loadAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.");
                mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Retrieve the URI of the selected folder
                Uri isoUri = data.getData();

                // Take persistable permission for the selected file
                requireActivity().getContentResolver().takePersistableUriPermission(isoUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Pass the URI to the other app
                requireActivity().grantUriPermission("com.kunpo88.baba2.say",isoUri,Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setComponent(new ComponentName("com.kunpo88.baba2.say", "xyz.aethersx2.android.EmulationActivity"));
                intent.putExtra("bootPath",isoUri.toString());
                requireActivity().startActivityForResult(intent,200);

                mInterstitialAd.show(requireActivity());

            }
        }else if(requestCode==200){
            loadAd();
        }
    }
}