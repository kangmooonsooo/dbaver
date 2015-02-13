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
package org.jkiss.dbeaver.ui.dialogs.struct;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.ui.UIUtils;

public class CreateProcedureDialog extends TrayDialog {

    private DBPDataSource dataSource;
    private String name;
    private DBSProcedureType type;

    public CreateProcedureDialog(Shell shell, DBPDataSource dataSource)
    {
        super(shell);
        this.dataSource = dataSource;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        getShell().setText(CoreMessages.dialog_struct_create_procedure_title);
        Composite group = (Composite) super.createDialogArea(parent);
        GridData gd = new GridData(GridData.FILL_BOTH);
        group.setLayoutData(gd);

        Composite propsGroup = new Composite(group, SWT.NONE);
        propsGroup.setLayout(new GridLayout(2, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        propsGroup.setLayoutData(gd);

        final Text nameText = UIUtils.createLabelText(propsGroup, CoreMessages.dialog_struct_create_procedure_label_name, ""); //$NON-NLS-2$
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                name = nameText.getText();
            }
        });
        final Combo typeCombo = UIUtils.createLabelCombo(propsGroup, CoreMessages.dialog_struct_create_procedure_combo_type, SWT.DROP_DOWN | SWT.READ_ONLY);
        typeCombo.add(DBSProcedureType.PROCEDURE.name());
        typeCombo.add(DBSProcedureType.FUNCTION.name());
        typeCombo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                type = typeCombo.getSelectionIndex() == 0 ? DBSProcedureType.PROCEDURE : DBSProcedureType.FUNCTION;
                nameText.setText(type == DBSProcedureType.PROCEDURE ? "NewProcedure" : "NewFunction"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
        typeCombo.select(0);
        return group;
    }

    public DBSProcedureType getProcedureType()
    {
        return type;
    }

    public String getProcedureName()
    {
        return DBObjectNameCaseTransformer.transformName(dataSource, name);
    }
}