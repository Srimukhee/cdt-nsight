/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Warren Paul (Nokia) - bug 200420.
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.internal.ui.cview.IncludeReferenceProxy;

/**
 * Base class for build configuration actions. 
 */
public class ChangeBuildConfigActionBase {
	
	/**
	 * List of selected managed-built projects
	 */
	protected HashSet fProjects = new HashSet();
	
	/**
	 * Fills the menu with build configurations which are common for all selected projects
	 * @param menu The menu to fill
	 */
	protected void fillMenu(Menu menu)	{
		// This should not happen 
		if (menu == null) return;

		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) items[i].dispose();
		
		List configNames = new ArrayList();
		Iterator projIter = fProjects.iterator();
		String sCurrentConfig = null;
		boolean bCurrentConfig = true;
		while (projIter.hasNext()) {
			ICConfigurationDescription[] cfgDescs = getCfgs((IProject)projIter.next());

			String sActiveConfig = null;
			// Store names and detect active configuration
			for (int i=0; i<cfgDescs.length; i++) {
				String s = cfgDescs[i].getName();
				if (!configNames.contains(s)) 
					configNames.add(s);
				if (cfgDescs[i].isActive())	
					sActiveConfig = s;
			}

			// Check whether all projects have the same active configuration
			if (bCurrentConfig) {
				if (sCurrentConfig == null)
					sCurrentConfig = sActiveConfig;
				else {
					if (!sCurrentConfig.equals(sActiveConfig)) 
						bCurrentConfig = false;
				}
			}
		}
		
		Iterator confIter = configNames.iterator();
		int accel = 0;
		while (confIter.hasNext()) {
			String sName = (String)confIter.next();
			String sDesc = null;
			projIter = fProjects.iterator();
			boolean commonName = true;
			boolean commonDesc = true;
			boolean firstProj = true;
			while (projIter.hasNext()) {
				ICConfigurationDescription[] cfgDescs = getCfgs((IProject)projIter.next());
				int i = 0;
				for (; i < cfgDescs.length; i++) {
					if (cfgDescs[i].getName().equals(sName)) {
						String sNewDesc = cfgDescs[i].getDescription();
						if (sNewDesc != null && sNewDesc.length() == 0) {
							sNewDesc = null;
						}
						if (commonDesc) {
							if (firstProj) {
								sDesc = sNewDesc;
								firstProj = false;
							} else if (sNewDesc == null && sDesc != null || sNewDesc != null && !sNewDesc.equals(sDesc)) {
								commonDesc = false;	
							}
						}
						break;
					}
				}
				if (i == cfgDescs.length) {
					commonName = false;
					break;
				}
			}
			if (commonName) {
				StringBuffer builder = new StringBuffer(sName);
				if (commonDesc) {
					if (sDesc != null) {
						builder.append(" (");	//$NON-NLS-1$
						builder.append(sDesc);
						builder.append(")");	//$NON-NLS-1$
					}
				} else {
					builder.append(" (...)");	//$NON-NLS-1$
				}
					
				IAction action = makeAction(sName ,builder, accel);
				if (bCurrentConfig && sCurrentConfig.equals(sName)) {
					action.setChecked(true);
				}
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(menu, -1);
				accel++;
			}
		}
	}

	protected IAction makeAction(String sName, StringBuffer builder, int accel) {
		return new ChangeConfigAction(fProjects, sName, builder.toString(), accel + 1);
	}

	/**
	 * selectionChanged() event handler. Fills the list of managed-built projects 
	 * based on the selection. If some non-managed-built projects are selected,
	 * disables the action. 
	 * @param action The action
	 * @param selection The selection
	 */
	protected void onSelectionChanged(IAction action, ISelection selection) {
		fProjects.clear();

		boolean badObject = false;
		
		if (selection != null )
		{
			if (selection instanceof IStructuredSelection) {
				if (selection.isEmpty()) {
					// could be a form editor or something.  try to get the project from the active part
					IWorkbenchPage page = CUIPlugin.getActivePage();
					if (page != null) {
						IWorkbenchPart part = page.getActivePart();
						if (part != null) {
							Object o = part.getAdapter(IResource.class);
							if (o != null && o instanceof IResource) {
								fProjects.add(((IResource)o).getProject());
							}
						}
					}
				}
				Iterator iter = ((IStructuredSelection)selection).iterator();
				while (iter.hasNext()) {
					Object selItem = iter.next();
					IProject project = null;
					if (selItem instanceof ICElement) {
						ICProject cproject = ((ICElement)selItem).getCProject();
						if (cproject != null) project = cproject.getProject();
					}
					else if (selItem instanceof IResource) {
						project = ((IResource)selItem).getProject();
					} else if (selItem instanceof IncludeRefContainer) {
						ICProject fCProject = ((IncludeRefContainer)selItem).getCProject();
						if (fCProject != null)
							project = fCProject.getProject();
					} else if (selItem instanceof IncludeReferenceProxy) {
						IncludeRefContainer irc = ((IncludeReferenceProxy)selItem).getIncludeRefContainer();
						if (irc != null) {
							ICProject fCProject = irc.getCProject();
							if (fCProject != null)
								project = fCProject.getProject();
						}
					} else if (selItem instanceof IAdaptable) {
						Object adapter = ((IAdaptable)selItem).getAdapter(IProject.class);
						if (adapter != null && adapter instanceof IProject) {
							project = (IProject)adapter;
						}
					}
					// Check whether the project is CDT project
					if (project != null) {
						if (!CoreModel.getDefault().isNewStyleProject(project))
							project = null;
						else {
							ICConfigurationDescription[] tmp = getCfgs(project);
							if (tmp == null || tmp.length == 0)	project = null;
						}
					}
					if (project != null) {
						fProjects.add(project);
					} else {
						badObject = true;
						break;
					}
				}
			}
			else
			if (selection instanceof ITextSelection) {
				// If a text selection check the selected part to see if we can find
				// an editor part that we can adapt to a resource and then
				// back to a project.
				IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							IWorkbenchPart part = page.getActivePart();
							if (part instanceof IEditorPart) {
								IEditorPart epart = (IEditorPart) part;
								IResource resource = (IResource) epart.getEditorInput().getAdapter(IResource.class);
								if (resource != null)
								{
									IProject project = resource.getProject();
									badObject = !(project != null && CoreModel.getDefault().isNewStyleProject(project));

									if (!badObject) {
										fProjects.add(project);
									}									
								}
							}
						}
					}
				}

			}
		}
		
		
		boolean enable = false;
		if (!badObject && !fProjects.isEmpty()) {
			Iterator iter = fProjects.iterator();
			ICConfigurationDescription[] firstConfigs = getCfgs((IProject)iter.next());
			for (int i = 0; i < firstConfigs.length; i++) {
				boolean common = true;
				Iterator iter2 = fProjects.iterator();
				while (iter2.hasNext()) {
					ICConfigurationDescription[] currentConfigs = getCfgs((IProject)iter2.next());
					int j = 0;
					for (; j < currentConfigs.length; j++) {
						if (firstConfigs[i].getName().equals(currentConfigs[j].getName())) 
							break;
					}
					if (j == currentConfigs.length) {
						common = false;
						break;
					}
				}
				if (common) {
					enable = true;
					break;
				}
			}
		}
		action.setEnabled(enable);
	}
	
	private ICConfigurationDescription[] getCfgs(IProject prj) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
		if (prjd == null) return null;
		ICConfigurationDescription[] tmp = prjd.getConfigurations();
		if (tmp == null) return null;
		return prjd.getConfigurations(); 
	}
	
	
}
