package com.mthotengera.androidpayintegration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.BuyButtonText;
import com.google.android.gms.wallet.fragment.Dimension;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private SupportWalletFragment mWalletFragment;
    private MaskedWallet mMaskedWallet;
    private FullWallet mFullWallet;
    public static final int MASKED_WALLLET_REQUEST_CODE = 888;
    public static final int FULL_WALLET_REQUEST_CODE = 889;

    public static final String WALLET_FRAGMENT_ID = "wallet_fragment";
    private GoogleApiClient mGoogleClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check if the mWalletFragment already exists
        mWalletFragment = (SupportWalletFragment) getSupportFragmentManager().findFragmentByTag(WALLET_FRAGMENT_ID);

        if(mWalletFragment == null){
            //Wallet fragment style
            WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                    .setBuyButtonText(BuyButtonText.BUY_NOW)
                    .setBuyButtonWidth(Dimension.MATCH_PARENT);

            // Addding Wallet fragment options
            WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build();

            // passing the wallet Options to Support WalletFragment

            mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

            // Iniatiate the WalletFragment using WalletFragmentInitParams

            WalletFragmentInitParams.Builder walletParams = WalletFragmentInitParams.newBuilder()
                    .setMaskedWalletRequest(generateMaskedWalletRequest())
                    .setMaskedWalletRequestCode(MASKED_WALLLET_REQUEST_CODE)
                    .setAccountName("Testing Google Wallet API");

            mWalletFragment.initialize(walletParams.build());


            // Adding Wallet UI to the application

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.wallet_button_holder, mWalletFragment, WALLET_FRAGMENT_ID)
                    .commit();
        }

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();
    }

    @Override
    protected void onStart() {
        mGoogleClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    public void requestFullWallet(View view){
        if(mWalletFragment == null){
            // Clicked on Confirm button without entering required details
            Snackbar.make(findViewById(android.R.id.content), "No Masked Wallet ",Snackbar.LENGTH_LONG).show();
            return;
        }
        Wallet.Payments.loadFullWallet(mGoogleClient,generateFullMaskedWalletRequest(mFullWallet.getGoogleTransactionId()),FULL_WALLET_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case MASKED_WALLLET_REQUEST_CODE:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                        Snackbar.make(findViewById(android.R.id.content), "Request successful ",Snackbar.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar.make(findViewById(android.R.id.content), "Request got canceled ",Snackbar.LENGTH_LONG).show();
                        break;
                    case WalletConstants.RESULT_ERROR:
                        Snackbar.make(findViewById(android.R.id.content), "An error occurred ",Snackbar.LENGTH_LONG).show();
                        break;

                }
                break;
            case FULL_WALLET_REQUEST_CODE:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        mFullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                        Snackbar.make(findViewById(android.R.id.content), "Full Wallet Request successful " + mFullWallet.getProxyCard().getPan(),Snackbar.LENGTH_LONG).show();
                        Wallet.Payments.notifyTransactionStatus(mGoogleClient, generateNotificationTransactionRequest(mFullWallet.getGoogleTransactionId(), NotifyTransactionStatusRequest.Status.SUCCESS));
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar.make(findViewById(android.R.id.content), "Full Wallet Request got canceled ",Snackbar.LENGTH_LONG).show();
                        break;
                    case WalletConstants.RESULT_ERROR:
                        Snackbar.make(findViewById(android.R.id.content), "An error occurred Full wallet",Snackbar.LENGTH_LONG).show();
                        break;

                }
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private MaskedWalletRequest generateMaskedWalletRequest(){
        MaskedWalletRequest maskedWalletRequest = MaskedWalletRequest.newBuilder()
                .setMerchantName(" Android Pay testing app")
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode("USD")
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.00")
                        .addLineItem(LineItem.newBuilder()
                            .setCurrencyCode("USD")
                            .setTotalPrice("10.00")
                            .setDescription("Sample Unit I")
                            .setQuantity("1")
                            .setUnitPrice("10.00")
                            .build()
                        ).build())
                .setEstimatedTotalPrice("$15.00")
                .build();
        return maskedWalletRequest;
    }
    private FullWalletRequest generateFullMaskedWalletRequest(String googleTransactionId){
        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.10")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Google I/O Sticker")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
        return fullWalletRequest;
    }
    public static NotifyTransactionStatusRequest generateNotificationTransactionRequest(String googleTransactionId, int status){
        return NotifyTransactionStatusRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setStatus(status)
                .build();
    }
}
