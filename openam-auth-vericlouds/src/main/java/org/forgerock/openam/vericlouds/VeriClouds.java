/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2017 ForgeRock AS. All Rights Reserved
 */
package org.forgerock.openam.vericlouds;
import java.security.Principal;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.openam.vericlouds.CredVerify;

/**
 * VeriClouds authentication module example.
 *
 * If you create your own module based on this example, you must modify all
 * occurrences of "VeriClouds" in addition to changing the name of the class.
 *
 * Please refer to OpenAM documentation for further information.
 *
 * Feel free to look at the code for authentication modules delivered with
 * OpenAM, as they implement this same API.
 */
public class VeriClouds extends AMLoginModule {

    // Name for the debug-log
    private final static Debug debug = Debug.getInstance("VeriClouds");

    // Name of the resource bundle
    private final static String amAuthVeriClouds = "amAuthVeriClouds";

    // Orders defined in the callbacks file
    // Not used by this module
    private final static int STATE_BEGIN = 1;
    private final static int STATE_AUTH = 2;
    private final static int STATE_ERROR = 3;

    private Map<String, Set<String>> options;
    private ResourceBundle bundle;
    private Map<String, String> sharedState;
    private CredVerify credVerify;
    private String checkPolicy;
    private String userIdType;

    public VeriClouds() {
        super();
    }

    /**
     * This method stores service attributes and localized properties for later
     * use.
     * @param subject
     * @param sharedState
     * @param options
     */
    @Override
    public void init(Subject subject, Map sharedState, Map options) {

        debug.message("VeriClouds::init");
        try{

            this.options = options;
            this.sharedState = sharedState;
            this.bundle = amCache.getResBundle(amAuthVeriClouds, getLoginLocale());

            String apiUrl = CollectionHelper.getMapAttr(this.options, "vericloudsApiUrl");
            String appKey = CollectionHelper.getMapAttr(this.options, "vericloudsAppKey");
            String appSecret = CollectionHelper.getMapAttr(this.options, "vericloudsAppSecret");
            checkPolicy = CollectionHelper.getMapAttr(this.options, "vericloudsCheckPolicy");
            userIdType = CollectionHelper.getMapAttr(this.options, "vericloudsUserIdType");

            if (apiUrl == null || apiUrl.trim().length() == 0){
                apiUrl = "https://api.vericlouds.com/index.php";
            }
            debug.message("VeriClouds::init apiUrl = " + apiUrl);
            debug.message("VeriClouds::init appKey = " + appKey);

            if (checkPolicy == null || checkPolicy.isEmpty()){
                throw new Exception("VeriClouds::init checkPolicy is not defined");
            }

            if (userIdType == null || userIdType.isEmpty()){
                throw new Exception("VeriClouds::init userIdType is not defined");
            }

            debug.message("VeriClouds::init checkPolicy = " + checkPolicy);
            debug.message("VeriClouds::init userIdType = " + userIdType);

            credVerify = new CredVerify(apiUrl, appKey, appSecret);
        }
        catch(Exception expt){
            debug.error("VeriClouds::init Error:" + expt.getMessage()+ expt.getStackTrace());
        }
    }

    @Override
    public int process(Callback[] callbacks, int state) throws LoginException {

        debug.message("VeriClouds::process state: {}", state);

        String userName = (String) sharedState.get(getUserKey());
        String userPassword = (String) sharedState.get(getPwdKey());

        debug.message("userName = " + userName);
        debug.message("userIdType = " + userIdType);

        switch (state) {

            case STATE_BEGIN:
                return STATE_AUTH;

            case STATE_AUTH:
                try{
                    debug.message("VeriClouds::process calling VeriClouds API");
                    if (credVerify == null){
                        debug.error("VeriClouds API is not proper configured");
                        // if this module is required in the Auth chain but not configured, we don't want to block admin from login
                        return ISAuthConstants.LOGIN_SUCCEED;
                    }

                    Boolean leaked = false;
                    if (checkPolicy.equals("enterprise")){
                        leaked = credVerify.verify(userPassword);
                    }
                    else{
                        leaked = credVerify.verify(userName, userPassword, userIdType);
                    }
                    
                    if (leaked == true){
                        debug.message("VeriClouds::process VeriClouds API: password leaked is true");
                        setErrorText("credverify-leaked-password");
                        return STATE_ERROR;
                    }
                    else{
                        debug.message("VeriClouds::process VeriClouds API: password leaked is false");
                        return ISAuthConstants.LOGIN_SUCCEED;
                    }
                }
                catch(Exception expt){
                    debug.error("VeriClouds::process calling VeriClouds API: "+ expt.getMessage()+expt.getStackTrace());
                    throw new AuthLoginException("VeriClouds::process VeriClouds API error: " + expt.getMessage() + expt.getStackTrace());
                }

            case STATE_ERROR:
                // return STATE_ERROR;
                throw new InvalidPasswordException(bundle.getString("credverify-leaked-password"), userName);
            default:
                throw new AuthLoginException("VeriClouds::process invalid state");
        }
    }

    @Override
    public Principal getPrincipal() {
        return null; // no implementation needed
    }

    private void setErrorText(String err) throws AuthLoginException {
        // Receive correct string from properties and substitute the
        // header in callbacks order 3.
        substituteHeader(STATE_ERROR, bundle.getString(err));
    }
}
