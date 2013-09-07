/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *       Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.aerogear.hybrid.ui.plugins.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jboss.tools.aerogear.hybrid.core.HybridCore;
import org.jboss.tools.aerogear.hybrid.core.HybridProject;
import org.jboss.tools.aerogear.hybrid.core.platform.PlatformConstants;
import org.jboss.tools.aerogear.hybrid.core.plugin.registry.CordovaRegistryPluginInfo;
import org.jboss.tools.aerogear.hybrid.ui.HybridUI;
import org.jboss.tools.aerogear.hybrid.ui.wizard.export.DirectorySelectionGroup;

public class CordovaPluginSelectionPage extends WizardPage {

	static final int PLUGIN_SOURCE_REGISTRY =1;
	static final int PLUGIN_SOURCE_GIT =2;
	static final int PLUGIN_SOURCE_DIRECTORY =3;
	
	private final IStructuredSelection initialSelection;
	private TabFolder tabFolder;
	private TabItem registryTab;
	private CordovaPluginCatalogViewer catalogViewer;
	private TabItem gitTab;
	private TabItem directoryTab;
	private DirectorySelectionGroup destinationDirectoryGroup;
	private Text textProject;
	private Group grpRepositoryUrl;
	private Text gitUrlTxt;

	protected CordovaPluginSelectionPage(String pageName,IStructuredSelection selection) {
		super(pageName);
		this.initialSelection = selection;
		setImageDescriptor(HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, CordovaPluginWizard.IMAGE_WIZBAN));
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Group grpProject = new Group(container, SWT.NONE);
		grpProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpProject.setText("Project");
		grpProject.setLayout(new GridLayout(3, false));
		
