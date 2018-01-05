package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final Toolbar helptoolbar = (Toolbar) findViewById(R.id.helptoolbar);
        setSupportActionBar(helptoolbar);
        final ActionBar ab = getSupportActionBar();
        assert null != ab;
        ab.setDisplayHomeAsUpEnabled(true);

        final Button contact = (Button) findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                final String[] address = {"COD3LTA@simonbohnen.me"};
                intent.putExtra(Intent.EXTRA_EMAIL, address);
                intent.putExtra(Intent.EXTRA_SUBJECT, "QuadraSolve Support");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        final ImageView codelta = (ImageView) findViewById(R.id.cod3lta);
        codelta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri twitteruri = Uri.parse("https://twitter.com/cod3lta");
                final Intent intent = new Intent(Intent.ACTION_VIEW, twitteruri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
