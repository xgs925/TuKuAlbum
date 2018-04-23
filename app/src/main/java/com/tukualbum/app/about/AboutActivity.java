package com.tukualbum.app.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.tukualbum.app.util.AlertDialogsHelper;
import com.tukualbum.app.util.ChromeCustomTabs;
import com.tukualbum.app.util.preferences.Prefs;

import com.tukualbum.app.activities.DonateActivity;
import com.tukualbum.app.util.ApplicationUtils;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tukualbum.app.util.ServerConstants.GITHUB_CALVIN;
import static com.tukualbum.app.util.ServerConstants.GITHUB_DONALD;
import static com.tukualbum.app.util.ServerConstants.GITHUB_GILBERT;
import static com.tukualbum.app.util.ServerConstants.GITHUB_LEAFPIC;
import static com.tukualbum.app.util.ServerConstants.GOOGLE_ABOUT_CALVIN;
import static com.tukualbum.app.util.ServerConstants.LEAFPIC_CROWDIN;
import static com.tukualbum.app.util.ServerConstants.LEAFPIC_ISSUES;
import static com.tukualbum.app.util.ServerConstants.LEAFPIC_LICENSE;
import static com.tukualbum.app.util.ServerConstants.MAIL_CALVIN;
import static com.tukualbum.app.util.ServerConstants.MAIL_DONALD;
import static com.tukualbum.app.util.ServerConstants.MAIL_GILBERT;
import static com.tukualbum.app.util.ServerConstants.TWITTER_ABOUT_DONALD;
import static com.tukualbum.app.util.ServerConstants.TWITTER_ABOUT_GILBERT;

/**
 * The Activity to show About application
 * <p>
 * Includes the following data:
 * - Developers
 * - Translators
 * - Relevant app links
 */
public class AboutActivity extends ThemedActivity implements ContactListener {

    @BindView(com.tukualbum.app.R.id.toolbar) Toolbar toolbar;
    @BindView(com.tukualbum.app.R.id.about_version_item_sub) ThemedTextView appVersion;
    @BindView(com.tukualbum.app.R.id.aboutAct_scrollView) ScrollView aboutScrollView;
    @BindView(com.tukualbum.app.R.id.about_developer_donald) AboutCreator aboutDonald;
    @BindView(com.tukualbum.app.R.id.about_developer_gilbert) AboutCreator aboutGilbert;
    @BindView(com.tukualbum.app.R.id.about_patryk_goworowski_item_sub) ThemedTextView specialThanksPatryk;
    @BindView(com.tukualbum.app.R.id.about_link_changelog) AboutLink linkChangelog;
    @BindView(com.tukualbum.app.R.id.list_contributors) RecyclerView rvContributors;

