package org.jboss.tools.aerogear.hybrid.ui.plugins.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.dsig.keyinfo.PGPData;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPlugin;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPluginInfo;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaPluginRegistryManager;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPluginVersion;
import org.jboss.tools.aerogear.hybrid.ui.HybridUI;

public class RegistryConfirmPage extends WizardPage {

	private CordovaPluginViewer pluginViewer;

	protected RegistryConfirmPage(String pageName) {
		super(pageName);
		setImageDescriptor(HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, CordovaPluginWizard.IMAGE_WIZBAN));

	}

	@Override
	public void createControl(Composite parent) {
		pluginViewer = new CordovaPluginViewer();
		pluginViewer.createControl(parent);
		setControl(pluginViewer.getControl());
	}
	
	void setSelectedPlugins(List<CordovaRegistryPluginInfo> selected){
		CordovaPluginRegistryManager client = new CordovaPluginRegistryManager("http://registry.cordova.io");
		ArrayList<CordovaRegistryPlugin> plugins = new ArrayList<CordovaRegistryPlugin>();
		for (CordovaRegistryPluginInfo cordovaPluginInfo : selected) {
			plugins.add(client.getCordovaPluginInfo(cordovaPluginInfo.getName()));
		}
		pluginViewer.getViewer().setInput(plugins);
	}
	
	public List<CordovaRegistryPluginVersion> getSelectedPluginVersions(){
			IStructuredSelection selection = (IStructuredSelection) pluginViewer.getViewer().getSelection();
			if(selection == null || selection.isEmpty())
				return Collections.emptyList();
			List<CordovaRegistryPluginVersion> versions = new ArrayList<CordovaRegistryPluginVersion>();
			Iterator<CordovaRegistryPlugin> iterator = selection.iterator();
			while (iterator.hasNext()) {
				CordovaRegistryPlugin cordovaRegistryPlugin = (CordovaRegistryPlugin) iterator
						.next();
				//TODO: add the selected version
				versions.add(cordovaRegistryPlugin.getVersions().get(0));
			}
			return versions;
	}

}
