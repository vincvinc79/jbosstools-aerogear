package org.jboss.tools.aerogear.hybrid.core.plugin.test;

import java.util.List;

import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPlugin;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPluginInfo;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaPluginRegistryManager;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPluginVersion;

import static org.junit.Assert.*;

import org.junit.Test;

public class CordovaPluginRegistryTest {
	
	@Test
	public void testRetrievePluginInfosFromCordovaRegistry(){
		CordovaPluginRegistryManager client = getCordovaIORegistryClient();
		List<CordovaRegistryPluginInfo> infos = client.retrievePluginInfos();
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		CordovaRegistryPluginInfo info = infos.get(0);
		assertNotNull(info.getName());
	}

	@Test
	public void testReadCordovaPluginFromCordovaRegistry(){
		CordovaPluginRegistryManager client = getCordovaIORegistryClient();
		List<CordovaRegistryPluginInfo> infos = client.retrievePluginInfos();
		CordovaRegistryPluginInfo info = infos.get(0);
		CordovaRegistryPlugin plugin = client.getCordovaPluginInfo(info.getName());
		assertNotNull(plugin);
		assertNotNull(plugin.getName());
		assertEquals(info.getName(), plugin.getName());
		assertNotNull(plugin.getVersions());
		List<CordovaRegistryPluginVersion> versions = plugin.getVersions();
		assertFalse(versions.isEmpty());
		CordovaRegistryPluginVersion version = versions.get(0);
		assertNotNull(version.getName());
		assertNotNull(version.getVersionNumber());
		assertNotNull(version.getDistributionSHASum());
		assertNotNull(version.getDistributionTarball());
	}

	private CordovaPluginRegistryManager getCordovaIORegistryClient() {
		CordovaPluginRegistryManager client = new CordovaPluginRegistryManager("http://registry.cordova.io/");
		return client;
	}
}