    private ChromeCustomTabs chromeTabs;
    private int emojiEasterEggCount = 0;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.tukualbum.app.R.layout.activity_about);
        ButterKnife.bind(this);
        chromeTabs = new ChromeCustomTabs(AboutActivity.this);

        initUi();
    }

    @Override
    public void onDestroy() {
        chromeTabs.destroy();
        super.onDestroy();
    }

    @OnClick(com.tukualbum.app.R.id.about_link_report_bug)
    public void onReportBug() {
        chromeTabs.launchUrl(LEAFPIC_ISSUES);
    }

    @OnClick(com.tukualbum.app.R.id.about_link_translate)
    public void onTranslate() {
        chromeTabs.launchUrl(LEAFPIC_CROWDIN);
    }

    @OnClick(com.tukualbum.app.R.id.about_link_rate)
    public void onRate() {
        // TODO: Link to app store
    }

    @OnClick(com.tukualbum.app.R.id.about_link_github)
    public void onGitHub() {
        chromeTabs.launchUrl(GITHUB_LEAFPIC);
    }

    @OnClick(com.tukualbum.app.R.id.about_link_donate)
    public void onDonate() {
        DonateActivity.startActivity(this);
    }

    @OnClick(com.tukualbum.app.R.id.about_link_license)
    public void onLicense() {
        chromeTabs.launchUrl(LEAFPIC_LICENSE);
    }

    @OnClick(com.tukualbum.app.R.id.about_link_changelog)
    public void onChangelog() {
        AlertDialog alertDialog = AlertDialogsHelper.showChangelogDialog(this);
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(com.tukualbum.app.R.string.ok_action).toUpperCase(),
                (dialogInterface, i) -> {
                });
        alertDialog.show();
    }

    //TODO: EMOJI EASTER EGG - NOTHING TO SHOW
    private void emojiEasterEgg() {
        emojiEasterEggCount++;
        if (emojiEasterEggCount > 3) {
            boolean showEasterEgg = Prefs.showEasterEgg();
            Toast.makeText(this,
                    (!showEasterEgg ? this.getString(com.tukualbum.app.R.string.easter_egg_enable) : this.getString(com.tukualbum.app.R.string.easter_egg_disable))
                            + " " + this.getString(com.tukualbum.app.R.string.emoji_easter_egg), Toast.LENGTH_SHORT).show();
            Prefs.setShowEasterEgg(!showEasterEgg);
            emojiEasterEggCount = 0;
        } else
            Toast.makeText(getBaseContext(), String.valueOf(emojiEasterEggCount), Toast.LENGTH_SHORT).show();
    }

    private void mail(String mail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + mail));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(com.tukualbum.app.R.string.send_mail_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        appVersion.setText(ApplicationUtils.getAppVersion());
        linkChangelog.setDescription(ApplicationUtils.getAppVersion());


        ArrayList<Contributor> contributors = new ArrayList<>(1);

        /* Calvin */
        Contributor calvin = new Contributor(
                getString(com.tukualbum.app.R.string.developer_calvin_name),
                getString(com.tukualbum.app.R.string.about_developer_calvin_description),
                com.tukualbum.app.R.drawable.calvin_profile);
        calvin.setEmail(MAIL_CALVIN);
        calvin.addSocial(getString(com.tukualbum.app.R.string.google_plus_link), GOOGLE_ABOUT_CALVIN);
        calvin.addSocial(getString(com.tukualbum.app.R.string.github), GITHUB_CALVIN);
        contributors.add(calvin);


        ContributorsAdapter contributorsAdapter = new ContributorsAdapter(this, contributors, this);
        rvContributors.setHasFixedSize(true);
        rvContributors.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvContributors.setAdapter(contributorsAdapter);

        /* Donald */
        ArrayList<Contact> donaldContacts = new ArrayList<>();
        donaldContacts.add(new Contact(TWITTER_ABOUT_DONALD, getString(com.tukualbum.app.R.string.twitter_link)));
        donaldContacts.add(new Contact(GITHUB_DONALD, getString(com.tukualbum.app.R.string.github_link)));
        aboutDonald.setupListeners(this, MAIL_DONALD, donaldContacts);

        /* Jibo */
        ArrayList<Contact> jiboContacts = new ArrayList<>();
        jiboContacts.add(new Contact(TWITTER_ABOUT_GILBERT, getString(com.tukualbum.app.R.string.twitter_link)));
        jiboContacts.add(new Contact(GITHUB_GILBERT, getString(com.tukualbum.app.R.string.github_link)));
        aboutGilbert.setupListeners(this, MAIL_GILBERT, jiboContacts);

        aboutGilbert.setOnClickListener(v -> emojiEasterEgg());
        specialThanksPatryk.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setScrollViewColor(aboutScrollView);
        setStatusBarColor();
        setNavBarColor();

        specialThanksPatryk.setLinkTextColor(getAccentColor());
    }

    @Override
    public void onContactClicked(Contact contact) {
        chromeTabs.launchUrl(contact.getValue());
    }

    @Override
    public void onMailClicked(String mail) {
        mail(mail);
    }
}
