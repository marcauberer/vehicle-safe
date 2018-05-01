package com.mrgames13.jimdo.vehiclesafe.App;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.net.URLEncoder;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    //Konstanten
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    //Variablen als Objekte
    private Resources res;
    private StorageUtils su;
    private ServerMessagingUtils smu;
    private Toolbar toolbar;
    private ProgressDialog pd_Progress;

    //Variablen
    private String result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //Toolbar initialisieren
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            toolbar.setBackgroundColor(res.getColor(R.color.colorPrimary));
            toolbar.setTitleTextColor(res.getColor(R.color.white));
            toolbar.setTitle(res.getString(R.string.settings));
            Drawable upArrow = res.getDrawable(R.drawable.ic_arrow_back);
            upArrow.setColorFilter(res.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(upArrow);
            root.addView(toolbar, 0);
        } else {
            ViewGroup root = findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            toolbar.setBackgroundColor(res.getColor(R.color.colorPrimary));
            toolbar.setTitleTextColor(res.getColor(R.color.white));
            toolbar.setTitle(res.getString(R.string.settings));
            Drawable upArrow = res.getDrawable(R.drawable.ic_arrow_back);
            upArrow.setColorFilter(res.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(upArrow);
            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, res.getDisplayMetrics());
            } else{
                height = toolbar.getHeight();
            }
            content.setPadding(0, height, 0, 0);
            root.addView(content);
            root.addView(toolbar);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupPreferencesScreen();
    }

    private void setupPreferencesScreen() {
        if (!isSimplePreferences(this)) return;
        addPreferencesFromResource(R.xml.pref_main);

        final EditTextPreference tracking_own_position_interval = (EditTextPreference) findPreference("tracking_own_position_interval");
        tracking_own_position_interval.setSummary(su.getString("tracking_own_position_interval", "3") + " " + res.getString(R.string.seconds));
        tracking_own_position_interval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                tracking_own_position_interval.setSummary(newValue + " " + res.getString(R.string.seconds));
                return true;
            }
        });

        final Preference account_change_password = findPreference("account_change_pw");
        account_change_password.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
                final EditText old_pw = dialog_view.findViewById(R.id.old_password_entry);
                final EditText new_pw = dialog_view.findViewById(R.id.new_password_entry);
                final EditText new_pw_confirm = dialog_view.findViewById(R.id.new_password_confirm_entry);
                AlertDialog d = new AlertDialog.Builder(SettingsActivity.this)
                        .setCancelable(true)
                        .setView(dialog_view)
                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(res.getString(R.string.pref_change_pw_t), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(new_pw.getText().toString().equals(new_pw_confirm.getText().toString())) {
                                    changePassword(old_pw.getText().toString(), new_pw.getText().toString());
                                } else {
                                    Toast.makeText(SettingsActivity.this, res.getString(R.string.passwords_does_not_match), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create();
                d.show();
                return false;
            }
        });

        final Preference account_delete = findPreference("account_delete_acc");
        account_delete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View dialog_view = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
                final EditText password = dialog_view.findViewById(R.id.password_entry);

                AlertDialog d = new AlertDialog.Builder(SettingsActivity.this)
                        .setCancelable(true)
                        .setView(dialog_view)
                        .setNegativeButton(res.getString(R.string.cancel), null)
                        .setPositiveButton(res.getString(R.string.next), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog d = new AlertDialog.Builder(SettingsActivity.this)
                                        .setCancelable(true)
                                        .setTitle(res.getString(R.string.pref_delete_acc_t))
                                        .setIcon(R.drawable.delete_red)
                                        .setMessage(res.getString(R.string.delete_acc_q))
                                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(res.getString(R.string.pref_delete_acc_t), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                deleteAccount(password.getText().toString());
                                            }
                                        })
                                        .create();
                                d.show();
                            }
                        })
                        .create();
                d.show();
                return false;
            }
        });

        final Preference about_serverinfo = findPreference("about_serverinfo");
        about_serverinfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(smu.isInternetAvailable()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getServerInfo(true, true);
                        }
                    }).start();
                } else {
                    Toast.makeText(SettingsActivity.this, res.getString(R.string.internet_is_not_available), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        if(smu.isInternetAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        //Info vom Server holen
                        final String serverinfo = getServerInfo(false, false);
                        //Result auseinandernehmen
                        int index1 = result.indexOf("~", 0);
                        int index2 = result.indexOf("~", index1 +1);
                        int index3 = result.indexOf("~", index2 +1);
                        int index4 = result.indexOf("~", index3 +1);
                        int index5 = result.indexOf("~", index4 +1);
                        int index6 = result.indexOf("~", index5 +1);
                        String client_name = result.substring(0, index1);
                        String server_state = result.substring(index1 +1, index2);
                        String min_app_version = result.substring(index2 +1, index3);
                        String newest_app_version = result.substring(index3 +1, index4);
                        String supporturl = result.substring(index4 +1, index5);
                        String owners = result.substring(index5 +1, index6);
                        //ServerState überschreiben
                        if(server_state.equals("1")) server_state = res.getString(R.string.serverstate_1);
                        if(server_state.equals("2")) server_state = res.getString(R.string.serverstate_2);
                        if(server_state.equals("3")) server_state = res.getString(R.string.serverstate_3);
                        if(server_state.equals("4")) server_state = res.getString(R.string.serverstate_4);
                        final String summary = server_state;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Preference serverinfo = findPreference("serverinfo");
                                about_serverinfo.setSummary(summary);
                            }
                        });
                    } catch(Exception e) {}
                }
            }).start();
        } else {
            about_serverinfo.setSummary(res.getString(R.string.internet_is_not_available));
        }

        final Preference about_opensouces = findPreference("about_opensourcelicenses");
        about_opensouces.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SpannableString s = new SpannableString(res.getString(R.string.openSourceLicense));
                Linkify.addLinks(s, Linkify.ALL);

                AlertDialog d = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(about_opensouces.getTitle())
                        .setMessage(Html.fromHtml(s.toString()))
                        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                d.show();
                ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                return false;
            }
        });

        Preference version = findPreference("about_appversion");
        PackageInfo pinfo;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setSummary("Version " + pinfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {}
        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return false;
            }
        });

        Preference developers = findPreference("about_developers");
        developers.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(res.getString(R.string.link_homepage)));
                startActivity(i);
                return false;
            }
        });

        Preference more_apps = findPreference("about_moreapps");
        more_apps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString(R.string.link_playstore_developer_site_market))));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString(R.string.link_playstore_developer_site))));
                }
                return false;
            }
        });
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) loadHeadersFromResource(R.xml.prefs_headers, target);
    }

    private static Preference.OnPreferenceChangeListener value_listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(value_listener);
        value_listener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return MainPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            bindPreferenceSummaryToValue(findPreference("tracking_own_position_interval"));
        }
    }

    private String getServerInfo(final boolean showProgressDialog, final boolean showResultDialog) {
        try {
            if(showProgressDialog) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Dialog für den Fortschritt anzeigen
                        pd_Progress = new ProgressDialog(SettingsActivity.this);
                        pd_Progress.setMessage(res.getString(R.string.pref_serverinfo_downloading_));
                        pd_Progress.setIndeterminate(true);
                        pd_Progress.setTitle(res.getString(R.string.pref_serverinfo_t));
                        pd_Progress.show();
                    }
                });
            }
            //Abfrage an den Server senden
            result = smu.sendRequest(null, "name="+ URLEncoder.encode(su.getString("Username"), "UTF-8")+"&command=getserverinfo");
            //Result auseinandernehmen
            int index1 = result.indexOf("~");
            int index2 = result.indexOf("~", index1 +1);
            int index3 = result.indexOf("~", index2 +1);
            int index4 = result.indexOf("~", index3 +1);
            int index5 = result.indexOf("~", index4 +1);
            int index6 = result.indexOf("~", index5 +1);
            int index7 = result.indexOf("~", index6 +1);
            int index8 = result.indexOf("~", index7 +1);
            final String client_name = result.substring(0, index1);
            final String server_state = result.substring(index1 +1, index2);
            final String min_appversion = result.substring(index2 +1, index3).replace(".", "");
            final String newest_appversion = result.substring(index3 +1, index4).replace(".", "");
            final String min_admin_version = result.substring(index4 +1, index5).replace(".", "");
            final String newest_admin_version = result.substring(index5 +1, index6).replace(".", "");
            final String support_url = result.substring(index6 +1, index7);
            final String owners = result.substring(index7 +1, index8);
            final String user_msg = result.substring(index8 +1);
            //Dialog für das Ergebnis anzeigen
            if(showResultDialog) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(showProgressDialog) pd_Progress.dismiss();
                        //Serverinfo überschreiben
                        String server_state_display = null;
                        if(server_state.equals("1")) server_state_display = res.getString(R.string.server_state) + ": " + res.getString(R.string.serverstate_1_short);
                        if(server_state.equals("2")) server_state_display = res.getString(R.string.server_state) + ": " + res.getString(R.string.serverstate_2_short);
                        if(server_state.equals("3")) server_state_display = res.getString(R.string.server_state) + ": " + res.getString(R.string.serverstate_3_short);
                        if(server_state.equals("4")) server_state_display = res.getString(R.string.server_state) + ": " + res.getString(R.string.serverstate_4_short);
                        //String einzeln zusammensetzen
                        String client_name_display = res.getString(R.string.client_name) + ": " + client_name;
                        String min_app_version_display = res.getString(R.string.min_app_version) + ": " + min_appversion;
                        String newest_app_version_display = res.getString(R.string.newest_app_version) + ": " + newest_appversion;
                        String support_display = res.getString(R.string.support_url) + ": " + support_url;
                        String owners_display = res.getString(R.string.owners) + ": " + owners;
                        //String zusammensetzen und Dialog anzeigen
                        final SpannableString info = new SpannableString(client_name_display + "\n" + server_state_display + "\n" + min_app_version_display + "\n" + newest_app_version_display + "\n" + support_display + "\n" + owners_display);
                        Linkify.addLinks(info, Linkify.WEB_URLS);
                        android.support.v7.app.AlertDialog.Builder d_Result;
                        d_Result = new android.support.v7.app.AlertDialog.Builder(SettingsActivity.this);
                        d_Result.setTitle(res.getString(R.string.pref_serverinfo_t))
                                .setMessage(info)
                                .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        android.support.v7.app.AlertDialog d = d_Result.show();
                        ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void changePassword(final String old_password, final String new_password) {
        if(smu.isInternetAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=changepassword&old_pw=" + URLEncoder.encode(Tools.encodeWithMd5(old_password), "UTF-8") + "&new_pw=" + URLEncoder.encode(Tools.encodeWithMd5(new_password), "UTF-8"));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result.equals("Action Successful")) {
                                    Toast.makeText(SettingsActivity.this, res.getString(R.string.changed_password_successfully), Toast.LENGTH_SHORT).show();
                                    Tools.makeAppRestart(SettingsActivity.this);
                                } else {
                                    Toast.makeText(SettingsActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingsActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Toast.makeText(SettingsActivity.this, res.getString(R.string.internet_is_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAccount(final String password) {
        if(smu.isInternetAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=deleteaccount&password=" + URLEncoder.encode(Tools.encodeWithMd5(password), "UTF-8"));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result.equals("Action Successful")) {
                                    Toast.makeText(SettingsActivity.this, res.getString(R.string.account_deleted_successfully), Toast.LENGTH_SHORT).show();
                                    Tools.makeAppRestart(SettingsActivity.this);
                                } else {
                                    Toast.makeText(SettingsActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingsActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Toast.makeText(SettingsActivity.this, res.getString(R.string.internet_is_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        su.putBoolean("KeepLoggedIn", false);
        //FirebaseTopic deabbonnieren
        FirebaseMessaging.getInstance().unsubscribeFromTopic(su.getString("AccID"));
        //LogIn-Activity starten
        startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
        overridePendingTransition(R.anim.in_login, R.anim.out_logout);
    }
}