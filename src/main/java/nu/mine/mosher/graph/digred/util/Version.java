package nu.mine.mosher.graph.digred.util;

import java.net.URL;
import java.util.jar.*;

public class Version {
    protected Version() {
    }

    public String version() {
        final String urlManifest = String.format("jrt:/%s/META-INF/MANIFEST.MF", getClass().getPackage().getName());
        try {
            final Manifest manifest = new Manifest(new URL(urlManifest).toURI().toURL().openStream());
            return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (final Throwable e) {
            return "UNOFFICIAL VERSION";
        }
    }
}
