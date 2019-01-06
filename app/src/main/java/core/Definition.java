package core;

import android.Manifest;

import java.util.Arrays;
import java.util.List;

public final class Definition {
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final int RESULT_LOGIN_SUCCESSFUL = 3004;
    public static final int REQUEST_LOGIN_WITH_GOOGLE = 3005;
    public static final int REQUEST_LOGIN = 3006;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3007;
    public static final int RESULT_CODE_RETURN_ADDRESS = 3008;
    public static final int REQUEST_PERMISSION_CODE = 3009;

    public static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
    };
}
