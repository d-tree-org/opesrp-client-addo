package org.smartregister.addo.contract;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.BaseProfileContract;

import java.util.Map;

public interface FamilyFocusedMemberProfileContract {

    interface View extends BaseProfileContract.View {


        FamilyFocusedMemberProfileContract.Presenter presenter();

        void setProfileImage(String baseEntityId);

        void setProfileName(String fullName);

        void setProfileDetailOne(String detailOne);

        void setProfileDetailTwo(String detailTwo);

        void setProfileDetailThree(String detailThree);

        void toggleFamilyHead(boolean show);

        void togglePrimaryCaregiver(boolean show);

        String getString(int id_with_value);

        void displayProgressBar(boolean b);
    }

    interface Presenter extends BaseProfileContract.Presenter {

        void fetchProfileData();

        void refreshProfileView();

        String getFamilyName();

        void submitVisit(Map<String, String> formForSubmission);

    }

    interface Interactor {

        void onDestroy(boolean isChangingConfiguration);

        void refreshProfileView(String baseEntityId, InteractorCallBack callback);

        void submitVisit(boolean editMode, String memberID, Map<String, String> formForSubmission, InteractorCallBack callBack);

    }

    interface InteractorCallBack {

        void refreshProfileTopSection(CommonPersonObjectClient client);

        void onSubmitted(boolean successful);
    }
}
