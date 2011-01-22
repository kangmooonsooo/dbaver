/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.views.navigator.database;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNProject;
import org.jkiss.dbeaver.model.project.DBPProjectListener;

public class DatabaseNavigatorView extends NavigatorViewBase implements DBPProjectListener {
    public static final String VIEW_ID = "org.jkiss.dbeaver.core.databaseNavigator";

/*
    private static final String PROP_WEIGHT_TOP = "sash-top";
    private static final String PROP_WEIGHT_BOTTOM = "sash-bottom";

    private int[] weights = new int[] {70, 30};
    private SashForm sashForm;
*/

    public DatabaseNavigatorView()
    {
        super();
        DBeaverCore.getInstance().getProjectRegistry().addProjectListener(this);
    }

    private DBNProject getActiveProjectNode()
    {
        return getModel().getRoot().getProject(DBeaverCore.getInstance().getProjectRegistry().getActiveProject());
    }

    public DBNNode getRootNode()
    {
        return getActiveProjectNode().getDatabases();
    }

    @Override
    public void dispose()
    {
        DBeaverCore.getInstance().getProjectRegistry().removeProjectListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

/*
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        // Add combo to change active project
        IContributionItem projectSelectorItem = new ControlContribution("activeProject") {
            protected Control createControl(Composite parent)
            {
                Combo projectCombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
                projectCombo.add("sdfsdf");
                return projectCombo;
            }
        };
        toolBarManager.add(projectSelectorItem);
        toolBarManager.update(true);
        actionBars.updateActionBars();
*/
    }
/*
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);

        Integer weightTop = memento.getInteger(PROP_WEIGHT_TOP);
        Integer weightBottom = memento.getInteger(PROP_WEIGHT_BOTTOM);
        if (weightTop != null && weightBottom != null) {
            weights = new int[] {weightTop, weightBottom};
        }
    }

    @Override
    public void createPartControl(Composite parent)
    {
        sashForm = UIUtils.createPartDivider(this, parent, SWT.VERTICAL | SWT.SMOOTH);
        super.createPartControl(sashForm);

        DatabaseNavigatorTree projectNavigator = createNavigatorTree(sashForm, getActiveProjectNode());
        projectNavigator.getViewer().addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                return !(element instanceof DBNProjectDatabases);
            }
        });

        sashForm.setWeights(weights);
    }

    @Override
    public void saveState(IMemento memento)
    {
        int[] weights = sashForm.getWeights();
        memento.putInteger(PROP_WEIGHT_TOP, weights[0]);
        memento.putInteger(PROP_WEIGHT_BOTTOM, weights[1]);
        super.saveState(memento);
    }
*/

    public void handleActiveProjectChange(IProject oldValue, IProject newValue)
    {
        getNavigatorTree().reloadTree(getRootNode());
    }
}
