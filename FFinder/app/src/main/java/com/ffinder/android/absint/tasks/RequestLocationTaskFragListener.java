package com.ffinder.android.absint.tasks;

import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.SearchStatus;
import com.ffinder.android.models.LocationModel;

/**
 * Created by SiongLeng on 3/9/2016.
 */
public abstract class RequestLocationTaskFragListener {

    public abstract void onUpdateStatus(String userId, SearchStatus newStatus);

    public abstract void onUpdateResult(String userId, LocationModel locationModel,
                                            SearchStatus finalSearchStatus, SearchResult result);

}
