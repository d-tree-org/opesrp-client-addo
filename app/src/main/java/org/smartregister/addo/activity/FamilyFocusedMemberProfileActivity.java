package org.smartregister.addo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.addo.R;
import org.smartregister.addo.contract.FamilyFocusedMemberProfileContract;
import org.smartregister.addo.dao.AdolescentDao;
import org.smartregister.addo.dao.AncDao;
import org.smartregister.addo.dao.FamilyDao;
import org.smartregister.addo.dao.PNCDao;
import org.smartregister.addo.presenter.FamilyFocusedMemberProfileActivityPresenter;
import org.smartregister.addo.util.ChildDBConstants;
import org.smartregister.addo.util.CoreConstants;
import org.smartregister.addo.util.JsonFormUtils;
import org.smartregister.addo.util.ReferralUtils;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.activity.FamilyWizardFormActivity;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.model.BaseFamilyProfileMemberModel;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.simprint.OnDialogButtonClick;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class FamilyFocusedMemberProfileActivity extends BaseProfileActivity implements FamilyFocusedMemberProfileContract.View {

    private TextView nameView;
    private TextView detailOneView;
    private TextView detailTwoView;
    private TextView detailThreeView;
    private CircleImageView imageView;
    private CustomFontTextView ctvScreeningMed, ctvCommodities, ctvDispense;
    private ProgressBar progressBar;

    private View familyHeadView;
    private View primaryCaregiverView;

    protected ViewPagerAdapter adapter;
    private String familyBaseEntityId;
    private String baseEntityId;
    private String familyHead;
    private String primaryCaregiver;
    private String villageTown;
    private String familyName;
    private String PhoneNumber;
    private CommonPersonObjectClient commonPersonObject;
    protected MemberObject memberObject;

    private FormUtils formUtils;

    public static final String CHILD_DANGER_SIGN_SCREENING_ENCOUNTER = "Child Danger Signs";
    public static final String ANC_DANGER_SIGN_SCREENING_ENCOUNTER = "ANC Danger Signs";
    public static final String PNC_DANGER_SIGN_SCREENING_ENCOUNTER = "PNC Danger Signs";
    public static final String ADOLESCENT_SCREENING_ENCOUNTER = "Adolescent Addo Screening";

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_focused_member_profile);

        Toolbar toolbar = findViewById(R.id.addo_focused_family_member_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable backArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            backArrow.setColorFilter(getResources().getColor(R.color.addo_primary), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(backArrow);
            actionBar.setTitle("");
        }
        this.appBarLayout = findViewById(R.id.toolbar_appbarlayout_addo_focused);

        this.imageRenderHelper = new ImageRenderHelper(this);
        progressBar = findViewById(R.id.progress_bar);

        initializePresenter();

        setupViews();
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        TextView toolBarTitle = findViewById(R.id.toolbar_title_focused);
        if(presenter().getFamilyName() == null) {
            toolBarTitle.setText(getString(R.string.search_results_return));
        } else {
            toolBarTitle.setText(String.format(getString(R.string.return_to_family_name), presenter().getFamilyName()));
        }

        detailOneView = findViewById(R.id.textview_detail_one_focused);
        detailTwoView = findViewById(R.id.textview_detail_two_focused);
        detailThreeView = findViewById(R.id.textview_detail_three_focused);

        familyHeadView = findViewById(R.id.family_head_focused);
        primaryCaregiverView = findViewById(R.id.primary_caregiver_focused);

        nameView = findViewById(R.id.textview_name_focused);

        imageView = findViewById(R.id.imageview_profile);
        imageView.setBorderWidth(2);

        ctvScreeningMed = findViewById(R.id.tv_focused_client_screening);
        ctvScreeningMed.setOnClickListener(this);
        ctvCommodities = findViewById(R.id.tv_focused_client_commodities);
        ctvCommodities.setOnClickListener(this);
        ctvDispense = findViewById(R.id.tv_focused_client_dispense);
        progressBar.setVisibility(View.GONE);
        ctvDispense.setOnClickListener(this);
    }

    @Override
    protected void initializePresenter() {
        commonPersonObject = (CommonPersonObjectClient) getIntent().getSerializableExtra(org.smartregister.addo.util.Constants.INTENT_KEY.CHILD_COMMON_PERSON);
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        villageTown = getIntent().getStringExtra(Constants.INTENT_KEY.VILLAGE_TOWN);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        PhoneNumber = commonPersonObject.getColumnmaps().get(org.smartregister.addo.util.Constants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER);
        memberObject = new MemberObject(commonPersonObject);
        presenter = new FamilyFocusedMemberProfileActivityPresenter(this, new BaseFamilyProfileMemberModel(), null, baseEntityId,
                familyBaseEntityId, familyHead, primaryCaregiver, familyName, villageTown);
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        presenter().fetchProfileData();
    }

    @Override
    public FamilyFocusedMemberProfileContract.Presenter presenter() {
        return (FamilyFocusedMemberProfileContract.Presenter) presenter;
    }

    @Override
    public void setProfileImage(String baseEntityId) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, getMemberProfileImageResourceIDentifier(baseEntityId));
    }

    @Override
    public void setProfileName(String fullName) {
        nameView.setText(fullName);
    }

    @Override
    public void setProfileDetailOne(String detailOne) {
        detailOneView.setText(detailOne);
    }

    @Override
    public void setProfileDetailTwo(String detailTwo) {
        detailTwoView.setText(detailTwo);
    }

    @Override
    public void setProfileDetailThree(String detailThree) {
        detailThreeView.setText(detailThree);
    }

    @Override
    public void toggleFamilyHead(boolean show) {
        if (show) {
            familyHeadView.setVisibility(View.VISIBLE);
        } else {
            familyHeadView.setVisibility(View.GONE);
        }
    }

    @Override
    public void togglePrimaryCaregiver(boolean show) {
        if (show) {
            primaryCaregiverView.setVisibility(View.VISIBLE);
        } else {
            primaryCaregiverView.setVisibility(View.GONE);
        }
    }

    @Override
    public void displayProgressBar(boolean b) {
        if (b) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        presenter().refreshProfileView();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tv_focused_client_screening:
                if (isChildClient()) {
                    startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getChildAddoDangerSigns()), getResources().getString(R.string.danger_sign_title_child));
                } else if (isAncClient()) {
                    startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAncAddoDangerSigns()), getResources().getString(R.string.danger_sign_title_anc));
                } else if (isPncClient()) {
                    startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getPncAddoDangerSigns()), getResources().getString(R.string.danger_sign_title_pnc));
                } else if (isAdolescentClient()) {
                    startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAdolescentAddoScreening()), getResources().getString(R.string.danger_signs_title_adolescent));
                } else {
                    Toast.makeText(this, "You clicked a client that is not in the focused group screening", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_focused_client_commodities:
                startFormActivityForSingleForms(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAddoCommodities()), getResources().getString(R.string.dispense_commodities));
                break;

            case R.id.tv_focused_client_dispense:
                startFormActivityForSingleForms(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getAddoAttendPrescriptionsFromHf()), getString(R.string.attend_prescription_form_title));
                break;

            default:
                super.onClick(view);
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isChildClient() {
        String entityType = Utils.getValue(commonPersonObject.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
        return entityType.equals(org.smartregister.addo.util.Constants.TABLE_NAME.CHILD);
    }

    private boolean isAncClient() {
        return AncDao.isANCMember(baseEntityId);
    }

    private boolean isPncClient() {
        return PNCDao.isPNCMember(baseEntityId);
    }

    private boolean isAdolescentClient() {
        return AdolescentDao.isAdolescentMember(baseEntityId);
    }

    public void startFormActivity(JSONObject jsonForm, String formTitle) {
        Form form = new Form();
        form.setName(formTitle);
        form.setActionBarBackground(R.color.family_actionbar);
        form.setNavigationBackground(R.color.family_navigation);
        form.setHomeAsUpIndicator(R.mipmap.ic_cross_white);
        form.setSaveLabel("FINISH");
        form.setHideSaveLabel(true);
        form.setWizard(true);

        Intent intent = new Intent(this, ReferralWizardFormActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, true);
        startActivityForResult(intent, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    public void startFormActivityForSingleForms(JSONObject jsonForm, String formTitle) {
        Form form = new Form();
        form.setName(formTitle);
        form.setActionBarBackground(R.color.family_actionbar);
        form.setHomeAsUpIndicator(R.mipmap.ic_cross_white);
        form.setHideSaveLabel(true);
        form.setWizard(false);

        Intent intent = new Intent(this, FamilyWizardFormActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, true);
        startActivityForResult(intent, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    private FormUtils getFormUtils() {
        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return formUtils;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);

                // complete any referrals
                FamilyDao.completeTasksForEntity(baseEntityId);

                // Check if it is ANC, PNC or Child Danger sing screening and handle medication based on the screening results
                String encounterType = form.optString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equalsIgnoreCase(CHILD_DANGER_SIGN_SCREENING_ENCOUNTER) || encounterType.equalsIgnoreCase(ANC_DANGER_SIGN_SCREENING_ENCOUNTER) ||
                        encounterType.equalsIgnoreCase(PNC_DANGER_SIGN_SCREENING_ENCOUNTER) || encounterType.equalsIgnoreCase(ADOLESCENT_SCREENING_ENCOUNTER)) {

                    //check if client is being referred
                    JSONArray a = form.getJSONObject("step3").getJSONArray("fields");
                    String buttonAction = "";

                    for (int i = 0; i < a.length(); i++) {
                        org.json.JSONObject jo = a.getJSONObject(i);
                        if (jo.getString("key").compareToIgnoreCase("save_n_refer") == 0) {
                            if (jo.optString("value") != null && jo.optString("value").compareToIgnoreCase("true") == 0) {
                                buttonAction = jo.getJSONObject("action").getString("behaviour");
                            }
                        }
                    }
                    if (!buttonAction.isEmpty()) {
                        // Check if the client has referral already or not
                        if (ReferralUtils.hasReferralTask(CoreConstants.REFERRAL_PLAN_ID, LocationHelper.getInstance().getOpenMrsLocationId(villageTown), baseEntityId, CoreConstants.JsonAssets.REFERRAL_CODE)) {
                            closeOpenNewReferral(this, new OnDialogButtonClick() {
                                @Override
                                public void onOkButtonClick() {
                                    // Close referral
                                    FamilyDao.archiveHFTasksForEntity(baseEntityId);
                                    // Open a new referral
                                    ReferralUtils.createReferralTask(baseEntityId, form.optString(org.smartregister.chw.anc.util.Constants.ENCOUNTER_TYPE), jsonString, villageTown);

                                    // Dispense
                                    checkDSPresentProposedMedsAndDispense(form);

                                }

                                @Override
                                public void onCancelButtonClick() {
                                    checkDSPresentProposedMedsAndDispense(form);
                                }
                            });
                        } else {
                            //refer
                            ReferralUtils.createReferralTask( baseEntityId, form.optString(org.smartregister.chw.anc.util.Constants.ENCOUNTER_TYPE), jsonString, villageTown);
                            checkDSPresentProposedMedsAndDispense(form);

                        }

                    } else {
                        checkDSPresentProposedMedsAndDispense(form);
                    }
                }
                //end of check referral

                Map<String, String> formForSubmission = new HashMap<>();
                formForSubmission.put(form.optString(org.smartregister.chw.anc.util.Constants.ENCOUNTER_TYPE), jsonString);
                submitForm(formForSubmission);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkDSPresentProposedMedsAndDispense(JSONObject form) {

        try {
            // Check if the focused group client is present or not; if not skip to dispensing
            if (isClientPresent(form)) {
                String dangerSigns;
                String suggestedMeds;

                JSONObject step2 = form.getJSONObject(JsonFormUtils.STEP2);
                JSONArray step2Fields = step2.getJSONArray(JsonFormUtils.FIELDS);
                JSONArray dangerSignsSelected;

                switch (form.optString(JsonFormUtils.ENCOUNTER_TYPE)) {
                    case CHILD_DANGER_SIGN_SCREENING_ENCOUNTER:
                        dangerSignsSelected = JsonFormUtils.getFieldJSONObject(step2Fields, "danger_signs_present_child").getJSONArray(JsonFormUtils.VALUE);
                        break;
                    case ANC_DANGER_SIGN_SCREENING_ENCOUNTER:
                        dangerSignsSelected = JsonFormUtils.getFieldJSONObject(step2Fields, "danger_signs_present").getJSONArray(JsonFormUtils.VALUE);
                        break;
                    case PNC_DANGER_SIGN_SCREENING_ENCOUNTER:
                        dangerSignsSelected = JsonFormUtils.getFieldJSONObject(step2Fields, "danger_signs_present_mama").getJSONArray(JsonFormUtils.VALUE);
                        break;
                    case ADOLESCENT_SCREENING_ENCOUNTER:
                        dangerSignsSelected = JsonFormUtils.getFieldJSONObject(step2Fields, "adolescent_condition_present").getJSONArray(JsonFormUtils.VALUE);
                        break;
                    default:
                        return;
                }

                // When there is danger signs and the none field is not selected open the dispense medication
                if (dangerSignsSelected.length() > 0 && !dangerSignsSelected.getString(0).equalsIgnoreCase("chk_none")) {
                    dangerSigns = JsonFormUtils.getFieldJSONObject(step2Fields, "danger_signs_captured").getString(JsonFormUtils.VALUE);
                    if (form.optString(org.smartregister.chw.anc.util.Constants.ENCOUNTER_TYPE).equalsIgnoreCase(CHILD_DANGER_SIGN_SCREENING_ENCOUNTER)) {
                        suggestedMeds = JsonFormUtils.getFieldJSONObject(step2Fields, "addo_medication_to_give").getString(JsonFormUtils.VALUE);
                    } else {
                        suggestedMeds = getResources().getString(R.string.default_dispense_message);
                    }
                    // Check if client has referral or to determine if they should be linked to another ADDO or not
                    JSONArray step3Fields = form.getJSONObject(JsonFormUtils.STEP3).getJSONArray(JsonFormUtils.FIELDS);
                    JSONObject referralButtonObject = JsonFormUtils.getFieldJSONObject(step3Fields, "save_n_refer");
                    String referralStatus;
                    if (referralButtonObject.optString(JsonFormUtils.VALUE) != null && referralButtonObject.optString(JsonFormUtils.VALUE).compareToIgnoreCase("true") == 0) {
                        referralStatus = "referred";
                    } else {
                        referralStatus = null;
                    }

                    dispenseMedication(dangerSigns, suggestedMeds, referralStatus);
                } else if (dangerSignsSelected.getString(0).equalsIgnoreCase("chk_none")) {
                    dispenseMedication(null, getResources().getString(R.string.default_dispense_message), null);
                }
            } else {
            dispenseMedication(null, null, null);
        }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void submitForm(Map<String, String> formForSubmission) {
        presenter().submitVisit(formForSubmission);
    }

    private Boolean isClientPresent(@NotNull JSONObject form) {
        try {
            JSONObject step1 = form.getJSONObject(JsonFormUtils.STEP1);
            JSONArray step1Fields = step1.getJSONArray(JsonFormUtils.FIELDS);
            if (form.optString(JsonFormUtils.ENCOUNTER_TYPE).equalsIgnoreCase(CHILD_DANGER_SIGN_SCREENING_ENCOUNTER)) {
                return JsonFormUtils.getFieldJSONObject(step1Fields, "child_present").getJSONArray(JsonFormUtils.VALUE).get(0).toString().equals("chk_child_present_yes");
            } else if (form.optString(JsonFormUtils.ENCOUNTER_TYPE).equalsIgnoreCase(ANC_DANGER_SIGN_SCREENING_ENCOUNTER)) {
                return JsonFormUtils.getFieldJSONObject(step1Fields, "pregnant_woman_present").getJSONArray(JsonFormUtils.VALUE).get(0).toString().equals("chk_pregnant_woman_present_yes");
            } else if (form.optString(JsonFormUtils.ENCOUNTER_TYPE).equalsIgnoreCase(PNC_DANGER_SIGN_SCREENING_ENCOUNTER)) {
                return JsonFormUtils.getFieldJSONObject(step1Fields, "mother_present").getJSONArray(JsonFormUtils.VALUE).get(0).toString().equals("chk_mother_present_yes");
            } else {
                return JsonFormUtils.getFieldJSONObject(step1Fields, "adolescent_present").getJSONArray(JsonFormUtils.VALUE).get(0).equals("adolescent_present_yes");
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return false;
    }

    private void dispenseMedication(String dangerSigns, String suggestedMeds, String referralStatus) {
        try {
            JSONObject form = new JSONObject();
            if (isAdolescentClient()) {
                form = getFormUtils().getFormJson(CoreConstants.JSON_FORM.getDangerSignsMedicationAdolescent());
            } else {
                form = getFormUtils().getFormJson(CoreConstants.JSON_FORM.getDangerSignsMedication());
            }
            JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
            JSONArray fields = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormField(fields, "danger_signs_captured", dangerSigns);
            updateFormField(fields, "addo_medication_to_give", suggestedMeds);
            updateFormField(fields, "referral_status", referralStatus);
            startFormActivity(form, getResources().getString(R.string.dispense_medication_title));
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private static void updateFormField(JSONArray formFieldArrays, String formFieldKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFieldKey);
            if (formObject != null) {
                try {
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeOpenNewReferral(Context context, final OnDialogButtonClick onDialogButtonClick) {
        final AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setMessage(getString(R.string.do_you_want_to_give_another_referral));
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(context, context.getResources().getString(R.string.referral_submitted), Toast.LENGTH_LONG).show();
                onDialogButtonClick.onOkButtonClick();
                alert.dismiss();
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogButtonClick.onCancelButtonClick();
                alert.dismiss();
            }
        });
        alert.show();
    }

    private int getMemberProfileImageResourceIDentifier(String baseEntityId) {

        int imageResourceId;

        if (AncDao.isANCMember(baseEntityId)) {
            imageResourceId = org.smartregister.addo.util.Utils.getAnCWomanImageResourceIdentifier();
        } else if (PNCDao.isPNCMember(baseEntityId)) {
            imageResourceId = org.smartregister.addo.util.Utils.getPnCWomanImageResourceIdentifier();
        } else if (AdolescentDao.isAdolescentMember(baseEntityId)) {
            imageResourceId = org.smartregister.addo.util.Utils.getMemberImageResourceIdentifier();
        } else {
            imageResourceId =org.smartregister.addo.util.Utils.getChildProfileImageResourceIDentifier();
        }


        return imageResourceId;

    }
}
