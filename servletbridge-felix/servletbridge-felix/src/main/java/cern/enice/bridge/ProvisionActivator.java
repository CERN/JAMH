package cern.enice.bridge;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class ProvisionActivator
    implements BundleActivator
{
    private final ServletContext servletContext;

    public ProvisionActivator(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public void start(BundleContext context)
        throws Exception
    {
        servletContext.setAttribute(BundleContext.class.getName(), context);

        ArrayList<Bundle> installed = new ArrayList<Bundle>();
        for (URL url : findBundles()) {
            this.servletContext.log("Installing bundle [" + url + "]");
            Bundle bundle = context.installBundle(url.toExternalForm());
            installed.add(bundle);
        }

        for (Bundle bundle : installed) {
            bundle.start();
        }
    }

    public void stop(BundleContext context)
        throws Exception
    {
    }

    private List<URL> findBundles()
        throws Exception
    {
        ArrayList<URL> list = new ArrayList<URL>();
        for (Object o : this.servletContext.getResourcePaths("/WEB-INF/bundles/")) {
            String name = (String)o;
            if (name.endsWith(".jar")) {
                URL url = this.servletContext.getResource(name);
                if (url != null) {
                    list.add(url);
                }
            }
        }

        return list;
    }
}