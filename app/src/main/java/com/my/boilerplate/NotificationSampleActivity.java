package com.my.boilerplate;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.my.boilerplate.view.SampleMenuAdapter;

public class NotificationSampleActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected ListView mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMenu = (ListView) findViewById(R.id.menu);
        mMenu.setAdapter(onSampleMenuCreate());
        mMenu.setOnItemClickListener(onClickMenuItem());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onSampleMenuCreate() {
        return new SampleMenuAdapter(
            this,
            new Pair[] {
                new Pair<>("A simple notification",
                           "The simplest notification that don't respond to " +
                           "the click."),
                new Pair<>("A notification redirecting to an Activity",
                           "The notification that lead you to the Activity " +
                           "with back stack to the StartActivity."),
                new Pair<>("A notification redirecting to a new Task",
                           "The notification that lead you to a new Task " +
                           "along with an Activity.")
            });
    }

    protected OnItemClickListener onClickMenuItem() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                switch (position) {
                    case 0:
                        createSimpleNotification();
                        break;
                    case 1:
                        createNotificationDirectingToActivityWithBackStack();
                        break;
                    case 2:
                        createNotificationDirectingToTransientActivity();
                        break;
                }
            }
        };
    }

    protected void createSimpleNotification() {
        NotificationCompat.Builder builder = new NotificationCompat
            .Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hello")
            .setContentText("The simple notification won't respond to the click.")
            // Setting this flag will make it so the notification is automatically
            // canceled when the user clicks it in the panel.
            .setAutoCancel(true);

        NotificationManagerCompat
            .from(this)
            .notify(0, builder.build());
    }

    protected void createNotificationDirectingToActivityWithBackStack() {
        PendingIntent pendingIntent = TaskStackBuilder
            .create(this)
            // Add the activity parent chain as specified by manifest
            // <meta-data> elements to the task stack builder.
            .addParentStack(NotificationResultSampleActivity1.class)
            // Add a new Intent to the task stack. The most recently added
            // Intent will invoke the Activity at the top of the final task stack.
            .addNextIntent(new Intent(this, NotificationResultSampleActivity1.class))
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Can we add more information in this pendingIntent? e.g. deep-link

        NotificationCompat.Builder builder = new NotificationCompat
            .Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hello")
            .setContentText("The notification will drive you to a result " +
                            "page with back stack to the start page.")
            // Setting this flag will make it so the notification is automatically
            // canceled when the user clicks it in the panel.
            .setAutoCancel(true)
            // Supply a PendingIntent to send when the notification is clicked.
            .setContentIntent(pendingIntent);

        NotificationManagerCompat
            .from(this)
            .notify(1, builder.build());
    }

    protected void createNotificationDirectingToTransientActivity() {
        PendingIntent pendingIntent = PendingIntent
            .getActivity(this, 0,
                         new Intent(this, NotificationResultSampleActivity2.class)
                             // This activity will become the start of a new task
                             // on this history stack and cause any existing task
                             // that would be associated with the activity to be
                             // cleared before the activity is started.
                             .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                       Intent.FLAG_ACTIVITY_CLEAR_TASK),
                         PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat
            .Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hello")
            .setContentText("The notification will drive you to a result " +
                            "page without back stack and it's hidden from " +
                            "the recent list.")
            // Setting this flag will make it so the notification is automatically
            // canceled when the user clicks it in the panel.
            .setAutoCancel(true)
            // Supply a PendingIntent to send when the notification is clicked.
            .setContentIntent(pendingIntent);

        NotificationManagerCompat
            .from(this)
            .notify(2, builder.build());
    }
}
