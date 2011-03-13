/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.erd.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.erd.model.DiagramLoader;
import org.jkiss.dbeaver.ext.erd.model.EntityDiagram;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.load.AbstractLoadService;
import org.jkiss.dbeaver.runtime.load.LoadingUtils;
import org.jkiss.dbeaver.ui.UIUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Standalone ERD editor
 */
public class ERDEditorStandalone extends ERDEditorPart {

    /**
     * No-arg constructor
     */
    public ERDEditorStandalone()
    {
    }

    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        loadDiagram();
    }

    public void doSave(IProgressMonitor monitor)
    {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DiagramLoader.save(getDiagramPart(), buffer);

            final IFile file = getEditorFile();
            file.setContents(new ByteArrayInputStream(buffer.toByteArray()), true, true, monitor);

            getCommandStack().markSaveLocation();
        } catch (Exception e) {
            UIUtils.showErrorDialog(getSite().getShell(), "Save diagram", null, e);
        }
    }

/*
    protected void createActions()
    {
        super.createActions();

        //addEditorAction(new SaveAction(this));
    }
*/

    protected synchronized void loadDiagram()
    {
        if (diagramLoadingJob != null) {
            // Do not start new one while old is running
            return;
        }
        diagramLoadingJob = LoadingUtils.createService(
            new AbstractLoadService<EntityDiagram>("Load diagram '" + getEditorInput().getName() + "'") {
                public EntityDiagram evaluate()
                    throws InvocationTargetException, InterruptedException
                {
                    try {
                        return loadContentFromFile(getProgressMonitor());
                    } catch (DBException e) {
                        log.error(e);
                    }

                    return null;
                }

                public Object getFamily()
                {
                    return ERDEditorStandalone.this;
                }
            },
            progressControl.createLoadVisualizer());
        diagramLoadingJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event)
            {
                diagramLoadingJob = null;
            }
        });
        diagramLoadingJob.schedule();

        setPartName(getEditorInput().getName());
    }

    private EntityDiagram loadContentFromFile(DBRProgressMonitor progressMonitor)
        throws DBException
    {
/*
        EntityDiagram entityDiagram = null;
        IFile file = input.getFile();
        try {
            setPartName(file.getName());

            InputStream is = file.getContents(true);
            ObjectInputStream ois = new ObjectInputStream(is);
            entityDiagram = (EntityDiagram) ois.readObject();
            ois.close();
        }
        catch (Exception e) {
            log.error("Error loading diagram from file '" + file.getFullPath().toString() + "'", e);
            entityDiagram = new EntityDiagram(null, file.getName());
        }
        return entityDiagram;
*/
        EntityDiagram entityDiagram = new EntityDiagram(null, getEditorInput().getName());
        entityDiagram.setLayoutManualAllowed(true);
        entityDiagram.setLayoutManualDesired(true);
        return entityDiagram;
    }

    private IFile getEditorFile()
    {
        return (IFile) getEditorInput().getAdapter(IFile.class);
    }

/*
    public DBNNode getRootNode() {
        IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IDatabaseNodeEditorInput) {
            return ((IDatabaseNodeEditorInput)editorInput).getTreeNode();
        }
        return null;
    }

    public Viewer getNavigatorViewer() {
        return null;
    }

    public IWorkbenchPart getWorkbenchPart() {
        return this;
    }
*/

}
