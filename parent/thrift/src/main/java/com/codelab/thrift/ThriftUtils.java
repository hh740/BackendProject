package com.codelab.thrift;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangke on 16/4/27.
 */
public class ThriftUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(ThriftUtils.class);

    public static interface Constants {
        String IFACE_SUFFIX = "$Iface";
        String CLIENT_SUFFIX = "$Client";
        String PROCESSOR_SUFFIX = "$Processor";

        String DEFAULT_CONFIG_NAME = "Default";

    }

    public static enum NodeType {
        /** Static configuration */
        Configuration,

        /** Dynamic thrift server pool */
        Pool,

        /** localization resource bundle */
        ResourceBundle,
    }

    public static final String ThriftZKRoot = "/services/";

    public static String getServiceDefinitionClass(Class<?> serviceImplClass) {
        Class<?>[] interfaces = serviceImplClass.getInterfaces();
        for (Class<?> i : interfaces) {
            String interfaceName = i.getName();
            if (!interfaceName.endsWith(ThriftUtils.Constants.IFACE_SUFFIX)) {
                LOGGER.debug("The interface {} doesn't end with {}. try next.", interfaceName, ThriftUtils.Constants.IFACE_SUFFIX);
                continue;
            }
            String serviceClassName = StringUtils.removeEnd(interfaceName, ThriftUtils.Constants.IFACE_SUFFIX);
            try {
                Class.forName(serviceClassName);
                // Should have a #Client inner class;
                Class.forName(serviceClassName + ThriftUtils.Constants.CLIENT_SUFFIX);

                return serviceClassName;
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Not a valid service interface {}, try next.", interfaceName, e);
            }
        }

        throw new IllegalArgumentException("Not a valid service implementation class: " + serviceImplClass);
    }

    public static String getThriftZKPath(String serviceClassName) {
        Validate.notNull(serviceClassName, "Service class");

        try {
            Class.forName(serviceClassName);
            Class.forName(serviceClassName + Constants.IFACE_SUFFIX);
            Class.forName(serviceClassName + Constants.CLIENT_SUFFIX);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("%s is not a valid thrift service interface", serviceClassName), e);
        }

        return getThriftZKPathImpl(serviceClassName);
    }
    private static String getThriftZKPathImpl(String serviceClassName) {
        return ThriftZKRoot + serviceClassName;
    }

}
