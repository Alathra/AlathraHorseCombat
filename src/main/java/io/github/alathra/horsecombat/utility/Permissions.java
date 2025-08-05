package io.github.alathra.horsecombat.utility;

public class Permissions {
    private static final String HORSECOMBAT_ADMIN = "alathrahorsecombat.admin";
    private static final String HORSECOMBAT_USER = "alathrahorsecombat.user";

    public static String getAdminPermissionNode() {
        return HORSECOMBAT_ADMIN;
    }

    public static String getUserPermissionNode() {
        return HORSECOMBAT_USER;
    }
}
