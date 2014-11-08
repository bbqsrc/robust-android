package so.brendan.robust.views;

import so.brendan.robust.models.RobustUser;

/**
 * A view for showing user profiles.
 */
public interface UserProfileView {
    public void setCurrentUser(RobustUser user);
}
