package pro.belbix.tim.properties;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        if (resource == null) {
            return super.createPropertySource(name, resource);
        }
        CompositePropertySource propertySource = new CompositePropertySource(name);
        new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource())
                .forEach(propertySource::addPropertySource);
        return propertySource;
    }
}