		Label lblProject = new Label(grpProject, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProject.setText("Project:");
		
		textProject = new Text(grpProject, SWT.BORDER);
		textProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textProject.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		
		
		Button btnProjectBrowse = new Button(grpProject, SWT.NONE);
		btnProjectBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog es = new ElementListSelectionDialog(getShell(), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
				es.setElements(HybridCore.getHybridProjects().toArray());
				es.setTitle("Project Selection");
				es.setMessage("Select a project to run");
				if (es.open() == Window.OK) {			
					HybridProject project = (HybridProject) es.getFirstResult();
					textProject.setText(project.getProject().getName());
				}		
			}
		});
		btnProjectBrowse.setText("Browse...");
		
		
		tabFolder = new TabFolder(container, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tabFolder);
		
		registryTab = new TabItem(tabFolder, SWT.NONE);
		registryTab.setText("Registry");
		catalogViewer = new CordovaPluginCatalogViewer();
		catalogViewer.createControl(tabFolder);
		catalogViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(validatePage());
			}
		});
		registryTab.setControl(catalogViewer.getControl());
		
		gitTab = new TabItem(tabFolder, SWT.NONE);
		gitTab.setText("Git");
		
		grpRepositoryUrl = new Group(tabFolder, SWT.NONE);
		grpRepositoryUrl.setText("Repository");
		gitTab.setControl(grpRepositoryUrl);
		grpRepositoryUrl.setLayout(new GridLayout(2, false));
		
		Label lblUrl = new Label(grpRepositoryUrl, SWT.NONE);
		lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText("URL:");
		
		gitUrlTxt = new Text(grpRepositoryUrl, SWT.BORDER);
		gitUrlTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		
		directoryTab = new TabItem(tabFolder, SWT.NONE);
		directoryTab.setText("Directory");
		
		destinationDirectoryGroup = new DirectorySelectionGroup(tabFolder, SWT.NONE);
		destinationDirectoryGroup.setText("Plugin:");
		destinationDirectoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		destinationDirectoryGroup.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}


		});
		directoryTab.setControl(destinationDirectoryGroup);
		tabFolder.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		setupFromInitialSelection();
		restoreWidgetValues();
	}
	
	private void setupFromInitialSelection() {
		if(initialSelection != null && !initialSelection.isEmpty()){
			Iterator<?> selects = initialSelection.iterator();
			while (selects.hasNext()) {
				Object obj  = selects.next();
				if(obj instanceof IResource ){
					IResource res = (IResource)obj;
					IProject project = res.getProject();
					HybridProject hybrid = HybridProject.getHybridProject(project);
					if(hybrid != null ){
						textProject.setText(project.getName());
					}
				}
			}
		}
	}
	
	@Override
	public IWizardPage getNextPage() {
		if( getSelectedTabItem()!=registryTab){
			return null;
		}
		CordovaPluginWizard wiz = (CordovaPluginWizard) getWizard();
		RegistryConfirmPage confirmPage = wiz.getRegistryConfirmPage();
		confirmPage.setSelectedPlugins(getCheckedCordovaRegistryItems());
		return super.getNextPage();
	}
	
	
	
	public int getPluginSourceType(){
		TabItem selected = getSelectedTabItem();
		if(selected == gitTab )
			return PLUGIN_SOURCE_GIT;
		if(selected == directoryTab )
			return PLUGIN_SOURCE_DIRECTORY;
		return PLUGIN_SOURCE_REGISTRY; // defaults to registry;
	}
	
	public String getSelectedDirectory(){
		return this.destinationDirectoryGroup.getValue();
	}
	
	public String getSpecifiedGitURL(){
		return this.gitUrlTxt.getText();
	}
	
	public String getProjectName(){
		return textProject.getText();
	}
	
	private List<CordovaRegistryPluginInfo> getCheckedCordovaRegistryItems(){
		IStructuredSelection selection = catalogViewer.getSelection();
		if(selection == null || selection.isEmpty())
			return Collections.emptyList();
		return selection.toList();
	}
	
	private TabItem getSelectedTabItem(){
		TabItem[] selections = tabFolder.getSelection();
		Assert.isTrue(selections.length>0);
		return selections[0];
	}
	
	private boolean validatePage() {
		//Check project
		String projectName = textProject.getText();
		if(projectName == null || projectName.isEmpty()){
			setMessage("Specify a project",ERROR);
			return false;
		}
		List<HybridProject> projects = HybridCore.getHybridProjects();
		boolean projectValid = false;
		for (HybridProject hybridProject : projects) {
			if (hybridProject.getProject().getName().equals(projectName)) {
				projectValid = true;
				break;
			}
		}
		if (!projectValid) {
			setMessage(
					"Specified project is not a valid project for this operation",
					ERROR);
			return false;
		}
		
		//Now tabs
		
		boolean valid = false;
		switch (getPluginSourceType()) {
		case PLUGIN_SOURCE_DIRECTORY:
			valid = validateDirectroyTab();
			break;
		case PLUGIN_SOURCE_GIT:	
			valid = validateGitTab(); 
			break;
		case PLUGIN_SOURCE_REGISTRY:
			valid = validateRegistryTab();
			break;
		}
		if(valid){
			setMessage(null,NONE);
		}
		return valid;
	}
	
	private boolean validateRegistryTab() {
		List<CordovaRegistryPluginInfo> infos = getCheckedCordovaRegistryItems();
		if (infos.isEmpty()){
			setMessage("Specify Cordova plugin(s) for installation", ERROR);
			return false;
		}
		return true;
	}

	private boolean validateGitTab(){
		String url = gitUrlTxt.getText();
		if( url == null || url.isEmpty() ){
			setMessage("Specify a git repository for fetching the Cordova plugin",ERROR);
			return false;
		}
		try {
			new URI(url);
		} catch (URISyntaxException e) {
			setMessage("Specify a valid address",ERROR);
			return false;
		}
		return true;
	}
	
	private boolean validateDirectroyTab(){
		String directory = destinationDirectoryGroup.getValue();
		if(directory == null || directory.isEmpty() ){
			setMessage("Select the directory for the Cordova plugin",ERROR);
			return false;
		}
		File pluginFile = new File(directory);
		if(!DirectorySelectionGroup.isValidDirectory(pluginFile)){
			setMessage(directory +" is not a valid directory",ERROR);
			return false;
		}
		if(!pluginFile.isDirectory()){
			setMessage("Select an existing directory", ERROR);
			return false;
		}
		File pluginXML = new File(pluginFile, PlatformConstants.FILE_XML_PLUGIN);
		if(!pluginXML.isFile()){
			setMessage("Specified directory is not a valid plugin directory",ERROR);
			return false;
		}
		return true;
		
	}
	
	void saveWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.saveHistory(settings);
		}
	}
	
	private void restoreWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.restoreHistory(settings);
		}
	}
}