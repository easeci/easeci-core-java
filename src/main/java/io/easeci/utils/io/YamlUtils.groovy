package io.easeci.utils.io

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

import java.nio.file.Path

/**
 * This static utilities methods are custom wrapper for basic, common and useful
 * operations on Yaml config files. It was created to make more easier works with
 * yaml files. Why groovy language is used here? Because in strong typed Java it is
 * really hard to deal with different types, so dynamically typing groovy is quiet
 * joyful solve of this issue.
 * @author Karol Meksuła
 * 2019-01-16
 * */

@Slf4j
class YamlUtils {
    private static final Yaml YAML = new Yaml()

    /**
     * This method loads file, read and next parse Yaml properties map to object
     * Nested structure is parsed to: Map<String, LinkedHashMap<String, LinkedHashMap.... etc.
     * @param path is a String representation of file's location
     * @return Map<String, LinkedHashMap> or Map<String, String>
     * @exception YamlException If yaml file's syntax is malformed (IO exception is handled in fileLoad(...))
     * */
    static Map<?, ?> ymlLoad(Path path) {
        try {
            String fileAsString = FileUtils.fileLoad(path.toString())
            return YAML.load(fileAsString)
        } catch(Exception e) {
            throw new YamlException("Could not parse yaml file. Errors occurred\n===>Error message:\n" + e.getMessage())
        }
    }

    /**
     * This method finds parameter based on path represents as String.
     * Thanks of that we have not to deal with strong typed dictionaries
     * so fetching data from deeply nested yaml object's structure is simple
     * like in dynamically typing languages.
     * @param path is a String representation of yaml file location
     * @param refs is a String representation of path to concrete pointed value
     *        for example: 'pipeline.meta.title'
     * @return YmlField<?> representation of one specified parameter
     * @exception YamlException If cannot cast value to String or key value not exists
     * */
    static YmlField<?> ymlGet(Path path, String refs) {
        final def KEY_NOT_EXIST = 'Could not find any value for key: ' + refs
        if (FileUtils.isExist(path as String)) {
            def ymlMap = ymlLoad(path)
            for (String key : extractKeys(refs)) {
                if (ymlMap == null) {
                    throw new YamlException(KEY_NOT_EXIST)
                }
                ymlMap = ymlMap.get(key)
            }
            if (!ymlMap.getClass() instanceof String) {
                throw new YamlException('Cannot return value, because it is not String value!')
            }
            return new YmlField<String>(refs, ymlMap)
        }
        throw new YamlException(KEY_NOT_EXIST)
    }

    private static List<String> extractKeys(String refs) {
        if (refs == null) {
            return Collections.emptyList()
        }
        String[] refArr = refs.split('\\.')
        return List.of(refArr)
    }

    static class YamlException extends RuntimeException {
        private String message

        YamlException(String message) {
           this.message = message
        }

        @Override
        String getMessage() {
            return message
        }
    }

    static class YmlField<T> {
        String referenceKey
        T value

        YmlField(String referenceKey, T value) {
            this.referenceKey = referenceKey
            this.value = value
        }

        static YmlField of(String referenceKey, Object value) {
            return new YmlField(referenceKey, value)
        }

        @Override
        String toString() {
            return "Reference: $referenceKey\nValue: $value"
        }
    }
}
