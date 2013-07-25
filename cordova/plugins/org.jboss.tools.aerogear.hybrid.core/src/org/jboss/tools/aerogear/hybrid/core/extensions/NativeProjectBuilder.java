/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.aerogear.hybrid.core.extensions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.aerogear.hybrid.core.HybridCore;
import org.jboss.tools.aerogear.hybrid.core.platform.AbstractNativeBinaryBuildDelegate;

public class NativeProjectBuilder extends ExtensionPointProxy{
	
	public static final String EXTENSION_POINT_ID = "org.jboss.tools.aerogear.hybrid.core.projectBuilder";
	public static final String ATTR_PLATFORM = "platform";
	public static final String ATTR_DELEGATE = "delegate";
	public static final String ATTR_ID="id";

	private String id;
	private String platform;

	NativeProjectBuilder(IConfigurationElement element) {
		super(element);
		this.id = element.getAttribute(ATTR_ID);
		this.platform = element.getAttribute(ProjectGenerator.ATTR_PLATFORM);

	}
	
	public String getPlatform() {
		return platform;
	}
	
	public String getID(){
		return id;
	}
	
	public AbstractNativeBinaryBuildDelegate createDelegate(IProject project, File destination) throws CoreException{
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensions(contributor);
		if(extensions == null )
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform is no longer available."));
		for (int i = 0; i < extensions.length; i++) {
			if(extensions[i].getExtensionPointUniqueIdentifier().equals(EXTENSION_POINT_ID)){
				IConfigurationElement[] configs = extensions[i].getConfigurationElements();
				for (int j = 0; j < configs.length; j++) {
					if(configs[j].getAttribute(ATTR_PLATFORM).equals(getPlatform())){
						AbstractNativeBinaryBuildDelegate delegate = (AbstractNativeBinaryBuildDelegate) configs[j].createExecutableExtension(ATTR_DELEGATE);
						delegate.init(project, destination);
						return delegate;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform has changed"));
	}
}