package de.jamesbeans.quadrasolve;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

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
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.simonbohnen.me/contact"));
                startActivity(browserIntent);
            }
        });
    }
}
