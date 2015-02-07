/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.dialogs.connection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.ui.dialogs.ActiveWizardDialog;

/**
 * NewConnectionDialog
 */
public class ConnectionDialog extends ActiveWizardDialog
{

    public static final int TEST_BUTTON_ID = 2000;
    private Button testButton;

    public ConnectionDialog(IWorkbenchWindow window, ConnectionWizard wizard)
    {
        super(window, wizard);
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
//        DataSourceDescriptor ds = ((ConnectionWizard)getWizard()).getDataSourceDescriptor();
//        if (ds != null) {
//            getShell().setImage(ds.getDriver().getIcon());
//        } else {
//            getShell().setImage(DBIcon.GEN_DATABASE.getImage());
//        }
        return super.createDialogArea(parent);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        super.createButtonsForButtonBar(parent);
        testButton = createButton(parent, TEST_BUTTON_ID, CoreMessages.dialog_connection_button_test, false);
        testButton.setEnabled(false);
        testButton.moveAbove(getButton(IDialogConstants.BACK_ID));
    }

    @Override
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == TEST_BUTTON_ID) {
            testConnection();
            return;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    public void updateButtons()
    {
        ConnectionWizard wizard = (ConnectionWizard) getWizard();
        ConnectionPageSettings settings = wizard.getPageSettings();
        testButton.setEnabled(settings != null && settings.isPageComplete());
        super.updateButtons();
    }

    private void testConnection()
    {
        ConnectionWizard wizard = (ConnectionWizard) getWizard();
        wizard.getPageSettings().saveSettings();
        wizard.testConnection(wizard.getPageSettings().getConnectionInfo());
    }

}