package de.plushnikov.intellij.plugin;

import java.util.List;

public interface LombokClassNames {
    String ACCESSORS = "lombok.experimental.Accessors";
    String ACCESS_LEVEL = "lombok.AccessLevel";
    String ALL_ARGS_CONSTRUCTOR = "lombok.AllArgsConstructor";
    String BUILDER = "lombok.Builder";
    String BUILDER_DEFAULT = "lombok.Builder.Default";
    String BUILDER_OBTAIN_VIA = "lombok.Builder.ObtainVia";
    String CLEANUP = "lombok.Cleanup";
    String COMMONS_LOG = "lombok.extern.apachecommons.CommonsLog";
    String CUSTOM_LOG = "lombok.CustomLog";
    String DATA = "lombok.Data";
    String DELEGATE = "lombok.Delegate";
    String EQUALS_AND_HASHCODE = "lombok.EqualsAndHashCode";
    String EQUALS_AND_HASHCODE_EXCLUDE = "lombok.EqualsAndHashCode.Exclude";
    String EQUALS_AND_HASHCODE_INCLUDE = "lombok.EqualsAndHashCode.Include";
    String EXPERIMENTAL_DELEGATE = "lombok.experimental.Delegate";
    String EXPERIMENTAL_VAR = "lombok.experimental.var";
    String EXTENSION_METHOD = "lombok.experimental.ExtensionMethod";
    String FIELD_DEFAULTS = "lombok.experimental.FieldDefaults";
    String FIELD_NAME_CONSTANTS = "lombok.experimental.FieldNameConstants";
    String FIELD_NAME_CONSTANTS_EXCLUDE = "lombok.experimental.FieldNameConstants.Exclude";
    String FIELD_NAME_CONSTANTS_INCLUDE = "lombok.experimental.FieldNameConstants.Include";
    String FLOGGER = "lombok.extern.flogger.Flogger";
    String GETTER = "lombok.Getter";
    String JACKSONIZED = "lombok.extern.jackson.Jacksonized";
    String JAVA_LOG = "lombok.extern.java.Log";
    String JBOSS_LOG = "lombok.extern.jbosslog.JBossLog";
    String LOG_4_J = "lombok.extern.log4j.Log4j";
    String LOG_4_J_2 = "lombok.extern.log4j.Log4j2";
    String NON_FINAL = "lombok.experimental.NonFinal";
    String NON_NULL = "lombok.NonNull";
    String NO_ARGS_CONSTRUCTOR = "lombok.NoArgsConstructor";
    String PACKAGE_PRIVATE = "lombok.experimental.PackagePrivate";
    String REQUIRED_ARGS_CONSTRUCTOR = "lombok.RequiredArgsConstructor";
    String SETTER = "lombok.Setter";
    String SINGULAR = "lombok.Singular";
    String SLF_4_J = "lombok.extern.slf4j.Slf4j";
    String SNEAKY_THROWS = "lombok.SneakyThrows";
    String SUPER_BUILDER = "lombok.experimental.SuperBuilder";
    String STANDARD_EXCEPTION = "lombok.experimental.StandardException";
    String SYNCHRONIZED = "lombok.Synchronized";
    String TOLERATE = "lombok.experimental.Tolerate";
    String TO_STRING = "lombok.ToString";
    String TO_STRING_EXCLUDE = "lombok.ToString.Exclude";
    String TO_STRING_INCLUDE = "lombok.ToString.Include";
    String UTILITY_CLASS = "lombok.experimental.UtilityClass";
    String VAL = "lombok.val";
    String VALUE = "lombok.Value";
    String VAR = "lombok.var";
    String WITH = "lombok.With";
    String WITHER = "lombok.experimental.Wither";
    String XSLF_4_J = "lombok.extern.slf4j.XSlf4j";

    List<String> MAIN_LOMBOK_CLASSES = List.of(
        ALL_ARGS_CONSTRUCTOR, REQUIRED_ARGS_CONSTRUCTOR, NO_ARGS_CONSTRUCTOR,
        DATA, GETTER, SETTER, EQUALS_AND_HASHCODE, TO_STRING,
        LOG_4_J, LOG_4_J_2, SLF_4_J, JAVA_LOG, JBOSS_LOG, FLOGGER, COMMONS_LOG,
        CUSTOM_LOG,
        BUILDER, SUPER_BUILDER, FIELD_DEFAULTS, VALUE,
        UTILITY_CLASS, WITH, WITHER, EXPERIMENTAL_DELEGATE,
        SNEAKY_THROWS, CLEANUP, SYNCHRONIZED, EXTENSION_METHOD
    );
}
