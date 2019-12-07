package org.smartregister.addo.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.Set;

import static org.smartregister.family.util.Utils.getName;

public class SimPrintIdentificationResultProvider implements RecyclerViewProvider<SimPrintIdentificationResultProvider.RegisterViewHolder> {


    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;

    private Context context;
    private CommonRepository commonRepository;
    private ImageRenderHelper imageRenderHelper;

    private String familyHead;
    private String primaryCaregiver;

    public SimPrintIdentificationResultProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener, String familyHead, String primaryCaregiver) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;

        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;

        this.context = context;
        this.commonRepository = commonRepository;
        this.imageRenderHelper = new ImageRenderHelper(context);

        this.familyHead = familyHead;
        this.primaryCaregiver = primaryCaregiver;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateIdentifierColumn(pc, viewHolder);

            return;
        }
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final SimPrintIdentificationResultProvider.RegisterViewHolder viewHolder) {

        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);

        String patientName = getName(firstName, middleName, lastName);

        String entityType = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.ENTITY_TYPE, false);

        String dob = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String dobString = Utils.getDuration(dob);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

        String dod = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOD, false);
        if (StringUtils.isNotBlank(dod)) {

            dobString = Utils.getDuration(dod, dob);
            dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

            patientName = patientName + ", " + Utils.getTranslatedDate(dobString, context) + " " + context.getString(org.smartregister.family.R.string.deceased_brackets);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(Color.GRAY);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.profile.setImageResource(Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.GONE);
        } else {
            patientName = patientName + ", " + Utils.getTranslatedDate(dobString, context);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(Color.BLACK);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.NORMAL);
            imageRenderHelper.refreshProfileImage(pc.getCaseId(), viewHolder.profile, Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.VISIBLE);
        }

        fillValue(viewHolder.patientNameAge, patientName);

        String gender_key = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        String gender = "";
        if (gender_key.equalsIgnoreCase("Male")) {
            gender = context.getString(org.smartregister.family.R.string.male);
        } else if (gender_key.equalsIgnoreCase("Female")) {
            gender = context.getString(org.smartregister.family.R.string.female);
        }
        fillValue(viewHolder.gender, gender);

        viewHolder.nextArrowColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.nextArrow.performClick();
            }
        });

        viewHolder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });

        viewHolder.registerColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });
        if (StringUtils.isBlank(dod)) {
            View patient = viewHolder.patientColumn;
            attachPatientOnclickListener(patient, client);

            View nextArrow = viewHolder.nextArrow;
            attachNextArrowOnclickListener(nextArrow, client);
        }

    }



    private void populateIdentifierColumn(CommonPersonObjectClient pc, SimPrintIdentificationResultProvider.RegisterViewHolder viewHolder) {
        String uniqueId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
        //fillValue(viewHolder.ancId, String.format(context.getString(R.string.unique_id_text), uniqueId));

        String baseEntityId = pc.getCaseId();
        if (StringUtils.isNotBlank(baseEntityId)) {
            if (baseEntityId.equals(familyHead)) {
                viewHolder.familyHead.setVisibility(View.VISIBLE);
            } else {
                viewHolder.familyHead.setVisibility(View.GONE);
            }


            if (baseEntityId.equals(primaryCaregiver)) {
                viewHolder.primaryCaregiver.setVisibility(View.VISIBLE);
            } else {
                viewHolder.primaryCaregiver.setVisibility(View.GONE);
            }
        }
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NORMAL);
    }

    private void attachNextArrowOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NEXT_ARROW);
    }



    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int i, int i1, boolean b, boolean b1) {

    }

    @Override
    public SmartRegisterClients updateClients(FilterOption filterOption, ServiceModeOption serviceModeOption, FilterOption filterOption1, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String s, String s1, String s2) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return null;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }

    public class RegisterViewHolder extends RecyclerView.ViewHolder {
        public ImageView status;
        public ImageView profile;
        public ImageView fingerprintStatus;
        public CustomFontTextView patientNameAge;
        public TextView gender;
        public TextView familyHead;
        public TextView primaryCaregiver;
        public ImageView nextArrow;

        public View statusLayout;
        public View patientColumn;
        public View nextArrowColumn;
        public View registerColumns;

        public RegisterViewHolder(View itemView) {
            super(itemView);

            status = itemView.findViewById(org.smartregister.family.R.id.status);
            profile = itemView.findViewById(org.smartregister.family.R.id.profile);
            fingerprintStatus = itemView.findViewById(org.smartregister.family.R.id.finger_print_status);

            patientNameAge = itemView.findViewById(org.smartregister.family.R.id.patient_name_age);
            gender = itemView.findViewById(org.smartregister.family.R.id.gender);
            familyHead = itemView.findViewById(org.smartregister.family.R.id.family_head);
            primaryCaregiver = itemView.findViewById(org.smartregister.family.R.id.primary_caregiver);
            nextArrow = itemView.findViewById(org.smartregister.family.R.id.next_arrow);

            statusLayout = itemView.findViewById(org.smartregister.family.R.id.status_layout);
            patientColumn = itemView.findViewById(org.smartregister.family.R.id.patient_column);
            nextArrowColumn = itemView.findViewById(org.smartregister.family.R.id.next_arrow_column);
            registerColumns = itemView.findViewById(org.smartregister.family.R.id.register_columns);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView pageInfoView;
        public Button nextPageView;
        public Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
        }
    }
}