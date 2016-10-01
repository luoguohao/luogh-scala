package com.luogh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.luogh.PlatformName.IBM_JAVA;
import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/9/29
 */
public class UserPrincipal {
    private static final Log LOG = LogFactory.getLog(UserPrincipal.class);
    private static final boolean windows =
            System.getProperty("os.name").startsWith("Windows");
    private static final boolean is64Bit =
            System.getProperty("os.arch").contains("64");
    private static final boolean aix = System.getProperty("os.name").equals("AIX");

    private static void initSubject(Subject subject) {
        // Temporarily switch the thread's ContextClassLoader to match this
        // class's classloader, so that we can properly load HadoopLoginModule
        // from the JAAS libraries.
        try {
            LoginContext context =  new LoginContext("simple", subject, null, new SimpleConfiguration());
            context.login();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * A JAAS configuration that defines the login modules that we want
     * to use for login.
     */
    private static class SimpleConfiguration extends javax.security.auth.login.Configuration {
        private static final String SIMPLE_CONFIG_NAME = "simple";
        private static final String OS_LOGIN_MODULE_NAME;
        private static final Map<String, String> BASIC_JAAS_OPTIONS = new HashMap<>();
        static {
            BASIC_JAAS_OPTIONS.put("debug", "true");
            OS_LOGIN_MODULE_NAME = getOSLoginModuleName();
        }

        private static final AppConfigurationEntry OS_SPECIFIC_LOGIN =
                                new AppConfigurationEntry(OS_LOGIN_MODULE_NAME,
                                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                    BASIC_JAAS_OPTIONS);
        private static final AppConfigurationEntry[] SIMPLE_CONF = new AppConfigurationEntry[]{OS_SPECIFIC_LOGIN};

        /* Return the OS login module class name */
        private static String getOSLoginModuleName() {
            if (IBM_JAVA) {
                if (windows) {
                    return is64Bit ? "com.ibm.security.auth.module.Win64LoginModule"
                            : "com.ibm.security.auth.module.NTLoginModule";
                } else if (aix) {
                    return is64Bit ? "com.ibm.security.auth.module.AIX64LoginModule"
                            : "com.ibm.security.auth.module.AIXLoginModule";
                } else {
                    return "com.ibm.security.auth.module.LinuxLoginModule";
                }
            } else {
                return windows ? "com.sun.security.auth.module.NTLoginModule"
                        : "com.sun.security.auth.module.UnixLoginModule";
            }
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
            if (SIMPLE_CONFIG_NAME.equals(appName))
                return SIMPLE_CONF;
            return null;
        }
    }

    private  static Class<? extends Principal> selectUserPrincicalClass() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try {
            String principalClass;
            if (IBM_JAVA) {
                if (is64Bit) {
                    principalClass = "com.ibm.security.auth.UsernamePrincipal";
                } else {
                    if (windows) {
                        principalClass = "com.ibm.security.auth.NTUserPrincipal";
                    } else if (aix) {
                        principalClass = "com.ibm.security.auth.AIXPrincipal";
                    } else {
                        principalClass = "com.ibm.security.auth.LinuxPrincipal";
                    }
                }
            } else {
                principalClass = windows ? "com.sun.security.auth.NTUserPrincipal"
                        : "com.sun.security.auth.UnixPrincipal";
            }
            return (Class<? extends Principal>) cl.loadClass(principalClass);
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to find JAAS classes:" + e.getMessage());
        }
        return null;
    }

    public static <T extends Principal> T getCanonicalUser(Class<T> principal, Subject subject) {
        for(T user : subject.getPrincipals(principal)) {
            return user;
        }
        return null;
    }

    public static String getLoginUser() {
        Class<? extends Principal> userPrincipalClass = selectUserPrincicalClass();
        AccessControlContext aContr = AccessController.getContext();
        Subject subject = Subject.getSubject(aContr);
        if(subject == null) {
            subject = new Subject();
            initSubject(subject);
        }
        return getCanonicalUser(userPrincipalClass,subject).getName();
    }


    public static void main(String[] args) throws Exception {
        out.println("login user:"+ getLoginUser());
    }
}

class PlatformName {
    /**
     * The complete platform 'name' to identify the platform as
     * per the java-vm.
     */
    public static final String PLATFORM_NAME =
            (System.getProperty("os.name").startsWith("Windows")
                    ? System.getenv("os") : System.getProperty("os.name"))
                    + "-" + System.getProperty("os.arch")
                    + "-" + System.getProperty("sun.arch.data.model");

    /**
     * The java vendor name used in this platform.
     */
    public static final String JAVA_VENDOR_NAME = System.getProperty("java.vendor");

    /**
     * A public static variable to indicate the current java vendor is
     * IBM java or not.
     */
    public static final boolean IBM_JAVA = JAVA_VENDOR_NAME.contains("IBM");
}
