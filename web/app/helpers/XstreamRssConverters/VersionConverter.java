package helpers.XstreamRssConverters;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import models.wedigbio.Version;

public class VersionConverter extends AbstractSingleValueConverter {
    public boolean canConvert(Class type) {
        return type.equals(Version.class);
    }
    public String toString(Object obj) {
        return obj == null ? null : ((Version) obj).getValue();
    }
    public Object fromString(String str) {
        return new Version(str);
    }
}