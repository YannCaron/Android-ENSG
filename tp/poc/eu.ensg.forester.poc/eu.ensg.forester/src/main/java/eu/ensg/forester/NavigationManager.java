package eu.ensg.forester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by cyann on 23/12/15.
 */
public class NavigationManager {

    private final Context context;

    public NavigationManager(Context context) {
        this.context = context;
    }

    private void navigateToActivity(Class<? extends Activity> clazz) {
        Intent activity = new Intent(context, clazz);
        context.startActivity(activity);
    }

    public void activityMap() {
        navigateToActivity(MainActivity.class);
    }

    public void activityData() {
        navigateToActivity(DataActivity.class);
    }


}
